package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.PainAndSymptomAssessment
import org.piramalswasthya.cho.R

class PainAndSymptomAssessmentDataset(
    private val context: Context,
    currentLanguage: Languages
) : ReferralFollowUpDataset(context, currentLanguage) {

    private lateinit var cache: PainAndSymptomAssessment

    var onShowAlert: ((String) -> Unit)? = null

    private val optionMild = context.getString(R.string.mild)
    private val optionModerate = context.getString(R.string.moderate)
    private val optionSevere = context.getString(R.string.severe)
    private val optionYes = context.getString(R.string.yes)
    private val optionNo = context.getString(R.string.no)
    private val severityOptionIds = listOf(R.string.mild, R.string.moderate, R.string.severe)
    private val painDurationOptionIds = listOf(
        R.string.pain_duration_less_than_one_month,
        R.string.pain_duration_one_to_six_months,
        R.string.pain_duration_more_than_six_months
    )
    private val basicSymptomOptionIds = listOf(
        R.string.symptom_nausea_vomiting,
        R.string.symptom_constipation,
        R.string.symptom_anxiety_restlessness,
        R.string.symptom_sleep_disturbance
    )
    private val distressingSymptomOptionIds = listOf(
        R.string.breathlessness,
        R.string.nausea,
        R.string.fatigue,
        R.string.weakness,
        R.string.other
    )

    // ---------------- Pain Severity ----------------
    private val painSeverity = FormElement(
        id = 1,
        inputType = InputType.DROPDOWN,
        title = context.getString(R.string.pain_severity),
        entries = arrayOf(optionMild, optionModerate, optionSevere),
        required = true,
        hasDependants = true,
        hasAlertError = true
    )

    // ---------------- Pain Duration ----------------
    private val painDuration = FormElement(
        id = 2,
        inputType = InputType.DROPDOWN,
        title = context.getString(R.string.pain_duration),
        entries = arrayOf(
            context.getString(R.string.pain_duration_less_than_one_month),
            context.getString(R.string.pain_duration_one_to_six_months),
            context.getString(R.string.pain_duration_more_than_six_months)
        ),
        required = true
    )

    // ---------------- Other Symptoms Present (Removed) ----------------
//    private val symptomsPresent = FormElement(
//        id = 3,
//        inputType = InputType.RADIO,
//        title = "Other symptoms present",
//        entries = arrayOf("Yes", "No"),
//        required = true,
//        hasDependants = true
//    )

    // ---------------- Other Symptoms Severity ----------------
    private val otherSymptomsSeverity = FormElement(
        id = 4,
        inputType = InputType.DROPDOWN,
        title = context.getString(R.string.other_symptoms_severity),
        entries = arrayOf(optionMild, optionModerate, optionSevere),
        required = false
    )

    // ---------------- Immediate Relief ----------------
    private val immediateReliefProvided = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = context.getString(R.string.immediate_relief_provided),
        entries = arrayOf(optionYes, optionNo),
        required = true
    )
    private val sectionCHeadline = FormElement(
        id = 11,
        inputType = InputType.HEADLINE,
        title = context.getString(R.string.palliative_care_identification_headline),
        required = false
    )
    private val sectionDHeadline = FormElement(
        id = 18,
        inputType = InputType.HEADLINE,
        title = context.getString(R.string.pain_symptom_assessment_palliative),
        required = false
    )

    private val persistentPainPresent = FormElement(
        id = 12,
        inputType = InputType.RADIO,
        title = context.getString(R.string.persistent_pain_present),
        entries = arrayOf(optionYes, optionNo),
        required = true,
        hasDependants = true
    )



    private val basicSymptoms = FormElement(
        id = 24,
        inputType = InputType.CHECKBOXES,
        title = context.getString(R.string.symptom_assessment_basic_field_title),
        entries = arrayOf(
            context.getString(R.string.symptom_nausea_vomiting),
            context.getString(R.string.symptom_constipation),
            context.getString(R.string.symptom_anxiety_restlessness),
            context.getString(R.string.symptom_sleep_disturbance)
        ),
        required = false
    )


    private val basicSymptomReliefProvided = FormElement(
        id = 26,
        inputType = InputType.RADIO,
        title = context.getString(R.string.basic_symptom_relief_provided),
        entries = arrayOf(optionYes, optionNo),
        required = false
    )

    private val basicPsychosocialSupportProvided = FormElement(
        id = 27,
        inputType = InputType.RADIO,
        title = context.getString(R.string.basic_psychosocial_support_provided),
        entries = arrayOf(optionYes, optionNo),
        required = false
    )

    private val basicCaregiverCounsellingProvided = FormElement(
        id = 28,
        inputType = InputType.RADIO,
        title = context.getString(R.string.basic_caregiver_counselling_provided),
        entries = arrayOf(optionYes, optionNo),
        required = false
    )

    private val basicManagementRemarks = FormElement(
        id = 29,
        inputType = InputType.EDIT_TEXT,
        title = context.getString(R.string.basic_management_remarks),
        required = false,
        etMaxLength = 250,
        multiLine = true
    )

    private val distressingSymptoms = FormElement(
        id = 14,
        inputType = InputType.CHECKBOXES,
        title = context.getString(R.string.distressing_symptoms_present),
        entries = arrayOf(
            context.getString(R.string.breathlessness),
            context.getString(R.string.nausea),
            context.getString(R.string.fatigue),
            context.getString(R.string.weakness),
            context.getString(R.string.other)
        ),
        required = false
    )

    private val bedriddenOrSeverelyDependent = FormElement(
        id = 15,
        inputType = InputType.RADIO,
        title = context.getString(R.string.bedridden_or_severely_dependent),
        entries = arrayOf(optionYes, optionNo),
        required = true,
        hasDependants = true
    )
    private val lifeLimitingIllnessKnown = FormElement(
        id = 16,
        inputType = InputType.RADIO,
        title = context.getString(R.string.life_limiting_illness_known),
        entries = arrayOf(optionYes, optionNo),
        required = true,
        hasDependants = true
    )

    private val caregiverSupportRequired = FormElement(
        id = 17,
        inputType = InputType.RADIO,
        title = context.getString(R.string.caregiver_support_required),
        entries = arrayOf(optionYes, optionNo),
        required = true,
        hasDependants = true
    )
    // ---------------- Section F: Referral & Follow-up ----------------

    override val referralRequired = createReferralRequired(6)

    override val referralLevel = createReferralLevel(7)

    override val reasonForReferral = createReasonForReferral(8)

    override val followUpRequired = createFollowUpRequired(9)

    override val followUpDate = createFollowUpDate(10)
    override val caseStatus = createCaseStatus(20)
    override val dateOfDeath = createDateOfDeath(21)
    override val remarks = createRemarks(22)

    // ---------------- Setup Page ----------------
    suspend fun setUpPage(savedRecord: PainAndSymptomAssessment?) {
        cache = savedRecord ?: createDefaultCache()
        populateFromCache(cache)

        val list = mutableListOf<FormElement>()
        // Section C: Palliative Care Identification
        list.add(sectionCHeadline)
        list.add(persistentPainPresent)
        list.add(basicSymptoms)
        list.add(basicSymptomReliefProvided)
        list.add(basicPsychosocialSupportProvided)
        list.add(basicCaregiverCounsellingProvided)
        list.add(basicManagementRemarks)
        list.add(distressingSymptoms)
        list.add(bedriddenOrSeverelyDependent)
        list.add(lifeLimitingIllnessKnown)
        list.add(caregiverSupportRequired)

        // Section D: Pain & Symptom Assessment – if any Section C field is affirmative OR migrated record has saved pain data
        if (shouldShowPainAssessment() || hasSavedPainAssessment()) {
            list.addAll(getPainAssessmentFields())
        }
        // Section F
        addReferralFollowUpElements(list)

        setUpPage(list)
    }

    // ---------------- Value Change Handler ----------------
    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        val referralFollowUpResult = handleReferralFollowUpChange(formId, index)
        if (referralFollowUpResult != -1) return referralFollowUpResult

        return when (formId) {
            persistentPainPresent.id,
            distressingSymptoms.id,
            bedriddenOrSeverelyDependent.id,
            lifeLimitingIllnessKnown.id,
            caregiverSupportRequired.id -> {
                val isShown = getFormList().any { it.id == sectionDHeadline.id }
                val shouldShow = shouldShowPainAssessment()

                if (shouldShow && !isShown) {
                    triggerDependants(
                        source = caregiverSupportRequired,
                        addItems = getPainAssessmentFields(),
                        removeItems = emptyList()
                    )
                } else if (!shouldShow && isShown) {
                    painSeverity.value = null
                    painDuration.value = null
                    otherSymptomsSeverity.value = null
                    immediateReliefProvided.value = null
                    triggerDependants(
                        source = caregiverSupportRequired,
                        addItems = emptyList(),
                        removeItems = getPainAssessmentFields()
                    )
                }
                formId
            }
            painSeverity.id -> {
                if (painSeverity.value == optionSevere) {
                    onShowAlert?.invoke(
                        context.getString(R.string.severe_pain_referral_alert)
                    )
                }
                -1
            }

            otherSymptomsSeverity.id -> {
                if (otherSymptomsSeverity.value == optionSevere) {
                    onShowAlert?.invoke(
                        context.getString(R.string.severe_symptoms_referral_alert)
                    )
                }
                -1
            }

            else -> -1
        }
    }

    // ---------------- Cache Helpers ----------------
    private fun createDefaultCache(): PainAndSymptomAssessment {
        return PainAndSymptomAssessment(
            patientID = "",
            benVisitNo = null
        )
    }

    private fun getLocalizedOptionValue(entry: String?, optionIds: List<Int>): String? {
        entry ?: return null
        optionIds.forEach { id ->
            val englishValue = englishResources.getString(id)
            val localizedValue = resources.getString(id)
            if (entry == englishValue || entry == localizedValue) return localizedValue
        }
        return entry
    }

    private fun getEnglishOptionValue(entry: String?, optionIds: List<Int>): String? {
        entry ?: return null
        optionIds.forEach { id ->
            val englishValue = englishResources.getString(id)
            val localizedValue = resources.getString(id)
            if (entry == localizedValue || entry == englishValue) return englishValue
        }
        return entry
    }

    private fun getLocalizedCsvValues(entry: String?, optionIds: List<Int>): String? {
        return entry
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.map { getLocalizedOptionValue(it, optionIds) ?: it }
            ?.joinToString(",")
            ?.takeIf { it.isNotBlank() }
    }

    private fun getEnglishCsvValues(entry: String?, optionIds: List<Int>): String? {
        return entry
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.map { getEnglishOptionValue(it, optionIds) ?: it }
            ?.joinToString(",")
            ?.takeIf { it.isNotBlank() }
    }

    private fun populateFromCache(cache: PainAndSymptomAssessment) {
        // Section C
        persistentPainPresent.value = when (cache.persistentPainPresent) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
        basicSymptoms.value = getLocalizedCsvValues(cache.basicSymptomsSelected, basicSymptomOptionIds)
        basicSymptomReliefProvided.value = when (cache.basicSymptomReliefProvided) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
        basicPsychosocialSupportProvided.value = when (cache.basicPsychosocialSupportProvided) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
        basicCaregiverCounsellingProvided.value = when (cache.basicCaregiverCounsellingProvided) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
        basicManagementRemarks.value = cache.basicManagementRemarks
        distressingSymptoms.value = getLocalizedCsvValues(cache.distressingSymptoms, distressingSymptomOptionIds)
        bedriddenOrSeverelyDependent.value = when (cache.bedriddenOrSeverelyDependent) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
        lifeLimitingIllnessKnown.value = when (cache.lifeLimitingIllnessKnown) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
        caregiverSupportRequired.value = when (cache.caregiverSupportRequired) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
        painSeverity.value = getLocalizedOptionValue(cache.painSeverity, severityOptionIds)
        painDuration.value = getLocalizedOptionValue(cache.painDuration, painDurationOptionIds)
//        symptomsPresent.value = when (cache.symptomsPresent) {
//            true -> "Yes"
//            false -> "No"
//            else -> null
//        }
        otherSymptomsSeverity.value = getLocalizedOptionValue(cache.otherSymptomsSeverity, severityOptionIds)
        immediateReliefProvided.value = when (cache.immediateReliefProvided) {
            true -> optionYes
            false -> optionNo
            else -> null
        }

        // Section F
        populateReferralFollowUpFromCache(cache)
    }

    private fun getPainAssessmentFields(): List<FormElement> {
        return listOf(sectionDHeadline, painSeverity, painDuration, otherSymptomsSeverity, immediateReliefProvided)
    }

    private fun shouldShowPainAssessment(): Boolean {
        return listOf(
            persistentPainPresent,
            bedriddenOrSeverelyDependent,
            lifeLimitingIllnessKnown,
            caregiverSupportRequired
        ).any { it.value == optionYes } || !distressingSymptoms.value.isNullOrBlank()
    }

    /** Returns true when a migrated record has saved Section D values but NULL Section C triggers. */
    private fun hasSavedPainAssessment(): Boolean {
        return listOf(
            painSeverity.value,
            painDuration.value,
            otherSymptomsSeverity.value,
            immediateReliefProvided.value
        ).any { !it.isNullOrBlank() }
    }

    // ---------------- Map Values ----------------
    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as PainAndSymptomAssessment).let {
            // Section C – preserve null for unanswered radios (unanswered ≠ "No")
            it.persistentPainPresent = when (persistentPainPresent.value) {
                optionYes -> true
                optionNo -> false
                else -> null
            }

            it.basicSymptomsSelected = getEnglishCsvValues(basicSymptoms.value, basicSymptomOptionIds)

            it.basicSymptomReliefProvided = when (basicSymptomReliefProvided.value) {
                optionYes -> true
                optionNo -> false
                else -> null
            }
            it.basicPsychosocialSupportProvided = when (basicPsychosocialSupportProvided.value) {
                optionYes -> true
                optionNo -> false
                else -> null
            }
            it.basicCaregiverCounsellingProvided = when (basicCaregiverCounsellingProvided.value) {
                optionYes -> true
                optionNo -> false
                else -> null
            }
            it.basicManagementRemarks = basicManagementRemarks.value?.trim()?.takeIf { v -> v.isNotEmpty() }

            it.distressingSymptoms = getEnglishCsvValues(distressingSymptoms.value, distressingSymptomOptionIds)

            it.bedriddenOrSeverelyDependent = when (bedriddenOrSeverelyDependent.value) {
                optionYes -> true
                optionNo -> false
                else -> null
            }
            it.lifeLimitingIllnessKnown = when (lifeLimitingIllnessKnown.value) {
                optionYes -> true
                optionNo -> false
                else -> null
            }
            it.caregiverSupportRequired = when (caregiverSupportRequired.value) {
                optionYes -> true
                optionNo -> false
                else -> null
            }

            // Derived Section C flags
            it.painAssessmentEnabled = shouldShowPainAssessment() || hasSavedPainAssessment()
            it.palliativeCareEligible = listOf(
                it.persistentPainPresent,
                it.bedriddenOrSeverelyDependent,
                it.lifeLimitingIllnessKnown,
                it.caregiverSupportRequired
            ).any { flag -> flag == true } || !distressingSymptoms.value.isNullOrBlank()

            it.painSeverity = getEnglishOptionValue(painSeverity.value, severityOptionIds)
            it.painDuration = getEnglishOptionValue(painDuration.value, painDurationOptionIds)

//            it.symptomsPresent = when (symptomsPresent.value) {
//                "Yes" -> true
//                "No" -> false
//                else -> null
//            }

            it.otherSymptomsSeverity = getEnglishOptionValue(otherSymptomsSeverity.value, severityOptionIds)

            it.immediateReliefProvided = when (immediateReliefProvided.value) {
                optionYes -> true
                optionNo -> false
                else -> null
            }

            // Section F
            mapReferralFollowUpValues(it)
        }
    }
}
