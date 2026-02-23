package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.EarDiagnosisAssessment
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType

class EarDiagnosisDataset(
    context: Context,
    currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private lateinit var cache: EarDiagnosisAssessment

    var onShowAlert: ((String) -> Unit)? = null


    private val difficultyHearing = FormElement(
        id = 1,
        inputType = InputType.RADIO,
        title = "Difficulty Hearing",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val whisperTestResponse = FormElement(
        id = 2,
        inputType = InputType.DROPDOWN,
        title = "Whisper Test Response",
        entries = arrayOf("Correct", "Incorrect"),
        required = true
    )

    private val hearingTestOutcome = FormElement(
        id = 3,
        inputType = InputType.DROPDOWN,
        title = "Hearing Test Outcome",
        entries = arrayOf("Normal", "Slight Loss", "Moderate", "Severe", "Deaf"),
        required = true,
        hasAlertError = true
    )

    private val earPain = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = "Earache / Ear Pain",
        entries = arrayOf("Yes", "No"),
        required = false
    )

    private val earDischarge = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = "Ear Discharge Present",
        entries = arrayOf("Yes", "No"),
        required = false
    )

    private val foreignBody = FormElement(
        id = 6,
        inputType = InputType.DROPDOWN,
        title = "Foreign Body Present in Ear",
        entries = arrayOf("Yes (Superficial)", "Yes (Deep)", "No"),
        required = false,
        hasAlertError = true
    )

    private val earConditionType = FormElement(
        id = 7,
        inputType = InputType.CHECKBOXES,
        title = "Type of Ear Condition",
        entries = arrayOf(
            "Otomycosis",
            "Otitis Externa",
            "Acute Ear Discharge",
            "Chronic Ear Discharge",
            "Ear Wax"
        ),
        required = false
    )

    private val congenitalMalformation = FormElement(
        id = 8,
        inputType = InputType.RADIO,
        title = "Congenital Ear Malformation",
        entries = arrayOf("Yes", "No"),
        required = false,
        hasAlertError = true
    )


    suspend fun setUpPage(savedRecord: EarDiagnosisAssessment?) {
        cache = savedRecord ?: createDefaultCache()

        populateFromCache(cache)

        val list = mutableListOf<FormElement>()
        list.add(difficultyHearing)

        if (difficultyHearing.value == "Yes") {
            list.add(whisperTestResponse)
            list.add(hearingTestOutcome)
        }

        list.addAll(
            listOf(
                earPain,
                earDischarge,
                foreignBody,
                earConditionType,
                congenitalMalformation
            )
        )

        setUpPage(list)
    }


    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {

            difficultyHearing.id -> {
                if (index == 0) {
                    triggerDependants(
                        source = difficultyHearing,
                        addItems = listOf(whisperTestResponse, hearingTestOutcome),
                        removeItems = emptyList()
                    )
                } else {
                    whisperTestResponse.value = null
                    hearingTestOutcome.value = null
                    triggerDependants(
                        source = difficultyHearing,
                        addItems = emptyList(),
                        removeItems = listOf(whisperTestResponse, hearingTestOutcome)
                    )
                }
                difficultyHearing.id
            }

            hearingTestOutcome.id -> {
                if (hearingTestOutcome.value != "Normal") {
                    onShowAlert?.invoke(
                        "Abnormal hearing detected. Refer patient to specialist at secondary level."
                    )
                }
                -1
            }

            else -> -1
        }
    }

    private fun createDefaultCache(): EarDiagnosisAssessment {
        return EarDiagnosisAssessment(
            patientID = "",
            benVisitNo = null
        )
    }

    private fun populateFromCache(cache: EarDiagnosisAssessment) {
        difficultyHearing.value = when (cache.difficultyHearing) {
            true -> "Yes"
            false -> "No"
            else -> null
        }

        whisperTestResponse.value = cache.whisperTestResponse
        hearingTestOutcome.value = cache.hearingTestOutcome
    }



    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as EarDiagnosisAssessment).let {

            it.difficultyHearing = difficultyHearing.value == "Yes"

            it.whisperTestResponse = whisperTestResponse.value

        }
    }
}
