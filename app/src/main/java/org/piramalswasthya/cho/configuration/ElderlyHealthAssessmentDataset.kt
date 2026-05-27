package org.piramalswasthya.cho.configuration
import android.content.Context
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.ElderlyHealthAssessment
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType


class ElderlyHealthAssessmentDataset(
    private val context: Context,
    currentLanguage: Languages
) : ReferralFollowUpDataset(context, currentLanguage) {

    private lateinit var cache: ElderlyHealthAssessment

    private var patientAge: Int? = null

    var onShowAlert: ((String) -> Unit)? = null

    private val optionYes = context.getString(R.string.yes_option)
    private val optionNo = context.getString(R.string.no_option)
    private val optionIndependent = context.getString(R.string.elderly_independent)
    private val optionDependent = context.getString(R.string.elderly_dependent)

    private val optionNoDecline = context.getString(R.string.elderly_status_no_decline)
    private val optionPartialDependence = context.getString(R.string.elderly_status_partial_dependence)
    private val optionFunctionalDependence = context.getString(R.string.elderly_status_functional_dependence)
    private val optionHighlyDependent = context.getString(R.string.elderly_status_highly_dependent)
    private val optionUnknown = context.getString(R.string.elderly_status_unknown)

    private val optionSuspected = context.getString(R.string.elderly_outcome_suspected)
    private val optionNotSuspected = context.getString(R.string.elderly_outcome_not_suspected)

    private val geriatricComplaints = FormElement(
        id = 1,
        inputType = InputType.RADIO,
        title = context.getString(R.string.elderly_geriatric_complaints),
        entries = arrayOf(optionYes, optionNo),
        required = true
    )

    private val multipleChronicConditions = FormElement(
        id = 2,
        inputType = InputType.CHECKBOXES,
        title = context.getString(R.string.elderly_multiple_chronic),
        entries = arrayOf(optionYes),
        required = false
    )

    private val recentFalls = FormElement(
        id = 3,
        inputType = InputType.CHECKBOXES,
        title = context.getString(R.string.elderly_recent_falls),
        entries = arrayOf(optionYes),
        required = false
    )

    private val difficultyWalkingBalance = FormElement(
        id = 4,
        inputType = InputType.CHECKBOXES,
        title = context.getString(R.string.elderly_difficulty_walking),
        entries = arrayOf(optionYes),
        required = false
    )

    private val visualHearingDifficulty = FormElement(
        id = 5,
        inputType = InputType.CHECKBOXES,
        title = context.getString(R.string.elderly_visual_hearing),
        entries = arrayOf(optionYes),
        required = false
    )

    private val functionalDecline = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = context.getString(R.string.elderly_functional_decline),
        entries = arrayOf(optionYes, optionNo),
        required = true,
        hasAlertError = true,
        hasDependants = true
    )

    // ADL Assessment Fields
    private val functionalassesmentHeadline = FormElement(
        id = 32,
        inputType = InputType.HEADLINE,
        title = context.getString(R.string.elderly_functional_assessment_headline),
        required = false
    )
    private var bathing = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = context.getString(R.string.elderly_bathing),
        entries = arrayOf(optionIndependent, optionDependent),
        required = true,
        hasDependants = true
    )

    private var dressing = FormElement(
        id = 8,
        inputType = InputType.RADIO,
        title = context.getString(R.string.elderly_dressing),
        entries = arrayOf(optionIndependent, optionDependent),
        required = true,
        hasDependants = true
    )

    private var toileting = FormElement(
        id = 9,
        inputType = InputType.RADIO,
        title = context.getString(R.string.elderly_toileting),
        entries = arrayOf(optionIndependent, optionDependent),
        required = true,
        hasDependants = true
    )

    private var transferring = FormElement(
        id = 10,
        inputType = InputType.RADIO,
        title = context.getString(R.string.elderly_transferring),
        entries = arrayOf(optionIndependent, optionDependent),
        required = true,
        hasDependants = true
    )

    private var continence = FormElement(
        id = 11,
        inputType = InputType.RADIO,
        title = context.getString(R.string.elderly_continence),
        entries = arrayOf(optionIndependent, optionDependent),
        required = true,
        hasDependants = true
    )

    private var feeding = FormElement(
        id = 12,
        inputType = InputType.RADIO,
        title = context.getString(R.string.elderly_feeding),
        entries = arrayOf(optionIndependent, optionDependent),
        required = true,
        hasDependants = true
    )

    private var totalScore = FormElement(
        id = 13,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.elderly_total_score),
        required = false
    )

    private var functionalStatus = FormElement(
        id = 14,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.elderly_functional_status),
        required = false
    )

    private var functionalDeclineFlag = FormElement(
        id = 15,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.elderly_functional_decline_flag),
        required = false
    )

    private val memoryLoss = FormElement(
        id = 16,
        inputType = InputType.RADIO,
        title = context.getString(R.string.elderly_memory_loss),
        entries = arrayOf(optionYes, optionNo),
        required = true,
        hasAlertError = true,
        hasDependants = true
    )

    // ---- Section B: Dementia Screening Checklist ----

    private val dementiaSectionHeadline = FormElement(
        id = 17,
        inputType = InputType.HEADLINE,
        title = context.getString(R.string.elderly_dementia_headline),
        required = false
    )

    private val dementiaMemoryLoss = FormElement(
        id = 18,
        inputType = InputType.CHECKBOXES,
        title = context.getString(R.string.elderly_dementia_memory_loss),
        entries = arrayOf(optionYes),
        required = false,
        hasDependants = true
    )

    private val dementiaDisorientation = FormElement(
        id = 19,
        inputType = InputType.CHECKBOXES,
        title = context.getString(R.string.elderly_dementia_disorientation),
        entries = arrayOf(optionYes),
        required = false,
        hasDependants = true
    )

    private val dementiaBehaviouralChanges = FormElement(
        id = 20,
        inputType = InputType.CHECKBOXES,
        title = context.getString(R.string.elderly_dementia_behavioural),
        entries = arrayOf(optionYes),
        required = false,
        hasDependants = true
    )

    private val dementiaSelfCareDecline = FormElement(
        id = 21,
        inputType = InputType.CHECKBOXES,
        title = context.getString(R.string.elderly_dementia_self_care),
        entries = arrayOf(optionYes),
        required = false,
        hasDependants = true
    )

    private var dementiaScreeningOutcome = FormElement(
        id = 22,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.elderly_screening_outcome),
        required = false
    )

    private var dementiaReferralRequired = FormElement(
        id = 23,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.elderly_referral_required),
        required = false
    )


    suspend fun setUpPage(
        savedRecord: ElderlyHealthAssessment?,
        patientAge: Int? = null
    ) {
        this.patientAge = patientAge
        cache = savedRecord ?: createDefaultCache()

        val list = mutableListOf<FormElement>()

        populateFromCache(cache)

        list.add(geriatricComplaints)
        list.add(multipleChronicConditions)
        list.add(recentFalls)
        list.add(difficultyWalkingBalance)
        list.add(visualHearingDifficulty)
        list.add(functionalDecline)

        if (functionalDecline.value == optionYes) {
            list.addAll(getADLFields())
            computeADLScore()
        }

        list.add(memoryLoss)

        // Section B: Dementia Screening – enabled only if Memory loss = Yes AND age >= 60
        if (memoryLoss.value == optionYes && patientAge != null && patientAge >= 60) {
            list.addAll(getDementiaSectionFields())
            computeDementiaOutcome()
        }
        addReferralFollowUpElements(list)

        setUpPage(list)
    }

    private fun getOptionalComplaintFields(): List<FormElement> {
        return listOf(
            multipleChronicConditions,
            recentFalls,
            difficultyWalkingBalance,
            visualHearingDifficulty
        )
    }

    private fun getADLFields(): List<FormElement> {
        return listOf(
            functionalassesmentHeadline,
            bathing,
            dressing,
            toileting,
            transferring,
            continence,
            feeding,
            totalScore,
            functionalStatus,
            functionalDeclineFlag
        )
    }

    private fun getADLAnswerFields(): List<FormElement> {
        return listOf(
            bathing,
            dressing,
            toileting,
            transferring,
            continence,
            feeding
        )
    }

    private fun getDementiaCheckboxFields(): List<FormElement> {
        return listOf(
            dementiaMemoryLoss,
            dementiaDisorientation,
            dementiaBehaviouralChanges,
            dementiaSelfCareDecline
        )
    }

    private fun getDementiaSectionFields(): List<FormElement> {
        return listOf(
            dementiaSectionHeadline,
            dementiaMemoryLoss,
            dementiaDisorientation,
            dementiaBehaviouralChanges,
            dementiaSelfCareDecline,
            dementiaScreeningOutcome,
            dementiaReferralRequired
        )
    }
    override val referralRequired = createReferralRequired(24)
    override val referralLevel = createReferralLevel(25)
    override val reasonForReferral = createReasonForReferral(26)
    override val followUpRequired = createFollowUpRequired(27)
    override val followUpDate = createFollowUpDate(28)
    override val caseStatus = createCaseStatus(29)
    override val dateOfDeath = createDateOfDeath(30)
    override val remarks = createRemarks(31)

    private suspend fun computeDementiaOutcome() {
        val anySelected = listOf(
            dementiaMemoryLoss,
            dementiaDisorientation,
            dementiaBehaviouralChanges,
            dementiaSelfCareDecline
        ).any { it.value == optionYes }

        val newOutcomeValue = if (anySelected) optionSuspected else optionNotSuspected
        val newReferralValue = if (anySelected) optionYes else optionNo

        // Find existing indices in the actual list BEFORE updating the references
        val oldOutcomeIndex = getIndexById(dementiaScreeningOutcome.id)
        val oldReferralIndex = getIndexById(dementiaReferralRequired.id)

        dementiaScreeningOutcome = dementiaScreeningOutcome.copy(value = newOutcomeValue)
        dementiaReferralRequired = dementiaReferralRequired.copy(value = newReferralValue)

        // If these elements are currently visible in the list, update them in place
        // This ensures DiffUtil detects the new objects without adding duplicates
        if (oldOutcomeIndex != -1) {
            setUpPage(
                mList = getFormList().toMutableList().apply {
                    this[oldOutcomeIndex] = dementiaScreeningOutcome
                    if (oldReferralIndex != -1) {
                        this[oldReferralIndex] = dementiaReferralRequired
                    }
                }
            )
        }
    }

    private suspend fun computeADLScore() {
        // Find existing indices in the actual list BEFORE updating the references
        val oldTotalIndex = getIndexById(totalScore.id)
        val oldStatusIndex = getIndexById(functionalStatus.id)
        val oldFlagIndex = getIndexById(functionalDeclineFlag.id)

        val adlFields = getADLAnswerFields()
        val allAnswered = adlFields.all { !it.value.isNullOrBlank() }

        if (!allAnswered) {
            totalScore = totalScore.copy(value = null)
            functionalStatus = functionalStatus.copy(value = null)
            functionalDeclineFlag = functionalDeclineFlag.copy(value = null)
        } else {
            val scores = adlFields.map { field ->
                when (field.value) {
                    optionIndependent -> 1
                    optionDependent -> 0
                    else -> 0
                }
            }
            val total = scores.sum()

            val status = when (total) {
                6 -> optionNoDecline
                in 4..5 -> optionPartialDependence
                in 2..3 -> optionFunctionalDependence
                in 0..1 -> optionHighlyDependent
                else -> optionUnknown
            }

            val flag = if (total <= 5) optionYes else optionNo

            totalScore = totalScore.copy(value = total.toString())
            functionalStatus = functionalStatus.copy(value = status)
            functionalDeclineFlag = functionalDeclineFlag.copy(value = flag)
        }

        if (oldTotalIndex != -1) {
            setUpPage(
                mList = getFormList().toMutableList().apply {
                    this[oldTotalIndex] = totalScore
                    if (oldStatusIndex != -1) {
                        this[oldStatusIndex] = functionalStatus
                    }
                    if (oldFlagIndex != -1) {
                        this[oldFlagIndex] = functionalDeclineFlag
                    }
                }
            )
        }
    }


    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        val referralFollowUpResult = handleReferralFollowUpChange(formId, index)
        if (referralFollowUpResult != -1) return referralFollowUpResult
        return when (formId) {

            functionalDecline.id -> {
                if (index == 0) {
                    onShowAlert?.invoke(context.getString(R.string.elderly_alert_functional_decline))
                }
                handleFunctionalDeclineChange(index)
            }

            bathing.id, dressing.id, toileting.id, transferring.id, continence.id, feeding.id -> {
                computeADLScore()
                getIndexById(totalScore.id)
            }

            memoryLoss.id -> handleMemoryLossChange(index)

            dementiaMemoryLoss.id,
            dementiaDisorientation.id,
            dementiaBehaviouralChanges.id,
            dementiaSelfCareDecline.id -> {
                computeDementiaOutcome()
                getIndexById(dementiaScreeningOutcome.id)
            }

            else -> -1
        }
    }



    private suspend fun handleMemoryLossChange(index: Int): Int {
        if (index == 0) {
            onShowAlert?.invoke(context.getString(R.string.elderly_alert_memory_loss))
        }

        val isEligible = patientAge != null && patientAge!! >= 60

        return if (index == 0 && isEligible) {
            // Memory loss = Yes and age >= 60 → show Section B
            triggerDependants(
                source = memoryLoss,
                addItems = getDementiaSectionFields(),
                removeItems = emptyList()
            )
            computeDementiaOutcome()
            memoryLoss.id
        } else {
            // Memory loss = No or age < 60 → hide Section B and clear values
            getDementiaSectionFields().forEach {
                it.value = null
            }
            triggerDependants(
                source = memoryLoss,
                addItems = emptyList(),
                removeItems = getDementiaSectionFields()
            )
            memoryLoss.id
        }
    }

    private suspend fun handleFunctionalDeclineChange(index: Int): Int {
        return if (index == 0) {
            // Functional decline = Yes → show ADL fields
            triggerDependants(
                source = functionalDecline,
                addItems = getADLFields(),
                removeItems = emptyList()
            )
            computeADLScore()
            functionalDecline.id
        } else {

            getADLFields().forEach {
                it.value = null
            }
            triggerDependants(
                source = functionalDecline,
                addItems = emptyList(),
                removeItems = getADLFields()
            )
            functionalDecline.id
        }
    }

    private fun createDefaultCache(): ElderlyHealthAssessment {
        return ElderlyHealthAssessment(
            patientId = "",
            benVisitNo = 0,
            geriatricComplaints = null,
            multipleChronicConditions = null,
            recentFalls = null,
            difficultyWalkingBalance = null,
            visualHearingDifficulty = null,
            functionalDecline = null,
            bathing = null,
            dressing = null,
            toileting = null,
            transferring = null,
            continence = null,
            feeding = null,
            totalScore = null,
            functionalStatus = null,
            functionalDeclineFlag = null,
            memoryLoss = null,
            dementiaMemoryLoss = null,
            dementiaDisorientation = null,
            dementiaBehaviouralChanges = null,
            dementiaSelfCareDecline = null,
            dementiaScreeningOutcome = null,
            dementiaReferralRequired = null
        )
    }

    private fun populateFromCache(cache: ElderlyHealthAssessment) {
        geriatricComplaints.value = when(cache.geriatricComplaints) {
            true -> optionYes
            false -> optionNo
            else -> null
        }

        multipleChronicConditions.value =
            if (cache.multipleChronicConditions == true) optionYes else null

        recentFalls.value =
            if (cache.recentFalls == true) optionYes else null

        difficultyWalkingBalance.value =
            if (cache.difficultyWalkingBalance == true) optionYes else null

        visualHearingDifficulty.value =
            if (cache.visualHearingDifficulty == true) optionYes else null

        functionalDecline.value = when(cache.functionalDecline) {
            true -> optionYes
            false -> optionNo
            else -> null
        }

        bathing.value = when(cache.bathing) {
            1 -> optionIndependent
            0 -> optionDependent
            else -> null
        }

        dressing.value = when(cache.dressing) {
            1 -> optionIndependent
            0 -> optionDependent
            else -> null
        }

        toileting.value = when(cache.toileting) {
            1 -> optionIndependent
            0 -> optionDependent
            else -> null
        }

        transferring.value = when(cache.transferring) {
            1 -> optionIndependent
            0 -> optionDependent
            else -> null
        }

        continence.value = when(cache.continence) {
            1 -> optionIndependent
            0 -> optionDependent
            else -> null
        }

        feeding.value = when(cache.feeding) {
            1 -> optionIndependent
            0 -> optionDependent
            else -> null
        }

        totalScore.value = cache.totalScore?.toString()
        functionalStatus.value = cache.functionalStatus
        functionalDeclineFlag.value = when(cache.functionalDeclineFlag) {
            true -> optionYes
            false -> optionNo
            else -> null
        }

        memoryLoss.value = when(cache.memoryLoss) {
            true -> optionYes
            false -> optionNo
            else -> null
        }

        // Dementia fields
        dementiaMemoryLoss.value =
            if (cache.dementiaMemoryLoss == true) optionYes else null

        dementiaDisorientation.value =
            if (cache.dementiaDisorientation == true) optionYes else null

        dementiaBehaviouralChanges.value =
            if (cache.dementiaBehaviouralChanges == true) optionYes else null

        dementiaSelfCareDecline.value =
            if (cache.dementiaSelfCareDecline == true) optionYes else null

        dementiaScreeningOutcome.value = cache.dementiaScreeningOutcome
        dementiaReferralRequired.value = when(cache.dementiaReferralRequired) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
        populateReferralFollowUpFromCache(cache)
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as ElderlyHealthAssessment).let {

            it.geriatricComplaints =
                geriatricComplaints.value == optionYes

            it.multipleChronicConditions =
                multipleChronicConditions.value == optionYes

            it.recentFalls =
                recentFalls.value == optionYes

            it.difficultyWalkingBalance =
                difficultyWalkingBalance.value == optionYes

            it.visualHearingDifficulty =
                visualHearingDifficulty.value == optionYes

            it.functionalDecline =
                functionalDecline.value == optionYes

            it.bathing = when(bathing.value) {
                optionIndependent -> 1
                optionDependent -> 0
                else -> null
            }

            it.dressing = when(dressing.value) {
                optionIndependent -> 1
                optionDependent -> 0
                else -> null
            }

            it.toileting = when(toileting.value) {
                optionIndependent -> 1
                optionDependent -> 0
                else -> null
            }

            it.transferring = when(transferring.value) {
                optionIndependent -> 1
                optionDependent -> 0
                else -> null
            }

            it.continence = when(continence.value) {
                optionIndependent -> 1
                optionDependent -> 0
                else -> null
            }

            it.feeding = when(feeding.value) {
                optionIndependent -> 1
                optionDependent -> 0
                else -> null
            }

            it.totalScore = totalScore.value?.toIntOrNull()
            it.functionalStatus = functionalStatus.value
            it.functionalDeclineFlag = when (functionalDeclineFlag.value) {
                optionYes -> true
                optionNo -> false
                else -> null
            }

            it.memoryLoss =
                memoryLoss.value == optionYes

            // Dementia fields
            it.dementiaMemoryLoss =
                dementiaMemoryLoss.value == optionYes

            it.dementiaDisorientation =
                dementiaDisorientation.value == optionYes

            it.dementiaBehaviouralChanges =
                dementiaBehaviouralChanges.value == optionYes

            it.dementiaSelfCareDecline =
                dementiaSelfCareDecline.value == optionYes

            it.dementiaScreeningOutcome =
                dementiaScreeningOutcome.value

            it.dementiaReferralRequired =
                dementiaReferralRequired.value == optionYes
            mapReferralFollowUpValues(it)
        }
    }
}
