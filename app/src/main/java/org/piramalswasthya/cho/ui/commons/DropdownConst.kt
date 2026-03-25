package org.piramalswasthya.cho.ui.commons

class DropdownConst {
    companion object {

        val careAndPreg: String = "Care in Pregnancy & Childbirth"
        val pwr: String = "Pregnant Women Registration"
        val pregnancyRegistration: String = "Pregnancy Registration"
        val anc: String = "ANC"
        val pnc: String = "PNC"
        val deliveryOutcome: String = "Delivery Outcome"

        val fpAndOtherRep: String = "Family Planning, Contraceptives Services & other Reproductive Health Care Services"
        val fpAndCs: String = "Eligible couple tracking"
        val ncdScreening: String = "NCD screening"

        val neonatalAndInfant: String = "Neonatal & Infant Health"
        val immunization: String = "Immunization Services"
        val ophthalmic: String = "Ophthalmic"
        val screening: String = "Screening"
        val ent: String = "ENT"
        val oral: String = "Oral"
        val ear: String = "EAR"
        val nose: String = "NOSE"
        val throat: String = "THROAT"
        val dental: String = "Dental"
        val entReasons: List<String> = listOf(ear, nose, throat)
        val oralReasons: List<String> = listOf(dental)
        val oralChiefComplaints: Set<String> = setOf(
            "Dental Decay",
            "Gum diseases",
            "Irregular arrangement of teeth and jaws",
            "Abnormal growth, patch or ulcers",
            "Cleft lip/ palate",
            "Dental Fluorosis",
            "Dental Emergencies"
        )

        val elderlyAndPalliative: String = "Elderly & Palliative"
        val persistentPain: String = "Persistent pain"
        val psychosocialCaregiverSupport: String = "Psychosocial Caregiver Support"
        val mentalHealth: String = "Mental Health"
        val mentalHealthScreening: String = "Mental Health Screening"





        val male_ncd: List<String> = listOf(ncdScreening, ophthalmic, ent, oral, elderlyAndPalliative, mentalHealth)
        val female_1_to_59: List<String> = listOf(careAndPreg, fpAndOtherRep, ophthalmic, ent, oral, elderlyAndPalliative, mentalHealth)
        val female_15_to_18: List<String> = listOf(careAndPreg, fpAndOtherRep, immunization, ophthalmic, ent, oral, elderlyAndPalliative, mentalHealth)
        val female_ncd: List<String> = listOf(careAndPreg, fpAndOtherRep, ncdScreening, ophthalmic, ent, oral, elderlyAndPalliative, mentalHealth)

        val male_elderly: List<String> = listOf(ncdScreening, ophthalmic, ent, oral, elderlyAndPalliative, mentalHealth)
        val female_elderly: List<String> = listOf(careAndPreg, fpAndOtherRep, ncdScreening, ophthalmic, ent, oral, elderlyAndPalliative, mentalHealth)
        val age_0_to_1: List<String> = listOf(neonatalAndInfant, ent, oral, elderlyAndPalliative)

        val visualAcuityList = listOf("6/6", "6/9", "6/12", "6/18", "6/24", "6/36", "6/60", "<6/60")
        val visualImpairmentList = listOf("6/18", "6/24", "6/36", "6/60", "<6/60")
        val nearVisualAcuityList = listOf("N6", "N8", "N10", "N12")
        val nearVAReducedList = listOf("N8", "N10", "N12")

        const val CHART_SNELLENS = "Snellen's distance chart"
        const val CHART_NEAR_VISION = "Near vision chart"
        val visualAcuityChartList = listOf(CHART_SNELLENS, CHART_NEAR_VISION)

        const val REASON_SYMPTOMATIC = "Symptomatic"
        const val REASON_FIRST_AID_INJURY_TRAUMA = "First aid for eye injury/ trauma"
        const val REASON_FIRST_AID_EYE_INJURY = REASON_FIRST_AID_INJURY_TRAUMA
        val ophthalmicReasonForVisitList = listOf(
            screening,
            REASON_SYMPTOMATIC,
            REASON_FIRST_AID_INJURY_TRAUMA
        )

        const val CONDITION_CATARACT = "Cataract"
        const val CONDITION_GLAUCOMA = "Glaucoma"
        const val CONDITION_DIABETIC_RETINOPATHY = "Diabetic retinopathy"
        const val CONDITION_PRESBYOPIA = "Presbyopia"
        const val CONDITION_TRACHOMA = "Trachoma"
        const val CONDITION_CORNEAL_DISEASE = "Corneal disease"
        const val CONDITION_CONJUNCTIVITIS = "Conjunctivitis/Acute red eye"
        const val CONDITION_DRY_EYE = "Dry eye / xerophthalmia"
        const val CONDITION_DRY_EYE_ALT = "Dry eye/ xerophthalmia"
        const val CONDITION_EYE_ALLERGY = "Eye allergy"

        val caseIdConditionsList = listOf(
            CONDITION_CATARACT,
            CONDITION_GLAUCOMA,
            CONDITION_DIABETIC_RETINOPATHY,
            CONDITION_PRESBYOPIA,
            CONDITION_TRACHOMA,
            CONDITION_CORNEAL_DISEASE,
            CONDITION_CONJUNCTIVITIS,
            CONDITION_DRY_EYE,
            CONDITION_EYE_ALLERGY
        )

        const val CONDITION_EYE_INJURY_BLUNT_PENETRATING =
            "Eye injuries from blunt trauma, penetrating injury to eye,"
        const val CONDITION_EYE_INJURY_BLUNT_PENETRATING_ALT =
            "Eye injuries from blunt trauma, penetrating injury to eye"
        const val CONDITION_CHEMICAL_EXPOSURE = "Chemical exposure (acid/ alkali/other),"
        const val CONDITION_CHEMICAL_EXPOSURE_ALT = "Chemical exposure (acid/ alkali/other)"
        const val CONDITION_FOREIGN_BODY_EYE = "Foreign body lodged in the eye"

        val ophthalmicChiefComplaints: Set<String> = setOf(
            CONDITION_DIABETIC_RETINOPATHY,
            CONDITION_GLAUCOMA,
            CONDITION_CATARACT,
            CONDITION_PRESBYOPIA,
            CONDITION_TRACHOMA,
            CONDITION_CORNEAL_DISEASE,
            CONDITION_CONJUNCTIVITIS,
            CONDITION_DRY_EYE,
            CONDITION_DRY_EYE_ALT,
            CONDITION_EYE_ALLERGY,
            CONDITION_EYE_INJURY_BLUNT_PENETRATING,
            CONDITION_EYE_INJURY_BLUNT_PENETRATING_ALT,
            CONDITION_CHEMICAL_EXPOSURE,
            CONDITION_CHEMICAL_EXPOSURE_ALT,
            CONDITION_FOREIGN_BODY_EYE
        )

        const val INJURY_MECHANICAL_FOREIGN_BODY = "Mechanical foreign body"
        const val INJURY_BLUNT_TRAUMA = "Blunt trauma"
        const val INJURY_PENETRATING = "Penetrating injury suspected"
        const val INJURY_CHEMICAL = "Chemical (acid/alkali/other)"
        val injuryTypeList = listOf(
            INJURY_MECHANICAL_FOREIGN_BODY,
            INJURY_BLUNT_TRAUMA,
            INJURY_PENETRATING,
            INJURY_CHEMICAL
        )

        const val FOREIGN_BODY_NOT_ATTEMPTED = "Not attempted"
        const val FOREIGN_BODY_ATTEMPTED_CONJUNCTIVAL_SAC = "Attempted from conjunctival sac"
        const val FOREIGN_BODY_LODGED_IN_CORNEA = "Foreign body lodged in cornea"
        val foreignBodyRemovalOptions = listOf(
            FOREIGN_BODY_NOT_ATTEMPTED,
            FOREIGN_BODY_ATTEMPTED_CONJUNCTIVAL_SAC,
            FOREIGN_BODY_LODGED_IN_CORNEA
        )

        const val TRACHOMA_SUSPECTED_ACTIVE = "Suspected active trachoma"
        const val TRACHOMA_SUSPECTED_TT = "Suspected TT/TI"
        const val TRACHOMA_NONE = "No trachoma"
        val trachomaStatusList = listOf(TRACHOMA_SUSPECTED_ACTIVE, TRACHOMA_SUSPECTED_TT, TRACHOMA_NONE)

        const val CORNEAL_OPACITY = "Corneal opacity"
        const val CORNEAL_ULCER = "Corneal ulcer suspected"
        const val CORNEAL_OTHER = "Other corneal pathology"
        val cornealDiseaseTypeList = listOf(CORNEAL_OPACITY, CORNEAL_ULCER, CORNEAL_OTHER)

        val consciousnessList = mutableListOf("Conscious", "Semi Conscious", "Unconscious")
        val dangerSignList = mutableListOf("Fast Breathing", "Chest Indrawing", "Stridor", "Grunt", "Respiratory Distress", "Cold and Calm Peripheral Pulses", "Convulsions", "Hypothermia", "Delirium", "Drowsy", "Uncontrolled Bleeding", "Hematemesis", "Refusal of Fits")
        val lymphNodeList = mutableListOf("Cervical LN", "Axillary LN", "Inguinal LN", "Generalised LN")
        val lymphTypeList = mutableListOf("Soft", "Firm", "Hard", "Fluctuant", "Matting", "Fixed", "Mobile")
        val extentOfEdemaList = mutableListOf("Foot", "Leg", "Facial puffiness", "Generalised")
        val abdominalTextureList = mutableListOf("Soft", "Tense", "Rigid", "Firm")
        val liverList = mutableListOf("Non Palpable", "Just Palpable", "Enlarged")
        val spleenList = mutableListOf("Non Palpable", "Just Palpable", "Enlarged")
        val tracheaList = mutableListOf("Central", "Elevated to Right", "Elevated to Left")
        val percussionSoundsList = mutableListOf("Dull", "Stony Dull", "Resonate", "Hyper-resonate")
        val handednessList = mutableListOf("No", "Right Handed", "Left Handed")
        val jointsList = mutableListOf("Ankle", "Elbow", "Hip", "Knee", "Shoulder", "Small Joints", "Temporo-mandibular", "Wrist")
        val lateralityList = mutableListOf("Left", "Right", "Bilateral")
        val abnormalityList = mutableListOf("Swelling", "Tenderness", "Deformity", "Restriction")
        val medicalTestList = mutableListOf("Blood Glucose", "Chikungunya", "Cholesterol", "Dengue", "HBA1C",
            "Haemoglobin", "Hepatitis B", "Hepatitis C", "HIV", "Malaria Test", "RBC", "RBS", "SPo2 Test", "Syphillis", "TB", "Typhoid Test Check",
            "Uric Acid", "Urine Albumin Test", "Urine Pregnancy Rapid Test", "Urine Sugar Test", "Visual Acuty Test")
        val medicationFormsList = mutableListOf("Tablet", "Capsule", "Syrup", "Suspension", "Oral Drops",
            "Ointment", "Cream", "Lotion", "Eye Drops", "Ear Drops")
        val tabletDosageList = mutableListOf("Half Tab", "One and Half Tab", "1 Tablet", "2 Tablets")
        val medicationFrequencyList = mutableListOf(
            "1-0-0",
            "0-1-0",
            "0-0-1",
            "1-1-0",
            "1-0-1",
            "0-1-1",
            "1-1-1",
            "1-1-1-1"
        )

        val medicalReferDropdownVal = mutableListOf(
            "Select none",
            "CHC",
            "FRU",
            "Other",
            "RH",
            "SDH",
            "UPHC",
            "PHC"
        )
        val frequencyMap = mapOf(
            "1-0-0" to "Once Daily(OD)",
            "0-1-0" to "Once Daily(OD)",
            "0-0-1" to "Once Daily(OD)",
            "1-1-0" to "Twice Daily(BD)",
            "1-0-1" to "Twice Daily(BD)",
            "0-1-1" to "Twice Daily(BD)",
            "1-1-1" to "Thrice Daily (TID)",
            "1-1-1-1" to "Four Times in a Day (QID)"
        )
        val medicationRouteList = mutableListOf("Ears","Eyes/Ear","Eyes","ID","IM","IV","Local application","Nostrils","Oral","Rectal")
        val unitVal = mutableListOf("Day(s)","Month(s)","Week(s)")
        val instructionDropdownList = mutableListOf("After Food","Before Food")

        val mutualVisitUnitsVal = mutableListOf("Hour(s)", "Day(s)", "Week(s)", "Month(s)", "Year(s)")
    }

}
