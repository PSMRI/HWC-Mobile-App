package org.piramalswasthya.cho.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InvestigationUpsync(
    val externalInvestigations: String?,
    val vanID: Int?,
    val parkingPlaceID: Int?,
    val beneficiaryRegID: String?,
    val benVisitID: String?,
    val visitCode: String?,
    val providerServiceMapID: String?,
    val createdBy: String?,
    val isSpecialist: Boolean?,
    val laboratoryList: List<LaboratoryUpsync>,
//    "externalInvestigations": "",
//    "vanID": 168,
//    "parkingPlaceID": 83,
//    "beneficiaryRegID": "33459",
//    "benVisitID": "5430",
//    "visitCode": "30016800005430",
//    "providerServiceMapID": "265",
//    "createdBy": "Sanjay",
//    "isSpecialist": false,
){
    constructor(user: UserDomain?, benFlow: BenFlow?, investigation: InvestigationCaseRecordWithHigherHealthCenter?, procedureList: List<ProceduresMasterData>) : this(
        investigation?.investigationCaseRecord?.externalInvestigations,
        user?.vanId,
        user?.parkingPlaceId,
        benFlow?.beneficiaryRegID.toString(),
        benFlow?.benVisitID.toString(),
        benFlow?.visitCode.toString(),
        user?.serviceMapId.toString(),
        user?.userName,
        false,
        procedureList.map {
            LaboratoryUpsync(user, it)
        }
    )
}

@JsonClass(generateAdapter = true)
data class LaboratoryUpsync(
    val procedureID: Int?,
    val procedureName: String?,
    val procedureDesc: String?,
    val procedureType: String?,
    val gender: String?,
    val providerServiceMapID: Int?
){
    constructor(user: UserDomain?, proceduresMasterData: ProceduresMasterData) : this(
        proceduresMasterData.procedureID,
        proceduresMasterData.procedureName,
        proceduresMasterData.procedureDesc,
        proceduresMasterData.procedureType,
        proceduresMasterData.gender,
        user?.serviceMapId
    )
}

//"investigation": {
//    "externalInvestigations": "",
//    "vanID": 168,
//    "parkingPlaceID": 83,
//    "beneficiaryRegID": "33459",
//    "benVisitID": "5430",
//    "visitCode": "30016800005430",
//    "providerServiceMapID": "265",
//    "createdBy": "Sanjay",
//    "isSpecialist": false,
//    "laboratoryList": [
//    {
//        "procedureID": 159,
//        "procedureName": "Haemoglobin (Hb)",
//        "procedureDesc": "Haemoglobin (Hb)",
//        "procedureType": "Laboratory",
//        "gender": "unisex",
//        "providerServiceMapID": 265
//    },
//    {
//        "procedureID": 158,
//        "procedureName": "Random Blood Sugar (RBS)",
//        "procedureDesc": "Random Blood Sugar (RBS)",
//        "procedureType": "Laboratory",
//        "gender": "unisex",
//        "providerServiceMapID": 265
//    }
//    ]
//},