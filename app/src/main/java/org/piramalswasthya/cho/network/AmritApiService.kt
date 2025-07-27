package org.piramalswasthya.cho.network

import androidx.room.Delete
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.piramalswasthya.cho.model.ANCPost
import org.piramalswasthya.cho.model.AllocationItemDataRequest
import org.piramalswasthya.cho.model.BenNewFlow
import org.piramalswasthya.cho.model.ECTNetwork
import org.piramalswasthya.cho.model.ImmunizationPost
import org.piramalswasthya.cho.model.LabResultDTO
import org.piramalswasthya.cho.model.LocationRequest
import org.piramalswasthya.cho.model.MasterLabProceduresRequestModel
import org.piramalswasthya.cho.model.MasterLocationModel
import org.piramalswasthya.cho.model.ModelObject
import org.piramalswasthya.cho.model.NetworkBody
import org.piramalswasthya.cho.model.OutreachActivityNetworkModel
import org.piramalswasthya.cho.model.PNCNetwork
import org.piramalswasthya.cho.model.PatientDoctorFormUpsync
import org.piramalswasthya.cho.model.PatientNetwork
import org.piramalswasthya.cho.model.PatientVisitInformation
import org.piramalswasthya.cho.model.PharmacistPatientDataRequest
import org.piramalswasthya.cho.model.PharmacistPatientIssueDataRequest
import org.piramalswasthya.cho.model.PrescribedMedicineDataRequest
import org.piramalswasthya.cho.model.PrescriptionTemplateDB
import org.piramalswasthya.cho.model.StockItemRequest
import org.piramalswasthya.cho.model.UserMasterVillage
import org.piramalswasthya.cho.model.fhir.SelectedOutreachProgram
import org.piramalswasthya.cho.utils.Constants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AmritApiService {


//---------------Amrit Demo------------------

    @Suppress("SpellCheckingInspection")
    private companion object ApiMappings{
        const val authenticate = "authenticateReference"
    }
    @Headers("No-Auth: true", "User-Agent: okhttp")
    @POST("common-api/user/userAuthenticate")   // @POST("common-api/user/userAuthenticate/")
    suspend fun getJwtToken(@Body json: TmcAuthUserRequest): Response<ResponseBody>

    @Headers("No-Auth: true")
    @POST("fhir/Patient")
    suspend fun createPatient(@Body json: RequestBody): Response<ResponseBody>

    @POST("common-api/doortodoorapp/getUserDetails")
    suspend fun getUserDetailsById(@Body userDetail: TmcUserDetailsRequest) : Response<ResponseBody>

    @POST("common-api/user/getLoginResponse")
    suspend fun getLoginResponse() : Response<ResponseBody>

    @POST("hwc-api/user/getUserVanSpDetails?apiKey=undefined")
    suspend fun getUserVanSpDetails(
        @Body vanServiceType: TmcUserVanSpDetailsRequest
    ): Response<ResponseBody>


    @POST("tm-api/user/getUserVanSpDetails/")
    suspend fun getTMVanSpDetails(
        @Body vanServiceType: TmcUserVanSpDetailsRequest
    ): Response<ResponseBody>

    @POST("hwc-api/location/getLocDetailsBasedOnSpIDAndPsmID")
    suspend fun getLocDetailsBasedOnSpIDAndPsmID(@Body request: LocationRequest): Response<ResponseBody>

    @GET("hwc-api/location/get/districtMaster/{stateId}")
    suspend fun getDistricts(@Path("stateId") stateId: Int): Response<ResponseBody>

    @GET("hwc-api/location/get/districtBlockMaster/{districtId}")
    suspend fun getDistrictBlocks(@Path("districtId") districtId: Int): Response<ResponseBody>

    @GET("hwc-api/wo/location/outreachMaster/{stateID}/wo")
    suspend fun getOutreachDropdownList(@Path("stateID") stateID: Int): Response<ResponseBody>

    @GET("hwc-api/location/get/villageMasterFromBlockID/{blockId}")
    suspend fun getVillages(@Path("blockId") blockId: Int, ): Response<ResponseBody>

    @GET("hwc-api/location/get/stateMaster?apiKey=undefined")
    suspend fun getStatesMasterList(): Response<ResponseBody>

    @GET("common-api/beneficiary/getLanguageList?apiKey=undefined")
    suspend fun getLanguagesList(): Response<ResponseBody>

    @GET("/hwc-api/master/get/visitReasonAndCategories?apiKey=undefined")
    suspend fun getVisitReasonAndCategories(): Response<ResponseBody>

    @POST("hwc-api/registrar/registrarMasterData?apiKey=undefined")
    suspend fun getRegistrarMasterData(@Body spID: TmcLocationDetailsRequest) : Response<ResponseBody>

    @POST("hwc-api/sync/userActivityLogsToServer")
    suspend fun saveUpsyncDetails(@Body selectedOutreachProgramList: List<SelectedOutreachProgram>) : Response<ResponseBody>

    @POST("hwc-api/sync/beneficiariesToServer")
    suspend fun saveBenificiaryDetails(@Body benificiary: PatientNetwork) : Response<ResponseBody>

    @POST("hwc-api/sync/beneficiariesToAppCount")
    suspend fun getBeneficiariesCount(@Body villageList: VillageIdList): Response<ResponseBody>

    @POST("hwc-api/sync/beneficiariesToApp")
    suspend fun downloadBeneficiariesFromServer(@Body villageList: VillageIdList): Response<ResponseBody>

    @POST("hwc-api/sync/activity/create")
    suspend fun createNewActivity(@Body networkModel: OutreachActivityNetworkModel): Response<ResponseBody>

    @GET("hwc-api/sync/activity/{userID}/getAllByUser")
    suspend fun getActivityByUser(@Path("userID") userID: Int): Response<ResponseBody>

    @GET("hwc-api/sync/activity/{activityId}/getById")
    suspend fun getActivityById(@Path("activityId") activityId: Int): Response<ResponseBody>

    @POST("hwc-api/sync/prescriptionTemplatesToServer")
    suspend fun sendTemplateToServer(@Body prescriptionTemplateDB: List<PrescriptionTemplateDB>): Response<ResponseBody>

    @DELETE("hwc-api/sync/{userID}/prescriptionTemplates/{tempID}/delete")
    suspend fun deleteTemplateFromServer(
        @Path("userID") userID: Int,
        @Path("tempID") tempID: Int
    ): Response<ResponseBody>

    @GET("hwc-api/sync/{userID}/prescriptionTemplatesDataToApp")
    suspend fun getTemplateFromServer(@Path("userID") userID: Int): Response<ResponseBody>

    @POST("/flw-api/maternalCare/ancVisit/saveAll")
    suspend fun postAncForm(@Body ancPostList: List<ANCPost>): Response<ResponseBody>

    @POST("/flw-api/maternalCare/pnc/saveAll")
    suspend fun postPncForm(@Body ancPostList: List<PNCNetwork>): Response<ResponseBody>

    @POST("/flw-api/child-care/vaccination/saveAll")
    suspend fun postChildImmunizationDetails(@Body immunizationList: List<ImmunizationPost>): Response<ResponseBody>

    @POST("/flw-api/couple/tracking/saveAll")
    suspend fun postEctForm(@Body ectPostList: List<ECTNetwork>): Response<ResponseBody>

    @GET("/flw-api/child-care/vaccine/getAll")
    suspend fun getAllChildVaccines(@Query("category") category: String): Response<ResponseBody>

    @POST("/hwc-api/sync/generalOPDNurseFormDataToServer")
    suspend fun saveNurseData(@Body patientVisitInfo: PatientVisitInformation) : Response<ResponseBody>

    @POST("hwc-api/sync/beneficiaryGeneralOPDNurseFormDataToApp")
    suspend fun getNurseData(@Body nurseDataRequest: NurseDataRequest) : Response<ResponseBody>

    @POST("/hwc-api/labTechnician/get/prescribedProceduresList?apiKey=undefined")
    suspend fun getLabTestPrescribedProceduresList(@Body labProceduresDataRequest: LabProceduresDataRequest) : Response<ResponseBody>

    // TODO: update with final api once developed
    @POST("/hwc-api/labTechnician/get/fetchProcCompMapMasterData?apiKey=undefined")
    suspend fun getMasterLabProceduresDate(/*@Body masterLabProceduresRequestModel: MasterLabProceduresRequestModel*/) : Response<ResponseBody>

    @POST("/hwc-api/generalOPD/save/doctorData?apiKey=undefined")
    suspend fun saveDoctorData(@Body patientDoctorForm: PatientDoctorFormUpsync) : Response<ResponseBody>

    @POST("/hwc-api/generalOPD/update/doctorData?apiKey=undefined")
    suspend fun updateDoctorData(@Body patientDoctorForm: PatientDoctorFormUpsync) : Response<ResponseBody>

    @POST("/hwc-api/labTechnician/save/LabTestResult?apiKey=undefined")
    suspend fun saveLabData(@Body labResultDTO: LabResultDTO) : Response<ResponseBody>

    @GET("/flw-api/user/getUserDetail")
    suspend fun getUserDetail(@Query("userId") userId: Int) : Response<ResponseBody>

    @POST("hwc-api/sync/benFlowStatusRecordsCount")
    suspend fun getBenFlowRecordCount(@Body villageList : VillageIdList) : Response<ResponseBody>

    @POST("hwc-api/sync/benFlowStatusRecordsToApp")
    suspend fun getBenFlowRecords(@Body villageList : VillageIdList) : Response<ResponseBody>

    @POST("hwc-api/registrar/create/BenReVisitToNurse")
    suspend fun createBenReVisitToNurse(@Body benNewFlow : BenNewFlow) : Response<ResponseBody>

//    @POST("hwc-api/registrar/registrarBeneficaryRegistrationNew?apiKey=f5e3e002-8ef8-44cd-9064-45fbc8cad")
//    suspend fun saveBenificiaryDetails(@Body benificiary: PatientNetwork) : Response<ResponseBody>

    @GET("/common-api/covid/master/VaccinationTypeAndDoseTaken?apiKey=undefined")
    suspend fun getVaccinationTypeAndDoseTaken(): Response<ResponseBody>

    @GET("hwc-api/masterVillage/{userID}/get")
    suspend fun getUserMasterVillage(@Path("userID") userID: Int): Response<ResponseBody>

    @POST("hwc-api/masterVillage/set")
    suspend fun setUserMasterVillage(@Body userMasterVillage: UserMasterVillage) : Response<ResponseBody>

    @POST("hwc-api/wo/location/update/villageCoordinates")
    suspend fun updateMasterVillageCoordinates(@Body masterLocationModel: MasterLocationModel) : Response<ResponseBody>

    @POST(authenticate)
    suspend fun getAuthRefIdForWebView(@Body body : NetworkBody) : ModelObject


    @POST("fhir-api/healthIDWithUID/createHealthIDWithUID")
    suspend fun createHid(@Body createHealthIdRequest: CreateHealthIdRequest): Response<ResponseBody>

    @GET("common-api/facility/getWorklocationMappedAbdmFacility/{workingLocationId}")
    suspend fun getWorkLocationMappedAbdmFacility(@Path("workingLocationId") workingLocationId :String): Response<ResponseBody>

    @POST("fhir-api/facility/saveAbdmFacilityId")
    suspend fun saveAbdmFacilityId(@Body saveAbdmFacilityId: SaveAbdmFacilityId): Response<ResponseBody>

    @POST("fhir-api/healthID/getBenhealthID")
    suspend fun getBenHealthID(@Body getBenHealthIdRequest: GetBenHealthIdRequest): Response<ResponseBody>

    @POST("fhir-api/careContext/generateOTPForCareContext")
    suspend fun generateOTPForCareContext(@Body generateOTPForCareContext: GenerateOTPForCareContextRequest): Response<ResponseBody>

    @POST("fhir-api/careContext/validateOTPAndCreateCareContext")
    suspend fun validateOTPAndCreateCareContext(@Body validateOTPAndCreateCareContextRequest: ValidateOTPAndCreateCareContextRequest): Response<ResponseBody>

    @POST("fhir-api/healthID/mapHealthIDToBeneficiary")
    suspend fun mapHealthIDToBeneficiary(@Body mapHIDtoBeneficiary: MapHIDtoBeneficiary): Response<ResponseBody>

    @POST("fhir-api/healthIDRecord/addHealthIdRecord")
    suspend fun addHealthIdRecord(@Body addHealthIdRecord: AddHealthIdRecord): Response<ResponseBody>

    @POST("fhir-api/healthIDCard/generateOTP")
    suspend fun generateOtpHealthId(@Body generateOtpHid: GenerateOtpHid): Response<ResponseBody>

    @POST("fhir-api/healthIDCard/verifyOTPAndGenerateHealthCard")
    suspend fun verifyOtpAndGenerateHealthCard(@Body validateOtpHid: ValidateOtpHid): Response<ResponseBody>
    @GET("hwc-api/master/nurse/masterData/{visitCategoryID}/{providerServiceMapID}/{gender}")
    suspend fun getNurseMasterData(@Path("visitCategoryID") visitCategoryID: Int,
                                   @Path("providerServiceMapID") providerServiceMapID : Int,
                                   @Path("gender") gender: String,
                                   @Query("apiKey") apiKey :String): Response<ResponseBody>

    @GET("hwc-api/master/doctor/masterData/{visitCategoryID}/{providerServiceMapID}/{gender}/{facilityID}/{vanID}")
    suspend fun getDoctorMasterData(@Path("visitCategoryID") visitCategoryID: Int,
                                   @Path("providerServiceMapID") providerServiceMapID : Int,
                                   @Path("gender") gender: String,
                                   @Path("facilityID") facilityID: Int,
                                   @Path("vanID") vanID: Int,
                                   @Query("apiKey") apiKey :String): Response<ResponseBody>

    @POST("hwc-api/generalOPD/getBenCaseRecordFromDoctorGeneralOPD")
    suspend fun getDoctorData(@Body nurseDataRequest: NurseDataRequest) : Response<ResponseBody>

    @POST("/hwc-api/registrar/get/benDetailsByRegIDForLeftPanelNew?apiKey=undefined")
    suspend fun getPharmacistPatientDetails(@Body pharmacistPatientDataRequest: PharmacistPatientDataRequest) : Response<ResponseBody>

    @POST("/inventory-api/allocateStockFromItemID/{facilityID}?apiKey=undefined")
    suspend fun getPharmacistAllocationItemList(@Body allocationItemDataRequest: List<AllocationItemDataRequest>, @Path("facilityID") facilityID: Int) : Response<ResponseBody>

    @POST("/inventory-api/RX/getPrescribedMedicines?apiKey=undefined")
    suspend fun getPharmacistPrescriptionList(@Body prescribedMedicineDataRequest: PrescribedMedicineDataRequest) : Response<ResponseBody>

    @POST("/inventory-api/patientIssue?apiKey=undefined")
    suspend fun savePharmacistData(@Body patientIssue: PharmacistPatientIssueDataRequest) : Response<ResponseBody>

    @POST("/inventory-api/itemBatchPartialSearch")
    suspend fun getPharmacistStockItemList(
        @Body request: StockItemRequest
    ):Response<ResponseBody>


}