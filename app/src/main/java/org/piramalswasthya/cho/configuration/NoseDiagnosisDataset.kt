package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.NoseDiagnosisAssessment
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType


class NoseDiagnosisDataset(
    private val context: Context,
    currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private lateinit var cache: NoseDiagnosisAssessment

    var onShowAlert: ((String) -> Unit)? = null

    private val optionYes = context.getString(R.string.yes)
    private val optionNo = context.getString(R.string.no)

    /* -------------------- FORM ELEMENTS -------------------- */

    private val difficultyBreathing = FormElement(
        id = 1,
        inputType = InputType.RADIO,
        title = context.getString(R.string.difficulty_in_breathing),
        entries = arrayOf(optionYes, optionNo),
        trueIndex = 0,
        falseIndex = 1,
        required = true,
        hasAlertError = true
    )

    private val openMouthBreathing = FormElement(
        id = 2,
        inputType = InputType.RADIO,
        title = context.getString(R.string.open_mouth_breathing),
        entries = arrayOf(optionYes, optionNo),
        trueIndex = 0,
        falseIndex = 1,
        required = false,
        hasAlertError = true
    )


    suspend fun setUpPage(savedRecord: NoseDiagnosisAssessment?) {
        cache = savedRecord ?: createDefaultCache()
        populateFromCache(cache)
        val list = mutableListOf<FormElement>()
        list.add(difficultyBreathing)
        list.add(openMouthBreathing)
        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {

            difficultyBreathing.id -> {
                difficultyBreathing.booleanValue = when (index) {
                    difficultyBreathing.trueIndex -> true
                    difficultyBreathing.falseIndex -> false
                    else -> null
                }
                if (difficultyBreathing.booleanValue == true) {
                    onShowAlert?.invoke(
                        context.getString(R.string.difficulty_in_breathing_alert)
                    )
                }
                -1
            }

            openMouthBreathing.id -> {
                openMouthBreathing.booleanValue = when (index) {
                    openMouthBreathing.trueIndex -> true
                    openMouthBreathing.falseIndex -> false
                    else -> null
                }
                if (openMouthBreathing.booleanValue == true) {
                    onShowAlert?.invoke(
                        context.getString(R.string.open_mouth_breathing_alert)
                    )
                }
                -1
            }
            else -> -1
        }
    }


    private fun createDefaultCache(): NoseDiagnosisAssessment {
        return NoseDiagnosisAssessment(
            patientID = "",
            benVisitNo = null
        )
    }

    private fun populateFromCache(cache: NoseDiagnosisAssessment) {
        difficultyBreathing.booleanValue = cache.difficultyBreathing
        difficultyBreathing.value = when (cache.difficultyBreathing) {
            true -> optionYes
            false -> optionNo
            else -> null
        }

        openMouthBreathing.booleanValue = cache.openMouthBreathing
        openMouthBreathing.value = when (cache.openMouthBreathing) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as NoseDiagnosisAssessment).let {
            it.difficultyBreathing = difficultyBreathing.booleanValue
            it.openMouthBreathing = openMouthBreathing.booleanValue
        }
    }
}
