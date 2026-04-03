package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.OphthalmicDao
import org.piramalswasthya.cho.model.OphthalmicVisit
import javax.inject.Inject

import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.model.OphthalmicNetwork
import org.piramalswasthya.cho.network.AmritApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.VillageIdList
import org.piramalswasthya.cho.utils.generateUuid
import java.net.SocketTimeoutException
import org.json.JSONObject
import timber.log.Timber

class OphthalmicRepository @Inject constructor(
    private val ophthalmicDao: OphthalmicDao,
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
            if (unsyncedList.isEmpty()) return@withContext true

            val networkList = mutableListOf<OphthalmicNetwork>()
            unsyncedList.forEach { assessment ->
                val patient = patientDao.getPatient(assessment.patientID)
                if (patient?.beneficiaryID != null && patient.beneficiaryRegID != null) {
                    networkList.add(OphthalmicNetwork(patient.beneficiaryID.toString(), patient.beneficiaryRegID.toString(), assessment))
                }
            }

            if (networkList.isEmpty()) return@withContext false

            try {
                val response = amritApiService.postOphthalmicForm(networkList)
                if (response.isSuccessful) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)
                        if (jsonObj.optInt("statusCode") == 200) {
                            unsyncedList.forEach {
                                it.syncState = SyncState.SYNCED.ordinal
                                ophthalmicDao.updateOphthalmicVisit(it)
                            }
                            return@withContext true
                        }
                    }
                }
                return@withContext false
            } catch (e: Exception) {
                Timber.e(e, "Error syncing Ophthalmic records")
                return@withContext false
            }
        }
    }

    suspend fun pullOphthalmicVisitsFromServer(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userRepo.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")
            try {
                val villageList = VillageIdList(
                    convertStringToIntList(user.assignVillageIds ?: ""),
                    prefDao.getLastPatientSyncTime()
                )
                val response = amritApiService.getOphthalmicVisits(villageList)
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
                                        OphthalmicNetwork::class.java
                                    )
                                    val patient = networkObj.beneficiaryRegID.toLongOrNull()?.let {
                                        patientDao.getPatientByBenRegId(it)
                                    }
                                    if (patient != null) {
                                        val existing = getOphthalmicVisit(
                                            patient.patientID,
                                            networkObj.data.benVisitNo
                                        )
                                        if (existing == null) {
                                            val visitId = if (networkObj.data.visitId.isBlank()) generateUuid() else networkObj.data.visitId
                                            val cache = networkObj.data.copy(
                                                visitId = visitId,
                                                patientID = patient.patientID,
                                                syncState = SyncState.SYNCED.ordinal
                                            )
                                            saveOphthalmicVisit(cache)
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
                return@withContext pullOphthalmicVisitsFromServer()
            } catch (e: Exception) {
                Timber.e(e, "Error pulling Ophthalmic records")
                return@withContext false
            }
        }
    }
}
