package org.piramalswasthya.cho.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EarDiagnosisNetwork(
    val beneficiaryID: String,
    val beneficiaryRegID: String,
    val assessmentId: Long? = null,
    val patientId: String? = null,
    val benVisitNo: Int? = null,
    val difficultyHearing: Boolean? = null,
    val whisperTestResponse: String? = null,
    val hearingTestOutcome: String? = null,
    val earPain: Boolean? = null,
    val earDischargePresent: Boolean? = null,
    val foreignBodyInEar: String? = null,
    val earConditionType: String? = null,
    val congenitalEarMalformation: Boolean? = null,
    val syncState: Int? = null
)

fun EarDiagnosisAssessment.toNetworkModel(
    beneficiaryID: String,
    beneficiaryRegID: String
): EarDiagnosisNetwork {
    return EarDiagnosisNetwork(
        beneficiaryID = beneficiaryID,
        beneficiaryRegID = beneficiaryRegID,
        assessmentId = assessmentId,
        patientId = patientId,
        benVisitNo = benVisitNo,
        difficultyHearing = difficultyHearing,
        whisperTestResponse = whisperTestResponse,
        hearingTestOutcome = hearingTestOutcome,
        earPain = earPain,
        earDischargePresent = earDischargePresent,
        foreignBodyInEar = foreignBodyInEar,
        earConditionType = earConditionType,
        congenitalEarMalformation = congenitalEarMalformation,
        syncState = syncState
    )
}

fun EarDiagnosisNetwork.toCacheModel(patientID: String): EarDiagnosisAssessment {
    return EarDiagnosisAssessment(
        assessmentId = 0L,
        patientId = patientID,
        benVisitNo = benVisitNo,
        difficultyHearing = difficultyHearing,
        whisperTestResponse = whisperTestResponse,
        hearingTestOutcome = hearingTestOutcome,
        earPain = earPain,
        earDischargePresent = earDischargePresent,
        foreignBodyInEar = foreignBodyInEar,
        earConditionType = earConditionType,
        congenitalEarMalformation = congenitalEarMalformation,
        syncState = syncState ?: 0
    )
}

@JsonClass(generateAdapter = true)
data class OphthalmicNetwork(
    val beneficiaryID: String,
    val beneficiaryRegID: String,
    val visitId: String? = null,
    val patientID: String? = null,
    val benVisitNo: Int? = null,
    val isDiabetic: Boolean? = null,
    val screeningPerformed: Boolean? = null,
    val visualAcuityChartUsed: String? = null,
    val distVARight: String? = null,
    val distVALeft: String? = null,
    val nearVA: String? = null,
    val caseIdConditions: String? = null,
    val cataractSymptoms: Boolean? = null,
    val glaucomaSymptoms: Boolean? = null,
    val diabeticRetinopathySymptoms: Boolean? = null,
    val presbyopiaSymptoms: Boolean? = null,
    val trachomaStatus: String? = null,
    val cornealDiseaseType: String? = null,
    val vitaminADeficiency: Boolean? = null,
    val injuryType: String? = null,
    val foreignBodyRemoval: String? = null,
    val chemicalExposure: Boolean? = null,
    val createdBy: String? = null,
    val createdDate: Long? = null,
    val updatedBy: String? = null,
    val updatedDate: Long? = null,
    val syncState: Int? = null
)

fun OphthalmicVisit.toNetworkModel(
    beneficiaryID: String,
    beneficiaryRegID: String
): OphthalmicNetwork {
    return OphthalmicNetwork(
        beneficiaryID = beneficiaryID,
        beneficiaryRegID = beneficiaryRegID,
        visitId = visitId,
        patientID = patientID,
        benVisitNo = benVisitNo,
        isDiabetic = isDiabetic,
        screeningPerformed = screeningPerformed,
        visualAcuityChartUsed = visualAcuityChartUsed,
        distVARight = distVARight,
        distVALeft = distVALeft,
        nearVA = nearVA,
        caseIdConditions = caseIdConditions,
        cataractSymptoms = cataractSymptoms,
        glaucomaSymptoms = glaucomaSymptoms,
        diabeticRetinopathySymptoms = diabeticRetinopathySymptoms,
        presbyopiaSymptoms = presbyopiaSymptoms,
        trachomaStatus = trachomaStatus,
        cornealDiseaseType = cornealDiseaseType,
        vitaminADeficiency = vitaminADeficiency,
        injuryType = injuryType,
        foreignBodyRemoval = foreignBodyRemoval,
        chemicalExposure = chemicalExposure,
        createdBy = createdBy,
        createdDate = createdDate,
        updatedBy = updatedBy,
        updatedDate = updatedDate,
        syncState = syncState
    )
}

fun OphthalmicNetwork.toCacheModel(patientID: String): OphthalmicVisit {
    return OphthalmicVisit(
        visitId = visitId ?: "",
        patientID = patientID,
        benVisitNo = benVisitNo ?: 0,
        isDiabetic = isDiabetic,
        screeningPerformed = screeningPerformed,
        visualAcuityChartUsed = visualAcuityChartUsed,
        distVARight = distVARight,
        distVALeft = distVALeft,
        nearVA = nearVA,
        caseIdConditions = caseIdConditions,
        cataractSymptoms = cataractSymptoms,
        glaucomaSymptoms = glaucomaSymptoms,
        diabeticRetinopathySymptoms = diabeticRetinopathySymptoms,
        presbyopiaSymptoms = presbyopiaSymptoms,
        trachomaStatus = trachomaStatus,
        cornealDiseaseType = cornealDiseaseType,
        vitaminADeficiency = vitaminADeficiency,
        injuryType = injuryType,
        foreignBodyRemoval = foreignBodyRemoval,
        chemicalExposure = chemicalExposure,
        createdBy = createdBy ?: "",
        createdDate = createdDate ?: 0L,
        updatedBy = updatedBy ?: "",
        updatedDate = updatedDate ?: 0L,
        syncState = syncState ?: 0
    )
}

@JsonClass(generateAdapter = true)
data class OralHealthNetwork(
    val beneficiaryID: String,
    val beneficiaryRegID: String,
    val oralHealthId: Long? = null,
    val patientID: String? = null,
    val benVisitNo: Int? = null,
    val toothDecayPresent: Boolean? = null,
    val toothDecaySymptoms: String? = null,
    val gumDiseasePresent: Boolean? = null,
    val gumDiseaseSymptoms: String? = null,
    val irregularTeethJaws: Boolean? = null,
    val abnormalGrowthUlcer: Boolean? = null,
    val cleftLipPalate: Boolean? = null,
    val dentalFluorosis: Boolean? = null,
    val dentalEmergency: String? = null,
    val createdDate: Long? = null,
    val createdBy: String? = null,
    val syncState: Int? = null
)

fun OralHealth.toNetworkModel(
    beneficiaryID: String,
    beneficiaryRegID: String
): OralHealthNetwork {
    return OralHealthNetwork(
        beneficiaryID = beneficiaryID,
        beneficiaryRegID = beneficiaryRegID,
        oralHealthId = oralHealthId,
        patientID = patientID,
        benVisitNo = benVisitNo,
        toothDecayPresent = toothDecayPresent,
        toothDecaySymptoms = toothDecaySymptoms,
        gumDiseasePresent = gumDiseasePresent,
        gumDiseaseSymptoms = gumDiseaseSymptoms,
        irregularTeethJaws = irregularTeethJaws,
        abnormalGrowthUlcer = abnormalGrowthUlcer,
        cleftLipPalate = cleftLipPalate,
        dentalFluorosis = dentalFluorosis,
        dentalEmergency = dentalEmergency,
        createdDate = createdDate,
        createdBy = createdBy,
        syncState = syncState
    )
}

fun OralHealthNetwork.toCacheModel(patientID: String): OralHealth {
    return OralHealth(
        oralHealthId = 0L,
        patientID = patientID,
        benVisitNo = benVisitNo,
        toothDecayPresent = toothDecayPresent,
        toothDecaySymptoms = toothDecaySymptoms,
        gumDiseasePresent = gumDiseasePresent,
        gumDiseaseSymptoms = gumDiseaseSymptoms,
        irregularTeethJaws = irregularTeethJaws,
        abnormalGrowthUlcer = abnormalGrowthUlcer,
        cleftLipPalate = cleftLipPalate,
        dentalFluorosis = dentalFluorosis,
        dentalEmergency = dentalEmergency,
        createdDate = createdDate,
        createdBy = createdBy,
        syncState = syncState ?: 0
    )
}

@JsonClass(generateAdapter = true)
data class PainAssessmentNetwork(
    val beneficiaryID: String,
    val beneficiaryRegID: String,
    val assessmentId: Long? = null,
    val patientID: String? = null,
    val benVisitNo: Int? = null,
    val painSeverity: String? = null,
    val painDuration: String? = null,
    val symptomsPresent: Boolean? = null,
    val otherSymptomsSeverity: String? = null,
    val immediateReliefProvided: Boolean? = null,
    val persistentPainPresent: Boolean? = null,
    val painAssessmentEnabled: Boolean? = null,
    val distressingSymptoms: String? = null,
    val bedriddenOrSeverelyDependent: Boolean? = null,
    val lifeLimitingIllnessKnown: Boolean? = null,
    val caregiverSupportRequired: Boolean? = null,
    val palliativeCareEligible: Boolean? = null,
    val basicSymptomsSelected: String? = null,
    val basicSymptomReliefProvided: Boolean? = null,
    val basicPsychosocialSupportProvided: Boolean? = null,
    val basicCaregiverCounsellingProvided: Boolean? = null,
    val basicManagementRemarks: String? = null,
    val syncState: Int? = null,
    val referralFollowUp: ReferralFollowUpFields? = null
)

fun PainAndSymptomAssessment.toNetworkModel(
    beneficiaryID: String,
    beneficiaryRegID: String
): PainAssessmentNetwork {
    return PainAssessmentNetwork(
        beneficiaryID = beneficiaryID,
        beneficiaryRegID = beneficiaryRegID,
        assessmentId = assessmentId,
        patientID = patientID,
        benVisitNo = benVisitNo,
        painSeverity = painSeverity,
        painDuration = painDuration,
        symptomsPresent = symptomsPresent,
        otherSymptomsSeverity = otherSymptomsSeverity,
        immediateReliefProvided = immediateReliefProvided,
        persistentPainPresent = persistentPainPresent,
        painAssessmentEnabled = painAssessmentEnabled,
        distressingSymptoms = distressingSymptoms,
        bedriddenOrSeverelyDependent = bedriddenOrSeverelyDependent,
        lifeLimitingIllnessKnown = lifeLimitingIllnessKnown,
        caregiverSupportRequired = caregiverSupportRequired,
        palliativeCareEligible = palliativeCareEligible,
        basicSymptomsSelected = basicSymptomsSelected,
        basicSymptomReliefProvided = basicSymptomReliefProvided,
        basicPsychosocialSupportProvided = basicPsychosocialSupportProvided,
        basicCaregiverCounsellingProvided = basicCaregiverCounsellingProvided,
        basicManagementRemarks = basicManagementRemarks,
        syncState = syncState,
        referralFollowUp = referralFollowUp
    )
}

fun PainAssessmentNetwork.toCacheModel(patientID: String): PainAndSymptomAssessment {
    return PainAndSymptomAssessment(
        assessmentId = 0L,
        patientID = patientID,
        benVisitNo = benVisitNo,
        painSeverity = painSeverity,
        painDuration = painDuration,
        symptomsPresent = symptomsPresent,
        otherSymptomsSeverity = otherSymptomsSeverity,
        immediateReliefProvided = immediateReliefProvided,
        persistentPainPresent = persistentPainPresent,
        painAssessmentEnabled = painAssessmentEnabled,
        distressingSymptoms = distressingSymptoms,
        bedriddenOrSeverelyDependent = bedriddenOrSeverelyDependent,
        lifeLimitingIllnessKnown = lifeLimitingIllnessKnown,
        caregiverSupportRequired = caregiverSupportRequired,
        palliativeCareEligible = palliativeCareEligible,
        basicSymptomsSelected = basicSymptomsSelected,
        basicSymptomReliefProvided = basicSymptomReliefProvided,
        basicPsychosocialSupportProvided = basicPsychosocialSupportProvided,
        basicCaregiverCounsellingProvided = basicCaregiverCounsellingProvided,
        basicManagementRemarks = basicManagementRemarks,
        syncState = syncState ?: 0,
        referralFollowUp = referralFollowUp ?: ReferralFollowUpFields()
    )
}

@JsonClass(generateAdapter = true)
data class PsychosocialCaregiverSupportNetwork(
    val beneficiaryID: String,
    val beneficiaryRegID: String,
    val assessmentId: Long? = null,
    val patientID: String? = null,
    val benVisitNo: Int? = null,
    val psychosocialCounsellingProvided: Boolean? = null,
    val caregiverCounsellingProvided: Boolean? = null,
    val caregiverDistressIdentified: Boolean? = null,
    val counsellingRemarks: String? = null,
    val syncState: Int? = null,
    val referralFollowUp: ReferralFollowUpFields? = null
)

fun PsychosocialCaregiverSupport.toNetworkModel(
    beneficiaryID: String,
    beneficiaryRegID: String
): PsychosocialCaregiverSupportNetwork {
    return PsychosocialCaregiverSupportNetwork(
        beneficiaryID = beneficiaryID,
        beneficiaryRegID = beneficiaryRegID,
        assessmentId = assessmentId,
        patientID = patientId,
        benVisitNo = benVisitNo,
        psychosocialCounsellingProvided = psychosocialCounsellingProvided,
        caregiverCounsellingProvided = caregiverCounsellingProvided,
        caregiverDistressIdentified = caregiverDistressIdentified,
        counsellingRemarks = counsellingRemarks,
        syncState = syncState,
        referralFollowUp = referralFollowUp
    )
}

fun PsychosocialCaregiverSupportNetwork.toCacheModel(patientID: String): PsychosocialCaregiverSupport {
    return PsychosocialCaregiverSupport(
        assessmentId = 0L,
        patientId = patientID,
        benVisitNo = benVisitNo,
        psychosocialCounsellingProvided = psychosocialCounsellingProvided,
        caregiverCounsellingProvided = caregiverCounsellingProvided,
        caregiverDistressIdentified = caregiverDistressIdentified,
        counsellingRemarks = counsellingRemarks,
        syncState = syncState ?: 0,
        referralFollowUp = referralFollowUp ?: ReferralFollowUpFields()
    )
}

@JsonClass(generateAdapter = true)
data class NoseDiagnosisNetwork(
    val beneficiaryID: String,
    val beneficiaryRegID: String,
    val assessmentId: Long? = null,
    val patientID: String? = null,
    val benVisitNo: Int? = null,
    val difficultyBreathing: Boolean? = null,
    val openMouthBreathing: Boolean? = null,
    val noseBleed: Boolean? = null,
    val systolicBp: Int? = null,
    val diastolicBp: Int? = null,
    val foreignBodyNose: String? = null,
    val sinusitis: Boolean? = null,
    val syncState: Int? = null
)

fun NoseDiagnosisAssessment.toNetworkModel(
    beneficiaryID: String,
    beneficiaryRegID: String
): NoseDiagnosisNetwork {
    return NoseDiagnosisNetwork(
        beneficiaryID = beneficiaryID,
        beneficiaryRegID = beneficiaryRegID,
        assessmentId = assessmentId,
        patientID = patientId,
        benVisitNo = benVisitNo,
        difficultyBreathing = difficultyBreathing,
        openMouthBreathing = openMouthBreathing,
        noseBleed = noseBleed,
        systolicBp = systolicBp,
        diastolicBp = diastolicBp,
        foreignBodyNose = foreignBodyNose,
        sinusitis = sinusitis,
        syncState = syncState
    )
}

fun NoseDiagnosisNetwork.toCacheModel(patientID: String): NoseDiagnosisAssessment {
    return NoseDiagnosisAssessment(
        assessmentId = 0L,
        patientId = patientID,
        benVisitNo = benVisitNo,
        difficultyBreathing = difficultyBreathing,
        openMouthBreathing = openMouthBreathing,
        noseBleed = noseBleed,
        systolicBp = systolicBp,
        diastolicBp = diastolicBp,
        foreignBodyNose = foreignBodyNose,
        sinusitis = sinusitis,
        syncState = syncState ?: 0
    )
}

@JsonClass(generateAdapter = true)
data class ThroatDiagnosisNetwork(
    val beneficiaryID: String,
    val beneficiaryRegID: String,
    val assessmentId: Long? = null,
    val patientID: String? = null,
    val benVisitNo: Int? = null,
    val symptoms: List<String>? = null,
    val neckSwelling: Boolean? = null,
    val difficultySwallowing: Boolean? = null,
    val tonsillitis: Boolean? = null,
    val pharyngitis: Boolean? = null,
    val laryngitis: Boolean? = null,
    val sinusitis: Boolean? = null,
    val cleftLip: Boolean? = null,
    val cleftPalate: Boolean? = null,
    val syncState: Int? = null
)

fun ThroatDiagnosisAssessment.toNetworkModel(
    beneficiaryID: String,
    beneficiaryRegID: String
): ThroatDiagnosisNetwork {
    return ThroatDiagnosisNetwork(
        beneficiaryID = beneficiaryID,
        beneficiaryRegID = beneficiaryRegID,
        assessmentId = assessmentId,
        patientID = patientId,
        benVisitNo = benVisitNo,
        symptoms = symptoms,
        neckSwelling = neckSwelling,
        difficultySwallowing = difficultySwallowing,
        tonsillitis = tonsillitis,
        pharyngitis = pharyngitis,
        laryngitis = laryngitis,
        sinusitis = sinusitis,
        cleftLip = cleftLip,
        cleftPalate = cleftPalate,
        syncState = syncState
    )
}

fun ThroatDiagnosisNetwork.toCacheModel(patientID: String): ThroatDiagnosisAssessment {
    return ThroatDiagnosisAssessment(
        assessmentId = 0L,
        patientId = patientID,
        benVisitNo = benVisitNo,
        symptoms = symptoms,
        neckSwelling = neckSwelling,
        difficultySwallowing = difficultySwallowing,
        tonsillitis = tonsillitis,
        pharyngitis = pharyngitis,
        laryngitis = laryngitis,
        sinusitis = sinusitis,
        cleftLip = cleftLip,
        cleftPalate = cleftPalate,
        syncState = syncState ?: 0
    )
}

@JsonClass(generateAdapter = true)
data class ElderlyHealthNetwork(
    val beneficiaryID: String,
    val beneficiaryRegID: String,
    val assessmentId: Long? = null,
    val patientID: String? = null,
    val benVisitNo: Int? = null,
    val geriatricComplaints: Boolean? = null,
    val multipleChronicConditions: Boolean? = null,
    val recentFalls: Boolean? = null,
    val difficultyWalkingBalance: Boolean? = null,
    val visualHearingDifficulty: Boolean? = null,
    val functionalDecline: Boolean? = null,
    val bathing: Int? = null,
    val dressing: Int? = null,
    val toileting: Int? = null,
    val transferring: Int? = null,
    val continence: Int? = null,
    val feeding: Int? = null,
    val totalScore: Int? = null,
    val functionalStatus: String? = null,
    val functionalDeclineFlag: Boolean? = null,
    val memoryLoss: Boolean? = null,
    val dementiaMemoryLoss: Boolean? = null,
    val dementiaDisorientation: Boolean? = null,
    val dementiaBehaviouralChanges: Boolean? = null,
    val dementiaSelfCareDecline: Boolean? = null,
    val dementiaScreeningOutcome: String? = null,
    val dementiaReferralRequired: Boolean? = null,
    val syncState: Int? = null,
    val referralFollowUp: ReferralFollowUpFields? = null
)

fun ElderlyHealthAssessment.toNetworkModel(
    beneficiaryID: String,
    beneficiaryRegID: String
): ElderlyHealthNetwork {
    return ElderlyHealthNetwork(
        beneficiaryID = beneficiaryID,
        beneficiaryRegID = beneficiaryRegID,
        assessmentId = assessmentId,
        patientID = patientId,
        benVisitNo = benVisitNo,
        geriatricComplaints = geriatricComplaints,
        multipleChronicConditions = multipleChronicConditions,
        recentFalls = recentFalls,
        difficultyWalkingBalance = difficultyWalkingBalance,
        visualHearingDifficulty = visualHearingDifficulty,
        functionalDecline = functionalDecline,
        bathing = bathing,
        dressing = dressing,
        toileting = toileting,
        transferring = transferring,
        continence = continence,
        feeding = feeding,
        totalScore = totalScore,
        functionalStatus = functionalStatus,
        functionalDeclineFlag = functionalDeclineFlag,
        memoryLoss = memoryLoss,
        dementiaMemoryLoss = dementiaMemoryLoss,
        dementiaDisorientation = dementiaDisorientation,
        dementiaBehaviouralChanges = dementiaBehaviouralChanges,
        dementiaSelfCareDecline = dementiaSelfCareDecline,
        dementiaScreeningOutcome = dementiaScreeningOutcome,
        dementiaReferralRequired = dementiaReferralRequired,
        syncState = syncState,
        referralFollowUp = referralFollowUp
    )
}

fun ElderlyHealthNetwork.toCacheModel(patientID: String): ElderlyHealthAssessment {
    return ElderlyHealthAssessment(
        assessmentId = 0L,
        patientId = patientID,
        benVisitNo = benVisitNo ?: 0,
        geriatricComplaints = geriatricComplaints,
        multipleChronicConditions = multipleChronicConditions,
        recentFalls = recentFalls,
        difficultyWalkingBalance = difficultyWalkingBalance,
        visualHearingDifficulty = visualHearingDifficulty,
        functionalDecline = functionalDecline,
        bathing = bathing,
        dressing = dressing,
        toileting = toileting,
        transferring = transferring,
        continence = continence,
        feeding = feeding,
        totalScore = totalScore,
        functionalStatus = functionalStatus,
        functionalDeclineFlag = functionalDeclineFlag,
        memoryLoss = memoryLoss,
        dementiaMemoryLoss = dementiaMemoryLoss,
        dementiaDisorientation = dementiaDisorientation,
        dementiaBehaviouralChanges = dementiaBehaviouralChanges,
        dementiaSelfCareDecline = dementiaSelfCareDecline,
        dementiaScreeningOutcome = dementiaScreeningOutcome,
        dementiaReferralRequired = dementiaReferralRequired,
        syncState = syncState ?: 0,
        referralFollowUp = referralFollowUp ?: ReferralFollowUpFields()
    )
}

@JsonClass(generateAdapter = true)
data class MentalHealthNetwork(
    val beneficiaryID: String,
    val beneficiaryRegID: String,
    val screeningId: Long? = null,
    val patientID: String? = null,
    val benVisitNo: Int? = null,
    val emotionalBehaviouralConcerns: Boolean? = null,
    val substanceUseConcerns: Boolean? = null,
    val selfHarmSuicideThoughts: Boolean? = null,
    val memoryLossConfusion: Boolean? = null,
    val seizuresFitsLoc: Boolean? = null,
    val isPostpartum: Boolean? = null,
    val phq9LittleInterest: Int? = null,
    val phq9FeelingDown: Int? = null,
    val phq9SleepTrouble: Int? = null,
    val phq9FeelingTired: Int? = null,
    val phq9Appetite: Int? = null,
    val phq9FeelingBad: Int? = null,
    val phq9Concentration: Int? = null,
    val phq9MovingSlowly: Int? = null,
    val phq9SelfHarmThoughts: Int? = null,
    val phq9TotalScore: Int? = null,
    val phq9DepressionSeverity: String? = null,
    val phq9SystemAction: String? = null,
    val substanceCurrentTobaccoUse: Boolean? = null,
    val substanceTobaccoType: String? = null,
    val substanceTobaccoFrequency: String? = null,
    val substanceTobaccoOutcome: String? = null,
    val substanceSystemAction: String? = null,
    val substanceAlcoholUse: Boolean? = null,
    val substanceTobaccoUse: Boolean? = null,
    val substanceOtherUse: Boolean? = null,
    val substanceOtherSpecify: String? = null,
    val substanceFrequency: String? = null,
    val briefInterventionGiven: Boolean? = null,
    val suicideCurrentThoughts: Boolean? = null,
    val suicidePlan: Boolean? = null,
    val suicidePreviousAttempt: Boolean? = null,
    val suicideHopelessness: Boolean? = null,
    val suicideImmediateAssess: Boolean? = null,
    val suicideRiskLevel: String? = null,
    val dementiaProgressiveMemoryLoss: Boolean? = null,
    val dementiaForgettingRecent: Boolean? = null,
    val dementiaDisorientation: Boolean? = null,
    val dementiaDailyActivities: Boolean? = null,
    val dementiaBehaviouralChanges: Boolean? = null,
    val epilepsyRecurrentSeizures: Boolean? = null,
    val epilepsyJerkyMovements: Boolean? = null,
    val epilepsyTongueBite: Boolean? = null,
    val epilepsyConfusionAfter: Boolean? = null,
    val epilepsyLocDuration: String? = null,
    val substanceAlcoholLoss: Boolean? = null,
    val substanceAlcoholImpact: Boolean? = null,
    val substanceAlcoholWithdrawal: Boolean? = null,
    val substanceAlcoholProblematic: Boolean? = null,
    val substanceAlcoholClassification: String? = null,
    val substanceAlcoholSystemAction: String? = null,
    val substanceAlcoholFrequency: String? = null,
    val edRecurrentEpisodeloss: Boolean? = null,
    val edRecurrentJerkyMovements: Boolean? = null,
    val edConfusionordrowsiness: Boolean? = null,
    val edProgressiveMemoryLoss: Boolean? = null,
    val edConfusionDisorientation: Boolean? = null,
    val edFunctionalDecline: Boolean? = null,
    val edScreeningOutcome: String? = null,
    val edPsychosocialInterventionProvided: Boolean? = null,
    val edInterventionType: String? = null,
    val edSessionDate: String? = null,
    val edDurationMinutes: Int? = null,
    val edRemarks: String? = null,
    val edReferralRequired: String? = null,
    val edReason: String? = null,
    val referralRequired: Boolean? = null,
    val referralLevel: String? = null,
    val reasonForReferral: String? = null,
    val referralDate: String? = null,
    val followUpRequired: Boolean? = null,
    val followUpDate: String? = null,
    val improvementNoted: String? = null,
    val adherenceToAdvice: String? = null,
    val referralEscalationRequired: Boolean? = null,
    val caseClosureReason: String? = null,
    val syncState: Int? = null
)

fun MentalHealthScreeningCache.toNetworkModel(
    beneficiaryID: String,
    beneficiaryRegID: String
): MentalHealthNetwork {
    return MentalHealthNetwork(
        beneficiaryID = beneficiaryID,
        beneficiaryRegID = beneficiaryRegID,
        screeningId = screeningId,
        patientID = patientId,
        benVisitNo = benVisitNo,
        emotionalBehaviouralConcerns = emotionalBehaviouralConcerns,
        substanceUseConcerns = substanceUseConcerns,
        selfHarmSuicideThoughts = selfHarmSuicideThoughts,
        memoryLossConfusion = memoryLossConfusion,
        seizuresFitsLoc = seizuresFitsLoc,
        isPostpartum = isPostpartum,
        phq9LittleInterest = phq9LittleInterest,
        phq9FeelingDown = phq9FeelingDown,
        phq9SleepTrouble = phq9SleepTrouble,
        phq9FeelingTired = phq9FeelingTired,
        phq9Appetite = phq9Appetite,
        phq9FeelingBad = phq9FeelingBad,
        phq9Concentration = phq9Concentration,
        phq9MovingSlowly = phq9MovingSlowly,
        phq9SelfHarmThoughts = phq9SelfHarmThoughts,
        phq9TotalScore = phq9TotalScore,
        phq9DepressionSeverity = phq9DepressionSeverity,
        phq9SystemAction = phq9SystemAction,
        substanceCurrentTobaccoUse = substanceCurrentTobaccoUse,
        substanceTobaccoType = substanceTobaccoType,
        substanceTobaccoFrequency = substanceTobaccoFrequency,
        substanceTobaccoOutcome = substanceTobaccoOutcome,
        substanceSystemAction = substanceSystemAction,
        substanceAlcoholUse = substanceAlcoholUse,
        substanceTobaccoUse = substanceTobaccoUse,
        substanceOtherUse = substanceOtherUse,
        substanceOtherSpecify = substanceOtherSpecify,
        substanceFrequency = substanceFrequency,
        briefInterventionGiven = briefInterventionGiven,
        suicideCurrentThoughts = suicideCurrentThoughts,
        suicidePlan = suicidePlan,
        suicidePreviousAttempt = suicidePreviousAttempt,
        suicideHopelessness = suicideHopelessness,
        suicideImmediateAssess = suicideImmediateAssess,
        suicideRiskLevel = suicideRiskLevel,
        dementiaProgressiveMemoryLoss = dementiaProgressiveMemoryLoss,
        dementiaForgettingRecent = dementiaForgettingRecent,
        dementiaDisorientation = dementiaDisorientation,
        dementiaDailyActivities = dementiaDailyActivities,
        dementiaBehaviouralChanges = dementiaBehaviouralChanges,
        epilepsyRecurrentSeizures = epilepsyRecurrentSeizures,
        epilepsyJerkyMovements = epilepsyJerkyMovements,
        epilepsyTongueBite = epilepsyTongueBite,
        epilepsyConfusionAfter = epilepsyConfusionAfter,
        epilepsyLocDuration = epilepsyLocDuration,
        substanceAlcoholLoss = substance_alcohol_loss,
        substanceAlcoholImpact = substanceAlcoholImpact,
        substanceAlcoholWithdrawal = substanceAlcoholWithdrawal,
        substanceAlcoholProblematic = substanceAlcoholProblematic,
        substanceAlcoholClassification = substanceAlcoholClassification,
        substanceAlcoholSystemAction = substanceAlcoholSystemAction,
        substanceAlcoholFrequency = substance_alcohol_frequency,
        edRecurrentEpisodeloss = edRecurrentEpisodeloss,
        edRecurrentJerkyMovements = edRecurrentJerkyMovements,
        edConfusionordrowsiness = edConfusionordrowsiness,
        edProgressiveMemoryLoss = edProgressiveMemoryLoss,
        edConfusionDisorientation = edConfusionDisorientation,
        edFunctionalDecline = edFunctionalDecline,
        edScreeningOutcome = edScreeningOutcome,
        edPsychosocialInterventionProvided = edPsychosocialInterventionProvided,
        edInterventionType = edInterventionType,
        edSessionDate = edSessionDate,
        edDurationMinutes = edDurationMinutes,
        edRemarks = edRemarks,
        edReferralRequired = edReferralRequired,
        edReason = edReason,
        referralRequired = referralRequired,
        referralLevel = referralLevel,
        reasonForReferral = reasonForReferral,
        referralDate = referralDate,
        followUpRequired = followUpRequired,
        followUpDate = followUpDate,
        improvementNoted = improvementNoted,
        adherenceToAdvice = adherenceToAdvice,
        referralEscalationRequired = referralEscalationRequired,
        caseClosureReason = caseClosureReason,
        syncState = syncState
    )
}

fun MentalHealthNetwork.toCacheModel(patientID: String): MentalHealthScreeningCache {
    return MentalHealthScreeningCache(
        screeningId = 0L,
        patientId = patientID,
        benVisitNo = benVisitNo,
        emotionalBehaviouralConcerns = emotionalBehaviouralConcerns,
        substanceUseConcerns = substanceUseConcerns,
        selfHarmSuicideThoughts = selfHarmSuicideThoughts,
        memoryLossConfusion = memoryLossConfusion,
        seizuresFitsLoc = seizuresFitsLoc,
        isPostpartum = isPostpartum,
        phq9LittleInterest = phq9LittleInterest,
        phq9FeelingDown = phq9FeelingDown,
        phq9SleepTrouble = phq9SleepTrouble,
        phq9FeelingTired = phq9FeelingTired,
        phq9Appetite = phq9Appetite,
        phq9FeelingBad = phq9FeelingBad,
        phq9Concentration = phq9Concentration,
        phq9MovingSlowly = phq9MovingSlowly,
        phq9SelfHarmThoughts = phq9SelfHarmThoughts,
        phq9TotalScore = phq9TotalScore,
        phq9DepressionSeverity = phq9DepressionSeverity,
        phq9SystemAction = phq9SystemAction,
        substanceCurrentTobaccoUse = substanceCurrentTobaccoUse,
        substanceTobaccoType = substanceTobaccoType,
        substanceTobaccoFrequency = substanceTobaccoFrequency,
        substanceTobaccoOutcome = substanceTobaccoOutcome,
        substanceSystemAction = substanceSystemAction,
        substanceAlcoholUse = substanceAlcoholUse,
        substanceTobaccoUse = substanceTobaccoUse,
        substanceOtherUse = substanceOtherUse,
        substanceOtherSpecify = substanceOtherSpecify,
        substanceFrequency = substanceFrequency,
        briefInterventionGiven = briefInterventionGiven,
        suicideCurrentThoughts = suicideCurrentThoughts,
        suicidePlan = suicidePlan,
        suicidePreviousAttempt = suicidePreviousAttempt,
        suicideHopelessness = suicideHopelessness,
        suicideImmediateAssess = suicideImmediateAssess,
        suicideRiskLevel = suicideRiskLevel,
        dementiaProgressiveMemoryLoss = dementiaProgressiveMemoryLoss,
        dementiaForgettingRecent = dementiaForgettingRecent,
        dementiaDisorientation = dementiaDisorientation,
        dementiaDailyActivities = dementiaDailyActivities,
        dementiaBehaviouralChanges = dementiaBehaviouralChanges,
        epilepsyRecurrentSeizures = epilepsyRecurrentSeizures,
        epilepsyJerkyMovements = epilepsyJerkyMovements,
        epilepsyTongueBite = epilepsyTongueBite,
        epilepsyConfusionAfter = epilepsyConfusionAfter,
        epilepsyLocDuration = epilepsyLocDuration,
        substance_alcohol_loss = substanceAlcoholLoss,
        substanceAlcoholImpact = substanceAlcoholImpact,
        substanceAlcoholWithdrawal = substanceAlcoholWithdrawal,
        substanceAlcoholProblematic = substanceAlcoholProblematic,
        substanceAlcoholClassification = substanceAlcoholClassification,
        substanceAlcoholSystemAction = substanceAlcoholSystemAction,
        substance_alcohol_frequency = substanceAlcoholFrequency,
        edRecurrentEpisodeloss = edRecurrentEpisodeloss,
        edRecurrentJerkyMovements = edRecurrentJerkyMovements,
        edConfusionordrowsiness = edConfusionordrowsiness,
        edProgressiveMemoryLoss = edProgressiveMemoryLoss,
        edConfusionDisorientation = edConfusionDisorientation,
        edFunctionalDecline = edFunctionalDecline,
        edScreeningOutcome = edScreeningOutcome,
        edPsychosocialInterventionProvided = edPsychosocialInterventionProvided,
        edInterventionType = edInterventionType,
        edSessionDate = edSessionDate,
        edDurationMinutes = edDurationMinutes,
        edRemarks = edRemarks,
        edReferralRequired = edReferralRequired,
        edReason = edReason,
        referralRequired = referralRequired,
        referralLevel = referralLevel,
        reasonForReferral = reasonForReferral,
        referralDate = referralDate,
        followUpRequired = followUpRequired,
        followUpDate = followUpDate,
        improvementNoted = improvementNoted,
        adherenceToAdvice = adherenceToAdvice,
        referralEscalationRequired = referralEscalationRequired,
        caseClosureReason = caseClosureReason,
        syncState = syncState ?: 0
    )
}
