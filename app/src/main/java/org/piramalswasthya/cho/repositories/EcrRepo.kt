package org.piramalswasthya.cho.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.InAppDb
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
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
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class EcrRepo @Inject constructor(
    private val amritApiService: AmritApiService,
    private val userRepo: UserRepo,
    private val database: InAppDb,
    private val preferenceDao: PreferenceDao,
    private val tmcNetworkApiService: AmritApiService
) {

//    suspend fun persistRecord(ecrForm: EligibleCoupleRegCache) {
//        withContext(Dispatchers.IO) {
//            database.ecrDao.upsert(ecrForm)
//        }
//    }
//
//    suspend fun getBenFromId(benId: Long): BenRegCache? {
//        return withContext(Dispatchers.IO) {
//            database.benDao.getBen(benId)
//        }
//    }
//
//    suspend fun getSavedRecord(benId: Long): EligibleCoupleRegCache? {
//        return withContext(Dispatchers.IO) {
//            database.ecrDao.getSavedECR(benId)
//        }
//    }
//
//    suspend fun getEct(benId: Long, createdDate: Long): EligibleCoupleTrackingCache? {
//        return withContext(Dispatchers.IO) {
//            database.ecrDao.getEct(benId, createdDate)
//        }
//    }
//
//    suspend fun saveEct(eligibleCoupleTrackingCache: EligibleCoupleTrackingCache) {
//        withContext(Dispatchers.IO) {
//            database.ecrDao.upsert(eligibleCoupleTrackingCache)
//        }
//    }
//
//    suspend fun pushAndUpdateEcrRecord(): Boolean {
//        return withContext(Dispatchers.IO) {
//            val ecrList = database.ecrDao.getAllUnprocessedECR()
//            val ecrPostList = mutableSetOf<EcrPost>()
//
//            ecrList.forEach {
//                ecrPostList.clear()
//                val ben = database.benDao.getBen(it.benId)
//                    ?: throw IllegalStateException("No beneficiary exists for benId: ${it.benId}!!")
//
//                ecrPostList.add(it.asPostModel())
//                it.syncState = SyncState.SYNCING
//                database.ecrDao.update(it)
//                val uploadDone = postECRDataToAmritServer(ecrPostList)
//                if (uploadDone) {
//                    it.processed = "P"
//                    it.syncState = SyncState.SYNCED
//                } else {
//                    it.syncState = SyncState.UNSYNCED
//                }
//                database.ecrDao.update(it)
//                if(!uploadDone)
//                    return@withContext false
//            }
//
//            return@withContext true
//        }
//    }
//
//    suspend fun postECRDataToAmritServer(ecrPostList: MutableSet<EcrPost>): Boolean {
//        if (ecrPostList.isEmpty()) return false
//
//        val user =
//            preferenceDao.getLoggedInUser() ?: throw IllegalStateException("No user logged in!!")
//
//        try {
//
//            val response = amritApiService.postEcrForm(ecrPostList.toList())
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
//                        val responsestatuscode = jsonObj.getInt("statusCode")
//
//                        when (responsestatuscode) {
//                            200 -> {
//                                Timber.d("Saved Successfully to server")
//                                return true
//                            }
//
//                            5002 -> {
//                                if (userRepo.refreshTokenTmc(
//                                        user.userName, user.password
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
//            Timber.w("Bad Response from server, need to check $ecrPostList $response ")
//            return false
//        } catch (e: SocketTimeoutException) {
//            Timber.d("Caught exception $e here")
//            return postECRDataToAmritServer(ecrPostList)
//        } catch (e: JSONException) {
//            Timber.d("Caught exception $e here")
//            return false
//        }
//    }
//
//    suspend fun pushAndUpdateEctRecord(): Boolean {
//        return withContext(Dispatchers.IO) {
//            val user = preferenceDao.getLoggedInUser()
//                ?: throw IllegalStateException("No user logged in!!")
//
//            val ectList = database.ecrDao.getAllUnprocessedECT()
//
//            val ectPostList = mutableSetOf<EligibleCoupleTrackingCache>()
//
//            ectList.forEach {
//                ectPostList.clear()
//                ectPostList.add(it)
//                it.syncState = SyncState.SYNCING
//                database.ecrDao.updateEligibleCoupleTracking(it)
//                val uploadDone = postECTDataToAmritServer(ectPostList)
//                if (uploadDone) {
//                    it.processed = "P"
//                    it.syncState = SyncState.SYNCED
//                } else {
//                    it.syncState = SyncState.UNSYNCED
//                }
//                database.ecrDao.updateEligibleCoupleTracking(it)
//                if(!uploadDone)
//                    return@withContext false
//            }
//
//            return@withContext true
//        }
//    }
//
//    private suspend fun postECTDataToAmritServer(ectPostList: MutableSet<EligibleCoupleTrackingCache>): Boolean {
//        if (ectPostList.isEmpty()) return false
//
//        val user =
//            preferenceDao.getLoggedInUser() ?: throw IllegalStateException("No user logged in!!")
//        try {
//
//            val response =
//                amritApiService.postEctForm(ectPostList.toList().map { it.asNetworkModel() })
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
//                        val responsestatuscode = jsonObj.getInt("statusCode")
//
//                        when (responsestatuscode) {
//                            200 -> {
//                                Timber.d("Saved Successfully to server")
//                                return true
//                            }
//
//                            5002 -> {
//                                if (userRepo.refreshTokenTmc(
//                                        user.userName, user.password
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
//            Timber.w("Bad Response from server, need to check $ectPostList $response ")
//            return false
//        } catch (e: SocketTimeoutException) {
//            Timber.d("Caught exception $e here")
//            return postECTDataToAmritServer(ectPostList)
//        } catch (e: JSONException) {
//            Timber.d("Caught exception $e here")
//            return false
//        }
//    }
//
//    suspend fun pullAndPersistEcrRecord(): Int {
//        return withContext(Dispatchers.IO) {
//            val user = preferenceDao.getLoggedInUser()
//                ?: throw IllegalStateException("No user logged in!!")
//            val lastTimeStamp = Konstants.defaultTimeStamp
//            try {
//                val response = tmcNetworkApiService.getEcrFormData(
//                    GetDataPaginatedRequest(
//                        user.userId, 0, getCurrentDate(lastTimeStamp), getCurrentDate()
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
//                        Timber.d("Pull from amrit eligible couple register data : $responseStatusCode")
//                        when (responseStatusCode) {
//                            200 -> {
//                                try {
//                                    val dataObj = jsonObj.getJSONArray("data")
//                                    val ecrList = getEcrCacheFromServerResponse(dataObj)
//                                    val assessList = getHighRiskAssess(dataObj)
//
//                                    ecrList.forEach { ecr ->
//                                        database.benDao.getBen(ecr.benId)?.let {
//                                            if (database.ecrDao.getSavedECR(ecr.benId) == null) {
//                                                database.ecrDao.upsert(ecr)
//                                            }
//                                        }
//                                    }
//                                    assessList.forEach { it1 ->
//                                        val assess = database.hrpDao.getNonPregnantAssess(it1.benId)
//                                        database.benDao.getBen(it1.benId)?.let {
//                                            if (assess == null) {
//                                                database.hrpDao.saveRecord(it1)
//                                            } else {
//                                                it1.misCarriage?.let {
//                                                    assess.misCarriage = it1.misCarriage
//                                                }
//                                                it1.homeDelivery?.let {
//                                                    assess.homeDelivery = it1.homeDelivery
//                                                }
//                                                it1.medicalIssues?.let {
//                                                    assess.medicalIssues = it1.medicalIssues
//                                                }
//                                                it1.pastCSection?.let {
//                                                    assess.pastCSection = it1.pastCSection
//                                                }
//                                                assess.isHighRisk = it1.isHighRisk
//                                                database.hrpDao.saveRecord(assess)
//                                            }
//                                        }
//                                    }
//                                } catch (e: Exception) {
//                                    Timber.d("Eligible Couple entries not synced $e")
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
//                Timber.d("get_ect error : $e")
//                pullAndPersistEcrRecord()
//
//            } catch (e: java.lang.IllegalStateException) {
//                Timber.d("get_ect error : $e")
//                return@withContext -1
//            }
//            -1
//        }
//    }
//
//
//    suspend fun pullAndPersistEctRecord(): Int {
//        return withContext(Dispatchers.IO) {
//            val user = preferenceDao.getLoggedInUser()
//                ?: throw IllegalStateException("No user logged in!!")
//            val lastTimeStamp = Konstants.defaultTimeStamp
//            try {
//                val response = tmcNetworkApiService.getEctFormData(
//                    GetDataPaginatedRequest(
//                        user.userId, 0, getCurrentDate(lastTimeStamp), getCurrentDate()
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
//                        Timber.d("Pull from amrit eligible couple register data : $responseStatusCode")
//                        when (responseStatusCode) {
//                            200 -> {
//                                try {
//                                    val dataObj = jsonObj.getJSONArray("data")
//                                    val ecrList = getEctCacheFromServerResponse(dataObj)
//                                    ecrList.filter {
//                                        !database.ecrDao.ectWithsameCreateDateExists(it.createdDate) && database.benDao.getBen(
//                                            it.benId
//                                        ) != null
//                                    }.takeIf { it.isNotEmpty() }?.let {
//                                        database.ecrDao.upsert(*it.toTypedArray())
//                                    }
//                                } catch (e: Exception) {
//                                    Timber.d("EC entries not synced $e")
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
//                Timber.d("get_ect error : $e")
//                pullAndPersistEctRecord()
//
//            } catch (e: java.lang.IllegalStateException) {
//                Timber.d("get_ect error : $e")
//                return@withContext -1
//            }
//            -1
//        }
//    }
//
//    private fun getEcrCacheFromServerResponse(dataObj: JSONArray): List<EligibleCoupleRegCache> {
////        TODO()
//        val list = mutableListOf<EligibleCoupleRegCache>()
//        var numMale: Int
//        var numFemale: Int
//        for (i in 0 until dataObj.length()) {
//            val ecrJson = dataObj.getJSONObject(i)
//            numMale = 0
//            numFemale = 0
//            try {
//                val ecr = EligibleCoupleRegCache(
//                    benId = ecrJson.getLong("benId"),
//                    dateOfReg = if (ecrJson.has("registrationDate"))
//                        getLongFromDate(ecrJson.getString("registrationDate")
//                    ) else getLongFromDate(
//                        ecrJson.getString("createdDate")
//                    ),
//                    bankAccount = if (ecrJson.has("bankAccountNumber")) ecrJson.getLong("bankAccountNumber") else null,
//                    bankName = if (ecrJson.has("bankName")) ecrJson.getString("bankName") else null,
//                    branchName = if (ecrJson.has("branchName")) ecrJson.getString("branchName") else null,
//                    ifsc = if (ecrJson.has("ifsc")) ecrJson.getString("ifsc") else null,
//                    noOfChildren = if (ecrJson.has("numChildren")) ecrJson.getInt("numChildren") else 0,
////                    noOfLiveChildren = if (ecrJson.has("noOfLiveChildren")) ecrJson.getInt("noOfLiveChildren") else 0,
////                    noOfMaleChildren = if (ecrJson.has("noOfMaleChildren")) ecrJson.getInt("noOfMaleChildren") else 0,
////                    noOfFemaleChildren = if (ecrJson.has("noOfFemaleChildren")) ecrJson.getInt("noOfFemaleChildren") else 0,
//                    dob1 = if (ecrJson.has("dob1")) getLongFromDate(ecrJson.getString("dob1")) else null,
//                    age1 = if (ecrJson.has("age1")) ecrJson.getInt("age1") else null,
//                    gender1 = if (ecrJson.has("gender1")) ecrJson.getString("gender1")
//                        .uppercase()
//                        .let {
//                            Gender.valueOf(it)
//                                .also { if (it == Gender.MALE) numMale++ else numFemale++ }
//                        } else null,
//                    marriageFirstChildGap = if (ecrJson.has("marriageFirstChildGap")) ecrJson.getInt(
//                        "marriageFirstChildGap"
//                    ) else null,
//                    dob2 = if (ecrJson.has("dob2")) getLongFromDate(ecrJson.getString("dob2")) else null,
//                    age2 = if (ecrJson.has("age2")) ecrJson.getInt("age2") else null,
//                    gender2 = if (ecrJson.has("gender2")) ecrJson.getString("gender2")
//                        .uppercase()
//                        .let {
//                            Gender.valueOf(it)
//                                .also { if (it == Gender.MALE) numMale++ else numFemale++ }
//                        } else null,
//                    firstAndSecondChildGap = if (ecrJson.has("firstAndSecondChildGap")) ecrJson.getInt(
//                        "firstAndSecondChildGap"
//                    ) else null,
//                    dob3 = if (ecrJson.has("dob3")) getLongFromDate(ecrJson.getString("dob3")) else null,
//                    age3 = if (ecrJson.has("age3")) ecrJson.getInt("age3") else null,
//                    gender3 = if (ecrJson.has("gender3")) ecrJson.getString("gender3")
//                        .uppercase()
//                        .let {
//                            Gender.valueOf(it)
//                                .also { if (it == Gender.MALE) numMale++ else numFemale++ }
//                        } else null,
//                    secondAndThirdChildGap = if (ecrJson.has("secondAndThirdChildGap")) ecrJson.getInt(
//                        "secondAndThirdChildGap"
//                    ) else null,
//                    dob4 = if (ecrJson.has("dob4")) getLongFromDate(ecrJson.getString("dob4")) else null,
//                    age4 = if (ecrJson.has("age4")) ecrJson.getInt("age4") else null,
//                    gender4 = if (ecrJson.has("gender4")) ecrJson.getString("gender4")
//                        .uppercase()
//                        .let {
//                            Gender.valueOf(it)
//                                .also { if (it == Gender.MALE) numMale++ else numFemale++ }
//                        } else null,
//                    thirdAndFourthChildGap = if (ecrJson.has("thirdAndFourthChildGap")) ecrJson.getInt(
//                        "thirdAndFourthChildGap"
//                    ) else null,
//                    dob5 = if (ecrJson.has("dob5")) getLongFromDate(ecrJson.getString("dob5")) else null,
//                    age5 = if (ecrJson.has("age5")) ecrJson.getInt("age5") else null,
//                    gender5 = if (ecrJson.has("gender5")) ecrJson.getString("gender5")
//                        .uppercase()
//                        .let {
//                            Gender.valueOf(it)
//                                .also { if (it == Gender.MALE) numMale++ else numFemale++ }
//                        } else null,
//                    fourthAndFifthChildGap = if (ecrJson.has("fourthAndFifthChildGap")) ecrJson.getInt(
//                        "fourthAndFifthChildGap"
//                    ) else null,
//                    dob6 = if (ecrJson.has("dob6")) getLongFromDate(ecrJson.getString("dob6")) else null,
//                    age6 = if (ecrJson.has("age6")) ecrJson.getInt("age6") else null,
//                    gender6 = if (ecrJson.has("gender6")) ecrJson.getString("gender6")
//                        .uppercase()
//                        .let {
//                            Gender.valueOf(it)
//                                .also { if (it == Gender.MALE) numMale++ else numFemale++ }
//                        } else null,
//                    fifthANdSixthChildGap = if (ecrJson.has("fifthANdSixthChildGap")) ecrJson.getInt(
//                        "fifthANdSixthChildGap"
//                    ) else null,
//                    dob7 = if (ecrJson.has("dob7")) getLongFromDate(ecrJson.getString("dob7")) else null,
//                    age7 = if (ecrJson.has("age7")) ecrJson.getInt("age7") else null,
//                    gender7 = if (ecrJson.has("gender7")) ecrJson.getString("gender7")
//                        .uppercase()
//                        .let {
//                            Gender.valueOf(it)
//                                .also { if (it == Gender.MALE) numMale++ else numFemale++ }
//                        } else null,
//                    sixthAndSeventhChildGap = if (ecrJson.has("sixthAndSeventhChildGap")) ecrJson.getInt(
//                        "sixthAndSeventhChildGap"
//                    ) else null,
//                    dob8 = if (ecrJson.has("dob8")) getLongFromDate(ecrJson.getString("dob8")) else null,
//                    age8 = if (ecrJson.has("age8")) ecrJson.getInt("age8") else null,
//                    gender8 = if (ecrJson.has("gender8")) ecrJson.getString("gender8")
//                        .uppercase()
//                        .let {
//                            Gender.valueOf(it)
//                                .also { if (it == Gender.MALE) numMale++ else numFemale++ }
//                        } else null,
//                    seventhAndEighthChildGap = if (ecrJson.has("seventhAndEighthChildGap")) ecrJson.getInt(
//                        "seventhAndEighthChildGap"
//                    ) else null,
//                    dob9 = if (ecrJson.has("dob9")) getLongFromDate(ecrJson.getString("dob9")) else null,
//                    age9 = if (ecrJson.has("age9")) ecrJson.getInt("age9") else null,
//                    gender9 = if (ecrJson.has("gender9")) ecrJson.getString("gender9")
//                        .uppercase()
//                        .let {
//                            Gender.valueOf(it)
//                                .also { if (it == Gender.MALE) numMale++ else numFemale++ }
//                        } else null,
//                    eighthAndNinthChildGap = if (ecrJson.has("eighthAndNinthChildGap")) ecrJson.getInt(
//                        "eighthAndNinthChildGap"
//                    ) else null,
//                    noOfLiveChildren = numMale + numFemale,
//                    noOfMaleChildren = numMale,
//                    noOfFemaleChildren = numFemale,
//                    isRegistered = if (ecrJson.has("isRegistered")) ecrJson.getBoolean("isRegistered") else false,
//                    processed = "P",
//                    createdBy = ecrJson.getString("createdBy"),
//                    createdDate = getLongFromDate(
//                        ecrJson.getString("createdDate")
//                    ),
//                    updatedBy = if (ecrJson.has("updatedBy")) ecrJson.getString("updatedBy") else ecrJson.getString(
//                        "createdBy"
//                    ),
//                    updatedDate = getLongFromDate(
//                        if (ecrJson.has("updatedDate")) ecrJson.getString(
//                            "updatedDate"
//                        ) else ecrJson.getString("createdDate")
//                    ),
//                    syncState = SyncState.SYNCED
//                )
//                if (ecr.isRegistered) list.add(ecr)
//            } catch (e: Exception) {
//                Timber.e("Caught $e at ECR PULL")
//            }
//
//        }
//
//        return list
//    }
//
//    private fun getEctCacheFromServerResponse(dataObj: JSONArray): List<EligibleCoupleTrackingCache> {
////        TODO()
//        val list = mutableListOf<EligibleCoupleTrackingCache>()
//        for (i in 0 until dataObj.length()) {
//            val ecrJson = dataObj.getJSONObject(i)
//            val ecr = EligibleCoupleTrackingCache(
//                benId = ecrJson.getLong("benId"),
//                visitDate = getLongFromDate(ecrJson.getString("visitDate")),
//                isPregnancyTestDone = if (ecrJson.has("isPregnancyTestDone")) ecrJson.getString("isPregnancyTestDone") else null,
//                isActive = if (ecrJson.has("isActive")) ecrJson.getBoolean("isActive") else false,
//                pregnancyTestResult = if (ecrJson.has("pregnancyTestResult")) ecrJson.getString("pregnancyTestResult") else null,
//                isPregnant = if (ecrJson.has("isPregnant")) ecrJson.getString("isPregnant") else null,
//                usingFamilyPlanning = if (ecrJson.has("usingFamilyPlanning")) ecrJson.getBoolean("usingFamilyPlanning") else null,
//                methodOfContraception = if (ecrJson.has("methodOfContraception")) ecrJson.getString(
//                    "methodOfContraception"
//                ) else null,
//                createdBy = ecrJson.getString("createdBy"),
//                createdDate = getLongFromDate(
//                    ecrJson.getString("createdDate")
//                ),
//                updatedBy = if (ecrJson.has("updatedBy")) ecrJson.getString("updatedBy") else ecrJson.getString(
//                    "createdBy"
//                ),
//                updatedDate = getLongFromDate(
//                    if (ecrJson.has("updatedDate")) ecrJson.getString(
//                        "updatedDate"
//                    ) else ecrJson.getString("createdDate")
//                ),
//                processed = "P",
//                syncState = SyncState.SYNCED
//            )
//            list.add(ecr)
//
//        }
//        return list
//    }
//
//    suspend fun getLatestEctByBenId(benId: Long): EligibleCoupleTrackingCache? {
//        return withContext(Dispatchers.IO){
//            database.ecrDao.getLatestEct(benId)
//        }
//    }
//    private suspend fun saveECRCacheFromResponse(dataObj: String): MutableList<EligibleCoupleRegCache> {
//        val tbScreeningList = mutableListOf<TBScreeningCache>()
//        var requestDTO = Gson().fromJson(dataObj, TBScreeningRequestDTO::class.java)
//        requestDTO?.tbScreeningList?.forEach { tbScreeningDTO ->
//            tbScreeningDTO.visitDate?.let {
//                var tbScreeningCache: TBScreeningCache? =
//                    tbDao.getTbScreening(tbScreeningDTO.benId,
//                        TBRepo.getLongFromDate(tbScreeningDTO.visitDate)
//                    )
//                if (tbScreeningCache == null) {
//                    tbDao.saveTbScreening(tbScreeningDTO.toCache())
//                }
//            }
//        }
//        return tbScreeningList
//    }
//
//    suspend fun getTbSuspectedDetailsFromServer(): Int {
//        return withContext(Dispatchers.IO) {
//            val user =
//                preferenceDao.getLoggedInUser()
//                    ?: throw IllegalStateException("No user logged in!!")
//            val lastTimeStamp = preferenceDao.getLastSyncedTimeStamp()
//            try {
//                val response = tmcNetworkApiService.getTBSuspectedData(
//                    GetBenRequest(
//                        user.userId,
//                        0,
//                        TBRepo.getCurrentDate(lastTimeStamp),
//                        TBRepo.getCurrentDate()
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
//                        Timber.d("Pull from amrit tb suspected data : $responseStatusCode")
//                        when (responseStatusCode) {
//                            200 -> {
//                                try {
//                                    val dataObj = jsonObj.getString("data")
//                                    saveTBSuspectedCacheFromResponse(dataObj)
//                                } catch (e: Exception) {
//                                    Timber.d("TB Suspected entries not synced $e")
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
//    private suspend fun saveTBSuspectedCacheFromResponse(dataObj: String): MutableList<TBSuspectedCache> {
//        val tbSuspectedList = mutableListOf<TBSuspectedCache>()
//        val requestDTO = Gson().fromJson(dataObj, TBSuspectedRequestDTO::class.java)
//        requestDTO?.tbSuspectedList?.forEach { tbSuspectedDTO ->
//            tbSuspectedDTO.visitDate?.let {
//                val tbSuspectedCache: TBSuspectedCache? =
//                    tbDao.getTbSuspected(
//                        tbSuspectedDTO.benId,
//                        TBRepo.getLongFromDate(tbSuspectedDTO.visitDate)
//                    )
//                if (tbSuspectedCache == null) {
//                    tbDao.saveTbSuspected(tbSuspectedDTO.toCache())
//                }
//            }
//        }
//        return tbSuspectedList
//    }

//    private fun getHighRiskAssess(dataObj: JSONArray): List<HRPNonPregnantAssessCache> {
////        TODO()
//        val list = mutableListOf<HRPNonPregnantAssessCache>()
//
//        for (i in 0 until dataObj.length()) {
//            val ecrJson = dataObj.getJSONObject(i)
//
//            try {
//                val hrnpa = HRPNonPregnantAssessCache(
//                    benId = ecrJson.getLong("benId"),
//                    misCarriage = if (ecrJson.has("misCarriage")) ecrJson.getString("misCarriage") else null,
//                    homeDelivery = if (ecrJson.has("homeDelivery")) ecrJson.getString("homeDelivery") else null,
//                    medicalIssues = if (ecrJson.has("medicalIssues")) ecrJson.getString("medicalIssues") else null,
//                    pastCSection = if (ecrJson.has("pastCSection")) ecrJson.getString("pastCSection") else null,
//                    isHighRisk = if (ecrJson.has("isHighRisk")) ecrJson.getBoolean("isHighRisk") else false,
//                    visitDate = getLongFromDate(
//                        ecrJson.getString("createdDate")
//                    ),
//                    syncState = SyncState.SYNCED
//                )
//                list.add(hrnpa)
//            } catch (e: Exception) {
//                Timber.e("Caught $e at ECR PULL")
//            }
//
//        }
//
//        return list
//    }
//
//    companion object {
//        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
//        private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
//        private fun getCurrentDate(millis: Long = System.currentTimeMillis()): String {
//            val dateString = dateFormat.format(millis)
//            val timeString = timeFormat.format(millis)
//            return "${dateString}T${timeString}.000Z"
//        }
//
//        private fun getLongFromDate(dateString: String): Long {
//            //Jul 22, 2023 8:17:23 AM"
//            val f = SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.ENGLISH)
//            val date = f.parse(dateString)
//            return date?.time ?: throw IllegalStateException("Invalid date for dateReg")
//        }
//    }
}