package org.piramalswasthya.cho.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.InAppDb
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.PregnantWomanAncCache
import org.piramalswasthya.cho.model.PregnantWomanRegistrationCache
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.MaternalHealthDao
import org.piramalswasthya.cho.database.room.dao.PatientDao
//import org.piramalswasthya.sakhi.database.room.dao.BenDao
//import org.piramalswasthya.sakhi.database.room.dao.MaternalHealthDao
import org.piramalswasthya.cho.helpers.Konstants
import org.piramalswasthya.cho.model.ANCPost
//import org.piramalswasthya.sakhi.helpers.getTodayMillis
//import org.piramalswasthya.sakhi.model.*
//import org.piramalswasthya.sakhi.network.GetDataPaginatedRequest
//import org.piramalswasthya.sakhi.network.getLongFromDate
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MaternalHealthRepo @Inject constructor(
    private val amritApiService: AmritApiService,
    private val maternalHealthDao: MaternalHealthDao,
    private val database: InAppDb,
    private val userRepo: UserRepo,
    private val patientDao: PatientDao,
    private val preferenceDao: PreferenceDao,
) {

    suspend fun getSavedRegistrationRecord(benId: String): PregnantWomanRegistrationCache? {
        return withContext(Dispatchers.IO) {
            maternalHealthDao.getSavedRecord(benId)
        }
    }

    fun getSavedActiveRecordObserve(benId: String): LiveData<PregnantWomanRegistrationCache?> {
        return maternalHealthDao.getSavedActiveRecordObserve(benId)
    }

    suspend fun getActiveRegistrationRecord(benId: String): PregnantWomanRegistrationCache? {
        return withContext(Dispatchers.IO) {
            maternalHealthDao.getSavedActiveRecord(benId)
        }
    }
//

    fun getLastVisitNumber(benId: String): LiveData<Int?> {
        return maternalHealthDao.getLastVisitNumber(benId)
    }

    suspend fun getSavedAncRecord(benId: String, visitNumber: Int): PregnantWomanAncCache? {
        return withContext(Dispatchers.IO) {
            maternalHealthDao.getSavedRecord(benId, visitNumber)
        }
    }


//
//    suspend fun getLatestAncRecord(benId: Long): PregnantWomanAncCache? {
//        return withContext(Dispatchers.IO) {
//            maternalHealthDao.getLatestAnc(benId)
//        }
//    }
//
    suspend fun getAllActiveAncRecords(benId: String): List<PregnantWomanAncCache> {
        return withContext(Dispatchers.IO) {
            maternalHealthDao.getAllActiveAncRecords(benId)
        }
    }

    fun getAllAncRecords(benId: String): LiveData<List<PregnantWomanAncCache>> {
        return maternalHealthDao.getAllAncRecords(benId)
    }
//
//    suspend fun getBenFromId(benId: Long): BenRegCache? {
//        return withContext(Dispatchers.IO) {
//            benDao.getBen(benId)
//        }
//    }
//
    suspend fun persistRegisterRecord(pregnancyRegistrationForm: PregnantWomanRegistrationCache) {
        withContext(Dispatchers.IO) {
            maternalHealthDao.saveRecord(pregnancyRegistrationForm)
        }
    }
//
//
//    suspend fun getAllAncRecords(benId: Long): List<AncStatus> {
//        return withContext(Dispatchers.IO) {
//            maternalHealthDao.getAllAncRecordsFor(benId)
//        }
//    }
//
    suspend fun persistAncRecord(ancCache: PregnantWomanAncCache) {
        withContext(Dispatchers.IO) {
            maternalHealthDao.saveRecord(ancCache)
        }
    }

    suspend fun updateAncRecord(ancCache: Array<PregnantWomanAncCache>) {
        withContext(Dispatchers.IO) {
            maternalHealthDao.updateANC(*ancCache)
        }
    }

//    @OptIn(ExperimentalCoroutinesApi::class)
//    val ancDueCount = maternalHealthDao.getAllPregnancyRecords().transformLatest {
//        Timber.d("From DB : ${it.count()}")
//        var count = 0
//        val notDeliveredList = it.filter { !it.value.any { it.pregnantWomanDelivered == true } }
//        notDeliveredList.keys.forEach { activePwrRecrod ->
//            val savedAncRecords = it[activePwrRecrod] ?: emptyList()
//            val isDue = if (savedAncRecords.isEmpty())
//                TimeUnit.MILLISECONDS.toDays(
//                    getTodayMillis() - activePwrRecrod.lmpDate
//                ) >= Konstants.minAnc1Week * 7
//            else {
//                val lastAncRecord = savedAncRecords.maxBy { it.visitNumber }
//                (activePwrRecrod.lmpDate + TimeUnit.DAYS.toMillis(280)) > (lastAncRecord.ancDate + TimeUnit.DAYS.toMillis(
//                    28
//                )) &&
//                        lastAncRecord.visitNumber < 4 && TimeUnit.MILLISECONDS.toDays(
//                    getTodayMillis() - lastAncRecord.ancDate
//                ) > 28
//            }
//            if (isDue)
//                count++
//        }
//        emit(count)
//    }
//
//
    suspend fun processNewAncVisit(): Boolean {
        return withContext(Dispatchers.IO) {
            val ancList = maternalHealthDao.getAllUnprocessedAncVisits()

            val ancPostList = mutableSetOf<ANCPost>()
            ancList.forEach {
                ancPostList.clear()
                val ben = patientDao.getPatient(it.patientID)
                if(ben.beneficiaryID != null){
                    ancPostList.add(it.asPostModel(ben.beneficiaryID!!))
                    it.syncState = SyncState.SYNCING
                    maternalHealthDao.updateANC(it)
                    val uploadDone = postDataToAmritServer(ancPostList)
                    if (uploadDone) {
                        it.processed = "P"
                        it.syncState = SyncState.SYNCED
                    } else {
                        it.syncState = SyncState.UNSYNCED
                    }
                    maternalHealthDao.updateANC(it)
//                if (!uploadDone)
//                    return@withContext false
                }
            }

            return@withContext true
        }
    }

    private suspend fun postDataToAmritServer(ancPostList: MutableSet<ANCPost>): Boolean {
        if (ancPostList.isEmpty()) return false
        val user =
            userRepo.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")

        try {

            val response = amritApiService.postAncForm(ancPostList.toList())
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
                                Log.d("anc error message", errormessage)
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

//    suspend fun processNewPwr(): Boolean {
//        return withContext(Dispatchers.IO) {
//            val user = preferenceDao.getLoggedInUser()
//                ?: throw IllegalStateException("No user logged in!!")
//
//            val pwrList = maternalHealthDao.getAllUnprocessedPWRs()
//
//            val pwrPostList = mutableSetOf<PwrPost>()
//
//            pwrList.forEach {
//                pwrPostList.clear()
//                val ben = benDao.getBen(it.benId)
//                    ?: throw IllegalStateException("No beneficiary exists for benId: ${it.benId}!!")
//                pwrPostList.add(it.asPwrPost())
//                it.syncState = SyncState.SYNCING
//                maternalHealthDao.updatePwr(it)
//                val uploadDone = postPwrToAmritServer(pwrPostList)
//                if (uploadDone) {
//                    it.processed = "P"
//                    it.syncState = SyncState.SYNCED
//                } else {
//                    it.syncState = SyncState.UNSYNCED
//                }
//                maternalHealthDao.updatePwr(it)
//                if (!uploadDone)
//                    return@withContext false
//            }
//
//            return@withContext true
//        }
//    }
//
//    suspend fun postPwrToAmritServer(pwrPostList: MutableSet<PwrPost>): Boolean {
//        if (pwrPostList.isEmpty()) return false
//        val user =
//            preferenceDao.getLoggedInUser()
//                ?: throw IllegalStateException("No user logged in!!")
//
//        try {
//
//            val response = amritApiService.postPwrForm(pwrPostList.toList())
//            val statusCode = response.code()
//
//            if (statusCode == 200) {
//                try {
//                    val responseString = response.body()?.string()
//                    if (responseString != null) {
//                        val jsonObj = JSONObject(responseString)
//
//                        val errormessage = jsonObj.getString("errorMessage")
//                        if (jsonObj.isNull("statusCode")) throw IllegalStateException("Amrit server not responding properly, Contact Service Administrator!!")
//
//                        when (jsonObj.getInt("statusCode")) {
//                            200 -> {
//                                Timber.d("Saved Successfully to server")
//                                return true
//                            }
//
//                            5002 -> {
//                                if (userRepo.refreshTokenTmc(
//                                        user.userName,
//                                        user.password
//                                    )
//                                ) throw SocketTimeoutException()
//                            }
//
//                            else -> {
//                                throw IOException("Throwing away IO eXcEpTiOn")
//                            }
//                        }
//                    }
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            } else {
//                //server_resp5();
//            }
//            Timber.w("Bad Response from server, need to check $pwrPostList $response ")
//            return false
//        } catch (e: SocketTimeoutException) {
//            Timber.d("Caught exception $e here")
//            return postPwrToAmritServer(pwrPostList)
//        } catch (e: JSONException) {
//            Timber.d("Caught exception $e here")
//            return false
//        }
//    }
//
//    suspend fun getPwrDetailsFromServer(): Int {
//        return withContext(Dispatchers.IO) {
//            val user =
//                preferenceDao.getLoggedInUser()
//                    ?: throw IllegalStateException("No user logged in!!")
//            val lastTimeStamp = Konstants.defaultTimeStamp
//            try {
//                val response = amritApiService.getPwrData(
//                    GetDataPaginatedRequest(
//                        ashaId = user.userId,
//                        pageNo = 0,
//                        fromDate = getCurrentDate(lastTimeStamp),
//                        toDate = getCurrentDate()
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
//                        Timber.d("Pull from amrit Pregnant women data : $responseStatusCode")
//                        when (responseStatusCode) {
//                            200 -> {
//                                try {
//                                    val dataObj = jsonObj.getString("data")
//                                    savePwrCacheFromResponse(dataObj)
//                                } catch (e: Exception) {
//                                    Timber.d("PWR entries not synced $e")
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
//                                throw IllegalStateException("$responseStatusCode received, dont know what todo!?")
//                            }
//                        }
//                    }
//                }
//
//            } catch (e: SocketTimeoutException) {
//                Timber.d("get_tb error : $e")
//                return@withContext -2
//
//            } catch (e: java.lang.IllegalStateException) {
//                Timber.d("get_tb error : $e")
//                return@withContext -1
//            }
//            -1
//        }
//    }
//
//    private suspend fun savePwrCacheFromResponse(dataObj: String): List<PwrPost> {
//        var pwrList =
//            Gson().fromJson(dataObj, Array<PwrPost>::class.java).toList()
//        pwrList.forEach { pwrDTO ->
//            pwrDTO.createdDate?.let {
//                var pwrCache: PregnantWomanRegistrationCache? =
//                    maternalHealthDao.getSavedRecord(pwrDTO.benId)
//                val hasBen = benDao.getBen(pwrDTO.benId) != null
//                benDao.getBen(pwrDTO.benId)?.let {
//                    if (hasBen && pwrCache == null && pwrDTO.isRegistered) {
//                        maternalHealthDao.saveRecord(pwrDTO.toPwrCache())
//                    }
//                    var assess = database.hrpDao.getPregnantAssess(pwrDTO.benId)
//                    if (assess == null) {
//                        database.hrpDao.saveRecord(
//                            HRPPregnantAssessCache(
//                                benId = pwrDTO.benId,
//                                visitDate = getLongFromDate(pwrDTO.createdDate),
//                                rhNegative = pwrDTO.rhNegative,
//                                homeDelivery = pwrDTO.homeDelivery,
//                                badObstetric = pwrDTO.badObstetric,
//                                lmpDate = getLongFromDate(pwrDTO.lmpDate),
//                                edd = getLongFromDate(pwrDTO.lmpDate) + TimeUnit.DAYS.toMillis(280),
//                                multiplePregnancy = if (!pwrDTO.isFirstPregnancyTest) "Yes" else "No",
//                                isHighRisk = isHighRisk(pwrDTO),
//                                syncState = SyncState.SYNCED
//                            )
//                        )
//                    } else {
//                        pwrDTO.rhNegative?.let {
//                            assess.rhNegative = pwrDTO.rhNegative
//                        }
//                        pwrDTO.homeDelivery?.let {
//                            assess.homeDelivery = pwrDTO.homeDelivery
//                        }
//                        pwrDTO.badObstetric?.let {
//                            assess.badObstetric = pwrDTO.badObstetric
//                        }
//                        assess.lmpDate = getLongFromDate(pwrDTO.lmpDate)
//                        assess.edd = getLongFromDate(pwrDTO.lmpDate) + TimeUnit.DAYS.toMillis(280)
//                        assess.multiplePregnancy = if (!pwrDTO.isFirstPregnancyTest) "Yes" else "No"
//                        assess.isHighRisk = assess.isHighRisk || isHighRisk(pwrDTO)
//                        database.hrpDao.saveRecord(assess)
//                    }
//                }
//            }
//        }
//        return pwrList
//    }
//
//    private fun isHighRisk(pwrDTO: PwrPost): Boolean {
//        return (pwrDTO.badObstetric == "Yes" ||
//                pwrDTO.rhNegative == "Yes" ||
//                pwrDTO.homeDelivery == "Yes" ||
//                !pwrDTO.isFirstPregnancyTest)
//    }
//
//    suspend fun getAncVisitDetailsFromServer(): Int {
//        return withContext(Dispatchers.IO) {
//            val user =
//                preferenceDao.getLoggedInUser()
//                    ?: throw IllegalStateException("No user logged in!!")
//            val lastTimeStamp = Konstants.defaultTimeStamp
//            try {
//                val response = amritApiService.getAncVisitsData(
//                    GetDataPaginatedRequest(
//                        ashaId = user.userId,
//                        pageNo = 0,
//                        fromDate = getCurrentDate(lastTimeStamp),
//                        toDate = getCurrentDate()
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
//                        Timber.d("Pull from amrit ANC Visit data : $responseStatusCode")
//                        when (responseStatusCode) {
//                            200 -> {
//                                try {
//                                    val dataObj = jsonObj.getString("data")
//                                    saveANCCacheFromResponse(dataObj)
//                                } catch (e: Exception) {
//                                    Timber.d("ANC visit entries not synced $e")
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
//                Timber.d("get_tb error : $e")
//                return@withContext -2
//
//            } catch (e: java.lang.IllegalStateException) {
//                Timber.d("get_tb error : $e")
//                return@withContext -1
//            }
//            -1
//        }
//    }
//
//    private suspend fun saveANCCacheFromResponse(dataObj: String): List<ANCPost> {
//        var ancList =
//            Gson().fromJson(dataObj, Array<ANCPost>::class.java).toList()
//        ancList.forEach { ancDTO ->
//            ancDTO.createdDate?.let {
//                val hasBen = benDao.getBen(ancDTO.benId) != null
//                val ancCache: PregnantWomanAncCache? =
//                    maternalHealthDao.getSavedRecord(ancDTO.benId, ancDTO.ancVisit)
//                if (hasBen && ancCache == null) {
//                    maternalHealthDao.saveRecord(ancDTO.toAncCache())
//                }
//            }
//        }
//        return ancList
//    }
//
//    suspend fun setToInactive(eligBenIds: Set<Long>) {
//        withContext(Dispatchers.IO) {
//            val records = maternalHealthDao.getAllActiveAncRecords(eligBenIds)
//            records.forEach {
//                it.isActive = false
//                if (it.processed != "N") it.processed = "U"
//                it.syncState = SyncState.UNSYNCED
//                it.updatedDate = System.currentTimeMillis()
//                maternalHealthDao.updateANC(it)
//            }
//            val records2 = maternalHealthDao.getAllActivePwrRecords(eligBenIds)
//            records2.forEach {
//                it.active = false
//                if (it.processed != "N") it.processed = "U"
//                it.syncState = SyncState.UNSYNCED
//                it.updatedDate = System.currentTimeMillis()
//                maternalHealthDao.updatePwr(it)
//            }
//        }
//    }
//
//    companion object {
//        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
//        private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
//        fun getCurrentDate(millis: Long = System.currentTimeMillis()): String {
//            val dateString = dateFormat.format(millis)
//            val timeString = timeFormat.format(millis)
//            return "${dateString}T${timeString}.000Z"
//        }
//    }
}