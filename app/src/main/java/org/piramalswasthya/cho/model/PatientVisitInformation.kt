package org.piramalswasthya.cho.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PatientVisitInformation(
    val benFlowID: String?,
    val beneficiaryID: String?,
    val beneficiaryRegID: String?,
    val createdBy: String?,
    val examinationDetails: ExaminationDetails?,
    val historyDetails: HistoryDetails?,
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
    constructor(user: UserDomain?, visit: VisitDB?, chiefComplaints: List<ChiefComplaintDB>?, vitals: PatientVitalsModel?, benFlow: BenFlow): this(
        benFlow.benFlowID.toString(),
        benFlow.beneficiaryID.toString(),
        benFlow.beneficiaryRegID.toString(),
        user?.userName,
        ExaminationDetails(
            user = user,
            benFlow = benFlow
        ),
        HistoryDetails(
            user = user,
            benFlow = benFlow
        ),
        user?.parkingPlaceId,
        user?.serviceMapId.toString(),
        user?.serviceId.toString(),
        "3",
        null,
        user?.vanId,
        VisitDetails(
            user = user,
            visit = visit,
            chiefComplaints = chiefComplaints,
            benFlow = benFlow
        ),
        VitalDetails(
            user = user,
            vitals = vitals,
            benFlow = benFlow
        )
    )
}




@JsonClass(generateAdapter = true)
data class PatientDoctorForm(
    val subVisitCategory: String?,
    val diagnosis: Diagnosis?,
    val investigation: Investigation?,
    val prescription: List<Prescription>?,
    val counsellingProvidedList: List<String>?,
    val refer: Refer?,
    val benFlowID: String?,
    val beneficiaryID: String?,
    val doctorFlag: String?,
    val nurseFlag: String?,
    val pharmacist_flag: String?,
    val sessionID: String?,
    val parkingPlaceID: Int?,
    val vanID: Int?,
    val beneficiaryRegID: String?,
    val providerServiceMapID: String?,
    val visitCode: String?,
    val benVisitID: String?,
    val serviceID: String?,
    val createdBy: String?,
    val isSpecialist: Boolean?,
){
    constructor(user: UserDomain?, benFlow: BenFlow?, diagnosis: List<Diagnosis>?, investigation: Investigation?, prescription: List<Prescription>?, refer: Refer? ): this(
        "Management of Communicable Diseases including National Health Programs",
        diagnosis,
        investigation,
        prescription,
        null,
        refer,
        benFlow?.benFlowID.toString(),
        benFlow?.beneficiaryID.toString(),
        benFlow?.doctorFlag.toString(),
        benFlow?.nurseFlag.toString(),
        benFlow?.pharmacist_flag.toString(),
        "3",
        user?.parkingPlaceId,
        user?.vanId,
        benFlow?.beneficiaryRegID.toString(),
        user?.serviceMapId.toString(),
        benFlow?.visitCode.toString(),
        benFlow?.benVisitID.toString(),
        user?.serviceId.toString(),
        user?.userName,
        false,
    )
}



//{
//    "subVisitCategory": "Management of Communicable Diseases including National Health Programs",
//    "findings": {
//        "beneficiaryRegID": "33459",
//        "benVisitID": "5430",
//        "providerServiceMapID": "265",
//        "otherSymptoms": null,
//        "significantFindings": null,
//        "isForHistory": null,
//        "complaints": [],
//        "createdBy": "Sanjay",
//        "vanID": 168,
//        "parkingPlaceID": 83,
//        "clinicalObservationsList": [
//        {
//            "conceptID": null,
//            "term": null,
//            "clinicalObservationsProvided": null
//        }
//        ],
//        "significantFindingsList": [
//        {
//            "conceptID": null,
//            "term": null,
//            "significantFindingsProvided": null
//        }
//        ],
//        "visitCode": "30016800005430",
//        "isSpecialist": false
//    },
//    "diagnosis": {
//        "prescriptionID": null,
//        "vanID": 168,
//        "parkingPlaceID": 83,
//        "provisionalDiagnosisList": [
//        {
//            "conceptID": "22253000",
//            "term": "Pain"
//        },
//        {
//            "conceptID": "1985008",
//            "term": "Vomit"
//        }
//        ],
//        "beneficiaryRegID": "33459",
//        "benVisitID": "5430",
//        "visitCode": "30016800005430",
//        "providerServiceMapID": "265",
//        "createdBy": "Sanjay",
//        "isSpecialist": false
//    },
//    "investigation": {
//        "externalInvestigations": "",
//        "vanID": 168,
//        "parkingPlaceID": 83,
//        "beneficiaryRegID": "33459",
//        "benVisitID": "5430",
//        "visitCode": "30016800005430",
//        "providerServiceMapID": "265",
//        "createdBy": "Sanjay",
//        "isSpecialist": false,
//        "laboratoryList": [
//        {
//            "procedureID": 159,
//            "procedureName": "Haemoglobin (Hb)",
//            "procedureDesc": "Haemoglobin (Hb)",
//            "procedureType": "Laboratory",
//            "gender": "unisex",
//            "providerServiceMapID": 265
//        },
//        {
//            "procedureID": 158,
//            "procedureName": "Random Blood Sugar (RBS)",
//            "procedureDesc": "Random Blood Sugar (RBS)",
//            "procedureType": "Laboratory",
//            "gender": "unisex",
//            "providerServiceMapID": 265
//        }
//        ]
//    },
//    "prescription": [
//        {
//            "id": null,
//            "drugID": 146,
//            "drugName": "Paracetamol",
//            "drugStrength": "125ml",
//            "formName": "Syrup",
//            "formID": 3,
//            "dose": "10 ml",
//            "qtyPrescribed": 1,
//            "frequency": "Once Daily(OD)",
//            "duration": 3,
//            "route": "Oral",
//            "durationView": "3 Day(s)",
//            "unit": "Day(s)",
//            "instructions": null,
//            "sctCode": null,
//            "sctTerm": null,
//            "createdBy": "Sanjay",
//            "vanID": 168,
//            "parkingPlaceID": 83,
//            "isEDL": true
//        },
//        {
//            "id": null,
//            "drugID": 145,
//            "drugName": "Paracetamol",
//            "drugStrength": "500mg",
//            "formName": "Tablet",
//            "formID": 1,
//            "dose": "Half Tab",
//            "qtyPrescribed": null,
//            "frequency": "Four Times in a Day (QID)",
//            "duration": 2,
//            "route": "Oral",
//            "durationView": "2 Day(s)",
//            "unit": "Day(s)",
//            "instructions": "After food",
//            "sctCode": null,
//            "sctTerm": null,
//            "createdBy": "Sanjay",
//            "vanID": 168,
//            "parkingPlaceID": 83,
//            "isEDL": true
//        }
//    ],
//    "counsellingProvidedList": null,
//    "refer": {
//        "referredToInstituteID": null,
//        "refrredToAdditionalServiceList": [
//            "HWC"
//        ],
//        "referredToInstituteName": null,
//        "otherReferredToInstituteName": null,
//        "referralReason": "Check",
//        "referralReasonList": null,
//        "otherReferralReason": null,
//        "revisitDate": null,
//        "vanID": 168,
//        "parkingPlaceID": 83,
//        "beneficiaryRegID": "33459",
//        "benVisitID": "5430",
//        "visitCode": "30016800005430",
//        "providerServiceMapID": "265",
//        "createdBy": "Sanjay",
//        "isSpecialist": false
//    },
//    "benFlowID": "20623",
//    "beneficiaryID": "273914628799",
//    "doctorFlag": "1",
//    "nurseFlag": "9",
//    "pharmacist_flag": "0",
//    "sessionID": "3",
//    "parkingPlaceID": 83,
//    "vanID": 168,
//    "beneficiaryRegID": "33459",
//    "providerServiceMapID": "265",
//    "visitCode": "30016800005430",
//    "benVisitID": "5430",
//    "serviceID": "9",
//    "createdBy": "Sanjay",
//    "isSpecialist": false
//}
