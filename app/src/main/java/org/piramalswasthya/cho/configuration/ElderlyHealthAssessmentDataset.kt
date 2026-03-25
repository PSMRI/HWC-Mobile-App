package org.piramalswasthya.cho.configuration
import android.content.Context
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.ElderlyHealthAssessment
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType


class ElderlyHealthAssessmentDataset(
    context: Context,
    currentLanguage: Languages
) : Dataset(context, currentLanguage) {

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
        hasAlertError = true
    )

    private val memoryLoss = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = "Memory loss or confusion",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasAlertError = true,
        hasDependants = true
    )

    // ---- Section B: Dementia Screening Checklist ----

    private val dementiaSectionHeadline = FormElement(
        id = 8,
        inputType = InputType.HEADLINE,
        title = "Dementia Screening Checklist",
        required = false
    )

    private val dementiaMemoryLoss = FormElement(
        id = 9,
        inputType = InputType.CHECKBOXES,
        title = "Progressive memory loss",
        entries = arrayOf("Yes"),
        required = false,
        hasDependants = true
    )

    private val dementiaDisorientation = FormElement(
        id = 10,
        inputType = InputType.CHECKBOXES,
        title = "Disorientation (time/place/person)",
        entries = arrayOf("Yes"),
        required = false,
        hasDependants = true
    )

    private val dementiaBehaviouralChanges = FormElement(
        id = 11,
        inputType = InputType.CHECKBOXES,
        title = "Behavioural changes",
        entries = arrayOf("Yes"),
        required = false,
        hasDependants = true
    )

    private val dementiaSelfCareDecline = FormElement(
        id = 12,
        inputType = InputType.CHECKBOXES,
        title = "Decline in self-care / routine activities",
        entries = arrayOf("Yes"),
        required = false,
        hasDependants = true
    )

    private var dementiaScreeningOutcome = FormElement(
        id = 13,
        inputType = InputType.TEXT_VIEW,
        title = "Screening Outcome",
        required = false
    )

    private var dementiaReferralRequired = FormElement(
        id = 14,
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
        list.add(memoryLoss)

        // Section B: Dementia Screening – enabled only if Memory loss = Yes AND age >= 60
        if (memoryLoss.value == "Yes" && patientAge != null && patientAge >= 60) {
            list.addAll(getDementiaSectionFields())
            computeDementiaOutcome()
        }

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


    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {

            functionalDecline.id -> {
                if (index == 0) {
                    onShowAlert?.invoke("Patient has functional decline. Please refer to higher facility if needed.")
                }
                -1
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
        }
    }
}