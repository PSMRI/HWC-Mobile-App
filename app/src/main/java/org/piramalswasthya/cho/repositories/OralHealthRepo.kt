package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.OralHealthDao
import org.piramalswasthya.cho.model.OralHealth
import javax.inject.Inject
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.model.OralHealthNetwork
import org.piramalswasthya.cho.model.toCacheModel
import org.piramalswasthya.cho.model.toNetworkModel
import org.piramalswasthya.cho.network.AmritApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import timber.log.Timber
import org.json.JSONArray

class OralHealthRepo @Inject constructor(
    private val oralHealthDao: OralHealthDao,
    private val patientDao: PatientDao,
    private val amritApiService: AmritApiService,
    private val userRepo: UserRepo,
    private val prefDao: PreferenceDao
) {
    suspend fun save(oralHealth: OralHealth) {
        if (oralHealth.oralHealthId == 0L) {
            oralHealthDao.insert(oralHealth)
        } else {
            oralHealthDao.update(oralHealth)
        }
    }

    suspend fun getByPatientId(patientID: String): OralHealth? {
        return oralHealthDao.getByPatientId(patientID)
    }

    suspend fun getByPatientIdAndVisitNo(
        patientID: String,
        benVisitNo: Int
    ): OralHealth? {
        return oralHealthDao.getByPatientIdAndVisitNo(patientID, benVisitNo)
    }
    suspend fun processOralVisits(): Boolean {
        return withContext(Dispatchers.IO) {
            val unsyncedList = oralHealthDao.getUnsyncedAssessments()
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
                post = { payload -> amritApiService.postOralForm(payload) },
                markSynced = { assessment ->
                    assessment.syncState = SyncState.SYNCED.ordinal
                    oralHealthDao.update(assessment)
                },
                logLabel = "Oral Health records"
            )
        }
    }

    suspend fun pullOralVisitsFromServer(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userRepo.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")
            val gson = Gson()
            AmritSyncRepositoryHelper.pullWithRetry(
                villageIds = user.assignVillageIds ?: "",
                lastSyncDate = prefDao.getLastPatientSyncTime(),
                fetch = { villageList -> amritApiService.getOralVisits(villageList) },
                refreshToken = { userRepo.refreshTokenTmc(user.userName, user.password) },
                onDataArray = { dataArray ->
                    AmritSyncRepositoryHelper.upsertByBeneficiaryRegId(
                        dataArray = dataArray,
                        parseNetwork = { jsonObj ->
                            gson.fromJson(jsonObj.toString(), OralHealthNetwork::class.java)
                        },
                        beneficiaryRegId = { it.beneficiaryRegID.toLongOrNull() },
                        resolvePatientId = { benRegId ->
                            patientDao.getPatientByBenRegId(benRegId)?.patientID
                        },
                        isExisting = { patientId, networkObj ->
                            getByPatientIdAndVisitNo(patientId, networkObj.benVisitNo ?: 0) != null
                        },
                        insertNew = { patientId, networkObj ->
                            save(
                                networkObj.toCacheModel(patientID = patientId).copy(
                                    syncState = SyncState.SYNCED.ordinal
                                )
                            )
                        }
                    )
                },
                logLabel = "Oral Health records"
            )
        }
    }
}
