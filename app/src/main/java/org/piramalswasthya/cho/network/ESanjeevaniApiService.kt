package org.piramalswasthya.cho.network

import okhttp3.ResponseBody
import org.piramalswasthya.cho.model.EsanjeevniObject
import org.piramalswasthya.cho.model.EsanjeevniPatient
import org.piramalswasthya.cho.model.ModelObject
import org.piramalswasthya.cho.model.NetworkBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ESanjeevaniApiService {
    @Suppress("SpellCheckingInspection")
    private companion object ApiMappings{
        const val authenticate = "authenticateReference"
    }
    @POST(authenticate)
    suspend fun getAuthRefIdForWebView(@Body body : NetworkBody) : ModelObject

    @Headers("No-Auth: true")
    @POST("aus/api/ThirdPartyAuth/providerLogin")
    suspend fun getJwtToken(@Body json: NetworkBody): EsanjeevniObject

    @POST("ps/api/v1/Patient")
    suspend fun savePatient(@Body json: EsanjeevniPatient): Response<ResponseBody>

}