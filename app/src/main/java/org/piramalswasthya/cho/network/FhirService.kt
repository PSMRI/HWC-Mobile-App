package org.piramalswasthya.cho.network

import okhttp3.ResponseBody
import org.hl7.fhir.r4.model.Patient
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface FhirService {

    @Headers("No-Auth: true")
    @POST("fhir/Patient")
    suspend fun createPatient(@Body json: Patient): Response<ResponseBody>

}