package org.piramalswasthya.cho.ui.commons

enum class CphcFormType {
    EAR,
    NOSE,
    THROAT,
    ORAL,
    OPHTHALMIC,
    ELDERLY,
    MENTAL,
    PAIN,
    PSYCHOSOCIAL,
    NCD,
    UNKNOWN,
}

object CphcFormTypeResolver {

    fun resolve(reasonForVisit: String?, subCategory: String?): CphcFormType {
        val key = (reasonForVisit?.takeIf { it.isNotBlank() } ?: subCategory)
            ?.trim()
            ?.lowercase()
            .orEmpty()
        if (key.isEmpty()) return CphcFormType.UNKNOWN

        return when (key) {
            DropdownConst.ear.lowercase() -> CphcFormType.EAR
            DropdownConst.nose.lowercase() -> CphcFormType.NOSE
            DropdownConst.throat.lowercase() -> CphcFormType.THROAT
            DropdownConst.dental.lowercase() -> CphcFormType.ORAL
            DropdownConst.elderlyHealthAssessment.lowercase(),
            DropdownConst.functionalDeclineOrDependency.lowercase() -> CphcFormType.ELDERLY
            DropdownConst.persistentPain.lowercase(),
            DropdownConst.distressingSymptoms.lowercase() -> CphcFormType.PAIN
            DropdownConst.psychosocialCaregiverSupport.lowercase(),
            DropdownConst.caregiverSupportCounselling.lowercase() -> CphcFormType.PSYCHOSOCIAL
            DropdownConst.mentalHealth.lowercase(),
            DropdownConst.mentalHealthScreening.lowercase(),
            DropdownConst.emotionalBehaviouralConcerns.lowercase(),
            DropdownConst.substanceUseConcerns.lowercase(),
            DropdownConst.selfHarmSuicideThoughts.lowercase(),
            DropdownConst.memoryLossConfusion.lowercase(),
            DropdownConst.seizuresFitsLoc.lowercase() -> CphcFormType.MENTAL
            DropdownConst.screening.lowercase(),
            DropdownConst.REASON_SYMPTOMATIC.lowercase(),
            DropdownConst.REASON_FIRST_AID_EYE_INJURY.lowercase(),
            DropdownConst.REASON_FIRST_AID_INJURY_TRAUMA.lowercase(),
            DropdownConst.ophthalmic.lowercase() -> CphcFormType.OPHTHALMIC
            DropdownConst.ncd.lowercase(),
            DropdownConst.ncdScreening.lowercase() -> CphcFormType.NCD
            else -> CphcFormType.UNKNOWN
        }
    }
}
