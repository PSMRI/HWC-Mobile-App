package org.piramalswasthya.cho.network

import com.squareup.moshi.JsonClass
import org.piramalswasthya.cho.ui.abha_id_activity.AbhaClientConstants
import org.piramalswasthya.cho.model.BenFlow
import org.piramalswasthya.cho.model.PrescriptionItemDTO
import org.piramalswasthya.cho.model.VillageLocationData
import java.text.SimpleDateFormat
import java.util.Locale

@JsonClass(generateAdapter = true)
data class D2DAuthUserRequest(
    val username: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class D2DAuthUserResponse(
    val jwt: String
)

@JsonClass(generateAdapter = true)
data class D2DSaveUserRequest(
    val id: Int,
    val username: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class D2DSaveUserResponse(
    val jwt: String
)


////////////////////---------TMC------------//////////////////////

@JsonClass(generateAdapter = true)
data class TmcAuthUserRequest(
    val userName: String,
    val password: String,
    val authKey: String = "",
    val doLogout: Boolean = true
)

@JsonClass(generateAdapter = true)
data class TmcUserDetailsRequest(
    val userID: Int
)

@JsonClass(generateAdapter = true)
data class TmcUserVanSpDetailsRequest(
    val userID: Int,
    val providerServiceMapID: Int
)


@JsonClass(generateAdapter = true)
data class TmcLocationDetailsRequest(
    val spID: Int,
    val spPSMID: Int
)

@JsonClass(generateAdapter = true)
data class TmcGenerateBenIdsRequest(
    val benIDRequired: Int,
    val vanID: Int
)

@JsonClass(generateAdapter = true)
data class GetBenRequest(
    val AshaId: String,
    val pageNo: Int,
    val fromDate: String,
    val toDate: String
)

@JsonClass(generateAdapter = true)
data class BenResponse(
    val benId: String,
    val benRegId: Long,
    val abhaDetails: List<BenAbhaResponse>?,
    val toDate: String
)

@JsonClass(generateAdapter = true)
data class BenAbhaResponse(
    val BeneficiaryRegID: Long,
    val HealthID: String,
    val HealthIDNumber: String,
    val AuthenticationMode: String?,
    val CreatedDate: String?
)
///////////////-------------Abha id-------------/////////////////

@JsonClass(generateAdapter = true)
data class AbhaTokenRequest(
    val clientId: String = AbhaClientConstants.clientId,
    val clientSecret: String = AbhaClientConstants.clientSecret,
    val grantType: String = AbhaClientConstants.grantType
)

@JsonClass(generateAdapter = true)
data class AbhaTokenResponse(
    val accessToken: String,
    val expiresIn: Int,
    val refreshExpiresIn: Int,
    val refreshToken: String,
    val tokenType: String
)

@JsonClass(generateAdapter = true)
data class AbhaGenerateAadhaarOtpRequest(
    var aadhaar: String
)

@JsonClass(generateAdapter = true)
data class AadhaarVerifyBioRequest(
    var aadhaar: String,
    var bioType: String,
    var pid: String
)

@JsonClass(generateAdapter = true)
data class AbhaGenerateAadhaarOtpResponse(
    val txnId: String
)

@JsonClass(generateAdapter = true)
data class AbhaGenerateAadhaarOtpResponseV2(
    val txnId: String,
    val mobileNumber: String
)

@JsonClass(generateAdapter = true)
data class AbhaResendAadhaarOtpRequest(
    val txnId: String
)


@JsonClass(generateAdapter = true)
data class AbhaVerifyAadhaarOtpRequest(
    val otp: String,
    val txnId: String
)


@JsonClass(generateAdapter = true)
data class AbhaVerifyAadhaarOtpResponse(
    val txnId: String
)


@JsonClass(generateAdapter = true)
data class AbhaGenerateMobileOtpRequest(
    val mobile: String,
    val txnId: String
)

@JsonClass(generateAdapter = true)
data class AbhaGenerateMobileOtpResponse(
    val txnId: String
)

data class AbhaCheckAndGenerateMobileOtpResponse(
    val mobileLinked: Boolean,
    val txnId: String
)


@JsonClass(generateAdapter = true)
data class AbhaVerifyMobileOtpRequest(
    val otp: String,
    val txnId: String
)


@JsonClass(generateAdapter = true)
data class AbhaVerifyMobileOtpResponse(
    val txnId: String
)

@JsonClass(generateAdapter = true)
data class StateCodeResponse(
    val code: String,
    val name: String,
    val districts: List<DistrictCodeResponse>?
)

@JsonClass(generateAdapter = true)
data class DistrictCodeResponse(
    val code: String,
    val name: String
)

@JsonClass(generateAdapter = true)
data class CreateAbhaIdGovRequest(

    val aadharNumber: Long,
    val benefitName: String,
    val consentHealthId: Boolean ,
    val dateOfBirth: String,
    val gender: String,
    val name: String,
    val stateCode: Int,
    val districtCode: Int
)

@JsonClass(generateAdapter = true)
data class CreateAbhaIdRequest(

    val email: String?,
    val firstName: String?,
    val healthId: String?,
    val lastName: String?,
    val middleName: String?,
    val password: String?,
    val profilePhoto: String?,
    val txnId: String
)

@JsonClass(generateAdapter = true)
data class CreateHIDResponse(
    val hID: Long,
    val healthIdNumber: String?,
    val name: String?,
    val gender: String?,
    val yearOfBirth: String?,
    val monthOfBirth: String?,
    val dayOfBirth: String?,
    val firstName: String?,
    val healthId: String?,
    val lastName: String?,
    val middleName: String?,
    val stateCode: String?,
    val districtCode: String?,
    val stateName: String?,
    val districtName: String?,
    val email: String?,
    val kycPhoto: String?,
    val mobile: String?,
    val authMethod: String?,
    val authMethods: Array<String>?,
    val deleted: Boolean,
    val processed: String?,
    val createdBy: String?,
    val txnId: String?,
)
@JsonClass(generateAdapter = true)
data class CreateAbhaIdResponse(

    val token: String,
    val refreshToken: String,
    val healthIdNumber: String,
    val name: String,
    val gender: String,
    val yearOfBirth: String,
    val monthOfBirth: String,
    val dayOfBirth: String,
    val firstName: String,
    val healthId: String?,
    val lastName: String,
    val middleName: String,
    val stateCode: String,
    val districtCode: String,
    val stateName: String,
    val districtName: String,
    val email: String?,
    val kycPhoto: String?,
    val profilePhoto: String,
    val mobile: String,
    val authMethods: Array<String>,
    val pincode: String?,
    val tags: Map<String, String>?,
    val alreadyExists: String,
    val new: Boolean,
    var txnId: String
)
@JsonClass(generateAdapter = true)
data class GenerateOtpHid(
    val authMethod: String?,
    val healthId: String?,
    val healthIdNumber: String?
)

@JsonClass(generateAdapter = true)
data class ValidateOtpHid(
    val otp: String?,
    val txnId: String?,
    val authMethod: String?
)
@JsonClass(generateAdapter = true)
data class CreateHealthIdRequest(
    val otp: String?,
    val txnId: String?,
    val address: String?,
    val dayOfBirth: String?,
    val email: String?,
    val profilePhoto: String?,
    val password: String?,
    val healthId: String?,
    val healthIdNumber: String?,
    val firstName: String?,
    val gender: String?,
    val lastName: String?,
    val middleName: String?,
    val monthOfBirth: String?,
    val name: String?,
    val pincode: Int?,
    val yearOfBirth: String?,
    val providerServiceMapID: Int?,
    val createdBy: String?
)
@JsonClass(generateAdapter = true)
data class GetBenHealthIdRequest(
    val beneficiaryRegID: Long?,
    val beneficiaryID: Long?,
)
@JsonClass(generateAdapter = true)
data class BenHealthDetails(
    val benHealthID: Int,
    val healthIdNumber: String,
    val beneficiaryRegID: Long,
    val healthId: String
)
data class MapHIDtoBeneficiary(
    val beneficiaryRegID: Long?,
    val beneficiaryID: Long?,
    val healthId: String?,
    val healthIdNumber: String?,
    var providerServiceMapId: Int?,
    var createdBy: String?
)

//  For getting VanSpDetails
@JsonClass(generateAdapter = true)
data class VanSpDetailsResponse(
    val data: VanSpDetailsData?,
    val statusCode: Int,
    val errorMessage: String,
    val status: String
)

@JsonClass(generateAdapter = true)
data class VanSpDetailsData(
    val userVanSpDetails: List<UserVanSpDetails>?
)
@JsonClass(generateAdapter = true)
data class VillageLocationMaster(
    val villageLocationData: List<VillageLocationData>?
)


@JsonClass(generateAdapter = true)
data class UserVanSpDetails(
    val ID: Int,
    val userID: Int,
    val vanID: Int,
    val vanNoAndType: String,
    val vanSession: Int,
    val servicePointID: Int,
    val servicePointName: String,
    val parkingPlaceID: Int,
    val facilityID: Int
)

@JsonClass(generateAdapter = true)
data class StateResponseData(
    val data: StateList,
    val statusCode: Int,
    val errorMessage: String,
    val status: String
)

@JsonClass(generateAdapter = true)
data class StateList(
    val stateMaster: List<State>
) : NetworkResponse()


@JsonClass(generateAdapter = true)
data class BenificiaryResponse(
    val beneficiaryID: String,
    val beneficiaryRegID: String
) : NetworkResponse()

@JsonClass(generateAdapter = true)
data class BenificiarySaveResponse(
    val beneficiaryID: Long,
    val beneficiaryRegID: Long
) : NetworkResponse()

@JsonClass(generateAdapter = true)
data class NurseDataResponse(
    val response: String?,
    val visitCode: String?,
    val visitID: Long?,
) : NetworkResponse()

@JsonClass(generateAdapter = true)
data class State(
    val stateID: Int,
    val govtLGDStateID: Int,
    val stateName: String
)

@JsonClass(generateAdapter = true)
data class DistrictResponse(
    val data: List<District>,
    val statusCode: Int,
    val errorMessage: String,
    val status: String
)

@JsonClass(generateAdapter = true)
data class DistrictList(
    val districtMaster: List<District>
) : NetworkResponse()

@JsonClass(generateAdapter = true)
data class District(
    val districtID: Int,
    val govtLGDDistrictID: Int,
    val districtName: String
)

@JsonClass(generateAdapter = true)
data class DistrictBlockResponse(
    val data: List<DistrictBlock>,
    val statusCode: Int,
    val errorMessage: String,
    val status: String
)

@JsonClass(generateAdapter = true)
data class BlockList(
    val blockMaster: List<DistrictBlock>
) : NetworkResponse()

@JsonClass(generateAdapter = true)
data class DistrictBlock(
    val blockID: Int,
    val govLGDSubDistrictID: Int,
    val blockName: String,
    val outputMapper: Map<String, Any> // You can adjust the type of `outputMapper` as per its structure
)

@JsonClass(generateAdapter = true)
data class VillageMasterResponse(
    val data: List<Village>,
    val statusCode: Int,
    val errorMessage: String,
    val status: String
)

@JsonClass(generateAdapter = true)
data class VillageList(
    val villageMaster: List<Village>
) : NetworkResponse()

@JsonClass(generateAdapter = true)
data class Village(
    val districtBranchID: Int,
    val govtLGDVillageID: Int,
    val villageName: String,
    val outputMapper: Map<String, Any> // You can adjust the type of `outputMapper` as per its structure
)

@JsonClass(generateAdapter = true)
data class VillageIdList(
    val villageID: List<Int>,
    val lastSyncDate: String,
)

@JsonClass(generateAdapter = true)
data class BenflowResponse(
    val benflowList: MutableList<BenFlow>,
) : NetworkResponse()

@JsonClass(generateAdapter = true)
data class NurseDataRequest(
    val benRegID: Long,
    val visitCode: Long,
)

@JsonClass(generateAdapter = true)
data class LabProceduresDataRequest(
    val beneficiaryRegID: Long,
    val benVisitID: Long,
    val visitCode: Long,
)

fun getLongFromDate(dateString: String?): Long {
    val f = SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.ENGLISH)
    val date = dateString?.let { f.parse(it) }
    return date?.time ?: 0L
}

