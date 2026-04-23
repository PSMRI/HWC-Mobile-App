package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.ElderlyHealthAssessmentDao
import org.piramalswasthya.cho.model.ElderlyHealthAssessment
import javax.inject.Inject
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.model.ElderlyHealthNetwork
import org.piramalswasthya.cho.model.toCacheModel
import org.piramalswasthya.cho.model.toNetworkModel
import org.piramalswasthya.cho.network.AmritApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao

class ElderlyHealthRepo @Inject constructor(
    private val elderlyHealthAssessmentDao: ElderlyHealthAssessmentDao,
    private val patientDao: PatientDao,
    private val amritApiService: AmritApiService,
    private val userRepo: UserRepo,
    private val prefDao: PreferenceDao
) {
    suspend fun saveAssessment(assessment: ElderlyHealthAssessment) {
        if (assessment.assessmentId == 0L) {
            elderlyHealthAssessmentDao.insert(assessment)
        } else {
            elderlyHealthAssessmentDao.update(assessment)
        }
    }

    suspend fun getAssessmentByPatientId(patientID: String): ElderlyHealthAssessment? {
        return elderlyHealthAssessmentDao.getAssessmentByPatientId(patientID)
    }

    suspend fun getAssessment(patientID: String, benVisitNo: Int): ElderlyHealthAssessment? {
        return elderlyHealthAssessmentDao.getAssessment(patientID, benVisitNo)
    }
    suspend fun processElderlyVisits(): Boolean {
        return withContext(Dispatchers.IO) {
            val unsyncedList = elderlyHealthAssessmentDao.getUnsyncedAssessments()
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
                post = { payload -> amritApiService.postElderlyForm(payload) },
                markSynced = { assessment ->
                    assessment.syncState = SyncState.SYNCED.ordinal
                    elderlyHealthAssessmentDao.update(assessment)
                },
                logLabel = "Elderly Health records"
            )
        }
    }

    suspend fun pullElderlyVisitsFromServer(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userRepo.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")
            val gson = Gson()
            AmritSyncRepositoryHelper.pullWithRetry(
                villageIds = user.assignVillageIds ?: "",
                lastSyncDate = prefDao.getLastPatientSyncTime(),
                fetch = { villageList -> amritApiService.getElderlyVisits(villageList) },
                refreshToken = { userRepo.refreshTokenTmc(user.userName, user.password) },
                onDataArray = { dataArray ->
                    AmritSyncRepositoryHelper.upsertByBeneficiaryRegId(
                        dataArray = dataArray,
                        parseNetwork = { jsonObj ->
                            gson.fromJson(jsonObj.toString(), ElderlyHealthNetwork::class.java)
                        },
                        beneficiaryRegId = { it.beneficiaryRegID.toLongOrNull() },
                        resolvePatientId = { benRegId ->
                            patientDao.getPatientByBenRegId(benRegId)?.patientID
                        },
                        isExisting = { patientId, networkObj ->
                            getAssessment(patientId, networkObj.benVisitNo ?: 0) != null
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
                logLabel = "Elderly records"
            )
        }
    }
}
