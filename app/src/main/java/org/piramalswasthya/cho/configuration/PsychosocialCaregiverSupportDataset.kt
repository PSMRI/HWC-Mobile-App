package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.PsychosocialCaregiverSupport
import org.piramalswasthya.cho.configuration.FormDataModel

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

    // ---------------- Setup Page ----------------
    suspend fun setUpPage(savedRecord: PsychosocialCaregiverSupport?) {
        cache = savedRecord ?: createDefaultCache()
        populateFromCache(cache)

        val list = mutableListOf<FormElement>().apply {
            add(psychosocialCounsellingProvided)
            add(caregiverCounsellingProvided)
            add(caregiverDistressIdentified)
            add(counsellingRemarks)
        }

        setUpPage(list)
    }

    // ---------------- Value Change Handler ----------------
    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return -1 // No dependent logic required for this section
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
        }
    }
}