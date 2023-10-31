package org.piramalswasthya.cho.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.database.room.dao.PncDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.PNCNetwork
import org.piramalswasthya.cho.model.PNCVisitCache
import org.piramalswasthya.cho.network.AmritApiService
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

    fun getLastVisitNumber(patientID: String): LiveData<Int?> {
        return pncDao.getLastVisitNumber(patientID)
    }

    suspend fun persistPncRecord(pncCache: PNCVisitCache) {
        withContext(Dispatchers.IO) {
            pncDao.insert(pncCache)
        }

    }

    suspend fun processPncVisits(): Boolean {
        return withContext(Dispatchers.IO) {

            val pncList = pncDao.getAllUnprocessedPncVisits()

            val pncPostList = mutableSetOf<PNCNetwork>()

            pncList.forEach {
                pncPostList.clear()
                val ben = patientDao.getPatient(it.patientID)
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