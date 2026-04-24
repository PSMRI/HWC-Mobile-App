package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.MentalHealthScreeningDao
import org.piramalswasthya.cho.model.MentalHealthScreeningCache
import javax.inject.Inject
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.model.MentalHealthNetwork
import org.piramalswasthya.cho.model.toCacheModel
import org.piramalswasthya.cho.model.toNetworkModel
import org.piramalswasthya.cho.network.AmritApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import timber.log.Timber

class MentalHealthScreeningRepo @Inject constructor(
    private val mentalHealthScreeningDao: MentalHealthScreeningDao,
    private val patientDao: PatientDao,
    private val amritApiService: AmritApiService,
    private val userRepo: UserRepo,
    private val prefDao: PreferenceDao
) {
    suspend fun saveScreening(screening: MentalHealthScreeningCache): MentalHealthScreeningCache {
        return if (screening.screeningId == 0L) {
            val screeningId = mentalHealthScreeningDao.insert(screening)
            screening.copy(screeningId = screeningId)
        } else {
            mentalHealthScreeningDao.update(screening)
            screening
        }
    }

    suspend fun getScreeningByPatientId(
        patientID: String
    ): MentalHealthScreeningCache? {
        return mentalHealthScreeningDao.getScreeningByPatientId(patientID)
    }

    suspend fun getScreeningByPatientIdAndVisitNo(
        patientID: String,
        benVisitNo: Int
    ): MentalHealthScreeningCache? {
        return mentalHealthScreeningDao.getScreeningByPatientIdAndVisitNo(
            patientID, benVisitNo
        )
    }
    suspend fun processMentalVisits(): Boolean {
        return withContext(Dispatchers.IO) {
            val unsyncedList = mentalHealthScreeningDao.getUnsyncedAssessments()
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
                post = { payload -> amritApiService.postMentalForm(payload) },
                markSynced = { assessment ->
                    assessment.syncState = SyncState.SYNCED.ordinal
                    mentalHealthScreeningDao.update(assessment)
                },
                logLabel = "Mental Health records"
            )
        }
    }

    suspend fun pullMentalVisitsFromServer(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userRepo.getLoggedInUser()
            if (user == null) {
                Timber.w("No user logged in. Skipping pull for Mental Health records")
                return@withContext false
            }
            val gson = Gson()
            AmritSyncRepositoryHelper.pullWithRetry(
                villageIds = user.assignVillageIds ?: "",
                lastSyncDate = prefDao.getLastPatientSyncTime(),
                fetch = { villageList -> amritApiService.getMentalVisits(villageList) },
                refreshToken = { userRepo.refreshTokenTmc(user.userName, user.password) },
                onDataArray = { dataArray ->
                    AmritSyncRepositoryHelper.upsertByBeneficiaryRegId(
                        dataArray = dataArray,
                        parseNetwork = { jsonObj ->
                            gson.fromJson(jsonObj.toString(), MentalHealthNetwork::class.java)
                        },
                        beneficiaryRegId = { it.beneficiaryRegID.toLongOrNull() },
                        resolvePatientId = { benRegId ->
                            patientDao.getPatientByBenRegId(benRegId)?.patientID
                        },
                        isExisting = { patientId, networkObj ->
                            getScreeningByPatientIdAndVisitNo(
                                patientId,
                                networkObj.benVisitNo ?: 0
                            ) != null
                        },
                        insertNew = { patientId, networkObj ->
                            saveScreening(
                                networkObj.toCacheModel(patientId).copy(
                                    syncState = SyncState.SYNCED.ordinal
                                )
                            )
                        }
                    )
                },
                logLabel = "Mental Health records"
            )
        }
    }
}
