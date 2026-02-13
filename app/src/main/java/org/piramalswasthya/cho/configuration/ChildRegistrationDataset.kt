package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.InfantRegCache
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.DeliveryOutcomeCache
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.network.getDateFromLong
import org.piramalswasthya.cho.network.getLongFromDate

class ChildRegistrationDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    companion object {
        private const val WEIGHT_MIN = 500
        private const val WEIGHT_MAX = 6000
    }

    // ─── Existing FLW fields ────────────────────────────────────────────

    private var babyName = FormElement(
        id = 1,
        inputType = InputType.TEXT_VIEW,
        title = "Name of Baby",
        required = false,
        hasDependants = false
    )

    private var infantTerm = FormElement(
        id = 2,
        inputType = InputType.TEXT_VIEW,
        title = "Infant Term",
        entries = arrayOf("Full Term", "Pre Term"),
        required = false,
        hasDependants = false
    )

    private var corticosteroidGiven = FormElement(
        id = 3,
        inputType = InputType.RADIO,
        title = "Was Corticosteroid Inj. given?",
        entries = arrayOf("Yes", "No", "Don't Know"),
        required = false,
        hasDependants = false
    )

    // Q2: Outcome at Birth (BRD)
    private val outcomeAtBirth = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.no_outcome_at_birth),
        arrayId = R.array.no_outcome_at_birth_array,
        entries = resources.getStringArray(R.array.no_outcome_at_birth_array),
        required = true,
        hasDependants = true
    )

    // Q3: Sex
    private var gender = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.no_sex),
        arrayId = R.array.no_sex_array,
        entries = resources.getStringArray(R.array.no_sex_array),
        required = true,
        hasDependants = false,
    )

    // Q4: Cried immediately after birth?
    private var babyCriedAtBirth = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.no_cried_immediately),
        arrayId = R.array.no_cried_immediately_array,
        entries = resources.getStringArray(R.array.no_cried_immediately_array),
        required = true,
        hasDependants = true
    )

    // Q5: Type of resuscitation (multi-select, shown if cried after resuscitation)
    private val typeOfResuscitation = FormElement(
        id = 7,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.no_type_of_resuscitation),
        arrayId = R.array.no_type_of_resuscitation_array,
        entries = resources.getStringArray(R.array.no_type_of_resuscitation_array),
        required = false,
        hasDependants = false
    )

    private var resuscitation = FormElement(
        id = 8,
        inputType = InputType.RADIO,
        title = "If No, Resuscitation Done",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = false
    )

    private var referred = FormElement(
        id = 9,
        inputType = InputType.RADIO,
        title = "Referred to higher facility for further management",
        entries = arrayOf("Yes", "No", "NA"),
        required = false,
        hasDependants = false
    )

    // Q6: Birth Weight (grams)
    private var weight = FormElement(
        id = 10,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.no_birth_weight),
        required = true,
        hasDependants = false,
        etMaxLength = 4,
        min = WEIGHT_MIN.toLong(),
        max = WEIGHT_MAX.toLong(),
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
    )

    // Q7: Any congenital anomaly detected?
    private var hadBirthDefect = FormElement(
        id = 11,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.no_congenital_anomaly_detected),
        arrayId = R.array.no_congenital_anomaly_array,
        entries = resources.getStringArray(R.array.no_congenital_anomaly_array),
        required = true,
        hasDependants = true
    )

    // Q8: Type of congenital anomaly (multi-select)
    private var birthDefect = FormElement(
        id = 12,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.no_type_of_congenital_anomaly),
        arrayId = R.array.no_type_of_congenital_anomaly_array,
        entries = resources.getStringArray(R.array.no_type_of_congenital_anomaly_array),
        required = false,
        hasDependants = true
    )

    // Q9: Other congenital anomaly
    private var otherDefect = FormElement(
        id = 13,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.no_other_congenital_anomaly),
        required = false,
        hasDependants = false,
    )

    // Q10: Newborn Complications (multi-select)
    private val newbornComplications = FormElement(
        id = 14,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.no_newborn_complications),
        arrayId = R.array.no_newborn_complications_array,
        entries = resources.getStringArray(R.array.no_newborn_complications_array),
        required = false,
        hasDependants = false
    )

    // Q11: Current Status of Baby
    private val currentStatusOfBaby = FormElement(
        id = 15,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.no_current_status_of_baby),
        arrayId = R.array.no_current_status_array,
        entries = resources.getStringArray(R.array.no_current_status_array),
        required = true,
        hasDependants = true
    )

    // Q12: Cause of Death (multi-select, shown if Died)
    private val causeOfDeath = FormElement(
        id = 16,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.no_cause_of_death),
        arrayId = R.array.no_cause_of_death_array,
        entries = resources.getStringArray(R.array.no_cause_of_death_array),
        required = false,
        hasDependants = true
    )

    // Q13: Other cause of death
    private val otherCauseOfDeath = FormElement(
        id = 17,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.no_other_cause_of_death),
        required = false,
        hasDependants = false
    )

    private var breastFeedingStarted = FormElement(
        id = 18,
        inputType = InputType.RADIO,
        title = "Breast feeding started within 1 hour of birth",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = false,
    )

    // Q14: Birth dose vaccines given (multi-select)
    private val birthDoseVaccinesGiven = FormElement(
        id = 19,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.no_birth_dose_vaccines_given),
        arrayId = R.array.no_birth_dose_vaccines_array,
        entries = resources.getStringArray(R.array.no_birth_dose_vaccines_array),
        required = false,
        hasDependants = true
    )

    // Q15: Reason for not giving birth dose vaccines
    private val reasonForNoVaccines = FormElement(
        id = 20,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.no_reason_for_no_vaccines),
        required = false,
        hasDependants = false
    )

    private val opv0Dose = FormElement(
        id = 21,
        inputType = InputType.DATE_PICKER,
        title = "OPV0 Dose",
        required = false,
        max = System.currentTimeMillis(),
        hasDependants = false
    )

    private val bcgDose = FormElement(
        id = 22,
        inputType = InputType.DATE_PICKER,
        title = "BCG Dose",
        required = false,
        max = System.currentTimeMillis(),
        hasDependants = false
    )

    private val hepBDose = FormElement(
        id = 23,
        inputType = InputType.DATE_PICKER,
        title = "HEP B-0 Dose",
        required = false,
        max = System.currentTimeMillis(),
        hasDependants = false
    )

    // Q16: Vitamin K injection given?
    private val vitaminKInjectionGiven = FormElement(
        id = 24,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.no_vitamin_k_injection_given),
        entries = arrayOf(resources.getString(R.string.yes), resources.getString(R.string.no)),
        required = true,
        hasDependants = true
    )

    // Q17: Reason for no Vitamin K
    private val reasonForNoVitaminK = FormElement(
        id = 25,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.no_reason_for_no_vitamin_k),
        required = false,
        hasDependants = false
    )

    private val vitkDose = FormElement(
        id = 26,
        inputType = InputType.DATE_PICKER,
        title = "VITK Dose",
        required = false,
        max = System.currentTimeMillis(),
        hasDependants = false
    )

    // Q18: Birth Certificate issued?
    private val birthCertificateIssued = FormElement(
        id = 27,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.no_birth_certificate_issued),
        arrayId = R.array.no_birth_certificate_array,
        entries = resources.getStringArray(R.array.no_birth_certificate_array),
        required = true,
        hasDependants = false
    )

    // ─── Helper: generate baby name from patient & delivery data ────────
    private fun generateBabyName(
        ben: Patient?,
        deliveryOutcomeCache: DeliveryOutcomeCache,
        babyIndex: Int
    ): String? {
        if (ben == null) return null
        return if (deliveryOutcomeCache.liveBirth == null || deliveryOutcomeCache.liveBirth == 1)
            "baby of ${ben.firstName}"
        else
            "baby ${babyIndex + 1} of ${ben.firstName}"
    }

    // ─── Helper: restore all saved field values from cache ─────────────
    private fun restoreSavedValues(saved: InfantRegCache) {
        infantTerm.value = saved.infantTerm
        corticosteroidGiven.value = saved.corticosteroidGiven
        outcomeAtBirth.value = saved.outcomeAtBirth

        gender.value = saved.genderID?.let {
             if (it > 0 && it <= (gender.entries?.size ?: 0)) gender.entries?.get(it - 1) else null
        }
        babyCriedAtBirth.value = when {
            saved.babyCriedAtBirth == true -> resources.getStringArray(R.array.no_cried_immediately_array).getOrNull(0) // Immediate cry
            saved.resuscitation == true -> resources.getStringArray(R.array.no_cried_immediately_array).getOrNull(1) // Cried after resuscitation
            else -> resources.getStringArray(R.array.no_cried_immediately_array).getOrNull(0)
        }
        typeOfResuscitation.value = saved.typeOfResuscitation
        resuscitation.value = if (saved.resuscitation == true) "Yes" else "No"
        referred.value = saved.referred
        hadBirthDefect.value = saved.hadBirthDefect
        birthDefect.value = saved.birthDefect
        otherDefect.value = saved.otherDefect
        weight.value = saved.weight?.let { (it * 1000).toInt().toString() } // Convert kg to grams for display
        breastFeedingStarted.value = if (saved.breastFeedingStarted == true) "Yes" else "No"
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

        opv0Dose.value = saved.opv0Dose?.let { getDateFromLong(it) }
        bcgDose.value = saved.bcgDose?.let { getDateFromLong(it) }
        hepBDose.value = saved.hepBDose?.let { getDateFromLong(it) }
        vitkDose.value = saved.vitkDose?.let { getDateFromLong(it) }
    }

    // ─── Helper: insert a field (and optional sub-field) after anchor ──
    private fun insertAfter(
        list: MutableList<FormElement>,
        anchor: FormElement,
        primary: FormElement,
        secondary: FormElement? = null
    ) {
        val idx = list.indexOf(anchor)
        if (idx < 0) return
        list.add(idx + 1, primary)
        if (secondary != null) list.add(idx + 2, secondary)
    }

    // ─── Helper: restore conditional (dependant) fields into the list ──
    private fun restoreConditionalFields(
        saved: InfantRegCache,
        list: MutableList<FormElement>
    ) {
        if (saved.babyCriedAtBirth == false || saved.resuscitation == true) {
            val criedIdx = list.indexOf(babyCriedAtBirth)
            if (criedIdx >= 0 && saved.typeOfResuscitation != null) {
                list.add(criedIdx + 1, typeOfResuscitation)
            }
        }
        if (saved.hadBirthDefect == resources.getStringArray(R.array.no_congenital_anomaly_array).getOrNull(0)) {
            val other = if (saved.birthDefect?.contains("Other") == true) otherDefect else null
            insertAfter(list, hadBirthDefect, birthDefect, other)
        }
        if (saved.currentStatusOfBaby == resources.getStringArray(R.array.no_current_status_array).getOrNull(3)) {
            val other = if (saved.causeOfDeath?.contains("Other") == true) otherCauseOfDeath else null
            insertAfter(list, currentStatusOfBaby, causeOfDeath, other)
        }
        if (saved.birthDoseVaccinesGiven?.contains("None") == true) {
            insertAfter(list, birthDoseVaccinesGiven, reasonForNoVaccines)
        }
        if (saved.vitaminKInjectionGiven == false) {
            insertAfter(list, vitaminKInjectionGiven, reasonForNoVitaminK)
        }
    }

    // ─── Helper: infer infant term from gestational age ────────────────
    private fun inferInfantTerm(deliveryOutcomeCache: DeliveryOutcomeCache) {
        if (!infantTerm.value.isNullOrBlank()) return
        deliveryOutcomeCache.gestationalAgeAtDelivery?.let { gaString ->
            val weeks = gaString.substringBefore("w").trim().toIntOrNull()
            if (weeks != null) {
                infantTerm.value = if (weeks < 37) "Pre Term" else "Full Term"
            }
        }
    }

    // ─── Helper: pre-populate defaults from delivery outcome ───────────
    private fun prepopulateDefaults(deliveryOutcomeCache: DeliveryOutcomeCache) {
        inferInfantTerm(deliveryOutcomeCache)

        val deliveryDate = deliveryOutcomeCache.dateOfDelivery?.let { getDateFromLong(it) }
        if (deliveryDate != null) {
            if (opv0Dose.value == null) opv0Dose.value = deliveryDate
            if (bcgDose.value == null) bcgDose.value = deliveryDate
            if (hepBDose.value == null) hepBDose.value = deliveryDate
            if (vitkDose.value == null) vitkDose.value = deliveryDate
        }
    }

    suspend fun setUpPage(
        ben: Patient?,
        deliveryOutcomeCache: DeliveryOutcomeCache,
        babyIndex: Int,
        saved: InfantRegCache?
    ) {
        val list = mutableListOf(
            babyName, infantTerm, corticosteroidGiven,
            outcomeAtBirth, gender, babyCriedAtBirth,
            weight, hadBirthDefect,
            newbornComplications, currentStatusOfBaby,
            breastFeedingStarted,
            birthDoseVaccinesGiven,
            opv0Dose, bcgDose, hepBDose,
            vitaminKInjectionGiven, vitkDose,
            birthCertificateIssued
        )

        deliveryOutcomeCache.dateOfDelivery?.let {
            opv0Dose.min = it
            bcgDose.min = it
            hepBDose.min = it
            vitkDose.min = it
        }

        babyName.value = saved?.babyName ?: generateBabyName(ben, deliveryOutcomeCache, babyIndex)

        if (saved != null) {
            restoreSavedValues(saved)
            restoreConditionalFields(saved, list)
        }

        prepopulateDefaults(deliveryOutcomeCache)
        setUpPage(list)
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
            babyCriedAtBirth.id -> {
                val selected = babyCriedAtBirth.entries?.getOrNull(index)
                babyCriedAtBirth.value = selected
                // "Cried after resuscitation" is index 1 — show typeOfResuscitation
                toggleDependant(
                    source = babyCriedAtBirth,
                    condition = selected == resources.getStringArray(R.array.no_cried_immediately_array).getOrNull(1),
                    showItems = listOf(typeOfResuscitation)
                )
            }

            hadBirthDefect.id -> {
                val selected = hadBirthDefect.entries?.getOrNull(index)
                hadBirthDefect.value = selected
                // "Yes" or "Suspected" (index 0 or 2) → show birthDefect
                val anomalyArray = resources.getStringArray(R.array.no_congenital_anomaly_array)
                toggleDependant(
                    source = hadBirthDefect,
                    condition = selected == anomalyArray.getOrNull(0) || selected == anomalyArray.getOrNull(2),
                    showItems = listOf(birthDefect),
                    hideItems = listOf(otherDefect)
                )
            }

            birthDefect.id -> {
                toggleDependant(
                    source = birthDefect,
                    condition = birthDefect.value?.contains("Other") == true,
                    showItems = listOf(otherDefect)
                )
            }

            currentStatusOfBaby.id -> {
                val selected = currentStatusOfBaby.entries?.getOrNull(index)
                currentStatusOfBaby.value = selected
                // "Died" (index 3) → show causeOfDeath
                toggleDependant(
                    source = currentStatusOfBaby,
                    condition = selected == resources.getStringArray(R.array.no_current_status_array).getOrNull(3),
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
                val selected = vitaminKInjectionGiven.entries?.getOrNull(index)
                vitaminKInjectionGiven.value = selected
                toggleDependant(
                    source = vitaminKInjectionGiven,
                    condition = selected == resources.getString(R.string.no),
                    showItems = listOf(reasonForNoVitaminK)
                )
            }

            weight.id -> {
                validateIntMinMax(weight)
            }

            otherDefect.id -> {
                validateAllAlphabetsSpecialOnEditText(otherDefect)
            }

            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as InfantRegCache).let { form ->
            form.babyName = babyName.value
            form.infantTerm = infantTerm.value
            form.corticosteroidGiven = corticosteroidGiven.value
            form.outcomeAtBirth = outcomeAtBirth.value

            form.genderID = gender.value?.let { value ->
                 gender.entries?.indexOf(value)?.plus(1)
            }

            // Map cried immediately to existing boolean field
            val criedValue = babyCriedAtBirth.value
            form.babyCriedAtBirth = criedValue == resources.getStringArray(R.array.no_cried_immediately_array).getOrNull(0) // Immediate cry
            form.resuscitation = criedValue == resources.getStringArray(R.array.no_cried_immediately_array).getOrNull(1) // Cried after resuscitation
            form.typeOfResuscitation = typeOfResuscitation.value

            form.referred = referred.value
            form.hadBirthDefect = hadBirthDefect.value
            form.birthDefect = birthDefect.value
            form.otherDefect = otherDefect.value
            // Convert grams to kg for DB storage (existing field is in kg)
            form.weight = weight.value?.toDoubleOrNull()?.let { it / 1000.0 }
            form.breastFeedingStarted = breastFeedingStarted.value == "Yes"

            form.newbornComplications = newbornComplications.value
            form.currentStatusOfBaby = currentStatusOfBaby.value
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

            form.opv0Dose = getLongFromDate(opv0Dose.value)
            form.bcgDose = getLongFromDate(bcgDose.value)
            form.hepBDose = getLongFromDate(hepBDose.value)
            form.vitkDose = getLongFromDate(vitkDose.value)
        }
    }
}
