package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.MentalHealthScreeningCache
import org.piramalswasthya.cho.R
class MentalHealthScreeningDataset(
    private val context: Context,
    currentLanguage: Languages
) : ReferralFollowUpDataset(context, currentLanguage) {

    private lateinit var phq9Options: Array<String>
    private lateinit var substanceFrequencyOptions: Array<String>
    private lateinit var suicideRiskOptions: Array<String>
    private lateinit var epilepsyDurationOptions: Array<String>
    private lateinit var yesNoOptions: Array<String>

    private lateinit var cache: MentalHealthScreeningCache
    private var lastPhq9AlertLevel = 0
    private var lastSuicideRiskLevel = ""

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
        }
    }



    private val emotionalBehaviouralConcerns: FormElement by lazy {
        FormElement(
            id = 101,
            inputType = InputType.RADIO,
            title = context.getString(R.string.emotional_behavioural_concerns),
            entries = yesNoOptions,
            required = true,
            hasDependants = true
        )
    }


    private val substanceUseConcerns: FormElement by lazy {
        FormElement(
            id = 102,
            inputType = InputType.RADIO,
            title = context.getString(R.string.substance_use_concerns),
            entries = yesNoOptions,
            required = true,
            hasDependants = true
        )
    }


    private val selfHarmSuicideThoughts: FormElement by lazy {
        FormElement(
            id = 103,
            inputType = InputType.RADIO,
            title = context.getString(R.string.self_harm_suicide_thoughts),
            entries = yesNoOptions,
            required = true,
            hasDependants = true
        )
    }

    // Q4
    private val memoryLossConfusion: FormElement by lazy {
        FormElement(
            id = 104,
            inputType = InputType.RADIO,
            title = context.getString(R.string.memory_loss_confusion),
            entries = yesNoOptions,
            required = true,
            hasDependants = true
        )
    }


    private val seizuresFitsLoc: FormElement by lazy {
        FormElement(
            id = 105,
            inputType = InputType.RADIO,
            title = context.getString(R.string.seizures_fits_loc),
            entries = yesNoOptions,
            required = true,
            hasDependants = true
        )
    }


    private val isPostpartum: FormElement by lazy {
        FormElement(
            id = 106,
            inputType = InputType.TEXT_VIEW,
            title = context.getString(R.string.postpartum_woman),
            entries = yesNoOptions,
            required = false,
            hasDependants = true
        )
    }



    private val phq9Header: FormElement by lazy {
        FormElement(
            id = 200,
            inputType = InputType.HEADLINE,
            title = context.getString(R.string.phq9_section_title),
            required = false
        )
    }

    private val phq9LittleInterest: FormElement by lazy {
        FormElement(
            id = 201,
            inputType = InputType.RADIO,
            title = context.getString(R.string.phq9_little_interest),
            entries = phq9Options,
            required = true,
            hasDependants = true
        )
    }

    private val phq9FeelingDown: FormElement by lazy {
        FormElement(
            id = 202,
            inputType = InputType.RADIO,
            title = context.getString(R.string.phq9_feeling_down),
            entries = phq9Options,
            required = true,
            hasDependants = true
        )
    }

    private val phq9SleepTrouble: FormElement by lazy {
        FormElement(
            id = 203,
            inputType = InputType.RADIO,
            title = context.getString(R.string.phq9_sleep_trouble),
            entries = phq9Options,
            required = true,
            hasDependants = true
        )
    }

    private val phq9FeelingTired: FormElement by lazy {
        FormElement(
            id = 204,
            inputType = InputType.RADIO,
            title = context.getString(R.string.phq9_feeling_tired),
            entries = phq9Options,
            required = true,
            hasDependants = true
        )
    }

    private val phq9Appetite: FormElement by lazy {
        FormElement(
            id = 205,
            inputType = InputType.RADIO,
            title = context.getString(R.string.phq9_appetite),
            entries = phq9Options,
            required = true,
            hasDependants = true
        )
    }

    private val phq9FeelingBad: FormElement by lazy {
        FormElement(
            id = 206,
            inputType = InputType.RADIO,
            title = context.getString(R.string.phq9_feeling_bad),
            entries = phq9Options,
            required = true,
            hasDependants = true
        )
    }

    private val phq9Concentration: FormElement by lazy {
        FormElement(
            id = 207,
            inputType = InputType.RADIO,
            title = context.getString(R.string.phq9_concentration),
            entries = phq9Options,
            required = true,
            hasDependants = true
        )
    }

    private val phq9MovingSlowly: FormElement by lazy {
        FormElement(
            id = 208,
            inputType = InputType.RADIO,
            title = context.getString(R.string.phq9_moving_slowly),
            entries = phq9Options,
            required = true,
            hasDependants = true
        )
    }

    private val phq9SelfHarmThoughts: FormElement by lazy {
        FormElement(
            id = 209,
            inputType = InputType.RADIO,
            title = context.getString(R.string.phq9_self_harm_thoughts),
            entries = phq9Options,
            required = true,
            hasDependants = true
        )
    }

    private var phq9TotalScore = FormElement(
        id = 210,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.phq9_total_score),
        required = false
    )
    private var phq9DepressionSeverity = FormElement(
        id = 211,
        inputType = InputType.TEXT_VIEW,
        title = "Depression Severity",
        required = false
    )

    private var phq9SystemAction = FormElement(
        id = 212,
        inputType = InputType.TEXT_VIEW,
        title = "System Action",
        required = false
    )



    private val substanceHeader: FormElement by lazy {
        FormElement(
            id = 300,
            inputType = InputType.HEADLINE,
            title = context.getString(R.string.substance_section_title),
            required = false
        )
    }
    private val substanceTobaccoHeader: FormElement by lazy {
        FormElement(
            id = 312,
            inputType = InputType.HEADLINE,
            title = context.getString(R.string.substance_tobacco_title),
            required = false
        )
    }
    private val substanceAlcoholHeader: FormElement by lazy {
        FormElement(
            id = 313,
            inputType = InputType.HEADLINE,
            title = context.getString(R.string.substance_alcohol_title),
            required = false
        )
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

    private var substanceTobaccoOutcome = FormElement(
        id = 310,
        inputType = InputType.TEXT_VIEW,
        title = "Tobacco use outcome",
        required = false
    )

    private var substanceSystemAction = FormElement(
        id = 311,
        inputType = InputType.TEXT_VIEW,
        title = "System action",
        required = false
    )

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
        FormElement(
            id = 303,
            inputType = InputType.RADIO,
            title = context.getString(R.string.substance_alcohol_loss),
            entries = yesNoOptions,
            required = true,
            hasDependants = true
        )
    }

    private val substanceAlcoholImpact: FormElement by lazy {
        FormElement(
            id = 314,
            inputType = InputType.RADIO,
            title = context.getString(R.string.substance_alcohol_impact),
            entries = yesNoOptions,
            required = true,
            hasDependants = true
        )
    }

    private val substanceAlcoholWithdrawal: FormElement by lazy {
        FormElement(
            id = 315,
            inputType = InputType.RADIO,
            title = context.getString(R.string.substance_alcohol_withdrawal),
            entries = yesNoOptions,
            required = true,
            hasDependants = true
        )
    }

    private val substanceAlcoholProblematic: FormElement by lazy {
        FormElement(
            id = 316,
            inputType = InputType.RADIO,
            title = context.getString(R.string.substance_alcohol_problematic),
            entries = yesNoOptions,
            required = true,
            hasDependants = true
        )
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

    private val substanceAlcoholClassification = FormElement(
        id = 317,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.substance_alcohol_classification),
        required = false
    )

    private val substanceAlcoholSystemAction = FormElement(
        id = 318,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.substance_alcohol_system_action),
        required = false
    )

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
        FormElement(
            id = 400,
            inputType = InputType.HEADLINE,
            title = context.getString(R.string.suicide_section_title),
            required = false
        )
    }

    private val suicideCurrentThoughts: FormElement by lazy {
        FormElement(
            id = 401,
            inputType = InputType.RADIO,
            title = context.getString(R.string.suicide_hopelessness),
            entries = yesNoOptions,
            required = true,
            hasDependants = true
        )
    }

    private val suicidePlan: FormElement by lazy {
        FormElement(
            id = 402,
            inputType = InputType.RADIO,
            title = context.getString(R.string.suicide_plan),
            entries = yesNoOptions,
            required = true,
            hasDependants = true
        )
    }

    private val suicidePreviousAttempt: FormElement by lazy {
        FormElement(
            id = 403,
            inputType = InputType.RADIO,
            title = context.getString(R.string.suicide_previous_attempt),
            entries = yesNoOptions,
            required = true,
            hasDependants = true
        )
    }

    private val suicideHopelessness: FormElement by lazy {
        FormElement(
            id = 404,
            inputType = InputType.RADIO,
            title = context.getString(R.string.suicide_hopelessness),
            entries = yesNoOptions,
            required = true,
            hasDependants = true
        )
    }

    private var suicideRiskLevel = FormElement(
        id = 405,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.suicide_risk_level),
        required = false
    )

    private val suicideImmediateAssess: FormElement by lazy {
        FormElement(
            id = 406,
            inputType = InputType.RADIO,
            title = context.getString(R.string.suicide_immediate_assess),
            entries = yesNoOptions,
            required = true,
            hasDependants = true
        )
    }



    private val dementiaHeader: FormElement by lazy {
        FormElement(
            id = 500,
            inputType = InputType.HEADLINE,
            title = context.getString(R.string.dementia_section_title),
            required = false
        )
    }

    private val dementiaProgressiveMemoryLoss: FormElement by lazy {
        FormElement(
            id = 501,
            inputType = InputType.RADIO,
            title = context.getString(R.string.dementia_progressive_memory_loss),
            entries = yesNoOptions,
            required = true
        )
    }

    private val dementiaForgettingRecent: FormElement by lazy {
        FormElement(
            id = 502,
            inputType = InputType.RADIO,
            title = context.getString(R.string.dementia_forgetting_recent),
            entries = yesNoOptions,
            required = true
        )
    }

    private val dementiaDisorientation: FormElement by lazy {
        FormElement(
            id = 503,
            inputType = InputType.RADIO,
            title = context.getString(R.string.dementia_disorientation),
            entries = yesNoOptions,
            required = true
        )
    }

    private val dementiaDailyActivities: FormElement by lazy {
        FormElement(
            id = 504,
            inputType = InputType.RADIO,
            title = context.getString(R.string.dementia_daily_activities),
            entries = yesNoOptions,
            required = true
        )
    }

    private val dementiaBehaviouralChanges: FormElement by lazy {
        FormElement(
            id = 505,
            inputType = InputType.RADIO,
            title = context.getString(R.string.dementia_behavioural_changes),
            entries = yesNoOptions,
            required = true
        )
    }



    private val epilepsyHeader: FormElement by lazy {
        FormElement(
            id = 600,
            inputType = InputType.HEADLINE,
            title = context.getString(R.string.epilepsy_section_title),
            required = false
        )
    }

    private val epilepsyRecurrentSeizures: FormElement by lazy {
        FormElement(
            id = 601,
            inputType = InputType.RADIO,
            title = context.getString(R.string.epilepsy_recurrent_seizures),
            entries = yesNoOptions,
            required = true
        )
    }

    private val epilepsyJerkyMovements: FormElement by lazy {
        FormElement(
            id = 602,
            inputType = InputType.RADIO,
            title = context.getString(R.string.epilepsy_jerky_movements),
            entries = yesNoOptions,
            required = true
        )
    }

    private val epilepsyTongueBite: FormElement by lazy {
        FormElement(
            id = 603,
            inputType = InputType.RADIO,
            title = context.getString(R.string.epilepsy_tongue_bite),
            entries = yesNoOptions,
            required = true
        )
    }

    private val epilepsyConfusionAfter: FormElement by lazy {
        FormElement(
            id = 604,
            inputType = InputType.RADIO,
            title = context.getString(R.string.epilepsy_confusion_after),
            entries = yesNoOptions,
            required = true
        )
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
        FormElement(
            id = 700,
            inputType = InputType.HEADLINE,
            title = context.getString(R.string.epilepsy_dementia_checklist_title),
            required = false
        )
    }


    private val edRecurrentJerkyMovements: FormElement by lazy {
        FormElement(
            id = 701,
            inputType = InputType.CHECKBOXES,
            title = context.getString(R.string.ed_recurrent_jerky_movements),
            entries = arrayOf(yesNoOptions[0]),
            required = false
        )
    }

    private val edProgressiveMemoryLoss: FormElement by lazy {
        FormElement(
            id = 702,
            inputType = InputType.CHECKBOXES,
            title = context.getString(R.string.ed_progressive_memory_loss),
            entries = arrayOf(yesNoOptions[0]),
            required = false
        )
    }

    private val edConfusionDisorientation: FormElement by lazy {
        FormElement(
            id = 703,
            inputType = InputType.CHECKBOXES,
            title = context.getString(R.string.ed_confusion_disorientation),
            entries = arrayOf(yesNoOptions[0]),
            required = false
        )
    }

    private val edFunctionalDecline: FormElement by lazy {
        FormElement(
            id = 704,
            inputType = InputType.CHECKBOXES,
            title = context.getString(R.string.ed_functional_decline),
            entries = arrayOf(yesNoOptions[0]),
            required = false
        )
    }

    private var edScreeningOutcome = FormElement(
        id = 705,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.ed_screening_outcome),
        required = false
    )

    private var edReferralRequired = FormElement(
        id = 706,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.ed_referral_required),
        required = false
    )
    private val edRecurrentEpisodeloss: FormElement by lazy {
        FormElement(
            id = 707,
            inputType = InputType.CHECKBOXES,
            title = context.getString(R.string.ed_edRecurrentEpisodeloss),
            entries = arrayOf(yesNoOptions[0]),
            required = false
        )
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

    // ── Section F: Referral & Follow-up ──────────────────────────────

    override val referralRequired = createReferralRequired(801)
    override val referralLevel = createReferralLevel(802)
    override val reasonForReferral = FormElement(
        id = 803,
        inputType = InputType.DROPDOWN,
        title = context.getString(R.string.mental_health_reason_for_referral),
        entries = context.resources.getStringArray(R.array.mental_health_referral_reasons),
        required = true
    )
    override val followUpRequired = createFollowUpRequired(804)
    override val followUpDate = createFollowUpDate(805)

    // ── All PHQ-9 form elements ──────────────────────────────────────

    private val phq9Elements = listOf(
        phq9Header, phq9LittleInterest, phq9FeelingDown, phq9SleepTrouble,
        phq9FeelingTired, phq9Appetite, phq9FeelingBad, phq9Concentration,
        phq9MovingSlowly, phq9SelfHarmThoughts, phq9TotalScore, phq9DepressionSeverity, phq9SystemAction
    )

    private val substanceElements = listOf(
        substanceHeader, substanceTobaccoHeader, substanceCurrentTobaccoUse,
        substanceTobaccoOutcome, substanceSystemAction, substanceAlcoholHeader, substanceAlcoholUse, substanceAlcoholProblematic, substanceAlcoholClassification, substanceAlcoholSystemAction
    )

    private val suicideElements = listOf(
        suicideHeader, suicidePreviousAttempt, suicidePlan,
        suicideHopelessness, suicideImmediateAssess, suicideRiskLevel
    )

    private val dementiaElements = listOf(
        dementiaHeader, dementiaProgressiveMemoryLoss, dementiaForgettingRecent,
        dementiaDisorientation, dementiaDailyActivities, dementiaBehaviouralChanges
    )

    private val epilepsyElements = listOf(
        epilepsyHeader, epilepsyRecurrentSeizures, epilepsyJerkyMovements,
        epilepsyTongueBite, epilepsyConfusionAfter, epilepsyLocDuration
    )

    private val edChecklistElements = listOf(
        edChecklistHeader,edRecurrentEpisodeloss, edRecurrentJerkyMovements, edProgressiveMemoryLoss,
        edConfusionDisorientation, edFunctionalDecline, edScreeningOutcome, edReferralRequired
    )

    // ── Setup Page ───────────────────────────────────────────────────

    suspend fun setUpPage(
        savedRecord: MentalHealthScreeningCache?,
        isPostpartumFromRmncha: Boolean = false
    ) {
        cache = savedRecord ?: MentalHealthScreeningCache(
            patientID = "",
            benVisitNo = null
        )

        cache.isPostpartum = isPostpartumFromRmncha

        populateFromCache(cache)

        val list = mutableListOf<FormElement>()

        list.add(emotionalBehaviouralConcerns)
        list.add(substanceUseConcerns)
        list.add(selfHarmSuicideThoughts)
        list.add(memoryLossConfusion)
        list.add(seizuresFitsLoc)
        list.add(isPostpartum)

        if (shouldShowPhq9()) {
            list.addAll(phq9Elements)
        }

        if (substanceUseConcerns.value == "Yes") {
            list.addAll(substanceElements)
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
                list.add(idx + 4, substanceAlcoholWithdrawal)
            }
        }

        if (selfHarmSuicideThoughts.value == "Yes") {
            list.addAll(suicideElements)
        }





        // Add the new Epilepsy & Dementia Checklist
        if (memoryLossConfusion.value == "Yes" || seizuresFitsLoc.value == "Yes") {
            list.addAll(
                listOf(
                    edChecklistHeader, edRecurrentEpisodeloss, edRecurrentJerkyMovements, edProgressiveMemoryLoss,
                    edConfusionDisorientation, edFunctionalDecline, edScreeningOutcome.copy(),
                    edReferralRequired.copy()
                )
            )
        }

        // Section F
        addReferralFollowUpElements(list)

        setUpPage(list)
    }

    private fun shouldShowPhq9(): Boolean {
        return emotionalBehaviouralConcerns.value == "Yes" ||
                selfHarmSuicideThoughts.value == "Yes" ||
                cache.isPostpartum == true
    }


    private fun computePhq9Total() {
        val scores = listOfNotNull(
            extractPhq9Score(phq9LittleInterest.value),
            extractPhq9Score(phq9FeelingDown.value),
            extractPhq9Score(phq9SleepTrouble.value),
            extractPhq9Score(phq9FeelingTired.value),
            extractPhq9Score(phq9Appetite.value),
            extractPhq9Score(phq9FeelingBad.value),
            extractPhq9Score(phq9Concentration.value),
            extractPhq9Score(phq9MovingSlowly.value),
            extractPhq9Score(phq9SelfHarmThoughts.value)
        )
        phq9TotalScore.value = if (scores.isNotEmpty()) scores.sum().toString() else null
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

            edRecurrentEpisodeloss.id, edRecurrentJerkyMovements.id, edProgressiveMemoryLoss.id,
            edConfusionDisorientation.id, edFunctionalDecline.id -> {
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

            // Handle referral & follow-up changes
            else -> {
                val result = handleReferralFollowUpChange(formId, index)
                if (result != -1) result else -1
            }
        }
    }



    private suspend fun rebuildConditionalSections() {
        val list = mutableListOf<FormElement>()

        list.add(emotionalBehaviouralConcerns)
        list.add(substanceUseConcerns)
        list.add(selfHarmSuicideThoughts)
        list.add(memoryLossConfusion)
        list.add(seizuresFitsLoc)
        list.add(isPostpartum)

        updatePhq9Outcome()
        updateTobaccoOutcome()
        computeSuicideRiskLevel()
        computeAlcoholClassification()
        computeAlcoholSystemAction()
        computeEdScreeningOutcome()

        if (shouldShowPhq9()) {
            val phq9ElementsWithFreshCopies = listOf(
                phq9Header, phq9LittleInterest, phq9FeelingDown, phq9SleepTrouble,
                phq9FeelingTired, phq9Appetite, phq9FeelingBad, phq9Concentration,
                phq9MovingSlowly, phq9SelfHarmThoughts,
                phq9TotalScore.copy(), phq9DepressionSeverity.copy(), phq9SystemAction.copy()
            )
            list.addAll(phq9ElementsWithFreshCopies)
        }

        if (substanceUseConcerns.value == "Yes") {
            val substanceElementsWithFreshCopies = listOf(
                substanceHeader, substanceTobaccoHeader, substanceCurrentTobaccoUse,
                substanceTobaccoOutcome.copy(), substanceSystemAction.copy(), substanceAlcoholHeader, substanceAlcoholUse, substanceAlcoholProblematic, substanceAlcoholClassification.copy(), substanceAlcoholSystemAction.copy()
            )
            list.addAll(substanceElementsWithFreshCopies)
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
                list.add(idx + 4, substanceAlcoholWithdrawal)
            } else {
                clearAlcoholSubFields()
            }
        }

        if (selfHarmSuicideThoughts.value == "Yes") {
            val suicideElementsWithFreshCopies = listOf(
                suicideHeader, suicidePreviousAttempt, suicidePlan,
                suicideHopelessness, suicideImmediateAssess,
                suicideRiskLevel.copy()
            )
            list.addAll(suicideElementsWithFreshCopies)
        }





        if (memoryLossConfusion.value == "Yes" || seizuresFitsLoc.value == "Yes") {
            list.addAll(
                listOf(
                    edChecklistHeader,edRecurrentEpisodeloss, edRecurrentJerkyMovements, edProgressiveMemoryLoss,
                    edConfusionDisorientation, edFunctionalDecline, edScreeningOutcome.copy(),

                )
            )
        }

        if (edScreeningOutcome.value == "Suspected") {
            list.add(edPsychosocialIntervention)
            if (edPsychosocialIntervention.value == "Yes") {
                list.add(edInterventionType)
            }
            edSessionDate.max = System.currentTimeMillis()
            list.add(edSessionDate)
            list.add(edDurationMinutes)
            list.add(edRemarks)
        } else {
            clearEdPsychosocialValues()
        }

        // Section F
        addReferralFollowUpElements(list)

        setUpPage(list)
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
        phq9SystemAction.hasAlertError = score >= 10
        if (score >= 10) {
            referralRequired.value = referralRequired.entries!!.first()
        } else {
            referralRequired.value = referralRequired.entries!!.last()
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
            "No use identified" -> "Close screening"
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

        // No answer yet — show nothing
        if (use == null && frequency == null && loss == null && impact == null && withdrawal == null && problematic == null) {
            substanceAlcoholClassification.value = null
            return
        }

        // Any of these present → Problematic (takes priority over everything)
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
            hasRiskFactor -> "Problematic"
            noAlcoholUse || occasionalWithNoRisks -> "Non-problematic"
            else -> null
        }
    }

    private fun computeAlcoholSystemAction() {
        substanceAlcoholSystemAction.value = when (substanceAlcoholClassification.value) {
            "Problematic"     -> "Brief intervention"
            "Non-problematic" -> "Referral"
            else              -> null
        }
    }

    private fun computeEdScreeningOutcome() {
        if (memoryLossConfusion.value != "Yes" && seizuresFitsLoc.value != "Yes") {
            edScreeningOutcome.value = null
            edReferralRequired.value = null
            return
        }

        val hasRisk = isYes(edRecurrentEpisodeloss.value) || isYes(edRecurrentJerkyMovements.value) ||
                isYes(edProgressiveMemoryLoss.value) ||
                isYes(edConfusionDisorientation.value) ||
                isYes(edFunctionalDecline.value)

        edScreeningOutcome.value = if (hasRisk) "Suspected" else "Not suspected"
        edReferralRequired.value = if (hasRisk) "Yes" else "No"
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

        suicideRiskLevel.value = when {
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
        briefInterventionGiven.value = null
    }
    private fun clearEdPsychosocialValues() {
        edPsychosocialIntervention.value = null
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
        substanceAlcoholSystemAction.value = null
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
        edProgressiveMemoryLoss.value = null
        edConfusionDisorientation.value = null
        edFunctionalDecline.value = null
        edScreeningOutcome.value = null
        edReferralRequired.value = null
    }

    // ── Populate from Cache ──────────────────────────────────────────

    private fun populateFromCache(cache: MentalHealthScreeningCache) {
        // Initial screening questions
        emotionalBehaviouralConcerns.value =
            cache.emotionalBehaviouralConcerns?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        substanceUseConcerns.value =
            cache.substanceUseConcerns?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        selfHarmSuicideThoughts.value =
            cache.selfHarmSuicideThoughts?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        memoryLossConfusion.value =
            cache.memoryLossConfusion?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        seizuresFitsLoc.value =
            cache.seizuresFitsLoc?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        isPostpartum.value =
            cache.isPostpartum?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }

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
        substanceCurrentTobaccoUse.value =
            cache.substanceCurrentTobaccoUse?.let { if (it) "Yes" else "No" }
        substanceTobaccoType.value = cache.substanceTobaccoType
        substanceTobaccoFrequency.value = cache.substanceTobaccoFrequency
        substanceTobaccoOutcome.value = cache.substanceTobaccoOutcome
        substanceSystemAction.value = cache.substanceSystemAction
        substanceAlcoholUse.value =
            cache.substanceAlcoholUse?.let { if (it) "Yes" else "No" }
        substanceTobaccoUse.value =
            cache.substanceTobaccoUse?.let { if (it) "Yes" else "No" }
        substance_alcohol_loss.value =
            cache.substance_alcohol_loss?.let { if (it) "Yes" else "No" }
        substanceAlcoholImpact.value =
            cache.substanceAlcoholImpact?.let { if (it) "Yes" else "No" }
        substanceAlcoholWithdrawal.value =
            cache.substanceAlcoholWithdrawal?.let { if (it) "Yes" else "No" }
        substanceAlcoholProblematic.value =
            cache.substanceAlcoholProblematic?.let { if (it) "Yes" else "No" }
        substanceAlcoholClassification.value = cache.substanceAlcoholClassification
        substanceAlcoholSystemAction.value = cache.substanceAlcoholSystemAction
        substanceOtherSpecify.value = cache.substanceOtherSpecify
        substance_alcohol_frequency.value = cache.substance_alcohol_frequency

        // Suicide Risk
        suicideCurrentThoughts.value =
            cache.suicideCurrentThoughts?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        suicidePlan.value =
            cache.suicidePlan?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        suicidePreviousAttempt.value =
            cache.suicidePreviousAttempt?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        suicideHopelessness.value =
            cache.suicideHopelessness?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        suicideImmediateAssess.value =
            cache.suicideImmediateAssess?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        suicideRiskLevel.value = cache.suicideRiskLevel

        // Dementia
        dementiaProgressiveMemoryLoss.value =
            cache.dementiaProgressiveMemoryLoss?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        dementiaForgettingRecent.value =
            cache.dementiaForgettingRecent?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        dementiaDisorientation.value =
            cache.dementiaDisorientation?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        dementiaDailyActivities.value =
            cache.dementiaDailyActivities?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        dementiaBehaviouralChanges.value =
            cache.dementiaBehaviouralChanges?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }

        // Epilepsy
        epilepsyRecurrentSeizures.value =
            cache.epilepsyRecurrentSeizures?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        epilepsyJerkyMovements.value =
            cache.epilepsyJerkyMovements?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        epilepsyTongueBite.value =
            cache.epilepsyTongueBite?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        epilepsyConfusionAfter.value =
            cache.epilepsyConfusionAfter?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        epilepsyLocDuration.value = cache.epilepsyLocDuration

        // Epilepsy & Dementia Checklist
        edRecurrentEpisodeloss.value =
            cache.edRecurrentEpisodeloss?.let { if (it) yesNoOptions[0] else null }
        edRecurrentJerkyMovements.value =
            cache.edRecurrentJerkyMovements?.let { if (it) yesNoOptions[0] else null }
        edProgressiveMemoryLoss.value =
            cache.edProgressiveMemoryLoss?.let { if (it) yesNoOptions[0] else null }
        edConfusionDisorientation.value =
            cache.edConfusionDisorientation?.let { if (it) yesNoOptions[0] else null }
        edFunctionalDecline.value =
            cache.edFunctionalDecline?.let { if (it) yesNoOptions[0] else null }
        edScreeningOutcome.value = cache.edScreeningOutcome
        edReferralRequired.value = cache.edReferralRequired
        edPsychosocialIntervention.value =
            cache.edPsychosocialInterventionProvided?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        edInterventionType.value = cache.edInterventionType
        edSessionDate.value = cache.edSessionDate
        edDurationMinutes.value = cache.edDurationMinutes?.toString()
        edRemarks.value = cache.edRemarks

        // Section F
        populateReferralFollowUpFromCache(cache)
    }

    // ── Map Values ───────────────────────────────────────────────────

    private fun extractPhq9Score(value: String?): Int? {
        return value?.firstOrNull()?.digitToIntOrNull()
    }

    private fun isYes(value: String?): Boolean =
        value == yesNoOptions.getOrNull(0)

    private fun isNo(value: String?): Boolean =
        value == yesNoOptions.getOrNull(1)

    private fun yesNoToBoolean(value: String?): Boolean? =
        when {
            value == yesNoOptions.getOrNull(0) -> true
            value == yesNoOptions.getOrNull(1) -> false
            else -> null
        }


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
            it.edProgressiveMemoryLoss = isYes(edProgressiveMemoryLoss.value)
            it.edConfusionDisorientation = isYes(edConfusionDisorientation.value)
            it.edFunctionalDecline = isYes(edFunctionalDecline.value)
            it.edScreeningOutcome = edScreeningOutcome.value
            it.edReferralRequired = edReferralRequired.value
            if (edScreeningOutcome.value == "Suspected") {
                it.edPsychosocialInterventionProvided = yesNoToBoolean(edPsychosocialIntervention.value)
                it.edInterventionType = if (it.edPsychosocialInterventionProvided == true) edInterventionType.value else null
                it.edSessionDate = if (it.edPsychosocialInterventionProvided == true) edSessionDate.value else null
                it.edDurationMinutes = if (it.edPsychosocialInterventionProvided == true) edDurationMinutes.value?.toIntOrNull() else null
                it.edRemarks = edRemarks.value
            } else {
                it.edPsychosocialInterventionProvided = null
                it.edInterventionType = null
                it.edSessionDate = null
                it.edDurationMinutes = null
                it.edRemarks = null
            }

            mapReferralFollowUpValues(it)
        }
    }
}