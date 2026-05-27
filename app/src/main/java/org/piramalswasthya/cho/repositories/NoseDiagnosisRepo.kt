package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.NoseDiagnosisAssessmentDao
import org.piramalswasthya.cho.model.NoseDiagnosisAssessment
import javax.inject.Inject
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.model.NoseDiagnosisNetwork
import org.piramalswasthya.cho.model.toCacheModel
import org.piramalswasthya.cho.model.toNetworkModel
import org.piramalswasthya.cho.network.AmritApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import timber.log.Timber

class NoseDiagnosisRepo @Inject constructor(
    private val noseDiagnosisAssessmentDao: NoseDiagnosisAssessmentDao,
    private val patientDao: PatientDao,
    private val amritApiService: AmritApiService,
    private val userRepo: UserRepo,
    private val prefDao: PreferenceDao
) {
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
                post = { payload -> amritApiService.postNoseForm(payload) },
                markSynced = { assessment ->
                    assessment.syncState = SyncState.SYNCED.ordinal
                    noseDiagnosisAssessmentDao.update(assessment)
                },
                logLabel = "Nose Diagnosis records"
            )
        }
    }

    suspend fun pullNoseVisitsFromServer(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userRepo.getLoggedInUser()
            if (user == null) {
                Timber.w("No user logged in. Skipping pull for Nose Diagnosis records")
                return@withContext false
            }
            val gson = Gson()
            AmritSyncRepositoryHelper.pullWithRetry(
                villageIds = user.assignVillageIds ?: "",
                lastSyncDate = prefDao.getLastPatientSyncTime(),
                fetch = { villageList -> amritApiService.getNoseVisits(villageList) },
                refreshToken = { userRepo.refreshTokenTmc(user.userName, user.password) },
                onDataArray = { dataArray ->
                    AmritSyncRepositoryHelper.upsertByBeneficiaryRegId(
                        dataArray = dataArray,
                        parseNetwork = { jsonObj ->
                            gson.fromJson(jsonObj.toString(), NoseDiagnosisNetwork::class.java)
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
                logLabel = "Nose Diagnosis records"
            )
        }
    }
}
