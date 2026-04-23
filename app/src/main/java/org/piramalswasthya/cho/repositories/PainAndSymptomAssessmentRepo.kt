package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.PainAndSymptomAssessmentDao
import org.piramalswasthya.cho.model.PainAndSymptomAssessment
import javax.inject.Inject
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.network.AmritApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.VillageIdList
import java.net.SocketTimeoutException
import org.json.JSONObject
import org.piramalswasthya.cho.model.PainAssessmentNetwork
import org.piramalswasthya.cho.model.toCacheModel
import org.piramalswasthya.cho.model.toNetworkModel
import timber.log.Timber
import org.json.JSONArray

class PainAndSymptomAssessmentRepo @Inject constructor(
    private val painAndSymptomAssessmentDao: PainAndSymptomAssessmentDao,
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
    suspend fun saveAssessment(assessment: PainAndSymptomAssessment) {
        if (assessment.assessmentId == 0L) {
            painAndSymptomAssessmentDao.insert(assessment)
        } else {
            painAndSymptomAssessmentDao.update(assessment)
        }
    }

    suspend fun getAssessmentByPatientId(
        patientID: String
    ): PainAndSymptomAssessment? {
        return painAndSymptomAssessmentDao.getAssessmentByPatientId(patientID)
    }

    suspend fun getAssessmentByPatientIdAndVisitNo(
        patientID: String,
        benVisitNo: Int
    ): PainAndSymptomAssessment? {
        return painAndSymptomAssessmentDao
            .getAssessmentByPatientIdAndVisitNo(patientID, benVisitNo)
    }
    suspend fun processPainvisits(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val unsyncedList = painAndSymptomAssessmentDao.getUnsyncedAssessments()
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

                val response = amritApiService.postPainForm(payload.map { it.second })
                val statusCode = response.body()?.string()?.let(::JSONObject)?.optInt("statusCode")
                if (response.isSuccessful && statusCode == STATUS_OK) {
                    payload.forEach { (assessment, _) ->
                        assessment.syncState = SyncState.SYNCED.ordinal
                        painAndSymptomAssessmentDao.update(assessment)
                    }
                    return@withContext true
                }

                false
            } catch (e: Exception) {
                Timber.e(e, "Error syncing Pain Assessment records")
                false
            }
        }
    }

    suspend fun pullPainVisitsFromServer(): Boolean {
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
                    val response = amritApiService.getPainVisits(villageList)
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
                                upsertPulledPainData(dataArray)
                                return@withContext true
                            }
                        }
                        STATUS_TOKEN_EXPIRED -> {
                            val tokenRefreshed = userRepo.refreshTokenTmc(user.userName, user.password)
                            if (!tokenRefreshed) {
                                Timber.w("Token refresh failed while pulling Pain Assessment records")
                                return@withContext false
                            }
                            if (attempt < MAX_PULL_ATTEMPTS - 1) {
                                delay(backoffMs)
                                backoffMs *= 2
                                return@repeat
                            }
                            Timber.w("Max retry attempts reached while pulling Pain Assessment records")
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
                    Timber.e(e, "Socket timeout while pulling Pain Assessment records after retries")
                    return@withContext false
                } catch (e: Exception) {
                    Timber.e(e, "Error pulling Pain Assessment records")
                    return@withContext false
                }
                return@withContext false
            }

            return@withContext false
        }
    }

    private suspend fun upsertPulledPainData(dataArray: JSONArray) {
        val gson = Gson()
        for (i in 0 until dataArray.length()) {
            val networkObj = gson.fromJson(
                dataArray.getJSONObject(i).toString(),
                PainAssessmentNetwork::class.java
            )
            val patient = networkObj.beneficiaryRegID.toLongOrNull()?.let {
                patientDao.getPatientByBenRegId(it)
            } ?: continue

            val existing = getAssessmentByPatientIdAndVisitNo(
                patient.patientID,
                networkObj.benVisitNo ?: 0
            )
            if (existing == null) {
                val cache = networkObj.toCacheModel(
                    patientID = patient.patientID
                ).copy(syncState = SyncState.SYNCED.ordinal)
                saveAssessment(cache)
            }
        }
    }

}
