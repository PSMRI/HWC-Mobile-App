package org.piramalswasthya.cho.model

data class HistoryDetails(
    val childVaccineDetails: ChildVaccineDetails,
    val comorbidConditions: ComorbidConditions,
    val developmentHistory: DevelopmentHistory,
    val familyHistory: FamilyHistory,
    val feedingHistory: FeedingHistory,
    val femaleObstetricHistory: FemaleObstetricHistory,
    val immunizationHistory: ImmunizationHistory,
    val medicationHistory: MedicationHistoryNetwork,
    val menstrualHistory: MenstrualHistory,
    val pastHistory: PastHistory,
    val perinatalHistroy: PerinatalHistroy,
    val personalHistory: PersonalHistory,
){
    constructor(user: UserDomain?) : this(
        ChildVaccineDetails(user),
        ComorbidConditions(user),
        DevelopmentHistory(user),
        FamilyHistory(user),
        FeedingHistory(user),
        FemaleObstetricHistory(user),
        ImmunizationHistory(user),
        MedicationHistoryNetwork(user),
        MenstrualHistory(user),
        PastHistory(user),
        PerinatalHistroy(user),
        PersonalHistory(user),
    )
}

data class ChildVaccineDetails(
    val benVisitID: String?,
    val beneficiaryRegID: String?,
    val childOptionalVaccineList: List<ChildOptionalVaccine>?,
    val createdBy: String?,
    val parkingPlaceID: Int?,
    val providerServiceMapID: String?,
    val vanID: Int?,
//benVisitID: null
//beneficiaryRegID: "33195"
//childOptionalVaccineList:[{vaccineName: null, sctCode: null, sctTerm: null, otherVaccineName: null, ageUnitID: null,…}]
//createdBy:"Pranathi"
//parkingPlaceID:10
//providerServiceMapID:"13"
//vanID:61
){
    constructor(user: UserDomain?) : this(
        null,
        "33195",
        arrayListOf(ChildOptionalVaccine()),
        user?.userName,
        user?.parkingPlaceId,
        user?.serviceMapId.toString(),
        user?.vanId
    )
}

data class ChildOptionalVaccine(
    val ageUnitID: String?,
    val otherVaccineName: String?,
    val receivedFacilityName: String?,
    val sctCode: String?,
    val sctTerm: String?,
    val vaccineName: String?,
){
    constructor() : this(
        null,
        null,
        null,
        null,
        null,
        null,
    )
}

data class ComorbidConditions(
    val benVisitID: String?,
    val beneficiaryRegID: String?,
    val comorbidityConcurrentConditionsList: List<ComorbidityConcurrentConditions>?,
    val createdBy: String?,
    val parkingPlaceID: Int?,
    val providerServiceMapID: String?,
    val vanID: Int?
//    benVisitID: null
//    beneficiaryRegID: "33195"
//comorbidityConcurrentConditionsList:[{comorbidConditions: null, otherComorbidCondition: null}]
//createdBy:"Pranathi"
//parkingPlaceI:10
//providerServiceMapID:"13"
//vanID:61
){
    constructor(user: UserDomain?) : this(
        null,
        "33195",
        arrayListOf(ComorbidityConcurrentConditions()),
        user?.userName,
        user?.parkingPlaceId,
        user?.serviceMapId.toString(),
        user?.vanId
    )
}

data class ComorbidityConcurrentConditions(
    val comorbidConditions: String?,
    val otherComorbidCondition: String?,
//    comorbidConditions: null
//    otherComorbidCondition: null
){
    constructor() : this(
        null,
        null
    )
}

data class DevelopmentHistory(
    val benVisitID: String?,
    val beneficiaryRegID: String?,
    val createdBy: String?,
    val developmentProblems: String?,
    val fineMotorMilestones: String?,
    val grossMotorMilestones: String?,
    val isFineMotorMilestones: String?,
    val isGrossMotorMilestones: String?,
    val isLanguageMilestones: String?,
    val isSocialMilestones: String?,
    val languageMilestones: String?,
    val parkingPlaceID: Int?,
    val providerServiceMapID: String?,
    val socialMilestones: String?,
    val vanID: Int?,
//    benVisitID: null
//    beneficiaryRegID: "33195"
//    createdBy:"Pranathi"
//    developmentProblems:null
//    fineMotorMilestones:null
//    grossMotorMilestones:null
//    isFineMotorMilestones:null
//    isGrossMotorMilestones:null
//    isLanguageMilestones:null
//    isSocialMilestones:null
//    languageMilestones:null
//    parkingPlaceID:10
//    providerServiceMapID:"13"
//    socialMilestones:null
//    vanID:61
){
    constructor(user: UserDomain?) : this(
        null,
        "33195",
        user?.userName,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        user?.parkingPlaceId,
        user?.serviceMapId.toString(),
        null,
        user?.vanId
    )
}

data class FamilyHistory(
    val benVisitID: String?,
    val beneficiaryRegID: String?,
    val createdBy: String?,
    val familyDiseaseList: List<FamilyDisease>?,
    val geneticDisorder: String?,
    val isConsanguineousMarrige: String?,
    val isGeneticDisorder: String?,
    val parkingPlaceID: Int?,
    val providerServiceMapID: String?,
    val vanID: Int?
//    benVisitID: null
//    beneficiaryRegID: "33195"
//    createdBy:"Pranathi"
//    familyDiseaseList:[,…]
//    geneticDisorder:null
//    isConsanguineousMarrige:null
//    isGeneticDisorder:null
//    parkingPlaceID:10
//    providerServiceMapID:"13"
//    vanID:61
){
    constructor(user: UserDomain?) : this(
        null,
        "33195",
        user?.userName,
        arrayListOf(FamilyDisease()),
        null,
        null,
        null,
        user?.parkingPlaceId,
        user?.serviceMapId.toString(),
        user?.vanId
    )
}

data class FamilyDisease(
    val diseaseType: String?,
    val diseaseTypeID: String?,
    val otherDiseaseType: String?,
    val snomedCode: String?,
    val snomedTerm: String?,
//    diseaseType: null
//    diseaseTypeID: null
//    otherDiseaseType: null
//    snomedCode: null
//    snomedTerm: null
){
    constructor() : this(
        null,
        null,
        null,
        null,
        null,
    )
}

data class FeedingHistory(
    val benVisitID: String?,
    val beneficiaryRegID: String?,
    val compFeedStartAge: String?,
    val createdBy: String?,
    val foodIntoleranceStatus: Int?,
    val noOfCompFeedPerDay: String?,
    val otherFoodIntolerance: String?,
    val parkingPlaceID: Int?,
    val providerServiceMapID: String?,
    val typeOfFeed: String?,
    val typeOfFoodIntolerances: String?,
    val vanID: Int?
//    benVisitID: null
//    beneficiaryRegID: "33195"
//    compFeedStartAge:null
//    createdBy:"Pranathi"
//    foodIntoleranceStatus:0
//    noOfCompFeedPerDay:null
//    otherFoodIntolerance:null
//    parkingPlaceID:10
//    providerServiceMapID:"13"
//    typeOfFeed:null
//    typeOfFoodIntolerances:null
//    vanID:61
){
    constructor(user: UserDomain?) : this(
        null,
        "33195",
        null,
        user?.userName,
        0,
        null,
        null,
        user?.parkingPlaceId,
        user?.serviceMapId.toString(),
        null,
        null,
        user?.vanId
    )
}

data class FemaleObstetricHistory(
    val benVisitID: String?,
    val beneficiaryRegID: String?,
    val complicationPregList: List<String>?,
    val createdBy: String?,
    val femaleObstetricHistoryList: List<String>?,
    val parkingPlaceID: Int?,
    val providerServiceMapID: String?,
    val totalNoOfPreg: Int?,
    val vanID: Int?,
//    benVisitID: null
//    beneficiaryRegID: "33211"
//    complicationPregList:[]
//    createdBy:"Pranathi"
//    femaleObstetricHistoryList:[]
//    parkingPlaceID:10
//    providerServiceMapID:"13"
//    totalNoOfPreg:null
//    vanID:61
){
    constructor(user: UserDomain?) : this(
        null,
        "33211",
        emptyList(),
        user?.userName,
        emptyList(),
        user?.parkingPlaceId,
        user?.serviceMapId.toString(),
        null,
        user?.vanId
    )
}



data class ImmunizationHistory(
    val benVisitID: String?,
    val beneficiaryRegID: String?,
    val createdBy: String?,
    val deleted: String?,
    val immunizationList: List<Immunization>?,
    val processed: String?,
    val providerServiceMapID: String?,
//    benVisitID: null
//    beneficiaryRegID: "33211"
//    createdBy:"Pranathi"
//    deleted:null
//    immunizationList:[{defaultReceivingAge: "At Birth", hideSelectAll: false,…},…]
//    processed:null
//    providerServiceMapID:"13"
){
    constructor(user: UserDomain?): this(
        null,
        "33211",
        user?.userName,
        null,
        arrayListOf(immunization_at_birth, immunization_6_weeks),
        null,
        user?.serviceMapId.toString()
    )
}

data class Immunization(
    val defaultReceivingAge: String?,
    val hideSelectAll: Boolean?,
    val vaccines: List<Vaccine>?
)

data class Vaccine(
    val hide: Boolean?,
    val sctCode: String?,
    val sctTerm: String?,
    val status: Boolean?,
    val vaccine: String?
)


val bcg_vaccine = Vaccine(false, "17971005", "Sedated", false, "BCG")
val opv_0_vaccine = Vaccine(true, "82286005", "Hyper IgM syndrome", false, "OPV-0")
val hbv_0_vaccine = Vaccine(true, "386661006", "Fever (finding)", false, "HBV-0")

val pentavalent_1_vaccine = Vaccine(false, null, null, false, "Pentavalent-1")
val rota_vaccine_1_vaccine = Vaccine(false, null, null, false, "Rota Vaccine-1")
val opv_1_vaccine = Vaccine(false, null, null, false, "OPV-1")
val fipv_1_vaccine = Vaccine(false, null, null, false, "fIPV-1")
val pcv_1_vaccine = Vaccine(false, null, null, false, "PCV1")

val vaccine_at_birth = arrayListOf(bcg_vaccine, opv_0_vaccine, hbv_0_vaccine)
val vaccine_6_weeks = arrayListOf(pentavalent_1_vaccine, rota_vaccine_1_vaccine, opv_1_vaccine, fipv_1_vaccine, pcv_1_vaccine)

val immunization_at_birth = Immunization("At Birth", false, vaccine_at_birth)
val immunization_6_weeks = Immunization("6 Weeks", false, vaccine_6_weeks)

data class MedicationHistoryNetwork(
    val benVisitID: String?,
    val beneficiaryRegID: String?,
    val createdBy: String?,
    val medicationHistoryList: List<MedicationHistoryList>?,
    val parkingPlaceID: Int?,
    val providerServiceMapID: String?,
    val vanID: Int?,
//    benVisitID: null
//    beneficiaryRegID: "33212"
//    createdBy:"Pranathi"
//    medicationHistoryList:[{currentMedication: null}]
//    parkingPlaceID:10
//    providerServiceMapID:"13"
//    vanID:61
){
    constructor(user: UserDomain?) : this(
        null,
        "33212",
        user?.userName,
        arrayListOf(MedicationHistoryList()),
        user?.parkingPlaceId,
        user?.serviceMapId.toString(),
        user?.vanId
    )
}

data class MedicationHistoryList(
    val currentMedication: String?
){
    constructor() : this(
        null
    )
}

data class MenstrualHistory(
    val benVisitID: String?,
    val beneficiaryRegID: String?,
    val bloodFlowDuration: String?,
    val createdBy: String?,
    val cycleLength: String?,
    val menstrualCycleStatus: String?,
    val menstrualCycleStatusID: String?,
    val menstrualCyclelengthID: String?,
    val menstrualFlowDurationID: String?,
    val menstrualProblemList: String?,
    val parkingPlaceID: Int?,
    val providerServiceMapID: String?,
    val regularity: String?,
    val vanID: Int?,
//        benVisitID:null
//        beneficiaryRegID:"33212"
//        bloodFlowDuration:null
//        createdBy:"Pranathi"
//        cycleLength:null
//        menstrualCycleStatus:null
//        menstrualCycleStatusID:null
//        menstrualCyclelengthID:null
//        menstrualFlowDurationID:null
//        menstrualProblemList:null
//        parkingPlaceID:10
//        providerServiceMapID:"13"
//        regularity:null
//        vanID:61
){
    constructor(user: UserDomain?) : this(
        null,
        "33212",
        null,
        user?.userName,
        null,
        null,
        null,
        null,
        null,
        null,
        user?.parkingPlaceId,
        user?.serviceMapId.toString(),
        null,
        user?.vanId
    )
}

data class PastHistory(
    val benVisitID: String?,
    val beneficiaryRegID: String?,
    val createdBy: String?,
    val parkingPlaceID: Int?,
    val pastIllness: List<PastIllness>?,
    val pastSurgery: List<PastSurgery>?,
    val providerServiceMapID: String?,
    val vanID: Int?,
//    benVisitID: null
//    beneficiaryRegID: "33212"
//    createdBy:"Pranathi"
//    parkingPlaceID:10
//    pastIllness:[{illnessTypeID: null, illnessType: null, otherIllnessType: null}]
//    pastSurgery:[{surgeryID: null, surgeryType: null, otherSurgeryType: null}]
//    providerServiceMapID:"13"
//    vanID:61
){
    constructor(user: UserDomain?) : this(
        null,
        "33212",
        user?.userName,
        user?.parkingPlaceId,
        arrayListOf(PastIllness()),
        arrayListOf(PastSurgery()),
        user?.serviceMapId.toString(),
        user?.vanId
    )
}

data class PastIllness(
    val illnessType: String?,
    val illnessTypeID: String?,
    val otherIllnessType: String?,
//    illnessType: null
//    illnessTypeID: null
//    otherIllnessType: null
){
    constructor() : this(
        null,
        null,
        null
    )
}

data class PastSurgery(
    val otherSurgeryType: String?,
    val surgeryID: String?,
    val surgeryType: String?,
//    otherSurgeryType: null
//    surgeryID: null
//    surgeryType: null
){
    constructor() : this(
        null,
        null,
        null
    )
}

data class PerinatalHistroy(
    val benVisitID: String?,
    val beneficiaryRegID: String?,
    val birthWeightG: String?,
    val complicationAtBirth: String?,
    val complicationAtBirthID: String?,
    val createdBy: String?,
    val deliveryPlaceID: String?,
    val deliveryTypeID: String?,
    val gestation: String?,
    val otherComplicationAtBirth: String?,
    val otherPlaceOfDelivery: String?,
    val parkingPlaceID: Int?,
    val placeOfDelivery: String?,
    val providerServiceMapID: String?,
    val typeOfDelivery: String?,
    val vanID: Int?,
//    benVisitID: null
//    beneficiaryRegID: "33212"
//    birthWeightG:null
//    complicationAtBirth:null
//    complicationAtBirthID:null
//    createdBy:"Pranathi"
//    deliveryPlaceID:null
//    deliveryTypeID:null
//    gestation:null
//    otherComplicationAtBirth:null
//    otherPlaceOfDelivery:null
//    parkingPlaceID:10
//    placeOfDelivery:null
//    providerServiceMapID:"13"
//    typeOfDelivery:null
//    vanID:61
){
    constructor(user: UserDomain?) : this(
        null,
        "33212",
        null,
        null,
        null,
        "Pranathi",
        null,
        null,
        null,
        null,
        null,
        user?.parkingPlaceId,
        null,
        user?.serviceMapId.toString(),
        null,
        user?.vanId
    )
}

data class PersonalHistory(
    val alcoholIntakeStatus: String?,
    val alcoholList: List<Alcohol>?,
    val allergicList: List<Allergic>?,
    val allergyStatus: String?,
    val benVisitID: String?,
    val beneficiaryRegID: String?,
    val createdBy: String?,
    val dietaryType: String?,
    val parkingPlaceID: Int?,
    val physicalActivityType: String?,
    val providerServiceMapID: String?,
    val riskySexualPracticesStatus: String?,
    val tobaccoList: List<Tobacco>?,
    val tobaccoUseStatus: String?,
    val vanID: Int?
//    alcoholIntakeStatus: null
//    alcoholList: [{alcoholTypeID: null, typeOfAlcohol: null, otherAlcoholType: null, alcoholIntakeFrequency: null,…}]
//    allergicList:[{allergyType: null, allergyName: null, snomedTerm: null, snomedCode: null,…}]
//    allergyStatus:null
//    benVisitID:null
//    beneficiaryRegID:"33212"
//    createdBy:"Pranathi"
//    dietaryType:null
//    parkingPlaceID:null
//    physicalActivityType:null
//    providerServiceMapID:"13"
//    riskySexualPracticesStatus:null
//    tobaccoList:[{tobaccoUseTypeID: null, tobaccoUseType: null, otherTobaccoUseType: null, number: null,…}]
//    tobaccoUseStatus:null
//    vanID:null
){
    constructor(user: UserDomain?) : this(
        null,
        arrayListOf(Alcohol()),
        arrayListOf(Allergic()),
        null,
        null,
        "33212",
        user?.userName,
        null,
        user?.parkingPlaceId,
        null,
        user?.serviceMapId.toString(),
        null,
        arrayListOf(Tobacco()),
        null,
        user?.vanId
    )
}

data class Alcohol(
    val alcoholIntakeFrequency: String?,
    val alcoholTypeID: String?,
    val avgAlcoholConsumption: String?,
    val duration: String?,
    val durationUnit: String?,
    val otherAlcoholType: String?,
    val typeOfAlcohol: String?,
//    alcoholIntakeFrequency: null
//    alcoholTypeID: null
//    avgAlcoholConsumption: null
//    duration: null
//    durationUnit: null
//    otherAlcoholType: null
//    typeOfAlcohol: null
){
    constructor() : this(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
    )
}

data class Allergic(
    val allergyName: String?,
    val allergyType: String?,
    val enableOtherAllergy: Boolean?,
    val otherAllergicReaction: String?,
    val snomedCode: String?,
    val snomedTerm: String?,
    val typeOfAllergicReactions: String?,
//    allergyName: null
//    allergyType: null
//    enableOtherAllergy: false
//    otherAllergicReaction: null
//    snomedCode: null
//    snomedTerm: null
//    typeOfAllergicReactions: null
){
    constructor() : this(
        null,
        null,
        false,
        null,
        null,
        null,
        null,
    )
}

data class Tobacco(
    val duration: String?,
    val durationUnit: String?,
    val number: String?,
    val numberperDay: String?,
    val numberperWeek: String?,
    val otherTobaccoUseType: String?,
    val perDay: String?,
    val tobaccoUseType: String?,
    val tobaccoUseTypeID: String?,
//    duration: null
//    durationUnit: null
//    number: null
//    numberperDay: null
//    numberperWeek: null
//    otherTobaccoUseType: null
//    perDay: null
//    tobaccoUseType: null
//    tobaccoUseTypeID: null
){
    constructor() : this(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
    )
}