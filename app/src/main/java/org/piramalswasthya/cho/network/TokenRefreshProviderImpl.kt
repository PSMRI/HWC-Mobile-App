package org.piramalswasthya.cho.network

import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.piramalswasthya.cho.crypt.CryptoUtil
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.interceptors.TokenInsertTmcInterceptor
import retrofit2.HttpException
import timber.log.Timber
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [TokenRefreshProvider] that calls the JWT/login API with stored
 * credentials and updates the token. Used by [AuthRefreshInterceptor] when any API
 * returns 401/403 or statusCode 5002 (token expired).
 */
@Singleton
class TokenRefreshProviderImpl @Inject constructor(
    private val userDao: UserDao,
    private val amritApiService: AmritApiService,
    private val preferenceDao: PreferenceDao
) : TokenRefreshProvider {

    private val cryptoUtil = CryptoUtil()

    override fun refresh(): Boolean = runBlocking {
        try {
            val user = userDao.getLoggedInUser() ?: run {
                Timber.w("TokenRefreshProvider: No logged-in user")
                return@runBlocking false
            }
            refreshTokenTmc(user.userName, user.password)
        } catch (e: Exception) {
            Timber.e(e, "TokenRefreshProvider: refresh failed")
            false
        }
    }

    private suspend fun refreshTokenTmc(userName: String, password: String): Boolean {
        return try {
            val encryptedPassword = cryptoUtil.encrypt(password)
            val response = amritApiService.getJwtToken(
                TmcAuthUserRequest(userName, encryptedPassword)
            )
            if (!response.isSuccessful) {
                return false
            }
            val bodyString = response.body()?.string()
                ?: return false
            val responseBody = JSONObject(bodyString)
            val responseStatusCode = responseBody.getInt("statusCode")
            if (responseStatusCode == 200) {
                val data = responseBody.getJSONObject("data")
                TokenInsertTmcInterceptor.setJwt(data.getString("jwtToken"))
                preferenceDao.registerJWTAmritToken(data.getString("jwtToken"))
                val token = data.getString("key")
                TokenInsertTmcInterceptor.setToken(token)
                preferenceDao.registerPrimaryApiToken(token)
                Timber.d("TokenRefreshProvider: Token refreshed for user $userName")
                true
            } else {
                val errorMessage = responseBody.optString("errorMessage", "Unknown error")
                Timber.w("TokenRefreshProvider: API error $responseStatusCode - $errorMessage")
                false
            }
        } catch (e: SocketTimeoutException) {
            Timber.w("TokenRefreshProvider: Timeout, retrying once")
            refreshTokenTmc(userName, password)
        } catch (e: HttpException) {
            Timber.w("TokenRefreshProvider: Auth failed - ${e.code()}")
            false
        }
    }
}
