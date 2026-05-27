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
import org.piramalswasthya.cho.model.PsychosocialCaregiverSupportNetwork
import org.piramalswasthya.cho.model.toCacheModel
import org.piramalswasthya.cho.model.toNetworkModel
import timber.log.Timber
import org.json.JSONArray

class PsychosocialCaregiverSupportRepo @Inject constructor(
    private val psychosocialCaregiverSupportDao: PsychosocialCaregiverSupportDao,
    private val patientDao: PatientDao,
    private val amritApiService: AmritApiService,
    private val userRepo: UserRepo,
    private val prefDao: PreferenceDao
) {
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
                post = { payload -> amritApiService.postPsychosocialCaregiverForm(payload) },
                markSynced = { assessment ->
                    assessment.syncState = SyncState.SYNCED.ordinal
                    psychosocialCaregiverSupportDao.update(assessment)
                },
                logLabel = "Psychosocial Caregiver Support Assessment records"
            )
        }
    }

    suspend fun pullPsychosocialCaregiverSupportVisitsFromServer(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userRepo.getLoggedInUser()
            if (user == null) {
                Timber.w("No user logged in. Skipping pull for Psychosocial Caregiver Support records")
                return@withContext false
            }
            val gson = Gson()
            AmritSyncRepositoryHelper.pullWithRetry(
                villageIds = user.assignVillageIds ?: "",
                lastSyncDate = prefDao.getLastPatientSyncTime(),
                fetch = { villageList -> amritApiService.getPsychosocialCaregiverVisits(villageList) },
                refreshToken = { userRepo.refreshTokenTmc(user.userName, user.password) },
                onDataArray = { dataArray ->
                    AmritSyncRepositoryHelper.upsertByBeneficiaryRegId(
                        dataArray = dataArray,
                        parseNetwork = { jsonObj ->
                            gson.fromJson(
                                jsonObj.toString(),
                                PsychosocialCaregiverSupportNetwork::class.java
                            )
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
                logLabel = "Psychosocial Caregiver Support records"
            )
        }
    }
}
