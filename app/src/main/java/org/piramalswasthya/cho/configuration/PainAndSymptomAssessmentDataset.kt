package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.PainAndSymptomAssessment
import java.util.concurrent.TimeUnit

class PainAndSymptomAssessmentDataset(
    context: Context,
    currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private lateinit var cache: PainAndSymptomAssessment

    var onShowAlert: ((String) -> Unit)? = null

    // ---------------- Pain Severity ----------------
    private val painSeverity = FormElement(
        id = 1,
        inputType = InputType.DROPDOWN,
        title = "Pain severity",
        entries = arrayOf("Mild", "Moderate", "Severe"),
        required = true,
        hasDependants = true,
        hasAlertError = true
    )

    // ---------------- Pain Duration ----------------
    private val painDuration = FormElement(
        id = 2,
        inputType = InputType.DROPDOWN,
        title = "Pain duration",
        entries = arrayOf("< 1 month", "1–6 months", "> 6 months"),
        required = true
    )

    // ---------------- Other Symptoms Present ----------------
    private val symptomsPresent = FormElement(
        id = 3,
        inputType = InputType.RADIO,
        title = "Other symptoms present",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    // ---------------- Other Symptoms Severity ----------------
    private val otherSymptomsSeverity = FormElement(
        id = 4,
        inputType = InputType.DROPDOWN,
        title = "Other symptoms severity",
        entries = arrayOf("Mild", "Moderate", "Severe"),
        required = false
    )

    // ---------------- Immediate Relief ----------------
    private val immediateReliefProvided = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = "Immediate relief provided",
        entries = arrayOf("Yes", "No"),
        required = true
    )

    // ---------------- Section F: Referral & Follow-up ----------------

    private val referralRequired = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = "Referral required",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val referralLevel = FormElement(
        id = 7,
        inputType = InputType.DROPDOWN,
        title = "Referral level",
        entries = arrayOf("PHC", "CHC", "District Hospital", "Palliative Care Unit"),
        required = false
    )

    private val reasonForReferral = FormElement(
        id = 8,
        inputType = InputType.DROPDOWN,
        title = "Reason for referral",
        entries = arrayOf(
            "Severe pain",
            "Functional dependence",
            "Dementia suspected",
            "End-of-life care"
        ),
        required = true
    )

    private val followUpRequired = FormElement(
        id = 9,
        inputType = InputType.RADIO,
        title = "Follow-up required",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val followUpDate = FormElement(
        id = 10,
        inputType = InputType.DATE_PICKER,
        title = "Follow-up date",
        required = false
    )

    // ---------------- Setup Page ----------------
    suspend fun setUpPage(savedRecord: PainAndSymptomAssessment?) {
        cache = savedRecord ?: createDefaultCache()
        populateFromCache(cache)

        val list = mutableListOf<FormElement>()
        list.add(painSeverity)
        list.add(painDuration)
        list.add(symptomsPresent)

        if (symptomsPresent.value == "Yes") {
            otherSymptomsSeverity.required = true
            list.add(otherSymptomsSeverity)
        }

        list.add(immediateReliefProvided)

        // Section F
        list.add(referralRequired)
        if (referralRequired.value == "Yes") {
            referralLevel.required = true
            list.add(referralLevel)
        }
        list.add(reasonForReferral)
        list.add(followUpRequired)
        if (followUpRequired.value == "Yes") {
            followUpDate.required = true
            followUpDate.min = System.currentTimeMillis()
            followUpDate.max = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365 * 10)
            list.add(followUpDate)
        }

        setUpPage(list)
    }

    // ---------------- Value Change Handler ----------------
    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {

            symptomsPresent.id -> {
                if (index == 0) { // Yes
                    otherSymptomsSeverity.required = true
                    triggerDependants(
                        source = symptomsPresent,
                        addItems = listOf(otherSymptomsSeverity),
                        removeItems = emptyList()
                    )
                } else { // No
                    otherSymptomsSeverity.value = null
                    otherSymptomsSeverity.required = false
                    triggerDependants(
                        source = symptomsPresent,
                        addItems = emptyList(),
                        removeItems = listOf(otherSymptomsSeverity)
                    )
                }
                symptomsPresent.id
            }

            painSeverity.id -> {
                if (painSeverity.value == "Severe") {
                    onShowAlert?.invoke(
                        "Severe pain detected. Referral to higher facility is recommended."
                    )
                }
                -1
            }

            otherSymptomsSeverity.id -> {
                if (otherSymptomsSeverity.value == "Severe") {
                    onShowAlert?.invoke(
                        "Severe symptoms detected. Referral to higher facility is recommended."
                    )
                }
                -1
            }

            referralRequired.id -> {
                if (index == 0) { // Yes
                    referralLevel.required = true
                    triggerDependants(
                        source = referralRequired,
                        addItems = listOf(referralLevel),
                        removeItems = emptyList()
                    )
                } else { // No
                    referralLevel.value = null
                    referralLevel.required = false
                    triggerDependants(
                        source = referralRequired,
                        addItems = emptyList(),
                        removeItems = listOf(referralLevel)
                    )
                }
                referralRequired.id
            }

            followUpRequired.id -> {
                if (index == 0) { // Yes
                    followUpDate.required = true
                    followUpDate.min = System.currentTimeMillis()
                    followUpDate.max = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365 * 10)
                    triggerDependants(
                        source = followUpRequired,
                        addItems = listOf(followUpDate),
                        removeItems = emptyList()
                    )
                } else { // No
                    followUpDate.value = null
                    followUpDate.required = false
                    triggerDependants(
                        source = followUpRequired,
                        addItems = emptyList(),
                        removeItems = listOf(followUpDate)
                    )
                }
                followUpRequired.id
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

    private fun populateFromCache(cache: PainAndSymptomAssessment) {
        painSeverity.value = cache.painSeverity
        painDuration.value = cache.painDuration
        symptomsPresent.value = when (cache.symptomsPresent) {
            true -> "Yes"
            false -> "No"
            else -> null
        }
        otherSymptomsSeverity.value = cache.otherSymptomsSeverity
        immediateReliefProvided.value = when (cache.immediateReliefProvided) {
            true -> "Yes"
            false -> "No"
            else -> null
        }

        // Section F
        referralRequired.value = when (cache.referralRequired) {
            true -> "Yes"
            false -> "No"
            else -> null
        }
        referralLevel.value = cache.referralLevel
        reasonForReferral.value = cache.reasonForReferral
        followUpRequired.value = when (cache.followUpRequired) {
            true -> "Yes"
            false -> "No"
            else -> null
        }
        followUpDate.value = cache.followUpDate
    }

    // ---------------- Map Values ----------------
    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as PainAndSymptomAssessment).let {

            it.painSeverity = painSeverity.value
            it.painDuration = painDuration.value

            it.symptomsPresent = when (symptomsPresent.value) {
                "Yes" -> true
                "No" -> false
                else -> null
            }

            it.otherSymptomsSeverity = otherSymptomsSeverity.value

            it.immediateReliefProvided = when (immediateReliefProvided.value) {
                "Yes" -> true
                "No" -> false
                else -> null
            }

            // Section F
            it.referralRequired = when (referralRequired.value) {
                "Yes" -> true
                "No" -> false
                else -> null
            }
            it.referralLevel = referralLevel.value
            it.reasonForReferral = reasonForReferral.value
            it.followUpRequired = when (followUpRequired.value) {
                "Yes" -> true
                "No" -> false
                else -> null
            }
            it.followUpDate = followUpDate.value
        }
    }
}