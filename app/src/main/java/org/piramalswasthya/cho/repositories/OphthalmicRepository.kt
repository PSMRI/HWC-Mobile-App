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
import org.json.JSONArray

class OphthalmicRepository @Inject constructor(
    private val ophthalmicDao: OphthalmicDao,
    private val patientDao: PatientDao,
    private val amritApiService: AmritApiService,
    private val userRepo: UserRepo,
    private val prefDao: PreferenceDao
) {
    private companion object {
        const val STATUS_OK = 200
        const val STATUS_TOKEN_EXPIRED = 5002
        const val STATUS_NO_RECORDS = 5000
        const val MAX_PULL_ATTEMPTS = 3
    }
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
            try {
                val unsyncedList = ophthalmicDao.getUnsyncedOphthalmicVisits()
                if (unsyncedList.isEmpty()) return@withContext true

                val payload = unsyncedList.mapNotNull { assessment ->
                    val patient = patientDao.getPatient(assessment.patientID)
                    val benId = patient?.beneficiaryID ?: return@mapNotNull null
                    val benRegId = patient.beneficiaryRegID ?: return@mapNotNull null
                    assessment to assessment.toNetworkModel(
                        beneficiaryID = benId.toString(),
                        beneficiaryRegID = benRegId.toString()
                    )
                }

                if (payload.isEmpty()) return@withContext false

                val response = amritApiService.postOphthalmicForm(payload.map { it.second })
                val statusCode = response.body()?.string()?.let(::JSONObject)?.optInt("statusCode")
                if (response.isSuccessful && statusCode == STATUS_OK) {
                    payload.forEach { (assessment, _) ->
                        assessment.syncState = SyncState.SYNCED.ordinal
                        ophthalmicDao.updateOphthalmicVisit(assessment)
                    }
                    return@withContext true
                }

                false
            } catch (e: Exception) {
                Timber.e(e, "Error syncing Ophthalmic records")
                false
            }
        }
    }

    suspend fun pullOphthalmicVisitsFromServer(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userRepo.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")
            var backoffMs = 500L

            repeat(MAX_PULL_ATTEMPTS) { attempt ->
                try {
                    val villageList = VillageIdList(
                        convertStringToIntList(user.assignVillageIds ?: ""),
                        prefDao.getLastPatientSyncTime()
                    )
                    val response = amritApiService.getOphthalmicVisits(villageList)
                    if (!response.isSuccessful) return@withContext false

                    val responseBody = response.body()?.string() ?: return@withContext false
                    val jsonObj = JSONObject(responseBody)
                    if (jsonObj.isNull("statusCode")) {
                        throw IllegalStateException("Amrit server not responding properly")
                    }

                    when (jsonObj.getInt("statusCode")) {
                        STATUS_OK -> {
                            val dataArray = jsonObj.optJSONArray("data")
                            if (dataArray != null) {
                                upsertPulledOphthalmicData(dataArray)
                                return@withContext true
                            }
                        }
                        STATUS_TOKEN_EXPIRED -> {
                            val tokenRefreshed = userRepo.refreshTokenTmc(user.userName, user.password)
                            if (!tokenRefreshed) {
                                Timber.w("Token refresh failed while pulling Ophthalmic records")
                                return@withContext false
                            }
                            if (attempt < MAX_PULL_ATTEMPTS - 1) {
                                delay(backoffMs)
                                backoffMs *= 2
                                return@repeat
                            }
                            Timber.w("Max retry attempts reached while pulling Ophthalmic records")
                            return@withContext false
                        }
                        STATUS_NO_RECORDS -> return@withContext true
                    }
                } catch (e: SocketTimeoutException) {
                    if (attempt < MAX_PULL_ATTEMPTS - 1) {
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

    private suspend fun upsertPulledOphthalmicData(dataArray: JSONArray) {
        val gson = Gson()
        for (i in 0 until dataArray.length()) {
            val networkObj = gson.fromJson(
                dataArray.getJSONObject(i).toString(),
                OphthalmicNetwork::class.java
            )
            val patient = networkObj.beneficiaryRegID.toLongOrNull()?.let {
                patientDao.getPatientByBenRegId(it)
            } ?: continue

            val visitNo = networkObj.benVisitNo ?: continue
            val existing = getOphthalmicVisit(patient.patientID, visitNo)
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
}
