package org.piramalswasthya.cho.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface FlwApiService {

    @GET("/flw-0.0.1/user/getUserDetail")
    suspend fun getUserDetail(@Query("userId") userId: Int) : Response<ResponseBody>

}