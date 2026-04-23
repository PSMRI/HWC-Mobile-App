package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.EarDiagnosisAssessmentDao
import org.piramalswasthya.cho.model.EarDiagnosisAssessment
import javax.inject.Inject
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.model.EarDiagnosisNetwork
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
import org.json.JSONArray
class EarDiagnosisRepo @Inject constructor(
    private val earDiagnosisAssessmentDao: EarDiagnosisAssessmentDao,
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

    suspend fun saveAssessment(assessment: EarDiagnosisAssessment) {
        if (assessment.assessmentId == 0L) {
            earDiagnosisAssessmentDao.insert(assessment)
        } else {
            earDiagnosisAssessmentDao.update(assessment)
        }
    }

    suspend fun getAssessmentByPatientId(patientID: String): EarDiagnosisAssessment? {
        return earDiagnosisAssessmentDao.getAssessmentByPatientId(patientID)
    }

    suspend fun getAssessmentByPatientIdAndVisitNo(patientID: String, benVisitNo: Int): EarDiagnosisAssessment? {
        return earDiagnosisAssessmentDao.getAssessmentByPatientIdAndVisitNo(patientID, benVisitNo)
    }
    suspend fun processEarVisits(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val unsyncedList = earDiagnosisAssessmentDao.getUnsyncedAssessments()
                if (unsyncedList.isEmpty()) return@withContext true

                val payload = unsyncedList.mapNotNull { assessment ->
                    val patient = patientDao.getPatient(assessment.patientId)
                    val benId = patient?.beneficiaryID ?: return@mapNotNull null
                    val benRegId = patient.beneficiaryRegID ?: return@mapNotNull null
                    assessment to assessment.toNetworkModel(
                        beneficiaryID = benId.toString(),
                        beneficiaryRegID = benRegId.toString()
                    )
                }

                if (payload.isEmpty()) return@withContext false

                val response = amritApiService.postEarForm(payload.map { it.second })
                val statusCode = response.body()?.string()?.let(::JSONObject)?.optInt("statusCode")
                if (response.isSuccessful && statusCode == STATUS_OK) {
                    payload.forEach { (assessment, _) ->
                        assessment.syncState = SyncState.SYNCED.ordinal
                        earDiagnosisAssessmentDao.update(assessment)
                    }
                    return@withContext true
                }

                false
            } catch (e: Exception) {
                Timber.e(e, "Error syncing Ear Diagnosis records")
                false
            }
        }
    }

    suspend fun pullEarVisitsFromServer(): Boolean {
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
                    val response = amritApiService.getEarVisits(villageList)
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
                                upsertPulledEarData(dataArray)
                                return@withContext true
                            }
                        }
                        STATUS_TOKEN_EXPIRED -> {
                            val tokenRefreshed = userRepo.refreshTokenTmc(user.userName, user.password)
                            if (!tokenRefreshed) {
                                Timber.w("Token refresh failed while pulling Ear records")
                                return@withContext false
                            }
                            if (attempt < MAX_PULL_ATTEMPTS - 1) {
                                delay(backoffMs)
                                backoffMs *= 2
                                return@repeat
                            }
                            Timber.w("Max retry attempts reached while pulling Ear records")
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
                    Timber.e(e, "Socket timeout while pulling Ear records after retries")
                    return@withContext false
                } catch (e: Exception) {
                    Timber.e(e, "Error pulling Ear records")
                    return@withContext false
                }
                return@withContext false
            }

            return@withContext false
        }
    }

    private suspend fun upsertPulledEarData(dataArray: JSONArray) {
        val gson = Gson()
        for (i in 0 until dataArray.length()) {
            val networkObj = gson.fromJson(
                dataArray.getJSONObject(i).toString(),
                EarDiagnosisNetwork::class.java
            )
            val patient = networkObj.beneficiaryRegID.toLongOrNull()?.let {
                patientDao.getPatientByBenRegId(it)
            } ?: continue

            val existing = getAssessmentByPatientIdAndVisitNo(
                patient.patientID,
                networkObj.benVisitNo ?: 0
            )
            if (existing == null) {
                val cache = networkObj.toCacheModel(patient.patientID).copy(
                    syncState = SyncState.SYNCED.ordinal
                )
                saveAssessment(cache)
            }
        }
    }
}
