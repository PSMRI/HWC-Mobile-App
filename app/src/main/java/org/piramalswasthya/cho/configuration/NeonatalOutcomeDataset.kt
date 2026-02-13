package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.configuration.FormDataModel
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.NeonatalOutcomeCache

/**
 * Dataset for Neonatal Outcome form per MHWC-200 requirements
 * Handles all fields for newborn registration including complications,
 * congenital anomalies, birth doses, and current status.
 */
class NeonatalOutcomeDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    companion object {
        private const val WEIGHT_MIN = 500
        private const val WEIGHT_MAX = 6000
    }

    // Q2: Outcome at Birth
    private val outcomeAtBirth = FormElement(
        id = 1,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.no_outcome_at_birth),
        arrayId = R.array.no_outcome_at_birth_array,
        entries = resources.getStringArray(R.array.no_outcome_at_birth_array),
        required = true,
        hasDependants = true
    )

    // Q3: Sex
    private val sex = FormElement(
        id = 2,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.no_sex),
        arrayId = R.array.no_sex_array,
        entries = resources.getStringArray(R.array.no_sex_array),
        required = true,
        hasDependants = true
    )

    // Q4: Cried immediately after birth?
    private val criedImmediately = FormElement(
        id = 3,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.no_cried_immediately),
        arrayId = R.array.no_cried_immediately_array,
        entries = resources.getStringArray(R.array.no_cried_immediately_array),
        required = true,
        hasDependants = true
    )

    // Q5: Type of resuscitation
    private val typeOfResuscitation = FormElement(
        id = 4,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.no_type_of_resuscitation),
        arrayId = R.array.no_type_of_resuscitation_array,
        entries = resources.getStringArray(R.array.no_type_of_resuscitation_array),
        required = false,
        hasDependants = false
    )

    // Q6: Birth Weight
    private val birthWeight = FormElement(
        id = 5,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.no_birth_weight),
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        etMaxLength = 4,
        min = WEIGHT_MIN.toLong(),
        max = WEIGHT_MAX.toLong(),
        hasDependants = false
    )

    // Q7: Any congenital anomaly detected?
    private val congenitalAnomalyDetected = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.no_congenital_anomaly_detected),
        arrayId = R.array.no_congenital_anomaly_array,
        entries = resources.getStringArray(R.array.no_congenital_anomaly_array),
        required = true,
        hasDependants = true
    )

    // Q8: Type of congenital anomaly
    private val typeOfCongenitalAnomaly = FormElement(
        id = 7,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.no_type_of_congenital_anomaly),
        arrayId = R.array.no_type_of_congenital_anomaly_array,
        entries = resources.getStringArray(R.array.no_type_of_congenital_anomaly_array),
        required = false,
        hasDependants = true
    )

    // Q9: Other congenital anomaly
    private val otherCongenitalAnomaly = FormElement(
        id = 8,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.no_other_congenital_anomaly),
        required = false,
        hasDependants = false
    )

    // Q10: Newborn Complications
    private val newbornComplications = FormElement(
        id = 9,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.no_newborn_complications),
        arrayId = R.array.no_newborn_complications_array,
        entries = resources.getStringArray(R.array.no_newborn_complications_array),
        required = false,
        hasDependants = true
    )

    // Q11: Current Status of Baby
    private val currentStatusOfBaby = FormElement(
        id = 10,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.no_current_status_of_baby),
        arrayId = R.array.no_current_status_array,
        entries = resources.getStringArray(R.array.no_current_status_array),
        required = true,
        hasDependants = true
    )

    // Q12: Cause of Death
    private val causeOfDeath = FormElement(
        id = 11,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.no_cause_of_death),
        arrayId = R.array.no_cause_of_death_array,
        entries = resources.getStringArray(R.array.no_cause_of_death_array),
        required = false,
        hasDependants = true
    )

    // Q13: Other cause of death
    private val otherCauseOfDeath = FormElement(
        id = 12,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.no_other_cause_of_death),
        required = false,
        hasDependants = false
    )

    // Q14: Birth dose vaccines given
    private val birthDoseVaccinesGiven = FormElement(
        id = 13,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.no_birth_dose_vaccines_given),
        arrayId = R.array.no_birth_dose_vaccines_array,
        entries = resources.getStringArray(R.array.no_birth_dose_vaccines_array),
        required = false,
        hasDependants = true
    )

    // Q15: Reason for no vaccines
    private val reasonForNoVaccines = FormElement(
        id = 14,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.no_reason_for_no_vaccines),
        required = false,
        hasDependants = false
    )

    // Q16: Vitamin K injection given?
    private val vitaminKInjectionGiven = FormElement(
        id = 15,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.no_vitamin_k_injection_given),
        entries = arrayOf(resources.getString(R.string.yes), resources.getString(R.string.no)),
        required = true,
        hasDependants = true
    )

    // Q17: Reason for no Vitamin K
    private val reasonForNoVitaminK = FormElement(
        id = 16,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.no_reason_for_no_vitamin_k),
        required = false,
        hasDependants = false
    )

    // Q18: Birth Certificate issued?
    private val birthCertificateIssued = FormElement(
        id = 17,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.no_birth_certificate_issued),
        arrayId = R.array.no_birth_certificate_array,
        entries = resources.getStringArray(R.array.no_birth_certificate_array),
        required = true,
        hasDependants = false
    )

    // ─── Helper: restore conditional (dependant) fields into the list ──
    private fun restoreConditionalFields(
        saved: NeonatalOutcomeCache,
        formElements: MutableList<FormElement>
    ) {
        // Restore typeOfResuscitation if "Cried after resuscitation" was selected
        val criedAfterResuscitation = resources.getStringArray(R.array.no_cried_immediately_array).getOrNull(1)
        if (saved.criedImmediately == criedAfterResuscitation && saved.typeOfResuscitation != null) {
            val criedIdx = formElements.indexOf(criedImmediately)
            if (criedIdx >= 0) {
                formElements.add(criedIdx + 1, typeOfResuscitation)
            }
        }
        if (saved.congenitalAnomalyDetected == resources.getStringArray(R.array.no_congenital_anomaly_array)[0]) { // Yes
            formElements.add(formElements.indexOf(congenitalAnomalyDetected) + 1, typeOfCongenitalAnomaly)
            if (saved.typeOfCongenitalAnomaly?.contains("Other") == true) {
                formElements.add(formElements.indexOf(typeOfCongenitalAnomaly) + 1, otherCongenitalAnomaly)
            }
        }
        if (saved.currentStatusOfBaby == resources.getStringArray(R.array.no_current_status_array)[3]) { // Died
            formElements.add(formElements.indexOf(currentStatusOfBaby) + 1, causeOfDeath)
            if (saved.causeOfDeath?.contains("Other") == true) {
                formElements.add(formElements.indexOf(causeOfDeath) + 1, otherCauseOfDeath)
            }
        }
        if (saved.birthDoseVaccinesGiven?.contains("None") == true) {
            formElements.add(formElements.indexOf(birthDoseVaccinesGiven) + 1, reasonForNoVaccines)
        }
        if (saved.vitaminKInjectionGiven == false) {
            formElements.add(formElements.indexOf(vitaminKInjectionGiven) + 1, reasonForNoVitaminK)
        }
    }

    suspend fun setUpPage(saved: NeonatalOutcomeCache?) {
        val formElements = mutableListOf(
            outcomeAtBirth,
            sex,
            criedImmediately,
            birthWeight,
            congenitalAnomalyDetected,
            newbornComplications,
            currentStatusOfBaby,
            birthDoseVaccinesGiven,
            vitaminKInjectionGiven,
            birthCertificateIssued
        )

        saved?.let {
            outcomeAtBirth.value = saved.outcomeAtBirth
            sex.value = saved.sex
            criedImmediately.value = saved.criedImmediately
            birthWeight.value = saved.birthWeight?.toString()
            congenitalAnomalyDetected.value = saved.congenitalAnomalyDetected
            typeOfCongenitalAnomaly.value = saved.typeOfCongenitalAnomaly
            otherCongenitalAnomaly.value = saved.otherCongenitalAnomaly
            newbornComplications.value = saved.newbornComplications
            currentStatusOfBaby.value = saved.currentStatusOfBaby
            causeOfDeath.value = saved.causeOfDeath
            otherCauseOfDeath.value = saved.otherCauseOfDeath
            birthDoseVaccinesGiven.value = saved.birthDoseVaccinesGiven
            reasonForNoVaccines.value = saved.reasonForNoVaccines
            vitaminKInjectionGiven.value = when (saved.vitaminKInjectionGiven) {
                true -> resources.getString(R.string.yes)
                false -> resources.getString(R.string.no)
                else -> null
            }
            reasonForNoVitaminK.value = saved.reasonForNoVitaminK
            birthCertificateIssued.value = saved.birthCertificateIssued

            restoreConditionalFields(saved, formElements)
        }

        setUpPage(formElements)
    }

    // ─── Helper: toggle dependant fields based on a condition ──────────
    private fun toggleDependant(
        source: FormElement,
        condition: Boolean,
        showItems: List<FormElement>,
        hideItems: List<FormElement> = emptyList()
    ): Int {
        return if (condition) {
            triggerDependants(source = source, removeItems = hideItems, addItems = showItems)
        } else {
            triggerDependants(source = source, removeItems = showItems + hideItems, addItems = emptyList())
        }
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            criedImmediately.id -> {
                val selectedValue = criedImmediately.entries?.getOrNull(index)
                criedImmediately.value = selectedValue
                // "Cried after resuscitation" is index 1 — show typeOfResuscitation
                toggleDependant(
                    source = criedImmediately,
                    condition = selectedValue == resources.getStringArray(R.array.no_cried_immediately_array).getOrNull(1),
                    showItems = listOf(typeOfResuscitation)
                )
            }
            congenitalAnomalyDetected.id -> {
                val selectedValue = congenitalAnomalyDetected.entries?.getOrNull(index)
                congenitalAnomalyDetected.value = selectedValue
                toggleDependant(
                    source = congenitalAnomalyDetected,
                    condition = selectedValue == resources.getStringArray(R.array.no_congenital_anomaly_array)[0], // Yes
                    showItems = listOf(typeOfCongenitalAnomaly),
                    hideItems = listOf(otherCongenitalAnomaly)
                )
            }
            typeOfCongenitalAnomaly.id -> {
                // handle multi-select and "Other"
                toggleDependant(
                    source = typeOfCongenitalAnomaly,
                    condition = typeOfCongenitalAnomaly.value?.contains("Other") == true,
                    showItems = listOf(otherCongenitalAnomaly)
                )
            }
            currentStatusOfBaby.id -> {
                val selectedValue = currentStatusOfBaby.entries?.getOrNull(index)
                currentStatusOfBaby.value = selectedValue
                toggleDependant(
                    source = currentStatusOfBaby,
                    condition = selectedValue == resources.getStringArray(R.array.no_current_status_array)[3], // Died
                    showItems = listOf(causeOfDeath),
                    hideItems = listOf(otherCauseOfDeath)
                )
            }
            causeOfDeath.id -> {
                toggleDependant(
                    source = causeOfDeath,
                    condition = causeOfDeath.value?.contains("Other") == true,
                    showItems = listOf(otherCauseOfDeath)
                )
            }
            birthDoseVaccinesGiven.id -> {
                toggleDependant(
                    source = birthDoseVaccinesGiven,
                    condition = birthDoseVaccinesGiven.value?.contains("None") == true,
                    showItems = listOf(reasonForNoVaccines)
                )
            }
            vitaminKInjectionGiven.id -> {
                val selectedValue = vitaminKInjectionGiven.entries?.getOrNull(index)
                vitaminKInjectionGiven.value = selectedValue
                toggleDependant(
                    source = vitaminKInjectionGiven,
                    condition = selectedValue == resources.getString(R.string.no),
                    showItems = listOf(reasonForNoVitaminK)
                )
            }
            birthWeight.id -> {
                validateIntMinMax(birthWeight)
            }
            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as NeonatalOutcomeCache).let { form ->
            form.outcomeAtBirth = outcomeAtBirth.value
            form.outcomeAtBirthId = outcomeAtBirth.getPosition()
            form.sex = sex.value
            form.sexId = sex.getPosition()
            form.criedImmediately = criedImmediately.value
            form.criedImmediatelyId = criedImmediately.getPosition()
            form.typeOfResuscitation = typeOfResuscitation.value
            form.birthWeight = birthWeight.value?.toIntOrNull()
            form.congenitalAnomalyDetected = congenitalAnomalyDetected.value
            form.congenitalAnomalyDetectedId = congenitalAnomalyDetected.getPosition()
            form.typeOfCongenitalAnomaly = typeOfCongenitalAnomaly.value
            form.otherCongenitalAnomaly = otherCongenitalAnomaly.value
            form.newbornComplications = newbornComplications.value
            form.currentStatusOfBaby = currentStatusOfBaby.value
            form.currentStatusOfBabyId = currentStatusOfBaby.getPosition()
            form.causeOfDeath = causeOfDeath.value
            form.otherCauseOfDeath = otherCauseOfDeath.value
            form.birthDoseVaccinesGiven = birthDoseVaccinesGiven.value
            form.reasonForNoVaccines = reasonForNoVaccines.value
            form.vitaminKInjectionGiven = when (vitaminKInjectionGiven.value) {
                resources.getString(R.string.yes) -> true
                resources.getString(R.string.no) -> false
                else -> null
            }
            form.reasonForNoVitaminK = reasonForNoVitaminK.value
            form.birthCertificateIssued = birthCertificateIssued.value
            form.birthCertificateIssuedId = birthCertificateIssued.getPosition()
            
            // Set audit flags
            form.isStillbirth = outcomeAtBirth.value?.contains("Still Birth") == true
            form.isNeonatalDeath = currentStatusOfBaby.value == resources.getStringArray(R.array.no_current_status_array)[3]
        }
    }
}
