package org.piramalswasthya.cho.configuration

import android.content.Context
import android.widget.LinearLayout
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
        private const val OUTCOME_LIVE_BIRTH_INDEX = 0
        private const val CURRENT_STATUS_DIED_INDEX = 3
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
        hasDependants = true,
        orientation = LinearLayout.VERTICAL
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
        hasAlertError = true,
        orientation = LinearLayout.VERTICAL,
    )

    // Q4: Cried immediately after birth?
    private var babyCriedAtBirth = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.no_cried_immediately),
        arrayId = R.array.no_cried_immediately_array,
        entries = resources.getStringArray(R.array.no_cried_immediately_array),
        required = true,
        hasDependants = true,
        orientation = LinearLayout.VERTICAL
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
        hasDependants = true,
        orientation = LinearLayout.VERTICAL
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
        etMaxLength = 300
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
        hasDependants = true,
        orientation = LinearLayout.VERTICAL
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
        hasDependants = false,
        etMaxLength = 300
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
        required = true,
        hasDependants = true
    )

    // Q15: Reason for not giving birth dose vaccines
    private val reasonForNoVaccines = FormElement(
        id = 20,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.no_reason_for_no_vaccines),
        required = false,
        hasDependants = false,
        etMaxLength = 200
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
        hasDependants = false,
        etMaxLength = 200
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
        hasDependants = false,
        hasAlertError = true,
        orientation = LinearLayout.VERTICAL
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
        outcomeAtBirth.value = when (saved.outcomeAtBirth) {
            "Diedrelative during delivery" -> resources.getStringArray(R.array.no_outcome_at_birth_array).getOrNull(3)
            else -> getLocalValueInArray(R.array.no_outcome_at_birth_array, saved.outcomeAtBirth)
        }

        gender.value = saved.genderID?.let {
             if (it > 0 && it <= (gender.entries?.size ?: 0)) gender.entries?.get(it - 1) else null
        }
        babyCriedAtBirth.value = when {
            saved.babyCriedAtBirth == true -> resources.getStringArray(R.array.no_cried_immediately_array).getOrNull(0) // Immediate cry
            saved.resuscitation == true -> resources.getStringArray(R.array.no_cried_immediately_array).getOrNull(1) // Cried after resuscitation
            isStillbirthOrDiedAtBirth(outcomeAtBirth.value) -> resources.getStringArray(R.array.no_cried_immediately_array).getOrNull(2) // Not applicable (Stillbirth)
            else -> null
        }
        typeOfResuscitation.value = getLocalValuesInArray(R.array.no_type_of_resuscitation_array, saved.typeOfResuscitation)
        resuscitation.value = saved.resuscitation?.let { if (it) "Yes" else "No" }
        referred.value = saved.referred
        hadBirthDefect.value = getLocalValueInArray(R.array.no_congenital_anomaly_array, saved.hadBirthDefect)
        birthDefect.value = getLocalValuesInArray(R.array.no_type_of_congenital_anomaly_array, saved.birthDefect)
        otherDefect.value = saved.otherDefect?.takeIf { it.isNotBlank() }
        weight.value = saved.weight?.let { (it * 1000).toInt().toString() } // Convert kg to grams for display
        breastFeedingStarted.value = saved.breastFeedingStarted?.let { if (it) "Yes" else "No" }
        newbornComplications.value = getLocalValuesInArray(R.array.no_newborn_complications_array, saved.newbornComplications)
        currentStatusOfBaby.value = getLocalValueInArray(R.array.no_current_status_array, saved.currentStatusOfBaby)
        causeOfDeath.value = getLocalValuesInArray(R.array.no_cause_of_death_array, saved.causeOfDeath)
        otherCauseOfDeath.value = saved.otherCauseOfDeath?.takeIf { it.isNotBlank() }
        birthDoseVaccinesGiven.value = getLocalValuesInArray(R.array.no_birth_dose_vaccines_array, saved.birthDoseVaccinesGiven)
        reasonForNoVaccines.value = saved.reasonForNoVaccines?.takeIf { it.isNotBlank() }
        vitaminKInjectionGiven.value = when (saved.vitaminKInjectionGiven) {
            true -> resources.getString(R.string.yes)
            false -> resources.getString(R.string.no)
            else -> null
        }
        reasonForNoVitaminK.value = saved.reasonForNoVitaminK?.takeIf { it.isNotBlank() }
        birthCertificateIssued.value = getLocalValueInArray(R.array.no_birth_certificate_array, saved.birthCertificateIssued)

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
        if (!list.contains(primary)) {
            list.add(idx + 1, primary)
        }
        if (secondary != null && !list.contains(secondary)) {
            val primaryIndex = list.indexOf(primary).takeIf { it >= 0 } ?: idx
            list.add(primaryIndex + 1, secondary)
        }
    }

    private fun getLocalValuesInArray(arrayId: Int, entry: String?): String? {
        if (entry.isNullOrBlank()) return null
        return entry.split(",")
            .mapNotNull { raw ->
                getLocalValueInArray(arrayId, raw.trim())?.takeIf { it.isNotBlank() }
            }
            .distinct()
            .joinToString(",")
            .takeIf { it.isNotBlank() }
    }

    private fun getEnglishValuesInArray(arrayId: Int, entry: String?): String? {
        if (entry.isNullOrBlank()) return null
        return entry.split(",")
            .mapNotNull { raw ->
                getEnglishValueInArray(arrayId, raw.trim())?.takeIf { it.isNotBlank() }
            }
            .distinct()
            .joinToString(",")
            .takeIf { it.isNotBlank() }
    }

    // ─── Helper: restore conditional (dependant) fields into the list ──
    private fun restoreConditionalFields(
        saved: InfantRegCache,
        list: MutableList<FormElement>
    ) {
        val localizedHadBirthDefect = getLocalValueInArray(R.array.no_congenital_anomaly_array, saved.hadBirthDefect)
        val localizedCurrentStatus = getLocalValueInArray(R.array.no_current_status_array, saved.currentStatusOfBaby)
        val localizedCauseOfDeath = getLocalValuesInArray(R.array.no_cause_of_death_array, saved.causeOfDeath)
        val localizedBirthDoseVaccines = getLocalValuesInArray(R.array.no_birth_dose_vaccines_array, saved.birthDoseVaccinesGiven)

        if (saved.babyCriedAtBirth == false || saved.resuscitation == true) {
            val criedIdx = list.indexOf(babyCriedAtBirth)
            if (criedIdx >= 0 && !saved.typeOfResuscitation.isNullOrBlank() && !list.contains(typeOfResuscitation)) {
                list.add(criedIdx + 1, typeOfResuscitation)
            }
        }
        val anomalyArray = resources.getStringArray(R.array.no_congenital_anomaly_array)
        if (localizedHadBirthDefect == anomalyArray.getOrNull(0) || localizedHadBirthDefect == anomalyArray.getOrNull(2)) {
            val other = if (hasSelectedOption(birthDefect.value, getOtherOption(birthDefect.entries))) otherDefect else null
            insertAfter(list, hadBirthDefect, birthDefect, other)
        }
        if (localizedCurrentStatus == resources.getStringArray(R.array.no_current_status_array).getOrNull(3)) {
            val other = if (hasSelectedOption(localizedCauseOfDeath, getOtherOption(causeOfDeath.entries))) otherCauseOfDeath else null
            insertAfter(list, currentStatusOfBaby, causeOfDeath, other)
        }
        if (hasSelectedOption(localizedBirthDoseVaccines, resources.getStringArray(R.array.no_birth_dose_vaccines_array).getOrNull(3))) {
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
            babyName,
            infantTerm,
            corticosteroidGiven,
            outcomeAtBirth
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
        }

        prepopulateDefaults(deliveryOutcomeCache)

        if (isStillbirthOrDiedAtBirth(outcomeAtBirth.value)) {
            birthDoseVaccinesGiven.required = false
            vitaminKInjectionGiven.required = false
            list.add(newbornComplications)
        } else {
            list.addAll(getLiveBirthBaseFields())
        }
        if (saved != null) {
            restoreConditionalFields(saved, list)
        }
        refreshConditionalRequirements()

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

    private fun hasSelectedOption(value: String?, option: String?): Boolean {
        if (value.isNullOrBlank() || option.isNullOrBlank()) return false
        return value.split(",").any { it.trim() == option }
    }

    private fun getOtherOption(entries: Array<String>?): String? {
        return entries?.firstOrNull { it.contains("Other", ignoreCase = true) }
    }

    private fun isLiveBirthOutcome(value: String?): Boolean {
        val liveBirth = resources.getStringArray(R.array.no_outcome_at_birth_array)
            .getOrNull(OUTCOME_LIVE_BIRTH_INDEX)
        return value == liveBirth
    }

    private fun isStillbirthOrDiedAtBirth(value: String?): Boolean {
        if (value.isNullOrBlank()) return false
        return !isLiveBirthOutcome(value)
    }

    private fun getOutcomeDependentFields(): List<FormElement> {
        return listOf(
            gender,
            babyCriedAtBirth,
            typeOfResuscitation,
            weight,
            hadBirthDefect,
            birthDefect,
            otherDefect,
            newbornComplications,
            currentStatusOfBaby,
            causeOfDeath,
            otherCauseOfDeath,
            breastFeedingStarted,
            birthDoseVaccinesGiven,
            reasonForNoVaccines,
            opv0Dose,
            bcgDose,
            hepBDose,
            vitaminKInjectionGiven,
            reasonForNoVitaminK,
            vitkDose,
            birthCertificateIssued
        )
    }

    private fun getLiveBirthBaseFields(): List<FormElement> {
        return mutableListOf<FormElement>().apply {
            add(gender)
            add(babyCriedAtBirth)
            if (babyCriedAtBirth.value == resources.getStringArray(R.array.no_cried_immediately_array).getOrNull(1)) {
                add(typeOfResuscitation)
            }
            add(weight)
            add(hadBirthDefect)
            val anomalyArray = resources.getStringArray(R.array.no_congenital_anomaly_array)
            val hasAnomaly = hadBirthDefect.value == anomalyArray.getOrNull(0) ||
                hadBirthDefect.value == anomalyArray.getOrNull(2)
            if (hasAnomaly) {
                add(birthDefect)
                if (hasSelectedOption(birthDefect.value, getOtherOption(birthDefect.entries))) {
                    add(otherDefect)
                }
            }
            add(newbornComplications)
            add(currentStatusOfBaby)
            if (currentStatusOfBaby.value == resources.getStringArray(R.array.no_current_status_array)
                    .getOrNull(CURRENT_STATUS_DIED_INDEX)
            ) {
                add(causeOfDeath)
                if (hasSelectedOption(causeOfDeath.value, getOtherOption(causeOfDeath.entries))) {
                    add(otherCauseOfDeath)
                }
            } else {
                add(breastFeedingStarted)
                add(birthDoseVaccinesGiven)
                if (hasSelectedOption(
                        birthDoseVaccinesGiven.value,
                        resources.getStringArray(R.array.no_birth_dose_vaccines_array).getOrNull(3)
                    )
                ) {
                    add(reasonForNoVaccines)
                }
                add(opv0Dose)
                add(bcgDose)
                add(hepBDose)
                add(vitaminKInjectionGiven)
                if (vitaminKInjectionGiven.value == resources.getString(R.string.no)) {
                    add(reasonForNoVitaminK)
                }
                add(vitkDose)
                add(birthCertificateIssued)
            }
        }
    }

    private fun resetConditionalRequirements() {
        birthDefect.required = false
        otherDefect.required = false
        causeOfDeath.required = false
        otherCauseOfDeath.required = false
        reasonForNoVaccines.required = false
        reasonForNoVitaminK.required = false
        birthDoseVaccinesGiven.required = true
        vitaminKInjectionGiven.required = true
    }

    private fun refreshConditionalRequirements() {
        resetConditionalRequirements()

        if (!isLiveBirthOutcome(outcomeAtBirth.value)) {
            birthDoseVaccinesGiven.required = false
            vitaminKInjectionGiven.required = false
            return
        }

        val anomalyArray = resources.getStringArray(R.array.no_congenital_anomaly_array)
        if (hadBirthDefect.value == anomalyArray.getOrNull(0) || hadBirthDefect.value == anomalyArray.getOrNull(2)) {
            birthDefect.required = true
        }
        if (hasSelectedOption(birthDefect.value, getOtherOption(birthDefect.entries))) {
            otherDefect.required = true
        }

        if (currentStatusOfBaby.value == resources.getStringArray(R.array.no_current_status_array)
                .getOrNull(CURRENT_STATUS_DIED_INDEX)
        ) {
            causeOfDeath.required = true
            if (hasSelectedOption(causeOfDeath.value, getOtherOption(causeOfDeath.entries))) {
                otherCauseOfDeath.required = true
            }
            birthDoseVaccinesGiven.required = false
            vitaminKInjectionGiven.required = false
        } else {
            val noneVaccine = resources.getStringArray(R.array.no_birth_dose_vaccines_array).getOrNull(3)
            if (hasSelectedOption(birthDoseVaccinesGiven.value, noneVaccine)) {
                reasonForNoVaccines.required = true
            }
            if (vitaminKInjectionGiven.value == resources.getString(R.string.no)) {
                reasonForNoVitaminK.required = true
            }
        }
    }

    private suspend fun handleOutcomeAtBirthChange(selectedValue: String?): Int {
        resetConditionalRequirements()

        val fieldsToAdd = if (isLiveBirthOutcome(selectedValue)) {
            getLiveBirthBaseFields()
        } else {
            birthDoseVaccinesGiven.required = false
            vitaminKInjectionGiven.required = false
            listOf(newbornComplications)
        }

        if (isStillbirthOrDiedAtBirth(selectedValue)) {
            emitAlertErrorMessage(R.string.no_alert_stillbirth)
        }

        val index = getIndexOfElement(outcomeAtBirth)
        if (index == -1) return -1

        return triggerDependants(
            source = outcomeAtBirth,
            removeItems = getOutcomeDependentFields(),
            addItems = fieldsToAdd.distinct(),
            position = index + 1
        )
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            outcomeAtBirth.id -> {
                val selected = outcomeAtBirth.entries?.getOrNull(index)
                outcomeAtBirth.value = selected
                val updateIndex = handleOutcomeAtBirthChange(selected)
                refreshConditionalRequirements()
                updateIndex
            }

            gender.id -> {
                val selected = gender.entries?.getOrNull(index)
                gender.value = selected
                if (selected == gender.entries?.getOrNull(2)) {
                    emitAlertErrorMessage(R.string.no_alert_ambiguous_sex)
                }
                -1
            }

            babyCriedAtBirth.id -> {
                val selected = babyCriedAtBirth.entries?.getOrNull(index)
                babyCriedAtBirth.value = selected
                // "Cried after resuscitation" is index 1 — show typeOfResuscitation
                val updateIndex = toggleDependant(
                    source = babyCriedAtBirth,
                    condition = selected == resources.getStringArray(R.array.no_cried_immediately_array).getOrNull(1),
                    showItems = listOf(typeOfResuscitation)
                )
                refreshConditionalRequirements()
                updateIndex
            }

            hadBirthDefect.id -> {
                val selected = hadBirthDefect.entries?.getOrNull(index)
                hadBirthDefect.value = selected
                // "Yes" or "Suspected" (index 0 or 2) → show birthDefect
                val anomalyArray = resources.getStringArray(R.array.no_congenital_anomaly_array)
                val updateIndex = toggleDependant(
                    source = hadBirthDefect,
                    condition = selected == anomalyArray.getOrNull(0) || selected == anomalyArray.getOrNull(2),
                    showItems = listOf(birthDefect),
                    hideItems = listOf(otherDefect)
                )
                refreshConditionalRequirements()
                updateIndex
            }

            birthDefect.id -> {
                val updateIndex = toggleDependant(
                    source = birthDefect,
                    condition = hasSelectedOption(birthDefect.value, getOtherOption(birthDefect.entries)),
                    showItems = listOf(otherDefect)
                )
                refreshConditionalRequirements()
                updateIndex
            }

            currentStatusOfBaby.id -> {
                val selected = currentStatusOfBaby.entries?.getOrNull(index)
                currentStatusOfBaby.value = selected

                // Admitted statuses require PNC counseling alert
                val statusArray = resources.getStringArray(R.array.no_current_status_array)
                if (selected == statusArray.getOrNull(1) || selected == statusArray.getOrNull(2)) {
                    emitAlertErrorMessage(R.string.no_alert_pnc_counseling)
                }

                val updateIndex = if (selected == statusArray.getOrNull(CURRENT_STATUS_DIED_INDEX)) {
                    emitAlertErrorMessage(R.string.no_alert_neonatal_death)
                    triggerDependants(
                        source = currentStatusOfBaby,
                        removeItems = listOf(
                            otherCauseOfDeath,
                            breastFeedingStarted,
                            birthDoseVaccinesGiven,
                            reasonForNoVaccines,
                            opv0Dose,
                            bcgDose,
                            hepBDose,
                            vitaminKInjectionGiven,
                            reasonForNoVitaminK,
                            vitkDose,
                            birthCertificateIssued
                        ),
                        addItems = listOf(causeOfDeath),
                        position = getIndexOfElement(currentStatusOfBaby) + 1
                    )
                } else {
                    triggerDependants(
                        source = currentStatusOfBaby,
                        removeItems = listOf(causeOfDeath, otherCauseOfDeath),
                        addItems = listOf(
                            breastFeedingStarted,
                            birthDoseVaccinesGiven,
                            opv0Dose,
                            bcgDose,
                            hepBDose,
                            vitaminKInjectionGiven,
                            vitkDose,
                            birthCertificateIssued
                        ),
                        position = getIndexOfElement(currentStatusOfBaby) + 1
                    )
                }
                refreshConditionalRequirements()
                updateIndex
            }

            causeOfDeath.id -> {
                val updateIndex = toggleDependant(
                    source = causeOfDeath,
                    condition = hasSelectedOption(causeOfDeath.value, getOtherOption(causeOfDeath.entries)),
                    showItems = listOf(otherCauseOfDeath)
                )
                refreshConditionalRequirements()
                updateIndex
            }

            birthDoseVaccinesGiven.id -> {
                val updateIndex = toggleDependant(
                    source = birthDoseVaccinesGiven,
                    condition = hasSelectedOption(
                        birthDoseVaccinesGiven.value,
                        resources.getStringArray(R.array.no_birth_dose_vaccines_array).getOrNull(3)
                    ),
                    showItems = listOf(reasonForNoVaccines)
                )
                refreshConditionalRequirements()
                updateIndex
            }

            vitaminKInjectionGiven.id -> {
                val selected = vitaminKInjectionGiven.entries?.getOrNull(index)
                vitaminKInjectionGiven.value = selected
                val updateIndex = toggleDependant(
                    source = vitaminKInjectionGiven,
                    condition = selected == resources.getString(R.string.no),
                    showItems = listOf(reasonForNoVitaminK)
                )
                refreshConditionalRequirements()
                updateIndex
            }

            birthCertificateIssued.id -> {
                val selected = birthCertificateIssued.entries?.getOrNull(index)
                birthCertificateIssued.value = selected
                if (selected == birthCertificateIssued.entries?.getOrNull(2)) { // "No (Not applied)"
                    emitAlertErrorMessage(R.string.no_alert_birth_certificate_legal)
                }
                -1
            }

            weight.id -> {
                val validation = validateIntMinMax(weight)
                if (weight.errorText == null) {
                    weight.value?.toIntOrNull()?.let { weightInGm ->
                        when {
                            weightInGm < 1000 -> emitAlertErrorMessage(R.string.no_alert_elbw)
                            weightInGm < 1500 -> emitAlertErrorMessage(R.string.no_alert_vlbw)
                            weightInGm < 2500 -> emitAlertErrorMessage(R.string.no_alert_lbw)
                            weightInGm >= 4000 -> emitAlertErrorMessage(R.string.no_alert_macrosomia)
                        }
                    }
                }
                validation
            }

            newbornComplications.id -> {
                val noneOption = resources.getStringArray(R.array.no_newborn_complications_array).lastOrNull()
                if (!hasSelectedOption(newbornComplications.value, noneOption) &&
                    !newbornComplications.value.isNullOrBlank()
                ) {
                    emitAlertErrorMessage(R.string.no_alert_complications)
                }
                -1
            }

            otherDefect.id -> {
                validateAllAlphabetsSpecialOnEditText(otherDefect)
            }

            reasonForNoVaccines.id -> {
                validateAllAlphabetsSpecialOnEditText(reasonForNoVaccines)
            }

            reasonForNoVitaminK.id -> {
                validateAllAlphabetsSpecialOnEditText(reasonForNoVitaminK)
            }

            otherCauseOfDeath.id -> {
                validateAllAlphabetsSpecialOnEditText(otherCauseOfDeath)
            }

            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as InfantRegCache).let { form ->
            form.babyName = babyName.value
            form.infantTerm = infantTerm.value
            form.corticosteroidGiven = corticosteroidGiven.value
            form.outcomeAtBirth = getEnglishValueInArray(R.array.no_outcome_at_birth_array, outcomeAtBirth.value)

            form.genderID = gender.value?.let { value ->
                 gender.entries?.indexOf(value)?.plus(1)
            }

            // Map cried immediately to existing boolean field
            val criedValue = babyCriedAtBirth.value
            val immediateCry = resources.getStringArray(R.array.no_cried_immediately_array).getOrNull(0)
            val criedAfterResuscitation = resources.getStringArray(R.array.no_cried_immediately_array).getOrNull(1)
            form.babyCriedAtBirth = when (criedValue) {
                immediateCry -> true
                criedAfterResuscitation -> false
                else -> null
            }
            form.resuscitation = when (criedValue) {
                immediateCry -> false
                criedAfterResuscitation -> true
                else -> null
            }
            form.typeOfResuscitation = if (criedValue == criedAfterResuscitation) {
                getEnglishValuesInArray(R.array.no_type_of_resuscitation_array, typeOfResuscitation.value)
            } else {
                null
            }

            form.referred = referred.value
            form.hadBirthDefect = getEnglishValueInArray(R.array.no_congenital_anomaly_array, hadBirthDefect.value)
            form.birthDefect = getEnglishValuesInArray(R.array.no_type_of_congenital_anomaly_array, birthDefect.value)
            form.otherDefect = otherDefect.value
            // Convert grams to kg for DB storage (existing field is in kg)
            form.weight = weight.value?.toDoubleOrNull()?.let { it / 1000.0 }
            form.breastFeedingStarted = breastFeedingStarted.value == "Yes"

            form.newbornComplications = getEnglishValuesInArray(R.array.no_newborn_complications_array, newbornComplications.value)
            form.currentStatusOfBaby = getEnglishValueInArray(R.array.no_current_status_array, currentStatusOfBaby.value)
            form.causeOfDeath = getEnglishValuesInArray(R.array.no_cause_of_death_array, causeOfDeath.value)
            form.otherCauseOfDeath = otherCauseOfDeath.value
            form.birthDoseVaccinesGiven = getEnglishValuesInArray(R.array.no_birth_dose_vaccines_array, birthDoseVaccinesGiven.value)
            form.reasonForNoVaccines = reasonForNoVaccines.value

            form.vitaminKInjectionGiven = when (vitaminKInjectionGiven.value) {
                resources.getString(R.string.yes) -> true
                resources.getString(R.string.no) -> false
                else -> null
            }
            form.reasonForNoVitaminK = reasonForNoVitaminK.value
            form.birthCertificateIssued = getEnglishValueInArray(R.array.no_birth_certificate_array, birthCertificateIssued.value)

            form.opv0Dose = getLongFromDate(opv0Dose.value)
            form.bcgDose = getLongFromDate(bcgDose.value)
            form.hepBDose = getLongFromDate(hepBDose.value)
            form.vitkDose = getLongFromDate(vitkDose.value)

            if (isStillbirthOrDiedAtBirth(form.outcomeAtBirth)) {
                form.genderID = null
                form.babyCriedAtBirth = null
                form.resuscitation = null
                form.typeOfResuscitation = null
                form.weight = null
                form.hadBirthDefect = null
                form.birthDefect = null
                form.otherDefect = null
                form.currentStatusOfBaby = null
                form.causeOfDeath = null
                form.otherCauseOfDeath = null
                form.breastFeedingStarted = null
                form.birthDoseVaccinesGiven = null
                form.reasonForNoVaccines = null
                form.opv0Dose = null
                form.bcgDose = null
                form.hepBDose = null
                form.vitaminKInjectionGiven = null
                form.reasonForNoVitaminK = null
                form.vitkDose = null
                form.birthCertificateIssued = null
            }
        }
    }
}
