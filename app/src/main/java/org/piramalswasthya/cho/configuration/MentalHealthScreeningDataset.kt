package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.MentalHealthScreeningCache

class MentalHealthScreeningDataset(
    context: Context,
    currentLanguage: Languages
) : ReferralFollowUpDataset(context, currentLanguage) {

    companion object {
        // PHQ-9 scoring options
        private val phq9Options = arrayOf(
            "0 - Not at all",
            "1 - Several days",
            "2 - More than half the days",
            "3 - Nearly every day"
        )

        private val substanceFrequencyOptions = arrayOf(
            "Daily",
            "Weekly",
            "Monthly",
            "Occasionally"
        )

        private val suicideRiskOptions = arrayOf(
            "Low",
            "Moderate",
            "High"
        )

        private val epilepsyDurationOptions = arrayOf(
            "Less than 5 minutes",
            "5–15 minutes",
            "More than 15 minutes"
        )
    }

    private lateinit var cache: MentalHealthScreeningCache

    // ── Section 1: Initial Screening Questions (Mandatory Radio) ─────

    // Q1
    private val emotionalBehaviouralConcerns = FormElement(
        id = 101,
        inputType = InputType.RADIO,
        title = "Emotional or behavioural concerns present",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    // Q2
    private val substanceUseConcerns = FormElement(
        id = 102,
        inputType = InputType.RADIO,
        title = "Substance use related concerns present",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    // Q3
    private val selfHarmSuicideThoughts = FormElement(
        id = 103,
        inputType = InputType.RADIO,
        title = "Thoughts of self-harm or suicide present",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    // Q4
    private val memoryLossConfusion = FormElement(
        id = 104,
        inputType = InputType.RADIO,
        title = "Memory loss or confusion present",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    // Q5
    private val seizuresFitsLoc = FormElement(
        id = 105,
        inputType = InputType.RADIO,
        title = "Seizures, fits, or loss of consciousness present",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    // Q6 (auto-derived from RMNCH+A, but shown as read-only info)
    private val isPostpartum = FormElement(
        id = 106,
        inputType = InputType.RADIO,
        title = "Post-partum woman (\u226412 months after delivery)",
        entries = arrayOf("Yes", "No"),
        required = false,
        hasDependants = true
    )

    // ── Section 2: PHQ-9 Depression Screening ────────────────────────
    // Enabled by Q1=Yes OR Q3=Yes OR Q6=Yes

    private val phq9Header = FormElement(
        id = 200,
        inputType = InputType.HEADLINE,
        title = "PHQ-9 Depression Screening",
        required = false
    )

    private val phq9LittleInterest = FormElement(
        id = 201,
        inputType = InputType.RADIO,
        title = "Little interest or pleasure in doing things",
        entries = phq9Options,
        required = true
    )

    private val phq9FeelingDown = FormElement(
        id = 202,
        inputType = InputType.RADIO,
        title = "Feeling down, depressed, or hopeless",
        entries = phq9Options,
        required = true
    )

    private val phq9SleepTrouble = FormElement(
        id = 203,
        inputType = InputType.RADIO,
        title = "Trouble falling/staying asleep, or sleeping too much",
        entries = phq9Options,
        required = true
    )

    private val phq9FeelingTired = FormElement(
        id = 204,
        inputType = InputType.RADIO,
        title = "Feeling tired or having little energy",
        entries = phq9Options,
        required = true
    )

    private val phq9Appetite = FormElement(
        id = 205,
        inputType = InputType.RADIO,
        title = "Poor appetite or overeating",
        entries = phq9Options,
        required = true
    )

    private val phq9FeelingBad = FormElement(
        id = 206,
        inputType = InputType.RADIO,
        title = "Feeling bad about yourself — or that you are a failure",
        entries = phq9Options,
        required = true
    )

    private val phq9Concentration = FormElement(
        id = 207,
        inputType = InputType.RADIO,
        title = "Trouble concentrating on things",
        entries = phq9Options,
        required = true
    )

    private val phq9MovingSlowly = FormElement(
        id = 208,
        inputType = InputType.RADIO,
        title = "Moving or speaking slowly, or being fidgety/restless",
        entries = phq9Options,
        required = true
    )

    private val phq9SelfHarmThoughts = FormElement(
        id = 209,
        inputType = InputType.RADIO,
        title = "Thoughts that you would be better off dead or of hurting yourself",
        entries = phq9Options,
        required = true
    )

    private val phq9TotalScore = FormElement(
        id = 210,
        inputType = InputType.TEXT_VIEW,
        title = "PHQ-9 Total Score",
        required = false
    )

    // ── Section 3: Substance Use Screening & Brief Intervention ──────
    // Enabled by Q2=Yes

    private val substanceHeader = FormElement(
        id = 300,
        inputType = InputType.HEADLINE,
        title = "Substance Use Screening & Brief Intervention",
        required = false
    )

    private val substanceAlcoholUse = FormElement(
        id = 301,
        inputType = InputType.RADIO,
        title = "Alcohol use",
        entries = arrayOf("Yes", "No"),
        required = true
    )

    private val substanceTobaccoUse = FormElement(
        id = 302,
        inputType = InputType.RADIO,
        title = "Tobacco use",
        entries = arrayOf("Yes", "No"),
        required = true
    )

    private val substanceOtherUse = FormElement(
        id = 303,
        inputType = InputType.RADIO,
        title = "Other substance use",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val substanceOtherSpecify = FormElement(
        id = 304,
        inputType = InputType.EDIT_TEXT,
        title = "Specify other substance",
        required = true,
        etMaxLength = 200,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT
    )

    private val substanceFrequency = FormElement(
        id = 305,
        inputType = InputType.DROPDOWN,
        title = "Frequency of substance use",
        entries = substanceFrequencyOptions,
        required = true
    )

    private val briefInterventionGiven = FormElement(
        id = 306,
        inputType = InputType.RADIO,
        title = "Brief intervention given",
        entries = arrayOf("Yes", "No"),
        required = true
    )

    // ── Section 4: Suicide Risk Screening ────────────────────────────
    // Enabled by Q3=Yes (shown AFTER PHQ-9)

    private val suicideHeader = FormElement(
        id = 400,
        inputType = InputType.HEADLINE,
        title = "Suicide Risk Screening",
        required = false
    )

    private val suicideCurrentThoughts = FormElement(
        id = 401,
        inputType = InputType.RADIO,
        title = "Current thoughts of self-harm or ending life",
        entries = arrayOf("Yes", "No"),
        required = true
    )

    private val suicidePlan = FormElement(
        id = 402,
        inputType = InputType.RADIO,
        title = "Has a specific plan to harm self",
        entries = arrayOf("Yes", "No"),
        required = true
    )

    private val suicidePreviousAttempt = FormElement(
        id = 403,
        inputType = InputType.RADIO,
        title = "Previous suicide attempt",
        entries = arrayOf("Yes", "No"),
        required = true
    )

    private val suicideHopelessness = FormElement(
        id = 404,
        inputType = InputType.RADIO,
        title = "Expressed hopelessness or being a burden",
        entries = arrayOf("Yes", "No"),
        required = true
    )

    private val suicideRiskLevel = FormElement(
        id = 405,
        inputType = InputType.DROPDOWN,
        title = "Suicide risk level",
        entries = suicideRiskOptions,
        required = true
    )

    // ── Section 5: Dementia Screening Checklist ──────────────────────
    // Enabled by Q4=Yes

    private val dementiaHeader = FormElement(
        id = 500,
        inputType = InputType.HEADLINE,
        title = "Dementia Screening Checklist",
        required = false
    )

    private val dementiaProgressiveMemoryLoss = FormElement(
        id = 501,
        inputType = InputType.RADIO,
        title = "Progressive memory loss",
        entries = arrayOf("Yes", "No"),
        required = true
    )

    private val dementiaForgettingRecent = FormElement(
        id = 502,
        inputType = InputType.RADIO,
        title = "Forgetting recent events",
        entries = arrayOf("Yes", "No"),
        required = true
    )

    private val dementiaDisorientation = FormElement(
        id = 503,
        inputType = InputType.RADIO,
        title = "Disorientation to time/place",
        entries = arrayOf("Yes", "No"),
        required = true
    )

    private val dementiaDailyActivities = FormElement(
        id = 504,
        inputType = InputType.RADIO,
        title = "Difficulty managing daily activities",
        entries = arrayOf("Yes", "No"),
        required = true
    )

    private val dementiaBehaviouralChanges = FormElement(
        id = 505,
        inputType = InputType.RADIO,
        title = "Behavioural changes",
        entries = arrayOf("Yes", "No"),
        required = true
    )

    // ── Section 6: Epilepsy Screening Checklist ──────────────────────
    // Enabled by Q5=Yes

    private val epilepsyHeader = FormElement(
        id = 600,
        inputType = InputType.HEADLINE,
        title = "Epilepsy Screening Checklist",
        required = false
    )

    private val epilepsyRecurrentSeizures = FormElement(
        id = 601,
        inputType = InputType.RADIO,
        title = "Recurrent seizures/fits",
        entries = arrayOf("Yes", "No"),
        required = true
    )

    private val epilepsyJerkyMovements = FormElement(
        id = 602,
        inputType = InputType.RADIO,
        title = "Jerky movements during episode",
        entries = arrayOf("Yes", "No"),
        required = true
    )

    private val epilepsyTongueBite = FormElement(
        id = 603,
        inputType = InputType.RADIO,
        title = "Tongue bite or incontinence during episode",
        entries = arrayOf("Yes", "No"),
        required = true
    )

    private val epilepsyConfusionAfter = FormElement(
        id = 604,
        inputType = InputType.RADIO,
        title = "Confusion after episode",
        entries = arrayOf("Yes", "No"),
        required = true
    )

    private val epilepsyLocDuration = FormElement(
        id = 605,
        inputType = InputType.DROPDOWN,
        title = "Duration of loss of consciousness",
        entries = epilepsyDurationOptions,
        required = true
    )

    // ── Section F: Referral & Follow-up ──────────────────────────────

    override val referralRequired = createReferralRequired(701)
    override val referralLevel = createReferralLevel(702)
    override val reasonForReferral = FormElement(
        id = 703,
        inputType = InputType.DROPDOWN,
        title = "Reason for referral",
        entries = arrayOf(
            "Depression (PHQ-9 score \u226510)",
            "Suicide risk identified",
            "Substance use disorder",
            "Dementia suspected",
            "Epilepsy suspected",
            "Post-partum depression",
            "Severe mental disorder suspected",
            "Other"
        ),
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

        if (substanceUseConcerns.value == "Yes") {
            list.addAll(substanceElements)
            if (substanceOtherUse.value == "Yes") {
                list.add(list.indexOf(substanceOtherUse) + 1, substanceOtherSpecify)
            }
        }

        if (selfHarmSuicideThoughts.value == "Yes") {
            list.addAll(suicideElements)
        }

        if (memoryLossConfusion.value == "Yes") {
            list.addAll(dementiaElements)
        }

        if (seizuresFitsLoc.value == "Yes") {
            list.addAll(epilepsyElements)
        }

        // Section F
        addReferralFollowUpElements(list)

        setUpPage(list)
    }

    private fun shouldShowPhq9(): Boolean {
        return emotionalBehaviouralConcerns.value == "Yes" ||
                selfHarmSuicideThoughts.value == "Yes" ||
                isPostpartum.value == "Yes"
    }

    // ── Value Change Handler ─────────────────────────────────────────

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
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
            cache.emotionalBehaviouralConcerns?.let { if (it) "Yes" else "No" }
        substanceUseConcerns.value =
            cache.substanceUseConcerns?.let { if (it) "Yes" else "No" }
        selfHarmSuicideThoughts.value =
            cache.selfHarmSuicideThoughts?.let { if (it) "Yes" else "No" }
        memoryLossConfusion.value =
            cache.memoryLossConfusion?.let { if (it) "Yes" else "No" }
        seizuresFitsLoc.value =
            cache.seizuresFitsLoc?.let { if (it) "Yes" else "No" }
        isPostpartum.value =
            cache.isPostpartum?.let { if (it) "Yes" else "No" }

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
            cache.substanceAlcoholUse?.let { if (it) "Yes" else "No" }
        substanceTobaccoUse.value =
            cache.substanceTobaccoUse?.let { if (it) "Yes" else "No" }
        substanceOtherUse.value =
            cache.substanceOtherUse?.let { if (it) "Yes" else "No" }
        substanceOtherSpecify.value = cache.substanceOtherSpecify
        substanceFrequency.value = cache.substanceFrequency
        briefInterventionGiven.value =
            cache.briefInterventionGiven?.let { if (it) "Yes" else "No" }

        // Suicide Risk
        suicideCurrentThoughts.value =
            cache.suicideCurrentThoughts?.let { if (it) "Yes" else "No" }
        suicidePlan.value =
            cache.suicidePlan?.let { if (it) "Yes" else "No" }
        suicidePreviousAttempt.value =
            cache.suicidePreviousAttempt?.let { if (it) "Yes" else "No" }
        suicideHopelessness.value =
            cache.suicideHopelessness?.let { if (it) "Yes" else "No" }
        suicideRiskLevel.value = cache.suicideRiskLevel

        // Dementia
        dementiaProgressiveMemoryLoss.value =
            cache.dementiaProgressiveMemoryLoss?.let { if (it) "Yes" else "No" }
        dementiaForgettingRecent.value =
            cache.dementiaForgettingRecent?.let { if (it) "Yes" else "No" }
        dementiaDisorientation.value =
            cache.dementiaDisorientation?.let { if (it) "Yes" else "No" }
        dementiaDailyActivities.value =
            cache.dementiaDailyActivities?.let { if (it) "Yes" else "No" }
        dementiaBehaviouralChanges.value =
            cache.dementiaBehaviouralChanges?.let { if (it) "Yes" else "No" }

        // Epilepsy
        epilepsyRecurrentSeizures.value =
            cache.epilepsyRecurrentSeizures?.let { if (it) "Yes" else "No" }
        epilepsyJerkyMovements.value =
            cache.epilepsyJerkyMovements?.let { if (it) "Yes" else "No" }
        epilepsyTongueBite.value =
            cache.epilepsyTongueBite?.let { if (it) "Yes" else "No" }
        epilepsyConfusionAfter.value =
            cache.epilepsyConfusionAfter?.let { if (it) "Yes" else "No" }
        epilepsyLocDuration.value = cache.epilepsyLocDuration

        // Section F
        populateReferralFollowUpFromCache(cache)
    }

    // ── Map Values ───────────────────────────────────────────────────

    private fun extractPhq9Score(value: String?): Int? {
        return value?.firstOrNull()?.digitToIntOrNull()
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as MentalHealthScreeningCache).let {
            // Initial screening
            it.emotionalBehaviouralConcerns = emotionalBehaviouralConcerns.value == "Yes"
            it.substanceUseConcerns = substanceUseConcerns.value == "Yes"
            it.selfHarmSuicideThoughts = selfHarmSuicideThoughts.value == "Yes"
            it.memoryLossConfusion = memoryLossConfusion.value == "Yes"
            it.seizuresFitsLoc = seizuresFitsLoc.value == "Yes"
            it.isPostpartum = isPostpartum.value == "Yes"

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
            if (substanceUseConcerns.value == "Yes") {
                it.substanceAlcoholUse = substanceAlcoholUse.value == "Yes"
                it.substanceTobaccoUse = substanceTobaccoUse.value == "Yes"
                it.substanceOtherUse = substanceOtherUse.value == "Yes"
                it.substanceOtherSpecify =
                    if (it.substanceOtherUse == true) substanceOtherSpecify.value else null
                it.substanceFrequency = substanceFrequency.value
                it.briefInterventionGiven = briefInterventionGiven.value == "Yes"
            } else {
                it.substanceAlcoholUse = null
                it.substanceTobaccoUse = null
                it.substanceOtherUse = null
                it.substanceOtherSpecify = null
                it.substanceFrequency = null
                it.briefInterventionGiven = null
            }

            // Suicide Risk
            if (selfHarmSuicideThoughts.value == "Yes") {
                it.suicideCurrentThoughts = suicideCurrentThoughts.value == "Yes"
                it.suicidePlan = suicidePlan.value == "Yes"
                it.suicidePreviousAttempt = suicidePreviousAttempt.value == "Yes"
                it.suicideHopelessness = suicideHopelessness.value == "Yes"
                it.suicideRiskLevel = suicideRiskLevel.value
            } else {
                it.suicideCurrentThoughts = null
                it.suicidePlan = null
                it.suicidePreviousAttempt = null
                it.suicideHopelessness = null
                it.suicideRiskLevel = null
            }

            // Dementia
            if (memoryLossConfusion.value == "Yes") {
                it.dementiaProgressiveMemoryLoss = dementiaProgressiveMemoryLoss.value == "Yes"
                it.dementiaForgettingRecent = dementiaForgettingRecent.value == "Yes"
                it.dementiaDisorientation = dementiaDisorientation.value == "Yes"
                it.dementiaDailyActivities = dementiaDailyActivities.value == "Yes"
                it.dementiaBehaviouralChanges = dementiaBehaviouralChanges.value == "Yes"
            } else {
                it.dementiaProgressiveMemoryLoss = null
                it.dementiaForgettingRecent = null
                it.dementiaDisorientation = null
                it.dementiaDailyActivities = null
                it.dementiaBehaviouralChanges = null
            }

            // Epilepsy
            if (seizuresFitsLoc.value == "Yes") {
                it.epilepsyRecurrentSeizures = epilepsyRecurrentSeizures.value == "Yes"
                it.epilepsyJerkyMovements = epilepsyJerkyMovements.value == "Yes"
                it.epilepsyTongueBite = epilepsyTongueBite.value == "Yes"
                it.epilepsyConfusionAfter = epilepsyConfusionAfter.value == "Yes"
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