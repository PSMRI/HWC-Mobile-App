package org.piramalswasthya.cho.repositories

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.cho.crypt.CryptoUtil
import org.piramalswasthya.cho.database.room.dao.BlockMasterDao
import org.piramalswasthya.cho.database.room.dao.DistrictMasterDao
import org.piramalswasthya.cho.database.room.dao.StateMasterDao
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.room.dao.VillageMasterDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.BlockMaster
import org.piramalswasthya.cho.model.DistrictMaster
import org.piramalswasthya.cho.model.FingerPrint
import org.piramalswasthya.cho.model.LocationData
import org.piramalswasthya.cho.model.LocationRequest
import org.piramalswasthya.cho.model.MasterLocationModel
import org.piramalswasthya.cho.model.StateMaster
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.model.UserDomain
import org.piramalswasthya.cho.model.UserMasterVillage
import org.piramalswasthya.cho.model.UserNetwork
import org.piramalswasthya.cho.model.VillageLocationData
import org.piramalswasthya.cho.model.VillageMaster
import org.piramalswasthya.cho.model.fhir.SelectedOutreachProgram
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.NetworkResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.cho.ui.login_activity.cho_login.outreach.OutreachViewModel
import org.piramalswasthya.cho.network.TmcAuthUserRequest
import org.piramalswasthya.cho.network.TmcUserVanSpDetailsRequest
import org.piramalswasthya.cho.network.networkResultInterceptor
import org.piramalswasthya.cho.network.refreshTokenInterceptor
import org.piramalswasthya.cho.network.socketTimeoutException
import retrofit2.HttpException
import timber.log.Timber
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.Date
import javax.inject.Inject

class UserRepo @Inject constructor(
    private val userDao: UserDao,
    private val preferenceDao: PreferenceDao,
    private val stateMasterDao: StateMasterDao,
    private val districtMasterDao: DistrictMasterDao,
    private val blockMasterDao: BlockMasterDao,
    private val villageMasterDao: VillageMasterDao,
    private val tmcNetworkApiService: AmritApiService
) {


    private var user: UserNetwork? = null

//    @SuppressLint("StaticFieldLeak")
//    val context: Context = application.applicationContext


    suspend fun getLoggedInUser(): UserDomain? {
        return withContext(Dispatchers.IO) {
            userDao.getLoggedInUser()?.asDomainModel()
        }
    }
     fun getLoggedInUserAsFlow(): Flow<Int?> {
        return userDao.getLoggedInUserAsFlow().map { it?.asDomainModel()?.userId }
    }
    suspend fun isUserLoggedIn(): Int {
        return withContext(Dispatchers.IO) {
            userDao.getLoggedInStatus()
        }
    }

     suspend fun setOutreachProgram(loginType: String?,
                                      selectedOption: String?,
                                      loginTimeStamp: String?,
                                      logoutTimeStamp: String?,
                                      lat: Double?,
                                      long: Double?,
                                      logoutType: String?,
                                    userImage: String?,
                                    isOutOfReach:Boolean?
     ) {
         var user = userDao.getLoggedInUser()
         var userName = user?.userName
         var userId = user?.userId
        val selectedOutreachProgram = SelectedOutreachProgram(
            userId = userId,
            userName = userName,
            loginType = loginType,
            option = selectedOption,
            logoutTimeStamp = logoutTimeStamp,
            loginTimeStamp = loginTimeStamp,
            latitude = lat,
            longitude = long,
            logoutType = logoutType,
            userImage = userImage,
            isOutOfReach = isOutOfReach
        )
        userDao.insertOutreachProgram(selectedOutreachProgram)
    }

    suspend fun authenticateUser(
        userName: String,
        password: String,
        loginType: String?,
        selectedOption: String?,
        loginTimeStamp: String?,
        logoutTimeStamp: String?,
        lat: Double?,
        long: Double?,
        userImage: String?,
        logoutType: String?,
        isBiometric: Boolean? = false,
        context: Context
    ): OutreachViewModel.State {
        return withContext(Dispatchers.IO) {
            //reset all login before another login
            userDao.resetAllUsersLoggedInState()
            val loggedInUser = userDao.getUser(userName, password)
            Timber.d("user", loggedInUser.toString())
            loggedInUser?.let {
                if (it.userName.lowercase() == userName.lowercase() && it.password == password) {
                    preferenceDao.setUserRoles(loggedInUser.roles);
                    preferenceDao.setUserLoginType(loginType);
                    val tokenB = preferenceDao.getPrimaryApiToken()
                    TokenInsertTmcInterceptor.setToken(
                        tokenB
                            ?: throw IllegalStateException("User logging offline without pref saved token B!")
                    )
                    it.userName = userName
                    it.loggedIn = true
                    it.lastLogInTime = Date()
                    userDao.update(loggedInUser)
//                    if(!isBiometric!!) {
//                        setOutreachProgram(
//                            loginType,
//                            selectedOption,
//                            loginTimeStamp,
//                            logoutTimeStamp,
//                            lat,
//                            long,
//                            logoutType,
//                            userImage
//                        )
//                    }
                    return@withContext OutreachViewModel.State.SUCCESS
                }
            }
            if(!isInternetAvailable(context)){
                GlobalScope.launch(Dispatchers.Main) {
                    Toast.makeText(context, "Network Error!", Toast.LENGTH_SHORT).show()
                }
                return@withContext OutreachViewModel.State.ERROR_NETWORK
            }
            try {
                getTokenTmc(userName, password,context)
                if (user != null) {
                    Timber.d("User Auth Complete!!!!")
                    user?.loggedIn = true
                    user?.lastLogInTime = Date()
                    preferenceDao.setUserRoles(user!!.roles);
                    if (userDao.getUser(userName, password)?.userName == userName) {
                        userDao.update(user!!.asCacheModel())
                    } else {
                        userDao.resetAllUsersLoggedInState()
                        userDao.insert(user!!.asCacheModel())
                    }
//                    if(!isBiometric!!) {
//                        setOutreachProgram(
//                            loginType,
//                            selectedOption,
//                            loginTimeStamp,
//                            logoutTimeStamp,
//                            lat,
//                            long,
//                            logoutType,
//                            userImage
//                        )
//                    }
                    return@withContext OutreachViewModel.State.SUCCESS
//                        }
                }
                return@withContext OutreachViewModel.State.ERROR_SERVER
//                }
//                return@withContext OutreachViewModel.State.ERROR_INPUT
            } catch (se: SocketTimeoutException) {
                return@withContext OutreachViewModel.State.ERROR_SERVER
            } catch (ce: ConnectException) {
                return@withContext OutreachViewModel.State.ERROR_NETWORK
            } catch (ue: UnknownHostException) {
                return@withContext OutreachViewModel.State.ERROR_NETWORK
            }
        }
    }
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            return networkCapabilities != null &&
                    (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    private suspend fun getUserVanSpDetails(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            val response = tmcNetworkApiService.getUserVanSpDetails(
                TmcUserVanSpDetailsRequest(
                    user!!.userId,
                    user!!.serviceMapId
                )
            )
            Timber.d("User Van Sp Details : $response")
            val statusCode = response.code()
            if (statusCode == 200) {
                val responseString = response.body()?.string() ?: return@withContext false
                val responseJson = JSONObject(responseString)
                val data = responseJson.getJSONObject("data")
                val vanSpDetailsArray = data.getJSONArray("UserVanSpDetails")

                for (i in 0 until vanSpDetailsArray.length()) {
                    val vanSp = vanSpDetailsArray.getJSONObject(i)
                    val vanId = vanSp.getInt("vanID")
                    user?.vanId = vanId
                    //val name = vanSp.getString("vanNoAndType")
                    val servicePointId = vanSp.getInt("servicePointID")
                    user?.servicePointId = servicePointId
                    val servicePointName = vanSp.getString("servicePointName")
                    user?.servicePointName = servicePointName
                    if (!vanSp.has("facilityID")) {
                        Toast.makeText(context, "Facility ID not found", Toast.LENGTH_LONG).show()
                        delay(3000)
                    }
                    val facilityId = vanSp.getInt("facilityID")
                    user?.facilityID = facilityId
                    user?.parkingPlaceId = vanSp.getInt("parkingPlaceID")

                }
                true
            } else {
                false
            }
        }
    }

    private suspend fun getUserMasterVillage(): Boolean{
        return withContext(Dispatchers.IO) {
            val response = tmcNetworkApiService.getUserMasterVillage(
                user!!.userId
            )
            val statusCode = response.code()
            if (statusCode == 200) {
                val responseString = response.body()?.string() ?: return@withContext false
                val responseJson = JSONObject(responseString)
                val responseStatusCode = responseJson.getInt("statusCode")
                if(responseStatusCode == 200) {
                    val data = responseJson.getJSONObject("data")
                    val masterVillageName = data.getString("villageName")
                    user?.masterVillageName = masterVillageName
                    val masterVillageId = data.getInt("districtBranchID")
                    user?.masterVillageID = masterVillageId
                    val blockId = data.getInt("blockID")
                    user?.masterBlockID = blockId
                    val masterLatitude = data.getDouble("latitude")
                    user?.masterLatitude = masterLatitude
                    val masterLongitude = data.getDouble("longitude")
                    user?.masterLongitude = masterLongitude
                    val masterLocAddress = data.getString("address")
                    user?.masterLocationAddress = masterLocAddress
                    val loginDistance = data.getInt("loginDistance")
                    user?.loginDistance = loginDistance
                    true
                }else
                    false
            }else {
                false
            }
        }

        }


    private suspend fun getTokenTmc(userName: String, password: String, context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val encryptedPassword = encrypt(password)

                val response =
                    tmcNetworkApiService.getJwtToken(
                        TmcAuthUserRequest(
                            userName,
                            encryptedPassword
                        )
                    )
                Timber.d("msg", response.toString())
                if (!response.isSuccessful) {
                    return@withContext
                }

                val responseBody = JSONObject(
                    response.body()?.string()
                        ?: throw IllegalStateException("Response success but data missing @ $response")
                )
                val responseStatusCode = responseBody.getInt("statusCode")
                if (responseStatusCode == 200) {
                    val data = responseBody.getJSONObject("data")
                    val token = data.getString("key")
                    val userId = data.getInt("userID")
                    Timber.d("Token", token.toString())
                    val privilegesArray = data.getJSONArray("previlegeObj")
                    val privilegesObject = privilegesArray.getJSONObject(0)
                    val rolesArray = extractRoles(privilegesObject);
//                    val roles = rolesArray;
//                    Log.i("roles are ", roles);
                    val name = data.getString("fullName")
                    user = UserNetwork(userId, userName, password, name, rolesArray)
                    val serviceId = privilegesObject.getInt("serviceID")
                    user?.serviceId = serviceId
                    val serviceMapId =
                        privilegesObject.getInt("providerServiceMapID")
                    user?.serviceMapId = serviceMapId
                    TokenInsertTmcInterceptor.setToken(token)
                    preferenceDao.registerPrimaryApiToken(token)
                    getUserVanSpDetails(context)
                    getLocDetailsBasedOnSpIDAndPsmID()
                    getUserMasterVillage()
//                    getUserAssignedVillageIds()
                } else {
                    val errorMessage = responseBody.getString("errorMessage")
                    GlobalScope.launch(Dispatchers.Main) {
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    }
                    Timber.d("Error Message $errorMessage")
                }
            } catch (e: retrofit2.HttpException) {
                Timber.d("Auth Failed!")
            }

        }

    }
    private suspend fun getLocDetailsBasedOnSpIDAndPsmID() {
        return withContext(Dispatchers.IO) {
            val response = tmcNetworkApiService.getLocDetailsBasedOnSpIDAndPsmID(
                LocationRequest(
                    user!!.vanId,
                    user!!.serviceMapId.toString(),
                    user!!.userId
                )
            )
            if (!response.isSuccessful) {
                return@withContext
            }

            val responseBody = JSONObject(
                response.body()?.string()
                    ?: throw IllegalStateException("Response success but data missing @ $response")
            )

//            user!!.assignVillageIds = "24286,24326,24250,24334,24351,24294"

            val responseStatusCode = responseBody.getInt("statusCode")
            if (responseStatusCode == 200) {
                val data = responseBody.getJSONObject("data")
                val otherLoc = data.getJSONObject("otherLoc")
                val stateId = otherLoc.getString("stateID")
                val districtList = otherLoc.getJSONArray("districtList")
                val districtObject = districtList.getJSONObject(0)
                val districtId = districtObject.getString("districtID")
                val districtName = districtObject.getString("districtName")
                val blockId = districtObject.getString("blockId")
                val blockName = districtObject.getString("blockName")
                val villageList = districtObject.getJSONArray("villageList")

                val itemType = object : TypeToken<List<VillageLocationData>>() {}.type
                var villageLocationDataList : List<VillageLocationData> = Gson().fromJson(villageList.toString(), itemType)
                villageLocationDataList = villageLocationDataList.toSet().toList()

                val stateMaster = data.getJSONArray("stateMaster")
                var stateMasterName : String = ""
                var govtLGDStateID : Int? = null
                for (i in 0 until stateMaster.length()) {
                    val jsonObject = stateMaster.getJSONObject(i)
                    val id = jsonObject.getInt("stateID").toString()
                    val stateName = jsonObject.getString("stateName")
                    val lgdStateId = jsonObject.getString("govtLGDStateID")
                    if (id == stateId) {
                         stateMasterName = stateName
                        govtLGDStateID = lgdStateId.toInt()
                    }
                }
                if(stateMasterDao.getStateById(stateId.toInt()) == null ){
                    stateMasterDao.insertStates(StateMaster(stateId.toInt(), stateMasterName, govtLGDStateID))
                }
                if(districtMasterDao.getDistrictById(districtId.toInt()) == null){
                    districtMasterDao.insertDistrict(DistrictMaster(districtId.toInt(),stateId.toInt(),govtLGDStateID,null, districtName))
                }
                if(blockMasterDao.getBlockById(blockId.toInt()) == null){
                    blockMasterDao.insertBlock(BlockMaster(blockId.toInt(),districtId.toInt(),null,null, blockName))
                }
                var villageIds = ""
                for(element in villageLocationDataList) {
                    var id = element.districtBranchID
                    villageIds += "$id,"
                    var name = element.villageName
                    if (villageMasterDao.getVillageById(id.toInt()) == null) {
                        villageMasterDao.insertVillage(
                            VillageMaster(
                                id.toInt(),
                                blockId.toInt(),
                                null,
                                null,
                                name?:""
                            )
                        )
                    }
                }

                user!!.stateId = stateId.toInt()
                user!!.districtID = districtId.toInt()
                user!!.blockID = blockId.toInt()

                if(villageIds.isNotEmpty()){
                    user!!.assignVillageIds = villageIds.substring(0, villageIds.length-1)
                }

                preferenceDao.saveUserLocationData(LocationData(
                    stateId.toInt(), stateMasterName, districtId.toInt(),districtName, blockId.toInt(),blockName, villageLocationDataList))
            }
        }
    }

//    private suspend fun getUserAssignedVillageIds(){
//        user!!.assignVillageIds = "54151,54676,463267"
////        val response = tmcNetworkApiService.getUserDetail(user!!.userId)
////        val responseBody = JSONObject(response.body()?.string() ?: "")
////        if(responseBody.has("data")){
////            val data = responseBody.getJSONObject("data")
////            user!!.assignVillageIds = data.getString("villageId")
////            user!!.assignVillageNames = data.getString("villageName")
////        }
//    }

    fun extractRoles(privilegesObject : JSONObject) : String{
//        return "Lab Technician,MO,Pharmacist,Registrar,Staff Nurse"
//        return "Lab Technician,MO,Pharmacist"

        val rolesObjectArray = privilegesObject.getJSONArray("roles")
        var roles = ""
        for (i in 0 until rolesObjectArray.length()) {
            val roleObject = rolesObjectArray.getJSONObject(i)
            roles += roleObject.getString("RoleName") + ","
        }
        return roles.substring(0, roles.length - 1)
    }

     fun encrypt(password: String): String {
        val util = CryptoUtil()
        return util.encrypt(password)
    }

    suspend fun refreshTokenTmc(userName: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val encryptedPassword = encrypt(password)
                val response =
                    tmcNetworkApiService.getJwtToken(TmcAuthUserRequest(userName, encryptedPassword))
                Timber.d("JWT : $response")
                if (!response.isSuccessful) {
                    return@withContext false
                }
                val responseBody = JSONObject(
                    response.body()?.string()
                        ?: throw IllegalStateException("Response success but data missing @ $response")
                )
                val responseStatusCode = responseBody.getInt("statusCode")
                if (responseStatusCode == 200) {
                    val data = responseBody.getJSONObject("data")
                    val token = data.getString("key")
                    TokenInsertTmcInterceptor.setToken(token)
                    preferenceDao.registerPrimaryApiToken(token)
                    return@withContext true
                } else {
                    val errorMessage = responseBody.getString("errorMessage")
                    Timber.d("Error Message $errorMessage")
                }
                return@withContext false
            } catch (se: SocketTimeoutException) {
                return@withContext refreshTokenTmc(userName, password)
            } catch (e: HttpException) {
                Timber.d("Auth Failed!")
                return@withContext false
            }


        }

    }
    suspend fun getUserCacheDetails(): UserCache?{
        return withContext(Dispatchers.IO){
            try {
                return@withContext userDao.getLoggedInUser()
            } catch (e: Exception) {
                Timber.d("Error in finding loggedIn user $e")
                return@withContext null
            }
        }
    }

    suspend fun insertFPDataToLocalDB(fpList: List<FingerPrint>){
        return withContext(Dispatchers.IO){
            try{
                for(item in fpList){
                    userDao.insertFpData(item)
                }
            } catch (e: Exception){
                Timber.d("Error in inserting Finger Print Data $e")
            }
        }
    }

    fun getFPDataFromLocalDB(): LiveData<List<FingerPrint>>{
        return  userDao.getAllFpData()
    }

    suspend fun updateLoginStatus(userName: String){
        try {
            userDao.resetAllUsersLoggedInState()
            userDao.updateLoggedInStatus(userName)
        } catch (e: Exception){
            Timber.d("Error in updating login status $e")
        }
    }
    private suspend fun saveLoginAuditDataToServer(auditEntryList : List<SelectedOutreachProgram>): NetworkResult<NetworkResponse> {

        return networkResultInterceptor {
            Timber.d("auditEntry is ", auditEntryList.toString())
            val response = tmcNetworkApiService.saveUpsyncDetails(auditEntryList)
            val responseBody = response.body()?.string()
            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    Timber.i("audit data synced",  response.body()?.string() ?: "")
                    NetworkResult.Success(NetworkResponse())
                },
                onTokenExpired = {
                    val user = getLoggedInUser()!!
                    refreshTokenTmc(user.userName, user.password)
                    saveLoginAuditDataToServer(auditEntryList)
                },
            )
        }
    }

    suspend fun updateVillageCoordinates(masterLocationModel: MasterLocationModel){
        tmcNetworkApiService.updateMasterVillageCoordinates(masterLocationModel)
    }
     suspend fun setUserMasterVillage(user:UserCache, userMasterVillage: UserMasterVillage) {
         val response = tmcNetworkApiService.setUserMasterVillage(userMasterVillage)
         val statusCode = response.code()
         if (statusCode == 200) {
             val responseString = response.body()?.string()
             val responseJson = JSONObject(responseString!!)
             val responseStatusCode = responseJson.getInt("statusCode")
             if (responseStatusCode == 200) {
                 val data = responseJson.getJSONObject("data")
                 val masterVillageName = data.getString("villageName")
                 user?.masterVillageName = masterVillageName
                 val masterVillageId = data.getInt("districtBranchID")
                 user?.masterVillageID = masterVillageId
                 val masterLocAddress = data.getString("address")
                 user?.masterLocationAddress = masterLocAddress
                 val loginDistance = data.getInt("loginDistance")
                 user?.loginDistance = loginDistance
                 val blockId = data.getInt("blockID")
                 user?.masterBlockID = blockId
                 val masterLatitude = data.getDouble("latitude")
                 user?.masterLatitude = masterLatitude
                 val masterLongitude = data.getDouble("longitude")
                 user?.masterLongitude = masterLongitude


                 userDao.update(user)

             }
         }
     }
    suspend fun processUnsyncedAuditData(): Boolean{
        val loginAuditDataListUnsynced: List<SelectedOutreachProgram> = userDao.getLoginAuditDataListUnsynced()
        if(loginAuditDataListUnsynced.isNotEmpty()){
            when(val response = saveLoginAuditDataToServer(loginAuditDataListUnsynced)){
                is NetworkResult.Success ->{
                    //TODO UPDATE SYNCED FLAG
                    loginAuditDataListUnsynced.forEach { it ->
                        userDao.updateAuditDataFlag(it.id)
                    }
                }
                is NetworkResult.Error -> {
                    if(response.code == socketTimeoutException){
                    throw SocketTimeoutException("caught exception")
                }
                    return false
                }
                else ->{}
            }
        }
        return true
    }

}