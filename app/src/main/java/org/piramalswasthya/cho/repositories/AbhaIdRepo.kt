package org.piramalswasthya.cho.repositories

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONArray
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
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
                val response = abhaApiService.getToken(
//                    id = if (BuildConfig.FLAVOR.contains("stag", true) ||
//                        BuildConfig.FLAVOR.contains("uat", true)
//                    ) {
//                        "sbx"
//                    } else {
//                        "abdm"
//                    },
                    requestId = generateUUID(),
                    timestamp = getCurrentTimestamp()
                )
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
                val response = abhaApiService.getAuthCert(
                    requestId = generateUUID(),
                    timestamp = getCurrentTimestamp()
                )
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    val result =
                        Gson().fromJson(responseBody, AbhaPublicCertificateResponse::class.java)
                    var key = result.publicKey
//                    key = key.replace("-----BEGIN PUBLIC KEY-----\n", "")
//                    key = key.replace("-----END PUBLIC KEY-----", "")
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

    suspend fun generateAadhaarOtpV3(req: AbhaGenerateAadhaarOtpRequest): NetworkResult<AbhaGenerateAadhaarOtpResponseV2> {
        return withContext(Dispatchers.IO) {
            try {
                // ABHA v1/v2 encryption technique
//                req.aadhaar = encryptData(req.aadhaar)
                // ABHA v3 encryption technique
                req.loginId = encryptData(req.loginId)
                // ABHA v1/v2 API
//                val response = abhaApiService.generateAadhaarOtpV2(req)
                // ABHA v3 API
                val response =
                    abhaApiService.generateAadhaarOtpV3(req, generateUUID(), getCurrentTimestamp())
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    val result =
                        Gson().fromJson(responseBody, AbhaGenerateAadhaarOtpResponseV2::class.java)
                    NetworkResult.Success(result)
                } else {

                    val errorString = response?.errorBody()?.string()
                    val (code, message) = parseAbhaErrorString(errorString) ?: Pair("", "")

                    if (message.contains("UIDAI Error code : 953")){
                        return@withContext NetworkResult.Error(-5, "You have requested multiple OTPs in this transaction. Please try again in 30 minutes.")
                    }
                    sendErrorResponse(response)
                }
            } catch (e: IOException) {
                NetworkResult.Error(-1, "Unable to connect to Internet!")
            } catch (e: JSONException) {
                if (e.message.toString().contains("UIDAI Error code : 953")){
                    return@withContext NetworkResult.Error(-5, "You have requested multiple OTPs in this transaction. Please try again in 30 minutes.")
                }
                NetworkResult.Error(-2, "Invalid response! Please try again!")
            } catch (e: SocketTimeoutException) {
                NetworkResult.Error(-3, "Request Timed out! Please try again!")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                NetworkResult.Error(-4, e.message ?: "Unknown Error")
            }
        }
    }

    suspend fun searchAbha(req: SearchAbhaRequest): NetworkResult<SearchAbhaResponse> {
        return withContext(Dispatchers.IO) {
            try {
                req.mobile = encryptData(req.mobile)
                val response = abhaApiService.searchAbha(req, generateUUID(), getCurrentTimestamp())
                if (response.isSuccessful) {
                    val intermediateResult = JSONArray(response.body()?.string())
                    val responseBody = intermediateResult.getJSONObject(0).toString()
                    val result =
                        Gson().fromJson(responseBody, SearchAbhaResponse::class.java)
                    NetworkResult.Success(result)
                } else {

                    val errorString = response?.errorBody()?.string()
                    val (code, message) = parseAbhaErrorString(errorString) ?: Pair("", "")

                    if (message.contains("User not found.") || code.contains("ABDM-1114")){
                        return@withContext NetworkResult.Error(-5, "User not found.")
                    }

                    sendErrorResponse(response)
                }
            } catch (e: IOException) {
                NetworkResult.Error(-1, "Unable to connect to Internet!")
            } catch (e: JSONException) {
                if (e.message.toString().contains("No value for details")){
                    return@withContext NetworkResult.Error(-5, "User not found.")
                }
                NetworkResult.Error(-2, "Invalid response! Please try again!")
            } catch (e: SocketTimeoutException) {
                NetworkResult.Error(-3, "Request Timed out! Please try again!")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                NetworkResult.Error(-4, e.message ?: "Unknown Error")
            }
        }
    }

    suspend fun generateAbhaOtp(req: LoginGenerateOtpRequest): NetworkResult<LoginGenerateOtpResponse> {
        return withContext(Dispatchers.IO) {
            try {
                req.loginId = encryptData(req.loginId)
                val response =
                    abhaApiService.loginGenerateOtp(req, generateUUID(), getCurrentTimestamp())
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    val result =
                        Gson().fromJson(responseBody, LoginGenerateOtpResponse::class.java)
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

    suspend fun verifyAbhaOtp(req: LoginVerifyOtpRequest): NetworkResult<LoginVerifyOtpResponse> {
        return withContext(Dispatchers.IO) {
            try {
                req.authData.otp.otpValue = encryptData(req.authData.otp.otpValue)
                val response =
                    abhaApiService.loginVerifyOtp(req, generateUUID(), getCurrentTimestamp())
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    val result =
                        Gson().fromJson(responseBody, LoginVerifyOtpResponse::class.java)
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


    fun parseAbhaErrorString(errorString: String?): Pair<String, String>? {
        return try {
            if (errorString.isNullOrEmpty()) return null
            val json = JSONObject(errorString)
            val errorObj = json.optJSONObject("error")
            val code = errorObj?.optString("code") ?: ""
            val message = errorObj?.optString("message") ?: ""
            Pair(code, message)
        } catch (e: Exception) {
            Timber.e("Error parsing error JSON: ${e.message}")
            null
        }
    }

    private fun generateUUID(): String {
        return UUID.randomUUID().toString()
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return sdf.format(Date())
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

            // Cipher used in ABHA v1/v2 APIs
//            val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            // Cipher used in ABHA v3 APIs
            val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding")
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
        var response: Response<ResponseBody>? = null
        return withContext(Dispatchers.IO) {
            try {
                // ABHA v3
                req.authData.otp.otpValue = encryptData(req.authData.otp.otpValue)
                req.authData.otp.timeStamp = getCurrentTimestamp()
                // ABHA v1/v2 API
//                val response = abhaApiService.verifyAadhaarOtp(req)
                // ABHA v3 API
                response = abhaApiService.verifyAadhaarOtp3(req, generateUUID(), getCurrentTimestamp())
                if (response?.isSuccessful == true) {
                    val responseBody = response?.body()?.string()
                    val result =
                        Gson().fromJson(responseBody, AbhaVerifyAadhaarOtpResponse::class.java)
                    NetworkResult.Success(result)
                } else {
                    val errorString = response?.errorBody()?.string()
                    val (code, message) = parseAbhaErrorString(errorString) ?: Pair("", "")

                    if (message.contains("The mobile number provided by you is already linked to 6 ABHA numbers. Please provide a different mobile number.")){
                        return@withContext NetworkResult.Error(-5, "The mobile number provided by you is already linked to 6 ABHA numbers.\nPlease provide a different mobile number.")
                    }

                    if (message.contains("UIDAI Error code : 400 : OTP validation failed")){
                        return@withContext NetworkResult.Error(-6, "Incorrect OTP, Please Enter Valid OTP")
                    }

                    sendErrorResponse(response)
                }
            } catch (e: IOException) {
                NetworkResult.Error(-1, "Unable to connect to Internet!")
            } catch (e: JSONException) {
                NetworkResult.Error(-2, "SMS Gateway is unavailable! Please try again!")
            } catch (e: SocketTimeoutException) {
                NetworkResult.Error(-3, "Request Timed out! Please try again!")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                NetworkResult.Error(-4, e.message ?: "Unknown Error")
            }
        }

    }

    suspend fun printAbhaCard(): NetworkResult<ResponseBody> {
        return withContext(Dispatchers.IO) {
            try {
                val response = abhaApiService.printAbhaCard(generateUUID(), getCurrentTimestamp())
                val responseBody = response.body()
                if (response.isSuccessful) {
                    NetworkResult.Success(
                        responseBody!!
                    )
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
                // ABHA v3
                req.authData.otp.otpValue = encryptData(req.authData.otp.otpValue)
                req.authData.otp.timeStamp = getCurrentTimestamp()
                // ABHA v1/v2 API
//                val response = abhaApiService.verifyMobileOtp(req)
                // ABHA v3 API
                val response =
                    abhaApiService.verifyMobileOtp3(req, generateUUID(), getCurrentTimestamp())
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

    private fun sendErrorResponse(response: Response<ResponseBody>?): NetworkResult.Error {
        return when (response?.code()) {
            503 -> NetworkResult.Error(503, "Service Unavailable! Please try later!")
            else -> {
                val errorBody = response?.errorBody()?.string()
                val errorJson = JSONObject(errorBody.toString())
                val detailsArray = errorJson.getJSONArray("details")
                val detailsObject = detailsArray.getJSONObject(0)
                val errorMessage = detailsObject.getString("message")
                NetworkResult.Error(response?.code()!!, errorMessage)
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
                    5000, 5002 -> {
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
                val user = prefDao.getLoggedInUser() ?: throw IllegalStateException("No user logged in!!")
                mapHIDtoBeneficiary.providerServiceMapId = user.serviceMapId
                mapHIDtoBeneficiary.createdBy = user.userName
                val response = amritApiService.mapHealthIDToBeneficiary(mapHIDtoBeneficiary)
                val responseBody = response.body()?.string()
                when(responseBody?.let { JSONObject(it).getInt("statusCode") }) {
                    200 -> NetworkResult.Success(responseBody)
                    5000,5002 -> {
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

    suspend fun addHealthIdRecord(addHealthIdRecord: AddHealthIdRecord): NetworkResult<String> {
        return withContext((Dispatchers.IO)) {
            try {
                val user =
                    prefDao.getLoggedInUser() ?: throw IllegalStateException("No user logged in!!")
                addHealthIdRecord.providerServiceMapId = user.serviceMapId
                addHealthIdRecord.createdBy = user.userName
                val response = amritApiService.addHealthIdRecord(addHealthIdRecord)
                val responseBody = response.body()?.string()
                when (responseBody?.let { JSONObject(it).getInt("statusCode") }) {
                    200 -> NetworkResult.Success(responseBody)
                    5000, 5002 -> {
                        if (JSONObject(responseBody).getString("errorMessage")
                                .contentEquals("Invalid login key or session is expired")
                        ) {
                            userRepo.refreshTokenTmc(user.userName, user.password)
                            addHealthIdRecord(addHealthIdRecord)
                        } else {
                            NetworkResult.Error(
                                0,
                                JSONObject(responseBody).getString("errorMessage")
                            )
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
                when (responseBody?.let { JSONObject(it).getInt("statusCode") }) {
                    200 -> NetworkResult.Success(
                        JSONObject(responseBody).getJSONObject("data").getString("txnId")
                    )

                    5000, 5002 -> {
                        val error = JSONObject(responseBody).getString("errorMessage")
                        if (error.contentEquals("Invalid login key or session is expired")) {
                            val user = prefDao.getLoggedInUser()!!
                            userRepo.refreshTokenTmc(user.userName, user.password)
                            generateOtpHid(generateOtpHid)
                        } else {
                            NetworkResult.Error(0, error)
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