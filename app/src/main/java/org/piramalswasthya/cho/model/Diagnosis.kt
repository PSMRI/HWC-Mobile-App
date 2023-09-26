package org.piramalswasthya.cho.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Diagnosis(
    val prescriptionID: Int?,
    val vanID: Int?,
    val parkingPlaceID: Int?,
    val provisionalDiagnosisList: List<ProvisionalDiagnosis>,
    val beneficiaryRegID: String?,
    val benVisitID: String?,
    val visitCode: String?,
    val providerServiceMapID: String?,
    val createdBy: String?,
    val isSpecialist: Boolean
) {
    constructor(user: UserDomain?, benFlow: BenFlow?) : this(
        null,
        user?.vanId,
        user?.parkingPlaceId,
        arrayListOf(ProvisionalDiagnosis("Pain",""), ProvisionalDiagnosis("Vomit","")),
        benFlow?.beneficiaryID.toString(),
        benFlow?.benVisitID.toString(),
        benFlow?.visitCode.toString(),
        user?.serviceMapId.toString(),
        user?.userName,
        false
    )
}

@JsonClass(generateAdapter = true)
data class ProvisionalDiagnosis(
    val term: String,
    val conceptID: String
){

}

//"diagnosis": {
//    "prescriptionID": null,
//    "vanID": 168,
//    "parkingPlaceID": 83,
//    "provisionalDiagnosisList": [
//    {
//        "conceptID": "22253000",
//        "term": "Pain"
//    },
//    {
//        "conceptID": "1985008",
//        "term": "Vomit"
//    }
//    ],
//    "beneficiaryRegID": "33459",
//    "benVisitID": "5430",
//    "visitCode": "30016800005430",
//    "providerServiceMapID": "265",
//    "createdBy": "Sanjay",
//    "isSpecialist": false
//},