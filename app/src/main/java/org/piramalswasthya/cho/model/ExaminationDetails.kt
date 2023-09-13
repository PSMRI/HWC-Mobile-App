package org.piramalswasthya.cho.model

data class ExaminationDetails(
    val cardioVascularExamination: CardioVascularExamination,
    val centralNervousSystemExamination: CentralNervousSystemExamination,
    val gastroIntestinalExamination: GastroIntestinalExamination,
    val generalExamination: GeneralExamination,
    val genitoUrinarySystemExamination: GenitoUrinarySystemExamination,
    val headToToeExamination: HeadToToeExamination,
    val musculoskeletalSystemExamination: MusculoskeletalSystemExamination,
    val respiratorySystemExamination: RespiratorySystemExamination,
){
    constructor(user: UserDomain?) : this(
        CardioVascularExamination(user),
        CentralNervousSystemExamination(user),
        GastroIntestinalExamination(user),
        GeneralExamination(user),
        GenitoUrinarySystemExamination(user),
        HeadToToeExamination(user),
        MusculoskeletalSystemExamination(user),
        RespiratorySystemExamination(user),
    )
}

data class CardioVascularExamination(
    val additionalHeartSounds: String?,
    val apexbeatLocation: String?,
    val apexbeatType: String?,
    val benVisitID: String?,
    val beneficiaryRegID: String?,
    val createdBy: String?,
    val firstHeartSound_S1: String?,
    val jugularVenousPulse_JVP: String?,
    val murmurs: String?,
    val parkingPlaceID: Int?,
    val pericardialRub: String?,
    val providerServiceMapID: String?,
    val secondHeartSound_S2: String?,
    val vanID: Int?
){
    constructor(user: UserDomain?) : this(
        null,
        null,
        null,
        null,
        "33195",
        user?.userName,
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

data class CentralNervousSystemExamination(
    val autonomicSystem: String?,
    val benVisitID: String?,
    val beneficiaryRegID: String?,
    val cerebellarSigns: String?,
    val cranialNervesExamination: String?,
    val createdBy: String?,
    val handedness: String?,
    val motorSystem: String?,
    val parkingPlaceID: Int?,
    val providerServiceMapID: String?,
    val sensorySystem: String?,
    val signsOfMeningealIrritation: String?,
    val skull: String?,
    val vanID: Int?,
//    autonomicSystem: null
//    benVisitID: null
//    beneficiaryRegID: "33195"
//    cerebellarSigns:null
//    cranialNervesExamination:null
//    createdBy:"Pranathi"
//    handedness:null
//    motorSystem:null
//    parkingPlaceID:10
//    providerServiceMapID:"13"
//    sensorySystem:null
//    signsOfMeningealIrritation:null
//    skull:null
//    vanID:61
){
    constructor(user: UserDomain?) : this(
        null,
        null,
        null,
        null,
        "33195",
        user?.userName,
        null,
        null,
        user?.parkingPlaceId,
        user?.serviceMapId.toString(),
        null,
        null,
        null,
        user?.vanId
    )
}

data class GastroIntestinalExamination(
    val analRegion: String?,
    val auscultation: String?,
    val benVisitID: String?,
    val beneficiaryRegID: String?,
    val createdBy: String?,
    val inspection: String?,
    val palpation_AbdomenTexture: String?,
    val palpation_Liver: String?,
    val palpation_LocationOfTenderness: String?,
    val palpation_Spleen: String?,
    val palpation_Tenderness: String?,
    val parkingPlaceID: Int?,
    val percussion: String?,
    val providerServiceMapID: String?,
    val vanID: Int?,
//    analRegion: null
//    auscultation: null
//    benVisitID: null
//    beneficiaryRegID: "33195"
//    createdBy:"Pranathi"
//    inspection:null
//    palpation_AbdomenTexture:null
//    palpation_Liver:null
//    palpation_LocationOfTenderness:null
//    palpation_Spleen:null
//    palpation_Tenderness:null
//    parkingPlaceID:10
//    percussion:null
//    providerServiceMapID:"13"
//    vanID:61
) {
    constructor(user : UserDomain?) : this(
        null,
        null,
        null,
        "33195",
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
        user?.vanId
    )
}

data class GeneralExamination(
    val benVisitID: String?,
    val beneficiaryRegID: String?,
    val builtAndAppearance: String?,
    val clubbing: String?,
    val coherence: String?,
    val comfortness: String?,
    val consciousness: String?,
    val cooperation: String?,
    val createdBy: String?,
    val cyanosis: String?,
    val dangerSigns: String?,
    val edema: String?,
    val edemaType: String?,
    val extentOfEdema: String?,
    val foetalMovements: String?,
    val gait: String?,
    val jaundice: String?,
    val lymphadenopathy: String?,
    val lymphnodesInvolved: String?,
    val pallor: String?,
    val parkingPlaceID: Int?,
    val providerServiceMapID: String?,
    val quickening: String?,
    val typeOfDangerSigns: String?,
    val typeOfLymphadenopathy: String?,
    val vanID: Int?
//    benVisitID: null
//    beneficiaryRegID: "33195"
//    builtAndAppearance:null
//    clubbing:null
//    coherence:null
//    comfortness:null
//    consciousness:null
//    cooperation:null
//    createdBy:"Pranathi"
//    cyanosis:null
//    dangerSigns:null
//    edema:null
//    edemaType:null
//    extentOfEdema:null
//    foetalMovements:null
//    gait:null
//    jaundice:null
//    lymphadenopathy:null
//    lymphnodesInvolved:null
//    pallor:null
//    parkingPlaceID:10
//    providerServiceMapID:"13"
//    quickening:null
//    typeOfDangerSigns:null
//    typeOfLymphadenopathy:null
//    vanID:61
){
    constructor(user: UserDomain?) : this(
        null,
        "33195",
        null,
        null,
        null,
        null,
        null,
        null,
        user?.userName,
        null,
        null,
        null,
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
        null,
        null,
        user?.vanId
    )
}

data class GenitoUrinarySystemExamination(
    val benVisitID: String?,
    val beneficiaryRegID: String?,
    val createdBy: String?,
    val externalGenitalia: String?,
    val parkingPlaceID: Int?,
    val providerServiceMapID: String?,
    val renalAngle: String?,
    val suprapubicRegion: String?,
    val vanID: Int?,
//    benVisitID: null
//    beneficiaryRegID: "33195"
//    createdBy:"Pranathi"
//    externalGenitalia:null
//    parkingPlaceID:10
//    providerServiceMapID:"13"
//    renalAngle:null
//    suprapubicRegion:null
//    vanID:61
){
    constructor(user: UserDomain?) : this(
        null,
        "33195",
        user?.userName,
        null,
        user?.parkingPlaceId,
        user?.serviceMapId.toString(),
        null,
        null,
        user?.vanId
    )
}

data class HeadToToeExamination(
    val benVisitID: String?,
    val beneficiaryRegID: String?,
    val breastAndNipples:  String?,
    val createdBy: String?,
    val ears: String?,
    val eyes: String?,
    val hair: String?,
    val head: String?,
    val headtoToeExam: String?,
    val lowerLimbs: String?,
    val nails: String?,
    val nipples: String?,
    val nose: String?,
    val oralCavity: String?,
    val parkingPlaceID: Int?,
    val providerServiceMapID: String?,
    val skin: String?,
    val throat: String?,
    val trunk: String?,
    val upperLimbs: String?,
    val vanID: Int?
//    benVisitID: null
//    beneficiaryRegID: "33195"
//    breastAndNipples:null
//    createdBy:"Pranathi"
//    ears:null
//    eyes:null
//    hair:null
//    head:null
//    headtoToeExam:null
//    lowerLimbs:null
//    nails:null
//    nipples:null
//    nose:null
//    oralCavity:null
//    parkingPlaceID:10
//    providerServiceMapID:"13"
//    skin:null
//    throat:null
//    trunk:null
//    upperLimbs:null
//    vanID:61
){
    constructor(user: UserDomain?) : this(
        null,
        "33195",
        null,
        user?.userName,
        null,
        null,
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
        null,
        null,
        null,
        user?.vanId,
    )
}

data class MusculoskeletalSystemExamination(
    val benVisitID: String?,
    val beneficiaryRegID: String?,
    val chestWall: String?,
    val createdBy: String?,
    val joint_Abnormality: String?,
    val joint_Laterality: String?,
    val joint_TypeOfJoint: String?,
    val lowerLimb_Abnormality: String?,
    val lowerLimb_Laterality: String?,
    val parkingPlaceID: Int?,
    val providerServiceMapID: String?,
    val spine: String?,
    val upperLimb_Abnormality: String?,
    val upperLimb_Laterality: String?,
    val vanID: Int?
//    benVisitID: null
//    beneficiaryRegID: "33195"
//    chestWall:null
//    createdBy:"Pranathi"
//    joint_Abnormality:null
//    joint_Laterality:null
//    joint_TypeOfJoint:null
//    lowerLimb_Abnormality:null
//    lowerLimb_Laterality:null
//    parkingPlaceID:10
//    providerServiceMapID:"13"
//    spine:null
//    upperLimb_Abnormality:null
//    upperLimb_Laterality:null
//    vanID:61
){
    constructor(user: UserDomain?) : this(
        null,
        "33195",
        null,
        user?.userName,
        null,
        null,
        null,
        null,
        null,
        user?.parkingPlaceId,
        user?.serviceMapId.toString(),
        null,
        null,
        null,
        user?.vanId
    )
}

data class RespiratorySystemExamination(
    val auscultation_BreathSounds: String?,
    val auscultation_ConductedSounds: String?,
    val auscultation_Crepitations: String?,
    val auscultation_PleuralRub: String?,
    val auscultation_Stridor: String?,
    val auscultation_Wheezing: String?,
    val benVisitID: String?,
    val beneficiaryRegID: String?,
    val createdBy: String?,
    val inspection: String?,
    val palpation: String?,
    val parkingPlaceID: Int?,
    val percussion: String?,
    val providerServiceMapID: String?,
    val signsOfRespiratoryDistress: String?,
    val trachea: String?,
    val vanID: Int?
//    auscultation_BreathSounds: null
//    auscultation_ConductedSounds: null
//    auscultation_Crepitations: null
//    auscultation_PleuralRub: null
//    auscultation_Stridor: null
//    auscultation_Wheezing: null
//    benVisitID: null
//    beneficiaryRegID: "33195"
//    createdBy:"Pranathi"
//    inspection:null
//    palpation:null
//    parkingPlaceID:10
//    percussion:null
//    providerServiceMapID:"13"
//    signsOfRespiratoryDistress:null
//    trachea:null
//    vanID:61
){
    constructor(user: UserDomain?) : this(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        "33195",
        user?.userName,
        null,
        null,
        user?.parkingPlaceId,
        null,
        user?.serviceMapId.toString(),
        null,
        null,
        user?.vanId
    )
}