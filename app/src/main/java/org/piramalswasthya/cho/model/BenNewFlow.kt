package org.piramalswasthya.cho.model

import org.piramalswasthya.cho.utils.DateTimeUtil
import java.sql.Timestamp
import java.util.Locale


data class BenNewFlow (

    val beneficiaryRegID: Long?,
    val i_bendemographics: IBenDemographics?,
    val benPhoneMaps: List<BenPhoneMap>?,
    val beneficiaryID: String?,
    val m_title: Map<String, Any>? = emptyMap(),
    val firstName: String?,
    val lastName: String?,
    val genderID: Int?,
    val m_gender: MGender?,
    val maritalStatus: Map<String, Any>? = emptyMap(),
    val dOB: String?,
    val age: Int?,
    val fatherName: String?,
    val isHIVPos: String?,
    val changeInSelfDetails: Boolean?,
    val changeInAddress: Boolean?,
    val changeInContacts: Boolean?,
    val changeInIdentities: Boolean?,
    val changeInOtherDetails: Boolean?,
    val changeInFamilyDetails: Boolean?,
    val changeInAssociations: Boolean?,
    val is1097: Boolean?,
    val createdBy: String?,
    val createdDate: String?,
    val changeInBankDetails: Boolean?,
    val beneficiaryIdentities: List<Any>? = emptyList(),
    val changeInBenImage: Boolean?,
    val actualAge: Int?,
    val ageUnits: String?,
    val emergencyRegistration: Boolean?,
    val providerServiceMapId: String?,
    val vanID: Int?
){
    constructor(user: UserDomain?, patientDisplay: PatientDisplay?) : this(
        patientDisplay?.patient?.beneficiaryRegID,
        i_bendemographics = IBenDemographics(user, patientDisplay),
        benPhoneMaps = arrayListOf(BenPhoneMap(patientDisplay?.patient)),
        beneficiaryID = patientDisplay?.patient?.beneficiaryID?.toString(),
        m_title = emptyMap(),
        firstName = patientDisplay?.patient?.firstName,
        lastName = patientDisplay?.patient?.lastName,
        genderID = patientDisplay?.patient?.genderID,
        m_gender = MGender(genderID = patientDisplay?.gender?.genderID, genderName = patientDisplay?.gender?.genderName),
        maritalStatus = emptyMap(),
        dOB = DateTimeUtil.formatDateToUTC(patientDisplay?.patient?.dob!!),
        0,
        "",
        "",
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        createdBy = user?.userName,
        createdDate = DateTimeUtil.formatDateToUTC(patientDisplay?.patient?.registrationDate!!),
        false,
        beneficiaryIdentities = emptyList(),
        false,
        actualAge = patientDisplay?.patient?.age,
        ageUnits = patientDisplay?.ageUnit?.name?.lowercase(),
        false,
        user?.serviceMapId?.toString(),
        user?.vanId
    )

//    val beneficiaryRegID: Long?,
//    val i_bendemographics: IBenDemographics?,
//    val benPhoneMaps: List<BenPhoneMap>?,
//    val beneficiaryID: String?,
//    val m_title: Map<String, Any>? = emptyMap(),
//    val firstName: String?,
//    val lastName: String?,
//    val genderID: Int?,
//    val maritalStatus: Map<String, Any>? = emptyMap(),
//    val dOB: String?,
//    val age: Int?,
//    val fatherName: String?,
//    val isHIVPos: String?,
//    val changeInSelfDetails: Boolean?,
//    val changeInAddress: Boolean?,
//    val changeInContacts: Boolean?,
//    val changeInIdentities: Boolean?,
//    val changeInOtherDetails: Boolean?,
//    val changeInFamilyDetails: Boolean?,
//    val changeInAssociations: Boolean?,
//    val is1097: Boolean?,
//    val createdBy: String?,
//    val createdDate: String?,
//    val changeInBankDetails: Boolean?,
//    val beneficiaryIdentities: List<Any>? = emptyList(),
//    val changeInBenImage: Boolean?,
//    val actualAge: Int?,
//    val ageUnits: String?,
//    val emergencyRegistration: Boolean?,
//    val providerServiceMapId: String?,
//    val vanID: Int?
}

data class IBenDemographics(
    val beneficiaryRegID: Long?,
    val stateID: Int?,
    val stateName: String?,
    val m_state: MState?,
    val districtID: Int?,
    val districtName: String?,
    val m_district: MDistrict?,
    val blockID: Int?,
    val blockName: String?,
    val m_districtblock: MDistrictblock?,
    val districtBranchID: Int?,
    val districtBranchName: String?,
    val m_districtbranchmapping: MDistrictbranchmapping?,
    val createdBy: String?,
    val parkingPlaceID: Int?,
    val servicePointID: Int?,
    val servicePointName: String?
){
    constructor(user: UserDomain?, patientDisplay: PatientDisplay?): this(
        patientDisplay?.patient?.beneficiaryRegID,
        stateID = patientDisplay?.state?.stateID,
        stateName = patientDisplay?.state?.stateName,
        m_state = MState(
            stateID = patientDisplay?.state?.stateID,
            stateName = patientDisplay?.state?.stateName,
            stateCode = "BH",
            countryID = 1
        ),
        districtID = patientDisplay?.district?.districtID,
        districtName = patientDisplay?.district?.districtName,
        m_district = MDistrict(
            districtID = patientDisplay?.district?.districtID,
            stateID = patientDisplay?.state?.stateID,
            districtName = patientDisplay?.district?.districtName,
        ),
        blockID = patientDisplay?.block?.blockID,
        blockName = patientDisplay?.block?.blockName,
        m_districtblock = MDistrictblock(
            blockID = patientDisplay?.block?.blockID,
            districtID = patientDisplay?.district?.districtID,
            blockName = patientDisplay?.block?.blockName,
            stateID = patientDisplay?.state?.stateID,
        ),
        districtBranchID = patientDisplay?.village?.districtBranchID,
        districtBranchName = patientDisplay?.village?.villageName,
        m_districtbranchmapping = MDistrictbranchmapping(
            districtBranchID = patientDisplay?.village?.districtBranchID,
            blockID = patientDisplay?.block?.blockID,
            villageName = patientDisplay?.village?.villageName,
        ),
        user?.userName,
        user?.parkingPlaceId,
        user?.servicePointId,
        user?.servicePointName
    )
}

data class MState(
    val stateID: Int?,
    val stateName: String?,
    val stateCode: String?,
    val countryID: Int?
)

data class MDistrict(
    val districtID: Int?,
    val stateID: Int?,
    val districtName: String?,
)

data class MDistrictblock(
    val blockID: Int?,
    val districtID: Int?,
    val blockName: String?,
    val stateID: Int?,
)

data class MDistrictbranchmapping(
    val districtBranchID: Int?,
    val blockID: Int?,
    val villageName: String?
)

data class MGender(
    val genderID: Int?,
    val genderName: String?
)

data class BenPhoneMap(
    val benPhMapID: Int?,
    val benificiaryRegID: Long?,
    val parentBenRegID: Int?,
    val benRelationshipID: Int?,
    val benRelationshipType: BenRelationshipType?,
    val phoneNo: String?
){
    constructor(patient: Patient?) : this(
        20738,
        patient?.beneficiaryRegID,
        877,
        11,
        BenRelationshipType(11, "Other"),
        patient?.phoneNo
    )
}

data class BenRelationshipType(
    val benRelationshipID: Int,
    val benRelationshipType: String
)