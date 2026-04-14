package org.piramalswasthya.cho.configuration
import android.content.Context
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.ElderlyHealthAssessment
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType


class ElderlyHealthAssessmentDataset(
    context: Context,
    currentLanguage: Languages
) : ReferralFollowUpDataset(context, currentLanguage) {

    private lateinit var cache: ElderlyHealthAssessment

    private var patientAge: Int? = null

    var onShowAlert: ((String) -> Unit)? = null


    private val geriatricComplaints = FormElement(
        id = 1,
        inputType = InputType.RADIO,
        title = "General geriatric complaints present?",
        entries = arrayOf("Yes", "No"),
        required = true
    )

    private val multipleChronicConditions = FormElement(
        id = 2,
        inputType = InputType.CHECKBOXES,
        title = "Multiple chronic conditions",
        entries = arrayOf("Yes"),
        required = false
    )

    private val recentFalls = FormElement(
        id = 3,
        inputType = InputType.CHECKBOXES,
        title = "Recent falls",
        entries = arrayOf("Yes"),
        required = false
    )

    private val difficultyWalkingBalance = FormElement(
        id = 4,
        inputType = InputType.CHECKBOXES,
        title = "Difficulty in walking or balance",
        entries = arrayOf("Yes"),
        required = false
    )

    private val visualHearingDifficulty = FormElement(
        id = 5,
        inputType = InputType.CHECKBOXES,
        title = "Visual or hearing difficulty",
        entries = arrayOf("Yes"),
        required = false
    )

    private val functionalDecline = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = "Functional decline / difficulty in daily activities (ADL)",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasAlertError = true,
        hasDependants = true
    )

    // ADL Assessment Fields
    private val functionalassesmentHeadline = FormElement(
        id = 32,
        inputType = InputType.HEADLINE,
        title = "Functional Assessment section",
        required = false
    )
    private var bathing = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = "Bathing",
        entries = arrayOf("Independent (1)", "Dependent (0)"),
        required = true,
        hasDependants = true
    )

    private var dressing = FormElement(
        id = 8,
        inputType = InputType.RADIO,
        title = "Dressing",
        entries = arrayOf("Independent (1)", "Dependent (0)"),
        required = true,
        hasDependants = true
    )

    private var toileting = FormElement(
        id = 9,
        inputType = InputType.RADIO,
        title = "Toileting",
        entries = arrayOf("Independent (1)", "Dependent (0)"),
        required = true,
        hasDependants = true
    )

    private var transferring = FormElement(
        id = 10,
        inputType = InputType.RADIO,
        title = "Transferring",
        entries = arrayOf("Independent (1)", "Dependent (0)"),
        required = true,
        hasDependants = true
    )

    private var continence = FormElement(
        id = 11,
        inputType = InputType.RADIO,
        title = "Continence",
        entries = arrayOf("Independent (1)", "Dependent (0)"),
        required = true,
        hasDependants = true
    )

    private var feeding = FormElement(
        id = 12,
        inputType = InputType.RADIO,
        title = "Feeding",
        entries = arrayOf("Independent (1)", "Dependent (0)"),
        required = true,
        hasDependants = true
    )

    private var totalScore = FormElement(
        id = 13,
        inputType = InputType.TEXT_VIEW,
        title = "Total Score",
        required = false
    )

    private var functionalStatus = FormElement(
        id = 14,
        inputType = InputType.TEXT_VIEW,
        title = "Functional Status",
        required = false
    )

    private var functionalDeclineFlag = FormElement(
        id = 15,
        inputType = InputType.TEXT_VIEW,
        title = "Functional Decline Flag",
        required = false
    )

    private val memoryLoss = FormElement(
        id = 16,
        inputType = InputType.RADIO,
        title = "Memory loss or confusion",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasAlertError = true,
        hasDependants = true
    )

    // ---- Section B: Dementia Screening Checklist ----

    private val dementiaSectionHeadline = FormElement(
        id = 17,
        inputType = InputType.HEADLINE,
        title = "Dementia Screening Checklist",
        required = false
    )

    private val dementiaMemoryLoss = FormElement(
        id = 18,
        inputType = InputType.CHECKBOXES,
        title = "Progressive memory loss",
        entries = arrayOf("Yes"),
        required = false,
        hasDependants = true
    )

    private val dementiaDisorientation = FormElement(
        id = 19,
        inputType = InputType.CHECKBOXES,
        title = "Disorientation (time/place/person)",
        entries = arrayOf("Yes"),
        required = false,
        hasDependants = true
    )

    private val dementiaBehaviouralChanges = FormElement(
        id = 20,
        inputType = InputType.CHECKBOXES,
        title = "Behavioural changes",
        entries = arrayOf("Yes"),
        required = false,
        hasDependants = true
    )

    private val dementiaSelfCareDecline = FormElement(
        id = 21,
        inputType = InputType.CHECKBOXES,
        title = "Decline in self-care / routine activities",
        entries = arrayOf("Yes"),
        required = false,
        hasDependants = true
    )

    private var dementiaScreeningOutcome = FormElement(
        id = 22,
        inputType = InputType.TEXT_VIEW,
        title = "Screening Outcome",
        required = false
    )

    private var dementiaReferralRequired = FormElement(
        id = 23,
        inputType = InputType.TEXT_VIEW,
        title = "Referral Required",
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

        if (functionalDecline.value == "Yes") {
            list.addAll(getADLFields())
            computeADLScore()
        }

        list.add(memoryLoss)

        // Section B: Dementia Screening – enabled only if Memory loss = Yes AND age >= 60
        if (memoryLoss.value == "Yes" && patientAge != null && patientAge >= 60) {
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
        ).any { it.value == "Yes" }

        val newOutcomeValue = if (anySelected) "Suspected" else "Not suspected"
        val newReferralValue = if (anySelected) "Yes" else "No"

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
        val adlFields = listOf(bathing, dressing, toileting, transferring, continence, feeding)
        val scores = adlFields.map { field ->
            when (field.value) {
                "Independent (1)" -> 1
                "Dependent (0)" -> 0
                else -> 0
            }
        }
        val total = scores.sum()

        val status = when (total) {
            6 -> "No decline"
            in 4..5 -> "Partial Dependence"
            in 2..3 -> "Functional Dependence"
            in 0..1 -> "Highly Dependent"
            else -> "Unknown"
        }

        val flag = if (total  <= 5) "Yes" else "no"

        // Find existing indices in the actual list BEFORE updating the references
        val oldTotalIndex = getIndexById(totalScore.id)
        val oldStatusIndex = getIndexById(functionalStatus.id)
        val oldFlagIndex = getIndexById(functionalDeclineFlag.id)

        totalScore = totalScore.copy(value = total.toString())
        functionalStatus = functionalStatus.copy(value = status)
        functionalDeclineFlag = functionalDeclineFlag.copy(value = flag)

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
                    onShowAlert?.invoke("Patient has functional decline. Please refer to higher facility if needed.")
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
            onShowAlert?.invoke("Patient has memory loss. Please refer to higher facility if needed.")
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
            patientID = "",
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
            true -> "Yes"
            false -> "No"
            else -> null
        }

        multipleChronicConditions.value =
            if (cache.multipleChronicConditions == true) "Yes" else null

        recentFalls.value =
            if (cache.recentFalls == true) "Yes" else null

        difficultyWalkingBalance.value =
            if (cache.difficultyWalkingBalance == true) "Yes" else null

        visualHearingDifficulty.value =
            if (cache.visualHearingDifficulty == true) "Yes" else null

        functionalDecline.value = when(cache.functionalDecline) {
            true -> "Yes"
            false -> "No"
            else -> null
        }

        bathing.value = when(cache.bathing) {
            1 -> "Independent (1)"
            0 -> "Dependent (0)"
            else -> null
        }

        dressing.value = when(cache.dressing) {
            1 -> "Independent (1)"
            0 -> "Dependent (0)"
            else -> null
        }

        toileting.value = when(cache.toileting) {
            1 -> "Independent (1)"
            0 -> "Dependent (0)"
            else -> null
        }

        transferring.value = when(cache.transferring) {
            1 -> "Independent (1)"
            0 -> "Dependent (0)"
            else -> null
        }

        continence.value = when(cache.continence) {
            1 -> "Independent (1)"
            0 -> "Dependent (0)"
            else -> null
        }

        feeding.value = when(cache.feeding) {
            1 -> "Independent (1)"
            0 -> "Dependent (0)"
            else -> null
        }

        totalScore.value = cache.totalScore?.toString()
        functionalStatus.value = cache.functionalStatus
        functionalDeclineFlag.value = when(cache.functionalDeclineFlag) {
            true -> "Yes"
            false -> "No"
            else -> null
        }

        memoryLoss.value = when(cache.memoryLoss) {
            true -> "Yes"
            false -> "No"
            else -> null
        }

        // Dementia fields
        dementiaMemoryLoss.value =
            if (cache.dementiaMemoryLoss == true) "Yes" else null

        dementiaDisorientation.value =
            if (cache.dementiaDisorientation == true) "Yes" else null

        dementiaBehaviouralChanges.value =
            if (cache.dementiaBehaviouralChanges == true) "Yes" else null

        dementiaSelfCareDecline.value =
            if (cache.dementiaSelfCareDecline == true) "Yes" else null

        dementiaScreeningOutcome.value = cache.dementiaScreeningOutcome
        dementiaReferralRequired.value = when(cache.dementiaReferralRequired) {
            true -> "Yes"
            false -> "No"
            else -> null
        }
        populateReferralFollowUpFromCache(cache)
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as ElderlyHealthAssessment).let {

            it.geriatricComplaints =
                geriatricComplaints.value == "Yes"

            it.multipleChronicConditions =
                multipleChronicConditions.value == "Yes"

            it.recentFalls =
                recentFalls.value == "Yes"

            it.difficultyWalkingBalance =
                difficultyWalkingBalance.value == "Yes"

            it.visualHearingDifficulty =
                visualHearingDifficulty.value == "Yes"

            it.functionalDecline =
                functionalDecline.value == "Yes"

            it.bathing = when(bathing.value) {
                "Independent (1)" -> 1
                "Dependent (0)" -> 0
                else -> null
            }

            it.dressing = when(dressing.value) {
                "Independent (1)" -> 1
                "Dependent (0)" -> 0
                else -> null
            }

            it.toileting = when(toileting.value) {
                "Independent (1)" -> 1
                "Dependent (0)" -> 0
                else -> null
            }

            it.transferring = when(transferring.value) {
                "Independent (1)" -> 1
                "Dependent (0)" -> 0
                else -> null
            }

            it.continence = when(continence.value) {
                "Independent (1)" -> 1
                "Dependent (0)" -> 0
                else -> null
            }

            it.feeding = when(feeding.value) {
                "Independent (1)" -> 1
                "Dependent (0)" -> 0
                else -> null
            }

            it.totalScore = totalScore.value?.toIntOrNull()
            it.functionalStatus = functionalStatus.value
            it.functionalDeclineFlag = functionalDeclineFlag.value == "Yes"

            it.memoryLoss =
                memoryLoss.value == "Yes"

            // Dementia fields
            it.dementiaMemoryLoss =
                dementiaMemoryLoss.value == "Yes"

            it.dementiaDisorientation =
                dementiaDisorientation.value == "Yes"

            it.dementiaBehaviouralChanges =
                dementiaBehaviouralChanges.value == "Yes"

            it.dementiaSelfCareDecline =
                dementiaSelfCareDecline.value == "Yes"

            it.dementiaScreeningOutcome =
                dementiaScreeningOutcome.value

            it.dementiaReferralRequired =
                dementiaReferralRequired.value == "Yes"
            mapReferralFollowUpValues(it)
        }
    }
}
