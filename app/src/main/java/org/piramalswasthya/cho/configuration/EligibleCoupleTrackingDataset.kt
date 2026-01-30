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
        inputType = InputType.TEXT_VIEW,
        title = "Month",
        arrayId = R.array.visit_months,
        entries = resources.getStringArray(R.array.visit_months),
        required = false
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
        required = false,
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

    fun setNumberOfChildren(count: Int) {
        numberOfChildren = count
        updateContraceptionOptions()
    }

    fun setLastAntraInfo(injectionDate: Long?, dose: String?) {
        lastAntraInjectionDate = injectionDate
        lastAntraDose = dose
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

    private fun calculateNextAntraDose(): String {
        if (lastAntraInjectionDate == null || lastAntraDose == null) {
            return resources.getStringArray(R.array.antra_doses)[0] // 1st Dose
        }

        val daysSinceLastInjection = ((System.currentTimeMillis() - lastAntraInjectionDate!!) / (1000 * 60 * 60 * 24)).toInt()

        // If gap > 120 days, restart from Dose 1
        if (daysSinceLastInjection > ANTRA_MAX_GAP_DAYS) {
            return resources.getStringArray(R.array.antra_doses)[0]
        }

        // Get next dose
        val doses = resources.getStringArray(R.array.antra_doses)
        val currentDoseIndex = doses.indexOf(lastAntraDose)
        return if (currentDoseIndex >= 0 && currentDoseIndex < doses.size - 1) {
            doses[currentDoseIndex + 1]
        } else {
            doses[0]
        }
    }

    private fun calculateAntraDueDate(injectionDate: Long): Long {
        // Due date = Injection Date + 76 days (minimum gap)
        return Calendar.getInstance().apply {
            timeInMillis = injectionDate
            add(Calendar.DAY_OF_YEAR, ANTRA_MIN_GAP_DAYS + 1)
        }.timeInMillis
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

        if (saved == null) {
            dateOfVisit.value = getDateFromLong(System.currentTimeMillis())
            dateOfVisit.value?.let {
                financialYear.value = getFinancialYear(it)
                month.value = resources.getStringArray(R.array.visit_months)[getMonth(it)!!]
            }

            // Set minimum date for visit (next month after last visit or registration date)
            dateOfVisit.min = lastTrack?.let {
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

            // ANTRA dose restart alert is handled in ViewModel/Fragment level
        } else {
            // Restore saved values
            dateOfVisit.value = getDateFromLong(saved.visitDate)
            financialYear.value = saved.financialYear ?: getFinancialYear(dateOfVisit.value)
            month.value = saved.visitMonth ?: resources.getStringArray(R.array.visit_months)[getMonth(dateOfVisit.value)!!]

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
                    if (saved.methodOfContraception == "ANTRA Injection") {
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
                    if (saved.methodOfContraception in listOf("Male Sterilization", "Female Sterilization", "MiniLap")) {
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

            if (saved.isPregnant == "No") {
                if (!list.contains(usingFamilyPlanning)) {
                    list.add(usingFamilyPlanning)
                    saved.usingFamilyPlanning?.let {
                        usingFamilyPlanning.value = if (it) "Yes" else "No"
                    }
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
                        triggerDependants(
                            source = pregnancyTestResult,
                            removeItems = listOf(isPregnant, usingFamilyPlanning, methodOfContraception, anyOtherMethod, antraDose, antraInjectionDate, antraDueDate, dateOfSterilization),
                            addItems = emptyList()
                        )
                    }
                    "Negative" -> {
                        // Clear pregnant, enable it, show isPregnant
                        isPregnant.value = null
                        isPregnant.isEnabled = true
                        triggerDependants(
                            source = pregnancyTestResult,
                            passedIndex = index,
                            triggerIndex = 1,
                            target = isPregnant,
                            targetSideEffect = listOf(usingFamilyPlanning, methodOfContraception, anyOtherMethod, antraDose, antraInjectionDate, antraDueDate, dateOfSterilization)
                        )
                    }
                    else -> -1
                }
            }

            isPregnant.id -> {
                when (isPregnant.value) {
                    "Yes" -> {
                        // Hide family planning
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
                    "ANTRA Injection" -> {
                        // Show ANTRA fields, remove others
                        antraDose.value = calculateNextAntraDose()
                        triggerDependants(
                            source = methodOfContraception,
                            removeItems = listOf(anyOtherMethod, dateOfSterilization),
                            addItems = listOf(antraDose, antraInjectionDate, antraDueDate)
                        )
                    }
                    "Male Sterilization", "Female Sterilization", "MiniLap" -> {
                        // Show date of sterilization, remove others
                        triggerDependants(
                            source = methodOfContraception,
                            removeItems = listOf(anyOtherMethod, antraDose, antraInjectionDate, antraDueDate),
                            addItems = listOf(dateOfSterilization)
                        )
                    }
                    "Any Other Method" -> {
                        // Show any other method text field, remove others
                        triggerDependants(
                            source = methodOfContraception,
                            removeItems = listOf(antraDose, antraInjectionDate, antraDueDate, dateOfSterilization),
                            addItems = listOf(anyOtherMethod)
                        )
                    }
                    else -> {
                        // Remove all conditional fields
                        triggerDependants(
                            source = methodOfContraception,
                            removeItems = listOf(anyOtherMethod, antraDose, antraInjectionDate, antraDueDate, dateOfSterilization),
                            addItems = emptyList()
                        )
                    }
                }
            }

            antraInjectionDate.id -> {
                // Calculate and set due date
                antraInjectionDate.value?.let {
                    val injectionDateLong = getLongFromDate(it)
                    val dueDate = calculateAntraDueDate(injectionDateLong)
                    antraDueDate.value = getDateFromLong(dueDate)
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
            if (methodOfContraception.value == "ANTRA Injection") {
                form.antraDose = antraDose.value
                form.antraInjectionDate = antraInjectionDate.value?.let { getLongFromDate(it) }
                form.antraDueDate = antraDueDate.value?.let { getLongFromDate(it) }
            } else {
                form.antraDose = null
                form.antraInjectionDate = null
                form.antraDueDate = null
            }

            // Handle Sterilization fields
            if (methodOfContraception.value in listOf("Male Sterilization", "Female Sterilization", "MiniLap")) {
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
        return methodOfContraception.value in listOf("Male Sterilization", "Female Sterilization", "MiniLap")
    }

    fun isAntraSelected(): Boolean {
        return methodOfContraception.value == "ANTRA Injection"
    }

    fun getAntraDueDate(): Long? {
        return antraDueDate.value?.let { getLongFromDate(it) }
    }

    fun getSterilizationMethod(): String? {
        return if (isSterilizationSelected()) methodOfContraception.value else null
    }
}
