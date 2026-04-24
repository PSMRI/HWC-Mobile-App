package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.OphthalmicDao
import org.piramalswasthya.cho.model.OphthalmicVisit
import javax.inject.Inject
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.model.OphthalmicNetwork
import org.piramalswasthya.cho.model.toCacheModel
import org.piramalswasthya.cho.model.toNetworkModel
import org.piramalswasthya.cho.network.AmritApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.utils.generateUuid
import timber.log.Timber
import org.json.JSONArray

class OphthalmicRepository @Inject constructor(
    private val ophthalmicDao: OphthalmicDao,
    private val patientDao: PatientDao,
    private val amritApiService: AmritApiService,
    private val userRepo: UserRepo,
    private val prefDao: PreferenceDao
) {
    suspend fun getOphthalmicVisit(patientID: String, benVisitNo: Int): OphthalmicVisit? {
        return ophthalmicDao.getOphthalmicVisit(patientID, benVisitNo)
    }

    suspend fun saveOphthalmicVisit(visit: OphthalmicVisit) {
        if (visit.visitId.isNotEmpty()) {
             val existing = ophthalmicDao.getOphthalmicVisitById(visit.visitId)
             if (existing == null) {
                 ophthalmicDao.insertOphthalmicVisit(visit)
             } else {
                 ophthalmicDao.updateOphthalmicVisit(visit)
             }
        } else {
            ophthalmicDao.insertOphthalmicVisit(visit)
        }
    }
    
    suspend fun updateOphthalmicVisit(visit: OphthalmicVisit) {
        ophthalmicDao.updateOphthalmicVisit(visit)
    }
    suspend fun processOphthalmicVisits(): Boolean {
        return withContext(Dispatchers.IO) {
            val unsyncedList = ophthalmicDao.getUnsyncedOphthalmicVisits()
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
                post = { payload -> amritApiService.postOphthalmicForm(payload) },
                markSynced = { assessment ->
                    assessment.syncState = SyncState.SYNCED.ordinal
                    ophthalmicDao.updateOphthalmicVisit(assessment)
                },
                logLabel = "Ophthalmic records"
            )
        }
    }

    suspend fun pullOphthalmicVisitsFromServer(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userRepo.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")
            val gson = Gson()
            AmritSyncRepositoryHelper.pullWithRetry(
                villageIds = user.assignVillageIds ?: "",
                lastSyncDate = prefDao.getLastPatientSyncTime(),
                fetch = { villageList -> amritApiService.getOphthalmicVisits(villageList) },
                refreshToken = { userRepo.refreshTokenTmc(user.userName, user.password) },
                onDataArray = { dataArray ->
                    AmritSyncRepositoryHelper.upsertByBeneficiaryRegId(
                        dataArray = dataArray,
                        parseNetwork = { jsonObj ->
                            gson.fromJson(jsonObj.toString(), OphthalmicNetwork::class.java)
                        },
                        beneficiaryRegId = { it.beneficiaryRegID.toLongOrNull() },
                        resolvePatientId = { benRegId ->
                            patientDao.getPatientByBenRegId(benRegId)?.patientID
                        },
                        isExisting = { patientId, networkObj ->
                            val visitNo = networkObj.benVisitNo ?: 0
                            getOphthalmicVisit(patientId, visitNo) != null
                        },
                        insertNew = { patientId, networkObj ->
                            val visitId: String = networkObj.visitId?.takeIf { it.isNotBlank() }
                                ?: generateUuid()
                            saveOphthalmicVisit(
                                networkObj.toCacheModel(patientId).copy(
                                    visitId = visitId,
                                    patientID = patientId,
                                    syncState = SyncState.SYNCED.ordinal
                                )
                            )
                        }
                    )
                },
                logLabel = "Ophthalmic records"
            )
        }
    }
}
