package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.helpers.setToStartOfTheDay
import org.piramalswasthya.cho.model.EligibleCoupleTrackingCache
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.PatientDisplay
import java.util.Calendar

class EligibleCoupleTrackingDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    companion object {
        // Method of Contraception indices
        const val INDEX_SELF = 0
        const val INDEX_ANTRA = 1
        const val INDEX_COPPER_T = 2
        const val INDEX_CONDOM = 3
        const val INDEX_MALA_N = 4
        const val INDEX_CHAYA = 5
        const val INDEX_ECP = 6
        const val INDEX_MALE_STERILIZATION = 7
        const val INDEX_FEMALE_STERILIZATION = 8
        const val INDEX_MINILAP = 9
        const val INDEX_ANY_OTHER = 10

        // ANTRA constants
        const val ANTRA_INJECTION = "ANTRA Injection"
        const val MALE_STERILIZATION_VAL = "Male Sterilization"
        const val FEMALE_STERILIZATION_VAL = "Female Sterilization"
        const val MINILAP_VAL = "MiniLap"
        const val ANTRA_MIN_GAP_DAYS = 75
        const val ANTRA_MAX_GAP_DAYS = 120

        // Backdating limits in days
        const val LMP_BACKDATE_LIMIT_DAYS = 180 // 6 months
        const val STERILIZATION_BACKDATE_LIMIT_DAYS = 60 // 2 months
        const val ANTRA_BACKDATE_LIMIT_DAYS = 60 // 2 months
    }

    private var dateOfVisit = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.tracking_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = true
    )

    private val financialYear = FormElement(
        id = 2,
        inputType = InputType.TEXT_VIEW,
        title = "Financial Year",
        required = false,
    )

    private val month = FormElement(
        id = 3,
        inputType = InputType.DROPDOWN,
        title = "Month",
        arrayId = R.array.visit_months,
        entries = resources.getStringArray(R.array.visit_months),
        required = true
    )

    private val lmpDate = FormElement(
        id = 4,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.lmp_date),
        required = false,
        max = System.currentTimeMillis(),
        min = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -LMP_BACKDATE_LIMIT_DAYS)
        }.timeInMillis
    )

    private val isPregnancyTestDone = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.is_pregnancy_test_done),
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val pregnancyTestResult = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.pregnancy_test_result),
        entries = arrayOf("Positive", "Negative"),
        required = true,
        hasDependants = true
    )

    private val isPregnant = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.is_woman_pregnant),
        entries = arrayOf("Yes", "No"),
        required = false,
        hasDependants = true
    )

    private val usingFamilyPlanning = FormElement(
        id = 8,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.using_family_planning),
        entries = arrayOf("Yes", "No"),
        required = false,
        hasDependants = true
    )

    private val methodOfContraception = FormElement(
        id = 9,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.method_of_contraception_label),
        arrayId = R.array.method_of_contraception,
        entries = resources.getStringArray(R.array.method_of_contraception),
        required = true,
        hasDependants = true
    )

    private val anyOtherMethod = FormElement(
        id = 10,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.any_other_method),
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT,
        etMaxLength = 50
    )

    private val antraDose = FormElement(
        id = 11,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.antra_dose),
        arrayId = R.array.antra_doses,
        entries = resources.getStringArray(R.array.antra_doses),
        required = true,
        hasDependants = true
    )

    private val antraInjectionDate = FormElement(
        id = 12,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.antra_injection_date),
        required = true,
        max = System.currentTimeMillis(),
        min = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -ANTRA_BACKDATE_LIMIT_DAYS)
        }.timeInMillis,
        hasDependants = true
    )

    private val antraDueDate = FormElement(
        id = 13,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.antra_due_date),
        required = false
    )

    private val dateOfSterilization = FormElement(
        id = 14,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.date_of_sterilization),
        required = true,
        max = System.currentTimeMillis(),
        min = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -STERILIZATION_BACKDATE_LIMIT_DAYS)
        }.timeInMillis
    )

    // Store number of children to filter sterilization options
    private var numberOfChildren: Int = 0

    // Store last ANTRA injection info for dose calculation
    private var lastAntraInjectionDate: Long? = null
    private var lastAntraDose: String? = null

    fun getIndexOfIsPregnant() = getIndexById(isPregnant.id)
    fun getContraceptionMethodId() = methodOfContraception.id

    fun setNumberOfChildren(count: Int) {
        numberOfChildren = count
        updateContraceptionOptions()
    }

    fun setLastAntraInfo(injectionDate: Long?, dose: String?) {
        lastAntraInjectionDate = injectionDate
        lastAntraDose = dose
    }

    private fun clearFamilyPlanningFields() {
        usingFamilyPlanning.value = null
        methodOfContraception.value = null
        anyOtherMethod.value = null
        antraDose.value = null
        antraInjectionDate.value = null
        antraDueDate.value = null
        dateOfSterilization.value = null
    }

    private fun clearPregnancyFields() {
        pregnancyTestResult.value = null
        isPregnant.value = null
        isPregnant.isEnabled = true
    }

    private fun updateContraceptionOptions() {
        val allOptions = resources.getStringArray(R.array.method_of_contraception).toMutableList()

        // If No. of Children = 0, hide sterilization options
        if (numberOfChildren == 0) {
            allOptions.removeAt(INDEX_MINILAP)
            allOptions.removeAt(INDEX_FEMALE_STERILIZATION)
            allOptions.removeAt(INDEX_MALE_STERILIZATION)
        }

        methodOfContraception.entries = allOptions.toTypedArray()
    }

    private fun calculateNextAntraDose(visitDate: Long): String {
        if (lastAntraInjectionDate == null || lastAntraDose == null) {
            return resources.getStringArray(R.array.antra_doses)[0] // 1st Dose
        }

        val daysSinceLastInjection = ((visitDate - lastAntraInjectionDate!!) / (1000 * 60 * 60 * 24)).toInt()

        // If gap is more than 120 days between any 2 doses then restart from "Dose-1"
        if (daysSinceLastInjection > ANTRA_MAX_GAP_DAYS) {
            return resources.getStringArray(R.array.antra_doses)[0]
        }

        // Get next dose
        val doses = resources.getStringArray(R.array.antra_doses)
        val currentDoseIndex = doses.indexOf(lastAntraDose)
        return if (currentDoseIndex >= 0 && currentDoseIndex < doses.size - 1) {
            doses[currentDoseIndex + 1]
        } else {
            // After 10th dose, stay on 10th or loop? Usually stays/repeats.
            doses.last()
        }
    }

    private fun calculateAntraDueDateRange(injectionDate: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = injectionDate
        
        cal.add(Calendar.DAY_OF_YEAR, 76)
        val startDate = getDateFromLong(cal.timeInMillis)
        
        cal.timeInMillis = injectionDate
        cal.add(Calendar.DAY_OF_YEAR, 120)
        val endDate = getDateFromLong(cal.timeInMillis)
        
        return "$startDate to $endDate"
    }

    suspend fun setUpPage(
        ben: PatientDisplay?,
        dateOfReg: Long,
        lastTrack: EligibleCoupleTrackingCache?,
        saved: EligibleCoupleTrackingCache?
    ) {
        val list = mutableListOf(
            dateOfVisit,
            financialYear,
            month,
            lmpDate,
            isPregnancyTestDone,
        )

        val now = System.currentTimeMillis()
        if (saved == null) {
            // Set minimum date for visit (next month after last visit or registration date)
            val minDate = lastTrack?.let {
                Calendar.getInstance().apply {
                    timeInMillis = it.visitDate
                    val currentMonth = get(Calendar.MONTH)
                    if (currentMonth == 11) {
                        set(Calendar.YEAR, get(Calendar.YEAR) + 1)
                        set(Calendar.MONTH, 0)
                    } else {
                        set(Calendar.MONTH, currentMonth + 1)
                    }
                    set(Calendar.DAY_OF_MONTH, 1)
                    setToStartOfTheDay()
                }.timeInMillis
            } ?: dateOfReg
            
            dateOfVisit.min = minDate
            // Ensure max is never less than min to avoid DatePicker crash
            dateOfVisit.max = if (now < minDate) minDate else now
            
            // Set initial value within [min, max] range
            val initialValue = if (now < minDate) minDate else now
            dateOfVisit.value = getDateFromLong(initialValue)
            dateOfVisit.value?.let {
                financialYear.value = getFinancialYear(it)
                month.value = resources.getStringArray(R.array.visit_months)[getMonth(it)!!]
            }

            // ANTRA dose restart alert is handled in ViewModel/Fragment level
        } else {
            // Restore saved values
            dateOfVisit.value = getDateFromLong(saved.visitDate)
            financialYear.value = saved.financialYear ?: getFinancialYear(dateOfVisit.value)
            month.value = saved.visitMonth ?: resources.getStringArray(R.array.visit_months)[getMonth(dateOfVisit.value)!!]
            
            // Still refresh max to avoid stale current time
            dateOfVisit.max = if (now < (dateOfVisit.min ?: 0L)) dateOfVisit.min else now

            saved.lmpDate?.let {
                lmpDate.value = getDateFromLong(it)
            }

            isPregnancyTestDone.value = saved.isPregnancyTestDone
            if (isPregnancyTestDone.value == "Yes") {
                list.add(list.indexOf(isPregnancyTestDone) + 1, pregnancyTestResult)
                pregnancyTestResult.value = saved.pregnancyTestResult

                if (pregnancyTestResult.value == "Negative") {
                    list.add(isPregnant)
                    isPregnant.value = saved.isPregnant
                }
            } else if (isPregnancyTestDone.value == "No") {
                list.add(usingFamilyPlanning)
                saved.usingFamilyPlanning?.let {
                    usingFamilyPlanning.value = if (it) "Yes" else "No"
                }

                if (saved.usingFamilyPlanning == true) {
                    list.add(methodOfContraception)
                    methodOfContraception.value = saved.methodOfContraception

                    // Handle ANTRA fields
                    if (saved.methodOfContraception == ANTRA_INJECTION) {
                        list.add(antraDose)
                        list.add(antraInjectionDate)
                        list.add(antraDueDate)
                        antraDose.value = saved.antraDose
                        saved.antraInjectionDate?.let {
                            antraInjectionDate.value = getDateFromLong(it)
                        }
                        saved.antraDueDate?.let {
                            antraDueDate.value = getDateFromLong(it)
                        }
                    }

                    // Handle Sterilization fields
                    if (saved.methodOfContraception in listOf(MALE_STERILIZATION_VAL, FEMALE_STERILIZATION_VAL, MINILAP_VAL)) {
                        list.add(dateOfSterilization)
                        saved.dateOfSterilization?.let {
                            dateOfSterilization.value = getDateFromLong(it)
                        }
                    }

                    // Handle Any Other Method
                    if (saved.methodOfContraception !in resources.getStringArray(R.array.method_of_contraception)) {
                        methodOfContraception.value = resources.getStringArray(R.array.method_of_contraception).last()
                        list.add(anyOtherMethod)
                        anyOtherMethod.value = saved.anyOtherMethod ?: saved.methodOfContraception
                    }
                }
            }

            if (saved.isPregnant == "No" && !list.contains(usingFamilyPlanning)) {
                list.add(usingFamilyPlanning)
                saved.usingFamilyPlanning?.let {
                    usingFamilyPlanning.value = if (it) "Yes" else "No"
                }
            }
        }

        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            dateOfVisit.id -> {
                financialYear.value = getFinancialYear(dateOfVisit.value)
                month.value = resources.getStringArray(R.array.visit_months)[getMonth(dateOfVisit.value)!!]
                -1
            }

            isPregnancyTestDone.id -> {
                when (isPregnancyTestDone.value) {
                    "Yes" -> {
                        // Show pregnancy test result, hide family planning
                        clearFamilyPlanningFields()
                        triggerDependants(
                            source = isPregnancyTestDone,
                            passedIndex = index,
                            triggerIndex = 0,
                            target = pregnancyTestResult,
                            targetSideEffect = listOf(usingFamilyPlanning, methodOfContraception, anyOtherMethod, antraDose, antraInjectionDate, antraDueDate, dateOfSterilization)
                        )
                    }
                    "No" -> {
                        // Hide pregnancy test result and isPregnant, show family planning
                        clearPregnancyFields()
                        triggerDependants(
                            source = isPregnancyTestDone,
                            removeItems = listOf(pregnancyTestResult, isPregnant),
                            addItems = listOf(usingFamilyPlanning)
                        )
                    }
                    else -> -1
                }
            }

            pregnancyTestResult.id -> {
                when (pregnancyTestResult.value) {
                    "Positive" -> {
                        // Auto-set pregnant = Yes, hide family planning
                        isPregnant.value = "Yes"
                        isPregnant.isEnabled = false
                        clearFamilyPlanningFields()
                        triggerDependants(
                            source = pregnancyTestResult,
                            removeItems = listOf(isPregnant, usingFamilyPlanning, methodOfContraception, anyOtherMethod, antraDose, antraInjectionDate, antraDueDate, dateOfSterilization),
                            addItems = emptyList()
                        )
                    }
                    "Negative" -> {
                        // Auto-set pregnant = No, hide it, show family planning
                        isPregnant.value = "No"
                        isPregnant.isEnabled = false
                        clearFamilyPlanningFields()
                        triggerDependants(
                            source = pregnancyTestResult,
                            removeItems = listOf(isPregnant),
                            addItems = listOf(usingFamilyPlanning)
                        )
                    }
                    else -> -1
                }
            }

            isPregnant.id -> {
                when (isPregnant.value) {
                    "Yes" -> {
                        // Hide family planning
                        clearFamilyPlanningFields()
                        triggerDependants(
                            source = isPregnant,
                            removeItems = listOf(usingFamilyPlanning, methodOfContraception, anyOtherMethod, antraDose, antraInjectionDate, antraDueDate, dateOfSterilization),
                            addItems = emptyList()
                        )
                    }
                    "No" -> {
                        // Show family planning
                        triggerDependants(
                            source = isPregnant,
                            passedIndex = index,
                            triggerIndex = 1,
                            target = usingFamilyPlanning,
                            targetSideEffect = listOf(methodOfContraception, anyOtherMethod, antraDose, antraInjectionDate, antraDueDate, dateOfSterilization)
                        )
                    }
                    else -> -1
                }
            }

            usingFamilyPlanning.id -> {
                triggerDependants(
                    source = usingFamilyPlanning,
                    passedIndex = index,
                    triggerIndex = 0,
                    target = methodOfContraception,
                    targetSideEffect = listOf(anyOtherMethod, antraDose, antraInjectionDate, antraDueDate, dateOfSterilization)
                )
            }

            methodOfContraception.id -> {
                when (methodOfContraception.value) {
                    ANTRA_INJECTION -> {
                        // Show ANTRA fields, remove others
                        anyOtherMethod.value = null
                        dateOfSterilization.value = null
                        antraDose.value = calculateNextAntraDose(getLongFromDate(dateOfVisit.value))
                        
                        // Set backdating limit based on dose
                        val now = System.currentTimeMillis()
                        if (antraDose.value == resources.getStringArray(R.array.antra_doses)[0]) {
                            antraInjectionDate.min = Calendar.getInstance().apply {
                                add(Calendar.DAY_OF_YEAR, -ANTRA_BACKDATE_LIMIT_DAYS)
                            }.timeInMillis
                        } else {
                            // Validate from previous dose logic can be added here or in handler
                            antraInjectionDate.min = lastAntraInjectionDate ?: 0L
                        }
                        antraInjectionDate.max = if (now < (antraInjectionDate.min ?: 0L)) antraInjectionDate.min else now

                        triggerDependants(
                            source = methodOfContraception,
                            removeItems = listOf(anyOtherMethod, dateOfSterilization),
                            addItems = listOf(antraDose, antraInjectionDate, antraDueDate)
                        )
                    }
                    MALE_STERILIZATION_VAL, FEMALE_STERILIZATION_VAL, MINILAP_VAL -> {
                        // Show date of sterilization, remove others
                        anyOtherMethod.value = null
                        antraDose.value = null
                        antraInjectionDate.value = null
                        antraDueDate.value = null
                        
                        val now = System.currentTimeMillis()
                        dateOfSterilization.max = if (now < (dateOfSterilization.min ?: 0L)) dateOfSterilization.min else now
                        
                        triggerDependants(
                            source = methodOfContraception,
                            removeItems = listOf(anyOtherMethod, antraDose, antraInjectionDate, antraDueDate),
                            addItems = listOf(dateOfSterilization)
                        )
                    }
                    "Any Other Method" -> {
                        // Show any other method text field, remove others
                        antraDose.value = null
                        antraInjectionDate.value = null
                        antraDueDate.value = null
                        dateOfSterilization.value = null
                        triggerDependants(
                            source = methodOfContraception,
                            removeItems = listOf(antraDose, antraInjectionDate, antraDueDate, dateOfSterilization),
                            addItems = listOf(anyOtherMethod)
                        )
                    }
                    else -> {
                        // Remove all conditional fields
                        anyOtherMethod.value = null
                        antraDose.value = null
                        antraInjectionDate.value = null
                        antraDueDate.value = null
                        dateOfSterilization.value = null
                        triggerDependants(
                            source = methodOfContraception,
                            removeItems = listOf(anyOtherMethod, antraDose, antraInjectionDate, antraDueDate, dateOfSterilization),
                            addItems = emptyList()
                        )
                    }
                }
            }

            antraInjectionDate.id -> {
                // Calculate and set due date range
                antraInjectionDate.value?.let {
                    val injectionDateLong = getLongFromDate(it)
                    antraDueDate.value = calculateAntraDueDateRange(injectionDateLong)
                    
                    // Gap validation: 75 to 120 days
                    if (antraDose.value != resources.getStringArray(R.array.antra_doses)[0] && lastAntraInjectionDate != null) {
                        val gap = ((injectionDateLong - lastAntraInjectionDate!!) / (1000 * 60 * 60 * 24)).toInt()
                        if (gap < 75 || gap > 120) {
                            antraInjectionDate.errorText = "Gap between doses should be 75-120 days"
                        } else {
                            antraInjectionDate.errorText = null
                        }
                    }
                }
                -1
            }

            anyOtherMethod.id -> {
                validateAllAlphabetsSpaceOnEditText(anyOtherMethod)
            }

            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as EligibleCoupleTrackingCache).let { form ->
            form.visitDate = getLongFromDate(dateOfVisit.value)
            form.financialYear = financialYear.value
            form.visitMonth = month.value
            form.lmpDate = lmpDate.value?.let { getLongFromDate(it) }
            form.isPregnancyTestDone = isPregnancyTestDone.value
            form.pregnancyTestResult = pregnancyTestResult.value
            form.isPregnant = isPregnant.value
            form.usingFamilyPlanning = usingFamilyPlanning.value?.let { it == "Yes" }

            // Handle method of contraception
            if (methodOfContraception.value == "Any Other Method") {
                form.methodOfContraception = anyOtherMethod.value
                form.anyOtherMethod = anyOtherMethod.value
            } else {
                form.methodOfContraception = methodOfContraception.value
                form.anyOtherMethod = null
            }

            // Handle ANTRA fields
            if (methodOfContraception.value == ANTRA_INJECTION) {
                form.antraDose = antraDose.value
                form.antraInjectionDate = antraInjectionDate.value?.let { getLongFromDate(it) }
                // Save the first date of the range to database
                form.antraDueDate = antraDueDate.value?.let { 
                    val firstDateStr = it.split(" to ")[0]
                    getLongFromDate(firstDateStr)
                }
            } else {
                form.antraDose = null
                form.antraInjectionDate = null
                form.antraDueDate = null
            }

            // Handle Sterilization fields
            if (methodOfContraception.value in listOf(MALE_STERILIZATION_VAL, FEMALE_STERILIZATION_VAL, MINILAP_VAL)) {
                form.dateOfSterilization = dateOfSterilization.value?.let { getLongFromDate(it) }
            } else {
                form.dateOfSterilization = null
            }
        }
    }

    fun isPregnancyPositive(): Boolean {
        return isPregnant.value == "Yes" || pregnancyTestResult.value == "Positive"
    }

    fun isSterilizationSelected(): Boolean {
        return methodOfContraception.value in listOf(MALE_STERILIZATION_VAL, FEMALE_STERILIZATION_VAL, MINILAP_VAL)
    }

    fun isAntraSelected(): Boolean {
        return methodOfContraception.value == ANTRA_INJECTION
    }

    fun getAntraDueDate(): Long? {
        return antraDueDate.value?.let { getLongFromDate(it) }
    }

    fun getSterilizationMethod(): String? {
        return if (isSterilizationSelected()) methodOfContraception.value else null
    }
}
