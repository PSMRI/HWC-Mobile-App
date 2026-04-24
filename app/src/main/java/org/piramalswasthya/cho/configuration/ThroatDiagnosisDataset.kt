package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.ThroatDiagnosisAssessment

class ThroatDiagnosisDataset(
    private val context: Context,
    currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private lateinit var cache: ThroatDiagnosisAssessment

    var onShowAlert: ((String) -> Unit)? = null

    private val optionYes = context.getString(R.string.yes)
    private val optionNo = context.getString(R.string.no)



    private val symptoms = FormElement(
        id = 1,
        inputType = InputType.CHECKBOXES,
        title = context.getString(R.string.throat_diagnosis_symptoms),
        entries = arrayOf(
            context.getString(R.string.throat_symptom_pain),
            context.getString(R.string.throat_symptom_soreness),
            context.getString(R.string.throat_symptom_cold),
            context.getString(R.string.throat_symptom_itching),
            context.getString(R.string.throat_symptom_hoarseness)
        ),
        required = true
    )
    private val symptomOptionIds = listOf(
        R.string.throat_symptom_pain,
        R.string.throat_symptom_soreness,
        R.string.throat_symptom_cold,
        R.string.throat_symptom_itching,
        R.string.throat_symptom_hoarseness
    )

    private val neckSwelling = FormElement(
        id = 2,
        inputType = InputType.RADIO,
        title = context.getString(R.string.throat_diagnosis_neck_swelling),
        entries = arrayOf(optionYes, optionNo),
        required = false,
        hasAlertError = true
    )

    private val difficultySwallowing = FormElement(
        id = 3,
        inputType = InputType.RADIO,
        title = context.getString(R.string.throat_diagnosis_difficulty_swallowing),
        entries = arrayOf(optionYes, optionNo),
        required = false,
        hasAlertError = true
    )

    private val tonsillitis = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = context.getString(R.string.throat_diagnosis_tonsillitis),
        entries = arrayOf(optionYes, optionNo),
        required = false
    )

    private val pharyngitis = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = context.getString(R.string.throat_diagnosis_pharyngitis),
        entries = arrayOf(optionYes, optionNo),
        required = false
    )

    private val laryngitis = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = context.getString(R.string.throat_diagnosis_laryngitis),
        entries = arrayOf(optionYes, optionNo),
        required = false
    )

    private val sinusitis = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = context.getString(R.string.throat_diagnosis_sinusitis),
        entries = arrayOf(optionYes, optionNo),
        required = false
    )

    private val cleftLip = FormElement(
        id = 8,
        inputType = InputType.RADIO,
        title = context.getString(R.string.throat_diagnosis_cleft_lip),
        entries = arrayOf(optionYes, optionNo),
        required = false,
        hasAlertError = true
    )

    private val cleftPalate = FormElement(
        id = 9,
        inputType = InputType.RADIO,
        title = context.getString(R.string.throat_diagnosis_cleft_palate),
        entries = arrayOf(optionYes, optionNo),
        required = false,
        hasAlertError = true
    )

    /* -------------------- PAGE SETUP -------------------- */

    suspend fun setUpPage(savedRecord: ThroatDiagnosisAssessment?) {
        cache = savedRecord ?: createDefaultCache()
        populateFromCache(cache)

        val list = mutableListOf<FormElement>()
        list.add(symptoms)
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
        return when (formId) {


            neckSwelling.id -> {
                if (index == 0) {
                    onShowAlert?.invoke(context.getString(R.string.throat_alert_neck_swelling))
                }
                neckSwelling.id
            }

            difficultySwallowing.id -> {
                if (index == 0) {
                    onShowAlert?.invoke(context.getString(R.string.throat_alert_difficulty_swallowing))
                }
                difficultySwallowing.id
            }

            cleftLip.id -> {
                if (index == 0) {
                    onShowAlert?.invoke(context.getString(R.string.throat_alert_cleft_lip))
                }
                cleftLip.id
            }

            cleftPalate.id -> {
                if (index == 0) {
                    onShowAlert?.invoke(context.getString(R.string.throat_alert_cleft_palate))
                }
                cleftPalate.id
            }

            symptoms.id -> symptoms.id

            else -> -1
        }
    }

    /* -------------------- MULTI-SELECT TRIGGER -------------------- */

    var onTriggerMultiSelect: ((formId: Int, title: String, items: Array<String>, selectedItems: BooleanArray) -> Unit)? =
        null

    fun triggerMultiSelect(formId: Int) {
        when (formId) {
            symptoms.id -> {
                val symptomList = symptoms.entries!!
                val selectedSymptoms = symptoms.value
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    ?: emptyList()
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
        ThroatDiagnosisAssessment(patientId = "", benVisitNo = null)

    private fun populateFromCache(cache: ThroatDiagnosisAssessment) {
        symptoms.value = getLocalizedSymptoms(cache.symptoms)?.joinToString(", ")
        neckSwelling.value = when (cache.neckSwelling) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
        difficultySwallowing.value = when (cache.difficultySwallowing) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
        tonsillitis.value = when (cache.tonsillitis) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
        pharyngitis.value = when (cache.pharyngitis) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
        laryngitis.value = when (cache.laryngitis) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
        sinusitis.value = when (cache.sinusitis) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
        cleftLip.value = when (cache.cleftLip) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
        cleftPalate.value = when (cache.cleftPalate) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as ThroatDiagnosisAssessment).apply {
            symptoms = getEnglishSymptoms(
                this@ThroatDiagnosisDataset.symptoms.value
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
            )
            neckSwelling = when (this@ThroatDiagnosisDataset.neckSwelling.value) {
                optionYes -> true
                optionNo -> false
                else -> null
            }
            difficultySwallowing = when (this@ThroatDiagnosisDataset.difficultySwallowing.value) {
                optionYes -> true
                optionNo -> false
                else -> null
            }
            tonsillitis = when (this@ThroatDiagnosisDataset.tonsillitis.value) {
                optionYes -> true
                optionNo -> false
                else -> null
            }
            pharyngitis = when (this@ThroatDiagnosisDataset.pharyngitis.value) {
                optionYes -> true
                optionNo -> false
                else -> null
            }
            laryngitis = when (this@ThroatDiagnosisDataset.laryngitis.value) {
                optionYes -> true
                optionNo -> false
                else -> null
            }
            sinusitis = when (this@ThroatDiagnosisDataset.sinusitis.value) {
                optionYes -> true
                optionNo -> false
                else -> null
            }
            cleftLip = when (this@ThroatDiagnosisDataset.cleftLip.value) {
                optionYes -> true
                optionNo -> false
                else -> null
            }
            cleftPalate = when (this@ThroatDiagnosisDataset.cleftPalate.value) {
                optionYes -> true
                optionNo -> false
                else -> null
            }
        }
    }

    private fun getLocalizedSymptoms(values: List<String>?): List<String>? {
        return values
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.map { entry ->
                symptomOptionIds.firstNotNullOfOrNull { id ->
                    val englishValue = englishResources.getString(id)
                    val localizedValue = resources.getString(id)
                    when (entry) {
                        englishValue, localizedValue -> localizedValue
                        else -> null
                    }
                } ?: entry
            }
            ?.takeIf { it.isNotEmpty() }
    }

    private fun getEnglishSymptoms(values: List<String>?): List<String>? {
        return values
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.map { entry ->
                symptomOptionIds.firstNotNullOfOrNull { id ->
                    val englishValue = englishResources.getString(id)
                    val localizedValue = resources.getString(id)
                    when (entry) {
                        localizedValue, englishValue -> englishValue
                        else -> null
                    }
                } ?: entry
            }
            ?.takeIf { it.isNotEmpty() }
    }
}
