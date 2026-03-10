package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.OralHealth

class OralHealthDataset(
    context: Context,
    currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    companion object {
        private const val YES = "Yes"
        private const val NO = "No"
    }

    private lateinit var cache: OralHealth
    private var lastSelectedToothDecaySymptoms: Set<String> = emptySet()
    private var lastSelectedGumDiseaseSymptoms: Set<String> = emptySet()
    var onShowAlert: ((String) -> Unit)? = null

    private val toothDecayPresent = FormElement(
        id = 1,
        inputType = InputType.RADIO,
        title = "Tooth Decay Present",
        entries = arrayOf(YES, NO),
        required = false,
        hasDependants = true
    )

    private val toothDecaySymptoms = FormElement(
        id = 2,
        inputType = InputType.CHECKBOXES,
        title = "Symptoms of Tooth Decay",
        entries = arrayOf(
            "Black spot",
            "Discoloration of tooth Cavity",
            "Hole in the tooth",
            "Sensitivity to hot and cold, sweet and sour",
            "Food lodgment in the cavity/ between teeth",
            "Pain",
            "Swelling",
            "Pus discharge"
        ),
        required = false,
        hasAlertError = true
    )

    private val gumDiseasePresent = FormElement(
        id = 3,
        inputType = InputType.RADIO,
        title = "Gum Diseases Present",
        entries = arrayOf(YES, NO),
        required = false,
        hasDependants = true
    )

    private val gumDiseaseSymptoms = FormElement(
        id = 4,
        inputType = InputType.CHECKBOXES,
        title = "Symptoms of Gum Diseases",
        entries = arrayOf(
            "Foul smell",
            "Bleeding gums",
            "Deposits and discoloration of tooth",
            "Loose teeth",
            "Widening gap between teeth",
            "Swollen gums"
        ),
        required = false,
        hasAlertError = true
    )

    suspend fun setUpPage(savedRecord: OralHealth?) {
        cache = savedRecord ?: createDefaultCache()
        populateFromCache(cache)
        lastSelectedToothDecaySymptoms = toSelectionSet(toothDecaySymptoms.value)
        lastSelectedGumDiseaseSymptoms = toSelectionSet(gumDiseaseSymptoms.value)

        val list = mutableListOf<FormElement>()
        list.add(toothDecayPresent)
        if (toothDecayPresent.value == YES) {
            toothDecaySymptoms.required = true
            list.add(toothDecaySymptoms)
        } else {
            toothDecaySymptoms.required = false
        }

        list.add(gumDiseasePresent)
        if (gumDiseasePresent.value == YES) {
            gumDiseaseSymptoms.required = true
            list.add(gumDiseaseSymptoms)
        } else {
            gumDiseaseSymptoms.required = false
        }

        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            toothDecayPresent.id -> {
                if (index == 0) {
                    toothDecaySymptoms.required = true
                    triggerDependants(
                        source = toothDecayPresent,
                        addItems = listOf(toothDecaySymptoms),
                        removeItems = emptyList()
                    )
                } else {
                    toothDecaySymptoms.value = null
                    toothDecaySymptoms.required = false
                    lastSelectedToothDecaySymptoms = emptySet()
                    triggerDependants(
                        source = toothDecayPresent,
                        addItems = emptyList(),
                        removeItems = listOf(toothDecaySymptoms)
                    )
                }
            }

            toothDecaySymptoms.id -> {
                val currentSelections = toSelectionSet(toothDecaySymptoms.value)
                val isNewSelection = currentSelections.size > lastSelectedToothDecaySymptoms.size
                if (isNewSelection) {
                    onShowAlert?.invoke(resources.getString(R.string.oral_health_referral_alert))
                }
                lastSelectedToothDecaySymptoms = currentSelections
                -1
            }

            gumDiseasePresent.id -> {
                if (index == 0) {
                    gumDiseaseSymptoms.required = true
                    triggerDependants(
                        source = gumDiseasePresent,
                        addItems = listOf(gumDiseaseSymptoms),
                        removeItems = emptyList()
                    )
                } else {
                    gumDiseaseSymptoms.value = null
                    gumDiseaseSymptoms.required = false
                    lastSelectedGumDiseaseSymptoms = emptySet()
                    triggerDependants(
                        source = gumDiseasePresent,
                        addItems = emptyList(),
                        removeItems = listOf(gumDiseaseSymptoms)
                    )
                }
            }

            gumDiseaseSymptoms.id -> {
                val currentSelections = toSelectionSet(gumDiseaseSymptoms.value)
                val isNewSelection = currentSelections.size > lastSelectedGumDiseaseSymptoms.size
                if (isNewSelection) {
                    onShowAlert?.invoke(resources.getString(R.string.oral_health_referral_alert))
                }
                lastSelectedGumDiseaseSymptoms = currentSelections
                -1
            }

            else -> -1
        }
    }

    private fun createDefaultCache(): OralHealth {
        return OralHealth(
            patientID = "",
            benVisitNo = null
        )
    }

    private fun toSelectionSet(value: String?): Set<String> {
        return value
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.toSet()
            ?: emptySet()
    }

    private fun populateFromCache(cache: OralHealth) {
        toothDecayPresent.value = when (cache.toothDecayPresent) {
            true -> YES
            false -> NO
            else -> null
        }
        toothDecaySymptoms.value = cache.toothDecaySymptoms

        gumDiseasePresent.value = when (cache.gumDiseasePresent) {
            true -> YES
            false -> NO
            else -> null
        }
        gumDiseaseSymptoms.value = cache.gumDiseaseSymptoms
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as OralHealth).let {
            it.toothDecayPresent = when (toothDecayPresent.value) {
                YES -> true
                NO -> false
                else -> null
            }
            it.toothDecaySymptoms = toothDecaySymptoms.value

            it.gumDiseasePresent = when (gumDiseasePresent.value) {
                YES -> true
                NO -> false
                else -> null
            }
            it.gumDiseaseSymptoms = gumDiseaseSymptoms.value
        }
    }
}

