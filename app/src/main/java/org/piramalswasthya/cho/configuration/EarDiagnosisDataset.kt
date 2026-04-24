package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.EarDiagnosisAssessment
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType

class EarDiagnosisDataset(
    private val context: Context,
    currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private lateinit var cache: EarDiagnosisAssessment

    var onShowAlert: ((String) -> Unit)? = null

    private val optionYes = context.getString(R.string.yes_option)
    private val optionNo = context.getString(R.string.no_option)

    private val difficultyHearing = FormElement(
        id = 1,
        inputType = InputType.RADIO,
        title = context.getString(R.string.ear_difficulty_hearing),
        entries = arrayOf(optionYes, optionNo),
        required = true,
        hasDependants = true
    )

    private val whisperTestResponse = FormElement(
        id = 2,
        inputType = InputType.DROPDOWN,
        title = context.getString(R.string.ear_whisper_test_response),
        entries = arrayOf(
            context.getString(R.string.ear_whisper_correct),
            context.getString(R.string.ear_whisper_incorrect)
        ),
        required = true
    )

    private val hearingTestOutcome = FormElement(
        id = 3,
        inputType = InputType.DROPDOWN,
        title = context.getString(R.string.ear_hearing_test_outcome),
        entries = arrayOf(
            context.getString(R.string.ear_hearing_normal),
            context.getString(R.string.ear_hearing_slight_loss),
            context.getString(R.string.ear_hearing_moderate),
            context.getString(R.string.ear_hearing_severe),
            context.getString(R.string.ear_hearing_deaf)
        ),
        required = true,
        hasAlertError = true
    )

    private val earPain = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = context.getString(R.string.ear_pain),
        entries = arrayOf(optionYes, optionNo),
        required = false
    )

    private val earDischarge = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = context.getString(R.string.ear_discharge_present),
        entries = arrayOf(optionYes, optionNo),
        required = false
    )

    private val optionForeignBodySuperficial = context.getString(R.string.ear_foreign_body_superficial)
    private val optionForeignBodyDeep = context.getString(R.string.ear_foreign_body_deep)

    private val foreignBody = FormElement(
        id = 6,
        inputType = InputType.DROPDOWN,
        title = context.getString(R.string.ear_foreign_body),
        entries = arrayOf(optionForeignBodySuperficial, optionForeignBodyDeep, optionNo),
        required = false,
        hasAlertError = true
    )

    private val earConditionType = FormElement(
        id = 7,
        inputType = InputType.CHECKBOXES,
        title = context.getString(R.string.ear_condition_type),
        entries = arrayOf(
            context.getString(R.string.ear_otomycosis),
            context.getString(R.string.ear_otitis_externa),
            context.getString(R.string.ear_acute_discharge),
            context.getString(R.string.ear_chronic_discharge),
            context.getString(R.string.ear_wax)
        ),
        required = false
    )

    private val congenitalMalformation = FormElement(
        id = 8,
        inputType = InputType.RADIO,
        title = context.getString(R.string.ear_congenital_malformation),
        entries = arrayOf(optionYes, optionNo),
        required = false,
        hasAlertError = true
    )


    suspend fun setUpPage(savedRecord: EarDiagnosisAssessment?) {
        cache = savedRecord ?: createDefaultCache()

        populateFromCache(cache)

        val list = mutableListOf<FormElement>()
        list.add(difficultyHearing)

        if (difficultyHearing.value == optionYes) {
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
                val normalOption = context.getString(R.string.ear_hearing_normal)
                if (hearingTestOutcome.value != normalOption) {
                    onShowAlert?.invoke(
                        context.getString(R.string.ear_alert_abnormal_hearing)
                    )
                }
                -1
            }
            foreignBody.id -> {
                if (foreignBody.value == optionForeignBodyDeep) {
                    onShowAlert?.invoke(
                        context.getString(R.string.ear_alert_deep_foreign_body)
                    )
                }
                -1
            }
            congenitalMalformation.id -> {
                if (congenitalMalformation.value == optionYes) {
                    onShowAlert?.invoke(
                        context.getString(R.string.ear_alert_congenital_malformation)
                    )
                }
                -1
            }
        else -> -1
        }
    }

    private fun createDefaultCache(): EarDiagnosisAssessment {
        return EarDiagnosisAssessment(
            patientId = "",
            benVisitNo = null
        )
    }

    private fun populateFromCache(cache: EarDiagnosisAssessment) {
        difficultyHearing.value = when (cache.difficultyHearing) {
            true -> optionYes
            false -> optionNo
            else -> null
        }

        whisperTestResponse.value = cache.whisperTestResponse
        hearingTestOutcome.value = cache.hearingTestOutcome
        earPain.value = when (cache.earPain) {
            true -> optionYes
            false -> optionNo
            else -> null
        }

        earDischarge.value = when (cache.earDischargePresent) {
            true -> optionYes
            false -> optionNo
            else -> null
        }

        foreignBody.value = cache.foreignBodyInEar
        earConditionType.value = cache.earConditionType

        congenitalMalformation.value = when (cache.congenitalEarMalformation) {
            true -> optionYes
            false -> optionNo
            else -> null
        }

    }




    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as EarDiagnosisAssessment).let {

            it.difficultyHearing = when (difficultyHearing.value) {
                optionYes -> true
                optionNo -> false
                else -> null
            }

            it.whisperTestResponse = whisperTestResponse.value
            it.hearingTestOutcome = hearingTestOutcome.value
            it.earPain = when (earPain.value) {
                optionYes -> true
                optionNo -> false
                else -> null
            }
            it.earDischargePresent = when (earDischarge.value) {
                optionYes -> true
                optionNo -> false
                else -> null
            }

            it.foreignBodyInEar = foreignBody.value
            it.earConditionType = earConditionType.value

            it.congenitalEarMalformation = when (congenitalMalformation.value) {
                optionYes -> true
                optionNo -> false
                else -> null
            }

        }
    }
}
