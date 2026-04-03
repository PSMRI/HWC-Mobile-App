package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.NoseDiagnosisAssessmentDao
import org.piramalswasthya.cho.model.NoseDiagnosisAssessment
import javax.inject.Inject

import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.model.NoseDiagnosisNetwork
import org.piramalswasthya.cho.network.AmritApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.VillageIdList
import java.net.SocketTimeoutException
import org.json.JSONObject
import timber.log.Timber

class NoseDiagnosisRepo @Inject constructor(
    private val noseDiagnosisAssessmentDao: NoseDiagnosisAssessmentDao,
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

    suspend fun saveAssessment(assessment: NoseDiagnosisAssessment) {
        if (assessment.assessmentId == 0L) {
            noseDiagnosisAssessmentDao.insert(assessment)
        } else {
            noseDiagnosisAssessmentDao.update(assessment)
        }
    }

    suspend fun getAssessmentByPatientId(patientID: String): NoseDiagnosisAssessment? {
        return noseDiagnosisAssessmentDao.getAssessmentByPatientId(patientID)
    }

    suspend fun getAssessmentByPatientIdAndVisitNo(
        patientID: String,
        benVisitNo: Int
    ): NoseDiagnosisAssessment? {
        return noseDiagnosisAssessmentDao.getAssessmentByPatientIdAndVisitNo(patientID, benVisitNo)
    }

    suspend fun processNoseVisits(): Boolean {
        return withContext(Dispatchers.IO) {
            val unsyncedList = noseDiagnosisAssessmentDao.getUnsyncedAssessments()
            if (unsyncedList.isEmpty()) return@withContext true

            val networkList = mutableListOf<NoseDiagnosisNetwork>()
            unsyncedList.forEach { assessment ->
                val patient = patientDao.getPatient(assessment.patientID)
                if (patient?.beneficiaryID != null && patient.beneficiaryRegID != null) {
                    networkList.add(NoseDiagnosisNetwork(patient.beneficiaryID.toString(), patient.beneficiaryRegID.toString(), assessment))
                }
            }

            if (networkList.isEmpty()) return@withContext false

            try {
                val response = amritApiService.postNoseForm(networkList)
                if (response.isSuccessful) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)
                        if (jsonObj.optInt("statusCode") == 200) {
                            unsyncedList.forEach {
                                it.syncState = SyncState.SYNCED.ordinal
                                noseDiagnosisAssessmentDao.update(it)
                            }
                            return@withContext true
                        }
                    }
                }
                return@withContext false
            } catch (e: Exception) {
                Timber.e(e, "Error syncing Nose Diagnosis records")
                return@withContext false
            }
        }
    }

    suspend fun pullNoseVisitsFromServer(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userRepo.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")
            try {
                val villageList = VillageIdList(
                    convertStringToIntList(user.assignVillageIds ?: ""),
                    prefDao.getLastPatientSyncTime()
                )
                val response = amritApiService.getNoseVisits(villageList)
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
                                        NoseDiagnosisNetwork::class.java
                                    )
                                    val patient = networkObj.beneficiaryRegID.toLongOrNull()?.let {
                                        patientDao.getPatientByBenRegId(it)
                                    }
                                    if (patient != null) {
                                        val existing = getAssessmentByPatientIdAndVisitNo(
                                            patient.patientID,
                                            networkObj.data.benVisitNo ?: 0
                                        )
                                        if (existing == null) {
                                            val cache = networkObj.data.copy(
                                                assessmentId = 0L,
                                                patientID = patient.patientID,
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
                return@withContext pullNoseVisitsFromServer()
            } catch (e: Exception) {
                Timber.e(e, "Error pulling Nose records")
                return@withContext false
            }
        }
    }
}
