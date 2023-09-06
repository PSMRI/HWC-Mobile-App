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

const val ioException = -1
const val jsonException = -2
const val socketTimeoutException = -3
const val exception = -4


suspend inline fun networkResultInterceptor(
    crossinline action: suspend () -> NetworkResult<NetworkResponse>,
): NetworkResult<NetworkResponse> {

    return withContext(Dispatchers.IO) {
        try {
            action()
        } catch (e: IOException) {
            NetworkResult.Error(ioException, "Unable to connect to Internet!")
        } catch (e: JSONException) {
            NetworkResult.Error(jsonException, "Invalid response! Please try again!")
        } catch (e: SocketTimeoutException) {
            NetworkResult.Error(socketTimeoutException, "Request Timed out! Please try again!")
        } catch (e: java.lang.Exception) {
            NetworkResult.Error(exception, e.message ?: "Unknown Error")
        } catch (e: Exception) {
            NetworkResult.Error(exception, e.message ?: "exception occured is")
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