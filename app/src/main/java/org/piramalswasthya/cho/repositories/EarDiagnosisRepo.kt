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
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao

class EarDiagnosisRepo @Inject constructor(
    private val earDiagnosisAssessmentDao: EarDiagnosisAssessmentDao,
    private val patientDao: PatientDao,
    private val amritApiService: AmritApiService,
    private val userRepo: UserRepo,
    private val prefDao: PreferenceDao
) {
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
            val unsyncedList = earDiagnosisAssessmentDao.getUnsyncedAssessments()
            AmritSyncRepositoryHelper.pushUnsynced(
                unsyncedList = unsyncedList,
                mapToPayload = { assessment ->
                    val patient = patientDao.getPatient(assessment.patientId)
                    val benId = patient?.beneficiaryID
                    val benRegId = patient?.beneficiaryRegID
                    if (benId == null || benRegId == null) null else {
                        assessment.toNetworkModel(
                            beneficiaryID = benId.toString(),
                            beneficiaryRegID = benRegId.toString()
                        )
                    }
                },
                post = { payload -> amritApiService.postEarForm(payload) },
                markSynced = { assessment ->
                    assessment.syncState = SyncState.SYNCED.ordinal
                    earDiagnosisAssessmentDao.update(assessment)
                },
                logLabel = "Ear Diagnosis records"
            )
        }
    }

    suspend fun pullEarVisitsFromServer(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userRepo.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")
            val gson = Gson()
            AmritSyncRepositoryHelper.pullWithRetry(
                villageIds = user.assignVillageIds ?: "",
                lastSyncDate = prefDao.getLastPatientSyncTime(),
                fetch = { villageList -> amritApiService.getEarVisits(villageList) },
                refreshToken = { userRepo.refreshTokenTmc(user.userName, user.password) },
                onDataArray = { dataArray ->
                    AmritSyncRepositoryHelper.upsertByBeneficiaryRegId(
                        dataArray = dataArray,
                        parseNetwork = { jsonObj ->
                            gson.fromJson(jsonObj.toString(), EarDiagnosisNetwork::class.java)
                        },
                        beneficiaryRegId = { it.beneficiaryRegID.toLongOrNull() },
                        resolvePatientId = { benRegId ->
                            patientDao.getPatientByBenRegId(benRegId)?.patientID
                        },
                        isExisting = { patientId, networkObj ->
                            getAssessmentByPatientIdAndVisitNo(
                                patientId,
                                networkObj.benVisitNo ?: 0
                            ) != null
                        },
                        insertNew = { patientId, networkObj ->
                            saveAssessment(
                                networkObj.toCacheModel(patientId).copy(
                                    syncState = SyncState.SYNCED.ordinal
                                )
                            )
                        }
                    )
                },
                logLabel = "Ear records"
            )
        }
    }
}
