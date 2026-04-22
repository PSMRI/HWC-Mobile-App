package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.OralHealthDao
import org.piramalswasthya.cho.model.OralHealth
import javax.inject.Inject
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.model.OralHealthNetwork
import org.piramalswasthya.cho.model.toCacheModel
import org.piramalswasthya.cho.model.toNetworkModel
import org.piramalswasthya.cho.network.AmritApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.VillageIdList
import java.net.SocketTimeoutException
import org.json.JSONObject
import timber.log.Timber

class OralHealthRepo @Inject constructor(
    private val oralHealthDao: OralHealthDao,
    private val patientDao: PatientDao,
    private val amritApiService: AmritApiService,
    private val userRepo: UserRepo,
    private val prefDao: PreferenceDao
) {
    private fun convertStringToIntList(villageIds: String): List<Int> {
        if (villageIds.trim().isEmpty()) {
            return emptyList()
        }
        return villageIds.split(",").map {
            it.trim().toInt()
        }
    }

    suspend fun save(oralHealth: OralHealth) {
        if (oralHealth.oralHealthId == 0L) {
            oralHealthDao.insert(oralHealth)
        } else {
            oralHealthDao.update(oralHealth)
        }
    }

    suspend fun getByPatientId(patientID: String): OralHealth? {
        return oralHealthDao.getByPatientId(patientID)
    }

    suspend fun getByPatientIdAndVisitNo(
        patientID: String,
        benVisitNo: Int
    ): OralHealth? {
        return oralHealthDao.getByPatientIdAndVisitNo(patientID, benVisitNo)
    }
    suspend fun processOralVisits(): Boolean {
        return withContext(Dispatchers.IO) {
            val unsyncedList = oralHealthDao.getUnsyncedAssessments()
            if (unsyncedList.isEmpty()) return@withContext true

            val networkList = mutableListOf<OralHealthNetwork>()
            val sentAssessments = mutableListOf<OralHealth>()
            unsyncedList.forEach { assessment ->
                val patient = patientDao.getPatient(assessment.patientID)
                if (patient?.beneficiaryID != null && patient.beneficiaryRegID != null) {
                    networkList.add(
                        assessment.toNetworkModel(
                            beneficiaryID = patient.beneficiaryID.toString(),
                            beneficiaryRegID = patient.beneficiaryRegID.toString()
                        )
                    )
                    sentAssessments.add(assessment)
                }
            }

            if (networkList.isEmpty()) return@withContext false

            try {
                val response = amritApiService.postOralForm(networkList)
                if (response.isSuccessful) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)
                        if (jsonObj.optInt("statusCode") == 200) {
                            sentAssessments.forEach {
                                it.syncState = SyncState.SYNCED.ordinal
                                oralHealthDao.update(it)
                            }
                            return@withContext true
                        }
                    }
                }
                return@withContext false
            } catch (e: Exception) {
                Timber.e(e, "Error syncing Oral Health records")
                return@withContext false
            }
        }
    }

    suspend fun pullOralVisitsFromServer(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userRepo.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")
            val maxAttempts = 3
            var backoffMs = 500L

            repeat(maxAttempts) { attempt ->
                try {
                    val villageList = VillageIdList(
                        convertStringToIntList(user.assignVillageIds ?: ""),
                        prefDao.getLastPatientSyncTime()
                    )
                    val response = amritApiService.getOralVisits(villageList)
                    if (response.isSuccessful) {
                        val responseString = response.body()?.string()
                        if (responseString != null) {
                            val jsonObj = JSONObject(responseString)
                            if (jsonObj.isNull("statusCode"))
                                throw IllegalStateException("Amrit server not responding properly")
                            val responseStatusCode = jsonObj.getInt("statusCode")
                            if (responseStatusCode == 200) {
                                val dataArray = jsonObj.optJSONArray("data")
                                if (dataArray != null) {
                                    val gson = Gson()
                                    for (i in 0 until dataArray.length()) {
                                        val networkObj = gson.fromJson(
                                            dataArray.getJSONObject(i).toString(),
                                            OralHealthNetwork::class.java
                                        )
                                        val patient = networkObj.beneficiaryRegID.toLongOrNull()?.let {
                                            patientDao.getPatientByBenRegId(it)
                                        }
                                        if (patient != null) {
                                            val existing = getByPatientIdAndVisitNo(
                                                patient.patientID,
                                                networkObj.benVisitNo ?: 0
                                            )
                                            if (existing == null) {
                                                val cache = networkObj.toCacheModel(
                                                    patientID = patient.patientID
                                                ).copy(syncState = SyncState.SYNCED.ordinal)
                                                save(cache)
                                            }
                                        }
                                    }
                                    return@withContext true
                                }
                            } else if (responseStatusCode == 5002) {
                                val tokenRefreshed = userRepo.refreshTokenTmc(user.userName, user.password)
                                if (!tokenRefreshed) {
                                    Timber.w("Token refresh failed while pulling Oral Health records")
                                    return@withContext false
                                }
                                if (attempt < maxAttempts - 1) {
                                    delay(backoffMs)
                                    backoffMs *= 2
                                    return@repeat
                                }
                                Timber.w("Max retry attempts reached while pulling Oral Health records")
                                return@withContext false
                            } else if (responseStatusCode == 5000) {
                                return@withContext true
                            }
                        }
                    }
                } catch (e: SocketTimeoutException) {
                    if (attempt < maxAttempts - 1) {
                        delay(backoffMs)
                        backoffMs *= 2
                        return@repeat
                    }
                    Timber.e(e, "Socket timeout while pulling Oral Health records after retries")
                    return@withContext false
                } catch (e: Exception) {
                    Timber.e(e, "Error pulling Oral Health records")
                    return@withContext false
                }
                return@withContext false
            }

            return@withContext false
        }
    }

}

