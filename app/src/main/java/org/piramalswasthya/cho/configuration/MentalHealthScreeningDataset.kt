package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.MentalHealthScreeningCache
import org.piramalswasthya.cho.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class MentalHealthScreeningDataset(
    private val context: Context,
    currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private lateinit var phq9Options: Array<String>
    private lateinit var substanceFrequencyOptions: Array<String>
    private lateinit var suicideRiskOptions: Array<String>
    private lateinit var epilepsyDurationOptions: Array<String>
    private lateinit var yesNoOptions: Array<String>
    private lateinit var alcoholSystemActionOptions: Array<String>

    private lateinit var cache: MentalHealthScreeningCache
    private var lastPhq9AlertLevel = 0
    private var lastSuicideRiskLevel = ""
    private var genderID: Int? = null
    private var age: Int? = null
    private var wasAutoReferralForced = false

    init {
        loadResourceStrings()
    }

    private fun loadResourceStrings() {
        context.resources.apply {
            phq9Options = getStringArray(R.array.phq9_options)
            substanceFrequencyOptions = getStringArray(R.array.substance_frequency_options)
            suicideRiskOptions = getStringArray(R.array.suicide_risk_options)
            epilepsyDurationOptions = getStringArray(R.array.epilepsy_duration_options)
            yesNoOptions = getStringArray(R.array.yes_no_options)
            alcoholSystemActionOptions = getStringArray(R.array.alcohol_system_action_options)
        }
    }



    private fun createYesNoRadioWithDependantsElement(elementId: Int, titleResId: Int): FormElement {
        return FormElement(
            id = elementId,
            inputType = InputType.RADIO,
            title = context.getString(titleResId),
            entries = yesNoOptions,
            required = true,
            hasDependants = true
        )
    }

    private val emotionalBehaviouralConcerns: FormElement by lazy {
        createYesNoRadioWithDependantsElement(101, R.string.emotional_behavioural_concerns)
    }

    private val substanceUseConcerns: FormElement by lazy {
        createYesNoRadioWithDependantsElement(102, R.string.substance_use_concerns)
    }

    private val selfHarmSuicideThoughts: FormElement by lazy {
        createYesNoRadioWithDependantsElement(103, R.string.self_harm_suicide_thoughts)
    }

    // Q4
    private val memoryLossConfusion: FormElement by lazy {
        createYesNoRadioWithDependantsElement(104, R.string.memory_loss_confusion)
    }

    private val seizuresFitsLoc: FormElement by lazy {
        createYesNoRadioWithDependantsElement(105, R.string.seizures_fits_loc)
    }


    private val isPostpartum: FormElement by lazy {
        FormElement(
            id = 106,
            inputType = InputType.RADIO,
            title = context.getString(R.string.postpartum_woman),
            entries = yesNoOptions,
            required = false,
            hasDependants = true
        )
    }

    private fun createHeadlineElement(elementId: Int, titleResId: Int): FormElement {
        return FormElement(
            id = elementId,
            inputType = InputType.HEADLINE,
            title = context.getString(titleResId),
            required = false
        )
    }

    private fun createTextViewElement(elementId: Int, titleStr: String): FormElement {
        return FormElement(
            id = elementId,
            inputType = InputType.TEXT_VIEW,
            title = titleStr,
            required = false
        )
    }

    private fun createDropDownElement(
        elementId: Int,
        titleStr: String,
        options: Array<String>,
        hasDependants: Boolean = false
    ): FormElement {
        return FormElement(
            id = elementId,
            inputType = InputType.DROPDOWN,
            title = titleStr,
            entries = options,
            required = false,
            hasDependants = hasDependants
        )
    }
    private val mhReferralRequired: FormElement by lazy {
        FormElement(
            id = 107,
            inputType = InputType.RADIO,
            title = context.getString(R.string.mh_referral_required_title),
            entries = yesNoOptions,
            required = true,
            hasDependants = true
        )
    }

    private val mhReferralLevel: FormElement by lazy {
        FormElement(
            id = 108,
            inputType = InputType.DROPDOWN,
            title = context.getString(R.string.mh_referral_level_title),
            entries = context.resources.getStringArray(R.array.mh_referral_level_options),
            required = true
        )
    }

    private val mhReasonForReferral: FormElement by lazy {
        FormElement(
            id = 109,
            inputType = InputType.DROPDOWN,
            title = context.getString(R.string.mh_reason_for_referral_title),
            entries = context.resources.getStringArray(R.array.mh_reason_for_referral_options),
            required = true
        )
    }

    private var mhReferralDate = FormElement(
        id = 110,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.mh_referral_date_title),
        required = false
    )

    // ── Follow-up & Closure fields ───────────────────────────────────
    private val mhFollowUpRequired: FormElement by lazy {
        FormElement(
            id = 111,
            inputType = InputType.RADIO,
            title = context.getString(R.string.mh_follow_up_required_title),
            entries = yesNoOptions,
            required = true,
            hasDependants = true
        )
    }

    private val mhFollowUpDate: FormElement by lazy {
        FormElement(
            id = 112,
            inputType = InputType.DATE_PICKER,
            title = context.getString(R.string.mh_follow_up_date_title),
            required = true
        )
    }

    private val mhImprovementNoted: FormElement by lazy {
        FormElement(
            id = 113,
            inputType = InputType.RADIO,
            title = context.getString(R.string.mh_improvement_noted_title),
            entries = context.resources.getStringArray(R.array.mh_improvement_noted_options),
            required = true
        )
    }

    private val mhRepeatPhq9Header: FormElement by lazy {
        FormElement(
            id = 114,
            inputType = InputType.HEADLINE,
            title = context.getString(R.string.mh_repeat_phq9_title),
            required = false
        )
    }

    private val mhReferralEscalation: FormElement by lazy {
        FormElement(
            id = 115,
            inputType = InputType.RADIO,
            title = context.getString(R.string.mh_referral_escalation_title),
            entries = yesNoOptions,
            required = true
        )
    }
    private val mhCaseClosureReason: FormElement by lazy {
        FormElement(
            id = 116,
            inputType = InputType.DROPDOWN,
            title = context.getString(R.string.mh_case_closure_reason_title),
            entries = context.resources.getStringArray(R.array.mh_case_closure_reason_options),
            required = true
        )
    }

    private val phq9Header: FormElement by lazy {
        createHeadlineElement(200, R.string.phq9_section_title)
    }

    private fun createPhq9RadioElement(elementId: Int, titleResId: Int): FormElement {
        return FormElement(
            id = elementId,
            inputType = InputType.RADIO,
            title = context.getString(titleResId),
            entries = phq9Options,
            required = true,
            hasDependants = true
        )
    }

    private val phq9LittleInterest: FormElement by lazy {
        createPhq9RadioElement(201, R.string.phq9_little_interest)
    }

    private val phq9FeelingDown: FormElement by lazy {
        createPhq9RadioElement(202, R.string.phq9_feeling_down)
    }

    private val phq9SleepTrouble: FormElement by lazy {
        createPhq9RadioElement(203, R.string.phq9_sleep_trouble)
    }

    private val phq9FeelingTired: FormElement by lazy {
        createPhq9RadioElement(204, R.string.phq9_feeling_tired)
    }

    private val phq9Appetite: FormElement by lazy {
        createPhq9RadioElement(205, R.string.phq9_appetite)
    }

    private val phq9FeelingBad: FormElement by lazy {
        createPhq9RadioElement(206, R.string.phq9_feeling_bad)
    }

    private val phq9Concentration: FormElement by lazy {
        createPhq9RadioElement(207, R.string.phq9_concentration)
    }

    private val phq9MovingSlowly: FormElement by lazy {
        createPhq9RadioElement(208, R.string.phq9_moving_slowly)
    }

    private val phq9SelfHarmThoughts: FormElement by lazy {
        createPhq9RadioElement(209, R.string.phq9_self_harm_thoughts)
    }

    private var phq9TotalScore = createTextViewElement(210, context.getString(R.string.phq9_total_score))

    private var phq9DepressionSeverity = createTextViewElement(211, "Depression Severity")

    private var phq9SystemAction = createTextViewElement(212, "System Action")

    private val substanceHeader: FormElement by lazy {
        createHeadlineElement(300, R.string.substance_section_title)
    }

    private val substanceTobaccoHeader: FormElement by lazy {
        createHeadlineElement(312, R.string.substance_tobacco_title)
    }

    private val substanceAlcoholHeader: FormElement by lazy {
        createHeadlineElement(313, R.string.substance_alcohol_title)
    }
    private val substanceCurrentTobaccoUse = FormElement(
        id = 307,
        inputType = InputType.RADIO,
        title = "Current tobacco use",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val substanceTobaccoType = FormElement(
        id = 308,
        inputType = InputType.DROPDOWN,
        title = "Type of tobacco use",
        entries = arrayOf("Smoking", "Smokeless", "Both"),
        required = true
    )

    private val substanceTobaccoFrequency = FormElement(
        id = 309,
        inputType = InputType.DROPDOWN,
        title = "Frequency of tobacco use",
        entries = arrayOf("Occasional", "Daily"),
        required = true
    )

    private var substanceTobaccoOutcome = createTextViewElement(310, "Tobacco use outcome")

    private var substanceSystemAction = createTextViewElement(311, "System action")

    private val substanceAlcoholUse: FormElement by lazy {
        FormElement(
            id = 301,
            inputType = InputType.RADIO,
            title = context.getString(R.string.substance_alcohol_use),
            entries = yesNoOptions,
            required = true,
            hasDependants = true
        )
    }

    private val substanceTobaccoUse: FormElement by lazy {
        FormElement(
            id = 302,
            inputType = InputType.RADIO,
            title = context.getString(R.string.substance_tobacco_use),
            entries = yesNoOptions,
            required = true
        )
    }

    private val substance_alcohol_loss: FormElement by lazy {
        createYesNoRadioWithDependantsElement(303, R.string.substance_alcohol_loss)
    }

    private val substanceAlcoholImpact: FormElement by lazy {
        createYesNoRadioWithDependantsElement(314, R.string.substance_alcohol_impact)
    }

    private val substanceAlcoholWithdrawal: FormElement by lazy {
        createYesNoRadioWithDependantsElement(315, R.string.substance_alcohol_withdrawal)
    }

    private val substanceAlcoholProblematic: FormElement by lazy {
        createYesNoRadioWithDependantsElement(316, R.string.substance_alcohol_problematic)
    }

    private val substanceOtherSpecify: FormElement by lazy {
        FormElement(
            id = 304,
            inputType = InputType.EDIT_TEXT,
            title = context.getString(R.string.substance_other_specify),
            required = true,
            etMaxLength = 200,
            etInputType = android.text.InputType.TYPE_CLASS_TEXT
        )
    }

    private val substanceAlcoholClassification = createTextViewElement(317, context.getString(R.string.substance_alcohol_classification))

    private val substanceAlcoholSystemAction by lazy {
        createDropDownElement(318, context.getString(R.string.substance_alcohol_system_action), alcoholSystemActionOptions, hasDependants = true)
    }

    private val substance_alcohol_frequency: FormElement by lazy {
        FormElement(
            id = 305,
            inputType = InputType.DROPDOWN,
            title = context.getString(R.string.substance_alcohol_frequency),
            entries = substanceFrequencyOptions,
            required = true
        )
    }

    private val briefInterventionGiven: FormElement by lazy {
        FormElement(
            id = 306,
            inputType = InputType.RADIO,
            title = context.getString(R.string.brief_intervention_given),
            entries = yesNoOptions,
            required = true
        )
    }



    private val suicideHeader: FormElement by lazy {
        createHeadlineElement(400, R.string.suicide_section_title)
    }

    private val suicideCurrentThoughts: FormElement by lazy {
        createYesNoRadioWithDependantsElement(401, R.string.suicide_current_thoughts)
    }

    private val suicidePlan: FormElement by lazy {
        createYesNoRadioWithDependantsElement(402, R.string.suicide_plan)
    }

    private val suicidePreviousAttempt: FormElement by lazy {
        createYesNoRadioWithDependantsElement(403, R.string.suicide_previous_attempt)
    }

    private val suicideHopelessness: FormElement by lazy {
        createYesNoRadioWithDependantsElement(404, R.string.suicide_hopelessness)
    }

    private var suicideRiskLevel = createTextViewElement(405, context.getString(R.string.suicide_risk_level))

    private val suicideImmediateAssess: FormElement by lazy {
        createYesNoRadioWithDependantsElement(406, R.string.suicide_immediate_assess)
    }



    private val dementiaHeader: FormElement by lazy {
        createHeadlineElement(500, R.string.dementia_section_title)
    }

    private fun createEpilepsyDementiaRadioElement(elementId: Int, titleResId: Int): FormElement {
        return FormElement(
            id = elementId,
            inputType = InputType.RADIO,
            title = context.getString(titleResId),
            entries = yesNoOptions,
            required = true
        )
    }

    private val dementiaProgressiveMemoryLoss: FormElement by lazy {
        createEpilepsyDementiaRadioElement(501, R.string.dementia_progressive_memory_loss)
    }

    private val dementiaForgettingRecent: FormElement by lazy {
        createEpilepsyDementiaRadioElement(502, R.string.dementia_forgetting_recent)
    }

    private val dementiaDisorientation: FormElement by lazy {
        createEpilepsyDementiaRadioElement(503, R.string.dementia_disorientation)
    }

    private val dementiaDailyActivities: FormElement by lazy {
        createEpilepsyDementiaRadioElement(504, R.string.dementia_daily_activities)
    }

    private val dementiaBehaviouralChanges: FormElement by lazy {
        createEpilepsyDementiaRadioElement(505, R.string.dementia_behavioural_changes)
    }



    private val epilepsyHeader: FormElement by lazy {
        createHeadlineElement(600, R.string.epilepsy_section_title)
    }

    private val epilepsyRecurrentSeizures: FormElement by lazy {
        createEpilepsyDementiaRadioElement(601, R.string.epilepsy_recurrent_seizures)
    }

    private val epilepsyJerkyMovements: FormElement by lazy {
        createEpilepsyDementiaRadioElement(602, R.string.epilepsy_jerky_movements)
    }

    private val epilepsyTongueBite: FormElement by lazy {
        createEpilepsyDementiaRadioElement(603, R.string.epilepsy_tongue_bite)
    }

    private val epilepsyConfusionAfter: FormElement by lazy {
        createEpilepsyDementiaRadioElement(604, R.string.epilepsy_confusion_after)
    }

    private val epilepsyLocDuration: FormElement by lazy {
        FormElement(
            id = 605,
            inputType = InputType.DROPDOWN,
            title = context.getString(R.string.epilepsy_loc_duration),
            entries = epilepsyDurationOptions,
            required = true
        )
    }
    private val edChecklistHeader: FormElement by lazy {
        createHeadlineElement(700, R.string.epilepsy_dementia_checklist_title)
    }


    private fun createEdCheckboxElement(elementId: Int, titleResId: Int): FormElement {
        return FormElement(
            id = elementId,
            inputType = InputType.CHECKBOXES,
            title = context.getString(titleResId),
            entries = arrayOf(yesNoOptions[0]),
            required = false
        )
    }

    private val edRecurrentJerkyMovements: FormElement by lazy {
        createEdCheckboxElement(701, R.string.ed_recurrent_jerky_movements)
    }

    private val edProgressiveMemoryLoss: FormElement by lazy {
        createEdCheckboxElement(702, R.string.ed_progressive_memory_loss)
    }

    private val edConfusionDisorientation: FormElement by lazy {
        createEdCheckboxElement(703, R.string.ed_confusion_disorientation)
    }

    private val edFunctionalDecline: FormElement by lazy {
        createEdCheckboxElement(704, R.string.ed_functional_decline)
    }

    private val edAge60Years: FormElement by lazy {
        FormElement(
            id = 714,
            inputType = InputType.TEXT_VIEW,
            title = context.getString(R.string.ed_age_60_years),
            entries = yesNoOptions,
            required = false,
            isEnabled = false
            
        )
    }

    private var edScreeningOutcome = createTextViewElement(705, context.getString(R.string.ed_screening_outcome))

    private var edReferralRequired = createTextViewElement(706, context.getString(R.string.ed_referral_required))
    private var edReason = createTextViewElement(715, context.getString(R.string.ed_reason))
    private val edRecurrentEpisodeloss: FormElement by lazy {
        createEdCheckboxElement(707, R.string.ed_edRecurrentEpisodeloss)
    }
    private val edConfusionordrowsiness: FormElement by lazy {
        createEdCheckboxElement(713, R.string.ed_edConfusionordrowsiness)
    }

    private val edPsychosocialIntervention: FormElement by lazy {
        FormElement(
            id = 708,
            inputType = InputType.RADIO,
            title = context.getString(R.string.ed_psychosocial_intervention_provided),
            entries = yesNoOptions,
            required = true,
            hasDependants = true
        )
    }

    private val edInterventionType: FormElement by lazy {
        FormElement(
            id = 709,
            inputType = InputType.CHECKBOXES,
            title = context.getString(R.string.ed_intervention_type),
            entries = arrayOf("Psychoeducation", "Counselling", "Stress management", "Family counselling"),
            required = true
        )
    }

    private val edSessionDate: FormElement by lazy {
        FormElement(
            id = 710,
            inputType = InputType.DATE_PICKER,
            title = context.getString(R.string.ed_session_date),
            required = true
        )
    }

    private val edDurationMinutes: FormElement by lazy {
        FormElement(
            id = 711,
            inputType = InputType.EDIT_TEXT,
            title = context.getString(R.string.ed_duration_minutes),
            required = true,
            etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
            min = 10,
            max = 60
        )
    }

    private val edRemarks: FormElement by lazy {
        FormElement(
            id = 712,
            inputType = InputType.EDIT_TEXT,
            title = context.getString(R.string.ed_remarks),
            required = false,
            etMaxLength = 250
        )
    }




    // ── Setup Page ───────────────────────────────────────────────────

    suspend fun setUpPage(
        savedRecord: MentalHealthScreeningCache?,
        isPostpartumFromRmncha: Boolean? = null,
        genderID: Int? = null,
        age: Int? = null
    ) {
        this.genderID = genderID
        this.age = age
        cache = savedRecord ?: MentalHealthScreeningCache(
            patientID = "",
            benVisitNo = null
        )

        cache.isPostpartum = if (genderID == 2 && age in 15..49) isPostpartumFromRmncha else null

        populateFromCache(cache)

        setUpPage(buildFormElementList())
    }

    private fun shouldShowPhq9(): Boolean {
        return isYes(emotionalBehaviouralConcerns.value) ||
                isYes(selfHarmSuicideThoughts.value) ||
                cache.isPostpartum == true || isYes(isPostpartum.value)
    }
    private fun todayDateString(): String =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    private fun shouldAutoSuggestReferral(): Boolean {
        return dementiaDailyActivities.value == yesNoOptions[0] ||
                substanceAlcoholClassification.value == "Problematic"
    }

    private fun shouldShowReferralRequiredField(): Boolean {
        return isYes(emotionalBehaviouralConcerns.value) ||
                isYes(substanceUseConcerns.value) ||
                isYes(selfHarmSuicideThoughts.value) ||
                isYes(memoryLossConfusion.value) ||
                isYes(seizuresFitsLoc.value) ||
                shouldAutoReferFromPhq9() ||
                shouldAutoReferFromHighSuicideRisk()
    }

    private fun isEdChecklistVisible(): Boolean {
        return memoryLossConfusion.value == "Yes" || seizuresFitsLoc.value == "Yes"
    }

    private fun shouldAutoReferUnder11(): Boolean {
        val isUnder11 = (age ?: Int.MAX_VALUE) < 11
        if (!isUnder11) return false

        return isYes(emotionalBehaviouralConcerns.value) ||
                isYes(substanceUseConcerns.value) ||
                isYes(selfHarmSuicideThoughts.value) ||
                isYes(memoryLossConfusion.value) ||
                isYes(seizuresFitsLoc.value)
    }

    private fun shouldAutoReferFromPhq9(): Boolean {
        return (phq9TotalScore.value?.toIntOrNull() ?: 0) >= 15
    }

    private fun shouldAutoReferFromHighSuicideRisk(): Boolean {
        val highRiskLabel = suicideRiskOptions.getOrElse(2) { "High" }
        return isYes(selfHarmSuicideThoughts.value) &&
                (suicideRiskLevel.value == highRiskLabel || suicideRiskLevel.value.equals("High", ignoreCase = true))
    }

    private fun shouldForceAutoReferralRequired(): Boolean {
        return shouldAutoReferUnder11() ||
                shouldAutoReferFromPhq9() ||
                shouldAutoReferFromHighSuicideRisk()
    }

    private fun applyAutoReferralRules() {
        val isAutoReferralForced = shouldForceAutoReferralRequired()
        if (isAutoReferralForced) {
            mhReferralRequired.value = yesNoOptions[0]
            if (mhReferralDate.value.isNullOrEmpty()) {
                mhReferralDate.value = todayDateString()
            }
        } else if (wasAutoReferralForced) {
            // Auto-force removed (e.g. suicide risk High -> Medium): clear stale forced values
            // so UI updates immediately without requiring manual scroll/rebind.
            mhReferralRequired.value = null
            mhReferralLevel.value = null
            mhReasonForReferral.value = null
            mhReferralDate.value = null
        }
        wasAutoReferralForced = isAutoReferralForced
    }

    private fun updateReferralRequiredOptions() {
        mhReferralRequired.entries = if (shouldForceAutoReferralRequired()) {
            arrayOf(yesNoOptions[0])
        } else {
            yesNoOptions
        }
    }

    private fun getReferralLevelOptionsByAge(): Array<String> {
        val currentAge = age
        return when {
            currentAge != null && currentAge < 6 -> arrayOf("RBSK DEIC")
            currentAge != null && currentAge in 6..10 -> arrayOf("PHC", "CHC", "DH")
            currentAge != null && currentAge >= 11 -> arrayOf("PHC", "CHC", "DH", "DMHP", "De-addiction")
            else -> context.resources.getStringArray(R.array.mh_referral_level_options)
        }
    }

    private fun updateReferralLevelOptions() {
        val options = getReferralLevelOptionsByAge()
        mhReferralLevel.entries = options

        if (mhReferralLevel.value == "De-addiction centre" && options.contains("De-addiction")) {
            mhReferralLevel.value = "De-addiction"
        }

        if (mhReferralLevel.value != null && !options.contains(mhReferralLevel.value)) {
            mhReferralLevel.value = null
        }
    }


    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            emotionalBehaviouralConcerns.id -> {
                rebuildConditionalSections()
                formId
            }

            substanceUseConcerns.id -> {
                if (substanceUseConcerns.value != "Yes") {
                    clearSubstanceValues()
                }
                rebuildConditionalSections()
                formId
            }

            selfHarmSuicideThoughts.id -> {
                rebuildConditionalSections()
                formId
            }

            memoryLossConfusion.id -> {
                if (memoryLossConfusion.value != "Yes") {
                    clearDementiaValues()
                    if (seizuresFitsLoc.value != "Yes") clearEdChecklistValues()
                }
                rebuildConditionalSections()
                formId
            }

            seizuresFitsLoc.id -> {
                if (seizuresFitsLoc.value != "Yes") {
                    clearEpilepsyValues()
                    if (memoryLossConfusion.value != "Yes") clearEdChecklistValues()
                }
                rebuildConditionalSections()
                formId
            }
            isPostpartum.id -> {
                rebuildConditionalSections()
                formId
            }
            mhReferralRequired.id -> {
                if (mhReferralRequired.value != yesNoOptions[0]) {
                    mhReferralLevel.value = null
                    mhReasonForReferral.value = null
                    mhReferralDate.value = null
                }
                rebuildConditionalSections()
                formId
            }

            mhFollowUpRequired.id -> {
                if (mhFollowUpRequired.value != yesNoOptions[0]) {
                    mhFollowUpDate.value = null
                    mhImprovementNoted.value = null
                    mhReferralEscalation.value = null
                    mhCaseClosureReason.value = null
                }
                rebuildConditionalSections()
                formId
            }

            substanceTobaccoUse.id -> {
                rebuildConditionalSections()
                formId
            }

            substanceCurrentTobaccoUse.id -> {
                rebuildConditionalSections()
                formId
            }

            substanceAlcoholUse.id, substance_alcohol_loss.id,
            substanceAlcoholImpact.id, substanceAlcoholWithdrawal.id,
            substanceAlcoholProblematic.id, substance_alcohol_frequency.id -> {
                rebuildConditionalSections()
                formId
            }

            phq9LittleInterest.id, phq9FeelingDown.id, phq9SleepTrouble.id,
            phq9FeelingTired.id, phq9Appetite.id, phq9FeelingBad.id,
            phq9Concentration.id, phq9MovingSlowly.id, phq9SelfHarmThoughts.id -> {
                rebuildConditionalSections()
                formId
            }

            suicidePreviousAttempt.id, suicidePlan.id,
            suicideCurrentThoughts.id, suicideHopelessness.id,
            suicideImmediateAssess.id -> {
                rebuildConditionalSections()
                formId
            }

            edRecurrentEpisodeloss.id, edRecurrentJerkyMovements.id, edConfusionordrowsiness.id, edProgressiveMemoryLoss.id,
            edConfusionDisorientation.id, edFunctionalDecline.id, substanceAlcoholSystemAction.id -> {
                rebuildConditionalSections()
                formId
            }
            edPsychosocialIntervention.id -> {
                rebuildConditionalSections()
                formId
            }

            edDurationMinutes.id -> {
                validateIntMinMax(edDurationMinutes)
            }

            edSessionDate.id, edRemarks.id -> {
                formId
            }

            else -> {
                -1
            }
        }
    }


    private fun buildFormElementList(): MutableList<FormElement> {
        val list = mutableListOf<FormElement>()
        updateEdDerivedFields()
        applyAutoReferralRules()
        updateReferralRequiredOptions()
        updateReferralLevelOptions()

        // Initial screening questions
        list.add(emotionalBehaviouralConcerns)
        list.add(substanceUseConcerns)
        list.add(selfHarmSuicideThoughts)
        list.add(memoryLossConfusion)
        list.add(seizuresFitsLoc)
        if (genderID == 2 && age in 15..49) {
            list.add(isPostpartum)
        }


        // PHQ-9 section
        if (shouldShowPhq9()) {
            list.addAll(listOf(
                phq9Header, phq9LittleInterest, phq9FeelingDown, phq9SleepTrouble,
                phq9FeelingTired, phq9Appetite, phq9FeelingBad, phq9Concentration,
                phq9MovingSlowly, phq9SelfHarmThoughts,
                phq9TotalScore.copy(), phq9DepressionSeverity.copy(), phq9SystemAction.copy()
            ))
        } else {
            clearPhq9Values()
        }

        // Substance use section
        if (substanceUseConcerns.value == "Yes") {
            list.addAll(listOf(
                substanceHeader, substanceTobaccoHeader, substanceCurrentTobaccoUse,
                substanceSystemAction.copy(),
                substanceAlcoholHeader, substanceAlcoholUse, substanceAlcoholWithdrawal, substanceAlcoholProblematic,
                substanceAlcoholClassification.copy(), substanceAlcoholSystemAction
            ))
            if (substanceCurrentTobaccoUse.value == "Yes") {
                val idx = list.indexOf(substanceCurrentTobaccoUse)
                list.add(idx + 1, substanceTobaccoType)
                list.add(idx + 2, substanceTobaccoFrequency)
            }
            if (substanceAlcoholUse.value == "Yes") {
                val idx = list.indexOf(substanceAlcoholUse)
                list.add(idx + 1, substance_alcohol_frequency)
                list.add(idx + 2, substance_alcohol_loss)
                list.add(idx + 3, substanceAlcoholImpact)
            } else {
                clearAlcoholSubFields()
            }
        }

        // Suicide risk section
        if (selfHarmSuicideThoughts.value == "Yes") {
            list.addAll(listOf(
                suicideHeader, suicidePreviousAttempt, suicidePlan,
                suicideHopelessness, suicideImmediateAssess, suicideRiskLevel.copy()
            ))
        }

        // Epilepsy & Dementia checklist
        if (memoryLossConfusion.value == "Yes" || seizuresFitsLoc.value == "Yes") {
            list.addAll(
                listOf(
                    edChecklistHeader,
                    edRecurrentEpisodeloss,
                    edRecurrentJerkyMovements,
                    edConfusionordrowsiness,
                    edProgressiveMemoryLoss,
                    edConfusionDisorientation,
                    edFunctionalDecline,
                    edAge60Years,
                    edScreeningOutcome.copy()
                )
            )
            list.add(edReferralRequired.copy())
            list.add(edReason.copy())
        }

        // Psychosocial intervention
        if (isBriefIntervention(substanceAlcoholSystemAction.value)) {
            list.add(edPsychosocialIntervention)
            if (isYes(edPsychosocialIntervention.value)) {
                list.add(edInterventionType)
                edSessionDate.max = System.currentTimeMillis()
                list.add(edSessionDate)
                list.add(edDurationMinutes)
                list.add(edRemarks)
            } else {
                clearEdPsychosocialDependants()
            }
        } else {
            clearEdPsychosocialValues()
        }

        if (shouldShowReferralRequiredField() && !isEdChecklistVisible()) {
            if (mhReferralRequired.value == null && shouldAutoSuggestReferral()) {
                mhReferralRequired.value = yesNoOptions[0]
            }
            list.add(mhReferralRequired.copy())
            if (mhReferralRequired.value == yesNoOptions[0]) {
                list.add(mhReferralLevel.copy())
                list.add(mhReasonForReferral.copy())
                if (mhReferralDate.value.isNullOrEmpty()) {
                    mhReferralDate.value = todayDateString()
                }
                list.add(mhReferralDate.copy())
            }
        } else {
            mhReferralRequired.value = null
            mhReferralLevel.value = null
            mhReasonForReferral.value = null
            mhReferralDate.value = null
        }
//
//        // Follow-up & Closure
//        list.add(mhFollowUpRequired)
//        if (mhFollowUpRequired.value == yesNoOptions[0]) {
//            mhFollowUpDate.min = System.currentTimeMillis()
//            mhFollowUpDate.max = System.currentTimeMillis() + java.util.concurrent.TimeUnit.DAYS.toMillis(365)
//            list.add(mhFollowUpDate)
//        }
//        list.add(mhImprovementNoted)
//        if (shouldShowPhq9()) {
//            list.add(mhRepeatPhq9Header)
//        }
//        list.add(mhReferralEscalation)
//        list.add(mhCaseClosureReason)


        return list
    }


    private suspend fun rebuildConditionalSections() {

        updatePhq9Outcome()
        updateTobaccoOutcome()
        computeSuicideRiskLevel()
        computeAlcoholClassification()
        computeEdScreeningOutcome()

        setUpPage(buildFormElementList())
    }


    private suspend fun updatePhq9Outcome() {
        val score = listOfNotNull(
            extractPhq9Score(phq9LittleInterest.value),
            extractPhq9Score(phq9FeelingDown.value),
            extractPhq9Score(phq9SleepTrouble.value),
            extractPhq9Score(phq9FeelingTired.value),
            extractPhq9Score(phq9Appetite.value),
            extractPhq9Score(phq9FeelingBad.value),
            extractPhq9Score(phq9Concentration.value),
            extractPhq9Score(phq9MovingSlowly.value),
            extractPhq9Score(phq9SelfHarmThoughts.value)
        ).sum()

        // Alert Logic
        val currentLevel = when {
            score >= 20 -> 3
            score >= 15 -> 2
            score >= 10 -> 1
            else -> 0
        }

        if (currentLevel > 0 && currentLevel != lastPhq9AlertLevel) {
            val alertMsg = when (currentLevel) {
                3 -> R.string.phq9_alert_emergency
                2 -> R.string.phq9_alert_urgent
                else -> R.string.phq9_alert_referral_mo_phc
            }
            emitAlertErrorMessage(alertMsg)
        }
        lastPhq9AlertLevel = currentLevel

        phq9TotalScore.value = score.toString()
        phq9DepressionSeverity.value = when (score) {
            in 0..4 -> "Minimal"
            in 5..9 -> "Mild"
            in 10..14 -> "Moderate"
            in 15..19 -> "Moderately Severe"
            else -> "Severe"
        }
        phq9SystemAction.value = when (score) {
            in 0..9 -> "Psychoeducation"
            in 10..14 -> "Counselling - Refer to MO/PHC"
            in 15..19 -> "Referral - Urgent referral"
            else -> "Referral - Emergency referral"
        }
        phq9SystemAction.errorText = when {
            score >= 20 -> context.getString(R.string.phq9_alert_emergency)
            score >= 15 -> context.getString(R.string.phq9_alert_urgent)
            score >= 10 -> context.getString(R.string.phq9_alert_referral_mo_phc)
            else -> null
        }

    }

    private fun updateTobaccoOutcome() {
        if (substanceCurrentTobaccoUse.value != "Yes") {
            substanceTobaccoType.value = null
            substanceTobaccoFrequency.value = null
        }
        val currentUse = substanceCurrentTobaccoUse.value
        val previousUse = substanceTobaccoUse.value

        substanceTobaccoOutcome.value = when {
            currentUse == "Yes"                      -> "Use identified"
            currentUse == "No"                       -> "No use identified"
            currentUse == null && previousUse == "Yes" -> "Use identified"
            else                                     -> null
        }

        substanceSystemAction.value = when (substanceTobaccoOutcome.value) {
            "Use identified"    -> "Brief counselling"
            "No use identified" -> "Referral"
            else                -> null
        }
    }

    private fun computeAlcoholClassification() {
        val use = substanceAlcoholUse.value
        val frequency = substance_alcohol_frequency.value
        val loss = substance_alcohol_loss.value
        val impact = substanceAlcoholImpact.value
        val withdrawal = substanceAlcoholWithdrawal.value
        val problematic = substanceAlcoholProblematic.value

        if (use == null && frequency == null && loss == null && impact == null && withdrawal == null && problematic == null) {
            substanceAlcoholClassification.value = null
            return
        }

        val hasRiskFactor = loss == "Yes" ||
                impact == "Yes" ||
                withdrawal == "Yes" ||
                problematic == "Yes" ||
                frequency == "Regular" ||
                frequency == "Daily"

        val noAlcoholUse = use == "No"
        val occasionalWithNoRisks = (frequency == "Occasionally") &&
                loss != "Yes" &&
                impact != "Yes" &&
                withdrawal != "Yes"

        substanceAlcoholClassification.value = when {
            noAlcoholUse -> "Non-problematic"
            hasRiskFactor -> "Problematic"
            occasionalWithNoRisks -> "Non-problematic"
            else -> null
        }
    }

    private fun computeEdScreeningOutcome() {
        if (memoryLossConfusion.value != "Yes" && seizuresFitsLoc.value != "Yes") {
            edScreeningOutcome.value = null
            edReferralRequired.value = null
            edReason.value = null
            return
        }

        val hasRisk = isYes(edRecurrentEpisodeloss.value) || isYes(edRecurrentJerkyMovements.value) || isYes(edConfusionordrowsiness.value) ||
                isYes(edProgressiveMemoryLoss.value) ||
                isYes(edConfusionDisorientation.value) ||
                isYes(edFunctionalDecline.value)

        edScreeningOutcome.value = if (hasRisk) "Suspected" else "Not suspected"
        edReferralRequired.value = if (edScreeningOutcome.value == "Suspected") "Yes" else "No"
        edReason.value =
            if (edScreeningOutcome.value == "Suspected") "Neurological condition suspected" else null
    }




    private suspend fun computeSuicideRiskLevel() {
        val fields = listOf(
            suicidePreviousAttempt.value,
            suicidePlan.value,
            suicideHopelessness.value,
            suicideImmediateAssess.value
        )

        val answeredCount = fields.count { it != null }
        if (answeredCount == 0) {
            suicideRiskLevel.value = null
            suicideRiskLevel.hasAlertError = false
            suicideRiskLevel.errorText = null
            return
        }

        val yesCount = fields.count { isYes(it) }

        // If previous attempt AND current intent/plan are both Yes → High
        val hasPreviousAttemptAndPlan = isYes(suicidePreviousAttempt.value) && isYes(suicidePlan.value)

        suicideRiskLevel.value = when {
            hasPreviousAttemptAndPlan -> suicideRiskOptions.getOrElse(2) { "High" }
            yesCount  ==0  -> suicideRiskOptions.getOrElse(0) { "Low" }
            yesCount in 1..2 -> suicideRiskOptions.getOrElse(1) { "Moderate" }
            else             -> suicideRiskOptions.getOrElse(2) { "High" }
        }

        val riskValue = suicideRiskLevel.value ?: ""
        val moderate  = suicideRiskOptions.getOrElse(1) { "Moderate" }
        val high      = suicideRiskOptions.getOrElse(2) { "High" }

        when (riskValue) {
            high -> {
                suicideRiskLevel.hasAlertError = true
                suicideRiskLevel.errorText = context.getString(R.string.suicide_risk_alert_high)
                if (lastSuicideRiskLevel != high) {
                    emitAlertErrorMessage(R.string.suicide_risk_alert_high)
                }
            }
            moderate -> {
                suicideRiskLevel.hasAlertError = true
                suicideRiskLevel.errorText = context.getString(R.string.suicide_risk_alert_moderate)
                if (lastSuicideRiskLevel != moderate) {
                    emitAlertErrorMessage(R.string.suicide_risk_alert_moderate)
                }
            }
            else -> {
                suicideRiskLevel.hasAlertError = false
                suicideRiskLevel.errorText = null
            }
        }
        lastSuicideRiskLevel = riskValue
    }



    private fun clearPhq9Values() {
        phq9LittleInterest.value = null
        phq9FeelingDown.value = null
        phq9SleepTrouble.value = null
        phq9FeelingTired.value = null
        phq9Appetite.value = null
        phq9FeelingBad.value = null
        phq9Concentration.value = null
        phq9MovingSlowly.value = null
        phq9SelfHarmThoughts.value = null
        phq9TotalScore.value = null
        phq9DepressionSeverity.value = null
        phq9SystemAction.value = null
    }

    private fun clearSubstanceValues() {
        substanceCurrentTobaccoUse.value = null
        substanceTobaccoType.value = null
        substanceTobaccoFrequency.value = null
        substanceTobaccoOutcome.value = null
        substanceSystemAction.value = null
        substanceAlcoholUse.value = null
        substanceTobaccoUse.value = null
        clearAlcoholSubFields()
        substanceAlcoholSystemAction.value = null
        briefInterventionGiven.value = null
    }
    private fun clearEdPsychosocialValues() {
        edPsychosocialIntervention.value = null
        clearEdPsychosocialDependants()
    }

    private fun clearEdPsychosocialDependants() {
        edInterventionType.value = null
        edSessionDate.value = null
        edDurationMinutes.value = null
        edRemarks.value = null
    }

    private fun clearAlcoholSubFields() {
        substance_alcohol_frequency.value = null
        substance_alcohol_loss.value = null
        substanceAlcoholImpact.value = null
        substanceAlcoholWithdrawal.value = null
        substanceAlcoholProblematic.value = null
        substanceAlcoholClassification.value = null
    }

    private fun clearSuicideValues() {
        suicideCurrentThoughts.value = null
        suicidePlan.value = null
        suicidePreviousAttempt.value = null
        suicideHopelessness.value = null
        suicideImmediateAssess.value =null
        suicideRiskLevel.value = null

    }

    private fun clearDementiaValues() {
        dementiaProgressiveMemoryLoss.value = null
        dementiaForgettingRecent.value = null
        dementiaDisorientation.value = null
        dementiaDailyActivities.value = null
        dementiaBehaviouralChanges.value = null
    }

    private fun clearEpilepsyValues() {
        epilepsyRecurrentSeizures.value = null
        epilepsyJerkyMovements.value = null
        epilepsyTongueBite.value = null
        epilepsyConfusionAfter.value = null
        epilepsyLocDuration.value = null
    }

    private fun clearEdChecklistValues() {
        edRecurrentEpisodeloss.value = null
        edRecurrentJerkyMovements.value = null
        edConfusionordrowsiness.value = null
        edProgressiveMemoryLoss.value = null
        edConfusionDisorientation.value = null
        edFunctionalDecline.value = null
        edAge60Years.value = null
        edScreeningOutcome.value = null
        edReferralRequired.value = null
        edReason.value = null
    }

    private fun updateEdDerivedFields() {
        edAge60Years.value = if ((age ?: 0) >= 60) yesNoOptions[0] else yesNoOptions[1]
    }


    // ── Populate from Cache ──────────────────────────────────────────

    private fun populateFromCache(cache: MentalHealthScreeningCache) {
        // Initial screening questions
        emotionalBehaviouralConcerns.value = boolToYesNo(cache.emotionalBehaviouralConcerns)
        substanceUseConcerns.value = boolToYesNo(cache.substanceUseConcerns)
        selfHarmSuicideThoughts.value = boolToYesNo(cache.selfHarmSuicideThoughts)
        memoryLossConfusion.value = boolToYesNo(cache.memoryLossConfusion)
        seizuresFitsLoc.value = boolToYesNo(cache.seizuresFitsLoc)
        isPostpartum.value = boolToYesNo(cache.isPostpartum)
        // Referral
        mhReferralRequired.value =
            cache.referralRequired?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        mhReferralLevel.value = cache.referralLevel
        mhReasonForReferral.value = cache.reasonForReferral

        if (cache.referralRequired == true) {
            mhReferralDate.value = cache.referralDate ?: todayDateString()
        } else {
            mhReferralDate.value = null
        }

        // Follow-up & Closure
        mhFollowUpRequired.value =
            cache.followUpRequired?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        mhFollowUpDate.value = cache.followUpDate
        mhImprovementNoted.value = cache.improvementNoted
        mhReferralEscalation.value =
            cache.referralEscalationRequired?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        mhCaseClosureReason.value = cache.caseClosureReason

        // PHQ-9
        phq9LittleInterest.value = cache.phq9LittleInterest?.let { phq9Options.getOrNull(it) }
        phq9FeelingDown.value = cache.phq9FeelingDown?.let { phq9Options.getOrNull(it) }
        phq9SleepTrouble.value = cache.phq9SleepTrouble?.let { phq9Options.getOrNull(it) }
        phq9FeelingTired.value = cache.phq9FeelingTired?.let { phq9Options.getOrNull(it) }
        phq9Appetite.value = cache.phq9Appetite?.let { phq9Options.getOrNull(it) }
        phq9FeelingBad.value = cache.phq9FeelingBad?.let { phq9Options.getOrNull(it) }
        phq9Concentration.value = cache.phq9Concentration?.let { phq9Options.getOrNull(it) }
        phq9MovingSlowly.value = cache.phq9MovingSlowly?.let { phq9Options.getOrNull(it) }
        phq9SelfHarmThoughts.value = cache.phq9SelfHarmThoughts?.let { phq9Options.getOrNull(it) }
        phq9TotalScore.value = cache.phq9TotalScore?.toString()
        phq9DepressionSeverity.value = cache.phq9DepressionSeverity
        phq9SystemAction.value = cache.phq9SystemAction

        // Substance Use
        substanceCurrentTobaccoUse.value = boolToYesNo(cache.substanceCurrentTobaccoUse)
        substanceTobaccoType.value = cache.substanceTobaccoType
        substanceTobaccoFrequency.value = cache.substanceTobaccoFrequency
        substanceTobaccoOutcome.value = cache.substanceTobaccoOutcome
        substanceSystemAction.value = cache.substanceSystemAction
        substanceAlcoholUse.value = boolToYesNo(cache.substanceAlcoholUse)
        substanceTobaccoUse.value = boolToYesNo(cache.substanceTobaccoUse)
        substance_alcohol_loss.value = boolToYesNo(cache.substance_alcohol_loss)
        substanceAlcoholImpact.value = boolToYesNo(cache.substanceAlcoholImpact)
        substanceAlcoholWithdrawal.value = boolToYesNo(cache.substanceAlcoholWithdrawal)
        substanceAlcoholProblematic.value = boolToYesNo(cache.substanceAlcoholProblematic)
        substanceAlcoholClassification.value = cache.substanceAlcoholClassification
        substanceAlcoholSystemAction.value = cache.substanceAlcoholSystemAction
        substanceOtherSpecify.value = cache.substanceOtherSpecify
        substance_alcohol_frequency.value = cache.substance_alcohol_frequency

        // Suicide Risk
        suicideCurrentThoughts.value = boolToYesNo(cache.suicideCurrentThoughts)
        suicidePlan.value = boolToYesNo(cache.suicidePlan)
        suicidePreviousAttempt.value = boolToYesNo(cache.suicidePreviousAttempt)
        suicideHopelessness.value = boolToYesNo(cache.suicideHopelessness)
        suicideImmediateAssess.value = boolToYesNo(cache.suicideImmediateAssess)
        suicideRiskLevel.value = cache.suicideRiskLevel

        // Dementia
        dementiaProgressiveMemoryLoss.value = boolToYesNo(cache.dementiaProgressiveMemoryLoss)
        dementiaForgettingRecent.value = boolToYesNo(cache.dementiaForgettingRecent)
        dementiaDisorientation.value = boolToYesNo(cache.dementiaDisorientation)
        dementiaDailyActivities.value = boolToYesNo(cache.dementiaDailyActivities)
        dementiaBehaviouralChanges.value = boolToYesNo(cache.dementiaBehaviouralChanges)

        // Epilepsy
        epilepsyRecurrentSeizures.value = boolToYesNo(cache.epilepsyRecurrentSeizures)
        epilepsyJerkyMovements.value = boolToYesNo(cache.epilepsyJerkyMovements)
        epilepsyTongueBite.value = boolToYesNo(cache.epilepsyTongueBite)
        epilepsyConfusionAfter.value = boolToYesNo(cache.epilepsyConfusionAfter)
        epilepsyLocDuration.value = cache.epilepsyLocDuration

        // Epilepsy & Dementia Checklist
        edRecurrentEpisodeloss.value = boolToChecked(cache.edRecurrentEpisodeloss)
        edRecurrentJerkyMovements.value = boolToChecked(cache.edRecurrentJerkyMovements)
        edConfusionordrowsiness.value = boolToChecked(cache.edConfusionordrowsiness)
        edProgressiveMemoryLoss.value = boolToChecked(cache.edProgressiveMemoryLoss)
        edConfusionDisorientation.value = boolToChecked(cache.edConfusionDisorientation)
        edFunctionalDecline.value = boolToChecked(cache.edFunctionalDecline)
        edScreeningOutcome.value = cache.edScreeningOutcome
        edReferralRequired.value = cache.edReferralRequired
        edReason.value = cache.edReason
        edPsychosocialIntervention.value = boolToYesNo(cache.edPsychosocialInterventionProvided)
        edInterventionType.value = cache.edInterventionType
        edSessionDate.value = cache.edSessionDate
        edDurationMinutes.value = cache.edDurationMinutes?.toString()
        edRemarks.value = cache.edRemarks


    }

    // ── Map Values ───────────────────────────────────────────────────

    private fun extractPhq9Score(value: String?): Int? {
        return value?.firstOrNull()?.digitToIntOrNull()
    }

    private fun isYes(value: String?): Boolean {
        return value == yesNoOptions[0]
    }
    private fun isBriefIntervention(value: String?): Boolean {
        return value == alcoholSystemActionOptions.getOrNull(0)
    }

    private fun isNo(value: String?): Boolean =
        value == yesNoOptions.getOrNull(1)

    private fun yesNoToBoolean(value: String?): Boolean? =
        when {
            value == yesNoOptions.getOrNull(0) -> true
            value == yesNoOptions.getOrNull(1) -> false
            else -> null
        }

    private fun boolToYesNo(value: Boolean?): String? =
        value?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }

    private fun boolToChecked(value: Boolean?): String? =
        value?.let { if (it) yesNoOptions[0] else null }


    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as MentalHealthScreeningCache).let {
            // Initial screening
            it.emotionalBehaviouralConcerns = yesNoToBoolean(emotionalBehaviouralConcerns.value)
            it.substanceUseConcerns = yesNoToBoolean(substanceUseConcerns.value)
            it.selfHarmSuicideThoughts = yesNoToBoolean(selfHarmSuicideThoughts.value)
            it.memoryLossConfusion = yesNoToBoolean(memoryLossConfusion.value)
            it.seizuresFitsLoc = yesNoToBoolean(seizuresFitsLoc.value)
            it.isPostpartum = yesNoToBoolean(isPostpartum.value)

            // PHQ-9
            if (shouldShowPhq9()) {
                it.phq9LittleInterest = extractPhq9Score(phq9LittleInterest.value)
                it.phq9FeelingDown = extractPhq9Score(phq9FeelingDown.value)
                it.phq9SleepTrouble = extractPhq9Score(phq9SleepTrouble.value)
                it.phq9FeelingTired = extractPhq9Score(phq9FeelingTired.value)
                it.phq9Appetite = extractPhq9Score(phq9Appetite.value)
                it.phq9FeelingBad = extractPhq9Score(phq9FeelingBad.value)
                it.phq9Concentration = extractPhq9Score(phq9Concentration.value)
                it.phq9MovingSlowly = extractPhq9Score(phq9MovingSlowly.value)
                it.phq9SelfHarmThoughts = extractPhq9Score(phq9SelfHarmThoughts.value)

                // Calculate total PHQ-9 score
                it.phq9TotalScore = listOfNotNull(
                    it.phq9LittleInterest, it.phq9FeelingDown, it.phq9SleepTrouble,
                    it.phq9FeelingTired, it.phq9Appetite, it.phq9FeelingBad,
                    it.phq9Concentration, it.phq9MovingSlowly, it.phq9SelfHarmThoughts
                ).sum()
                it.phq9DepressionSeverity = phq9DepressionSeverity.value
                it.phq9SystemAction = phq9SystemAction.value
            } else {
                it.phq9LittleInterest = null
                it.phq9FeelingDown = null
                it.phq9SleepTrouble = null
                it.phq9FeelingTired = null
                it.phq9Appetite = null
                it.phq9FeelingBad = null
                it.phq9Concentration = null
                it.phq9MovingSlowly = null
                it.phq9SelfHarmThoughts = null
                it.phq9TotalScore = null
            }


            // Substance Use
            if (substanceUseConcerns.value == "Yes") {
                it.substanceCurrentTobaccoUse = substanceCurrentTobaccoUse.value == "Yes"
                it.substanceTobaccoType = if (it.substanceCurrentTobaccoUse == true) substanceTobaccoType.value else null
                it.substanceTobaccoFrequency = if (it.substanceCurrentTobaccoUse == true) substanceTobaccoFrequency.value else null
                it.substanceTobaccoOutcome = substanceTobaccoOutcome.value
                it.substanceSystemAction = substanceSystemAction.value
                it.substanceAlcoholUse = substanceAlcoholUse.value == "Yes"
                it.substanceTobaccoUse = substanceTobaccoUse.value == "Yes"
                it.substance_alcohol_loss = substance_alcohol_loss.value == "Yes"
                it.substanceAlcoholImpact = substanceAlcoholImpact.value == "Yes"
                it.substanceAlcoholWithdrawal = substanceAlcoholWithdrawal.value == "Yes"
                it.substanceAlcoholProblematic = substanceAlcoholProblematic.value == "Yes"
                it.substanceAlcoholClassification = substanceAlcoholClassification.value
                it.substanceAlcoholSystemAction = substanceAlcoholSystemAction.value
                it.substance_alcohol_frequency = substance_alcohol_frequency.value
                it.briefInterventionGiven = briefInterventionGiven.value == "Yes"
            } else {
                it.substanceCurrentTobaccoUse = null
                it.substanceTobaccoType = null
                it.substanceTobaccoFrequency = null
                it.substanceTobaccoOutcome = null
                it.substanceSystemAction = null
                it.substanceAlcoholUse = null
                it.substanceTobaccoUse = null
                it.substance_alcohol_loss = null
                it.substanceAlcoholImpact = null
                it.substanceAlcoholWithdrawal = null
                it.substanceAlcoholProblematic = null
                it.substanceAlcoholClassification = null
                it.substanceAlcoholSystemAction = null
                it.substanceOtherSpecify = null
                it.substance_alcohol_frequency = null
                it.briefInterventionGiven = null
            }


            // Suicide Risk
            if (isYes(selfHarmSuicideThoughts.value)) {
                it.suicideCurrentThoughts = isYes(suicideCurrentThoughts.value)
                it.suicidePlan = isYes(suicidePlan.value)
                it.suicidePreviousAttempt = isYes(suicidePreviousAttempt.value)
                it.suicideHopelessness = isYes(suicideHopelessness.value)
                it.suicideImmediateAssess = isYes(suicideImmediateAssess.value)
                it.suicideRiskLevel = suicideRiskLevel.value
            } else {
                it.suicideCurrentThoughts = null
                it.suicidePlan = null
                it.suicidePreviousAttempt = null
                it.suicideHopelessness = null
                it.suicideImmediateAssess = null
                it.suicideRiskLevel = null
            }

            if (isYes(memoryLossConfusion.value)) {
                it.dementiaProgressiveMemoryLoss = isYes(dementiaProgressiveMemoryLoss.value)
                it.dementiaForgettingRecent = isYes(dementiaForgettingRecent.value)
                it.dementiaDisorientation = isYes(dementiaDisorientation.value)
                it.dementiaDailyActivities = isYes(dementiaDailyActivities.value)
                it.dementiaBehaviouralChanges = isYes(dementiaBehaviouralChanges.value)
            } else {
                it.dementiaProgressiveMemoryLoss = null
                it.dementiaForgettingRecent = null
                it.dementiaDisorientation = null
                it.dementiaDailyActivities = null
                it.dementiaBehaviouralChanges = null
            }

            if (isYes(seizuresFitsLoc.value)) {
                it.epilepsyRecurrentSeizures = isYes(epilepsyRecurrentSeizures.value)
                it.epilepsyJerkyMovements = isYes(epilepsyJerkyMovements.value)
                it.epilepsyTongueBite = isYes(epilepsyTongueBite.value)
                it.epilepsyConfusionAfter = isYes(epilepsyConfusionAfter.value)
                it.epilepsyLocDuration = epilepsyLocDuration.value
            } else {
                it.epilepsyRecurrentSeizures = null
                it.epilepsyJerkyMovements = null
                it.epilepsyTongueBite = null
                it.epilepsyConfusionAfter = null
                it.epilepsyLocDuration = null
            }

            it.edRecurrentEpisodeloss = isYes(edRecurrentEpisodeloss.value)
            it.edRecurrentJerkyMovements = isYes(edRecurrentJerkyMovements.value)
            it.edConfusionordrowsiness = isYes(edConfusionordrowsiness.value)
            it.edProgressiveMemoryLoss = isYes(edProgressiveMemoryLoss.value)
            it.edConfusionDisorientation = isYes(edConfusionDisorientation.value)
            it.edFunctionalDecline = isYes(edFunctionalDecline.value)
            it.edScreeningOutcome = edScreeningOutcome.value
            it.edReferralRequired =
                if (it.edScreeningOutcome == "Suspected") "Yes" else edReferralRequired.value
            it.edReason =
                if (it.edScreeningOutcome == "Suspected") "Neurological condition suspected" else null
            it.edPsychosocialInterventionProvided = yesNoToBoolean(edPsychosocialIntervention.value)
            it.edInterventionType = if (it.edPsychosocialInterventionProvided == true) edInterventionType.value else null
            it.edSessionDate = if (it.edPsychosocialInterventionProvided == true) edSessionDate.value else null
            it.edDurationMinutes = if (it.edPsychosocialInterventionProvided == true) edDurationMinutes.value?.toIntOrNull() else null
            it.edRemarks = edRemarks.value
            it.referralRequired = if (shouldForceAutoReferralRequired()) {
                true
            } else {
                yesNoToBoolean(mhReferralRequired.value)
            }
            if (it.referralRequired == true) {
                it.referralLevel = mhReferralLevel.value
                it.reasonForReferral = mhReasonForReferral.value
                it.referralDate = mhReferralDate.value ?: todayDateString()
            } else {
                it.referralLevel = null
                it.reasonForReferral = null
                it.referralDate = null
            }

            it.followUpRequired = yesNoToBoolean(mhFollowUpRequired.value)
            if (it.followUpRequired == true) {
                it.followUpDate = mhFollowUpDate.value
                it.improvementNoted = mhImprovementNoted.value
                it.referralEscalationRequired = yesNoToBoolean(mhReferralEscalation.value)
                it.caseClosureReason = mhCaseClosureReason.value
            } else {
                it.followUpDate = null
                it.improvementNoted = null
                it.referralEscalationRequired = null
                it.caseClosureReason = null
            }
        }
    }
}


