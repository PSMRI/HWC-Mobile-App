package org.piramalswasthya.cho.repositories

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONArray
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.database.room.dao.PncDao
//import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.PNCNetwork
import org.piramalswasthya.cho.model.PNCVisitCache
import org.piramalswasthya.cho.model.PatientWithDeliveryOutcomeAndPncCache
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.VillageIdList
import org.piramalswasthya.cho.network.getLongFromDate
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

class PncRepo @Inject constructor(
    private val amritApiService: AmritApiService,
    private val pncDao: PncDao,
    private val userRepo: UserRepo,
    private val preferenceDao: PreferenceDao,
    private val patientDao: PatientDao,
) {
    suspend fun getSavedPncRecord(patientID: String, visitNumber: Int): PNCVisitCache? {
        return withContext(Dispatchers.IO) {
            pncDao.getSavedRecord(patientID, visitNumber)
        }
    }

    suspend fun getLastFilledPncRecord(patientID: String): PNCVisitCache? {
        return withContext(Dispatchers.IO) {
            pncDao.getLastSavedRecord(patientID)
        }
    }

    suspend fun getLastVisitNumber(patientID: String): Int? {
        return withContext(Dispatchers.IO) {
            pncDao.getAllPNCsByPatId(patientID)
                .maxByOrNull { it.pncPeriod }
                ?.pncPeriod
        }
    }

    suspend fun savePnc(pncCache: PNCVisitCache): Long {
        return withContext(Dispatchers.IO) {
            pncDao.insert(pncCache)
        }
    }

    suspend fun persistPncRecord(pncCache: PNCVisitCache) {
        withContext(Dispatchers.IO) {
            pncDao.insert(pncCache)
        }

    }

    suspend fun getAllPNCsByPatId(patientID: String): List<PNCVisitCache>{
        return pncDao.getAllPNCsByPatId(patientID)
    }

    /**
     * Get all patients who are eligible for PNC (have delivered and within 42 days or not completed all visits)
     * Uses a single optimized query instead of N+1 pattern
     */
    fun getAllPNCMothers(): Flow<List<PatientWithDeliveryOutcomeAndPncCache>> {
        return pncDao.getAllPNCMothersWithData()
    }

    /**
     * Get count of PNC mothers
     */
    fun getPNCMothersCount(): Flow<Int> {
        return pncDao.getPNCMothersCount()
    }

    suspend fun processPncVisits(): Boolean {
        return withContext(Dispatchers.IO) {

            val pncList = pncDao.getAllUnprocessedPncVisits()

            val pncPostList = mutableSetOf<PNCNetwork>()

            pncList.forEach {
                pncPostList.clear()
                val ben = patientDao.getPatient(it.patientID)
                if (ben.beneficiaryID != null){
                    pncPostList.add(it.asNetworkModel(ben.beneficiaryID!!))
                    it.syncState = SyncState.SYNCING
                    pncDao.update(it)
                    val uploadDone = postDataToAmritServer(pncPostList)
                    if (uploadDone) {
                        it.processed = "P"
                        it.syncState = SyncState.SYNCED
                    } else {
                        it.syncState = SyncState.UNSYNCED
                    }
                    pncDao.update(it)
//                if (!uploadDone)
//                    return@withContext false
                }
            }

            return@withContext true
        }
    }
//
    private suspend fun postDataToAmritServer(ancPostList: MutableSet<PNCNetwork>): Boolean {
        if (ancPostList.isEmpty()) return false
        val user =
            userRepo.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")

        try {

            val response = amritApiService.postPncForm(ancPostList.toList())
            val statusCode = response.code()

            if (statusCode == 200) {
                try {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val errormessage = jsonObj.getString("errorMessage")
                        if (jsonObj.isNull("statusCode")) throw IllegalStateException("Amrit server not responding properly, Contact Service Administrator!!")
                        val responsestatuscode = jsonObj.getInt("statusCode")

                        when (responsestatuscode) {
                            200 -> {
                                Timber.d("Saved Successfully to server")
                                return true
                            }

                            5002 -> {
                                if (userRepo.refreshTokenTmc(
                                        user.userName,
                                        user.password
                                    )
                                ) throw SocketTimeoutException()
                            }

                            else -> {
                                Log.d("pnc error message", errormessage)
                                throw IOException("Throwing away IO eXcEpTiOn")
                            }
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                //server_resp5();
            }
            Timber.w("Bad Response from server, need to check $ancPostList $response ")
            return false
        } catch (e: SocketTimeoutException) {
            Timber.d("Caught exception $e here")
            return postDataToAmritServer(ancPostList)
        } catch (e: JSONException) {
            Timber.d("Caught exception $e here")
            return false
        }
    }

    private fun convertStringToIntList(villageIds: String): List<Int> {
        if (villageIds.trim().isEmpty()) return emptyList()
        return villageIds.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    suspend fun pullPncVisitsFromServer(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userRepo.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")
            try {
                val villageList = VillageIdList(
                    convertStringToIntList(user.assignVillageIds ?: ""),
                    preferenceDao.getLastPatientSyncTime()
                )

                val response = amritApiService.getAllPncVisits(villageList)
                if (response.code() != 200) {
                    Timber.w("Bad response from server for PNC getAll: $response")
                    return@withContext false
                }

                val responseString = response.body()?.string()
                if (responseString.isNullOrBlank()) {
                    Timber.w("Empty response body for PNC getAll")
                    return@withContext false
                }

                val jsonObj = JSONObject(responseString)
                val responseStatusCode = jsonObj.optInt("statusCode", 200)
                val errorMessage = jsonObj.optString("errorMessage")

                when (responseStatusCode) {
                    200 -> {
                        val dataArray = when (val dataNode = jsonObj.opt("data")) {
                            is JSONArray -> dataNode
                            is JSONObject -> dataNode.optJSONArray("data")
                            else -> null
                        } ?: JSONArray()

                        val gson = Gson()
                        var savedCount = 0
                        for (i in 0 until dataArray.length()) {
                            val networkModel = gson.fromJson(
                                dataArray.getJSONObject(i).toString(),
                                PNCNetwork::class.java
                            )

                            if (networkModel.benId == 0L) {
                                Timber.w("Skipping PNC getAll item with invalid benId at index=$i")
                                continue
                            }

                            val patient = patientDao.getPatientByAnyBeneficiaryId(networkModel.benId)
                            if (patient == null) {
                                Timber.w("No local patient found for PNC benId=${networkModel.benId}, skipping")
                                continue
                            }

                            if (networkModel.pncPeriod <= 0) {
                                Timber.w("Skipping PNC getAll item with invalid pncPeriod at index=$i")
                                continue
                            }

                            val incoming = networkModel.toCacheModel(patient.patientID)
                            val existing = pncDao.getSavedRecord(patient.patientID, networkModel.pncPeriod)
                            val merged = if (existing != null) {
                                incoming.copy(
                                    id = existing.id,
                                    createdDate = if (existing.createdDate > 0L) existing.createdDate else incoming.createdDate,
                                    createdBy = if (existing.createdBy.isNotBlank()) existing.createdBy else incoming.createdBy
                                )
                            } else incoming

                            pncDao.insert(merged)
                            savedCount++
                        }

                        Timber.d("PNC getAll downsync completed, saved=$savedCount received=${dataArray.length()}")
                        return@withContext true
                    }

                    5000 -> {
                        Timber.d("No PNC records found on server")
                        return@withContext true
                    }

                    5002 -> {
                        if (userRepo.refreshTokenTmc(user.userName, user.password)) {
                            throw SocketTimeoutException()
                        }
                    }

                    else -> {
                        Timber.w("PNC getAll failed: $errorMessage")
                        throw IOException("PNC getAll failed with statusCode=$responseStatusCode")
                    }
                }
                return@withContext false
            } catch (e: SocketTimeoutException) {
                Timber.d("Caught timeout for PNC getAll $e; retrying")
                return@withContext pullPncVisitsFromServer()
            } catch (e: JSONException) {
                Timber.d("Caught JSON exception for PNC getAll $e")
                return@withContext false
            }
        }
    }

    private fun PNCNetwork.toCacheModel(patientID: String): PNCVisitCache {
        return PNCVisitCache(
            id = id,
            patientID = patientID,
            pncPeriod = pncPeriod,
            isActive = isActive,
            pncDate = getLongFromDate(pncDate),
            ifaTabsGiven = ifaTabsGiven,
            calciumSupplementation = calciumSupplementation,
            anyContraceptionMethod = anyContraceptionMethod,
            contraceptionMethod = contraceptionMethod,
            sterilisationDate = sterilisationDate?.let { getLongFromDate(it) },
            otherPpcMethod = otherPpcMethod,
            anyDangerSign = anyDangerSign,
            motherDangerSign = motherDangerSign,
            otherDangerSign = otherDangerSign,
            maternalSymptoms = maternalSymptoms,
            otherMaternalSymptoms = otherMaternalSymptoms,
            pallor = pallor,
            vaginalBleeding = vaginalBleeding,
            referralFacility = referralFacility,
            motherDeath = motherDeath,
            deathDate = deathDate?.let { getLongFromDate(it) },
            causeOfDeath = causeOfDeath,
            otherDeathCause = otherDeathCause,
            placeOfDeath = placeOfDeath,
            otherPlaceOfDeath = otherPlaceOfDeath,
            remarks = remarks,
            processed = "P",
            createdBy = createdBy,
            createdDate = getLongFromDate(createdDate),
            updatedBy = updatedBy,
            updatedDate = getLongFromDate(updatedDate),
            syncState = SyncState.SYNCED
        )
    }
//
//
//    //PULL
//    suspend fun getPncVisitsFromServer(): Int {
//        return withContext(Dispatchers.IO) {
//            val user =
//                preferenceDao.getLoggedInUser()
//                    ?: throw IllegalStateException("No user logged in!!")
//            val lastTimeStamp = Konstants.defaultTimeStamp
//            try {
//                val response = amritApiService.getPncVisitsData(
//                    GetDataPaginatedRequest(
//                        ashaId = user.userId,
//                        pageNo = 0,
//                        fromDate = MaternalHealthRepo.getCurrentDate(lastTimeStamp),
//                        toDate = MaternalHealthRepo.getCurrentDate()
//                    )
//                )
//                val statusCode = response.code()
//                if (statusCode == 200) {
//                    val responseString = response.body()?.string()
//                    if (responseString != null) {
//                        val jsonObj = JSONObject(responseString)
//
//                        val errorMessage = jsonObj.getString("errorMessage")
//                        val responseStatusCode = jsonObj.getInt("statusCode")
//                        Timber.d("Pull from amrit PNC Visit data : $responseStatusCode")
//                        when (responseStatusCode) {
//                            200 -> {
//                                try {
//                                    val dataObj = jsonObj.getString("data")
//                                    savePNCCacheFromResponse(dataObj)
//                                } catch (e: Exception) {
//                                    Timber.d("PNC visit entries not synced $e")
//                                    return@withContext 0
//                                }
//
//                                return@withContext 1
//                            }
//
//                            5002 -> {
//                                if (userRepo.refreshTokenTmc(
//                                        user.userName, user.password
//                                    )
//                                ) throw SocketTimeoutException("Refreshed Token!")
//                                else throw IllegalStateException("User Logged out!!")
//                            }
//
//                            5000 -> {
//                                if (errorMessage == "No record found") return@withContext 0
//                            }
//
//                            else -> {
//                                throw IllegalStateException("$responseStatusCode received, don't know what todo!?")
//                            }
//                        }
//                    }
//                }
//
//            } catch (e: SocketTimeoutException) {
//                Timber.d("get_pnc error : $e")
//                return@withContext -2
//
//            } catch (e: java.lang.IllegalStateException) {
//                Timber.d("get_pnc error : $e")
//                return@withContext -1
//            }
//            -1
//        }
//    }
//
//    private suspend fun savePNCCacheFromResponse(dataObj: String): List<PNCNetwork> {
//        val pncList =
//            Gson().fromJson(dataObj, Array<PNCNetwork>::class.java).toList()
//        pncList.forEach { pncDTO ->
//            val ancCache =
//                pncDao.getSavedRecord(pncDTO.benId, pncDTO.pncPeriod)
//            if (ancCache == null) {
//                pncDao.insert(pncDTO.asCacheModel())
//            }
//
//        }
//        return pncList
//    }
//
//    suspend fun setToInactive(eligBenIds: Set<Long>) {
//        withContext(Dispatchers.IO) {
//            val records = pncDao.getAllPNCs(eligBenIds)
//            records.forEach {
//                it.isActive = false
//                if (it.processed != "N") it.processed = "U"
//                it.syncState = SyncState.UNSYNCED
//                it.updatedDate = System.currentTimeMillis()
//                pncDao.update(it)
//            }
//        }
//    }

}
