package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.model.PregnantWomanRegistrationCache
import timber.log.Timber
import java.util.Calendar
import java.util.concurrent.TimeUnit

class PregnantWomanRegistrationDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    companion object {
        // Test result constants
        const val TEST_RESULT_REACTIVE = "Reactive"
        const val TEST_RESULT_NON_REACTIVE = "Non-Reactive"
        const val TEST_RESULT_TEST_NOT_DONE = "Test Not Done"
        const val TEST_RESULT_POSITIVE = "Positive"
        const val TEST_RESULT_NEGATIVE = "Negative"

        // Test result indices (matching array order)
        private const val TEST_RESULT_INDEX_REACTIVE = 0
        private const val TEST_RESULT_INDEX_NON_REACTIVE = 1
        private const val TEST_RESULT_INDEX_NOT_DONE = 2

        private fun getMinLmpMillis(): Long {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -42 * 7) // Last 42 weeks
            return cal.timeInMillis
        }

        private fun getEddFromLmp(lmpDate: Long): Long {
            return lmpDate + TimeUnit.DAYS.toMillis(280)
        }

        private fun calculateGestationalAge(lmpDate: Long, currentDate: Long): Pair<Int, Int> {
            val days = TimeUnit.MILLISECONDS.toDays(currentDate - lmpDate)
            val weeks = (days / 7).toInt()
            val remainingDays = (days % 7).toInt()
            return Pair(weeks, remainingDays)
        }

        private fun calculateTrimester(weeks: Int): String {
            return when {
                weeks <= 12 -> "First"
                weeks <= 26 -> "Second"
                else -> "Third"
            }
        }

        private fun calculateBMI(weight: Double, height: Double): Double {
            return weight / ((height / 100) * (height / 100))
        }

        private fun getBMICategory(bmi: Double): String {
            return when {
                bmi < 18.5 -> "Underweight"
                bmi < 24.9 -> "Normal"
                bmi < 29.9 -> "Overweight"
                else -> "Obese"
            }
        }
    }

    private var _isFormReadOnly: Boolean = false
    val isFormReadOnly: Boolean
        get() = _isFormReadOnly
    private lateinit var registrationCache: PregnantWomanRegistrationCache
    private var dateOfRegMillis: Long = System.currentTimeMillis()
    private var oneYearBeforeRegMillis: Long = 0L

    var onShowAlert: ((String) -> Unit)? = null
    var onNavigateToEligibleCouple: (() -> Unit)? = null
    var onNavigateToVitalsAndPrescription: (() -> Unit)? = null

    // Date of Registration
    private val dateOfReg = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = "Date of PW Registration",
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = true
    )

    // RCH ID
    private val rchId = FormElement(
        id = 2,
        inputType = InputType.EDIT_TEXT,
        title = "RCH ID No. of Woman",
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        etMaxLength = 12
    )

    // Pregnancy Test Flow
    private val pregnancyTestAtFacility = FormElement(
        id = 3,
        inputType = InputType.RADIO,
        title = "Is the pregnancy test conducted at facility?",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val uptResult = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = "Result of UPT",
        entries = arrayOf("Positive", "Negative"),
        required = false,
        hasDependants = true
    )

    // Pregnancy Details
    private val lmp = FormElement(
        id = 5,
        inputType = InputType.DATE_PICKER,
        title = "LMP",
        required = false,
        hasDependants = true,
        max = System.currentTimeMillis(),
        min = getMinLmpMillis()
    )

    private val edd = FormElement(
        id = 6,
        inputType = InputType.TEXT_VIEW,
        title = "EDD",
        required = false
    )

    private val gestationalAge = FormElement(
        id = 7,
        inputType = InputType.TEXT_VIEW,
        title = "Gestational Age (Weeks)",
        required = false,
    )

    private val trimester = FormElement(
        id = 8,
        inputType = InputType.TEXT_VIEW,
        title = "Trimester",
        required = false,
    )

    // Blood Group
    private val bloodGroup = FormElement(
        id = 9,
        inputType = InputType.DROPDOWN,
        title = "Blood Group",
        entries = arrayOf(
            "A +ve",
            "B +ve",
            "AB +ve",
            "O +ve",
            "A -ve",
            "B -ve",
            "AB -ve",
            "O -ve"
        ),
        required = true
    )

    // Gravida & Para
    private val gravida = FormElement(
        id = 10,
        inputType = InputType.EDIT_TEXT,
        title = "Gravida",
        required = true,
        min = 1,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        etMaxLength = 2,
        hasDependants = true
    )

    private val para = FormElement(
        id = 11,
        inputType = InputType.EDIT_TEXT,
        title = "Para",
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        etMaxLength = 2
    )

    // History Fields
    private val historyOfAbortions = FormElement(
        id = 12,
        inputType = InputType.RADIO,
        title = "History of abortions",
        entries = arrayOf("Yes", "No"),
        required = false,
        isEnabled = false,
        hasDependants = false
    )

    private val previousLSCS = FormElement(
        id = 13,
        inputType = InputType.RADIO,
        title = "History of previous LSCS",
        entries = arrayOf("Yes", "No"),
        required = false,
        hasDependants = true
    )

    private val complicationsInPreviousPregnancy = FormElement(
        id = 14,
        inputType = InputType.CHECKBOXES,
        title = "Any complications in previous pregnancy",
        entries = arrayOf(
            "None",
            "Gestational Diabetes",
            "Pre-eclampsia",
            "Eclampsia",
            "Hemorrhage",
            "Preterm Birth",
            "Stillbirth"
        ),
        required = true,
        hasDependants = true
    )

    // Anthropometry
    private val height = FormElement(
        id = 15,
        inputType = InputType.EDIT_TEXT,
        title = "Height (cm)",
        required = true,
        min = 100,
        max = 220,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        etMaxLength = 3,
        hasDependants = true
    )

    private val weight = FormElement(
        id = 16,
        inputType = InputType.EDIT_TEXT,
        title = "Weight (Kgs)",
        required = true,
        min = 30,
        max = 150,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL,
        etMaxLength = 6,
        hasDependants = true
    )

    private val bmi = FormElement(
        id = 17,
        inputType = InputType.TEXT_VIEW,
        title = "BMI",
        required = false,
    )

    // Pre-existing Conditions
    private val preExistingConditions = FormElement(
        id = 18,
        inputType = InputType.CHECKBOXES,
        title = "Pre-existing conditions",
        entries = arrayOf(
            "None",
            "Hypertension",
            "Diabetes Mellitus",
            "Thyroid",
            "Heart disease",
            "Epilepsy",
            "Tuberculosis",
            "HIV",
            "Sexually transmitted Infections",
            "Severe Malnutrition",
            "Kidney disease",
            "Auto Immune disorders"
        ),
        required = true,
        hasDependants = true
    )

    // Lab Investigations - VDRL/RPR
    private val vdrlRprResult = FormElement(
        id = 19,
        inputType = InputType.DROPDOWN,
        title = "VDRL/RPR Test result",
        entries = arrayOf(TEST_RESULT_REACTIVE, TEST_RESULT_NON_REACTIVE, TEST_RESULT_TEST_NOT_DONE),
        required = true,
        hasDependants = true
    )

    private val vdrlRprDate = FormElement(
        id = 20,
        inputType = InputType.DATE_PICKER,
        title = "Date of VDRL/RPR Test done",
        required = false,
        max = System.currentTimeMillis(),

        )

    // Lab Investigations - HIV
    private val hivResult = FormElement(
        id = 21,
        inputType = InputType.DROPDOWN,
        title = "HIV Test result",
        entries = arrayOf(TEST_RESULT_REACTIVE, TEST_RESULT_NON_REACTIVE, TEST_RESULT_TEST_NOT_DONE),
        required = true,
        hasDependants = true
    )

    private val hivTestDate = FormElement(
        id = 22,
        inputType = InputType.DATE_PICKER,
        title = "Date of HIV Test done",
        required = false,
        max = System.currentTimeMillis(),
    )

    // Lab Investigations - HBsAg
    private val hbsAgResult = FormElement(
        id = 23,
        inputType = InputType.DROPDOWN,
        title = "HBsAg Test result",
        entries = arrayOf(TEST_RESULT_POSITIVE, TEST_RESULT_NEGATIVE, TEST_RESULT_TEST_NOT_DONE),
        required = true,
        hasDependants = true
    )

    private val hbsAgTestDate = FormElement(
        id = 24,
        inputType = InputType.DATE_PICKER,
        title = "Date of HBsAg Test done",
        required = false,
        max = System.currentTimeMillis(),
    )

    // High-Risk Pregnancy Flag
    private val isHighRiskPregnancy = FormElement(
        id = 25,
        inputType = InputType.RADIO,
        title = "High-Risk Conditions Present?",
        entries = arrayOf("Yes", "No"),
        required = false,
        isEnabled = false // Auto-calculated
    )

    suspend fun setUpPage(
        ben: PatientDisplay?,
        savedRecord: PregnantWomanRegistrationCache?
    ) {
        this.registrationCache = savedRecord ?: createDefaultRegistrationCache(ben)
        _isFormReadOnly = savedRecord?.isFirstAncSubmitted == true

        // Initialize date of registration
        dateOfRegMillis = savedRecord?.dateOfRegistration ?: System.currentTimeMillis()
        oneYearBeforeRegMillis = dateOfRegMillis - TimeUnit.DAYS.toMillis(365)

        val list = mutableListOf<FormElement>()

        // Always add Date of Registration first
        list.add(dateOfReg)
        list.add(rchId)

        // Build form based on read-only or editable mode
        if (isFormReadOnly) {
            savedRecord?.let { buildReadOnlyFormList(it, list) }
        } else {
            buildEditableFormList(savedRecord, list)
        }

        setUpPage(list)
    }

    /**
     * Build form list for read-only mode
     */
    private suspend fun buildReadOnlyFormList(
        cache: PregnantWomanRegistrationCache,
        list: MutableList<FormElement>
    ) {
        populateFormFromCache(cache)

        // Add all main fields
        list.addAll(listOf(
            pregnancyTestAtFacility,
            uptResult,
            lmp, edd, gestationalAge, trimester,
            bloodGroup,
            gravida, para
        ))

        // Add history fields if gravida > 1
        addHistoryFieldsIfNeeded(cache, list)

        // Add anthropometry, pre-existing conditions, and lab fields
        list.addAll(listOf(
            height, weight, bmi,
            preExistingConditions,
            vdrlRprResult,
            hivResult,
            hbsAgResult,
            isHighRiskPregnancy
        ))

        // Add test date fields conditionally
        addTestDateFieldsIfNeeded(cache, list)

        // Make all fields read-only
        makeFormReadOnly(list)
    }

    /**
     * Build form list for editable mode
     */
    private suspend fun buildEditableFormList(
        savedRecord: PregnantWomanRegistrationCache?,
        list: MutableList<FormElement>
    ) {
        list.add(pregnancyTestAtFacility)

        savedRecord?.let { cache ->
            populateFormFromCache(cache)
            val hasLmpDate = cache.lmpDate > 0

            if (hasLmpDate) {
                list.add(uptResult)
                list.addAll(getBaseRegistrationFields())
                addHistoryFieldsIfNeeded(cache, list)
                addTestDateFieldsIfNeeded(cache, list)
            } else {
                list.add(uptResult)
            }
        }
    }

    /**
     * Add history fields if gravida > 1
     */
    private fun addHistoryFieldsIfNeeded(
        cache: PregnantWomanRegistrationCache,
        list: MutableList<FormElement>
    ) {
        cache.numPrevPregnancy?.let { gravidaValue ->
            if (gravidaValue > 1) {
                list.addAll(listOf(
                    historyOfAbortions,
                    previousLSCS,
                    complicationsInPreviousPregnancy
                ))
            }
        }
    }

    /**
     * Add test date fields if test results indicate they should be shown
     */
    private fun addTestDateFieldsIfNeeded(
        cache: PregnantWomanRegistrationCache,
        list: MutableList<FormElement>
    ) {
        if (shouldShowTestDateField(cache.vdrlRprTestResult, isHbsAg = false)) {
            list.add(vdrlRprDate)
        }
        if (shouldShowTestDateField(cache.hivTestResult, isHbsAg = false)) {
            list.add(hivTestDate)
        }
        if (shouldShowTestDateField(cache.hbsAgTestResult, isHbsAg = true)) {
            list.add(hbsAgTestDate)
        }
    }

    /**
     * Check if test date field should be shown based on test result
     */
    private fun shouldShowTestDateField(testResult: String?, isHbsAg: Boolean): Boolean {
        return when {
            testResult == null -> false
            isHbsAg -> testResult == TEST_RESULT_POSITIVE || testResult == TEST_RESULT_NEGATIVE
            else -> testResult == TEST_RESULT_REACTIVE || testResult == TEST_RESULT_NON_REACTIVE
        }
    }

    private fun getBaseRegistrationFields(): List<FormElement> {
        return listOf(
            lmp, edd, gestationalAge, trimester,
            bloodGroup,
            gravida, para,
            height, weight, bmi,
            preExistingConditions,
            vdrlRprResult,
            hivResult,
            hbsAgResult,
            isHighRiskPregnancy
        )
    }


    fun createDefaultRegistrationCache(ben: PatientDisplay?): PregnantWomanRegistrationCache {
        return PregnantWomanRegistrationCache(
            patientID = ben?.patient?.patientID ?: "",
            syncState = SyncState.UNSYNCED,
            createdDate = System.currentTimeMillis(),
            updatedDate = System.currentTimeMillis(),
            dateOfRegistration = System.currentTimeMillis(),
            bloodGroupId = 0,
            vdrlRprTestResultId = 0,
            hivTestResultId = 0,
            hbsAgTestResultId = 0,
            complicationPrevPregnancyId = null,
            hrpIdById = 0,
            isFirstAncSubmitted = false,
            id = 0,
            mcpCardNumber = null,
            rchId = null,
            lmpDate = 0L,
            bloodGroup = null,
            weight = null,
            height = null,
            vdrlRprTestResult = null,
            dateOfVdrlRprTest = null,
            historyOfAbortions = null,
            previousLSCS = null,
            hivTestResult = null,
            dateOfHivTest = null,
            hbsAgTestResult = null,
            dateOfHbsAgTest = null,
            pastIllness = null,
            otherPastIllness = null,
            is1st = true,
            numPrevPregnancy = null,
            complicationPrevPregnancy = null,
            otherComplication = null,
            isHrp = false,
            hrpIdBy = null,
            active = true,
            tt1 = null,
            tt2 = null,
            ttBooster = null,
            processed = "N",
            createdBy = "",
            updatedBy = ""
        )
    }

    private fun populateFormFromCache(cache: PregnantWomanRegistrationCache) {
        populateBasicFields(cache)
        populateTestResultFields(cache)
        populateHistoryFieldsIfNeeded(cache)
    }

    /**
     * Populate basic form fields from cache
     */
    private fun populateBasicFields(cache: PregnantWomanRegistrationCache) {
        dateOfReg.value = getDateFromLong(cache.dateOfRegistration)
        rchId.value = cache.rchId?.toString()

        if (cache.lmpDate > 0) {
            lmp.value = getDateFromLong(cache.lmpDate)
            updateCalculatedFields(System.currentTimeMillis())
        }

        bloodGroup.value = cache.bloodGroup
        gravida.value = cache.numPrevPregnancy?.toString()
        para.value = cache.numPrevPregnancy?.toString()
        height.value = cache.height?.toString()
        weight.value = cache.weight?.toString()
        updateBMI()
    }

    /**
     * Populate test result fields and their date fields
     */
    private fun populateTestResultFields(cache: PregnantWomanRegistrationCache) {
        populateVdrlRprFields(cache)
        populateHivFields(cache)
        populateHbsAgFields(cache)
    }

    /**
     * Populate VDRL/RPR test result and date fields
     */
    private fun populateVdrlRprFields(cache: PregnantWomanRegistrationCache) {
        vdrlRprResult.value = cache.vdrlRprTestResult
        if (shouldShowTestDateField(cache.vdrlRprTestResult, isHbsAg = false)) {
            vdrlRprDate.isEnabled = true
            vdrlRprDate.required = true
            vdrlRprDate.value = cache.dateOfVdrlRprTest?.let { getDateFromLong(it) }
        }
    }

    /**
     * Populate HIV test result and date fields
     */
    private fun populateHivFields(cache: PregnantWomanRegistrationCache) {
        hivResult.value = cache.hivTestResult
        if (shouldShowTestDateField(cache.hivTestResult, isHbsAg = false)) {
            hivTestDate.isEnabled = true
            hivTestDate.required = true
            hivTestDate.value = cache.dateOfHivTest?.let { getDateFromLong(it) }
        }
    }

    /**
     * Populate HBsAg test result and date fields
     */
    private fun populateHbsAgFields(cache: PregnantWomanRegistrationCache) {
        hbsAgResult.value = cache.hbsAgTestResult
        if (shouldShowTestDateField(cache.hbsAgTestResult, isHbsAg = true)) {
            hbsAgTestDate.isEnabled = true
            hbsAgTestDate.required = true
            hbsAgTestDate.value = cache.dateOfHbsAgTest?.let { getDateFromLong(it) }
        }
    }

    /**
     * Populate history fields if gravida > 1
     */
    private fun populateHistoryFieldsIfNeeded(cache: PregnantWomanRegistrationCache) {
        cache.numPrevPregnancy?.let { gravidaValue ->
            if (gravidaValue > 1) {
                enableHistoryFields(cache)
                updateHighRiskStatus()
            }
        }
    }

    /**
     * Enable and populate history-related fields
     */
    private fun enableHistoryFields(cache: PregnantWomanRegistrationCache) {
        historyOfAbortions.isEnabled = true
        historyOfAbortions.required = true
        historyOfAbortions.value = if (cache.historyOfAbortions == true) "Yes" else "No"

        previousLSCS.isEnabled = true
        previousLSCS.required = true
        previousLSCS.value = if (cache.previousLSCS == true) "Yes" else "No"

        complicationsInPreviousPregnancy.isEnabled = true
        complicationsInPreviousPregnancy.required = true
        complicationsInPreviousPregnancy.value = cache.complicationPrevPregnancy

        preExistingConditions.value = cache.pastIllness
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            dateOfReg.id -> handleDateOfRegChange()
            pregnancyTestAtFacility.id -> handlePregnancyTestFlow(index)
            uptResult.id -> handleUPTResultFlow(index)
            lmp.id -> handleLmpChange()
            historyOfAbortions.id -> handleHistoryOfAbortionsChange(index)
            gravida.id -> handleGravidaChange()
            para.id -> handleParaChange()
            height.id, weight.id -> handleAnthropometryChange()
            vdrlRprResult.id -> {
                handleVdrlRprResultChange(index)
                return vdrlRprResult.id // Return the ID so Fragment can update UI
            }
            hivResult.id -> {
                handleHivResultChange(index)
                return hivResult.id // Return the ID so Fragment can update UI
            }
            hbsAgResult.id -> {
                handleHbsAgResultChange(index)
                return hbsAgResult.id // Return the ID so Fragment can update UI
            }
            previousLSCS.id -> handlePreviousLSCSChange(index)
            complicationsInPreviousPregnancy.id -> handleComplicationsChange(index)
            preExistingConditions.id -> handlePreExistingConditionsChange(index)
            else -> -1
        }
    }


    private fun handleDateOfRegChange(): Int {
        dateOfReg.value?.let {
            dateOfRegMillis = getLongFromDate(it)
            oneYearBeforeRegMillis = dateOfRegMillis - TimeUnit.DAYS.toMillis(365)

            // Update min dates for test dates (should be >= Date of Registration - 1 year)
            vdrlRprDate.min = oneYearBeforeRegMillis
            hivTestDate.min = oneYearBeforeRegMillis
            hbsAgTestDate.min = oneYearBeforeRegMillis

            // Update max dates for test dates (should be <= today's date)
            val today = System.currentTimeMillis()
            vdrlRprDate.max = today
            hivTestDate.max = today
            hbsAgTestDate.max = today

            // Update LMP max date
            lmp.max = dateOfRegMillis
        }
        return -1
    }

    private fun handlePregnancyTestFlow(index: Int): Int {
        return if (index == 0) { // "Yes" selected
            triggerDependants(
                source = pregnancyTestAtFacility,
                addItems = listOf(uptResult),
                removeItems = emptyList()
            )
        } else { // "No" selected
            onShowAlert?.invoke("Please conduct UPT (Urine Pregnancy Test)")
            triggerDependants(
                source = pregnancyTestAtFacility,
                addItems = listOf(uptResult),
                removeItems = emptyList()
            )
        }
    }

    private fun handleUPTResultFlow(index: Int): Int {
        val baseRegistrationFields = listOf(
            lmp, edd, gestationalAge, trimester, bloodGroup,
            gravida, para, height, weight, bmi,
            preExistingConditions, vdrlRprResult, hivResult, hbsAgResult,
            isHighRiskPregnancy
        )

        return if (index == 0) { // Positive
            // Get current gravida value to decide if history fields should be included
            val gravidaValue = gravida.value?.toIntOrNull() ?: 1

            val registrationFields = mutableListOf<FormElement>().apply {
                addAll(baseRegistrationFields)
                // Only add history fields if gravida > 1
                if (gravidaValue > 1) {
                    add(historyOfAbortions)
                    add(previousLSCS)
                    add(complicationsInPreviousPregnancy)
                }
                // IMPORTANT: DO NOT add date fields here initially
                // They will be added dynamically when test results are selected
            }

            triggerDependants(
                source = uptResult,
                addItems = registrationFields,
                removeItems = emptyList()
            )
        } else { // Negative
            triggerDependants(
                source = uptResult,
                addItems = emptyList(),
                removeItems = baseRegistrationFields +
                        listOf(historyOfAbortions, previousLSCS, complicationsInPreviousPregnancy,
                            vdrlRprDate, hivTestDate, hbsAgTestDate) // Remove date fields if they exist
            )
        }
    }

    private fun handleLmpChange(): Int {
        lmp.value?.let {
            updateCalculatedFields(System.currentTimeMillis())
        }
        return lmp.id
    }

    private fun handleGravidaChange(): Int {
        gravida.value?.toIntOrNull()?.let { gravidaValue ->
            Timber.d("Gravida changed to: $gravidaValue")

            if (gravidaValue <= 1) {
                removeHistoryFields()
            } else {
                addHistoryFields()
            }

            handleParaFieldForGravida(gravidaValue)
            updateHighRiskStatus()
            return historyOfAbortions.id
        }

        return -1
    }

    /**
     * Remove history fields when gravida <= 1
     */
    private fun removeHistoryFields() {
        val historyFields = listOf(historyOfAbortions, previousLSCS, complicationsInPreviousPregnancy)

        historyFields.forEach { field ->
            field.isEnabled = false
            field.required = false
            field.value = null
            triggerDependants(
                source = gravida,
                removeItems = listOf(field),
                addItems = emptyList()
            )
        }

        updateHighRiskStatus()
    }

    /**
     * Add history fields when gravida > 1
     */
    private fun addHistoryFields() {
        historyOfAbortions.isEnabled = true
        historyOfAbortions.required = true

        val paraPosition = getIndexById(para.id)
        if (paraPosition >= 0 && getIndexById(historyOfAbortions.id) < 0) {
            triggerDependants(
                source = gravida,
                addItems = listOf(historyOfAbortions),
                removeItems = emptyList(),
                position = paraPosition + 1
            )
        }

        addFieldIfNotExists(previousLSCS) { getIndexById(historyOfAbortions.id) + 1 }
        addFieldIfNotExists(complicationsInPreviousPregnancy) { getIndexById(previousLSCS.id) + 1 }
    }

    /**
     * Add a field if it doesn't exist in the form
     */
    private fun addFieldIfNotExists(field: FormElement, positionProvider: () -> Int) {
        field.isEnabled = true
        field.required = true
        if (getIndexById(field.id) < 0) {
            triggerDependants(
                source = gravida,
                addItems = listOf(field),
                removeItems = emptyList(),
                position = positionProvider()
            )
        }
    }

    /**
     * Handle Para field based on gravida value
     */
    private fun handleParaFieldForGravida(gravidaValue: Int) {
        if (gravidaValue == 1) {
            para.value = "0"
            para.isEnabled = false
            para.required = false
            para.errorText = null
            Timber.d("Para auto-set to 0 and disabled")
        } else {
            para.isEnabled = true
            para.required = true
            if (para.value == "0") {
                para.value = null
            }
        }
    }

    private fun handleHistoryOfAbortionsChange(index: Int): Int {
        if (index == 0) { // Yes
            onShowAlert?.invoke("History of abortions detected – High Risk Pregnancy")
        }
        updateHighRiskStatus()
        return -1
    }

    private fun handlePreviousLSCSChange(index: Int): Int {
        if (index == 0) { // Yes
            onShowAlert?.invoke("Previous LSCS detected – High Risk Pregnancy")
        }
        updateHighRiskStatus()
        return -1
    }

    private fun handleVdrlRprResultChange(index: Int) {
        when (index) {
            TEST_RESULT_INDEX_REACTIVE -> { // Reactive
                onShowAlert?.invoke("VDRL/RPR Test is Reactive - HRP condition")
                showVdrlRprDateField()
            }
            TEST_RESULT_INDEX_NON_REACTIVE -> { // Non-Reactive
                showVdrlRprDateField()
            }
            TEST_RESULT_INDEX_NOT_DONE -> { // Test Not Done
                hideVdrlRprDateField()
            }
        }
        updateHighRiskStatus()
    }

    private fun handleHivResultChange(index: Int) {
        when (index) {
            TEST_RESULT_INDEX_REACTIVE -> { // Reactive
                onShowAlert?.invoke("HIV Test is Reactive - HRP condition")
                showHivTestDateField()
            }
            TEST_RESULT_INDEX_NON_REACTIVE -> { // Non-Reactive
                showHivTestDateField()
            }
            TEST_RESULT_INDEX_NOT_DONE -> { // Test Not Done
                hideHivTestDateField()
            }
        }
        updateHighRiskStatus()
    }

    private fun handleHbsAgResultChange(index: Int) {
        when (index) {
            TEST_RESULT_INDEX_REACTIVE -> { // Positive
                onShowAlert?.invoke("HBsAg Test is Positive - HRP condition")
                showHbsAgTestDateField()
            }
            TEST_RESULT_INDEX_NON_REACTIVE -> { // Negative
                showHbsAgTestDateField()
            }
            TEST_RESULT_INDEX_NOT_DONE -> { // Test Not Done
                hideHbsAgTestDateField()
            }
        }
        updateHighRiskStatus()
    }
    /**
     * Generic helper to show a test date field
     */
    private fun showTestDateField(dateField: FormElement, sourceField: FormElement) {
        val today = System.currentTimeMillis()
        dateField.min = oneYearBeforeRegMillis
        dateField.max = today
        dateField.isEnabled = true
        dateField.required = true
        addDateFieldAfterSource(sourceField, dateField)
    }

    /**
     * Generic helper to hide a test date field
     */
    private fun hideTestDateField(dateField: FormElement) {
        dateField.isEnabled = false
        dateField.required = false
        dateField.value = null
        removeDateField(dateField)
    }

    private fun showVdrlRprDateField() {
        showTestDateField(vdrlRprDate, vdrlRprResult)
    }

    private fun hideVdrlRprDateField() {
        hideTestDateField(vdrlRprDate)
    }

    private fun showHivTestDateField() {
        showTestDateField(hivTestDate, hivResult)
    }

    private fun hideHivTestDateField() {
        hideTestDateField(hivTestDate)
    }

    private fun showHbsAgTestDateField() {
        showTestDateField(hbsAgTestDate, hbsAgResult)
    }

    private fun hideHbsAgTestDateField() {
        hideTestDateField(hbsAgTestDate)
    }

    // Helper methods to add/remove fields
    private fun addDateFieldAfterSource(source: FormElement, dateField: FormElement) {
        // Check if date field already exists
        if (getIndexById(dateField.id) < 0) {
            // Use triggerDependants to add the date field
            triggerDependants(
                source = source,
                addItems = listOf(dateField),
                removeItems = emptyList(),
                position = getIndexById(source.id) + 1
            )
        }
    }

    private fun removeDateField(dateField: FormElement) {
        // Find which test result this date field belongs to
        val sourceField = when (dateField.id) {
            vdrlRprDate.id -> vdrlRprResult
            hivTestDate.id -> hivResult
            hbsAgTestDate.id -> hbsAgResult
            else -> null
        }

        sourceField?.let {
            triggerDependants(
                source = it,
                removeItems = listOf(dateField),
                addItems = emptyList()
            )
        }
    }
    /**
     * Generic helper to handle checkbox field changes with "None" option
     */
    private fun handleCheckboxFieldChange(
        field: FormElement,
        index: Int,
        alertMessage: String? = null
    ): Int {
        val entries = field.entries!!
        val clickedOption = entries[index]
        val currentValue = field.value ?: ""

        if (index == 0) { // "None" clicked
            field.value = clickedOption
        } else {
            val selections = mutableSetOf<String>()
            if (currentValue.isNotEmpty()) {
                selections.addAll(currentValue.split(",").map { it.trim() }.filter { it.isNotEmpty() })
            }
            selections.remove("None")

            if (selections.contains(clickedOption)) {
                selections.remove(clickedOption)
            } else {
                selections.add(clickedOption)
                alertMessage?.let { onShowAlert?.invoke(it) }
            }

            field.value = if (selections.isEmpty()) "" else selections.joinToString(",")
        }

        updateHighRiskStatus()
        return getIndexById(field.id)
    }

    private fun handleComplicationsChange(index: Int): Int {
        return handleCheckboxFieldChange(
            field = complicationsInPreviousPregnancy,
            index = index,
            alertMessage = "Previous pregnancy complication: ${complicationsInPreviousPregnancy.entries!![index]} - High Risk Pregnancy"
        )
    }

    private fun handleParaChange(): Int {
        val paraValue = para.value?.toIntOrNull() ?: return -1
        val gravidaValue = gravida.value?.toIntOrNull() ?: return -1

        return if (paraValue > gravidaValue) {
            para.errorText = "Para cannot exceed Gravida"
            para.value = gravidaValue.toString()
            -1
        } else {
            para.errorText = null
            -1
        }
    }

    private fun handleAnthropometryChange(): Int {
        validateIntMinMax(height)
        validateIntMinMax(weight)
        updateBMI()
        updateHighRiskStatus()
        if (getIndexById(bmi.id) >= 0) {
            return bmi.id
        }
        return -1
    }

    private fun handlePreExistingConditionsChange(index: Int): Int {
        return handleCheckboxFieldChange(
            field = preExistingConditions,
            index = index,
            alertMessage = "Pre-existing condition: ${preExistingConditions.entries!![index]} - High Risk Pregnancy"
        )
    }

    private fun updateCalculatedFields(currentDate: Long) {
        lmp.value?.let { lmpStr ->
            val lmpLong = getLongFromDate(lmpStr)

            // Calculate EDD
            val eddLong = getEddFromLmp(lmpLong)
            edd.value = getDateFromLong(eddLong)

            // Calculate gestational age
            val (weeks, days) = calculateGestationalAge(lmpLong, currentDate)
            gestationalAge.value = "$weeks weeks $days days"

            // Calculate trimester
            trimester.value = calculateTrimester(weeks)
        }
    }

    private fun updateBMI() {
        val heightValue = height.value?.toDoubleOrNull()
        val weightValue = weight.value?.toDoubleOrNull()

        if (heightValue != null && heightValue > 0 && weightValue != null && weightValue > 0) {
            val bmiValue = calculateBMI(weightValue, heightValue)
            val category = getBMICategory(bmiValue)
            bmi.value = String.format("%.1f (%s)", bmiValue, category)

            Timber.d("BMI updated: Height=$heightValue, Weight=$weightValue, BMI=${bmi.value}")

            // Check for height < 145 cm HRP condition
            if (heightValue < 145) {
                onShowAlert?.invoke("Height < 145 cm detected - HRP condition")
            }
        } else {
            // Clear BMI if values are invalid
            bmi.value = ""
            Timber.d("BMI cleared - invalid height or weight values")
        }
    }

    private fun updateHighRiskStatus() {
        val isHRP = checkHighRiskConditions()
        isHighRiskPregnancy.value = if (isHRP) "Yes" else "No"

        // Also update the cache
        registrationCache.isHrp = isHRP
    }

    private fun checkHighRiskConditions(): Boolean {
        return checkHeightCondition() ||
                checkHistoryOfAbortions() ||
                checkPreviousLSCS() ||
                checkPreExistingConditions() ||
                checkComplicationsInPreviousPregnancy() ||
                checkLabResults()
    }

    /**
     * Check if height < 145 cm (HRP condition)
     */
    private fun checkHeightCondition(): Boolean {
        return height.value?.toDoubleOrNull()?.let { it < 145 } == true
    }

    /**
     * Check if history of abortions exists
     */
    private fun checkHistoryOfAbortions(): Boolean {
        return historyOfAbortions.value == "Yes"
    }

    /**
     * Check if previous LSCS exists
     */
    private fun checkPreviousLSCS(): Boolean {
        return previousLSCS.value == "Yes"
    }

    /**
     * Check if pre-existing conditions exist (excluding "None")
     */
    private fun checkPreExistingConditions(): Boolean {
        return preExistingConditions.value?.let { value ->
            value.isNotEmpty() && hasNonNoneSelections(value)
        } == true
    }

    /**
     * Check if complications in previous pregnancy exist (excluding "None")
     */
    private fun checkComplicationsInPreviousPregnancy(): Boolean {
        return complicationsInPreviousPregnancy.value?.let { value ->
            value.isNotEmpty() && hasNonNoneSelections(value)
        } == true
    }

    /**
     * Check if any lab results indicate HRP
     */
    private fun checkLabResults(): Boolean {
        return vdrlRprResult.value == TEST_RESULT_REACTIVE ||
                hivResult.value == TEST_RESULT_REACTIVE ||
                hbsAgResult.value == TEST_RESULT_POSITIVE
    }

    /**
     * Check if comma-separated selections contain non-"None" values
     */
    private fun hasNonNoneSelections(value: String): Boolean {
        val selections = value.split(",").map { it.trim() }
        return selections.isNotEmpty() && !selections.contains("None")
    }

    private fun makeFormReadOnly(fields: List<FormElement>) {
        fields.forEach { field ->
            when (field.inputType) {
                InputType.EDIT_TEXT -> field.inputType = InputType.TEXT_VIEW
                InputType.DATE_PICKER -> field.inputType = InputType.TEXT_VIEW
                InputType.DROPDOWN -> field.inputType = InputType.TEXT_VIEW
                InputType.RADIO -> field.inputType = InputType.TEXT_VIEW
                InputType.CHECKBOXES -> field.inputType = InputType.TEXT_VIEW
                else -> {
                    // No conversion needed for other input types
                }
            }
            field.isEnabled = false
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as PregnantWomanRegistrationCache).let { cache ->
            // Map Date of Registration
            cache.dateOfRegistration = dateOfReg.value?.let { getLongFromDate(it) } ?: System.currentTimeMillis()

            // Map RCH ID
            cache.rchId = rchId.value?.toLongOrNull()

            // Map pregnancy details
            cache.lmpDate = lmp.value?.let { getLongFromDate(it) } ?: 0L

            // Map blood group
            cache.bloodGroup = bloodGroup.value
            cache.bloodGroupId = bloodGroup.getPosition()

            // Map anthropometry
            cache.height = height.value?.toIntOrNull()
            cache.weight = weight.value?.toIntOrNull()

            // Map obstetric history
            cache.numPrevPregnancy = gravida.value?.toIntOrNull()
            cache.is1st = (gravida.value?.toIntOrNull() ?: 1) == 1
            cache.historyOfAbortions = historyOfAbortions.value == "Yes"
            cache.previousLSCS = previousLSCS.value == "Yes"
            cache.complicationPrevPregnancy = complicationsInPreviousPregnancy.value

            // Map pre-existing conditions
            cache.pastIllness = preExistingConditions.value

            // Map VDRL/RPR lab investigations
            cache.vdrlRprTestResult = vdrlRprResult.value
            cache.vdrlRprTestResultId = vdrlRprResult.getPosition()
            cache.dateOfVdrlRprTest = vdrlRprDate.value?.let { getLongFromDate(it) }

            // Map HIV lab investigations
            cache.hivTestResult = hivResult.value
            cache.hivTestResultId = hivResult.getPosition()
            cache.dateOfHivTest = hivTestDate.value?.let { getLongFromDate(it) }

            // Map HBsAg lab investigations
            cache.hbsAgTestResult = hbsAgResult.value
            cache.hbsAgTestResultId = hbsAgResult.getPosition()
            cache.dateOfHbsAgTest = hbsAgTestDate.value?.let { getLongFromDate(it) }

            // Map high-risk status
            cache.isHrp = isHighRiskPregnancy.value == "Yes"

            // Set sync state
            cache.syncState = SyncState.UNSYNCED

            // Mark as first ANC submitted if this is the first submission
            if (!cache.isFirstAncSubmitted) {
                cache.isFirstAncSubmitted = true
            }
        }
    }

    fun shouldNavigateToVitals(): Boolean {
        // Navigate to Vitals & Prescription after successful submission
        return !isFormReadOnly
    }
}