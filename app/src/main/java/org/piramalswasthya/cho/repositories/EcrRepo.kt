package org.piramalswasthya.cho.repositories

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.InAppDb
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.ECTNetwork
import org.piramalswasthya.cho.model.EligibleCoupleRegCache
import org.piramalswasthya.cho.model.EligibleCoupleTrackingCache
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.VillageIdList
//import org.piramalswasthya.sakhi.database.room.SyncState
//import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
//import org.piramalswasthya.sakhi.helpers.Konstants
//import org.piramalswasthya.sakhi.model.BenRegCache
//import org.piramalswasthya.sakhi.model.EcrPost
//import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
//import org.piramalswasthya.sakhi.model.EligibleCoupleTrackingCache
//import org.piramalswasthya.sakhi.model.Gender
//import org.piramalswasthya.sakhi.model.HRPNonPregnantAssessCache
//import org.piramalswasthya.sakhi.network.AmritApiService
//import org.piramalswasthya.sakhi.network.GetDataPaginatedRequest
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

class EcrRepo @Inject constructor(
    private val amritApiService: AmritApiService,
    private val userRepo: UserRepo,
    private val database: InAppDb,
    private val patientRepo: PatientRepo,
    private val preferenceDao: PreferenceDao,
    private val tmcNetworkApiService: AmritApiService
) {

    // ===== Eligible Couple Registration Methods =====

    fun getAllPatientsWithECR() = database.ecrDao.getAllPatientsWithECR()

    suspend fun getPatientWithECR(patientId: String) =
        database.ecrDao.getPatientWithECR(patientId)

    suspend fun getSavedECR(patientId: String) =
        database.ecrDao.getSavedECR(patientId)

    suspend fun saveECR(ecrCache: org.piramalswasthya.cho.model.EligibleCoupleRegCache) {
        database.ecrDao.upsert(ecrCache)
    }

    suspend fun updateECR(ecrCache: org.piramalswasthya.cho.model.EligibleCoupleRegCache) {
        database.ecrDao.update(ecrCache)
    }

    suspend fun getECRCount() =
        database.ecrDao.ecrCount()

    // ===== Eligible Couple Tracking Methods =====

    suspend fun getAllECT(patientID: String): List<EligibleCoupleTrackingCache> {
        return database.ecrDao.getAllECT(patientID)
    }

    suspend fun getEct(patientID: String, createdDate: Long): EligibleCoupleTrackingCache? {
        return database.ecrDao.getEct(patientID, createdDate)
    }

    fun getPatientsForTrackingList(): Flow<List<Patient>> {
        return database.ecrDao.getPatientsForTrackingList()
    }

    fun getEligibleCoupleTrackingCount(): Flow<Int> {
        return database.ecrDao.getEligibleCoupleTrackingCount()
    }

    suspend fun saveEct(eligibleCoupleTrackingCache: EligibleCoupleTrackingCache) {
        database.ecrDao.upsert(eligibleCoupleTrackingCache)
    }

    suspend fun resetEctAfterAbortion(patientID: String, createdBy: String) {
        val allEct = database.ecrDao.getAllECT(patientID)
        val mostRecent = allEct.maxByOrNull { it.visitDate }
        val hasActive = allEct.any { it.isActive }

        allEct.forEach { ect ->
            var changed = false
            if (ect.isPregnant != null) {
                ect.isPregnant = null
                changed = true
            }
            if (ect.pregnancyTestResult != null) {
                ect.pregnancyTestResult = null
                changed = true
            }
            // Reactivate the most recent row only when nothing is currently active,
            // so the EC-tracking DAO (which filters on isActive = 1) keeps the
            // patient visible after the post-abortion statusOfWomanID = 1 reset.
            if (!hasActive && ect === mostRecent && !ect.isActive) {
                ect.isActive = true
                changed = true
            }
            if (changed) {
                if (ect.processed != "N") ect.processed = "U"
                ect.syncState = SyncState.UNSYNCED
                database.ecrDao.upsert(ect)
            }
        }

        if (allEct.isEmpty()) {
            val freshEct = EligibleCoupleTrackingCache(
                patientID = patientID,
                visitDate = System.currentTimeMillis(),
                createdBy = createdBy,
                updatedBy = createdBy,
                processed = "N",
                isActive = true,
                syncState = SyncState.UNSYNCED
            )
            database.ecrDao.upsert(freshEct)
        }
    }


    suspend fun pushAndUpdateEctRecord(): Boolean {
        return withContext(Dispatchers.IO) {
            val ectList = database.ecrDao.getAllUnprocessedECT()
            if (ectList.isEmpty()) {
                Timber.d("No unsynced EC tracking records found for upload")
                return@withContext true
            }

            val ectPostList = mutableSetOf<EligibleCoupleTrackingCache>()
            var hasFailures = false

            ectList.forEach {
                ectPostList.clear()
                ectPostList.add(it)
                it.syncState = SyncState.SYNCING
                database.ecrDao.updateEligibleCoupleTracking(it)
                val uploadDone = postECTDataToAmritServer(ectPostList)
                if (uploadDone) {
                    it.processed = "P"
                    it.syncState = SyncState.SYNCED
                } else {
                    it.syncState = SyncState.UNSYNCED
                    hasFailures = true
                }
                database.ecrDao.updateEligibleCoupleTracking(it)
//                if(!uploadDone)
//                    return@withContext false
            }

            return@withContext !hasFailures
        }
    }

    private suspend fun postECTDataToAmritServer(ectPostList: MutableSet<EligibleCoupleTrackingCache>): Boolean {
        if (ectPostList.isEmpty()) return false

        val user =
            userRepo.getLoggedInUser() ?: throw IllegalStateException("No user logged in!!")
        try {
            val ectPayload = ectPostList.toList().mapNotNull { cache ->
                val benId = runCatching { patientRepo.getPatient(cache.patientID).beneficiaryID }
                    .getOrNull()
                if (benId == null || benId <= 0L) {
                    Timber.w("Skipping ECT upload: unresolved beneficiaryID for patient ${cache.patientID}")
                    null
                } else {
                    cache.asNetworkModel(benId)
                }
            }

            if (ectPayload.isEmpty()) {
                Timber.w("Skipping ECT upload: no valid beneficiary IDs found in payload")
                return false
            }

            Timber.d("Uploading EC tracking payload count=${ectPayload.size}")
            val response =
                amritApiService.postEctForm(ectPayload)
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
                                        user.userName, user.password
                                    )
                                ) throw SocketTimeoutException()
                            }

                            else -> {
                                Log.d("error ecr is ", errormessage)
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
            Timber.w("Bad Response from server, need to check $ectPostList $response ")
            return false
        } catch (e: SocketTimeoutException) {
            Timber.d("Caught exception $e here")
            return postECTDataToAmritServer(ectPostList)
        } catch (e: JSONException) {
            Timber.d("Caught exception $e here")
            return false
        }
    }

    suspend fun getLatestEctByBenId(benId: String): EligibleCoupleTrackingCache? {
        return database.ecrDao.getLatestEct(benId)
    }

    // ===== Pull Eligible Couples from Server =====

    suspend fun pullEligibleCouplesFromServer(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userRepo.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")
            try {
                val villageList = VillageIdList(
                    RepositorySyncUtils.parseVillageIds(user.assignVillageIds ?: ""),
                    preferenceDao.getLastPatientSyncTime()
                )
                val response = amritApiService.getEligibleCouples(villageList)
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)
                        val errorMessage = jsonObj.optString("errorMessage")
                        val responseStatusCode = jsonObj.optInt("statusCode", 200)
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataArray = when (val dataNode = jsonObj.opt("data")) {
                                        is org.json.JSONArray -> dataNode
                                        is org.json.JSONObject -> dataNode.optJSONArray("data")
                                        else -> null
                                    } ?: org.json.JSONArray()
                                    val gson = Gson()
                                    var savedCount = 0
                                    for (i in 0 until dataArray.length()) {
                                        val ectNetwork = gson.fromJson(
                                            dataArray.getJSONObject(i).toString(),
                                            ECTNetwork::class.java
                                        )
                                        val benId = ectNetwork.benId
                                        if (benId == null || benId == 0L) {
                                            Timber.w("Skipping EC record: benId missing/invalid in payload index=$i")
                                            continue
                                        }
                                        // Map server benId to local patientID.
                                        // benId may come as beneficiaryID or beneficiaryRegID depending on backend source.
                                        val patient = patientRepo.getPatientByAnyBeneficiaryId(benId)
                                        if (patient != null) {
                                            val existingEct = database.ecrDao.getEct(
                                                patient.patientID,
                                                org.piramalswasthya.cho.network.getLongFromDate(ectNetwork.visitDate)
                                            ) ?: database.ecrDao.getEctByVisitDay(
                                                patient.patientID,
                                                org.piramalswasthya.cho.network.getLongFromDate(ectNetwork.visitDate)
                                            )
                                            if (existingEct == null) {
                                                val cache = ectNetwork.toCache(patient.patientID)
                                                database.ecrDao.upsert(cache)
                                                savedCount++
                                                Timber.d("Saved EC tracking record for patient ${patient.patientID}")
                                            } else {
                                                Timber.d("EC tracking record already exists for patient ${patient.patientID} on ${ectNetwork.visitDate}")
                                            }
                                            val ecrSource = existingEct ?: ectNetwork.toCache(patient.patientID)
                                            ensureEcrRowForTrackedPatient(patient.patientID, ecrSource, user.userName)
                                        } else {
                                            Timber.w("No local patient found for benId $benId, skipping EC record")
                                        }
                                    }
                                    Timber.d("EC downsync completed, received=${dataArray.length()} saved=$savedCount")
                                } catch (e: Exception) {
                                    Timber.e(e, "Error parsing EC downsync data")
                                    return@withContext false
                                }
                                return@withContext true
                            }
                            5002 -> {
                                if (userRepo.refreshTokenTmc(
                                        user.userName, user.password
                                    )
                                ) throw SocketTimeoutException()
                            }
                            5000 -> {
                                Timber.d("No EC records found on server")
                                return@withContext true
                            }
                            else -> {
                                Log.d("error ec pull", errorMessage)
                                throw IOException("EC pull failed with code $responseStatusCode")
                            }
                        }
                    }
                }
                Timber.w("Bad response from server for EC pull: $response")
                return@withContext false
            } catch (e: SocketTimeoutException) {
                Timber.d("Caught timeout exception $e, retrying EC pull")
                return@withContext pullEligibleCouplesFromServer()
            } catch (e: JSONException) {
                Timber.d("Caught JSON exception $e for EC pull")
                return@withContext false
            }
        }
    }

    private suspend fun ensureEcrRowForTrackedPatient(
        patientId: String,
        trackingCache: EligibleCoupleTrackingCache,
        defaultUserName: String
    ) {
        val existingEcr = database.ecrDao.getSavedECR(patientId)
        if (existingEcr != null) {
            if ((existingEcr.lmpDate == null || existingEcr.lmpDate == 0L) && trackingCache.lmpDate != null) {
                existingEcr.lmpDate = trackingCache.lmpDate
                existingEcr.syncState = SyncState.SYNCED
                existingEcr.processed = "P"
                database.ecrDao.update(existingEcr)
            }
            return
        }

        val ecr = EligibleCoupleRegCache(
            patientID = patientId,
            dateOfReg = trackingCache.createdDate,
            lmpDate = trackingCache.lmpDate,
            noOfChildren = 0,
            noOfLiveChildren = 0,
            noOfMaleChildren = 0,
            noOfFemaleChildren = 0,
            isRegistered = true,
            processed = "P",
            createdBy = trackingCache.createdBy.ifBlank { defaultUserName },
            createdDate = trackingCache.createdDate,
            updatedBy = trackingCache.updatedBy.ifBlank { defaultUserName },
            syncState = SyncState.SYNCED
        )
        database.ecrDao.upsert(ecr)
    }
}
