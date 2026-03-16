package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.helpers.setToStartOfTheDay
import org.piramalswasthya.cho.model.DeliveryOutcomeCache
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.PNCVisitCache
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.model.getDateStrFromLong
import java.util.Calendar
import java.util.concurrent.TimeUnit

class PncFormDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private var visit: Int = 0
    private var dateOfDelivery: Long = 0L
    private var previousPncVisitDate: Long? = null

    private val pncPeriod = FormElement(
        id = 1,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.pnc_period),
//        entries = resources.getStringArray(R.array.pnc_period_array),
        arrayId = -1,
        required = true,
        hasDependants = false
    )

    private val visitDate = FormElement(
        id = 2,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.pnc_visit_date),
        arrayId = -1,
        required = true,
        hasDependants = false
    )

    private val ifaTabsGiven = FormElement(
        id = 3,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.pnc_ifa_tabs_given),
        required = false,
        hasDependants = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 3,
        max = 400,
        min = 0,
    )

    private val calciumSupplementation = FormElement(
        id = 17,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.pnc_calcium_supplementation),
        required = false,
        hasDependants = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 3,
        max = 400,
        min = 0,
    )

    private val anyContraceptionMethod = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.pnc_any_contraception_method),
        entries = resources.getStringArray(R.array.pnc_confirmation_array),
        required = false,
        hasDependants = true
    )

    private val contraceptionMethod = FormElement(
        id = 5,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.pnc_contraception_method),
        entries = resources.getStringArray(R.array.pnc_contraception_method_array),
        required = false,
        hasDependants = true
    )

    private val otherPpcMethod = FormElement(
        id = 6,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.pnc_other_ppc_method),
        required = true,
        hasDependants = false
    )

    private val dateOfSterilisation = FormElement(
        id = 18,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.pnc_date_of_sterilization),
        arrayId = -1,
        max = System.currentTimeMillis(),
        required = true,
        hasDependants = false
    )

    private val anyDangerSign = FormElement(
        id = 19,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.pnc_any_danger_sign),
        entries = resources.getStringArray(R.array.pnc_confirmation_array),
        required = false,
        hasDependants = true
    )

    private val maternalSymptoms = FormElement(
        id = 20,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.pnc_maternal_symptoms),
        entries = resources.getStringArray(R.array.pnc_maternal_symptoms_array),
        required = true,
        hasDependants = true
    )

    private val otherMaternalSymptoms = FormElement(
        id = 21,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.pnc_other_maternal_symptoms),
        required = true,
        hasDependants = false,
        etMaxLength = 50
    )

    private val pallor = FormElement(
        id = 22,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.pnc_pallor),
        entries = resources.getStringArray(R.array.pnc_pallor_array),
        required = false,
        hasDependants = false
    )

    private val vaginalBleeding = FormElement(
        id = 23,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.pnc_vaginal_bleeding),
        entries = resources.getStringArray(R.array.pnc_vaginal_bleeding_array),
        required = false,
        hasDependants = false
    )

    private val motherDangerSign = FormElement(
        id = 7,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.pnc_mother_danger_sign),
        entries = resources.getStringArray(R.array.pnc_mother_danger_sign_array),
        required = false,
        hasDependants = true
    )

    private val otherDangerSign = FormElement(
        id = 8,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.pnc_other_danger_sign),
        required = true,
        hasDependants = false
    )

    private val referralFacility = FormElement(
        id = 9,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.pnc_referral_facility),
        entries = resources.getStringArray(R.array.pnc_referral_facility_array),
        required = false,
        hasDependants = false
    )

    private val motherDeath = FormElement(
        id = 10,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.pnc_mother_death),
        entries = resources.getStringArray(R.array.pnc_confirmation_array),
        required = false,
        hasDependants = true,
    )

    private val deathDate = FormElement(
        id = 11,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.pnc_death_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = false
    )

    private val causeOfDeath = FormElement(
        id = 12,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.pnc_death_cause),
        entries = resources.getStringArray(R.array.pnc_death_cause_array),
        required = true,
        hasDependants = true,
    )

    private val otherDeathCause = FormElement(
        id = 13,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.pnc_other_death_cause),
        required = true,
        hasDependants = false,
        etMaxLength = 300
    )

    private val placeOfDeath = FormElement(
        id = 14,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.pnc_death_place),
        entries = resources.getStringArray(R.array.pnc_death_place_array),
        required = true,
        hasDependants = true,
    )

    private val otherPlaceOfDeath = FormElement(
        id = 24,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.other_place_of_death),
        required = true,
        hasDependants = false
    )

    private val remarks = FormElement(
        id = 15,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.pnc_remarks),
        required = false,
        hasDependants = false
    )

    private val deliveryDate = FormElement(
        id = 16,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.delivery_date),
        required = false,
        hasDependants = false
    )

    companion object {
        fun getMinDeliveryDate(): Long {
            val cal = Calendar.getInstance()
            cal.add(Calendar.YEAR, -1)
            return cal.timeInMillis
        }
    }

    private val sterilisation: Array<String> by lazy {
        resources.getStringArray(R.array.sterilization_methods_array)
    }

    suspend fun setUpPage(
        visitNumber: Int,
        ben: PatientDisplay?,
        deliveryOutcomeCache: DeliveryOutcomeCache,
        previousPnc: PNCVisitCache?,
        saved: PNCVisitCache?,
        hasPreviousPermanentSterilization: Boolean = false,
        lastSterilizationVisit: PNCVisitCache? = null
    ) {
        val list = mutableListOf(
            deliveryDate,
            pncPeriod,
            visitDate,
            motherDeath,
            ifaTabsGiven,
            calciumSupplementation,
            anyContraceptionMethod,
            anyDangerSign,
            maternalSymptoms,
            pallor,
            vaginalBleeding,
            referralFacility,
            remarks
        )
        dateOfDelivery = deliveryOutcomeCache.dateOfDelivery ?: 0L
        previousPncVisitDate = previousPnc?.pncDate
        
        if (dateOfDelivery != 0L) {
            deathDate.min = dateOfDelivery
            dateOfSterilisation.min = dateOfDelivery
            deliveryDate.isEnabled = false
        }
        deathDate.max = System.currentTimeMillis()
        dateOfSterilisation.max = System.currentTimeMillis()
        anyDangerSign.value = anyDangerSign.entries!!.last()
        motherDeath.value = motherDeath.entries!!.last()
        
        // Set default value for motherDeath to "No"
        motherDeath.value = motherDeath.entries!!.last()
        
        val daysSinceDeliveryMillis = if (deliveryOutcomeCache.dateOfDelivery != null) {
            val deliveryCal = Calendar.getInstance()
            deliveryCal.timeInMillis = deliveryOutcomeCache.dateOfDelivery!!
            deliveryCal.setToStartOfTheDay()
            val deliveryMillis = deliveryCal.timeInMillis
            Calendar.getInstance().setToStartOfTheDay().timeInMillis - deliveryMillis
        } else {
            0L
        }
        val daysSinceDelivery = if (daysSinceDeliveryMillis > 0) {
            TimeUnit.MILLISECONDS.toDays(daysSinceDeliveryMillis)
        } else 0L
        
        deliveryDate.value = getDateFromLong(dateOfDelivery)
        pncPeriod.entries =
            listOf(
                1,
                3,
                7,
                14,
                21,
                28,
                42
            ).filter { if (daysSinceDelivery == 0L) it <= 1 else it <= daysSinceDelivery }
                .filter { it > (previousPnc?.pncPeriod ?: 0) }
                .map { "Day $it" }.toTypedArray()

        // Handle permanent sterilization - disable contraception fields if already selected
        if (hasPreviousPermanentSterilization && lastSterilizationVisit != null) {
            anyContraceptionMethod.isEnabled = false
            contraceptionMethod.isEnabled = false
            dateOfSterilisation.isEnabled = false
            otherPpcMethod.isEnabled = false

            anyContraceptionMethod.value = if (lastSterilizationVisit.anyContraceptionMethod == true)
                anyContraceptionMethod.entries!!.first() else anyContraceptionMethod.entries!!.last()

            contraceptionMethod.value = lastSterilizationVisit.contraceptionMethod
            dateOfSterilisation.value = getDateFromLong(lastSterilizationVisit.sterilisationDate ?: System.currentTimeMillis())
            otherPpcMethod.value = lastSterilizationVisit.otherPpcMethod

            if (lastSterilizationVisit.anyContraceptionMethod == true) {
                list.add(list.indexOf(anyContraceptionMethod) + 1, contraceptionMethod)

                if (lastSterilizationVisit.contraceptionMethod?.let { it in sterilisation } == true) {
                    list.add(list.indexOf(contraceptionMethod) + 1, dateOfSterilisation)
                }

                if (lastSterilizationVisit.contraceptionMethod == contraceptionMethod.entries!!.last()) {
                    list.add(list.indexOf(contraceptionMethod) + 1, otherPpcMethod)
                }
            }
        }

        saved?.let {
            pncPeriod.value = "Day ${it.pncPeriod}"
            visitDate.value = getDateFromLong(it.pncDate)
            ifaTabsGiven.value = it.ifaTabsGiven?.toString()
            calciumSupplementation.value = it.calciumSupplementation?.toString()
            anyContraceptionMethod.value = it.anyContraceptionMethod?.let {
                if (it)
                    anyContraceptionMethod.entries!!.first()
                else
                    anyContraceptionMethod.entries!!.last()
            }
            if (it.anyContraceptionMethod == true) {
                list.add(list.indexOf(anyContraceptionMethod) + 1, contraceptionMethod)
            }
            contraceptionMethod.value = it.contraceptionMethod
            dateOfSterilisation.value = getDateFromLong(it.sterilisationDate ?: System.currentTimeMillis())
            if (it.contraceptionMethod == contraceptionMethod.entries!!.last()) {
                list.add(list.indexOf(contraceptionMethod) + 1, otherPpcMethod)
            }
            if (it.contraceptionMethod?.let { method -> method in sterilisation } == true) {
                list.add(list.indexOf(contraceptionMethod) + 1, dateOfSterilisation)
            }
            otherPpcMethod.value = it.otherPpcMethod
            anyDangerSign.value = it.anyDangerSign
            anyDangerSign.value?.let { dangerSignValue ->
                val isDangerSignYes = dangerSignValue == anyDangerSign.entries!!.first()
                referralFacility.required = isDangerSignYes
                if (isDangerSignYes) {
                    list.add(list.indexOf(anyDangerSign) + 1, motherDangerSign)
                }
            }
            maternalSymptoms.value = it.maternalSymptoms
            if (it.maternalSymptoms?.contains("Other") == true || it.maternalSymptoms?.contains(maternalSymptoms.entries!!.last()) == true) {
                list.add(list.indexOf(maternalSymptoms) + 1, otherMaternalSymptoms)
            }
            otherMaternalSymptoms.value = it.otherMaternalSymptoms
            pallor.value = it.pallor
            // Check for Severe pallor → referral alert
            if (it.pallor?.equals("Severe", ignoreCase = true) == true) {
                referralFacility.required = true
                if (it.referralFacility.isNullOrBlank()) {
                    referralFacility.errorText = resources.getString(R.string.pnc_referral_alert_severe_pallor)
                }
            }
            
            vaginalBleeding.value = it.vaginalBleeding
            // Check for Heavy bleeding or foul smell → referral alert
            val vaginalBleedingValue = it.vaginalBleeding?.lowercase() ?: ""
            if (vaginalBleedingValue.contains("heavy", ignoreCase = true) || 
                vaginalBleedingValue.contains("foul smell", ignoreCase = true)) {
                referralFacility.required = true
                if (it.referralFacility.isNullOrBlank()) {
                    referralFacility.errorText = resources.getString(R.string.pnc_referral_alert_vaginal_bleeding)
                }
            }
            motherDangerSign.value = it.motherDangerSign
            if (it.motherDangerSign == motherDangerSign.entries!!.last()) {
                list.add(list.indexOf(motherDangerSign) + 1, otherDangerSign)
            }
            otherDangerSign.value = it.otherDangerSign
            referralFacility.value = it.referralFacility
            motherDeath.value =
                if (it.motherDeath) motherDeath.entries!!.first() else motherDeath.entries!!.last()
            if (it.motherDeath) {
                deathDate.value = getDateStrFromLong(it.deathDate)
                causeOfDeath.value = it.causeOfDeath
                otherDeathCause.value = it.otherDeathCause
                placeOfDeath.value = it.placeOfDeath
                otherPlaceOfDeath.value = it.otherPlaceOfDeath
                list.addAll(
                    list.indexOf(motherDeath) + 1,
                    listOf(deathDate, causeOfDeath, placeOfDeath)
                )
                if (causeOfDeath.value == causeOfDeath.entries!!.last())
                    list.add(list.indexOf(causeOfDeath) + 1, otherDeathCause)
                placeOfDeath.entries?.indexOf(it.placeOfDeath)?.takeIf { index -> index >= 0 }?.let { index ->
                    if (index == 8) { // "Other" is at index 8
                        list.add(list.indexOf(placeOfDeath) + 1, otherPlaceOfDeath)
                    }
                }
            }
            remarks.value = it.remarks
        }

//        pncPeriod.entries = pncPeriod.entries!!.
//        if (saved == null) {
//            dateOfDelivery.value = Dataset.getDateFromLong(System.currentTimeMillis())
//            dateOfDischarge.value = Dataset.getDateFromLong(System.currentTimeMillis())
//        } else {
//            list = mutableListOf(
//                dateOfDelivery,
//                timeOfDelivery,
//                placeOfDelivery,
//                typeOfDelivery,
//                hadComplications,
////                complication,
////                causeOfDeath,
////                otherCauseOfDeath,
////                otherComplication,
//                deliveryOutcome,
//                liveBirth,
//                stillBirth,
//                dateOfDischarge,
//                timeOfDischarge,
//                isJSYBenificiary
//            )
//            dateOfDelivery.value = Dataset.getDateFromLong(saved.dateOfDelivery)
//            timeOfDelivery.value = saved.timeOfDelivery
//            placeOfDelivery.value = saved.placeOfDelivery
//            typeOfDelivery.value = saved.typeOfDelivery
//            hadComplications.value = if (saved.hadComplications == true) "Yes" else "No"
//            complication.value = saved.complication
//            causeOfDeath.value = saved.causeOfDeath
//            otherCauseOfDeath.value = saved.otherCauseOfDeath
//            otherComplication.value = saved.otherComplication
//            deliveryOutcome.value = saved.deliveryOutcome.toString()
//            liveBirth.value = saved.liveBirth.toString()
//            stillBirth.value = saved.stillBirth.toString()
//            dateOfDischarge.value = Dataset.getDateFromLong(saved.dateOfDischarge)
//            timeOfDischarge.value = saved.timeOfDischarge
//            isJSYBenificiary.value = if (saved.isJSYBenificiary == true) "Yes" else "No"
//        }
//        ben?.let {
//            dateOfDelivery.min = it.regDate
//        }
        setUpPage(list)

    }

    /**
     * Evaluates all referral conditions and updates referralFacility.required and errorText accordingly.
     * Checks: anyDangerSign, maternalSymptoms (≥2 symptoms), pallor (Severe), vaginalBleeding (Heavy/Foul smell).
     * Priority: anyDangerSign > multiple symptoms > severe pallor > vaginal bleeding
     */
    private fun updateReferralRequirement() {
        // Check anyDangerSign - highest priority
        val hasAnyDangerSign = anyDangerSign.value?.equals(anyDangerSign.entries!!.first(), ignoreCase = true) == true
        
        if (hasAnyDangerSign) {
            referralFacility.required = true
            referralFacility.errorText = null
            return
        }
        
        // Check maternalSymptoms - ≥2 actual symptoms (excluding "None")
        val maternalSymptomsSelected = maternalSymptoms.value?.split(",")?.map { it.trim() } ?: emptyList()
        val noneEntry = maternalSymptoms.entries?.find { it.equals("None", ignoreCase = true) }
        val actualSymptoms = maternalSymptomsSelected.filter { it.isNotBlank() && !it.equals(noneEntry, ignoreCase = true) }
        
        if (actualSymptoms.size >= 2) {
            referralFacility.required = true
            // Only show alert if no facility selected yet; clear once selected so it doesn't block submission
            if (referralFacility.value.isNullOrBlank()) {
                referralFacility.errorText = resources.getString(R.string.pnc_referral_alert_multiple_symptoms)
            } else {
                referralFacility.errorText = null
            }
            return
        }
        
        // Check pallor - Severe
        val pallorValue = pallor.value?.trim() ?: ""
        if (pallorValue.equals("Severe", ignoreCase = true)) {
            referralFacility.required = true
            if (referralFacility.value.isNullOrBlank()) {
                referralFacility.errorText = resources.getString(R.string.pnc_referral_alert_severe_pallor)
            } else {
                referralFacility.errorText = null
            }
            return
        }
        
        // Check vaginalBleeding - Heavy or Foul smell
        val vaginalBleedingValue = vaginalBleeding.value?.trim()?.lowercase() ?: ""
        if (vaginalBleedingValue.contains("heavy", ignoreCase = true) || 
            vaginalBleedingValue.contains("foul smell", ignoreCase = true)) {
            referralFacility.required = true
            if (referralFacility.value.isNullOrBlank()) {
                referralFacility.errorText = resources.getString(R.string.pnc_referral_alert_vaginal_bleeding)
            } else {
                referralFacility.errorText = null
            }
            return
        }
        
        // No referral conditions met - clear requirement
        referralFacility.required = false
        referralFacility.errorText = null
    }

    // ─── Helper: handle PNC period selection and compute visit date range ─
    private fun handlePncPeriodChange(): Int {
        visitDate.inputType = InputType.DATE_PICKER
        visitDate.value = null
        val today = Calendar.getInstance().setToStartOfTheDay().timeInMillis
        val deliveryCal = Calendar.getInstance()
        deliveryCal.timeInMillis = dateOfDelivery
        deliveryCal.setToStartOfTheDay()
        val deliveryDateStart = deliveryCal.timeInMillis

        // Previous PNC visit date (if exists) - must be after this
        val previousVisitDateStart = previousPncVisitDate?.let {
            val prevCal = Calendar.getInstance()
            prevCal.timeInMillis = it
            prevCal.setToStartOfTheDay()
            prevCal.timeInMillis
        }

        when (val visitNumber = pncPeriod.value!!.substring(4).toInt()) {
            1 -> {
                // Day 1: Delivery date only (24-48 hours)
                visitDate.min = minOf(today, deliveryDateStart)
                visitDate.max = minOf(
                    today,
                    deliveryDateStart + TimeUnit.DAYS.toMillis(1)
                )
            }

            3 -> {
                // Day 3: Exactly Delivery + 3 days
                val day3Date = deliveryDateStart + TimeUnit.DAYS.toMillis(3)
                visitDate.min = minOf(today, day3Date)
                visitDate.max = minOf(today, day3Date)
            }

            7, 14, 21, 28, 42 -> {
                // Day 7/14/21/28/42: ±3 days from scheduled date
                val scheduledDate = deliveryDateStart + TimeUnit.DAYS.toMillis(visitNumber.toLong())
                val minDate = scheduledDate - TimeUnit.DAYS.toMillis(3)
                val maxDate = scheduledDate + TimeUnit.DAYS.toMillis(3)

                // Visit date cannot be before delivery date
                val minAllowed = maxOf(deliveryDateStart, minDate)
                // Visit date cannot be after today
                val maxAllowed = minOf(today, maxDate)
                // Visit date cannot be earlier than previous PNC visit date
                val finalMin = previousVisitDateStart?.let { maxOf(minAllowed, it + TimeUnit.DAYS.toMillis(1)) } ?: minAllowed

                visitDate.min = minOf(today, finalMin)
                visitDate.max = minOf(today, maxAllowed)
            }

            else -> throw IllegalStateException("Illegal PNC Date $visitNumber")
        }
        return -1
    }

    // ─── Helper: handle contraception method selection ─────────────────
    private fun handleContraceptionMethodChange(index: Int): Int {
        val selected = contraceptionMethod.entries?.getOrNull(index)?.trim() ?: ""
        val anyOtherValue = contraceptionMethod.entries!!.last().trim()
        val result1 = if (selected.equals(anyOtherValue, ignoreCase = true)) {
            triggerDependants(
                source = contraceptionMethod,
                passedIndex = index,
                triggerIndex = contraceptionMethod.entries!!.lastIndex,
                target = otherPpcMethod
            )
        } else {
            triggerDependants(
                source = contraceptionMethod,
                passedIndex = -1,
                triggerIndex = contraceptionMethod.entries!!.lastIndex,
                target = otherPpcMethod
            )
        }

        val isSterilisation = sterilisation.any { it.equals(selected, ignoreCase = true) }
        val result2 = if (isSterilisation) {
            dateOfSterilisation.min = dateOfDelivery
            dateOfSterilisation.max = System.currentTimeMillis()
            triggerDependants(
                source = contraceptionMethod,
                passedIndex = index,
                triggerIndex = index,
                target = dateOfSterilisation
            )
        } else {
            dateOfSterilisation.value = null
            triggerDependants(
                source = contraceptionMethod,
                passedIndex = -1,
                triggerIndex = index,
                target = dateOfSterilisation
            )
        }

        return if (result1 != -1) result1 else result2
    }

    // ─── Helper: handle danger sign toggle and referral requirement ────
    private fun handleDangerSignChange(index: Int): Int {
        val result = triggerDependants(
            source = anyDangerSign,
            passedIndex = index,
            triggerIndex = 0,
            target = motherDangerSign,
            targetSideEffect = listOf(otherDangerSign)
        )

        val oldRequiredState = referralFacility.required
        if (index == 0) {
            referralFacility.required = true
        } else {
            referralFacility.required = false
            referralFacility.errorText = null
        }
        val referralFacilityIndex = getIndexById(referralFacility.id)
        return if (oldRequiredState != referralFacility.required && referralFacilityIndex != -1) {
            referralFacilityIndex
        } else {
            result
        }
    }

    // ─── Helper: handle mother death toggle ───────────────────────────
    private fun handleMotherDeathChange(index: Int): Int {
        return if (index == 0) {
            triggerDependants(
                source = motherDeath,
                removeItems = listOf(
                    ifaTabsGiven,
                    calciumSupplementation,
                    anyContraceptionMethod,
                    anyDangerSign,
                    maternalSymptoms,
                    pallor,
                    vaginalBleeding,
                    referralFacility,
                    remarks
                ),
                addItems = listOf(
                    deathDate,
                    causeOfDeath,
                    placeOfDeath,
                    otherDeathCause,
                    otherPlaceOfDeath
                )
            )
        } else {
            triggerDependants(
                source = motherDeath,
                removeItems = listOf(
                    deathDate,
                    causeOfDeath,
                    placeOfDeath,
                    otherDeathCause,
                    otherPlaceOfDeath
                ),
                addItems = listOf(
                    ifaTabsGiven,
                    calciumSupplementation,
                    anyContraceptionMethod,
                    anyDangerSign,
                    maternalSymptoms,
                    pallor,
                    vaginalBleeding,
                    referralFacility,
                    remarks
                )
            )
        }
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            pncPeriod.id -> handlePncPeriodChange()

            ifaTabsGiven.id -> {
                // IFA Tablets: >0 and ≤400, supports 180-day postpartum supplementation
                val result = validateIntMinMax(ifaTabsGiven)
                ifaTabsGiven.value?.toIntOrNull()?.let { value ->
                    if (value > 400) {
                        ifaTabsGiven.errorText = resources.getString(R.string.pnc_ifa_max_error)
                        return getIndexById(ifaTabsGiven.id)
                    }
                }
                result
            }
            
            calciumSupplementation.id -> {
                // Calcium: ≤400
                val result = validateIntMinMax(calciumSupplementation)
                calciumSupplementation.value?.toIntOrNull()?.let { value ->
                    if (value > 400) {
                        calciumSupplementation.errorText = resources.getString(R.string.pnc_calcium_max_error)
                        return getIndexById(calciumSupplementation.id)
                    }
                }
                result
            }
            
            anyContraceptionMethod.id -> triggerDependants(
                source = anyContraceptionMethod,
                passedIndex = index,
                triggerIndex = 0,
                target = contraceptionMethod,
                targetSideEffect = listOf(otherPpcMethod, dateOfSterilisation)
            )

            contraceptionMethod.id -> handleContraceptionMethodChange(index)

            anyDangerSign.id -> handleDangerSignChange(index)

            maternalSymptoms.id -> {
                // Check if "Other" is selected
                val selectedValues = maternalSymptoms.value?.split(",")?.map { it.trim() } ?: emptyList()
                val hasOther = selectedValues.any { it.equals(maternalSymptoms.entries!!.last(), ignoreCase = true) }
                
                // Update referral requirement based on all conditions
                updateReferralRequirement()
                
                return triggerDependants(
                    source = maternalSymptoms,
                    passedIndex = if (hasOther) maternalSymptoms.entries!!.lastIndex else -1,
                    triggerIndex = maternalSymptoms.entries!!.lastIndex,
                    target = otherMaternalSymptoms
                )
            }

            otherMaternalSymptoms.id -> {
                validateAllAlphabetsSpaceOnEditText(otherMaternalSymptoms)
            }

            motherDangerSign.id ->
                triggerDependants(
                    source = motherDangerSign,
                    passedIndex = index,
                    triggerIndex = motherDangerSign.entries!!.lastIndex,
                    target = otherDangerSign
                )
            
            pallor.id -> {
                // Update referral requirement based on all conditions
                updateReferralRequirement()
                val referralFacilityIndex = getIndexById(referralFacility.id)
                return if (referralFacility.required && referralFacilityIndex != -1) referralFacilityIndex else -1
            }
            
            vaginalBleeding.id -> {
                // Update referral requirement based on all conditions
                updateReferralRequirement()
                val referralFacilityIndex = getIndexById(referralFacility.id)
                return if (referralFacility.required && referralFacilityIndex != -1) referralFacilityIndex else -1
            }

            referralFacility.id -> {
                // Clear the informational alert errorText once the user selects a facility
                // so it doesn't block form submission
                if (!referralFacility.value.isNullOrBlank()) {
                    referralFacility.errorText = null
                }
                -1
            }

            motherDeath.id -> handleMotherDeathChange(index)

            causeOfDeath.id -> {
                triggerDependants(
                    source = causeOfDeath,
                    passedIndex = index,
                    triggerIndex = causeOfDeath.entries!!.lastIndex,
                    target = otherDeathCause
                )
            }

            placeOfDeath.id -> {
                val triggerIndex = 8 // "Other" is at index 8
                return triggerDependants(
                    source = placeOfDeath,
                    passedIndex = index,
                    triggerIndex = triggerIndex,
                    target = otherPlaceOfDeath
                )
            }

//
            else -> -1
        }
    }

//    private fun validateMaxDeliveryOutcome() : Int {
//        if(!liveBirth.value.isNullOrEmpty() && !stillBirth.value.isNullOrEmpty() &&
//            !deliveryOutcome.value.isNullOrEmpty() && deliveryOutcome.errorText.isNullOrEmpty()) {
//            if(deliveryOutcome.value!!.toInt() != liveBirth.value!!.toInt() + stillBirth.value!!.toInt()) {
//                deliveryOutcome.errorText = "Outcome of Delivery should equal to sum of Live and Still births"
//            }
//        }
//        return -1
//    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as PNCVisitCache).let { form ->
            form.pncPeriod = pncPeriod.value!!.substring(4).toInt()
            form.pncDate = getLongFromDate(visitDate.value!!)
            form.ifaTabsGiven = ifaTabsGiven.value?.takeIf { it.isNotEmpty() }?.toInt()
            form.calciumSupplementation = calciumSupplementation.value?.takeIf { it.isNotEmpty() }?.toInt()
            form.anyContraceptionMethod =
                anyContraceptionMethod.value?.let { it == anyContraceptionMethod.entries!!.first() }
            form.contraceptionMethod = contraceptionMethod.value?.takeIf { it.isNotEmpty() }
            form.sterilisationDate = dateOfSterilisation.value?.let { getLongFromDate(it) }
            form.otherPpcMethod = otherPpcMethod.value?.takeIf { it.isNotEmpty() }
            form.anyDangerSign = anyDangerSign.value?.takeIf { it.isNotEmpty() }
            form.maternalSymptoms = maternalSymptoms.value?.takeIf { it.isNotEmpty() }
            form.otherMaternalSymptoms = otherMaternalSymptoms.value?.takeIf { it.isNotEmpty() }
            form.pallor = pallor.value?.takeIf { it.isNotEmpty() }
            form.vaginalBleeding = vaginalBleeding.value?.takeIf { it.isNotEmpty() }
            form.motherDangerSign = motherDangerSign.value?.takeIf { it.isNotEmpty() }
            form.otherDangerSign = otherDangerSign.value?.takeIf { it.isNotEmpty() }
            form.referralFacility = referralFacility.value?.takeIf { it.isNotEmpty() }
            form.motherDeath =
                motherDeath.value?.let { it == motherDeath.entries!!.first() } ?: false
            form.deathDate = deathDate.value?.let { getLongFromDate(it) }
            form.causeOfDeath = causeOfDeath.value?.takeIf { it.isNotEmpty() }
            form.otherDeathCause = otherDeathCause.value?.takeIf { it.isNotEmpty() }
            form.placeOfDeath = placeOfDeath.value?.takeIf { it.isNotEmpty() }
            form.otherPlaceOfDeath = otherPlaceOfDeath.value?.takeIf { it.isNotEmpty() }
            form.remarks = remarks.value?.takeIf { it.isNotEmpty() }
        }
    }
}