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

    private val optionYes = context.getString(R.string.yes_option)
    private val optionNo = context.getString(R.string.no_option)

    private lateinit var cache: OralHealth
    private var lastSelectedToothDecaySymptoms: Set<String> = emptySet()
    private var lastSelectedGumDiseaseSymptoms: Set<String> = emptySet()
        private var lastSelectedDentalEmergencySelections: Set<String> = emptySet()
    var onShowAlert: ((String) -> Unit)? = null

    private val toothDecayPresent = FormElement(
        id = 1,
        inputType = InputType.RADIO,
        title = context.getString(R.string.oral_tooth_decay_present),
        entries = arrayOf(optionYes, optionNo),
        required = false,
        hasDependants = true
    )

    private val toothDecaySymptoms = FormElement(
        id = 2,
        inputType = InputType.CHECKBOXES,
        title = context.getString(R.string.oral_tooth_decay_symptoms),
        entries = arrayOf(
            context.getString(R.string.oral_symptom_black_spot),
            context.getString(R.string.oral_symptom_discoloration),
            context.getString(R.string.oral_symptom_hole),
            context.getString(R.string.oral_symptom_sensitivity),
            context.getString(R.string.oral_symptom_food_lodgment),
            context.getString(R.string.oral_symptom_pain),
            context.getString(R.string.oral_symptom_swelling),
            context.getString(R.string.oral_symptom_pus_discharge)
        ),
        required = false,
        hasAlertError = true
    )

    private val gumDiseasePresent = FormElement(
        id = 3,
        inputType = InputType.RADIO,
        title = context.getString(R.string.oral_gum_disease_present),
        entries = arrayOf(optionYes, optionNo),
        required = false,
        hasDependants = true
    )

    private val gumDiseaseSymptoms = FormElement(
        id = 4,
        inputType = InputType.CHECKBOXES,
        title = context.getString(R.string.oral_gum_disease_symptoms),
        entries = arrayOf(
            context.getString(R.string.oral_gum_foul_smell),
            context.getString(R.string.oral_gum_bleeding),
            context.getString(R.string.oral_gum_deposits),
            context.getString(R.string.oral_gum_loose_teeth),
            context.getString(R.string.oral_gum_widening_gap),
            context.getString(R.string.oral_gum_swollen)
        ),
        required = false,
        hasAlertError = true
    )

    private val irregularTeethJaws = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = context.getString(R.string.oral_irregular_teeth_jaws),
        entries = arrayOf(optionYes, optionNo),
        required = false,
        hasAlertError = true
    )

    private val abnormalGrowthUlcer = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = context.getString(R.string.oral_abnormal_growth_ulcer),
        entries = arrayOf(optionYes, optionNo),
        required = false,
        hasAlertError = true
    )

    private val cleftLipPalate = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = context.getString(R.string.oral_cleft_lip_palate),
        entries = arrayOf(optionYes, optionNo),
        required = false,
        hasAlertError = true
    )

    private val dentalFluorosis = FormElement(
        id = 8,
        inputType = InputType.RADIO,
        title = context.getString(R.string.oral_dental_fluorosis),
        entries = arrayOf(optionYes, optionNo),
        required = false,
        hasAlertError = true
    )

    private val dentalEmergency = FormElement(
        id = 9,
        inputType = InputType.CHECKBOXES,
        title = context.getString(R.string.oral_dental_emergency),
        entries = arrayOf(
            context.getString(R.string.oral_emergency_pain),
            context.getString(R.string.oral_emergency_abscess),
            context.getString(R.string.oral_emergency_swelling),
            context.getString(R.string.oral_emergency_tooth_injury),
            context.getString(R.string.oral_emergency_avulsion),
            context.getString(R.string.oral_emergency_non_healing_ulcer),
            context.getString(R.string.oral_emergency_uncontrolled_bleeding),
            context.getString(R.string.oral_emergency_trauma)
        ),
        required = false,
        hasAlertError = true
    )

    suspend fun setUpPage(savedRecord: OralHealth?) {
        cache = savedRecord ?: createDefaultCache()
        populateFromCache(cache)
        lastSelectedToothDecaySymptoms = toSelectionSet(toothDecaySymptoms.value)
        lastSelectedGumDiseaseSymptoms = toSelectionSet(gumDiseaseSymptoms.value)
        lastSelectedDentalEmergencySelections = toSelectionSet(dentalEmergency.value)

        val list = mutableListOf<FormElement>()
        list.add(toothDecayPresent)
        if (toothDecayPresent.value == optionYes) {
            toothDecaySymptoms.required = true
            list.add(toothDecaySymptoms)
        } else {
            toothDecaySymptoms.required = false
        }

        list.add(gumDiseasePresent)
        if (gumDiseasePresent.value == optionYes) {
            gumDiseaseSymptoms.required = true
            list.add(gumDiseaseSymptoms)
        } else {
            gumDiseaseSymptoms.required = false
        }

        list.add(irregularTeethJaws)
        list.add(abnormalGrowthUlcer)
        list.add(cleftLipPalate)
        list.add(dentalFluorosis)
        list.add(dentalEmergency)

        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            toothDecayPresent.id -> handleToothDecayPresent(index)
            toothDecaySymptoms.id -> handleToothDecaySymptoms()
            gumDiseasePresent.id -> handleGumDiseasePresent(index)
            gumDiseaseSymptoms.id -> handleGumDiseaseSymptoms()
            irregularTeethJaws.id,
            abnormalGrowthUlcer.id,
            cleftLipPalate.id,
            dentalFluorosis.id -> handleSimpleAlert(index)
            dentalEmergency.id -> handleDentalEmergency()
            else -> -1
        }
    }

    private fun handleToothDecayPresent(index: Int): Int {
        return if (index == 0) {
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

    private fun handleToothDecaySymptoms(): Int {
        val currentSelections = toSelectionSet(toothDecaySymptoms.value)
        val isNewSelection = currentSelections.size > lastSelectedToothDecaySymptoms.size
        if (isNewSelection) {
            onShowAlert?.invoke(resources.getString(R.string.oral_health_referral_alert))
        }
        lastSelectedToothDecaySymptoms = currentSelections
        return -1
    }

    private fun handleGumDiseasePresent(index: Int): Int {
        return if (index == 0) {
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

    private fun handleGumDiseaseSymptoms(): Int {
        val currentSelections = toSelectionSet(gumDiseaseSymptoms.value)
        val isNewSelection = currentSelections.size > lastSelectedGumDiseaseSymptoms.size
        if (isNewSelection) {
            onShowAlert?.invoke(resources.getString(R.string.oral_health_referral_alert))
        }
        lastSelectedGumDiseaseSymptoms = currentSelections
        return -1
    }

    private fun handleSimpleAlert(index: Int): Int {
        if (index == 0) {
            onShowAlert?.invoke(resources.getString(R.string.oral_health_referral_alert))
        }
        return -1
    }

    private fun handleDentalEmergency(): Int {
        val currentSelections = toSelectionSet(dentalEmergency.value)
        if (currentSelections.size > lastSelectedDentalEmergencySelections.size) {
            onShowAlert?.invoke(resources.getString(R.string.oral_health_referral_alert))
        }
        lastSelectedDentalEmergencySelections = currentSelections
        return -1
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

    private fun String?.toYesNoBool(): Boolean? = when (this) {
        optionYes -> true
        optionNo  -> false
        else -> null
    }

    private fun populateFromCache(cache: OralHealth) {
        toothDecayPresent.value = when (cache.toothDecayPresent) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
        toothDecaySymptoms.value = if (cache.toothDecayPresent == true) {
            cache.toothDecaySymptoms
        } else {
            null
        }

        gumDiseasePresent.value = when (cache.gumDiseasePresent) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
        gumDiseaseSymptoms.value = if (cache.gumDiseasePresent == true) cache.gumDiseaseSymptoms else null

        irregularTeethJaws.value = when (cache.irregularTeethJaws) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
        abnormalGrowthUlcer.value = when (cache.abnormalGrowthUlcer) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
        cleftLipPalate.value = when (cache.cleftLipPalate) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
        dentalFluorosis.value = when (cache.dentalFluorosis) {
            true -> optionYes
            false -> optionNo
            else -> null
        }
        dentalEmergency.value = cache.dentalEmergency
    }


    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as OralHealth).let {
            it.toothDecayPresent  = toothDecayPresent.value.toYesNoBool()
            it.toothDecaySymptoms = if (it.toothDecayPresent == true) toothDecaySymptoms.value else null

            it.gumDiseasePresent  = gumDiseasePresent.value.toYesNoBool()
            it.gumDiseaseSymptoms = if (it.gumDiseasePresent == true) gumDiseaseSymptoms.value else null

            it.irregularTeethJaws  = irregularTeethJaws.value.toYesNoBool()
            it.abnormalGrowthUlcer = abnormalGrowthUlcer.value.toYesNoBool()
            it.cleftLipPalate      = cleftLipPalate.value.toYesNoBool()
            it.dentalFluorosis     = dentalFluorosis.value.toYesNoBool()
            it.dentalEmergency     = dentalEmergency.value
        }
    }
}
