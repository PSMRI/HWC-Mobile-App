package org.piramalswasthya.cho.configuration

import android.content.Context
import android.content.res.Resources
import android.util.Range
import androidx.annotation.StringRes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.helpers.setToStartOfTheDay
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.utils.HelperUtil.getLocalizedResources
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit


/**
 * Base class to be extended to use as a sandwich between viewModel and repository objects.
 * @see org.piramalswasthya.sakhi.adapters.FormInputAdapter
 */
abstract class Dataset(context: Context, currentLanguage: Languages) {

    /**
     * Resource object of currently selected language. To be used to get language specific strings from strings.xml.
     */
    protected var resources: Resources
    protected var englishResources: Resources

    init {
        englishResources = getLocalizedResources(context, Languages.ENGLISH)
        resources = getLocalizedResources(context, currentLanguage)
    }

    /**
     * Helper function to get resource instance chosen language.
     */

    protected companion object {
        fun getLongFromDate(dateString: String?): Long {
            val f = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            val date = dateString?.let { f.parse(it) }
            return date?.time ?: 0L
        }

        fun getFinancialYear(dateString: String?): String? {
            val f = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            val date = dateString?.let { f.parse(it) }
            return date?.let {
                if (it.month >= 3) {
                    "" + (it.year + 1900) + " - " + (it.year + 1902)
                } else {
                    "" + (it.year + 1899) + " - " + (it.year + 1901)
                }
            }
        }

        fun getMonth(dateString: String?): Int? {
            val f = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            dateString?.let {
                val date = f.parse(it)
                val cal = Calendar.getInstance().apply { time = date }
                return cal.get(Calendar.MONTH)
            } ?: kotlin.run { return null }

        }

        fun getDateFromLong(dateLong: Long): String? {
            if (dateLong == 0L) return null
            val cal = Calendar.getInstance()
            cal.timeInMillis = dateLong
            val f = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            return f.format(cal.time)


        }

        fun getMinDateOfReg(): Long {
            return Calendar.getInstance().apply {
                set(Calendar.YEAR, 2020)
                set(Calendar.MONTH, 0)
                set(Calendar.DAY_OF_MONTH, 1)
            }.timeInMillis

        }
    }


    private val list = mutableListOf<FormElement>()

    private val _listFlow = MutableStateFlow<List<FormElement>>(emptyList())
    val listFlow = _listFlow.asStateFlow()

    private val _alertErrorMessageFlow = MutableStateFlow<String?>(null)
    val alertErrorMessageFlow = _alertErrorMessageFlow.asStateFlow()

    suspend fun resetErrorMessageFlow() {
        _alertErrorMessageFlow.emit(null)
    }

    protected fun FormElement.getPosition(): Int {
        return value?.let { entries?.indexOf(it)?.plus(1) } ?: 0
    }

    protected fun FormElement.getStringFromPosition(position: Int): String? {
        return if (position <= 0) null else entries?.get(position - 1)
    }

    protected fun FormElement.getEnglishStringFromPosition(position: Int): String? {
        return if (position <= 0) null else englishResources.getStringArray(arrayId)[position - 1]
    }


    protected abstract suspend fun handleListOnValueChanged(formId: Int, index: Int): Int

    abstract fun mapValues(cacheModel: FormDataModel, pageNumber: Int = 0)
    protected fun getIndexOfElement(element: FormElement) = list.indexOf(element)
    suspend fun updateList(formId: Int, index: Int) {
        list.find { it.id == formId }?.let {
            if (it.inputType == InputType.DROPDOWN) {
                it.errorText = null
            }
        }
        val updateIndex = handleListOnValueChanged(formId, index)
        if (updateIndex != -1) {
            val newList = list.toMutableList()
//            if (updateUIForCurrentElement) {
//                Timber.d("Updating UI element ...")
//                newList[updateIndex] = list[updateIndex].cloneForm()
//                updateUIForCurrentElement = false
//            }
            Timber.d("Emitting ${newList}}")
//            _listFlow.emit(emptyList())
            _listFlow.emit(newList)
        }
    }

    protected suspend fun setUpPage(mList: List<FormElement>) {
        list.clear()
        list.addAll(mList)
        _listFlow.emit(list.toMutableList())
    }


    protected fun triggerDependants(
        source: FormElement,
        passedIndex: Int,
        triggerIndex: Int,
        target: List<FormElement>,
        targetSideEffect: List<FormElement>? = null
    ): Int {
        return if (passedIndex == triggerIndex) {
            if (!list.containsAll(target)) {
                val listIndex = list.indexOf(source)
                list.addAll(
                    listIndex + 1, target
                )
                listIndex
            } else -1
        } else {
            val anyRemoved = list.removeAll(
                target
            )
            if (anyRemoved) {

                targetSideEffect?.let { sideEffectList ->
                    sideEffectList.forEach { it.value = null
                    it.errorText = null}
                    list.removeAll(sideEffectList)
                }
                target.forEach {
                    it.value = null
                    it.errorText = null
                }
                list.indexOf(source)
            } else -1
        }
    }

    protected fun triggerDependantsReverse(
        source: FormElement,
        passedIndex: Int,
        triggerIndex: Int,
        target: List<FormElement>,
        targetSideEffect: List<FormElement>? = null
    ): Int {
        return if (passedIndex != triggerIndex) {
            if (!list.containsAll(target)) {
                val listIndex = list.indexOf(source)
                list.addAll(
                    listIndex + 1, target
                )
                listIndex
            } else -1
        } else {
            val anyRemoved = list.removeAll(
                target
            )
            if (anyRemoved) {
                target.forEach {
                    it.value = null
                }
                targetSideEffect?.let { sideEffectList ->
                    list.removeAll(sideEffectList)
                    sideEffectList.forEach { it.value = null }
                }
                list.indexOf(source)
            } else -1
        }
    }


    protected fun triggerDependants(
        source: FormElement,
        passedIndex: Int,
        triggerIndex: Int,
        target: FormElement,
        targetSideEffect: List<FormElement>? = null
    ): Int {
        return if (passedIndex == triggerIndex) {
            if (!list.contains(target)) {
                val listIndex = list.indexOf(source)
                list.add(
                    listIndex + 1, target
                )
                listIndex
            } else -1
        } else {
            val anyRemoved = list.remove(
                target
            )
            if (anyRemoved) {
                target.value = null
                targetSideEffect?.let { sideEffectList ->
                    list.removeAll(sideEffectList)
                    sideEffectList.forEach { it.value = null }
                }
                list.indexOf(source)
            } else -1
        }
    }

    protected fun triggerDependants(
        age: Int,
        ageUnit: FormElement,
        ageTriggerRange: Range<Int>,
        ageUnitTriggerIndex: Int,
        target: FormElement,
        placeAfter: FormElement,
        targetSideEffect: List<FormElement>? = null
    ): Int {
        Timber.d("YTRU")
        return if (age in ageTriggerRange && ageUnit.value == ageUnit.entries?.get(
                ageUnitTriggerIndex
            )
        ) {
            if (!list.contains(target)) {
                val listIndex = list.indexOf(placeAfter)
                list.add(
                    listIndex + 1, target
                )
                listIndex + 1
            } else -1
        } else {
            val anyRemoved = list.remove(
                target
            )
            if (anyRemoved) {
                target.value = null
                targetSideEffect?.let { sideEffectList ->
                    list.removeAll(sideEffectList)
                    sideEffectList.forEach { it.value = null }
                }
                302
            } else -1
        }
    }

    protected fun triggerDependants(
        source: FormElement,
        removeItems: List<FormElement>,
        addItems: List<FormElement>,
        position: Int = -1,
    ): Int {
        removeItems.forEach {
            it.value = null
        }
        list.removeAll(removeItems)
//        list.removeAll(addItems)
        addItems.forEach {
            if (list.contains(it)) list.remove(it)
        }
        val addPosition = if (position == -2) list.lastIndex + 1 else position.takeIf { it != -1 }
            ?: (list.indexOf(source) + 1)
        list.addAll(addPosition, addItems)
        return addPosition
//        return if (age in ageTriggerRange && ageUnit.value == ageUnit.entries?.get(
//                ageUnitTriggerIndex
//            )
//        ) {
//            if (!list.contains(target)) {
//                val listIndex = list.indexOf(placeAfter)
//                list.add(
//                    listIndex + 1, target
//                )
//                listIndex + 1
//            } else
//                -1
//        } else {
//            val anyRemoved = list.remove(
//                target
//            )
//            if (anyRemoved) {
//                target.value = null
//                targetSideEffect?.let { sideEffectList ->
//                    list.removeAll(sideEffectList)
//                    sideEffectList.forEach { it.value = null }
//                }
//                302
//            } else -1
//        }
    }


    protected fun getDiffYears(a: Calendar, b: Calendar): Int {
        var diff = b.get(Calendar.YEAR) - a.get(Calendar.YEAR)
        if (a.get(Calendar.MONTH) >= b.get(Calendar.MONTH)
            && a.get(
                Calendar.DAY_OF_MONTH
            ) > b.get(
                Calendar.DAY_OF_MONTH
            )
        ) {
            diff--
        }
        return diff
    }

    protected fun getDiffMonths(a: Calendar, b: Calendar): Int {
        var diffY = b.get(Calendar.YEAR) - a.get(Calendar.YEAR)
        if (a.get(Calendar.MONTH) > b.get(Calendar.MONTH) || a.get(Calendar.MONTH) == b.get(
                Calendar.MONTH
            ) && a.get(
                Calendar.DAY_OF_MONTH
            ) > b.get(
                Calendar.DAY_OF_MONTH
            )
        ) {
            diffY--
        }
        if (diffY != 0) return -1
        var diffM = b.get(Calendar.MONTH) - a.get(Calendar.MONTH)
        if (a.get(Calendar.DAY_OF_MONTH) > b.get(Calendar.DAY_OF_MONTH)) {
            diffM--
        }
        if (diffM < 0) diffM += 12

        return diffM
    }

    private fun getDiffDays(a: Calendar, b: Calendar): Int {
        val millisDiff = b.timeInMillis - a.timeInMillis
        return TimeUnit.MILLISECONDS.toDays(millisDiff).toInt()
    }

    protected fun getEddFromLmp(lmp: Long) =

        Calendar.getInstance().apply {
            timeInMillis = lmp
            add(Calendar.WEEK_OF_YEAR, 40)
        }.timeInMillis

    protected fun getMinFromMaxForLmp(lmp: Long) =

        Calendar.getInstance().apply {
            timeInMillis = lmp
            add(Calendar.WEEK_OF_YEAR, -40)
        }.timeInMillis


    protected suspend fun emitAlertErrorMessage(
        @StringRes errorMessage: Int
    ) {
        _alertErrorMessageFlow.emit(resources.getString(errorMessage))
    }

    protected fun assignValuesToAgeAndAgeUnitFromDob(
        dob: Long, ageElement: FormElement, ageUnitElement: FormElement,
        ageAtMarriageElement: FormElement? = null, timeStampDateOfMarriage: Long? = null
    ): Int {
        ageUnitElement.errorText = null
        ageElement.errorText = null
        val calDob = Calendar.getInstance().apply {
            timeInMillis = dob
        }.setToStartOfTheDay()
        val calNow = Calendar.getInstance().setToStartOfTheDay()
        val yearsDiff = getDiffYears(calDob, calNow)
        ageAtMarriageElement?.value = null
        ageAtMarriageElement?.max = yearsDiff.toLong()
        timeStampDateOfMarriage?.let {
            ageAtMarriageElement?.value =
                getDiffYears(calDob, Calendar.getInstance().apply { timeInMillis = it }).toString()
        }
        if (yearsDiff > 0) {
            ageUnitElement.value = ageUnitElement.entries?.last()
            ageElement.value = yearsDiff.toString()
            return -1
        } else {
            val monthDiff = getDiffMonths(calDob, calNow)
            if (monthDiff > 0) {
                ageUnitElement.value = ageUnitElement.entries?.get(1)
                ageElement.value = monthDiff.toString()
                return -1
            } else {
                val dayDiff = getDiffDays(calDob, calNow)
                if (dayDiff >= 0) {
                    ageUnitElement.value = ageUnitElement.entries?.get(0)
                    ageElement.value = dayDiff.toString()
                    return -1
                }
            }
            return -1
        }
    }


    protected fun assignValuesToAgeFromDob(
        dob: Long, ageElement: FormElement
    ): Int {
        ageElement.errorText = null
        val calDob = Calendar.getInstance().apply {
            timeInMillis = dob
        }.setToStartOfTheDay()
        val calNow = Calendar.getInstance().setToStartOfTheDay()
        val yearsDiff = getDiffYears(calDob, calNow)
        if (yearsDiff > 0) {
            ageElement.value = yearsDiff.toString()
        } else
            ageElement.value = "0"
        return -1
    }

    protected fun getDoMFromDoR(yearsSinceMarriage: Int?, regDate: Long): Long? {
        if (yearsSinceMarriage == null) return null
        val cal = Calendar.getInstance()
//        cal.timeInMillis = regDate
        cal.add(Calendar.YEAR, -1 * yearsSinceMarriage)
        return cal.timeInMillis

    }


    protected fun validateEmptyOnEditText(formElement: FormElement): Int {
        if (formElement.required) {
            if (formElement.value.isNullOrEmpty()) formElement.errorText =
                resources.getString(R.string.form_input_empty_error)
            else {
                formElement.errorText = null
            }
        }
        return -1
    }

    private fun String.isAllUppercaseOrSpace() =
        takeIf { it.isNotEmpty() }?.toCharArray()?.all { it.isUpperCase() || it.isWhitespace() }
            ?: false


    private fun String.isAllAlphabetsAndSpace() =
        takeIf { it.isNotEmpty() }?.toCharArray()?.all { it.isWhitespace() || it.isLetter() }
            ?: false

    private fun String.isThereAnyNumber() =
        takeIf { it.isNotEmpty() }?.toCharArray()?.any { it.isDigit() }
            ?: false

    private fun String.isAllAlphaNumericAndSpace() =
        takeIf { it.isNotEmpty() }?.toCharArray()
            ?.all { it.isWhitespace() || it.isLetter() || it.isDigit() }
            ?: false

    private fun String.isAllAlphaNumeric() =
        takeIf { it.isNotEmpty() }?.toCharArray()
            ?.all { it.isLetter() || it.isDigit() }
            ?: false

    private fun String.isAnyAlphabetOrSpace() =
        takeIf { it.isNotEmpty() }?.toCharArray()
            ?.any { it.isWhitespace() || it.isLetter() }
            ?: false

    protected fun validateAllCapsOrSpaceOnEditText(formElement: FormElement): Int {
        if (formElement.allCaps) {
            formElement.value?.takeIf { it.isNotEmpty() }?.isAllUppercaseOrSpace()?.let {
                Timber.d("Is ok : $it")
                formElement.errorText = if (it) null
                else resources.getString(R.string.form_input_upper_case_error)
            } ?: run {
                if (!formElement.required) formElement.errorText = null
            }
        }
        return -1
    }

    protected fun validateEditTextFullLengthOccupied(formElement: FormElement): Int {

        formElement.value?.takeIf { it.isNotEmpty() }?.let {
            Timber.d("length : ${it.length}")
            formElement.errorText = if (it.length == formElement.etMaxLength) null
            else resources.getString(
                R.string.form_input_missing_entry_error,
                formElement.etMaxLength
            )
        } ?: run {
            formElement.errorText = null
        }
        return -1
    }

    protected fun validateAllAlphabetsSpaceOnEditText(formElement: FormElement): Int {
        formElement.value?.takeIf { it.isNotEmpty() }?.isAllAlphabetsAndSpace()?.let {
            if (formElement.errorText != null && formElement.errorText != resources.getString(R.string.form_input_alphabet_space_only_error))
                return@let
            if (it) formElement.errorText = null
            else formElement.errorText =
                resources.getString(R.string.form_input_alphabet_space_only_error)
        }
        return -1
    }

    protected fun validateAllAlphabetsSpecialOnEditText(formElement: FormElement): Int {
        formElement.value?.takeIf { it.isNotEmpty() }?.isThereAnyNumber()?.let {
            if (formElement.errorText != null && formElement.errorText != resources.getString(R.string.form_input_alphabet_special_only_error))
                return@let
            if (it) formElement.errorText =
                resources.getString(R.string.form_input_alphabet_special_only_error)
            else formElement.errorText =
                null
        }
        return -1
    }

    protected fun validateAllAlphaNumericSpaceOnEditText(formElement: FormElement): Int {
        formElement.value?.takeIf { it.isNotEmpty() }?.let {
            val isValid = it.isAllAlphaNumericAndSpace()
            if (formElement.errorText != null && formElement.errorText != resources.getString(R.string.form_input_alph_numeric_space_only_error))
                return@let
            if (isValid) formElement.errorText = null
            else formElement.errorText =
                resources.getString(R.string.form_input_alph_numeric_space_only_error)
        } ?: kotlin.run {
            formElement.errorText = null
        }
        return -1
    }

    protected fun validateAllAlphaNumericOnEditText(formElement: FormElement): Int {
        formElement.value?.takeIf { it.isNotEmpty() }?.let {
            val isValid = it.isAllAlphaNumeric()
            if (formElement.errorText != null && formElement.errorText != resources.getString(R.string.form_input_alph_numeric_space_only_error))
                return@let
            if (isValid) formElement.errorText = null
            else formElement.errorText =
                resources.getString(R.string.form_input_alph_numeric_space_only_error)
        } ?: kotlin.run {
            formElement.errorText = null
        }
        return -1
    }

    protected fun validateNoAlphabetSpaceOnEditText(formElement: FormElement): Int {
        formElement.value?.takeIf { it.isNotEmpty() }?.isAnyAlphabetOrSpace()?.let {
            if (formElement.errorText != null && formElement.errorText != resources.getString(R.string.form_input__no_alpha_space_error))
                return@let
            if (it) formElement.errorText =
                resources.getString(R.string.form_input__no_alpha_space_error)
            else formElement.errorText =
                null
        } ?: kotlin.run {
            formElement.errorText = null
        }
        return -1
    }

    protected fun validateAllDigitOnEditText(formElement: FormElement): Int {
        formElement.value?.takeIf { it.isNotEmpty() }?.all { it.isDigit() }?.let {
            if (it) formElement.errorText = null
            else formElement.errorText =
                resources.getString(R.string.form_input_digit_only_error)
        } ?: kotlin.run {
            formElement.errorText = null
        }
        return -1
    }

    protected fun validateIntMinMax(formElement: FormElement): Int {
        formElement.errorText = formElement.value?.takeIf { it.isNotEmpty() }?.toLong()?.let {
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
        return -1
    }

    protected fun validateDoubleMinMax(formElement: FormElement): Int {
        formElement.errorText = formElement.value?.takeIf { it.isNotEmpty() }?.let {
            if (it.first() == '.')
                "0$it"
            else it
        }?.toDouble()?.let {
            formElement.minDecimal?.let { min ->
                formElement.maxDecimal?.let { max ->
                    if (it < min) {
                        resources.getString(
                            R.string.form_input_min_limit_error_decimal, formElement.title, min
                        )
                    } else if (it > max) {
                        resources.getString(
                            R.string.form_input_max_limit_error_decimal, formElement.title, max
                        )
                    } else null
                }
            }
        }
        return -1
    }

    protected fun validateDoubleUpto1DecimalPlaces(formElement: FormElement): Int {
        formElement.errorText = formElement.value?.takeIf { it.isNotEmpty() }?.let {
            val periodCount = it.count { it == '.' }
            if (periodCount > 1) {
                "Invalid value"
            } else if (periodCount == 0) {
                if (it.any { !it.isDigit() })
                    "Invalid value"
                else
                    null
            } else {
                val prePeriod = it.substringBefore('.')
                val postPeriod = it.substringAfter('.')
                if (prePeriod.any { !it.isDigit() } || postPeriod.any { !it.isDigit() })
                    "Invalid value"
                else if (postPeriod.length > 1)
                    "Only 1 decimal place allowed"
                else
                    null
            }

        }
        return -1
    }


    protected fun validateMobileNumberOnEditText(formElement: FormElement): Int {
        formElement.errorText = formElement.value?.takeIf { it.isNotEmpty() }?.toLong()?.let {
            if (it < 6_000_000_000L) resources.getString(R.string.form_input_error_invalid_mobile) else null
        }
        return -1
    }


    protected fun validateRchIdOnEditText(formElement: FormElement): Int {
        formElement.errorText = formElement.value?.takeIf { it.isNotEmpty() }?.let { text ->
            if (text.length < 12) resources.getString(R.string.form_input_error_invalid_rch) else {
                val firstChar = text.first()
                if (text.all { it == firstChar })
                    "All digits cannot be $firstChar"
                else
                    null
            }
        }
        return -1
    }

    protected fun validateAllZerosOnEditText(formElement: FormElement): Int {

        formElement.value?.takeIf { it.isNotEmpty() }?.let {
            if (it.all { it == '0' })
                formElement.errorText = "Cannot be 0"
            else
                formElement.errorText = null
        } ?: kotlin.run { formElement.errorText = null }
        return -1
    }


    protected fun validateMcpOnEditText(formElement: FormElement): Int {
        formElement.errorText = formElement.value?.takeIf { it.isNotEmpty() }?.let {
            if (it.length < 12) resources.getString(R.string.form_input_error_invalid_mcp) else if (it.all { it == '0' }) {
                "Cannot be 0"
            } else null
        }
        return -1
    }

    protected fun validateAadharNoOnEditText(formElement: FormElement): Int {
        formElement.errorText = formElement.value?.takeIf { it.isNotEmpty() }?.let { text ->
            if (text.length < 12 || text.all { it == '0' }) resources.getString(R.string.form_input_error_invalid_aadhar) else null
        }
        return -1
    }

    private val bpRegex = Regex("(\\d{2,3})/(\\d{2,3})")

    protected fun validateForBp(
        bp: FormElement,
        minSys: Int = 50,
        maxSys: Int = 300,
        minDia: Int = 30,
        maxDia: Int = 200
    ): Int {
        if (bp.value.isNullOrEmpty()) {
            bp.errorText = null
            return -1
        }
        val matchResult = bpRegex.matchEntire(bp.value!!)
        if (matchResult == null)
            bp.errorText = "Invalid format. Should be like 123/56"
        else {
            val sys = matchResult.groupValues[1].toInt()
            val dia = matchResult.groupValues[2].toInt()
            bp.errorText = if (sys < minSys) "Systole should not be less than $minSys"
            else if (sys > maxSys) "Systole should not be greater than $maxSys"
            else if (dia < minDia) "Diastole should not be less then $minDia"
            else if (dia > maxDia) "Diastole should not be greater than $maxDia"
            else if (dia > sys) "Diastole cannot be greater than systole"
            else null
        }
        return -1

    }


    fun getIndexById(id: Int): Int {
        return list.find { it.id == id }?.let {
            list.indexOf(it)
        } ?: -1
    }

    fun getListSize() = list.size

    fun setValueById(id: Int, value: String?) {
        list.find { it.id == id }?.let {
            it.value = value
        }
    }


    protected fun calculateAge(date: Long): Int {
        val dob = Calendar.getInstance()
        dob.timeInMillis = date
        val today = Calendar.getInstance()
        var age = today[Calendar.YEAR] - dob[Calendar.YEAR]
        if (today[Calendar.DAY_OF_MONTH] < dob[Calendar.DAY_OF_MONTH]) {
            age--
        }
        return age
    }

    protected fun calculateDob(age: Int): Long {
        val dob = Calendar.getInstance()
        dob.set(Calendar.YEAR, dob[Calendar.YEAR] - age)
        return dob.timeInMillis
    }

    fun getLocalValueInArray(arrayId: Int, entry: String?): String? {
        entry?.let {
            return resources.getStringArray(arrayId)[englishResources.getStringArray(arrayId)
                .indexOf(it)]
        }
        return null
    }

    fun getEnglishValueInArray(arrayId: Int, entry: String?): String? {
        entry?.let {
            return englishResources.getStringArray(arrayId)[resources.getStringArray(arrayId)
                .indexOf(it)]
        }
        return null
    }

}