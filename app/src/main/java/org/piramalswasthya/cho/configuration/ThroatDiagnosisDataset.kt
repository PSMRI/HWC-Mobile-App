package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.ThroatDiagnosisAssessment

class ThroatDiagnosisDataset(
    context: Context,
    currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private lateinit var cache: ThroatDiagnosisAssessment

    var onShowAlert: ((String) -> Unit)? = null

    /* -------------------- FORM ELEMENTS -------------------- */

    private val throatSymptomsPresent = FormElement(
        id = 10,
        inputType = InputType.RADIO,
        title = "Throat Symptoms Present?",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val symptoms = FormElement(
        id = 1,
        inputType = InputType.CHECKBOXES,
        title = "Symptoms",
        entries = arrayOf(
            "Pain",
            "Soreness",
            "Cold",
            "Itching",
            "Hoarseness"
        ),
        required = true
    )

    private val neckSwelling = FormElement(
        id = 2,
        inputType = InputType.RADIO,
        title = "Swelling in the neck (Thyroid)",
        entries = arrayOf("Yes", "No"),
        required = false,
        hasAlertError = true
    )

    private val difficultySwallowing = FormElement(
        id = 3,
        inputType = InputType.RADIO,
        title = "Difficulty in Swallowing",
        entries = arrayOf("Yes", "No"),
        required = false,
        hasAlertError = true
    )

    private val tonsillitis = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = "Tonsillitis",
        entries = arrayOf("Yes", "No"),
        required = false
    )

    private val pharyngitis = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = "Pharyngitis",
        entries = arrayOf("Yes", "No"),
        required = false
    )

    private val laryngitis = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = "Laryngitis",
        entries = arrayOf("Yes", "No"),
        required = false
    )

    private val sinusitis = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = "Sinusitis",
        entries = arrayOf("Yes", "No"),
        required = false
    )

    private val cleftLip = FormElement(
        id = 8,
        inputType = InputType.RADIO,
        title = "Cleft Lip",
        entries = arrayOf("Yes", "No"),
        required = false,
        hasAlertError = true
    )

    private val cleftPalate = FormElement(
        id = 9,
        inputType = InputType.RADIO,
        title = "Cleft Palate",
        entries = arrayOf("Yes", "No"),
        required = false,
        hasAlertError = true
    )

    /* -------------------- PAGE SETUP -------------------- */

    suspend fun setUpPage(savedRecord: ThroatDiagnosisAssessment?) {
        cache = savedRecord ?: createDefaultCache()
        populateFromCache(cache)

        val list = mutableListOf<FormElement>()
        list.add(throatSymptomsPresent)
        if (throatSymptomsPresent.value == "Yes") {
            list.add(symptoms)
        }
        list.addAll(
            listOf(
                neckSwelling,
                difficultySwallowing,
                tonsillitis,
                pharyngitis,
                laryngitis,
                sinusitis,
                cleftLip,
                cleftPalate
            )
        )
        setUpPage(list)
    }

    /* -------------------- VALUE CHANGE -------------------- */

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        when (formId) {
            throatSymptomsPresent.id -> {
                if (index == 0) {
                    triggerDependants(
                        source = throatSymptomsPresent,
                        addItems = listOf(symptoms),
                        removeItems = emptyList()
                    )
                } else {
                    symptoms.value = null
                    triggerDependants(
                        source = throatSymptomsPresent,
                        addItems = emptyList(),
                        removeItems = listOf(symptoms)
                    )
                }
                throatSymptomsPresent.id
            }

            neckSwelling.id -> {
                if (index == 0) {
                    onShowAlert?.invoke(
                        "Neck swelling detected. Refer patient to specialist at secondary level."
                    )
                }
                neckSwelling.id
            }

            difficultySwallowing.id -> {
                if (index == 0) {
                    onShowAlert?.invoke(
                        "Difficulty in swallowing detected. Refer patient to specialist at secondary level."
                    )
                }
                difficultySwallowing.id
            }

            cleftLip.id -> {
                if (index == 0) {
                    onShowAlert?.invoke(
                        "Cleft lip detected. Refer patient to specialist at secondary level."
                    )
                }
                cleftLip.id
            }

            cleftPalate.id -> {
                if (index == 0) {
                    onShowAlert?.invoke(
                        "Cleft palate detected. Refer patient to specialist at secondary level."
                    )
                }
                cleftPalate.id
            }

            symptoms.id -> symptoms.id
        }
        return -1
    }

    /* -------------------- MULTI-SELECT TRIGGER -------------------- */

    var onTriggerMultiSelect: ((formId: Int, title: String, items: Array<String>, selectedItems: BooleanArray) -> Unit)? =
        null

    fun triggerMultiSelect(formId: Int) {
        when (formId) {
            symptoms.id -> {
                val symptomList = symptoms.entries!!
                val selectedSymptoms = symptoms.value?.split(",") ?: emptyList()
                val selectedItems = BooleanArray(symptomList.size) {
                    selectedSymptoms.contains(symptomList[it])
                }
                onTriggerMultiSelect?.invoke(
                    formId,
                    symptoms.title!!,
                    symptomList,
                    selectedItems
                )
            }
        }
    }

    suspend fun updateMultiSelectValue(formId: Int, selectedItems: List<String>) {
        val valueString = if (selectedItems.isEmpty()) null else selectedItems.joinToString(", ")
        when (formId) {
            symptoms.id -> {
                symptoms.value = valueString
            }
        }
        updateList(formId, -1)
    }

    /* -------------------- CACHE -------------------- */

    private fun createDefaultCache() =
        ThroatDiagnosisAssessment(patientID = "", benVisitNo = null)

    private fun populateFromCache(cache: ThroatDiagnosisAssessment) {
        throatSymptomsPresent.value = if (cache.symptoms.isNullOrEmpty()) "No" else "Yes"
        symptoms.value = cache.symptoms?.joinToString(", ")
        neckSwelling.value = when (cache.neckSwelling) {
            true -> "Yes"
            false -> "No"
            else -> null
        }
        difficultySwallowing.value = when (cache.difficultySwallowing) {
            true -> "Yes"
            false -> "No"
            else -> null
        }
        tonsillitis.value = when (cache.tonsillitis) {
            true -> "Yes"
            false -> "No"
            else -> null
        }
        pharyngitis.value = when (cache.pharyngitis) {
            true -> "Yes"
            false -> "No"
            else -> null
        }
        laryngitis.value = when (cache.laryngitis) {
            true -> "Yes"
            false -> "No"
            else -> null
        }
        sinusitis.value = when (cache.sinusitis) {
            true -> "Yes"
            false -> "No"
            else -> null
        }
        cleftLip.value = when (cache.cleftLip) {
            true -> "Yes"
            false -> "No"
            else -> null
        }
        cleftPalate.value = when (cache.cleftPalate) {
            true -> "Yes"
            false -> "No"
            else -> null
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as ThroatDiagnosisAssessment).apply {
            symptoms =
                this@ThroatDiagnosisDataset.symptoms.value?.split(", ")?.filter { it.isNotBlank() }
            neckSwelling = when (this@ThroatDiagnosisDataset.neckSwelling.value) {
                "Yes" -> true
                "No" -> false
                else -> null
            }
            difficultySwallowing = when (this@ThroatDiagnosisDataset.difficultySwallowing.value) {
                "Yes" -> true
                "No" -> false
                else -> null
            }
            tonsillitis = when (this@ThroatDiagnosisDataset.tonsillitis.value) {
                "Yes" -> true
                "No" -> false
                else -> null
            }
            pharyngitis = when (this@ThroatDiagnosisDataset.pharyngitis.value) {
                "Yes" -> true
                "No" -> false
                else -> null
            }
            laryngitis = when (this@ThroatDiagnosisDataset.laryngitis.value) {
                "Yes" -> true
                "No" -> false
                else -> null
            }
            sinusitis = when (this@ThroatDiagnosisDataset.sinusitis.value) {
                "Yes" -> true
                "No" -> false
                else -> null
            }
            cleftLip = when (this@ThroatDiagnosisDataset.cleftLip.value) {
                "Yes" -> true
                "No" -> false
                else -> null
            }
            cleftPalate = when (this@ThroatDiagnosisDataset.cleftPalate.value) {
                "Yes" -> true
                "No" -> false
                else -> null
            }
        }
    }
}