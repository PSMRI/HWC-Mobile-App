package org.piramalswasthya.cho.configuration

import android.content.Context
import android.widget.LinearLayout
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.helpers.getWeeksOfPregnancy
import org.piramalswasthya.cho.model.DeliveryOutcomeCache
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.PregnantWomanAncCache
import org.piramalswasthya.cho.model.PregnantWomanRegistrationCache
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Dataset for Delivery Outcome form per MHWC-199 requirements
 * Implements all validations, alerts, and conditional field visibility
 * Also handles mother's condition, complications, and admission status post-delivery
 */
class DeliveryOutcomeDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    companion object {
        private const val WEEKS_IN_PREGNANCY = 40
        private const val DAYS_IN_WEEK = 7
        private const val EDD_TOLERANCE_WEEKS_BEFORE = 4
        private const val EDD_TOLERANCE_WEEKS_AFTER = 2
        private const val POST_TERM_THRESHOLD_DAYS = 14
        private const val PRETERM_THRESHOLD_WEEKS = 37
        private const val EXTREMELY_PRETERM_WEEKS = 28
        private const val VERY_PRETERM_WEEKS = 32
        private const val POST_TERM_WEEKS = 42
        
        // Indices in do_place_of_delivery_array
        private const val PLACE_PRIVATE_HOSPITAL_INDEX = 6
        private const val PLACE_HOME_DELIVERY_INDEX = 7
        private const val PLACE_ON_THE_WAY_INDEX = 8
        
        // Indices in do_mode_of_delivery_array
        private const val MODE_ASSISTED_INDEX = 1
        private const val MODE_LSCS_INDEX = 2
        private const val MODE_EMERGENCY_LSCS_INDEX = 3
        
        // Indices in do_delivery_conducted_by_array
        private const val CONDUCTED_BY_DAI_TBA_INDEX = 4
        private const val CONDUCTED_BY_FAMILY_MEMBER_INDEX = 5
        private const val CONDUCTED_BY_SELF_INDEX = 6
    }

    // Woman Details Section (Display-only, auto-populated)
    private val womanName = FormElement(
        id = 100,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.do_woman_name),
        required = false
    )

    private val womanAge = FormElement(
        id = 101,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.do_woman_age),
        required = false
    )

    private val caseId = FormElement(
        id = 102,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.do_case_id),
        required = false
    )

    // Date of Delivery (dd/MM/yyyy per JIRA MHWC-199)
    private val dateOfDelivery = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.do_delivery_date),
        arrayId = -1,
        required = true,
        hasDependants = true,
        max = System.currentTimeMillis(), // Cannot be future date
        dateFormat = DATE_FORMAT_DD_MM_YYYY
    )

    // Time of Delivery (12-hour format)
    private val timeOfDelivery = FormElement(
        id = 2,
        inputType = InputType.TIME_PICKER,
        title = resources.getString(R.string.do_delivery_time),
        arrayId = -1,
        required = false,
        hasDependants = true
    )

    // Gestational Age at Delivery (Auto-calculated, display-only)
    private val gestationalAgeAtDelivery = FormElement(
        id = 3,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.do_gestational_age_at_delivery),
        required = false
    )

    // Place of Delivery
    private val placeOfDelivery = FormElement(
        id = 4,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.do_place_of_delivery),
        entries = resources.getStringArray(R.array.do_place_of_delivery_array),
        required = true,
        hasDependants = true
    )

    // Private Hospital Name (Conditional)
    private val privateHospitalName = FormElement(
        id = 5,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.do_private_hospital_name),
        required = false,
        etMaxLength = 100,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT,
        hasDependants = false
    )

    // Delivery Conducted By
    private val deliveryConductedBy = FormElement(
        id = 6,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.do_delivery_conducted_by),
        entries = resources.getStringArray(R.array.do_delivery_conducted_by_array),
        required = true,
        hasDependants = true
    )

    // Mode of Delivery
    private val modeOfDelivery = FormElement(
        id = 7,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.do_mode_of_delivery),
        entries = resources.getStringArray(R.array.do_mode_of_delivery_array),
        required = true,
        hasDependants = true
    )

    // Indication for LSCS/Assisted (Multi-select checkbox)
    private val indicationForLSCS = FormElement(
        id = 8,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.do_indication_for_lscs),
        entries = resources.getStringArray(R.array.do_indication_for_lscs_array),
        required = false, // Will be set to true conditionally
        hasDependants = true
    )

    // Indication Other (Free text if "Other" selected)
    private val indicationForLSCSOther = FormElement(
        id = 9,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.do_indication_for_lscs_other),
        required = false,
        etMaxLength = 200,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT,
        multiLine = true,
        hasDependants = false
    )

    // Mother's Condition (post-delivery)
    /** 4 options: use vertical layout per requirement (vertical when >2 options). */
    private val motherCondition = FormElement(
        id = 10,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.do_mother_condition),
        entries = resources.getStringArray(R.array.do_mother_condition_array),
        required = true,
        hasDependants = true,
        hasAlertError = true,
        orientation = LinearLayout.VERTICAL
    )

    private val maternalComplications = FormElement(
        id = 11,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.do_maternal_complications),
        entries = resources.getStringArray(R.array.do_maternal_complications_array),
        required = true,
        hasDependants = true,
        hasAlertError = true
    )

    /** 2 options: use horizontal layout per requirement (horizontal when ≤2 options). */
    private val motherCurrentlyAdmitted = FormElement(
        id = 12,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.do_mother_currently_admitted),
        entries = resources.getStringArray(R.array.do_mother_admitted_array),
        required = true,
        hasDependants = true,
        orientation = LinearLayout.HORIZONTAL
    )

    private val dateOfDischarge = FormElement(
        id = 13,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.do_discharge_date),
        arrayId = -1,
        required = true,
        hasDependants = false,
        max = System.currentTimeMillis()
    )

    private val timeOfDischarge = FormElement(
        id = 14,
        inputType = InputType.TIME_PICKER,
        title = resources.getString(R.string.do_discharge_time),
        arrayId = -1,
        required = false,
        hasDependants = false
    )

    private val deliveryOutcome = FormElement(
        id = 15,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.do_delivery_outcome),
        required = true,
        hasDependants = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 1,
        max = 6,
        min = 0
    )

    private val liveBirth = FormElement(
        id = 16,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.do_live_birth),
        required = true,
        hasDependants = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 1,
        max = 6,
        min = 0
    )

    private val stillBirth = FormElement(
        id = 17,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.do_still_birth),
        required = true,
        hasDependants = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 1,
        max = 6,
        min = 0
    )

    // Store EDD and LMP for calculations
    private var eddDate: Long = 0L
    private var lmpDate: Long = 0L
    private var patientCaseId: String? = null
    private var deliveryDateMillis: Long = 0L

    /**
     * Set up the form page with patient and pregnancy data
     */
    suspend fun setUpPage(
        pwr: PregnantWomanRegistrationCache,
        anc: PregnantWomanAncCache?,
        saved: DeliveryOutcomeCache?,
        patientName: String,
        patientAge: String,
        caseId: String
    ) {
        // Validate LMP date
        if (pwr.lmpDate <= 0) {
            throw IllegalStateException("Invalid LMP date: ${pwr.lmpDate}")
        }
        
        // Store LMP and calculate EDD
        lmpDate = pwr.lmpDate
        eddDate = getEddFromLmp(lmpDate)
        patientCaseId = caseId

        // Note: Woman name, age, and case ID are displayed in the card header,
        // so they are not included in the form elements list

        // Set date validation constraints: EDD ± 4 weeks
        val minDate = eddDate - TimeUnit.DAYS.toMillis(EDD_TOLERANCE_WEEKS_BEFORE * DAYS_IN_WEEK.toLong())
        val maxDate = minOf(
            eddDate + TimeUnit.DAYS.toMillis(EDD_TOLERANCE_WEEKS_AFTER * DAYS_IN_WEEK.toLong()),
            System.currentTimeMillis() // Cannot be future date
        )
        dateOfDelivery.min = maxOf(minDate, pwr.lmpDate)
        dateOfDelivery.max = maxDate

        val formElements = mutableListOf<FormElement>(
            dateOfDelivery,
            timeOfDelivery,
            gestationalAgeAtDelivery,
            placeOfDelivery,
            deliveryConductedBy,
            modeOfDelivery
        )

        if (saved == null) {
            // New record - set default date to today (dd/MM/yyyy per JIRA)
            dateOfDelivery.value = getDateFromLong(System.currentTimeMillis(), DATE_FORMAT_DD_MM_YYYY)
            deliveryDateMillis = System.currentTimeMillis()
            dateOfDischarge.min = deliveryDateMillis
            // Calculate and display initial gestational age
            updateGestationalAge(System.currentTimeMillis())
            
            // Initialize mother condition fields
            motherCondition.value = null
            motherCurrentlyAdmitted.value = null
            maternalComplications.value = null
            dateOfDischarge.value = null
            
            // Initialize new outcome fields
            stillBirth.value = "0"
            deliveryOutcome.value = null
            liveBirth.value = null
            timeOfDischarge.value = null

            // Add mother condition fields for new records (both halves of form)
            formElements.add(motherCondition)
            formElements.add(motherCurrentlyAdmitted)

            // Add delivery outcome fields
            formElements.add(deliveryOutcome)
            formElements.add(liveBirth)
            formElements.add(stillBirth)
        } else {
            // Load saved values (dd/MM/yyyy per JIRA)
            dateOfDelivery.value = saved.dateOfDelivery?.let { getDateFromLong(it, DATE_FORMAT_DD_MM_YYYY) }
            deliveryDateMillis = saved.dateOfDelivery ?: System.currentTimeMillis()
            dateOfDischarge.min = deliveryDateMillis
            dateOfDischarge.max = System.currentTimeMillis()
            
            timeOfDelivery.value = saved.timeOfDelivery
            placeOfDelivery.value = getLocalValueInArray(
                R.array.do_place_of_delivery_array,
                saved.placeOfDelivery
            )
            deliveryConductedBy.value = getLocalValueInArray(
                R.array.do_delivery_conducted_by_array,
                saved.deliveryConductedBy
            )
            modeOfDelivery.value = getLocalValueInArray(
                R.array.do_mode_of_delivery_array,
                saved.modeOfDelivery
            )
            indicationForLSCS.value = saved.indicationForLSCS
            indicationForLSCSOther.value = saved.indicationForLSCSOther
            privateHospitalName.value = saved.privateHospitalName

            // Load mother condition fields
            motherCondition.value = saved.motherCondition
            motherCurrentlyAdmitted.value = when (saved.motherCurrentlyAdmitted) {
                true -> resources.getStringArray(R.array.do_mother_admitted_array)[0]
                false -> resources.getStringArray(R.array.do_mother_admitted_array)[1]
                null -> null
            }
            maternalComplications.value = saved.maternalComplications
            dateOfDischarge.value = saved.dateOfDischarge?.let { getDateFromLong(it) }
            timeOfDischarge.value = saved.timeOfDischarge
            deliveryOutcome.value = saved.deliveryOutcome?.toString()
            liveBirth.value = saved.liveBirth?.toString()
            stillBirth.value = saved.stillBirth?.toString()

            // Update gestational age for saved date
            saved.dateOfDelivery?.let { updateGestationalAge(it) }

            // Handle conditional fields based on saved values
            handlePlaceOfDeliveryChange(placeOfDelivery.value)
            handleModeOfDeliveryChange(modeOfDelivery.value)
            handleDeliveryConductedByChange(deliveryConductedBy.value)
            
            // Add mother condition fields (second half of form)
            formElements.add(motherCondition)
            
            // Add maternal complications if mother condition is "Complication" or "Critical"
            val conditionIndex = resources.getStringArray(R.array.do_mother_condition_array).indexOf(motherCondition.value)
            if (conditionIndex == 1 || conditionIndex == 2) {
                formElements.add(maternalComplications)
            }
            
            // Always add admission status field
            formElements.add(motherCurrentlyAdmitted)
            
            // Add discharge date if mother is discharged (not currently admitted)
            if (motherCurrentlyAdmitted.value == resources.getStringArray(R.array.do_mother_admitted_array)[1]) {
                formElements.add(dateOfDischarge)
                formElements.add(timeOfDischarge)
            }

            // Add delivery outcome fields at the end
            formElements.add(deliveryOutcome)
            formElements.add(liveBirth)
            formElements.add(stillBirth)
        }

        setUpPage(formElements)
    }

    /**
     * Alternative setUpPage for simpler use case (backward compatibility)
     */
    suspend fun setUpPage(deliveryDateMillis: Long, saved: DeliveryOutcomeCache?, isDelivered: Boolean = false) {
        this.deliveryDateMillis = deliveryDateMillis
        dateOfDischarge.min = deliveryDateMillis
        dateOfDischarge.max = System.currentTimeMillis()
        dateOfDelivery.max = System.currentTimeMillis()

        val list = mutableListOf<FormElement>(
            dateOfDelivery,
            motherCondition,
            motherCurrentlyAdmitted
        )

        if (saved != null) {
            dateOfDelivery.value = saved.dateOfDelivery?.let { getDateFromLong(it) }
            this.deliveryDateMillis = saved.dateOfDelivery ?: deliveryDateMillis
            dateOfDelivery.min = this.deliveryDateMillis
            dateOfDischarge.min = this.deliveryDateMillis
            
            motherCondition.value = saved.motherCondition
            motherCurrentlyAdmitted.value = when (saved.motherCurrentlyAdmitted) {
                true -> resources.getStringArray(R.array.do_mother_admitted_array)[0]
                false -> resources.getStringArray(R.array.do_mother_admitted_array)[1]
                null -> null
            }
            maternalComplications.value = saved.maternalComplications
            dateOfDischarge.value = saved.dateOfDischarge?.let { getDateFromLong(it) }

            val conditionIndex = resources.getStringArray(R.array.do_mother_condition_array).indexOf(motherCondition.value)
            if (conditionIndex == 1 || conditionIndex == 2) {
                list.add(list.indexOf(motherCondition) + 1, maternalComplications)
            }
            if (motherCurrentlyAdmitted.value == resources.getStringArray(R.array.do_mother_admitted_array)[1]) {
                list.add(list.indexOf(motherCurrentlyAdmitted) + 1, dateOfDischarge)
            }
        } else {
            dateOfDelivery.value = getDateFromLong(deliveryDateMillis)
            motherCondition.value = null
            motherCurrentlyAdmitted.value = null
            maternalComplications.value = null
            dateOfDischarge.value = null
        }

        setUpPage(list)
    }

    /**
     * Calculate gestational age in weeks and days format (e.g., "38w 5d")
     */
    private fun calculateGestationalAge(deliveryDate: Long): String {
        if (lmpDate <= 0) return "NA"
        
        val daysDiff = TimeUnit.MILLISECONDS.toDays(deliveryDate - lmpDate)
        val weeks = (daysDiff / DAYS_IN_WEEK).toInt()
        val days = (daysDiff % DAYS_IN_WEEK).toInt()
        
        return "${weeks}w ${days}d"
    }

    /**
     * Get gestational age classification for alerts
     */
    private fun getGestationalAgeClassification(weeks: Int): String? {
        return when {
            weeks < EXTREMELY_PRETERM_WEEKS -> resources.getString(R.string.do_alert_extremely_preterm)
            weeks < VERY_PRETERM_WEEKS -> resources.getString(R.string.do_alert_very_preterm)
            weeks < PRETERM_THRESHOLD_WEEKS -> resources.getString(R.string.do_alert_moderate_preterm)
            weeks < POST_TERM_WEEKS -> resources.getString(R.string.do_alert_term)
            else -> resources.getString(R.string.do_alert_post_term)
        }
    }

    /**
     * Update gestational age display and show classification alert
     */
    private suspend fun updateGestationalAge(deliveryDate: Long) {
        val gestationalAgeStr = calculateGestationalAge(deliveryDate)
        gestationalAgeAtDelivery.value = gestationalAgeStr

        if (lmpDate > 0) {
            val weeks = TimeUnit.MILLISECONDS.toDays(deliveryDate - lmpDate).toInt() / DAYS_IN_WEEK
            
            // Show gestational age classification alert in dialog
            when {
                weeks < EXTREMELY_PRETERM_WEEKS -> emitAlertErrorMessage(R.string.do_alert_extremely_preterm)
                weeks < VERY_PRETERM_WEEKS -> emitAlertErrorMessage(R.string.do_alert_very_preterm)
                weeks < PRETERM_THRESHOLD_WEEKS -> emitAlertErrorMessage(R.string.do_alert_moderate_preterm)
                weeks >= POST_TERM_WEEKS -> emitAlertErrorMessage(R.string.do_alert_post_term)
                // Term (37-42 weeks) - no alert needed, it's normal
            }
        }
    }

    /**
     * Validate date of delivery against EDD constraints
     */
    private suspend fun validateDeliveryDate(): Boolean {
        val deliveryDateLong = dateOfDelivery.value?.let { getLongFromDate(it, DATE_FORMAT_DD_MM_YYYY) } ?: return false

        // Check if future date
        if (deliveryDateLong > System.currentTimeMillis()) {
            dateOfDelivery.errorText = resources.getString(R.string.do_error_future_date)
            return false
        }

        // Check EDD ± 4 weeks constraint
        val minAllowed = eddDate - TimeUnit.DAYS.toMillis(EDD_TOLERANCE_WEEKS_BEFORE * DAYS_IN_WEEK.toLong())
        val maxAllowed = eddDate + TimeUnit.DAYS.toMillis(EDD_TOLERANCE_WEEKS_AFTER * DAYS_IN_WEEK.toLong())

        if (deliveryDateLong < minAllowed || deliveryDateLong > maxAllowed) {
            val minDateStr = getDateFromLong(minAllowed, DATE_FORMAT_DD_MM_YYYY) ?: ""
            val maxDateStr = getDateFromLong(maxAllowed, DATE_FORMAT_DD_MM_YYYY) ?: ""
            dateOfDelivery.errorText = resources.getString(
                R.string.do_error_date_range,
                minDateStr,
                maxDateStr
            )
            return false
        }

        // If >14 days from EDD → Show alert: "Post-term delivery" (per JIRA)
        val daysFromEDD = TimeUnit.MILLISECONDS.toDays(deliveryDateLong - eddDate).toInt()
        if (daysFromEDD > POST_TERM_THRESHOLD_DAYS) {
            emitAlertErrorMessage(R.string.do_alert_post_term_delivery)
        }

        // Check for preterm delivery (<37 weeks)
        val weeks = TimeUnit.MILLISECONDS.toDays(deliveryDateLong - lmpDate).toInt() / DAYS_IN_WEEK
        if (weeks < PRETERM_THRESHOLD_WEEKS) {
            emitAlertErrorMessage(R.string.do_alert_preterm_delivery)
        }

        dateOfDelivery.errorText = null
        return true
    }

    /**
     * Handle place of delivery change - show alerts and enable/disable hospital name
     */
    private suspend fun handlePlaceOfDeliveryChange(selectedValue: String?) {
        val placeArray = resources.getStringArray(R.array.do_place_of_delivery_array)
        when (selectedValue) {
            placeArray[PLACE_PRIVATE_HOSPITAL_INDEX] -> { // "Private Hospital"
                val index = getIndexOfElement(placeOfDelivery)
                if (index != -1 && getIndexOfElement(privateHospitalName) == -1) {
                    triggerDependants(
                        source = placeOfDelivery,
                        removeItems = emptyList(),
                        addItems = listOf(privateHospitalName),
                        position = index + 1
                    )
                }
                privateHospitalName.isEnabled = true
            }
            placeArray[PLACE_HOME_DELIVERY_INDEX] -> { // "Home delivery"
                if (getIndexOfElement(privateHospitalName) != -1) {
                    triggerDependants(
                        source = placeOfDelivery,
                        removeItems = listOf(privateHospitalName),
                        addItems = emptyList()
                    )
                    privateHospitalName.value = null
                }
                emitAlertErrorMessage(R.string.do_alert_home_delivery)
            }
            placeArray[PLACE_ON_THE_WAY_INDEX] -> { // "On the way to facility"
                if (getIndexOfElement(privateHospitalName) != -1) {
                    triggerDependants(
                        source = placeOfDelivery,
                        removeItems = listOf(privateHospitalName),
                        addItems = emptyList()
                    )
                    privateHospitalName.value = null
                }
                emitAlertErrorMessage(R.string.do_alert_on_the_way)
            }
            else -> {
                if (getIndexOfElement(privateHospitalName) != -1) {
                    triggerDependants(
                        source = placeOfDelivery,
                        removeItems = listOf(privateHospitalName),
                        addItems = emptyList()
                    )
                    privateHospitalName.value = null
                }
            }
        }
    }

    /**
     * Handle mode of delivery change - enable/disable indication field
     */
    private suspend fun handleModeOfDeliveryChange(selectedValue: String?) {
        val modeArray = resources.getStringArray(R.array.do_mode_of_delivery_array)
        val requiresIndication = selectedValue == modeArray[MODE_ASSISTED_INDEX] || // "Vacuum/ Forceps Assisted"
                selectedValue == modeArray[MODE_LSCS_INDEX] || // "Lower Segment Cesarean Section (LSCS)"
                selectedValue == modeArray[MODE_EMERGENCY_LSCS_INDEX] // "Emergency LSCS"

        if (requiresIndication) {
            indicationForLSCS.required = true
            val index = getIndexOfElement(modeOfDelivery)
            if (index != -1 && getIndexOfElement(indicationForLSCS) == -1) {
                triggerDependants(
                    source = modeOfDelivery,
                    removeItems = emptyList(),
                    addItems = listOf(indicationForLSCS),
                    position = index + 1
                )
            }
        } else {
            indicationForLSCS.required = false
            val removeItems = mutableListOf<FormElement>()
            if (getIndexOfElement(indicationForLSCS) != -1) {
                removeItems.add(indicationForLSCS)
                indicationForLSCS.value = null
                indicationForLSCS.errorText = null
            }
            if (getIndexOfElement(indicationForLSCSOther) != -1) {
                removeItems.add(indicationForLSCSOther)
                indicationForLSCSOther.value = null
            }
            if (removeItems.isNotEmpty()) {
                triggerDependants(
                    source = modeOfDelivery,
                    removeItems = removeItems,
                    addItems = emptyList()
                )
            }
        }
    }

    /** Indices in do_delivery_conducted_by_array for unskilled delivery (Dai/TBA, Family member, Self/Unassisted). */
    private val unskilledDeliveryConductedByIndices = setOf(
        CONDUCTED_BY_DAI_TBA_INDEX,
        CONDUCTED_BY_FAMILY_MEMBER_INDEX,
        CONDUCTED_BY_SELF_INDEX
    )

    /**
     * Handle delivery conducted by change - show alert for unskilled delivery.
     * Prefers actual value (deliveryConductedBy.value set by adapter) for reliability;
     * index/selectedValue used as fallback (e.g. when loading saved record).
     */
    private suspend fun handleDeliveryConductedByChange(selectedValue: String?, selectedIndex: Int? = null) {
        val valueToCheck = deliveryConductedBy.value?.trim() ?: selectedValue?.trim()
        val shouldAlert = when {
            valueToCheck != null -> {
                val conductedByArray = resources.getStringArray(R.array.do_delivery_conducted_by_array)
                val unskilledOptions = listOf(
                    conductedByArray[CONDUCTED_BY_DAI_TBA_INDEX], // "Dai/TBA (Traditional Birth Attendant)"
                    conductedByArray[CONDUCTED_BY_FAMILY_MEMBER_INDEX], // "Family member"
                    conductedByArray[CONDUCTED_BY_SELF_INDEX]  // "Self/Unassisted"
                )
                valueToCheck in unskilledOptions.map { it.trim() }
            }
            selectedIndex != null -> selectedIndex in unskilledDeliveryConductedByIndices
            else -> false
        }
        if (shouldAlert) {
            emitAlertErrorMessage(R.string.do_alert_unskilled_delivery)
        }
    }

    /**
     * Handle indication for LSCS change - enable/disable "Other" text field
     */
    private suspend fun handleIndicationForLSCSChange() {
        val indicationArray = resources.getStringArray(R.array.do_indication_for_lscs_array)
        val otherOption = indicationArray[indicationArray.size - 1] // "Other (specify)"

        if (indicationForLSCS.value?.contains(otherOption) == true) {
            indicationForLSCSOther.required = true
            val index = getIndexOfElement(indicationForLSCS)
            if (index != -1 && getIndexOfElement(indicationForLSCSOther) == -1) {
                triggerDependants(
                    source = indicationForLSCS,
                    removeItems = emptyList(),
                    addItems = listOf(indicationForLSCSOther),
                    position = index + 1
                )
            }
        } else {
            indicationForLSCSOther.required = false
            if (getIndexOfElement(indicationForLSCSOther) != -1) {
                triggerDependants(
                    source = indicationForLSCS,
                    removeItems = listOf(indicationForLSCSOther),
                    addItems = emptyList()
                )
                indicationForLSCSOther.value = null
                indicationForLSCSOther.errorText = null
            }
        }
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            dateOfDelivery.id -> handleDateOfDeliveryChange()
            timeOfDelivery.id -> -1
            placeOfDelivery.id -> {
                val selectedValue = placeOfDelivery.entries?.getOrNull(index)
                placeOfDelivery.value = selectedValue
                handlePlaceOfDeliveryChange(selectedValue)
                getIndexOfElement(placeOfDelivery)
            }
            deliveryConductedBy.id -> {
                val selectedValue = deliveryConductedBy.entries?.getOrNull(index)
                deliveryConductedBy.value = selectedValue
                handleDeliveryConductedByChange(selectedValue, index)
                -1
            }
            modeOfDelivery.id -> {
                val selectedValue = modeOfDelivery.entries?.getOrNull(index)
                modeOfDelivery.value = selectedValue
                handleModeOfDeliveryChange(selectedValue)
                getIndexOfElement(modeOfDelivery)
            }
            indicationForLSCS.id -> handleIndicationForLSCSValidation()
            indicationForLSCSOther.id -> handleIndicationForLSCSOtherValidation()
            privateHospitalName.id -> -1
            motherCondition.id -> handleMotherConditionChange(index)
            motherCurrentlyAdmitted.id -> handleMotherAdmittedChange(index)
            dateOfDischarge.id -> handleDateOfDischargeValidation()
            maternalComplications.id -> handleMaternalComplicationsValidation()
            deliveryOutcome.id -> validateDeliveryOutcome(deliveryOutcome)
            liveBirth.id -> validateDeliveryOutcome(liveBirth)
            stillBirth.id -> validateDeliveryOutcome(stillBirth)
            else -> -1
        }
    }

    private suspend fun handleDateOfDeliveryChange(): Int {
        if (validateDeliveryDate()) {
            getLongFromDate(dateOfDelivery.value, DATE_FORMAT_DD_MM_YYYY)?.let {
                updateGestationalAge(it)
                deliveryDateMillis = it
                dateOfDischarge.min = it
                // Re-validate discharge date: if set and now before new delivery date, show error
                dateOfDischarge.value?.let { dischargeStr ->
                    val dischargeLong = getLongFromDate(dischargeStr)
                    dateOfDischarge.errorText = if (dischargeLong != 0L && dischargeLong < deliveryDateMillis) {
                        resources.getString(R.string.do_discharge_date_before_delivery)
                    } else null
                }
            }
        }
        return -1
    }

    private suspend fun handleIndicationForLSCSValidation(): Int {
        handleIndicationForLSCSChange()
        // Validate indication is selected if required
        if (indicationForLSCS.required && indicationForLSCS.value.isNullOrBlank()) {
            indicationForLSCS.errorText = resources.getString(R.string.do_error_indication_required)
        } else {
            indicationForLSCS.errorText = null
        }
        return getIndexOfElement(indicationForLSCS)
    }

    private fun handleIndicationForLSCSOtherValidation(): Int {
        val value = indicationForLSCSOther.value?.trim()
        if (indicationForLSCSOther.required && value.isNullOrBlank()) {
            indicationForLSCSOther.errorText = resources.getString(R.string.do_error_indication_other_required)
        } else {
            indicationForLSCSOther.errorText = null
        }
        return -1
    }

    private suspend fun handleMotherConditionChange(index: Int): Int {
        maternalComplications.value = null
        maternalComplications.errorText = null
        return when (index) {
            0, 3 -> { // Healthy/Stable or Maternal Death
                triggerDependants(
                    source = motherCondition,
                    removeItems = listOf(maternalComplications),
                    addItems = emptyList()
                )
                getIndexOfElement(motherCondition)
            }
            1, 2 -> { // Complication or Critical
                val currentIndex = getIndexOfElement(motherCondition)
                triggerDependants(
                    source = motherCondition,
                    removeItems = emptyList(),
                    addItems = listOf(maternalComplications),
                    position = if (currentIndex != -1) currentIndex + 1 else -1
                )
                getIndexOfElement(motherCondition)
            }
            else -> -1
        }
    }

    private suspend fun handleMotherAdmittedChange(index: Int): Int {
        dateOfDischarge.value = null
        dateOfDischarge.errorText = null
        val result = triggerDependants(
            source = motherCurrentlyAdmitted,
            passedIndex = index,
            triggerIndex = 1, // Index 1 = "No (Discharged)"
            target = dateOfDischarge
        )
        return result
    }

    private fun handleDateOfDischargeValidation(): Int {
        dateOfDischarge.value?.let { dateStr ->
            val dischargeLong = getLongFromDate(dateStr)
            dateOfDischarge.errorText = if (dischargeLong != 0L && dischargeLong < deliveryDateMillis) {
                resources.getString(R.string.do_discharge_date_before_delivery)
            } else null
        } ?: run { dateOfDischarge.errorText = null }
        return -1
    }

    private fun handleMaternalComplicationsValidation(): Int {
        if (!maternalComplications.value.isNullOrBlank()) {
            maternalComplications.errorText = null
        }
        return -1
    }

    /**
     * Validate delivery outcome fields: ensures deliveryOutcome = liveBirth + stillBirth
     */
    private fun validateDeliveryOutcome(formElement: FormElement): Int {
        formElement.errorText = formElement.value?.takeIf { it.isNotEmpty() }?.toLongOrNull()?.let {
            formElement.min?.let { min ->
                formElement.max?.let { max ->
                    if (it < min) {
                        resources.getString(
                            R.string.form_input_min_limit_error, formElement.title, min
                        )
                    } else if (it > max) {
                        resources.getString(
                            R.string.form_input_max_limit_error, formElement.title, max
                        )
                    } else null
                }
            }
        }

        if (!liveBirth.value.isNullOrEmpty() && !stillBirth.value.isNullOrEmpty() &&
            !deliveryOutcome.value.isNullOrEmpty() && formElement.errorText.isNullOrEmpty()
        ) {
            if (deliveryOutcome.value!!.toInt() != liveBirth.value!!.toInt() + stillBirth.value!!.toInt()) {
                formElement.errorText =
                    "Outcome of Delivery should be equal to sum of Live and Still births"
            } else {
                deliveryOutcome.errorText = null
                liveBirth.errorText = null
                stillBirth.errorText = null
            }
        }

        if (!deliveryOutcome.value.isNullOrEmpty()) {
            stillBirth.max = deliveryOutcome.value?.toLongOrNull()
            liveBirth.max = deliveryOutcome.value?.toLongOrNull()
        }
        return -1
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        val form = cacheModel as DeliveryOutcomeCache
        val admittedYes = resources.getStringArray(R.array.do_mother_admitted_array)[0]
        val conditionMaternalDeath = resources.getStringArray(R.array.do_mother_condition_array)[3]
        
        // Map delivery details fields
        form.dateOfDelivery = getLongFromDate(dateOfDelivery.value, DATE_FORMAT_DD_MM_YYYY)?.takeIf { it != 0L } ?: deliveryDateMillis
        form.timeOfDelivery = timeOfDelivery.value
        form.placeOfDelivery = placeOfDelivery.value
        form.modeOfDelivery = modeOfDelivery.value
        form.deliveryConductedBy = deliveryConductedBy.value
        form.gestationalAgeAtDelivery = gestationalAgeAtDelivery.value
        form.indicationForLSCS = indicationForLSCS.value
        form.indicationForLSCSOther = indicationForLSCSOther.value
        form.privateHospitalName = privateHospitalName.value
        
        // Map mother condition fields
        form.motherCondition = motherCondition.value
        form.maternalComplications = maternalComplications.value?.takeIf { s -> s.isNotBlank() }
        form.motherCurrentlyAdmitted = motherCurrentlyAdmitted.value == admittedYes
        form.dateOfDischarge = dateOfDischarge.value?.let { getLongFromDate(it) }.takeIf { it != 0L }
        form.timeOfDischarge = timeOfDischarge.value
        form.deliveryOutcome = deliveryOutcome.value?.toIntOrNull()
        form.liveBirth = liveBirth.value?.toIntOrNull()
        form.stillBirth = stillBirth.value?.toIntOrNull()
        form.isDeath = motherCondition.value == conditionMaternalDeath
        if (form.isDeath == true) {
            form.isDeathValue = "Maternal Death"
        }
    }

    /** Index of Maternal Complications element (for showing intensive PNC / hysterectomy alerts). */
    fun getIndexOfMaternalComplications() = getIndexById(maternalComplications.id)

    /** True if Mother's Condition is Complication (specify) or Critical/ICU. */
    fun isComplicationOrCritical(): Boolean {
        val v = motherCondition.value ?: return false
        val arr = resources.getStringArray(R.array.do_mother_condition_array)
        return v == arr.getOrNull(1) || v == arr.getOrNull(2)
    }

    /** True if Mother's Condition is Maternal Death. */
    fun isMaternalDeath(): Boolean {
        val v = motherCondition.value ?: return false
        return v == resources.getStringArray(R.array.do_mother_condition_array).getOrNull(3)
    }

    /** True if selected complications include PPH or Uterine rupture (intensive PNC alert). */
    fun hasPphOrUterineRupture(): Boolean {
        val raw = maternalComplications.value ?: return false
        val pph = resources.getStringArray(R.array.do_maternal_complications_array).getOrNull(0) ?: "Post-Partum Hemorrhage (PPH)"
        val uterine = resources.getStringArray(R.array.do_maternal_complications_array).getOrNull(4) ?: "Uterine rupture"
        return raw.contains(pph) || raw.contains(uterine)
    }

    /** True if Hysterectomy performed is selected (family planning / EC update). */
    fun hasHysterectomy(): Boolean {
        val raw = maternalComplications.value ?: return false
        val hyst = resources.getStringArray(R.array.do_maternal_complications_array).getOrNull(8) ?: "Hysterectomy performed"
        return raw.contains(hyst)
    }
}
