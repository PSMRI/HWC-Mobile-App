package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.OphthalmicDao
import org.piramalswasthya.cho.model.OphthalmicVisit
import javax.inject.Inject
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.model.OphthalmicNetwork
import org.piramalswasthya.cho.model.toCacheModel
import org.piramalswasthya.cho.model.toNetworkModel
import org.piramalswasthya.cho.network.AmritApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.VillageIdList
import org.piramalswasthya.cho.utils.generateUuid
import java.net.SocketTimeoutException
import org.json.JSONObject
import timber.log.Timber

class OphthalmicRepository @Inject constructor(
    private val ophthalmicDao: OphthalmicDao,
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
    suspend fun getOphthalmicVisit(patientID: String, benVisitNo: Int): OphthalmicVisit? {
        return ophthalmicDao.getOphthalmicVisit(patientID, benVisitNo)
    }

    suspend fun saveOphthalmicVisit(visit: OphthalmicVisit) {
        if (visit.visitId.isNotEmpty()) {
             val existing = ophthalmicDao.getOphthalmicVisitById(visit.visitId)
             if (existing == null) {
                 ophthalmicDao.insertOphthalmicVisit(visit)
             } else {
                 ophthalmicDao.updateOphthalmicVisit(visit)
             }
        } else {
            ophthalmicDao.insertOphthalmicVisit(visit)
        }
    }
    
    suspend fun updateOphthalmicVisit(visit: OphthalmicVisit) {
        ophthalmicDao.updateOphthalmicVisit(visit)
    }
    suspend fun processOphthalmicVisits(): Boolean {
        return withContext(Dispatchers.IO) {
            val unsyncedList = ophthalmicDao.getUnsyncedOphthalmicVisits()
            if (unsyncedList.isEmpty()) return@withContext true

            val networkList = mutableListOf<OphthalmicNetwork>()
            val sentAssessments = mutableListOf<OphthalmicVisit>()
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
                val response = amritApiService.postOphthalmicForm(networkList)
                if (response.isSuccessful) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)
                        if (jsonObj.optInt("statusCode") == 200) {
                            sentAssessments.forEach {
                                it.syncState = SyncState.SYNCED.ordinal
                                ophthalmicDao.updateOphthalmicVisit(it)
                            }
                            return@withContext true
                        }
                    }
                }
                return@withContext false
            } catch (e: Exception) {
                Timber.e(e, "Error syncing Ophthalmic records")
                return@withContext false
            }
        }
    }

    suspend fun pullOphthalmicVisitsFromServer(): Boolean {
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
                    val response = amritApiService.getOphthalmicVisits(villageList)
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
                                            OphthalmicNetwork::class.java
                                        )
                                        val patient = networkObj.beneficiaryRegID.toLongOrNull()?.let {
                                            patientDao.getPatientByBenRegId(it)
                                        }
                                        if (patient != null) {
                                            val visitNo = networkObj.benVisitNo ?: continue
                                            val existing = getOphthalmicVisit(
                                                patient.patientID,
                                                visitNo
                                            )
                                            if (existing == null) {
                                                val visitId = if (networkObj.visitId.isNullOrBlank()) generateUuid() else networkObj.visitId
                                                val cache = networkObj.toCacheModel(patient.patientID).copy(
                                                    visitId = visitId ?: generateUuid(),
                                                    patientID = patient.patientID,
                                                    syncState = SyncState.SYNCED.ordinal
                                                )
                                                saveOphthalmicVisit(cache)
                                            }
                                        }
                                    }
                                    return@withContext true
                                }
                            } else if (responseStatusCode == 5002) {
                                val tokenRefreshed = userRepo.refreshTokenTmc(user.userName, user.password)
                                if (!tokenRefreshed) {
                                    Timber.w("Token refresh failed while pulling Ophthalmic records")
                                    return@withContext false
                                }
                                if (attempt < maxAttempts - 1) {
                                    delay(backoffMs)
                                    backoffMs *= 2
                                    return@repeat
                                }
                                Timber.w("Max retry attempts reached while pulling Ophthalmic records")
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
                    Timber.e(e, "Socket timeout while pulling Ophthalmic records after retries")
                    return@withContext false
                } catch (e: Exception) {
                    Timber.e(e, "Error pulling Ophthalmic records")
                    return@withContext false
                }
                return@withContext false
            }
            return@withContext false
        }
    }
}
