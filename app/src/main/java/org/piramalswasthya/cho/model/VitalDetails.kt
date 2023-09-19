package org.piramalswasthya.cho.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VitalDetails(
    val bMI: Double?,
    val benVisitID: String?,
    val beneficiaryRegID: String?,
    val bloodGlucose_2hr_PP: String?,
    val bloodGlucose_Fasting: String?,
    val bloodGlucose_Random: String?,
    val coughAtNightChecked: String?,
    val createdBy: String?,
    val diastolicBP_1stReading: String?,
    val frequentCoughChecked: String?,
    val headCircumference_cm: String?,
    val height_cm: String?,
    val hemoglobin: String?,
    val hipCircumference_cm: String?,
    val midUpperArmCircumference_MUAC_cm: String?,
    val painInChestChecked: String?,
    val parkingPlaceID: Int?,
    val providerServiceMapID: String?,
    val pulseRate: String?,
    val rbsCheckBox: Boolean?,
    val rbsTestRemarks: String?,
    val rbsTestResult: String?,
    val respiratoryRate: String?,
    val sPO2: String?,
    val shortnessOfBreathChecked: String?,
    val sputumChecked: String?,
    val systolicBP_1stReading: String?,
    val temperature: String?,
    val vanID: Int?,
    val waistCircumference_cm: String?,
    val waistHipRatio: String?,
    val weight_Kg: String?,
    val wheezingChecked: String?,
//    bMI: 30.9
//    benVisitID: null
//    beneficiaryRegID: "33140"
//    bloodGlucose_2hr_PP:null
//    bloodGlucose_Fasting:null
//    bloodGlucose_Random:null
//    coughAtNightChecked:null
//    createdBy:"Pranathi"
//    diastolicBP_1stReading:null
//    frequentCoughChecked:null
//    headCircumference_cm:null
//    height_cm:"180"
//    hemoglobin:null
//    hipCircumference_cm:null
//    midUpperArmCircumference_MUAC_cm:null
//    painInChestChecked:null
//    parkingPlaceID:10
//    providerServiceMapID:"13"
//    pulseRate:null
//    rbsCheckBox:true
//    rbsTestRemarks:null
//    rbsTestResult:null
//    respiratoryRate:null
//    sPO2:null
//    shortnessOfBreathChecked:null
//    sputumChecked:null
//    systolicBP_1stReading:null
//    temperature:null
//    vanID:61
//    waistCircumference_cm:null
//    waistHipRatio:null
//    weight_Kg:"100"
//    wheezingChecked:null
){
    constructor(user: UserDomain?, vitals: PatientVitalsModel?, benFlow: BenFlow): this(
        vitals?.bmi?.toDouble(),
        null,
        benFlow.beneficiaryRegID.toString(),
        null,
        null,
        null,
        null,
        user?.userName,
        null,
        null,
        null,
        vitals?.height,
        null,
        null,
        null,
        null,
        user?.parkingPlaceId,
        user?.serviceMapId.toString(),
        null,
        true,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        user?.vanId,
        null,
        null,
        vitals?.weight,
        null
    )
//    constructor(user: UserDomain?, vitals: PatientVitalsModel?, benFlow: BenFlow): this(
//        vitals?.bmi?.toDouble(),
//        null,
//        benFlow.beneficiaryRegID.toString(),
//        null,
//        null,
//        null,
//        null,
//        user?.userName,
//        vitals?.bpDiastolic,
//        null,
//        null,
//        vitals?.height,
//        null,
//        null,
//        null,
//        null,
//        user?.parkingPlaceId,
//        user?.serviceMapId.toString(),
//        vitals?.pulseRate,
//        true,
//        null,
//        vitals?.rbs,
//        vitals?.respiratoryRate,
//        vitals?.spo2,
//        null,
//        null,
//        vitals?.bpSystolic,
//        vitals?.temperature,
//        user?.vanId,
//        vitals?.waistCircumference,
//        null,
//        vitals?.weight,
//        null
//    )
}