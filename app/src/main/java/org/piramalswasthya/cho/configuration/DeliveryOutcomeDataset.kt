package org.piramalswasthya.cho.configuration

import android.content.Context
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

    // Store EDD and LMP for calculations
    private var eddDate: Long = 0L
    private var lmpDate: Long = 0L
    private var patientCaseId: String? = null

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
            // Calculate and display initial gestational age
            updateGestationalAge(System.currentTimeMillis())
        } else {
            // Load saved values (dd/MM/yyyy per JIRA)
            dateOfDelivery.value = saved.dateOfDelivery?.let { getDateFromLong(it, DATE_FORMAT_DD_MM_YYYY) }
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

            // Update gestational age for saved date
            saved.dateOfDelivery?.let { updateGestationalAge(it) }

            // Handle conditional fields based on saved values
            handlePlaceOfDeliveryChange(placeOfDelivery.value)
            handleModeOfDeliveryChange(modeOfDelivery.value)
            handleDeliveryConductedByChange(deliveryConductedBy.value)
        }

        setUpPage(formElements)
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
            val classification = getGestationalAgeClassification(weeks)
            classification?.let {
                gestationalAgeAtDelivery.errorText = it
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
            dateOfDelivery.id -> {
                if (validateDeliveryDate()) {
                    updateGestationalAge(getLongFromDate(dateOfDelivery.value, DATE_FORMAT_DD_MM_YYYY))
                }
                -1
            }

            timeOfDelivery.id -> {
                -1
            }

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

            indicationForLSCS.id -> {
                handleIndicationForLSCSChange()
                // Validate indication is selected if required
                if (indicationForLSCS.required && indicationForLSCS.value.isNullOrBlank()) {
                    indicationForLSCS.errorText = resources.getString(R.string.do_error_indication_required)
                } else {
                    indicationForLSCS.errorText = null
                }
                getIndexOfElement(indicationForLSCS)
            }

            indicationForLSCSOther.id -> {
                val value = indicationForLSCSOther.value?.trim()
                if (indicationForLSCSOther.required && value.isNullOrBlank()) {
                    indicationForLSCSOther.errorText = resources.getString(R.string.do_error_indication_other_required)
                } else {
                    indicationForLSCSOther.errorText = null
                }
                -1
            }

            privateHospitalName.id -> {
                -1
            }

            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as? DeliveryOutcomeCache)?.let { form ->
            form.dateOfDelivery = getLongFromDate(dateOfDelivery.value, DATE_FORMAT_DD_MM_YYYY)
            form.timeOfDelivery = timeOfDelivery.value
            form.placeOfDelivery = placeOfDelivery.value
            form.modeOfDelivery = modeOfDelivery.value
            form.deliveryConductedBy = deliveryConductedBy.value
            form.gestationalAgeAtDelivery = gestationalAgeAtDelivery.value
            form.indicationForLSCS = indicationForLSCS.value
            form.indicationForLSCSOther = indicationForLSCSOther.value
            form.privateHospitalName = privateHospitalName.value
        } ?: return
    }

}
