package org.piramalswasthya.cho.network

import org.piramalswasthya.cho.model.ModelObject
import org.piramalswasthya.cho.model.NetworkBody
import retrofit2.http.Body
import retrofit2.http.POST

interface ESanjeevaniApiService {
    @Suppress("SpellCheckingInspection")
    private companion object ApiMappings{
        const val authenticate = "authenticateReference"
    }
    @POST(authenticate)
    suspend fun getAuthRefIdForWebView(@Body body : NetworkBody) : ModelObject
}