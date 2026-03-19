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
//        list.add(symptomsPresent)
//
//        if (symptomsPresent.value == "Yes") {
//            otherSymptomsSeverity.required = true
//            list.add(otherSymptomsSeverity)
//        }

        // Directly show otherSymptomsSeverity
        list.add(otherSymptomsSeverity)

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

//            symptomsPresent.id -> {
//                if (index == 0) { // Yes
//                    otherSymptomsSeverity.required = true
//                    triggerDependants(
//                        source = symptomsPresent,
//                        addItems = listOf(otherSymptomsSeverity),
//                        removeItems = emptyList()
//                    )
//                } else { // No
//                    otherSymptomsSeverity.value = null
//                    otherSymptomsSeverity.required = false
//                    triggerDependants(
//                        source = symptomsPresent,
//                        addItems = emptyList(),
//                        removeItems = listOf(otherSymptomsSeverity)
//                    )
//                }
//                symptomsPresent.id
//            }

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

    private fun populateFromCache(cache: PainAndSymptomAssessment) {
        painSeverity.value = cache.painSeverity
        painDuration.value = cache.painDuration
//        symptomsPresent.value = when (cache.symptomsPresent) {
//            true -> "Yes"
//            false -> "No"
//            else -> null
//        }
        otherSymptomsSeverity.value = cache.otherSymptomsSeverity
        immediateReliefProvided.value = when (cache.immediateReliefProvided) {
            true -> optionYes
            false -> optionNo
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

//            it.symptomsPresent = when (symptomsPresent.value) {
//                "Yes" -> true
//                "No" -> false
//                else -> null
//            }

            it.otherSymptomsSeverity = otherSymptomsSeverity.value

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