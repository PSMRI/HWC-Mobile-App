package org.piramalswasthya.cho.model

data class VisitDetails(
    val adherence: Adherence,
    val chiefComplaints: List<ChiefComplaints>,
    val visitDetails: VisitDetailsNetwork,
){
    constructor(user: UserDomain?) : this(
        Adherence(user),
        arrayListOf(ChiefComplaints(user)),
        VisitDetailsNetwork(user),
    )
}


data class Adherence(
    val benVisitID: String?,
    val beneficiaryRegID: String?,
    val createdBy: String?,
    val drugReason: String?,
    val parkingPlaceID: Int?,
    val progress: String?,
    val providerServiceMapID: String?,
    val referralReason: String?,
    val toDrugs: String?,
    val toReferral: String?,
    val vanID: Int?,
//    benVisitID: null
//    beneficiaryRegID: "33140"
//    createdBy:"Pranathi"
//    drugReason:null
//    parkingPlaceID:10
//    progress:null
//    providerServiceMapID:"13"
//    referralReason:null
//    toDrugs:null
//    toReferral:null
//    vanID:61
){
    constructor(user: UserDomain?) : this(
        null,
        "33140",
        user?.userName,
        null,
        user?.parkingPlaceId,
        null,
        user?.serviceMapId.toString(),
        null,
        null,
        null,
        user?.vanId
    )
}

data class ChiefComplaints(
    val benVisitID: String?,
    val beneficiaryRegID: String?,
    val chiefComplaint: String?,
    val chiefComplaintID: String?,
    val createdBy: String?,
    val parkingPlaceID: Int?,
    val providerServiceMapID: String?,
    val vanID: Int?,
//    benVisitID: null
//    beneficiaryRegID: "33140"
//    chiefComplaint:null
//    chiefComplaintID:null
//    createdBy:"Pranathi"
//    parkingPlaceID:null
//    providerServiceMapID:"13"
//    vanID:null
){
    constructor(user: UserDomain?) : this(
        null,
        "33140",
        null,
        null,
        user?.userName,
        user?.parkingPlaceId,
        user?.serviceMapId.toString(),
        user?.vanId,
    )
}

data class VisitDetailsNetwork(
    val IdrsOrCbac: String?,
    val beneficiaryRegID: String?,
    val createdBy: String?,
    val fileIDs: String?,
    val followUpForFpMethod: String?,
    val healthFacilityLocation: String?,
    val healthFacilityType: String?,
    val otherFollowUpForFpMethod: String?,
    val otherSideEffects: String?,
    val parkingPlaceID: Int?,
    val pregnancyStatus: String?,
    val providerServiceMapID: String?,
    val rCHID: String?,
    val reportFilePath: String?,
    val sideEffects: String?,
    val subVisitCategory: String?,
    val vanID: Int?,
    val visitCategory: String?,
    val visitNo: String?,
    val visitReason: String?,
//    IdrsOrCbac: null
//    beneficiaryRegID: "33140"
//    createdBy:"Pranathi"
//    fileIDs:null
//    followUpForFpMethod:null
//    healthFacilityLocation:null
//    healthFacilityType:null
//    otherFollowUpForFpMethod:null
//    otherSideEffects:null
//    parkingPlaceID:10
//    pregnancyStatus:null
//    providerServiceMapID:"13"
//    rCHID:null
//    reportFilePath:null
//    sideEffects:null
//    subVisitCategory:"Basic Oral Health Care Services"
//    vanID:61
//    visitCategory:"General OPD"
//    visitNo:null
//    visitReason:"New Chief Complaint"
){
    constructor(user: UserDomain?) : this(
        null,
        "33140",
        user?.userName,
        null,
        null,
        null,
        null,
        null,
        null,
        user?.parkingPlaceId,
        null,
        user?.serviceMapId.toString(),
        null,
        null,
        null,
        "Basic Oral Health Care Services",
        user?.vanId,
        "General OPD",
        null,
        "New Chief Complaint",
    )
}

