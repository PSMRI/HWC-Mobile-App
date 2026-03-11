package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.PsychosocialCaregiverSupport
import java.util.concurrent.TimeUnit

class PsychosocialCaregiverSupportDataset(
    context: Context,
    currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private lateinit var cache: PsychosocialCaregiverSupport

    // ---------------- Psychosocial counselling ----------------
    private val psychosocialCounsellingProvided = FormElement(
        id = 1,
        inputType = InputType.RADIO,
        title = "Psychosocial counselling provided",
        entries = arrayOf("Yes", "No"),
        required = true
    )

    // ---------------- Caregiver counselling ----------------
    private val caregiverCounsellingProvided = FormElement(
        id = 2,
        inputType = InputType.RADIO,
        title = "Caregiver counselling provided",
        entries = arrayOf("Yes", "No"),
        required = true
    )

    // ---------------- Caregiver distress ----------------
    private val caregiverDistressIdentified = FormElement(
        id = 3,
        inputType = InputType.CHECKBOXES,
        title = "Caregiver distress identified",
        entries = arrayOf("Yes"),
        required = false
    )

    // ---------------- Counselling remarks ----------------
    private val counsellingRemarks = FormElement(
        id = 4,
        inputType = InputType.EDIT_TEXT,
        title = "Counselling remarks",
        required = false,
        etMaxLength = 250,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT
    )

    // ---------------- Section F: Referral & Follow-up ----------------

    private val referralRequired = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = "Referral required",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val referralLevel = FormElement(
        id = 6,
        inputType = InputType.DROPDOWN,
        title = "Referral level",
        entries = arrayOf("PHC", "CHC", "District Hospital", "Palliative Care Unit"),
        required = false
    )

    private val reasonForReferral = FormElement(
        id = 7,
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
        id = 8,
        inputType = InputType.RADIO,
        title = "Follow-up required",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val followUpDate = FormElement(
        id = 9,
        inputType = InputType.DATE_PICKER,
        title = "Follow-up date",
        required = false
    )

    // ---------------- Setup Page ----------------
    suspend fun setUpPage(savedRecord: PsychosocialCaregiverSupport?) {
        cache = savedRecord ?: createDefaultCache()
        populateFromCache(cache)

        val list = mutableListOf<FormElement>()
        list.add(psychosocialCounsellingProvided)
        list.add(caregiverCounsellingProvided)
        list.add(caregiverDistressIdentified)
        list.add(counsellingRemarks)

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
    private fun createDefaultCache(): PsychosocialCaregiverSupport {
        return PsychosocialCaregiverSupport(
            patientID = "",
            benVisitNo = null
        )
    }

    private fun populateFromCache(cache: PsychosocialCaregiverSupport) {
        psychosocialCounsellingProvided.value = when (cache.psychosocialCounsellingProvided) {
            true -> "Yes"
            false -> "No"
            else -> null
        }

        caregiverCounsellingProvided.value = when (cache.caregiverCounsellingProvided) {
            true -> "Yes"
            false -> "No"
            else -> null
        }

        caregiverDistressIdentified.value =
            if (cache.caregiverDistressIdentified == true) "Yes" else null

        counsellingRemarks.value = cache.counsellingRemarks

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
        (cacheModel as PsychosocialCaregiverSupport).let {

            it.psychosocialCounsellingProvided =
                psychosocialCounsellingProvided.value == "Yes"

            it.caregiverCounsellingProvided =
                caregiverCounsellingProvided.value == "Yes"

            it.caregiverDistressIdentified =
                caregiverDistressIdentified.value == "Yes"

            it.counsellingRemarks = counsellingRemarks.value

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