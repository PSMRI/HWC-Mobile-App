package org.piramalswasthya.cho.network


import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface AbhaApiService {

    @Headers("No-Auth: true")
    @POST
    suspend fun getToken(
        @Url url: String = "https://dev.abdm.gov.in/gateway/v0.5/sessions",
        @Body request: AbhaTokenRequest = AbhaTokenRequest()
    ): Response<ResponseBody>

    @POST("v1/registration/aadhaar/generateOtp")
    suspend fun generateAadhaarOtp(@Body aadhaar: AbhaGenerateAadhaarOtpRequest): Response<ResponseBody>

    @POST("v2/registration/aadhaar/generateOtp")
    suspend fun generateAadhaarOtpV2(@Body aadhaar: AbhaGenerateAadhaarOtpRequest): Response<ResponseBody>

    @POST("v1/registration/aadhaar/resendAadhaarOtp")
    suspend fun resendAadhaarOtp(@Body aadhaar: AbhaResendAadhaarOtpRequest): Response<ResponseBody>

    @POST("v1/registration/aadhaar/verifyOTP")
    suspend fun verifyAadhaarOtp(@Body request: AbhaVerifyAadhaarOtpRequest): Response<ResponseBody>

    @POST("v1/registration/aadhaar/generateMobileOTP")
    suspend fun generateMobileOtp(@Body mobile: AbhaGenerateMobileOtpRequest): Response<ResponseBody>

    @POST("v2/registration/aadhaar/checkAndGenerateMobileOTP")
    suspend fun checkAndGenerateMobileOtp(@Body request: AbhaGenerateMobileOtpRequest): Response<ResponseBody>

    @POST("v1/registration/aadhaar/verifyMobileOTP")
    suspend fun verifyMobileOtp(@Body request: AbhaVerifyMobileOtpRequest): Response<ResponseBody>

    @POST("v1/registration/aadhaar/createHealthIdWithPreVerified")
    suspend fun createAbhaId(@Body request: CreateAbhaIdRequest): Response<ResponseBody>

    @POST("v1/hid/benefit/createHealthId/demo/auth")
    suspend fun createAbhaIdGov(@Body request: CreateAbhaIdGovRequest): Response<ResponseBody>

    @GET("v1/account/getCard")
    suspend fun getPdfCard(): Response<ResponseBody>

    @GET("v1/account/getPngCard")
    suspend fun getPngCard(): Response<ResponseBody>

    @GET
    suspend fun getAuthCert(
        @Url url: String = "https://healthidsbx.abdm.gov.in/api/v2/auth/cert"
    ): Response<ResponseBody>

    @GET("v2/ha/lgd/states")
    suspend fun getStateAndDistricts(): Response<ResponseBody>

    @POST("v1/registration/aadhaar/verifyBio")
    suspend fun verifyBio(@Body aadhaarVerifyBioRequest: AadhaarVerifyBioRequest): Response<ResponseBody>
}