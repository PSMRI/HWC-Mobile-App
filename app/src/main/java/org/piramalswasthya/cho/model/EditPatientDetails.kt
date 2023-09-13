package org.piramalswasthya.cho.model

data class EditPatientDetails(
    val benFlowID: String?,
    val beneficiaryID: String?,
    val beneficiaryRegID: String?,
    val createdBy: String?,
    val examinationDetails: ExaminationDetails?,
    val historyDetails: HistoryDetails,
    val parkingPlaceID: Int?,
    val providerServiceMapID: String?,
    val serviceID: String?,
    val sessionID: String?,
    val tcRequest: String?,
    val vanID: Int?,
    val visitDetails: VisitDetails?,
    val vitalDetails: VitalDetails?,
//    benFlowID: "20307"
//    beneficiaryID:"932497226317"
//    beneficiaryRegID:"33140"
//    createdBy:"Pranathi"
//    examinationDetails:{,…}
//    historyDetails:{pastHistory: {vanID: 61, parkingPlaceID: 10,…}, comorbidConditions: {vanID: 61, parkingPlaceID: 10,…},…}
//    parkingPlaceID:10
//    providerServiceMapID:"13"
//    serviceID:"4"
//    sessionID:"3"
//    tcRequest:null
//    vanID:61
//    visitDetails:{visitDetails: {beneficiaryRegID: "33140", providerServiceMapID: "13", visitNo: null,…},…}
//    vitalDetails:{beneficiaryRegID: "33140", benVisitID: null, providerServiceMapID: "13", weight_Kg: "100",…}
){
    constructor(user: UserDomain?): this(
        "20307",
        "932497226317",
        "33140",
        user?.userName,
        ExaminationDetails(user),
        HistoryDetails(user),
        user?.parkingPlaceId,
        user?.serviceMapId.toString(),
        user?.serviceId.toString(),
        "3",
        null,
        user?.vanId,
        VisitDetails(user),
        VitalDetails(user)
    )
}