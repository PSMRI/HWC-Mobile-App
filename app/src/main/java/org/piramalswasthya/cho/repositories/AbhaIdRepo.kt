package org.piramalswasthya.cho.repositories

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.*
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.inject.Inject

class AbhaIdRepo @Inject constructor(
    private val abhaApiService: AbhaApiService,
    private val amritApiService: AmritApiService,
    private val userRepo: UserRepo,
    private val prefDao: PreferenceDao
) {
    suspend fun getAccessToken(): NetworkResult<AbhaTokenResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = abhaApiService.getToken()
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    val result = Gson().fromJson(responseBody, AbhaTokenResponse::class.java)
                    NetworkResult.Success(result)
                } else {
                    sendErrorResponse(response)
                }
            } catch (e: IOException) {
                NetworkResult.Error(-1, "Unable to connect to Internet!")
            } catch (e: JSONException) {
                NetworkResult.Error(-2, "Invalid response! Please try again!")
            } catch (e: SocketTimeoutException) {
                NetworkResult.Error(-3, "Request Timed out! Please try again!")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                NetworkResult.Error(-4, e.message ?: "Unknown Error")
            }
        }
    }

    suspend fun getAuthCert(): NetworkResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = abhaApiService.getAuthCert()
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    var key = responseBody!!
                    key = key.replace("-----BEGIN PUBLIC KEY-----\n", "")
                    key = key.replace("-----END PUBLIC KEY-----", "")
                    key = key.trim()
                    NetworkResult.Success(key)
                } else {
                    sendErrorResponse(response)
                }
            } catch (e: IOException) {
                NetworkResult.Error(-1, "Unable to connect to Internet!")
            } catch (e: JSONException) {
                NetworkResult.Error(-2, "Invalid response! Please try again!")
            } catch (e: SocketTimeoutException) {
                NetworkResult.Error(-3, "Request Timed out! Please try again!")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                NetworkResult.Error(-4, e.message ?: "Unknown Error")
            }
        }
    }


    suspend fun getStateAndDistricts(): NetworkResult<List<StateCodeResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = abhaApiService.getStateAndDistricts()
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    val typeToken = object : TypeToken<List<StateCodeResponse>>() {}.type
                    val stateCodes = Gson().fromJson<List<StateCodeResponse>>(responseBody, typeToken)
                    NetworkResult.Success(stateCodes)
                } else {
                    sendErrorResponse(response)
                }
            } catch (e: IOException) {
                NetworkResult.Error(-1, "Unable to connect to Internet!")
            } catch (e: JSONException) {
                NetworkResult.Error(-2, "Invalid response! Please try again!")
            } catch (e: SocketTimeoutException) {
                NetworkResult.Error(-3, "Request Timed out! Please try again!")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                NetworkResult.Error(-4, e.message ?: "Unknown Error")
            }
        }
    }

    suspend fun generateOtpForAadhaarV2(req: AbhaGenerateAadhaarOtpRequest): NetworkResult<AbhaGenerateAadhaarOtpResponseV2> {
        return withContext(Dispatchers.IO) {
            try {
                req.aadhaar = encryptData(req.aadhaar)
                val response = abhaApiService.generateAadhaarOtpV2(req)
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    val result =
                        Gson().fromJson(responseBody, AbhaGenerateAadhaarOtpResponseV2::class.java)
                    NetworkResult.Success(result)
                } else {
                    sendErrorResponse(response)
                }
            } catch (e: IOException) {
                NetworkResult.Error(-1, "Unable to connect to Internet!")
            } catch (e: JSONException) {
                NetworkResult.Error(-2, "Invalid response! Please try again!")
            } catch (e: SocketTimeoutException) {
                NetworkResult.Error(-3, "Request Timed out! Please try again!")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                NetworkResult.Error(-4, e.message ?: "Unknown Error")
            }
        }
    }

    private fun encryptData(txt: String): String {
        var encryptedTextBase64 = ""
        val publicKeyString = prefDao.getPublicKeyForAbha()!!
        Timber.d(publicKeyString)
        try {
            val publicKeyBytes = Base64.decode(publicKeyString, Base64.DEFAULT)
            val keyFactory = KeyFactory.getInstance("RSA")
            val publicKeySpec = X509EncodedKeySpec(publicKeyBytes)
            val publicKey = keyFactory.generatePublic(publicKeySpec)

            val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            val encryptedText = cipher.doFinal(txt.toByteArray())
            encryptedTextBase64 =
                Base64.encodeToString(encryptedText, Base64.DEFAULT).replace("\n", "")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return encryptedTextBase64
    }

    suspend fun resendOtpForAadhaar(req: AbhaResendAadhaarOtpRequest): NetworkResult<AbhaGenerateAadhaarOtpResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = abhaApiService.resendAadhaarOtp(req)
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    val result =
                        Gson().fromJson(responseBody, AbhaGenerateAadhaarOtpResponse::class.java)
                    NetworkResult.Success(result)
                } else {
                    sendErrorResponse(response)
                }
            } catch (e: IOException) {
                NetworkResult.Error(-1, "Unable to connect to Internet!")
            } catch (e: JSONException) {
                NetworkResult.Error(-2, "Invalid response! Please try again!")
            } catch (e: SocketTimeoutException) {
                NetworkResult.Error(-3, "Request Timed out! Please try again!")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                NetworkResult.Error(-4, e.message ?: "Unknown Error")
            }
        }
    }

    suspend fun verifyOtpForAadhaar(req: AbhaVerifyAadhaarOtpRequest): NetworkResult<AbhaVerifyAadhaarOtpResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = abhaApiService.verifyAadhaarOtp(req)
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    val result =
                        Gson().fromJson(responseBody, AbhaVerifyAadhaarOtpResponse::class.java)
                    NetworkResult.Success(result)
                } else {
                    sendErrorResponse(response)
                }
            } catch (e: IOException) {
                NetworkResult.Error(-1, "Unable to connect to Internet!")
            } catch (e: JSONException) {
                NetworkResult.Error(-2, "Invalid response! Please try again!")
            } catch (e: SocketTimeoutException) {
                NetworkResult.Error(-3, "Request Timed out! Please try again!")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                NetworkResult.Error(-4, e.message ?: "Unknown Error")
            }
        }

    }

    suspend fun checkAndGenerateOtpForMobileNumber(req: AbhaGenerateMobileOtpRequest): NetworkResult<AbhaCheckAndGenerateMobileOtpResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = abhaApiService.checkAndGenerateMobileOtp(request = req)
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    val result =
                        Gson().fromJson(
                            responseBody,
                            AbhaCheckAndGenerateMobileOtpResponse::class.java
                        )
                    NetworkResult.Success(result)
                } else {
                    sendErrorResponse(response)
                }
            } catch (e: IOException) {
                NetworkResult.Error(-1, "Unable to connect to Internet!")
            } catch (e: JSONException) {
                NetworkResult.Error(-2, "Invalid response! Please try again!")
            } catch (e: SocketTimeoutException) {
                NetworkResult.Error(-3, "Request Timed out! Please try again!")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                NetworkResult.Error(-4, e.message ?: "Unknown Error")
            }
        }
    }

    suspend fun verifyOtpForMobileNumber(req: AbhaVerifyMobileOtpRequest): NetworkResult<AbhaVerifyMobileOtpResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = abhaApiService.verifyMobileOtp(req)
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    val result =
                        Gson().fromJson(responseBody, AbhaVerifyMobileOtpResponse::class.java)
                    NetworkResult.Success(result)
                } else {
                    sendErrorResponse(response)
                }
            } catch (e: IOException) {
                NetworkResult.Error(-1, "Unable to connect to Internet!")
            } catch (e: JSONException) {
                NetworkResult.Error(-2, "Invalid response! Please try again!")
            } catch (e: SocketTimeoutException) {
                NetworkResult.Error(-3, "Request Timed out! Please try again!")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                NetworkResult.Error(-4, e.message ?: "Unknown Error")
            }
        }
    }

    suspend fun generateAbhaId(req: CreateAbhaIdRequest): NetworkResult<CreateAbhaIdResponse> {
        return withContext((Dispatchers.IO)) {
            try {
                val response = abhaApiService.createAbhaId(req)
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    val result = Gson().fromJson(responseBody, CreateAbhaIdResponse::class.java)
                    NetworkResult.Success(result)
                } else {
                    sendErrorResponse(response)
                }
            } catch (e: IOException) {
                NetworkResult.Error(-1, "Unable to connect to Internet!")
            } catch (e: JSONException) {
                NetworkResult.Error(-2, "Invalid response! Please try again!")
            } catch (e: SocketTimeoutException) {
                NetworkResult.Error(-3, "Request Timed out! Please try again!")
            } catch (e: java.lang.Exception) {
                NetworkResult.Error(-4, e.message ?: "Unknown Error")
            }
        }
    }

    suspend fun downloadPdfCard(): Response<ResponseBody> {
        return abhaApiService.getPdfCard()
    }

    private fun sendErrorResponse(response: Response<ResponseBody>): NetworkResult.Error {
        return when (response.code()) {
            503 -> NetworkResult.Error(503, "Service Unavailable! Please try later!")
            else -> {
                val errorBody = response.errorBody()?.string()
                val errorJson = JSONObject(errorBody.toString())
                val detailsArray = errorJson.getJSONArray("details")
                val detailsObject = detailsArray.getJSONObject(0)
                val errorMessage = detailsObject.getString("message")
                NetworkResult.Error(response.code(), errorMessage)
            }
        }
    }

    suspend fun generateAbhaIdGov(request: CreateAbhaIdGovRequest): NetworkResult<CreateAbhaIdResponse> {
        return withContext((Dispatchers.IO)) {
            try {
                val response = abhaApiService.createAbhaIdGov(request)
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    val result = Gson().fromJson(responseBody, CreateAbhaIdResponse::class.java)
                    NetworkResult.Success(result)
                } else {
                    sendErrorResponse(response)
                }
            } catch (e: IOException) {
                NetworkResult.Error(-1, "Unable to connect to Internet!")
            } catch (e: JSONException) {
                NetworkResult.Error(-2, "Invalid response! Please try again!")
            } catch (e: SocketTimeoutException) {
                NetworkResult.Error(-3, "Request Timed out! Please try again!")
            } catch (e: java.lang.Exception) {
                NetworkResult.Error(-4, e.message ?: "Unknown Error")
            }
        }
    }

    suspend fun verifyBio(aadhaarVerifyBioRequest: AadhaarVerifyBioRequest): NetworkResult<CreateAbhaIdResponse> {
        return withContext((Dispatchers.IO)) {
            try {
                val response = abhaApiService.verifyBio(aadhaarVerifyBioRequest)
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    val result = Gson().fromJson(responseBody, CreateAbhaIdResponse::class.java)
                    NetworkResult.Success(result)
                } else {
                    sendErrorResponse(response)
                }
            } catch (e: IOException) {
                NetworkResult.Error(-1, "Unable to connect to Internet!")
            } catch (e: JSONException) {
                NetworkResult.Error(-2, "Invalid response! Please try again!")
            } catch (e: SocketTimeoutException) {
                NetworkResult.Error(-3, "Request Timed out! Please try again!")
            } catch (e: java.lang.Exception) {
                NetworkResult.Error(-4, e.message ?: "Unknown Error")
            }
        }
    }

    suspend fun createHealthIdWithUid(createHealthIdRequest: CreateHealthIdRequest): NetworkResult<CreateHIDResponse> {
        return withContext((Dispatchers.IO)) {
            try {
                val response = amritApiService.createHid(createHealthIdRequest)
                val responseBody = response.body()?.string()
                when (responseBody?.let { JSONObject(it).getInt("statusCode") }) {
                    200 -> {
                        val data = responseBody.let { JSONObject(it).getString("data") }
                        val result = Gson().fromJson(data, CreateHIDResponse::class.java)
                        NetworkResult.Success(result)
                    }
                    5000 -> {
                        if (JSONObject(responseBody).getString("errorMessage")
                                .contentEquals("Invalid login key or session is expired")) {
                            val user = userRepo.getLoggedInUser()!!
                            userRepo.refreshTokenTmc(user.userName, user.password)
                            createHealthIdWithUid(createHealthIdRequest)
                        } else {
                            NetworkResult.Error(0,JSONObject(responseBody).getString("errorMessage"))
                        }
                    }
                    else -> {
                        NetworkResult.Error(0, responseBody.toString())
                    }
                }
            } catch (e: IOException) {
                NetworkResult.Error(-1, "Unable to connect to Internet!")
            } catch (e: JSONException) {
                NetworkResult.Error(-2, "Invalid response! Please try again!")
            } catch (e: SocketTimeoutException) {
                NetworkResult.Error(-3, "Request Timed out! Please try again!")
            } catch (e: java.lang.Exception) {
                NetworkResult.Error(-4, e.message ?: "Unknown Error")
            }
        }
    }

    suspend fun mapHealthIDToBeneficiary(mapHIDtoBeneficiary: MapHIDtoBeneficiary): NetworkResult<String> {
        return withContext((Dispatchers.IO)) {
            try {
                val response = amritApiService.mapHealthIDToBeneficiary(mapHIDtoBeneficiary)
                val responseBody = response.body()?.string()
                when(responseBody?.let { JSONObject(it).getInt("statusCode") }) {
                    200 -> NetworkResult.Success(responseBody)
                    5000 -> {
                        if (JSONObject(responseBody).getString("errorMessage")
                                .contentEquals("Invalid login key or session is expired")){
                            val user = userRepo.getLoggedInUser()!!
                            userRepo.refreshTokenTmc(user.userName, user.password)
                            mapHealthIDToBeneficiary(mapHIDtoBeneficiary)
                        } else {
                            NetworkResult.Error(0, JSONObject(responseBody).getString("errorMessage"))
                        }
                    }
                    else -> NetworkResult.Error(0, responseBody.toString())
                }
            } catch (e: IOException) {
                NetworkResult.Error(-1, "Unable to connect to Internet!")
            } catch (e: JSONException) {
                NetworkResult.Error(-2, "Invalid response! Please try again!")
            } catch (e: SocketTimeoutException) {
                NetworkResult.Error(-3, "Request Timed out! Please try again!")
            } catch (e: java.lang.Exception) {
                NetworkResult.Error(-4, e.message ?: "Unknown Error")
            }
        }
    }

    suspend fun generateOtpHid(generateOtpHid: GenerateOtpHid): NetworkResult<String> {
        return withContext((Dispatchers.IO)) {
            try {
                val response = amritApiService.generateOtpHealthId(generateOtpHid)
                val responseBody = response.body()?.string()
                when(responseBody?.let { JSONObject(it).getInt("statusCode") }) {
                    200 -> NetworkResult.Success(JSONObject(responseBody).getJSONObject("data").getString("txnId"))
                    5000 -> {
                        val error = JSONObject(responseBody).getString("errorMessage")
                        if ( error.contentEquals("Invalid login key or session is expired")){
                            val user = userRepo.getLoggedInUser()!!
                            userRepo.refreshTokenTmc(user.userName, user.password)
                            generateOtpHid(generateOtpHid)
                        } else {
                            NetworkResult.Error(0,error)
                        }
                    }
                    else -> NetworkResult.Error(0, responseBody.toString())
                }
            } catch (e: IOException) {
                NetworkResult.Error(-1, "Unable to connect to Internet!")
            } catch (e: JSONException) {
                NetworkResult.Error(-2, "Invalid response! Please try again!")
            } catch (e: SocketTimeoutException) {
                NetworkResult.Error(-3, "Request Timed out! Please try again!")
            } catch (e: java.lang.Exception) {
                NetworkResult.Error(-4, e.message ?: "Unknown Error")
            }
        }
    }

    suspend fun verifyOtpAndGenerateHealthCard(validateOtpHid: ValidateOtpHid): NetworkResult<String> {
        return withContext((Dispatchers.IO)) {
            try {
                val response = amritApiService.verifyOtpAndGenerateHealthCard(validateOtpHid)
                val responseBody = response.body()?.string()
                when(responseBody?.let { JSONObject(it).getInt("statusCode") }) {
                    200 -> NetworkResult.Success(JSONObject(responseBody).getJSONObject("data").getString("data"))
                    5000 -> {
                        if (JSONObject(responseBody).getString("errorMessage")
                                .contentEquals("Invalid login key or session is expired")){
                            val user = userRepo.getLoggedInUser()!!
                            userRepo.refreshTokenTmc(user.userName, user.password)
                            verifyOtpAndGenerateHealthCard(validateOtpHid)
                        } else {
                            NetworkResult.Error(0, JSONObject(responseBody).getString("errorMessage"))
                        }
                    }
                    else -> NetworkResult.Error(0, responseBody.toString())

                }
            } catch (e: IOException) {
                NetworkResult.Error(-1, "Unable to connect to Internet!")
            } catch (e: JSONException) {
                NetworkResult.Error(-2, "Invalid response! Please try again!")
            } catch (e: SocketTimeoutException) {
                NetworkResult.Error(-3, "Request Timed out! Please try again!")
            } catch (e: java.lang.Exception) {
                NetworkResult.Error(-4, e.message ?: "Unknown Error")
            }
        }
    }
}