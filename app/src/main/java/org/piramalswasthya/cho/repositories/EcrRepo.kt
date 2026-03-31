package org.piramalswasthya.cho.repositories

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.InAppDb
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.ECTNetwork
import org.piramalswasthya.cho.model.EligibleCoupleTrackingCache
import org.piramalswasthya.cho.network.AmritApiService
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

    suspend fun saveEct(eligibleCoupleTrackingCache: EligibleCoupleTrackingCache) {
        database.ecrDao.upsert(eligibleCoupleTrackingCache)
    }


    suspend fun pushAndUpdateEctRecord(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userRepo.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")

            val ectList = database.ecrDao.getAllUnprocessedECT()

            val ectPostList = mutableSetOf<EligibleCoupleTrackingCache>()

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
                }
                database.ecrDao.updateEligibleCoupleTracking(it)
//                if(!uploadDone)
//                    return@withContext false
            }

            return@withContext true
        }
    }

    private suspend fun postECTDataToAmritServer(ectPostList: MutableSet<EligibleCoupleTrackingCache>): Boolean {
        if (ectPostList.isEmpty()) return false

        val user =
            userRepo.getLoggedInUser() ?: throw IllegalStateException("No user logged in!!")
        try {
            val response =
                amritApiService.postEctForm(
                    ectPostList.toList()
                    .filter{ ben -> patientRepo.getPatient(ben.patientID).beneficiaryID != null }
                    .map {
                        val pat = patientRepo.getPatient(it.patientID)
                        it.asNetworkModel(pat.beneficiaryID!!)
                    }
                )
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
                val response = amritApiService.getEligibleCouples()
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)
                        val errorMessage = jsonObj.getString("errorMessage")
                        if (jsonObj.isNull("statusCode"))
                            throw IllegalStateException("Amrit server not responding properly, Contact Service Administrator!!")
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataArray = jsonObj.getJSONArray("data")
                                    val gson = Gson()
                                    for (i in 0 until dataArray.length()) {
                                        val ectNetwork = gson.fromJson(
                                            dataArray.getJSONObject(i).toString(),
                                            ECTNetwork::class.java
                                        )
                                        // Map server benId to local patientID
                                        val patient = database.patientDao.getBen(ectNetwork.benId)
                                        if (patient != null) {
                                            val existingEct = database.ecrDao.getEct(
                                                patient.patientID,
                                                org.piramalswasthya.cho.network.getLongFromDate(ectNetwork.visitDate)
                                            )
                                            if (existingEct == null) {
                                                val cache = ectNetwork.toCache(patient.patientID)
                                                database.ecrDao.upsert(cache)
                                                Timber.d("Saved EC tracking record for patient ${patient.patientID}")
                                            } else {
                                                Timber.d("EC tracking record already exists for patient ${patient.patientID} on ${ectNetwork.visitDate}")
                                            }
                                        } else {
                                            Timber.w("No local patient found for benId ${ectNetwork.benId}, skipping EC record")
                                        }
                                    }
                                    Timber.d("EC downsync completed, processed ${dataArray.length()} records")
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
}