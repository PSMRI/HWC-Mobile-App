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
import kotlin.math.min

class PregnantWomanRegistrationDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    companion object {
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

    private var isFormReadOnly: Boolean = false
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
        entries = arrayOf("Reactive", "Non-Reactive", "Test Not Done"),
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
        entries = arrayOf("Reactive", "Non-Reactive", "Test Not Done"),
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
        entries = arrayOf("Positive", "Negative", "Test Not Done"),
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
        isFormReadOnly = savedRecord?.isFirstAncSubmitted == true

        // Initialize date of registration
        dateOfRegMillis = savedRecord?.dateOfRegistration ?: System.currentTimeMillis()
        oneYearBeforeRegMillis = dateOfRegMillis - TimeUnit.DAYS.toMillis(365)

        val list = mutableListOf<FormElement>()

        // Always add Date of Registration first
        list.add(dateOfReg)
        list.add(rchId)

        // If form is read-only, we need to show ALL fields with saved values
        if (isFormReadOnly) {
            // For read-only mode, show all fields including test results and dates
            savedRecord?.let { cache ->
                populateFormFromCache(cache, list)

                // Add all the main fields that should be visible in read-only mode
                list.addAll(listOf(
                    pregnancyTestAtFacility,
                    uptResult,
                    lmp, edd, gestationalAge, trimester,
                    bloodGroup,
                    gravida, para
                ))

                // Add history fields if gravida > 1
                cache.numPrevPregnancy?.let { gravidaValue ->
                    if (gravidaValue > 1) {
                        list.addAll(listOf(
                            historyOfAbortions,
                            previousLSCS,
                            complicationsInPreviousPregnancy
                        ))
                    }
                }

                // Add anthropometry fields
                list.addAll(listOf(
                    height, weight, bmi
                ))

                // Add pre-existing conditions
                list.add(preExistingConditions)

                // Add lab investigation fields
                list.addAll(listOf(
                    vdrlRprResult,
                    hivResult,
                    hbsAgResult
                ))

                // Add high-risk pregnancy flag
                list.add(isHighRiskPregnancy)

                // Add date fields conditionally based on saved test results
                cache.vdrlRprTestResult?.let { vdrlResult ->
                    if (vdrlResult == "Reactive" || vdrlResult == "Non-Reactive") {
                        list.add(vdrlRprDate)
                    }
                }

                cache.hivTestResult?.let { hivResultValue ->
                    if (hivResultValue == "Reactive" || hivResultValue == "Non-Reactive") {
                        list.add(hivTestDate)
                    }
                }

                cache.hbsAgTestResult?.let { hbsAgResultValue ->
                    if (hbsAgResultValue == "Positive" || hbsAgResultValue == "Negative") {
                        list.add(hbsAgTestDate)
                    }
                }

                // Make all fields read-only
                makeFormReadOnly(list)
            }
        } else {
            // For new form/editable mode, start with basic fields
            list.add(pregnancyTestAtFacility)

            // Set initial values from saved record (for editing existing record)
            savedRecord?.let { cache ->
                populateFormFromCache(cache, list)

                // If editing an existing record, add the UPT result field
                // and conditionally add other fields based on UPT result
                val hasLmpDate = cache.lmpDate > 0
                if (hasLmpDate) {
                    list.add(uptResult)

                    // Add all fields that would be shown for positive UPT
                    list.addAll(getBaseRegistrationFields())

                    // Add history fields if gravida > 1
                    cache.numPrevPregnancy?.let { gravidaValue ->
                        if (gravidaValue > 1) {
                            list.addAll(listOf(
                                historyOfAbortions,
                                previousLSCS,
                                complicationsInPreviousPregnancy
                            ))
                        }
                    }

                    // Add date fields conditionally
                    cache.vdrlRprTestResult?.let { vdrlResult ->
                        if (vdrlResult == "Reactive" || vdrlResult == "Non-Reactive") {
                            list.add(vdrlRprDate)
                        }
                    }

                    cache.hivTestResult?.let { hivResultValue ->
                        if (hivResultValue == "Reactive" || hivResultValue == "Non-Reactive") {
                            list.add(hivTestDate)
                        }
                    }

                    cache.hbsAgTestResult?.let { hbsAgResultValue ->
                        if (hbsAgResultValue == "Positive" || hbsAgResultValue == "Negative") {
                            list.add(hbsAgTestDate)
                        }
                    }
                } else {
                    // If no LMP date, just add UPT result field
                    list.add(uptResult)
                }
            }
        }

        setUpPage(list)
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

    private suspend fun addDateFieldsForSavedRecords(cache: PregnantWomanRegistrationCache) {
        // Add VDRL/RPR date field if test was done
        cache.vdrlRprTestResult?.let { vdrlResultValue ->
            if (vdrlResultValue == "Reactive" || vdrlResultValue == "Non-Reactive") {
                addDateFieldAfterTestResult(vdrlRprResult, vdrlRprDate)
            }
        }

        // Add HIV date field if test was done
        cache.hivTestResult?.let { hivResultValue ->
            if (hivResultValue == "Reactive" || hivResultValue == "Non-Reactive") {
                addDateFieldAfterTestResult(hivResult, hivTestDate)
            }
        }

        // Add HBsAg date field if test was done
        cache.hbsAgTestResult?.let { hbsAgResultValue ->
            if (hbsAgResultValue == "Positive" || hbsAgResultValue == "Negative") {
                addDateFieldAfterTestResult(hbsAgResult, hbsAgTestDate)
            }
        }
    }

    private suspend fun addDateFieldAfterTestResult(testResultField: FormElement, dateField: FormElement) {
        val testResultPosition = getIndexById(testResultField.id)
        if (testResultPosition >= 0 && getIndexById(dateField.id) < 0) {
            triggerDependants(
                source = testResultField,
                addItems = listOf(dateField),
                removeItems = emptyList(),
                position = testResultPosition + 1
            )
        }
    }

    private fun createDefaultRegistrationCache(ben: PatientDisplay?): PregnantWomanRegistrationCache {
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

    private fun populateFormFromCache(cache: PregnantWomanRegistrationCache, list: MutableList<FormElement>) {
        dateOfReg.value = getDateFromLong(cache.dateOfRegistration)
        rchId.value = cache.rchId?.toString()

        // Set other values if available
        if (cache.lmpDate > 0) {
            lmp.value = getDateFromLong(cache.lmpDate)
            updateCalculatedFields(System.currentTimeMillis())
        }

        bloodGroup.value = cache.bloodGroup
        gravida.value = cache.numPrevPregnancy?.toString()
        para.value = cache.numPrevPregnancy?.toString()
        height.value = cache.height?.toString()
        weight.value = cache.weight?.toString()

        // Update BMI
        updateBMI()

        // Set VDRL/RPR values
        vdrlRprResult.value = cache.vdrlRprTestResult
        cache.vdrlRprTestResult?.let { vdrlResult ->
            if (vdrlResult == "Reactive" || vdrlResult == "Non-Reactive") {
                vdrlRprDate.isEnabled = true
                vdrlRprDate.required = true
                vdrlRprDate.value = cache.dateOfVdrlRprTest?.let { date -> getDateFromLong(date) }
            }
        }

        // Set HIV values
        hivResult.value = cache.hivTestResult
        cache.hivTestResult?.let { hivResultValue ->
            if (hivResultValue == "Reactive" || hivResultValue == "Non-Reactive") {
                hivTestDate.isEnabled = true
                hivTestDate.required = true
                hivTestDate.value = cache.dateOfHivTest?.let { date -> getDateFromLong(date) }
            }
        }

        // Set HBsAg values
        hbsAgResult.value = cache.hbsAgTestResult
        cache.hbsAgTestResult?.let { hbsAgResultValue ->
            if (hbsAgResultValue == "Positive" || hbsAgResultValue == "Negative") {
                hbsAgTestDate.isEnabled = true
                hbsAgTestDate.required = true
                hbsAgTestDate.value = cache.dateOfHbsAgTest?.let { date -> getDateFromLong(date) }
            }
        }

        cache.numPrevPregnancy?.let { gravidaValue ->
            if (gravidaValue > 1) {
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

                // Update high risk status
                updateHighRiskStatus()
            }
        }
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
                // Gravida is 1 or less - remove the field
                historyOfAbortions.isEnabled = false
                historyOfAbortions.required = false
                historyOfAbortions.value = null

                // Remove from form if it's currently visible
                triggerDependants(
                    source = gravida,
                    removeItems = listOf(historyOfAbortions),
                    addItems = emptyList()
                )

                // Also disable/remove previous LSCS and complications
                previousLSCS.isEnabled = false
                previousLSCS.required = false
                previousLSCS.value = null
                triggerDependants(
                    source = gravida,
                    removeItems = listOf(previousLSCS),
                    addItems = emptyList()
                )

                complicationsInPreviousPregnancy.isEnabled = false
                complicationsInPreviousPregnancy.required = false
                complicationsInPreviousPregnancy.value = null
                triggerDependants(
                    source = gravida,
                    removeItems = listOf(complicationsInPreviousPregnancy),
                    addItems = emptyList()
                )

                updateHighRiskStatus()

            } else {
                // Gravida is more than 1 - add the field
                historyOfAbortions.isEnabled = true
                historyOfAbortions.required = true

                // Add to form after the para field (position logic depends on your form structure)
                val paraPosition = getIndexById(para.id)
                if (paraPosition >= 0 && getIndexById(historyOfAbortions.id) < 0) {
                    // Add after para
                    triggerDependants(
                        source = gravida,
                        addItems = listOf(historyOfAbortions),
                        removeItems = emptyList(),
                        position = paraPosition + 1
                    )
                }

                // Also enable/add previous LSCS and complications
                previousLSCS.isEnabled = true
                previousLSCS.required = true
                if (getIndexById(previousLSCS.id) < 0) {
                    val historyPosition = getIndexById(historyOfAbortions.id)
                    triggerDependants(
                        source = gravida,
                        addItems = listOf(previousLSCS),
                        removeItems = emptyList(),
                        position = historyPosition + 1
                    )
                }

                complicationsInPreviousPregnancy.isEnabled = true
                complicationsInPreviousPregnancy.required = true
                if (getIndexById(complicationsInPreviousPregnancy.id) < 0) {
                    val lscsPosition = getIndexById(previousLSCS.id)
                    triggerDependants(
                        source = gravida,
                        addItems = listOf(complicationsInPreviousPregnancy),
                        removeItems = emptyList(),
                        position = lscsPosition + 1
                    )
                }
            }

            // Handle Para field
            if (gravidaValue == 1) {
                // Set para to 0 and disable it
                para.value = "0"
                para.isEnabled = false
                para.required = false
                para.errorText = null

                Timber.d("Para auto-set to 0 and disabled")
            } else {
                // Enable para field
                para.isEnabled = true
                para.required = true

                // Clear auto-set value if it was "0"
                if (para.value == "0") {
                    para.value = null
                }
            }

            updateHighRiskStatus()
            return historyOfAbortions.id
        }

        return -1
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
            0 -> { // Reactive
                onShowAlert?.invoke("VDRL/RPR Test is Reactive - HRP condition")
                showVdrlRprDateField()
            }
            1 -> { // Non-Reactive
                showVdrlRprDateField()
            }
            2 -> { // Test Not Done
                hideVdrlRprDateField()
            }
        }
        updateHighRiskStatus()
    }

    private fun handleHivResultChange(index: Int) {
        when (index) {
            0 -> { // Reactive
                onShowAlert?.invoke("HIV Test is Reactive - HRP condition")
                showHivTestDateField()
            }
            1 -> { // Non-Reactive
                showHivTestDateField()
            }
            2 -> { // Test Not Done
                hideHivTestDateField()
            }
        }
        updateHighRiskStatus()
    }

    private fun handleHbsAgResultChange(index: Int) {
        when (index) {
            0 -> { // Positive
                onShowAlert?.invoke("HBsAg Test is Positive - HRP condition")
                showHbsAgTestDateField()
            }
            1 -> { // Negative
                showHbsAgTestDateField()
            }
            2 -> { // Test Not Done
                hideHbsAgTestDateField()
            }
        }
        updateHighRiskStatus()
    }
    private fun showVdrlRprDateField() {
        // Update date constraints
        val today = System.currentTimeMillis()
        vdrlRprDate.min = oneYearBeforeRegMillis
        vdrlRprDate.max = today

        vdrlRprDate.isEnabled = true
        vdrlRprDate.required = true

        // Add date field after VDRL/RPR result field
        addDateFieldAfterSource(vdrlRprResult, vdrlRprDate)
    }

    private fun hideVdrlRprDateField() {
        vdrlRprDate.isEnabled = false
        vdrlRprDate.required = false
        vdrlRprDate.value = null

        // Remove date field
        removeDateField(vdrlRprDate)
    }

    private fun showHivTestDateField() {
        // Update date constraints
        val today = System.currentTimeMillis()
        hivTestDate.min = oneYearBeforeRegMillis
        hivTestDate.max = today

        hivTestDate.isEnabled = true
        hivTestDate.required = true

        // Add date field after HIV result field
        addDateFieldAfterSource(hivResult, hivTestDate)
    }

    private fun hideHivTestDateField() {
        hivTestDate.isEnabled = false
        hivTestDate.required = false
        hivTestDate.value = null

        // Remove date field
        removeDateField(hivTestDate)
    }

    private fun showHbsAgTestDateField() {
        // Update date constraints
        val today = System.currentTimeMillis()
        hbsAgTestDate.min = oneYearBeforeRegMillis
        hbsAgTestDate.max = today

        hbsAgTestDate.isEnabled = true
        hbsAgTestDate.required = true

        // Add date field after HBsAg result field
        addDateFieldAfterSource(hbsAgResult, hbsAgTestDate)
    }

    private fun hideHbsAgTestDateField() {
        hbsAgTestDate.isEnabled = false
        hbsAgTestDate.required = false
        hbsAgTestDate.value = null

        // Remove date field
        removeDateField(hbsAgTestDate)
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
    private fun handleComplicationsChange(index: Int): Int {
        val entries = complicationsInPreviousPregnancy.entries!!
        val clickedOption = entries[index]

        // Get current value
        val currentValue = complicationsInPreviousPregnancy.value ?: ""

        Timber.d("Complications change - Index: $index, Option: $clickedOption, Current: '$currentValue'")

        if (index == 0) { // "None" clicked
            // When "None" is clicked, set it as the only selection
            complicationsInPreviousPregnancy.value = clickedOption
            Timber.d("Set to 'None' only")
        } else {
            // Handle other options
            val selections = mutableSetOf<String>()

            if (currentValue.isNotEmpty()) {
                // Split the current value and filter out empty strings
                selections.addAll(currentValue.split(",").map { it.trim() }.filter { it.isNotEmpty() })
            }

            // Remove "None" if it exists (can't have "None" with other options)
            selections.remove("None")

            // Toggle the clicked option
            if (selections.contains(clickedOption)) {
                selections.remove(clickedOption)
                Timber.d("Removed: $clickedOption")
            } else {
                selections.add(clickedOption)
                // Show HRP alert for the selected complication
                onShowAlert?.invoke("Previous pregnancy complication: $clickedOption - High Risk Pregnancy")
                Timber.d("Added: $clickedOption")
            }

            // Update the value
            complicationsInPreviousPregnancy.value = if (selections.isEmpty()) ""
            else selections.joinToString(",")

            Timber.d("New value: '${complicationsInPreviousPregnancy.value}'")
        }

        updateHighRiskStatus()
        return getIndexById(complicationsInPreviousPregnancy.id)
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
        val entries = preExistingConditions.entries!!
        val clickedOption = entries[index]

        // Get current value
        val currentValue = preExistingConditions.value ?: ""

        Timber.d("Pre-existing conditions change - Index: $index, Option: $clickedOption, Current: '$currentValue'")

        if (index == 0) { // "None" clicked
            // When "None" is clicked, set it as the only selection
            preExistingConditions.value = clickedOption
            Timber.d("Set to 'None' only for pre-existing conditions")
        } else {
            // Handle other options
            val selections = mutableSetOf<String>()

            if (currentValue.isNotEmpty()) {
                // Split the current value and filter out empty strings
                selections.addAll(currentValue.split(",").map { it.trim() }.filter { it.isNotEmpty() })
            }

            // Remove "None" if it exists (can't have "None" with other options)
            selections.remove("None")

            // Toggle the clicked option
            if (selections.contains(clickedOption)) {
                selections.remove(clickedOption)
                Timber.d("Removed: $clickedOption from pre-existing conditions")
            } else {
                selections.add(clickedOption)
                // Show HRP alert for the selected condition
                onShowAlert?.invoke("Pre-existing condition: $clickedOption - High Risk Pregnancy")
                Timber.d("Added: $clickedOption to pre-existing conditions")
            }

            // Update the value
            preExistingConditions.value = if (selections.isEmpty()) ""
            else selections.joinToString(",")

            Timber.d("New value for pre-existing conditions: '${preExistingConditions.value}'")
        }

        updateHighRiskStatus()
        return getIndexById(preExistingConditions.id)
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
        var isHRP = false

        // Check height < 145 cm
        height.value?.toDoubleOrNull()?.let {
            if (it < 145) isHRP = true
        }

        // Check history of abortion
        if (historyOfAbortions.value == "Yes") isHRP = true

        // Check previous LSCS
        if (previousLSCS.value == "Yes") isHRP = true

        // Check pre-existing conditions (excluding "None")
        preExistingConditions.value?.let { value ->
            if (value.isNotEmpty()) {
                // Parse comma-separated string
                val selections = value.split(",").map { it.trim() }
                if (selections.isNotEmpty() && !selections.contains("None")) {
                    isHRP = true
                }
            }
        }

        // Check complications in previous pregnancy (excluding "None")
        complicationsInPreviousPregnancy.value?.let { value ->
            if (value.isNotEmpty()) {
                // Parse comma-separated string
                val selections = value.split(",").map { it.trim() }
                if (selections.isNotEmpty() && !selections.contains("None")) {
                    isHRP = true
                }
            }
        }

        // Check lab results
        if (vdrlRprResult.value == "Reactive" ||
            hivResult.value == "Reactive" ||
            hbsAgResult.value == "Positive") {
            isHRP = true
        }

        return isHRP
    }

    private fun makeFormReadOnly(fields: List<FormElement>) {
        fields.forEach { field ->
            when (field.inputType) {
                InputType.EDIT_TEXT -> field.inputType = InputType.TEXT_VIEW
                InputType.DATE_PICKER -> field.inputType = InputType.TEXT_VIEW
                InputType.DROPDOWN -> field.inputType = InputType.TEXT_VIEW
                InputType.RADIO -> field.inputType = InputType.TEXT_VIEW
                InputType.CHECKBOXES -> field.inputType = InputType.TEXT_VIEW
                else -> {}
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

    fun getIsFormReadOnly(): Boolean = isFormReadOnly

    fun shouldNavigateToVitals(): Boolean {
        // Navigate to Vitals & Prescription after successful submission
        return !isFormReadOnly
    }
}