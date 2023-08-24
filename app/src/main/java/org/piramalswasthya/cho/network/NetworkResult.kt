package org.piramalswasthya.cho.network

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

suspend inline fun networkResultInterceptor(
    crossinline action: suspend () -> NetworkResult<NetworkResponse>,
): NetworkResult<NetworkResponse> {

    return withContext(Dispatchers.IO) {
        try {
            action()
        } catch (e: IOException) {
            NetworkResult.Error(-1, "Unable to connect to Internet!")
        } catch (e: JSONException) {
            NetworkResult.Error(-2, "Invalid response! Please try again!")
        } catch (e: SocketTimeoutException) {
            NetworkResult.Error(-3, "Request Timed out! Please try again!")
        } catch (e: java.lang.Exception) {
            NetworkResult.Error(-4, e.message ?: "Unknown Error")
        } catch (e: Exception) {
            NetworkResult.Error(-4, e.message ?: "exception occured is")
        }

    }

}

suspend inline fun refreshTokenInterceptor(
    responseBody: String?,
    crossinline onSuccess: suspend () -> NetworkResult<NetworkResponse>,
    crossinline onTokenExpired: suspend () -> NetworkResult<NetworkResponse>
) : NetworkResult<NetworkResponse>{
    return withContext(Dispatchers.IO) {
         when (responseBody?.let { JSONObject(it).getInt("statusCode") }) {
            200 -> {
                onSuccess()
            }
            5002 -> {
                onTokenExpired()
            }
            else -> {
                NetworkResult.Error(0, responseBody.toString())
            }
        }
    }
}

sealed class NetworkResult<out T : Any> {
    data class Success<out T : Any>(val data: T) : NetworkResult<T>()
    data class Error(val code: Int, val message: String) : NetworkResult<Nothing>()
    object NetworkError : NetworkResult<Nothing>()
}

open class NetworkResponse {

}