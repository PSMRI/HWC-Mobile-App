package org.piramalswasthya.cho.repositories

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.cho.crypt.CryptoUtil
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.LocationEntity
import org.piramalswasthya.cho.model.UserDomain
import org.piramalswasthya.cho.model.UserNetwork
import org.piramalswasthya.cho.model.fhir.SelectedOutreachProgram
import org.piramalswasthya.cho.network.AmritApiService
//import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.cho.ui.login_activity.cho_login.outreach.OutreachViewModel

import org.piramalswasthya.cho.network.TmcAuthUserRequest
import org.piramalswasthya.cho.network.TmcLocationDetailsRequest
import org.piramalswasthya.cho.network.TmcUserDetailsRequest
import org.piramalswasthya.cho.network.TmcUserVanSpDetailsRequest
import retrofit2.HttpException

import timber.log.Timber
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UserRepo @Inject constructor(
    private val userDao: UserDao,
    private val preferenceDao: PreferenceDao,
    private val tmcNetworkApiService: AmritApiService
) {


    private var user: UserNetwork? = null

    suspend fun getLoggedInUser(): UserDomain? {
        return withContext(Dispatchers.IO) {
            userDao.getLoggedInUser()?.asDomainModel()
        }
    }

    private suspend fun setOutreachProgram(selectedOption: String, timestamp: String) {
        var userId = userDao.getLoggedInUser()?.userId
        val selectedOutreachProgram = SelectedOutreachProgram(
            userId = userId,
            option = selectedOption, timestamp = timestamp
        )
        userDao.insertOutreachProgram(selectedOutreachProgram)
    }

    suspend fun authenticateUser(
        userName: String,
        password: String,
        selectedOption: String,
        timestamp: String
    ): OutreachViewModel.State {
        return withContext(Dispatchers.IO) {
            val loggedInUser = userDao.getUser(userName, password)
            Timber.d("user", loggedInUser.toString())
            loggedInUser?.let {
                if (it.userName.lowercase() == userName.lowercase() && it.password == password) {
                    val tokenB = preferenceDao.getPrimaryApiToken()

                    TokenInsertTmcInterceptor.setToken(
                        tokenB
                            ?: throw IllegalStateException("User logging offline without pref saved token B!")
                    )
                    it.userName = userName
                    it.loggedIn = true
                    userDao.update(loggedInUser)

                    Timber.w("User Logged in!")
                    setOutreachProgram(selectedOption, timestamp)
                    return@withContext OutreachViewModel.State.SUCCESS
                }
            }

            try {
                getTokenTmc(userName, password)
                if (user != null) {

                    Timber.d("User Auth Complete!!!!")
                    user?.loggedIn = true
                    if (userDao.getUser(userName, password)?.userName == userName) {
                        userDao.update(user!!.asCacheModel())
                    } else {
                        userDao.resetAllUsersLoggedInState()
                        userDao.insert(user!!.asCacheModel())
                    }
                    setOutreachProgram(selectedOption, timestamp)
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
            } catch (ce: ConnectException) {
                return@withContext OutreachViewModel.State.ERROR_NETWORK
            }
        }
    }


    private suspend fun getUserVanSpDetails(): Boolean {
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
                    user?.parkingPlaceId = vanSp.getInt("parkingPlaceID")

                }
                true
//                getLocationDetails()
            } else {
                false
            }
        }
    }


    private suspend fun getTokenTmc(userName: String, password: String) {
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

                    user = UserNetwork(userId, userName, password)
                    val serviceId = privilegesObject.getInt("serviceID")
                    user?.serviceId = serviceId
                    val serviceMapId =
                        privilegesObject.getInt("providerServiceMapID")
                    user?.serviceMapId = serviceMapId
                    TokenInsertTmcInterceptor.setToken(token)
                    preferenceDao.registerPrimaryApiToken(token)
                    getUserVanSpDetails()
                } else {
                    val errorMessage = responseBody.getString("errorMessage")
                    Timber.d("Error Message $errorMessage")
                }
            } catch (e: retrofit2.HttpException) {
                Timber.d("Auth Failed!")
            }

        }

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
                    Log.d("key", token)
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

//
//    suspend fun logout() {
//        withContext(Dispatchers.IO) {
//            val loggedInUser = userDao.getLoggedInUser()!!
//            userDao.logout(loggedInUser)
//        }
//    }


}