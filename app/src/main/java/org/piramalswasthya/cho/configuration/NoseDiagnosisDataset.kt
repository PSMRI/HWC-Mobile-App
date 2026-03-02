package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.NoseDiagnosisAssessment
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType


class NoseDiagnosisDataset(
    context: Context,
    currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private lateinit var cache: NoseDiagnosisAssessment

    var onShowAlert: ((String) -> Unit)? = null

    /* -------------------- FORM ELEMENTS -------------------- */

    private val difficultyBreathing = FormElement(
        id = 1,
        inputType = InputType.RADIO,
        title = "Difficulty in Breathing",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasAlertError = true
    )

    private val openMouthBreathing = FormElement(
        id = 2,
        inputType = InputType.RADIO,
        title = "Open Mouth Breathing",
        entries = arrayOf("Yes", "No"),
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
                if (index == 0) {
                    onShowAlert?.invoke(
                        "Difficulty in breathing detected. Diagnose URI / Rhinitis / Sinusitis. Refer if not manageable at HWC."
                    )
                }
                -1
            }

            openMouthBreathing.id -> {
                if (index == 0) {
                    onShowAlert?.invoke(
                        "Open mouth breathing detected. Diagnose URI / Rhinitis / Sinusitis. Refer if not manageable at HWC."
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
        difficultyBreathing.value = when (cache.difficultyBreathing) {
            true -> "Yes"
            false -> "No"
            else -> null
        }

        openMouthBreathing.value = when (cache.openMouthBreathing) {
            true -> "Yes"
            false -> "No"
            else -> null
        }
    }
    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as NoseDiagnosisAssessment).let {
            it.difficultyBreathing = difficultyBreathing.value == "Yes"
            it.openMouthBreathing = openMouthBreathing.value == "Yes"

        }
    }
}
