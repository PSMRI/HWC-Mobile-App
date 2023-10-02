package org.piramalswasthya.cho.model

data class Refer(
    val referredToInstituteID: Int?,
    val refrredToAdditionalServiceList: List<String>?,
    val referredToInstituteName: String?,
    val otherReferredToInstituteName: String?,
    val referralReason: String?,
    val referralReasonList: List<String>?,
    val otherReferralReason: String?,
    val revisitDate: String?,
    val vanID: Int?,
    val parkingPlaceID: Int?,
    val beneficiaryRegID: String?,
    val benVisitID: String?,
    val visitCode: String?,
    val providerServiceMapID: String?,
    val createdBy: String?,
    val isSpecialist: Boolean?
//    "referredToInstituteID": null,
//    "refrredToAdditionalServiceList": [
//        "HWC"
//    ],
//    "referredToInstituteName": null,
//    "otherReferredToInstituteName": null,
//    "referralReason": "Check",
//    "referralReasonList": null,
//    "otherReferralReason": null,
//    "revisitDate": null,
//    "vanID": 168,
//    "parkingPlaceID": 83,
//    "beneficiaryRegID": "33459",
//    "benVisitID": "5430",
//    "visitCode": "30016800005430",
//    "providerServiceMapID": "265",
//    "createdBy": "Sanjay",
//    "isSpecialist": false
){
    constructor(user: UserDomain?, benFlow: BenFlow?,institutionID:Int, institutionName:String) : this(
        null,
        arrayListOf("HWC"),
        null,
        null,
        "Check",
        null,
        null,
        null,
        user?.vanId,
        user?.parkingPlaceId,
        benFlow?.beneficiaryRegID.toString(),
        benFlow?.benVisitID.toString(),
        benFlow?.visitCode.toString(),
        user?.serviceId.toString(),
        user?.userName,
        false
    )
}