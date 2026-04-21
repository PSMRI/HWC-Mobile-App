package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.PsychosocialCaregiverSupportDao
import org.piramalswasthya.cho.model.PsychosocialCaregiverSupport
import javax.inject.Inject
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.network.AmritApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.VillageIdList
import java.net.SocketTimeoutException
import org.json.JSONObject
import org.piramalswasthya.cho.model.PsychosocialCaregiverSupportNetwork
import org.piramalswasthya.cho.model.toCacheModel
import org.piramalswasthya.cho.model.toNetworkModel
import timber.log.Timber

class PsychosocialCaregiverSupportRepo @Inject constructor(
    private val psychosocialCaregiverSupportDao: PsychosocialCaregiverSupportDao,
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
    suspend fun saveAssessment(assessment: PsychosocialCaregiverSupport) {
        if (assessment.assessmentId == 0L) {
            psychosocialCaregiverSupportDao.insert(assessment)
        } else {
            psychosocialCaregiverSupportDao.update(assessment)
        }
    }

    suspend fun getAssessmentByPatientId(
        patientID: String
    ): PsychosocialCaregiverSupport? {
        return psychosocialCaregiverSupportDao
            .getAssessmentByPatientId(patientID)
    }

    suspend fun getAssessmentByPatientIdAndVisitNo(
        patientID: String,
        benVisitNo: Int
    ): PsychosocialCaregiverSupport? {
        return psychosocialCaregiverSupportDao
            .getAssessmentByPatientIdAndVisitNo(patientID, benVisitNo)
    }
    suspend fun processPsychosocialCaregiverVisits(): Boolean {
        return withContext(Dispatchers.IO) {
            val unsyncedList = psychosocialCaregiverSupportDao.getUnsyncedAssessments()
            if (unsyncedList.isEmpty()) return@withContext true

            val networkList = mutableListOf<PsychosocialCaregiverSupportNetwork>()
            unsyncedList.forEach { assessment ->
                val patient = patientDao.getPatient(assessment.patientID)
                if (patient?.beneficiaryID != null && patient.beneficiaryRegID != null) {
                    networkList.add(
                        assessment.toNetworkModel(
                            beneficiaryID = patient.beneficiaryID.toString(),
                            beneficiaryRegID = patient.beneficiaryRegID.toString()
                        )
                    )
                }
            }

            if (networkList.isEmpty()) return@withContext false

            try {
                val response = amritApiService.postPsychosocialCaregiverForm(networkList)
                if (response.isSuccessful) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)
                        if (jsonObj.optInt("statusCode") == 200) {
                            unsyncedList.forEach {
                                it.syncState = SyncState.SYNCED.ordinal
                                psychosocialCaregiverSupportDao.update(it)
                            }
                            return@withContext true
                        }
                    }
                }
                return@withContext false
            } catch (e: Exception) {
                Timber.e(e, "Error syncing Psychosocial Caregiver Support Assessment records")
                return@withContext false
            }
        }
    }

    suspend fun pullPsychosocialCaregiverSupportVisitsFromServer(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userRepo.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")
            try {
                val villageList = VillageIdList(
                    convertStringToIntList(user.assignVillageIds ?: ""),
                    prefDao.getLastPatientSyncTime()
                )
                val response = amritApiService.getPsychosocialCaregiverVisits(villageList)
                if (response.isSuccessful) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)
                        val errorMessage = jsonObj.optString("errorMessage")
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
                                        PsychosocialCaregiverSupportNetwork::class.java
                                    )
                                    val patient = networkObj.beneficiaryRegID.toLongOrNull()?.let {
                                        patientDao.getPatientByBenRegId(it)
                                    }
                                    if (patient != null) {
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
                                return@withContext true
                            }
                        } else if (responseStatusCode == 5002) {
                            if (userRepo.refreshTokenTmc(user.userName, user.password)) {
                                throw SocketTimeoutException()
                            }
                        } else if (responseStatusCode == 5000) {
                            return@withContext true
                        }
                    }
                }
                return@withContext false
            } catch (e: SocketTimeoutException) {
                return@withContext pullPsychosocialCaregiverSupportVisitsFromServer()
            } catch (e: Exception) {
                Timber.e(e, "Error pulling Psychosocial Caregiver Support Assessment records")
                return@withContext false
            }

        }
    }
}
