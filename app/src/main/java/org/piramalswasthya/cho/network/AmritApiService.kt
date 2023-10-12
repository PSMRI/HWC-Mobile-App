package org.piramalswasthya.cho.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.hl7.fhir.r4.model.Patient
import org.piramalswasthya.cho.model.BenNewFlow
import org.piramalswasthya.cho.model.DownloadPatRequest
import org.piramalswasthya.cho.model.LabResultDTO
import org.piramalswasthya.cho.model.LocationRequest
import org.piramalswasthya.cho.model.ModelObject
import org.piramalswasthya.cho.model.NetworkBody
import org.piramalswasthya.cho.model.PatientDoctorFormUpsync
import org.piramalswasthya.cho.model.PatientNetwork
import org.piramalswasthya.cho.model.PatientVisitInformation
import org.piramalswasthya.cho.utils.Constants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AmritApiService {
//    @Suppress("SpellCheckingInspection")
//    private companion object ApiMappings{
//        const val authenticate = "authenticateReference"
//    }
//    @Headers("No-Auth: true")
//    @POST(Constants.commonApi + "/user/userAuthenticate/")
//    suspend fun getJwtToken(@Body json: TmcAuthUserRequest): Response<ResponseBody>
//
//    @Headers("No-Auth: true")
//    @POST("fhir/Patient")
//    suspend fun createPatient(@Body json: RequestBody): Response<ResponseBody>
//
//    @POST(Constants.commonApi + "/doortodoorapp/getUserDetails")
//    suspend fun getUserDetailsById(@Body userDetail: TmcUserDetailsRequest) : Response<ResponseBody>
//
//    @POST(Constants.commonApi + "/user/getLoginResponse")
//    suspend fun getLoginResponse() : Response<ResponseBody>
//
//    @POST(Constants.hwcApi + "/user/getUserVanSpDetails?apiKey=undefined")
//    suspend fun getUserVanSpDetails(
//        @Body vanServiceType: TmcUserVanSpDetailsRequest
//    ): Response<ResponseBody>
//
//
//    @POST("tmapi-v1.0/user/getUserVanSpDetails/")
//    suspend fun getTMVanSpDetails(
//        @Body vanServiceType: TmcUserVanSpDetailsRequest
//    ): Response<ResponseBody>
//
//    @POST(Constants.hwcApi + "/location/getLocDetailsBasedOnSpIDAndPsmID")
//    suspend fun getStates(@Body request: LocationRequest): Response<ResponseBody>
//
//    @GET(Constants.hwcApi + "/location/get/districtMaster/{stateId}")
//    suspend fun getDistricts(@Path("stateId") stateId: Int): Response<ResponseBody>
//
//    @GET(Constants.hwcApi + "/location/get/districtBlockMaster/{districtId}")
//    suspend fun getDistrictBlocks(@Path("districtId") districtId: Int): Response<ResponseBody>
//
//    @GET(Constants.hwcApi + "/location/get/villageMasterFromBlockID/{blockId}")
//    suspend fun getVillages(@Path("blockId") blockId: Int, ): Response<ResponseBody>
//
//    @GET(Constants.hwcApi + "/location/get/stateMaster?apiKey=undefined")
//    suspend fun getStatesMasterList(): Response<ResponseBody>
//
//    @GET(Constants.commonApi + "/beneficiary/getLanguageList?apiKey=undefined")
//    suspend fun getLanguagesList(): Response<ResponseBody>
//
//    @GET(Constants.hwcApi + "/master/get/visitReasonAndCategories?apiKey=undefined")
//    suspend fun getVisitReasonAndCategories(): Response<ResponseBody>
//
//    @POST(Constants.hwcApi + "/registrar/registrarMasterData?apiKey=undefined")
//    suspend fun getRegistrarMasterData(@Body spID: TmcLocationDetailsRequest) : Response<ResponseBody>
//
//    @POST(Constants.hwcApi + "/sync/beneficiariesToServer")
//    suspend fun saveBenificiaryDetails(@Body benificiary: PatientNetwork) : Response<ResponseBody>
//
//    @POST(Constants.hwcApi + "/generalOPD/save/nurseData?apiKey=undefined")
//    suspend fun saveNurseData(@Body patientVisitInfo: PatientVisitInformation) : Response<ResponseBody>
//
////    @GET("/flw-0.0.1/user/getUserDetail")
////    suspend fun getUserDetail(@Query("userId") userId: Int) : Response<ResponseBody>
//
//    @POST(Constants.hwcApi + "/sync/benFlowStatusRecordsToApp")
//    suspend fun getBenFlowRecords(@Body villageList : VillageIdList) : Response<ResponseBody>
//
////    @POST("https://55f8-183-82-96-201.ngrok-free.app/registrar/registrarBeneficaryRegistrationNew?apiKey=f5e3e002-8ef8-44cd-9064-45fbc8cad")
////    suspend fun saveBenificiaryDetails(@Body benificiary: PatientNetwork) : Response<ResponseBody>
//
//    @GET(Constants.commonApi + "/covid/master/VaccinationTypeAndDoseTaken?apiKey=undefined")
//    suspend fun getVaccinationTypeAndDoseTaken(): Response<ResponseBody>
//
//    @POST(authenticate)
//    suspend fun getAuthRefIdForWebView(@Body body : NetworkBody) : ModelObject
//
////    @POST("tmapi-v1.0/user/getUserVanSpDetails/")
////    suspend fun getTMVanSpDetails(
////        @Body vanServiceType: TmcUserVanSpDetailsRequest
////    ): Response<ResponseBody>
////
////    @POST("mmuapi-v1.0/location/getLocDetailsBasedOnSpIDAndPsmID/")
////    suspend fun getLocationDetails(
////        @Body locationDetails: TmcLocationDetailsRequest
////    ): Response<ResponseBody>
////
////    @POST("bengenapi-v1.0/generateBeneficiaryController/generateBeneficiaryIDs/")
////    suspend fun generateBeneficiaryIDs(
////        @Body obj: TmcGenerateBenIdsRequest
////    ): Response<ResponseBody>
////
////
////    @POST("tmapi-v1.0/registrar/registrarBeneficaryRegistrationNew")
////    suspend fun getBenIdFromBeneficiarySending(@Body beneficiaryDataSending: BeneficiaryDataSending): Response<ResponseBody>
////
////    @POST("identity-0.0.1/rmnch/syncDataToAmrit")
////    suspend fun submitRmnchDataAmrit(@Body sendingRMNCHData: SendingRMNCHData): Response<ResponseBody>
////
////    @POST("identity-0.0.1/rmnch/getBeneficiaryDataForAsha")
////    suspend fun getBeneficiaries(@Body userDetail: GetBenRequest): Response<ResponseBody>
////
////    @POST("identity-0.0.1/id/getByBenId")
////    suspend fun getBeneficiaryWithId(@Query("benId") benId: Long) : Response<ResponseBody>
////
//    @POST(Constants.hwcApi + "/healthIDWithUID/createHealthIDWithUID")
//    suspend fun createHid(@Body createHealthIdRequest: CreateHealthIdRequest): Response<ResponseBody>
//
//    @POST(Constants.hwcApi + "/healthID/mapHealthIDToBeneficiary")
//    suspend fun mapHealthIDToBeneficiary(@Body mapHIDtoBeneficiary: MapHIDtoBeneficiary): Response<ResponseBody>
//
//    @POST(Constants.hwcApi + "/healthIDCard/generateOTP")
//    suspend fun generateOtpHealthId(@Body generateOtpHid: GenerateOtpHid): Response<ResponseBody>
//
//    @POST(Constants.hwcApi + "/healthIDCard/verifyOTPAndGenerateHealthCard")
//    suspend fun verifyOtpAndGenerateHealthCard(@Body validateOtpHid: ValidateOtpHid): Response<ResponseBody>
//    @GET(Constants.hwcApi + "/master/nurse/masterData/{visitCategoryID}/{providerServiceMapID}/{gender}")
//    suspend fun getNurseMasterData(@Path("visitCategoryID") visitCategoryID: Int,
//                                   @Path("providerServiceMapID") providerServiceMapID : Int,
//                                   @Path("gender") gender: String,
//                                   @Query("apiKey") apiKey :String): Response<ResponseBody>
//
//    @GET(Constants.hwcApi + "/master/doctor/masterData/{visitCategoryID}/{providerServiceMapID}/{gender}/{facilityID}/{vanID}")
//    suspend fun getDoctorMasterData(@Path("visitCategoryID") visitCategoryID: Int,
//                                    @Path("providerServiceMapID") providerServiceMapID : Int,
//                                    @Path("gender") gender: String,
//                                    @Path("facilityID") facilityID: Int,
//                                    @Path("vanID") vanID: Int,
//                                    @Query("apiKey") apiKey :String): Response<ResponseBody>

//---------------Amrit Demo------------------

    @Suppress("SpellCheckingInspection")
    private companion object ApiMappings{
        const val authenticate = "authenticateReference"
    }
    @Headers("No-Auth: true")
    @POST("commonapi-v1.0/user/userAuthenticate/")
    suspend fun getJwtToken(@Body json: TmcAuthUserRequest): Response<ResponseBody>

    @Headers("No-Auth: true")
    @POST("fhir/Patient")
    suspend fun createPatient(@Body json: RequestBody): Response<ResponseBody>

    @POST("commonapi-v1.0/doortodoorapp/getUserDetails")
    suspend fun getUserDetailsById(@Body userDetail: TmcUserDetailsRequest) : Response<ResponseBody>

    @POST("commonapi-v1.0/user/getLoginResponse")
    suspend fun getLoginResponse() : Response<ResponseBody>

    @POST("hwc-facility-service/user/getUserVanSpDetails?apiKey=undefined")
    suspend fun getUserVanSpDetails(
        @Body vanServiceType: TmcUserVanSpDetailsRequest
    ): Response<ResponseBody>


    @POST("tmapi-v1.0/user/getUserVanSpDetails/")
    suspend fun getTMVanSpDetails(
        @Body vanServiceType: TmcUserVanSpDetailsRequest
    ): Response<ResponseBody>

    @POST("hwc-facility-service/location/getLocDetailsBasedOnSpIDAndPsmID")
    suspend fun getLocDetailsBasedOnSpIDAndPsmID(@Body request: LocationRequest): Response<ResponseBody>

    @GET("hwc-facility-service/location/get/districtMaster/{stateId}")
    suspend fun getDistricts(@Path("stateId") stateId: Int): Response<ResponseBody>

    @GET("hwc-facility-service/location/get/districtBlockMaster/{districtId}")
    suspend fun getDistrictBlocks(@Path("districtId") districtId: Int): Response<ResponseBody>

    @GET("hwc-facility-service/wo/location/outreachMaster/{stateID}/wo")
    suspend fun getOutreachDropdownList(@Path("stateID") stateID: Int): Response<ResponseBody>

    @GET("hwc-facility-service/location/get/villageMasterFromBlockID/{blockId}")
    suspend fun getVillages(@Path("blockId") blockId: Int, ): Response<ResponseBody>

    @GET("hwc-facility-service/location/get/stateMaster?apiKey=undefined")
    suspend fun getStatesMasterList(): Response<ResponseBody>

    @GET("commonapi-v1.0/beneficiary/getLanguageList?apiKey=undefined")
    suspend fun getLanguagesList(): Response<ResponseBody>

    @GET("/hwc-facility-service/master/get/visitReasonAndCategories?apiKey=undefined")
    suspend fun getVisitReasonAndCategories(): Response<ResponseBody>

    @POST("hwc-facility-service/registrar/registrarMasterData?apiKey=undefined")
    suspend fun getRegistrarMasterData(@Body spID: TmcLocationDetailsRequest) : Response<ResponseBody>

    @POST("hwc-facility-service/sync/beneficiariesToServer")
    suspend fun saveBenificiaryDetails(@Body benificiary: PatientNetwork) : Response<ResponseBody>
    @POST("hwc-facility-service/sync/beneficiariesToApp")
    suspend fun downloadBeneficiariesFromServer(@Body villageList: VillageIdList): Response<ResponseBody>

    @POST("/hwc-facility-service/sync/generalOPDNurseFormDataToServer")
    suspend fun saveNurseData(@Body patientVisitInfo: PatientVisitInformation) : Response<ResponseBody>

    @POST("hwc-facility-service/sync/beneficiaryGeneralOPDNurseFormDataToApp")
    suspend fun getNurseData(@Body nurseDataRequest: NurseDataRequest) : Response<ResponseBody>

    @POST("/hwc-facility-service/labTechnician/get/prescribedProceduresList?apiKey=undefined")
    suspend fun getLabTestPrescribedProceduresList(@Body labProceduresDataRequest: LabProceduresDataRequest) : Response<ResponseBody>

    @POST("/hwc-facility-service/generalOPD/save/doctorData?apiKey=undefined")
    suspend fun saveDoctorData(@Body patientDoctorForm: PatientDoctorFormUpsync) : Response<ResponseBody>

    @POST("/hwc-facility-service/labTechnician/save/LabTestResult?apiKey=undefined")
    suspend fun saveLabData(@Body labResultDTO: LabResultDTO) : Response<ResponseBody>

    @GET("/flw-0.0.1/user/getUserDetail")
    suspend fun getUserDetail(@Query("userId") userId: Int) : Response<ResponseBody>

    @POST("hwc-facility-service/sync/benFlowStatusRecordsToApp")
    suspend fun getBenFlowRecords(@Body villageList : VillageIdList) : Response<ResponseBody>

    @POST("hwc-facility-service/registrar/create/BenReVisitToNurse")
    suspend fun createBenReVisitToNurse(@Body benNewFlow : BenNewFlow) : Response<ResponseBody>

//    @POST("hwc-facility-service/registrar/registrarBeneficaryRegistrationNew?apiKey=f5e3e002-8ef8-44cd-9064-45fbc8cad")
//    suspend fun saveBenificiaryDetails(@Body benificiary: PatientNetwork) : Response<ResponseBody>

    @GET("/commonapi-v1.0/covid/master/VaccinationTypeAndDoseTaken?apiKey=undefined")
    suspend fun getVaccinationTypeAndDoseTaken(): Response<ResponseBody>

    @POST(authenticate)
    suspend fun getAuthRefIdForWebView(@Body body : NetworkBody) : ModelObject

//    @POST("tmapi-v1.0/user/getUserVanSpDetails/")
//    suspend fun getTMVanSpDetails(
//        @Body vanServiceType: TmcUserVanSpDetailsRequest
//    ): Response<ResponseBody>
//
//    @POST("mmuapi-v1.0/location/getLocDetailsBasedOnSpIDAndPsmID/")
//    suspend fun getLocationDetails(
//        @Body locationDetails: TmcLocationDetailsRequest
//    ): Response<ResponseBody>
//
//    @POST("bengenapi-v1.0/generateBeneficiaryController/generateBeneficiaryIDs/")
//    suspend fun generateBeneficiaryIDs(
//        @Body obj: TmcGenerateBenIdsRequest
//    ): Response<ResponseBody>
//
//
//    @POST("tmapi-v1.0/registrar/registrarBeneficaryRegistrationNew")
//    suspend fun getBenIdFromBeneficiarySending(@Body beneficiaryDataSending: BeneficiaryDataSending): Response<ResponseBody>
//
//    @POST("identity-0.0.1/rmnch/syncDataToAmrit")
//    suspend fun submitRmnchDataAmrit(@Body sendingRMNCHData: SendingRMNCHData): Response<ResponseBody>
//
//    @POST("identity-0.0.1/rmnch/getBeneficiaryDataForAsha")
//    suspend fun getBeneficiaries(@Body userDetail: GetBenRequest): Response<ResponseBody>
//
//    @POST("identity-0.0.1/id/getByBenId")
//    suspend fun getBeneficiaryWithId(@Query("benId") benId: Long) : Response<ResponseBody>
//
    @POST("fhirapi-v1.0/healthIDWithUID/createHealthIDWithUID")
    suspend fun createHid(@Body createHealthIdRequest: CreateHealthIdRequest): Response<ResponseBody>
    @POST("fhirapi-v1.0/healthID/getBenhealthID")
    suspend fun getBenHealthID(@Body getBenHealthIdRequest: GetBenHealthIdRequest): Response<ResponseBody>

    @POST("fhirapi-v1.0/healthID/mapHealthIDToBeneficiary")
    suspend fun mapHealthIDToBeneficiary(@Body mapHIDtoBeneficiary: MapHIDtoBeneficiary): Response<ResponseBody>

    @POST("fhirapi-v1.0/healthIDCard/generateOTP")
    suspend fun generateOtpHealthId(@Body generateOtpHid: GenerateOtpHid): Response<ResponseBody>

    @POST("fhirapi-v1.0/healthIDCard/verifyOTPAndGenerateHealthCard")
    suspend fun verifyOtpAndGenerateHealthCard(@Body validateOtpHid: ValidateOtpHid): Response<ResponseBody>
    @GET("hwc-facility-service/master/nurse/masterData/{visitCategoryID}/{providerServiceMapID}/{gender}")
    suspend fun getNurseMasterData(@Path("visitCategoryID") visitCategoryID: Int,
                                   @Path("providerServiceMapID") providerServiceMapID : Int,
                                   @Path("gender") gender: String,
                                   @Query("apiKey") apiKey :String): Response<ResponseBody>

    @GET("hwc-facility-service/master/doctor/masterData/{visitCategoryID}/{providerServiceMapID}/{gender}/{facilityID}/{vanID}")
    suspend fun getDoctorMasterData(@Path("visitCategoryID") visitCategoryID: Int,
                                   @Path("providerServiceMapID") providerServiceMapID : Int,
                                   @Path("gender") gender: String,
                                   @Path("facilityID") facilityID: Int,
                                   @Path("vanID") vanID: Int,
                                   @Query("apiKey") apiKey :String): Response<ResponseBody>

    @POST("hwc-facility-service/generalOPD/getBenCaseRecordFromDoctorGeneralOPD")
    suspend fun getDoctorData(@Body nurseDataRequest: NurseDataRequest) : Response<ResponseBody>

    @POST("/hwc-facility-service/registrar/get/benDetailsByRegIDForLeftPanelNew?apiKey=undefined")
    suspend fun getPharmacistPatientDetails(@Body pharmacistPatientDataRequest: PharmacistPatientDataRequest) : Response<ResponseBody>

    @POST("/inventoryapi-v1.0/RX/getPrescribedMedicines?apiKey=undefined")
    suspend fun getPharmacistPrescriptionList(@Body prescribedMedicineDataRequest: PrescribedMedicineDataRequest) : Response<ResponseBody>

    @GET("hwc-facility-service/common/getPharma-worklist-New/{providerServiceMapID}/{serviceID}/{vanID}")
    suspend fun dispensePrescriptionForPharmacist(@Path("providerServiceMapID") providerServiceMapID: Int,
                                   @Path("serviceID") serviceID : Int,
                                   @Path("vanID") vanID: Int,
                                   @Query("apiKey") apiKey :String): Response<ResponseBody>

}