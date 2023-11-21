package org.piramalswasthya.cho.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VisitDetails(
    val adherence: Adherence?,
    val chiefComplaints: List<ChiefComplaintsNetwork>?,
    val visitDetails: VisitDetailsNetwork?,
){
    constructor(user: UserDomain?, visit: VisitDB?, chiefComplaints: List<ChiefComplaintDB>?, benFlow: BenFlow) : this(
        Adherence(user, benFlow),
        chiefComplaints?.map { it ->
            ChiefComplaintsNetwork(
                user = user,
                chiefComplaint = it,
                benFlow = benFlow
            )
        },
        VisitDetailsNetwork(
            user = user,
            visit = visit,
            benFlow = benFlow
        ),
    )
}

@JsonClass(generateAdapter = true)
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
    constructor(user: UserDomain?, benFlow: BenFlow) : this(
        null,
        benFlow.beneficiaryRegID?.toString(),
        user?.userName,
        null,
        user?.parkingPlaceId,
        null,
        user?.serviceMapId?.toString(),
        null,
        null,
        null,
        user?.vanId
    )
}

@JsonClass(generateAdapter = true)
data class ChiefComplaintsNetwork(
    val benVisitID: String?,
    val beneficiaryRegID: String?,
    val chiefComplaint: String?,
    val chiefComplaintID: Int?,
    val createdBy: String?,
    val description: String?,
    val duration: String?,
    val parkingPlaceID: Int?,
    val providerServiceMapID: String?,
    val unitOfDuration: String?,
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
    constructor(user: UserDomain?, chiefComplaint: ChiefComplaintDB, benFlow: BenFlow) : this(
        null,
        beneficiaryRegID = benFlow.beneficiaryRegID?.toString(),
        chiefComplaint = chiefComplaint.chiefComplaint,
        chiefComplaintID = chiefComplaint.chiefComplaintId,
        createdBy = user?.userName,
        description = chiefComplaint.description,
        duration = chiefComplaint.duration,
        parkingPlaceID = user?.parkingPlaceId,
        providerServiceMapID = user?.serviceMapId?.toString(),
        unitOfDuration = chiefComplaint.durationUnit,
        vanID = user?.vanId,
    )
}

@JsonClass(generateAdapter = true)
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
    constructor(user: UserDomain?, visit: VisitDB?, benFlow: BenFlow) : this(
        null,
        benFlow.beneficiaryRegID?.toString(),
        user?.userName,
        null,
        null,
        null,
        null,
        null,
        null,
        user?.parkingPlaceId,
        null,
        user?.serviceMapId?.toString(),
        null,
        null,
        null,
        visit?.subCategory,
        user?.vanId,
        visit?.category,
        null,
        visit?.reasonForVisit,
    )
}

