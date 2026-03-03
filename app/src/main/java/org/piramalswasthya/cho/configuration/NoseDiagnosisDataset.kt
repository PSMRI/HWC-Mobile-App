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
    private val noseBleed = FormElement(
        id = 3,
        inputType = InputType.RADIO,
        title = "Nose Bleed (Epistaxis)",
        entries = arrayOf("Yes", "No"),
        required = false,
        hasDependants = true
    )

    private val systolicBP = FormElement(
        id = 4,
        inputType = InputType.EDIT_TEXT,
        title = "Systolic BP (mmHg)",
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        hasAlertError = true

    )

    private val diastolicBP = FormElement(
        id = 5,
        inputType = InputType.EDIT_TEXT,
        title = "Diastolic BP (mmHg)",
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        hasAlertError = true

    )

    private val foreignBodyNose = FormElement(
        id = 6,
        inputType = InputType.DROPDOWN,
        title = "Foreign Body Nose",
        entries = arrayOf(
            "Yes (anterior visible)",
            "Yes (posterior visible)",
            "No"
        ),
        required = false,
        hasAlertError = true

    )

    private val sinusitis = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = "Sinusitis",
        entries = arrayOf("Yes (Facial pain / tenderness)", "No"),
        required = false,
        hasAlertError = true
    )


    suspend fun setUpPage(savedRecord: NoseDiagnosisAssessment?) {
        cache = savedRecord ?: createDefaultCache()
        populateFromCache(cache)
        val list = mutableListOf<FormElement>()
        list.add(difficultyBreathing)
        list.add(openMouthBreathing)
        list.add(noseBleed)

        if (noseBleed.value == "Yes") {
            list.add(systolicBP)
            list.add(diastolicBP)
        }

        list.addAll(
            listOf(
                foreignBodyNose,
                sinusitis
            )
        )

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
            noseBleed.id -> {
                if (index == 0) {
                    triggerDependants(
                        source = noseBleed,
                        addItems = listOf(systolicBP, diastolicBP),
                        removeItems = emptyList()
                    )
                } else {
                    systolicBP.value = null
                    diastolicBP.value = null
                    triggerDependants(
                        source = noseBleed,
                        addItems = emptyList(),
                        removeItems = listOf(systolicBP, diastolicBP)
                    )
                }
                noseBleed.id
            }

            systolicBP.id -> {
                systolicBP.value?.toIntOrNull()?.let {
                    if (it > 120) {
                        onShowAlert?.invoke(
                            "Systolic BP > 120 mmHg. Refer patient to specialist at secondary level."
                        )
                    }
                }
                -1
            }

            diastolicBP.id -> {
                diastolicBP.value?.toIntOrNull()?.let {
                    if (it > 80) {
                        onShowAlert?.invoke(
                            "Diastolic BP > 80 mmHg. Refer patient to specialist at secondary level."
                        )
                    }
                }
                -1
            }

            foreignBodyNose.id -> {
                if (foreignBodyNose.value == "Yes (posterior visible)") {
                    onShowAlert?.invoke(
                        "Posterior nasal foreign body detected. Refer patient to specialist at secondary level."
                    )
                }
                -1
            }

            sinusitis.id -> {
                if (index == 0) {
                    onShowAlert?.invoke(
                        "Chronic sinusitis suspected. Refer patient to specialist at secondary level."
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
        noseBleed.value = when (cache.noseBleed) {
            true -> "Yes"
            false -> "No"
            else -> null
        }

        systolicBP.value = cache.systolicBP?.toString()
        diastolicBP.value = cache.diastolicBP?.toString()

        foreignBodyNose.value = cache.foreignBodyNose
        sinusitis.value = when (cache.sinusitis) {
            true -> "Yes"
            false -> "No"
            else -> null
        }
    }
    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as NoseDiagnosisAssessment).let {
            it.difficultyBreathing = difficultyBreathing.value == "Yes"
            it.openMouthBreathing = openMouthBreathing.value == "Yes"
            it.noseBleed = noseBleed.value == "Yes"

            it.systolicBP = systolicBP.value?.toIntOrNull()
            it.diastolicBP = diastolicBP.value?.toIntOrNull()

            it.foreignBodyNose = foreignBodyNose.value
            it.sinusitis = sinusitis.value == "Yes"

        }
    }
}
