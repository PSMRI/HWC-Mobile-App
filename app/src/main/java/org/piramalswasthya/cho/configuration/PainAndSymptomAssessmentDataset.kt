package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.PainAndSymptomAssessment

class PainAndSymptomAssessmentDataset(
    context: Context,
    currentLanguage: Languages
) : ReferralFollowUpDataset(context, currentLanguage) {

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

    override val referralRequired = createReferralRequired(6)

    override val referralLevel = createReferralLevel(7)

    override val reasonForReferral = createReasonForReferral(8)

    override val followUpRequired = createFollowUpRequired(9)

    override val followUpDate = createFollowUpDate(10)

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
        addReferralFollowUpElements(list)

        setUpPage(list)
    }

    // ---------------- Value Change Handler ----------------
    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        val referralFollowUpResult = handleReferralFollowUpChange(formId, index)
        if (referralFollowUpResult != -1) return referralFollowUpResult

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
        populateReferralFollowUpFromCache(cache)
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
            mapReferralFollowUpValues(it)
        }
    }
}
