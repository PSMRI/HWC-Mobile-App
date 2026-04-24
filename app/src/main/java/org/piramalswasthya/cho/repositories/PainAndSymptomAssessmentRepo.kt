package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.PainAndSymptomAssessmentDao
import org.piramalswasthya.cho.model.PainAndSymptomAssessment
import javax.inject.Inject
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.network.AmritApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
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
            val unsyncedList = painAndSymptomAssessmentDao.getUnsyncedAssessments()
            AmritSyncRepositoryHelper.pushUnsynced(
                unsyncedList = unsyncedList,
                mapToPayload = { assessment ->
                    val patient = patientDao.getPatient(assessment.patientID)
                    val benId = patient?.beneficiaryID
                    val benRegId = patient?.beneficiaryRegID
                    if (benId == null || benRegId == null) null else {
                        assessment.toNetworkModel(
                            beneficiaryID = benId.toString(),
                            beneficiaryRegID = benRegId.toString()
                        )
                    }
                },
                post = { payload -> amritApiService.postPainForm(payload) },
                markSynced = { assessment ->
                    assessment.syncState = SyncState.SYNCED.ordinal
                    painAndSymptomAssessmentDao.update(assessment)
                },
                logLabel = "Pain Assessment records"
            )
        }
    }

    suspend fun pullPainVisitsFromServer(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userRepo.getLoggedInUser()
            if (user == null) {
                Timber.w("No user logged in. Skipping pull for Pain Assessment records")
                return@withContext false
            }
            val gson = Gson()
            AmritSyncRepositoryHelper.pullWithRetry(
                villageIds = user.assignVillageIds ?: "",
                lastSyncDate = prefDao.getLastPatientSyncTime(),
                fetch = { villageList -> amritApiService.getPainVisits(villageList) },
                refreshToken = { userRepo.refreshTokenTmc(user.userName, user.password) },
                onDataArray = { dataArray ->
                    AmritSyncRepositoryHelper.upsertByBeneficiaryRegId(
                        dataArray = dataArray,
                        parseNetwork = { jsonObj ->
                            gson.fromJson(jsonObj.toString(), PainAssessmentNetwork::class.java)
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
                                networkObj.toCacheModel(patientID = patientId).copy(
                                    syncState = SyncState.SYNCED.ordinal
                                )
                            )
                        }
                    )
                },
                logLabel = "Pain Assessment records"
            )
        }
    }
}
