package org.piramalswasthya.cho.ui.commons

class DropdownConst {
    companion object {

        val careAndPreg: String = "Care in Pregnancy & Childbirth"
        val anc: String = "ANC"
        val pnc: String = "PNC"

        val fpAndOtherRep: String = "Family Planning, Contraceptives Services & other Reproductive Health Care Services"
        val fpAndCs: String = "Eligible couple tracking"

        val neonatalAndInfant: String = "Neonatal & Infant Health"
        val immunization: String = "Immunization Services"

        val female_1_to_59: List<String> = listOf(careAndPreg, fpAndOtherRep)
        val age_0_to_1: List<String> = listOf(neonatalAndInfant)

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
        val fingerList = mutableListOf("Right Thumb","Right Index Finger","Left Thumb","Left Index Finger")
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
            "1-0-0" to "Once Daily (OD)",
            "0-1-0" to "Once Daily (OD)",
            "0-0-1" to "Once Daily (OD)",
            "1-1-0" to "Twice Daily (BD)",
            "1-0-1" to "Twice Daily (BD)",
            "0-1-1" to "Twice Daily (BD)",
            "1-1-1" to "Thrice Daily (TID)",
            "1-1-1-1" to "Four Times in a Day (QID)"
        )
        val medicationRouteList = mutableListOf("Ears","Eyes/Ear","Eyes","ID","IM","IV","Local application","Nostrils","Oral","Rectal")
        val unitVal = mutableListOf("Day(s)","Month(s)","Week(s)")
        val instructionDropdownList = mutableListOf("After Food","Before Food")

        val mutualVisitUnitsVal = mutableListOf("Hour(s)", "Day(s)", "Week(s)", "Month(s)", "Year(s)")
    }

}