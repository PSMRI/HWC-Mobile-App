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

    // Resource-backed arrays
    private lateinit var phq9Options: Array<String>
    private lateinit var substanceFrequencyOptions: Array<String>
    private lateinit var suicideRiskOptions: Array<String>
    private lateinit var epilepsyDurationOptions: Array<String>
    private lateinit var yesNoOptions: Array<String>

    private lateinit var cache: MentalHealthScreeningCache

    init {
        // Load all string arrays from resources
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

    // ── Section 1: Initial Screening Questions (Mandatory Radio) ─────

    // Q1
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

    // Q2
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

    // Q3
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

    // Q5
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

    // Q6 (auto-derived from RMNCH+A, but shown as read-only info)
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

    // ── Section 2: PHQ-9 Depression Screening ────────────────────────
    // Enabled by Q1=Yes OR Q3=Yes OR Q6=Yes

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
            required = true
        )
    }

    private val phq9FeelingDown: FormElement by lazy {
        FormElement(
            id = 202,
            inputType = InputType.RADIO,
            title = context.getString(R.string.phq9_feeling_down),
            entries = phq9Options,
            required = true
        )
    }

    private val phq9SleepTrouble: FormElement by lazy {
        FormElement(
            id = 203,
            inputType = InputType.RADIO,
            title = context.getString(R.string.phq9_sleep_trouble),
            entries = phq9Options,
            required = true
        )
    }

    private val phq9FeelingTired: FormElement by lazy {
        FormElement(
            id = 204,
            inputType = InputType.RADIO,
            title = context.getString(R.string.phq9_feeling_tired),
            entries = phq9Options,
            required = true
        )
    }

    private val phq9Appetite: FormElement by lazy {
        FormElement(
            id = 205,
            inputType = InputType.RADIO,
            title = context.getString(R.string.phq9_appetite),
            entries = phq9Options,
            required = true
        )
    }

    private val phq9FeelingBad: FormElement by lazy {
        FormElement(
            id = 206,
            inputType = InputType.RADIO,
            title = context.getString(R.string.phq9_feeling_bad),
            entries = phq9Options,
            required = true
        )
    }

    private val phq9Concentration: FormElement by lazy {
        FormElement(
            id = 207,
            inputType = InputType.RADIO,
            title = context.getString(R.string.phq9_concentration),
            entries = phq9Options,
            required = true
        )
    }

    private val phq9MovingSlowly: FormElement by lazy {
        FormElement(
            id = 208,
            inputType = InputType.RADIO,
            title = context.getString(R.string.phq9_moving_slowly),
            entries = phq9Options,
            required = true
        )
    }

    private val phq9SelfHarmThoughts: FormElement by lazy {
        FormElement(
            id = 209,
            inputType = InputType.RADIO,
            title = context.getString(R.string.phq9_self_harm_thoughts),
            entries = phq9Options,
            required = true
        )
    }

    private val phq9TotalScore: FormElement by lazy {
        FormElement(
            id = 210,
            inputType = InputType.TEXT_VIEW,
            title = context.getString(R.string.phq9_total_score),
            required = false
        )
    }

    // ── Section 3: Substance Use Screening & Brief Intervention ──────
    // Enabled by Q2=Yes

    private val substanceHeader: FormElement by lazy {
        FormElement(
            id = 300,
            inputType = InputType.HEADLINE,
            title = context.getString(R.string.substance_section_title),
            required = false
        )
    }

    private val substanceAlcoholUse: FormElement by lazy {
        FormElement(
            id = 301,
            inputType = InputType.RADIO,
            title = context.getString(R.string.substance_alcohol_use),
            entries = yesNoOptions,
            required = true
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

    private val substanceOtherUse: FormElement by lazy {
        FormElement(
            id = 303,
            inputType = InputType.RADIO,
            title = context.getString(R.string.substance_other_use),
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

    private val substanceFrequency: FormElement by lazy {
        FormElement(
            id = 305,
            inputType = InputType.DROPDOWN,
            title = context.getString(R.string.substance_frequency),
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

    // ── Section 4: Suicide Risk Screening ────────────────────────────
    // Enabled by Q3=Yes (shown AFTER PHQ-9)

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
            title = context.getString(R.string.suicide_current_thoughts),
            entries = yesNoOptions,
            required = true
        )
    }

    private val suicidePlan: FormElement by lazy {
        FormElement(
            id = 402,
            inputType = InputType.RADIO,
            title = context.getString(R.string.suicide_plan),
            entries = yesNoOptions,
            required = true
        )
    }

    private val suicidePreviousAttempt: FormElement by lazy {
        FormElement(
            id = 403,
            inputType = InputType.RADIO,
            title = context.getString(R.string.suicide_previous_attempt),
            entries = yesNoOptions,
            required = true
        )
    }

    private val suicideHopelessness: FormElement by lazy {
        FormElement(
            id = 404,
            inputType = InputType.RADIO,
            title = context.getString(R.string.suicide_hopelessness),
            entries = yesNoOptions,
            required = true
        )
    }

    private val suicideRiskLevel: FormElement by lazy {
        FormElement(
            id = 405,
            inputType = InputType.DROPDOWN,
            title = context.getString(R.string.suicide_risk_level),
            entries = suicideRiskOptions,
            required = true
        )
    }

    // ── Section 5: Dementia Screening Checklist ──────────────────────
    // Enabled by Q4=Yes

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

    // ── Section 6: Epilepsy Screening Checklist ──────────────────────
    // Enabled by Q5=Yes

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

    // ── Section F: Referral & Follow-up ──────────────────────────────

    override val referralRequired = createReferralRequired(701)
    override val referralLevel = createReferralLevel(702)
    override val reasonForReferral = FormElement(
        id = 703,
        inputType = InputType.DROPDOWN,
        title = context.getString(R.string.mental_health_reason_for_referral),
        entries = context.resources.getStringArray(R.array.mental_health_referral_reasons),
        required = true
    )
    override val followUpRequired = createFollowUpRequired(704)
    override val followUpDate = createFollowUpDate(705)

    // ── All PHQ-9 form elements ──────────────────────────────────────

    private val phq9Elements = listOf(
        phq9Header, phq9LittleInterest, phq9FeelingDown, phq9SleepTrouble,
        phq9FeelingTired, phq9Appetite, phq9FeelingBad, phq9Concentration,
        phq9MovingSlowly, phq9SelfHarmThoughts, phq9TotalScore
    )

    private val substanceElements = listOf(
        substanceHeader, substanceAlcoholUse, substanceTobaccoUse,
        substanceOtherUse, substanceFrequency, briefInterventionGiven
    )

    private val suicideElements = listOf(
        suicideHeader, suicideCurrentThoughts, suicidePlan,
        suicidePreviousAttempt, suicideHopelessness, suicideRiskLevel
    )

    private val dementiaElements = listOf(
        dementiaHeader, dementiaProgressiveMemoryLoss, dementiaForgettingRecent,
        dementiaDisorientation, dementiaDailyActivities, dementiaBehaviouralChanges
    )

    private val epilepsyElements = listOf(
        epilepsyHeader, epilepsyRecurrentSeizures, epilepsyJerkyMovements,
        epilepsyTongueBite, epilepsyConfusionAfter, epilepsyLocDuration
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

        // Auto-derive postpartum status
        if (isPostpartumFromRmncha) {
            cache.isPostpartum = true
        }

        populateFromCache(cache)

        val list = mutableListOf<FormElement>()

        // Initial screening questions (always shown)
        list.add(emotionalBehaviouralConcerns)
        list.add(substanceUseConcerns)
        list.add(selfHarmSuicideThoughts)
        list.add(memoryLossConfusion)
        list.add(seizuresFitsLoc)
        list.add(isPostpartum)

        // Conditionally add sub-screening sections based on saved values
        if (shouldShowPhq9()) {
            list.addAll(phq9Elements)
        }

        if (isYes(substanceUseConcerns.value)) {
            list.addAll(substanceElements)
            if (isYes(substanceOtherUse.value)) {
                list.add(list.indexOf(substanceOtherUse) + 1, substanceOtherSpecify)
            }
        }

        if (isYes(selfHarmSuicideThoughts.value)) {
            list.addAll(suicideElements)
        }

        if (isYes(memoryLossConfusion.value)) {
            list.addAll(dementiaElements)
        }

        if (isYes(seizuresFitsLoc.value)) {
            list.addAll(epilepsyElements)
        }

        // Section F
        addReferralFollowUpElements(list)

        setUpPage(list)
    }

    private fun shouldShowPhq9(): Boolean {
        return isYes(emotionalBehaviouralConcerns.value) ||
                isYes(selfHarmSuicideThoughts.value) ||
                isYes(isPostpartum.value)
    }

    /**
     * Recalculates PHQ-9 total score based on current form values.
     * Extracts numeric scores from each PHQ-9 question and sums them.
     */
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

    // ── Value Change Handler ─────────────────────────────────────────

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            // PHQ-9 items (201-209) -> Recalculate total
            in 201..209 -> {
                computePhq9Total()
                formId
            }

            // Q1: Emotional/behavioural concerns -> Enable PHQ-9
            emotionalBehaviouralConcerns.id -> {
                rebuildConditionalSections()
                formId
            }

            // Q2: Substance use concerns -> Enable Substance Use Screening
            substanceUseConcerns.id -> {
                if (index == 0) { // Yes
                    triggerDependants(
                        source = substanceUseConcerns,
                        addItems = substanceElements,
                        removeItems = emptyList()
                    )
                } else { // No
                    clearSubstanceValues()
                    triggerDependants(
                        source = substanceUseConcerns,
                        addItems = emptyList(),
                        removeItems = substanceElements + listOf(substanceOtherSpecify)
                    )
                }
                formId
            }

            // Q3: Self-harm/suicide -> Enable PHQ-9 then Suicide Risk Screening
            selfHarmSuicideThoughts.id -> {
                rebuildConditionalSections()
                formId
            }

            // Q4: Memory loss/confusion -> Enable Dementia Screening
            memoryLossConfusion.id -> {
                if (index == 0) { // Yes
                    triggerDependants(
                        source = memoryLossConfusion,
                        addItems = dementiaElements,
                        removeItems = emptyList()
                    )
                } else { // No
                    clearDementiaValues()
                    triggerDependants(
                        source = memoryLossConfusion,
                        addItems = emptyList(),
                        removeItems = dementiaElements
                    )
                }
                formId
            }

            // Q5: Seizures/fits -> Enable Epilepsy Screening
            seizuresFitsLoc.id -> {
                if (index == 0) { // Yes
                    triggerDependants(
                        source = seizuresFitsLoc,
                        addItems = epilepsyElements,
                        removeItems = emptyList()
                    )
                } else { // No
                    clearEpilepsyValues()
                    triggerDependants(
                        source = seizuresFitsLoc,
                        addItems = emptyList(),
                        removeItems = epilepsyElements
                    )
                }
                formId
            }

            // Q6: Postpartum -> Enable PHQ-9 for postpartum depression
            isPostpartum.id -> {
                rebuildConditionalSections()
                formId
            }

            // Substance Other Use -> show/hide specify field
            substanceOtherUse.id -> {
                if (index == 0) { // Yes
                    triggerDependants(
                        source = substanceOtherUse,
                        addItems = listOf(substanceOtherSpecify),
                        removeItems = emptyList()
                    )
                } else { // No
                    substanceOtherSpecify.value = null
                    triggerDependants(
                        source = substanceOtherUse,
                        addItems = emptyList(),
                        removeItems = listOf(substanceOtherSpecify)
                    )
                }
                formId
            }

            // Handle referral & follow-up changes
            else -> {
                val result = handleReferralFollowUpChange(formId, index)
                if (result != -1) result else -1
            }
        }
    }

    /**
     * Rebuilds PHQ-9 and Suicide Risk sections based on current Q1, Q3, Q6 values.
     * PHQ-9 is shown if ANY of Q1, Q3, Q6 = Yes.
     * Suicide Risk is shown if Q3 = Yes (after PHQ-9).
     */
    private suspend fun rebuildConditionalSections() {
        val showPhq9 = shouldShowPhq9()
        val showSuicideRisk = selfHarmSuicideThoughts.value == "Yes"

        // Determine what to add and remove
        val addItems = mutableListOf<FormElement>()
        val removeItems = mutableListOf<FormElement>()

        if (showPhq9) {
            addItems.addAll(phq9Elements)
        } else {
            clearPhq9Values()
            removeItems.addAll(phq9Elements)
        }

        if (showSuicideRisk) {
            addItems.addAll(suicideElements)
        } else {
            clearSuicideValues()
            removeItems.addAll(suicideElements)
        }

        // Use a dummy source to trigger the rebuild
        triggerDependants(
            source = emotionalBehaviouralConcerns,
            addItems = addItems,
            removeItems = removeItems
        )
    }

    // ── Clear Helpers ────────────────────────────────────────────────

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
    }

    private fun clearSubstanceValues() {
        substanceAlcoholUse.value = null
        substanceTobaccoUse.value = null
        substanceOtherUse.value = null
        substanceOtherSpecify.value = null
        substanceFrequency.value = null
        briefInterventionGiven.value = null
    }

    private fun clearSuicideValues() {
        suicideCurrentThoughts.value = null
        suicidePlan.value = null
        suicidePreviousAttempt.value = null
        suicideHopelessness.value = null
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

        // Substance Use
        substanceAlcoholUse.value =
            cache.substanceAlcoholUse?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        substanceTobaccoUse.value =
            cache.substanceTobaccoUse?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        substanceOtherUse.value =
            cache.substanceOtherUse?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        substanceOtherSpecify.value = cache.substanceOtherSpecify
        substanceFrequency.value = cache.substanceFrequency
        briefInterventionGiven.value =
            cache.briefInterventionGiven?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }

        // Suicide Risk
        suicideCurrentThoughts.value =
            cache.suicideCurrentThoughts?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        suicidePlan.value =
            cache.suicidePlan?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        suicidePreviousAttempt.value =
            cache.suicidePreviousAttempt?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
        suicideHopelessness.value =
            cache.suicideHopelessness?.let { if (it) yesNoOptions[0] else yesNoOptions[1] }
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
            if (isYes(substanceUseConcerns.value)) {
                it.substanceAlcoholUse = isYes(substanceAlcoholUse.value)
                it.substanceTobaccoUse = isYes(substanceTobaccoUse.value)
                it.substanceOtherUse = isYes(substanceOtherUse.value)
                it.substanceOtherSpecify =
                    if (it.substanceOtherUse == true) substanceOtherSpecify.value else null
                it.substanceFrequency = substanceFrequency.value
                it.briefInterventionGiven = isYes(briefInterventionGiven.value)
            } else {
                it.substanceAlcoholUse = null
                it.substanceTobaccoUse = null
                it.substanceOtherUse = null
                it.substanceOtherSpecify = null
                it.substanceFrequency = null
                it.briefInterventionGiven = null
            }

            // Suicide Risk
            if (isYes(selfHarmSuicideThoughts.value)) {
                it.suicideCurrentThoughts = isYes(suicideCurrentThoughts.value)
                it.suicidePlan = isYes(suicidePlan.value)
                it.suicidePreviousAttempt = isYes(suicidePreviousAttempt.value)
                it.suicideHopelessness = isYes(suicideHopelessness.value)
                it.suicideRiskLevel = suicideRiskLevel.value
            } else {
                it.suicideCurrentThoughts = null
                it.suicidePlan = null
                it.suicidePreviousAttempt = null
                it.suicideHopelessness = null
                it.suicideRiskLevel = null
            }

            // Dementia
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

            // Epilepsy
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

            // Section F
            mapReferralFollowUpValues(it)
        }
    }
}