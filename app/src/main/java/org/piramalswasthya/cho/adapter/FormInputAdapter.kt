package org.piramalswasthya.cho.adapter

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.children
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.RvItemFormAgePickerViewV2Binding
import org.piramalswasthya.cho.databinding.RvItemFormCheckV2Binding
import org.piramalswasthya.cho.databinding.RvItemFormDatepickerV2Binding
import org.piramalswasthya.cho.databinding.RvItemFormDropdownV2Binding
import org.piramalswasthya.cho.databinding.RvItemFormEditTextV2Binding
import org.piramalswasthya.cho.databinding.RvItemFormHeadlineV2Binding
import org.piramalswasthya.cho.databinding.RvItemFormImageViewV2Binding
import org.piramalswasthya.cho.databinding.RvItemFormRadioV2Binding
import org.piramalswasthya.cho.databinding.RvItemFormTextViewV2Binding
import org.piramalswasthya.cho.databinding.RvItemFormTimepickerV2Binding
import org.piramalswasthya.cho.helpers.Konstants
import org.piramalswasthya.cho.configuration.Dataset
import org.piramalswasthya.cho.helpers.getDateString
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.utils.KeyboardUtils
import org.piramalswasthya.cho.utils.setupDropdownKeyboardHandling
import org.piramalswasthya.cho.model.InputType.AGE_PICKER
import org.piramalswasthya.cho.model.InputType.CHECKBOXES
import org.piramalswasthya.cho.model.InputType.DATE_PICKER
import org.piramalswasthya.cho.model.InputType.DROPDOWN
import org.piramalswasthya.cho.model.InputType.EDIT_TEXT
import org.piramalswasthya.cho.model.InputType.HEADLINE
import org.piramalswasthya.cho.model.InputType.IMAGE_VIEW
import org.piramalswasthya.cho.model.InputType.RADIO
import org.piramalswasthya.cho.model.InputType.TEXT_VIEW
import org.piramalswasthya.cho.model.InputType.TIME_PICKER
import org.piramalswasthya.cho.model.InputType.values
import timber.log.Timber
import java.util.Calendar


class FormInputAdapter(
    private val imageClickListener: ImageClickListener? = null,
    private val ageClickListener: AgeClickListener? = null,
    private val formValueListener: FormValueListener? = null,
    private val isEnabled: Boolean = true
) : ListAdapter<FormElement, ViewHolder>(FormInputDiffCallBack) {

    //    @Inject
//    lateinit var preferenceDao: PreferenceDao
//    @Inject
//    lateinit var context: Context
    object FormInputDiffCallBack : DiffUtil.ItemCallback<FormElement>() {
        override fun areItemsTheSame(oldItem: FormElement, newItem: FormElement) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: FormElement, newItem: FormElement): Boolean {
            val contentsSame = oldItem.errorText == newItem.errorText && oldItem.value == newItem.value
            Timber.d("${oldItem.id} errorText: ${oldItem.errorText}==${newItem.errorText}, value: '${oldItem.value}'=='${newItem.value}', same=$contentsSame")
            return contentsSame
        }
    }


    class EditTextInputViewHolder private constructor(private val binding: RvItemFormEditTextV2Binding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormEditTextV2Binding.inflate(layoutInflater, parent, false)
                return EditTextInputViewHolder(binding)
            }
        }

        fun bind(item: FormElement, isEnabled: Boolean, formValueListener: FormValueListener?) {
            Timber.d("binding triggered!!! $isEnabled ${item.id}")
            if (!isEnabled) {
                binding.et.isEnabled = false
                binding.et.isClickable = false
                binding.et.isFocusable = false
                binding.et.isFocusableInTouchMode = false
                binding.et.isLongClickable = false
                binding.et.isCursorVisible = false
                handleHintLength(item)
                binding.form = item
                binding.et.setText(item.value)
                binding.executePendingBindings()
                return
            } else {
                binding.et.isEnabled = true
                binding.et.isClickable = true
                binding.et.isFocusable = true
                binding.et.isFocusableInTouchMode = true
                binding.et.isLongClickable = true
                binding.et.isCursorVisible = true
            }
            binding.form = item
            if (item.errorText == null) binding.tilEditText.isErrorEnabled = false
            Timber.d("Bound EditText item ${item.title} with ${item.required}")
            binding.tilEditText.error = item.errorText
            handleHintLength(item)
            if (item.hasSpeechToText) {
                binding.tilEditText.endIconDrawable =
                    AppCompatResources.getDrawable(binding.root.context, R.drawable.ic_mic)
                binding.tilEditText.setEndIconOnClickListener {
                    formValueListener?.onValueChanged(item, Konstants.micClickIndex)
                }
            } else {
                binding.tilEditText.endIconDrawable = null
                binding.tilEditText.setEndIconOnClickListener(null)
            }

            if (!binding.et.hasFocus()) {
                val currentText = binding.et.text?.toString() ?: ""
                val itemValue = item.value ?: ""
                if (currentText != itemValue) {
                    binding.et.setText(itemValue)
                }
            }

            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(editable: Editable?) {
                    val textValue = editable?.toString() ?: ""
                    item.value = textValue

                    val trimmedValue = textValue.trim()
                    if (trimmedValue.isNotBlank() && item.errorText != null) {
                        item.errorText = null
                        binding.tilEditText.isErrorEnabled = false
                        binding.tilEditText.error = null
                    }

                    formValueListener?.onValueChanged(item, -1)
                    if (item.errorText != binding.tilEditText.error) {
                        binding.tilEditText.isErrorEnabled = item.errorText != null
                        binding.tilEditText.error = item.errorText
                    }
//                        binding.tilEditText.error = null
//                    else if(item.errorText!= null && binding.tilEditText.error==null)
//                        binding.tilEditText.error = item.errorText


//                    if(item.etInputType == InputType.TYPE_CLASS_NUMBER && (item.hasDependants|| item.hasAlertError)){
//                        formValueListener?.onValueChanged(item,-1)
//                    }

//                    editable.let { item.value = it.toString() }
//                    item.value = editable.toString()
//                    Timber.d("Item ET : $item")
//                    if (item.isMobileNumber) {
//                        if (item.etMaxLength == 10) {
//                            if (editable.first().toString()
//                                    .toInt() < 6 || editable.length != item.etMaxLength
//                            ) {
//                                item.errorText = "Invalid Mobile Number !"
//                                binding.tilEditText.error = item.errorText
//                            } else {
//                                item.errorText = null
//                                binding.tilEditText.error = item.errorText
//                            }
//                        } else if (item.etMaxLength == 12) {
//                            if (editable.first().toString()
//                                    .toInt() == 0 || editable.length != item.etMaxLength
//                            ) {
//                                item.errorText = "Invalid ${item.title} !"
//                                binding.tilEditText.error = item.errorText
//                            } else {
//                                item.errorText = null
//                                binding.tilEditText.error = item.errorText
//                            }
//                        }
//                    }
//                else if (item.etInputType == InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL) {
//                        val entered = editable.toString().toDouble()
//                        item.minDecimal?.let {
//                            if (entered < it) {
//                                binding.tilEditText.error = "Field value has to be at least $it"
//                                item.errorText = binding.tilEditText.error.toString()
//                            }
//                        }
//                        item.maxDecimal?.let {
//                            if (entered > it) {
//                                binding.tilEditText.error =
//                                    "Field value has to be less than $it"
//                                item.errorText = binding.tilEditText.error.toString()
//                            }
//                        }
//                        if (item.minDecimal != null && item.maxDecimal != null && entered >= item.minDecimal!! && entered <= item.maxDecimal!!) {
//                            binding.tilEditText.error = null
//                            item.errorText = null
//                        }

//                    } else if (item.etInputType == (InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL)) {
//                        val age = editable.toString().toLong()
//                        item.min?.let {
//                            if (age < it) {
//                                binding.tilEditText.error = "Field value has to be at least $it"
//                                item.errorText = binding.tilEditText.error.toString()
//                            }
//                        }
//                        item.max?.let {
//                            if (age > it) {
//                                binding.tilEditText.error =
//                                    "Field value has to be less than $it"
//                                item.errorText = binding.tilEditText.error.toString()
//                            }
//                        }
//                        if (item.min != null && item.max != null && age >= item.min!! && age <= item.max!!) {
//                            binding.tilEditText.error = null
//                            item.errorText = null
//                        }
//                    } else {
//                        if (item.errorText != null && editable.isNotBlank()) {
//                            item.errorText = null
//                            binding.tilEditText.error = null
//                        }

//                    }

                }
            }
            binding.et.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) binding.et.addTextChangedListener(textWatcher)
                else {
                    binding.et.removeTextChangedListener(textWatcher)
                    val imm =
                        binding.root.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
                    imm!!.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
                }
            }
            binding.et.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && (event.action == KeyEvent.ACTION_UP || event.action == KeyEvent.ACTION_DOWN)) {
                    return@OnKeyListener true
                }
                false
            })

//            item.errorText?.also { binding.tilEditText.error = it }
//                ?: run { binding.tilEditText.error = null }
//            val etFilters = mutableListOf<InputFilter>(InputFilter.LengthFilter(item.etMaxLength))
//            binding.et.inputType = item.etInputType
//            if (item.etInputType == InputType.TYPE_CLASS_TEXT && item.allCaps) {
//                etFilters.add(AllCaps())
//                etFilters.add(FormEditTextDefaultInputFilter)
//            }
//            else if(item.etInputType == InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
//                etFilters.add(DecimalDigitsInputFilter)
//            binding.et.filters = etFilters.toTypedArray()
            binding.executePendingBindings()
        }

        private fun handleHintLength(item: FormElement) {
            if (item.title.length > Konstants.editTextHintLimit) {
                binding.tvHint.visibility = View.VISIBLE
                binding.et.hint = null
                binding.tilEditText.hint = null
                binding.tilEditText.isHintEnabled = false
            } else {
                binding.tvHint.visibility = View.GONE
                binding.tilEditText.isHintEnabled = true
            }
        }
    }

    class DropDownInputViewHolder private constructor(private val binding: RvItemFormDropdownV2Binding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormDropdownV2Binding.inflate(layoutInflater, parent, false)
                return DropDownInputViewHolder(binding)
            }
        }

        fun bind(item: FormElement, isEnabled: Boolean, formValueListener: FormValueListener?) {
            binding.form = item
            if (item.errorText == null) {
                binding.tilRvDropdown.error = null
                binding.tilRvDropdown.isErrorEnabled = false
            }
            if (!isEnabled) {
                binding.tilRvDropdown.visibility = View.GONE
                binding.tilEditText.visibility = View.VISIBLE
                binding.et.isFocusable = false
                binding.et.isClickable = false
                binding.executePendingBindings()
                return
            }

            binding.actvRvDropdown.setupDropdownKeyboardHandling()

            binding.actvRvDropdown.setOnItemClickListener { _, _, index, _ ->
                item.value = item.entries?.get(index)
                item.booleanValue = when (index) {
                    item.trueIndex -> true
                    item.falseIndex -> false
                    else -> null
                }
                Timber.d("Item DD : $item")
//                if (item.hasDependants || item.hasAlertError) {
                formValueListener?.onValueChanged(item, index)
//                }
                binding.tilRvDropdown.isErrorEnabled = item.errorText != null
                binding.tilRvDropdown.error = item.errorText
            }

            item.errorText?.let { binding.tilRvDropdown.error = it }
            binding.executePendingBindings()

        }
    }

    class RadioInputViewHolder private constructor(private val binding: RvItemFormRadioV2Binding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormRadioV2Binding.inflate(layoutInflater, parent, false)
                return RadioInputViewHolder(binding)
            }
        }

        fun bind(
            item: FormElement, isEnabled: Boolean, formValueListener: FormValueListener?
        ) {
            binding.rg.isClickable = isEnabled
            binding.rg.isFocusable = isEnabled
//            binding.rg.isEnabled = isEnabled
            binding.invalidateAll()
            binding.form = item

            binding.rg.removeAllViews()

            binding.rg.apply {
                item.entries?.let { items ->
                    orientation = item.orientation ?: LinearLayout.HORIZONTAL
                    weightSum = items.size.toFloat()
                    val isHorizontal = orientation == LinearLayout.HORIZONTAL
                    items.forEach {
                        val rdBtn = RadioButton(this.context)
                        rdBtn.layoutParams = RadioGroup.LayoutParams(
                            if (isHorizontal) 0 else RadioGroup.LayoutParams.MATCH_PARENT,
                            RadioGroup.LayoutParams.WRAP_CONTENT,
                            1.0F
                        ).apply {
                            gravity = Gravity.CENTER_HORIZONTAL
                        }
                        rdBtn.id = View.generateViewId()
                        rdBtn.text = it
                        addView(rdBtn)
                        if (item.value == it) rdBtn.isChecked = true
                        rdBtn.setOnClickListener {
                            KeyboardUtils.hideKeyboard(binding.root)
                            KeyboardUtils.hideKeyboardFromActivity(binding.root.context)
                        }
                        rdBtn.setOnCheckedChangeListener { _, b ->
                            if (b) {
                                item.value = it
                                val index = item.entries!!.indexOf(it)
                                item.booleanValue = when (index) {
                                    item.trueIndex -> true
                                    item.falseIndex -> false
                                    else -> null
                                }
                                if (item.hasDependants || item.hasAlertError) {
                                    Timber.d(
                                        "listener trigger : ${item.id} ${
                                            index
                                        } $it"
                                    )
                                    formValueListener?.onValueChanged(
                                        item, index
                                    )
                                }
                            }
                            item.errorText = null
                            binding.llContent.setBackgroundResource(0)
                        }
                    }
//                    item.value?.let { value ->
//                        children.forEach {
//                            if ((it as RadioButton).text == value) {
//                                clearCheck()
//                                check(it.id)
//                            }
//                        }
//                    }
                }
            }

            if (!isEnabled) {
                binding.rg.children.forEach {
                    it.isClickable = false
                    it.isFocusable = false
                    it.isEnabled = true
                }
            } else {
                binding.rg.isEnabled = true
            }
            if (item.errorText != null) binding.llContent.setBackgroundResource(R.drawable.state_errored)
            else binding.llContent.setBackgroundResource(0)

            //item.errorText?.let { binding.rg.error = it }
            binding.executePendingBindings()
            val str = binding.tvNullable.text
//            val str = binding.tvNullableHr.text
            val spannableString = SpannableString(str)

            val colorSpan = ForegroundColorSpan(Color.parseColor("#B00020"))
            val sizeSpan = RelativeSizeSpan(1.2f)

            if (item.required && item.doubleStar) {
                spannableString.setSpan(
                    colorSpan,
                    str.length - 2,
                    str.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
//                spannableString.setSpan(sizeSpan, str.length - 2, str.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                binding.tvNullable.text = spannableString
                binding.tvNullableHr.text = spannableString
            } else if (item.required) {
                spannableString.setSpan(
                    colorSpan,
                    str.length - 1,
                    str.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
//                spannableString.setSpan(sizeSpan, str.length - 1, str.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                binding.tvNullableHr.text = spannableString
                binding.tvNullable.text = spannableString
            }

        }
    }

    class CheckBoxesInputViewHolder private constructor(private val binding: RvItemFormCheckV2Binding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormCheckV2Binding.inflate(layoutInflater, parent, false)
                return CheckBoxesInputViewHolder(binding)
            }
        }

        fun bind(
            item: FormElement, isEnabled: Boolean, formValueListener: FormValueListener?
        ) {
            binding.form = item

            if (item.errorText != null) binding.clRi.setBackgroundResource(R.drawable.state_errored)
            else binding.clRi.setBackgroundResource(0)
            // Clear listeners before removing views to prevent false unchecked callbacks
            // that would strip values from item.value during rebind
            for (i in 0 until binding.llChecks.childCount) {
                (binding.llChecks.getChildAt(i) as? CheckBox)?.setOnCheckedChangeListener(null)
            }
            binding.llChecks.removeAllViews()
            binding.llChecks.apply {
                item.entries?.let { items ->
                    orientation = item.orientation ?: LinearLayout.VERTICAL
                    weightSum = items.size.toFloat()
                    items.forEachIndexed { index, it ->
                        val cbx = CheckBox(this.context)
                        cbx.layoutParams = RadioGroup.LayoutParams(
                            RadioGroup.LayoutParams.MATCH_PARENT,
                            RadioGroup.LayoutParams.WRAP_CONTENT,
                            1.0F
                        )
                        cbx.id = View.generateViewId()
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) cbx.setTextAppearance(
                            context, android.R.style.TextAppearance_Material_Medium
                        )
                        else cbx.setTextAppearance(android.R.style.TextAppearance_Material_Subhead)
                        cbx.text = it
                        addView(cbx)
                        if (item.value?.split(",")?.any { s -> s.trim() == it } == true) cbx.isChecked = true
                        cbx.setOnClickListener {
                            KeyboardUtils.hideKeyboard(binding.root)
                            KeyboardUtils.hideKeyboardFromActivity(binding.root.context)
                        }
                        if (!isEnabled) {
                            cbx.isClickable = false
                            cbx.isFocusable = false
                            cbx.isEnabled = true
                        }
                        cbx.setOnCheckedChangeListener { _, b ->
                            if (b) {
                                if (item.value != null && item.value!!.isNotEmpty()) item.value = item.value + "," + it
                                else item.value = it
                                if (item.hasDependants || item.hasAlertError) {
                                    Timber.d(
                                        "listener trigger : ${item.id} ${
                                            item.entries!!.indexOf(
                                                it
                                            )
                                        } $it"
                                    )
//                                    formValueListener?.onValueChanged(
//                                        item, item.entries!!.indexOf(it)
//                                    )
                                }
                            } else {
                                if (item.value?.contains(it) == true) {
                                    item.value = item.value!!.split(",").filter { s -> s.trim() != it }.joinToString(",").trim().takeIf { str -> str.isNotEmpty() } ?: null
                                }
                            }
                            formValueListener?.onValueChanged(
                                item, (index + 1) * (if (b) 1 else -1)
                            )
                            if (item.value.isNullOrBlank()) {
                                item.value = null
                            } else {
                                Timber.d("Called here!")
                                item.errorText = null
                                binding.clRi.setBackgroundResource(0)
                            }
                            Timber.d("Checkbox value : ${item.value}")

                        }
                    }
                }
            }
            binding.executePendingBindings()

        }
    }

    class DatePickerInputViewHolder private constructor(private val binding: RvItemFormDatepickerV2Binding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormDatepickerV2Binding.inflate(layoutInflater, parent, false)
                return DatePickerInputViewHolder(binding)
            }
        }

        fun bind(
            item: FormElement, isEnabled: Boolean, formValueListener: FormValueListener?
        ) {
            binding.form = item
            binding.invalidateAll()
            if (!isEnabled) {
                binding.et.isEnabled = false
                binding.et.isFocusable = false
                binding.et.isClickable = false
                binding.et.isFocusableInTouchMode = false
                binding.et.isLongClickable = false
                binding.executePendingBindings()
                return
            }
            val today = Calendar.getInstance()
            var thisYear = today.get(Calendar.YEAR)
            var thisMonth = today.get(Calendar.MONTH)
            var thisDay = today.get(Calendar.DAY_OF_MONTH)

            item.errorText?.also { binding.tilEditText.error = it }
                ?: run { binding.tilEditText.error = null }
            binding.et.setOnClickListener {
                KeyboardUtils.hideKeyboard(binding.et)
                KeyboardUtils.hideKeyboardFromActivity(binding.root.context)

                item.value?.let { value ->
                    val parts = value.split("[-/]".toRegex())
                    if (parts.size >= 3) {
                        thisDay = parts[0].trim().toIntOrNull() ?: thisDay
                        thisMonth = (parts[1].trim().toIntOrNull() ?: (thisMonth + 1)) - 1
                        thisYear = parts[2].trim().toIntOrNull() ?: thisYear
                    }
                }
                val formatDate: (Long) -> String? = { millis ->
                    item.dateFormat?.let { Dataset.getDateFromLong(millis, it) } ?: getDateString(millis)
                }
                val datePickerDialog = DatePickerDialog(
                    it.context, { _, year, month, day ->
                        val millis = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, day)
                        }.timeInMillis
                        item.value = when {
                            item.min != null && millis < item.min!! -> formatDate(item.min!!)
                            item.max != null && millis > item.max!! -> formatDate(item.max!!)
                            else -> formatDate(millis)
                        }
                        binding.invalidateAll()
                        if (item.hasDependants) formValueListener?.onValueChanged(item, -1)
                    }, thisYear, thisMonth, thisDay
                )
                item.errorText = null
                binding.tilEditText.error = null
                datePickerDialog.datePicker.maxDate = item.max ?: 0
                datePickerDialog.datePicker.minDate = item.min ?: 0
                if (item.showYearFirstInDatePicker)
                    datePickerDialog.datePicker.touchables[0].performClick()
                datePickerDialog.show()
            }
            binding.executePendingBindings()

        }
    }

    class TimePickerInputViewHolder private constructor(private val binding: RvItemFormTimepickerV2Binding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormTimepickerV2Binding.inflate(layoutInflater, parent, false)
                return TimePickerInputViewHolder(binding)
            }
        }

        fun bind(
            item: FormElement, isEnabled: Boolean
        ) {
            binding.form = item
            binding.et.isEnabled = isEnabled
            binding.et.setOnClickListener {
                KeyboardUtils.hideKeyboard(binding.et)
                KeyboardUtils.hideKeyboardFromActivity(binding.root.context)

                val hour: Int
                val hourOfDay: Int  // 24-hour format (0-23) for TimePickerDialog
                val minute: Int
                if (item.value == null) {
                    val currentTime = Calendar.getInstance()
                    hourOfDay = currentTime.get(Calendar.HOUR_OF_DAY)
                    minute = currentTime.get(Calendar.MINUTE)
                } else {
                    // Parse existing time value (handle both 12-hour with AM/PM and 24-hour formats)
                    val timeValue = item.value!!
                    val timeParts = timeValue.split(":")
                    if (timeParts.size >= 2) {
                        val hourStr = timeParts[0].trim()
                        val minuteAndAmPm = timeParts[1].trim()
                        val minuteStr = minuteAndAmPm.substringBefore(" ").trim()

                        val parsedHour = hourStr.toIntOrNull() ?: 0
                        val hasAmPm = timeValue.contains("AM", ignoreCase = true) ||
                                timeValue.contains("PM", ignoreCase = true) ||
                                timeValue.contains("am", ignoreCase = true) ||
                                timeValue.contains("pm", ignoreCase = true)

                        if (hasAmPm) {
                            // Already in 12-hour format with AM/PM - convert to 24-hour format
                            val isPM = timeValue.contains("PM", ignoreCase = true) ||
                                    timeValue.contains("pm", ignoreCase = true)
                            hourOfDay = when {
                                parsedHour == 12 && !isPM -> 0   // 12 AM = 0:00
                                parsedHour == 12 && isPM -> 12   // 12 PM = 12:00
                                isPM -> parsedHour + 12            // PM: add 12
                                else -> parsedHour                // AM: keep as is (except 12 AM handled above)
                            }
                        } else {
                            // Already in 24-hour format
                            hourOfDay = parsedHour
                        }
                        minute = minuteStr.toIntOrNull() ?: 0
                        Timber.d("Time picker parsed hourOfDay: $hourOfDay, minute: $minute from value: $timeValue")
                    } else {
                        // Fallback to current time if parsing fails
                        val currentTime = Calendar.getInstance()
                        hourOfDay = currentTime.get(Calendar.HOUR_OF_DAY)
                        minute = currentTime.get(Calendar.MINUTE)
                    }
                }

                val mTimePicker = TimePickerDialog(it.context, { _, selectedHourOfDay, selectedMinute ->
                    // Format time in 12-hour format with AM/PM
                    // selectedHourOfDay is in 24-hour format (0-23) even when dialog is in 12-hour mode
                    val amPm = if (selectedHourOfDay < 12) "AM" else "PM"
                    val displayHour = when {
                        selectedHourOfDay == 0 -> 12
                        selectedHourOfDay > 12 -> selectedHourOfDay - 12
                        else -> selectedHourOfDay
                    }
                    val formattedMinute = String.format("%02d", selectedMinute)
                    item.value = "$displayHour:$formattedMinute $amPm"
                    binding.invalidateAll()

                }, hourOfDay, minute, false)
                mTimePicker.setTitle("Select Time")
                mTimePicker.show()
            }
            binding.executePendingBindings()

        }
    }

    class TextViewInputViewHolder private constructor(private val binding: RvItemFormTextViewV2Binding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormTextViewV2Binding.inflate(layoutInflater, parent, false)
                return TextViewInputViewHolder(binding)
            }
        }

        fun bind(item: FormElement) {
            binding.form = item
            binding.executePendingBindings()
        }
    }

    class ImageViewInputViewHolder private constructor(private val binding: RvItemFormImageViewV2Binding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormImageViewV2Binding.inflate(layoutInflater, parent, false)
                return ImageViewInputViewHolder(binding)
            }
        }

        fun bind(
            item: FormElement, clickListener: ImageClickListener?, isEnabled: Boolean
        ) {
            binding.form = item
            if (isEnabled) {
                binding.clickListener = clickListener
                if (item.errorText != null) binding.clRi.setBackgroundResource(R.drawable.state_errored)
                else binding.clRi.setBackgroundResource(0)
            }
            binding.executePendingBindings()

        }
    }

    class AgePickerViewInputViewHolder private constructor(private val binding: RvItemFormAgePickerViewV2Binding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    RvItemFormAgePickerViewV2Binding.inflate(layoutInflater, parent, false)
                return AgePickerViewInputViewHolder(binding)
            }
        }

        fun bind(
            item: FormElement, clickListener: AgeClickListener?, isEnabled: Boolean
        ) {
            binding.form = item
            if (isEnabled) {
                binding.clickListener = clickListener
                binding.et.setOnClickListener {
                    KeyboardUtils.hideKeyboard(binding.et)
                    KeyboardUtils.hideKeyboardFromActivity(binding.root.context)
                    clickListener?.onAgeClick(item)
                }
//                if (item.errorText == null) binding.tilEditText.isErrorEnabled = false
//                Timber.d("Bound EditText item ${item.title} with ${item.required}")
//                binding.tilEditText.error = item.errorText
            }
            binding.executePendingBindings()

        }
    }

    class HeadlineViewHolder private constructor(private val binding: RvItemFormHeadlineV2Binding) :
        ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemFormHeadlineV2Binding.inflate(layoutInflater, parent, false)
                return HeadlineViewHolder(binding)
            }
        }

        fun bind(
            item: FormElement,
            formValueListener: FormValueListener?,
        ) {
            binding.form = item
            if (item.subtitle == null)
                binding.textView8.visibility = View.GONE
            formValueListener?.onValueChanged(item, -1)
            binding.executePendingBindings()

        }
    }

    class ImageClickListener(private val imageClick: (formId: Int) -> Unit) {

        fun onImageClick(form: FormElement) = imageClick(form.id)

    }

    class AgeClickListener(private val ageClick: (formId: Int) -> Unit) {

        fun onAgeClick(form: FormElement) = ageClick(form.id)

    }

    class FormValueListener(private val valueChanged: (id: Int, value: Int) -> Unit) {

        fun onValueChanged(form: FormElement, index: Int) {
            valueChanged(form.id, index)

        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inputTypes = values()
        return when (inputTypes[viewType]) {
            EDIT_TEXT -> EditTextInputViewHolder.from(parent)
            DROPDOWN -> DropDownInputViewHolder.from(parent)
            RADIO -> RadioInputViewHolder.from(parent)
            DATE_PICKER -> DatePickerInputViewHolder.from(parent)
            TEXT_VIEW -> TextViewInputViewHolder.from(parent)
            IMAGE_VIEW -> ImageViewInputViewHolder.from(parent)
            CHECKBOXES -> CheckBoxesInputViewHolder.from(parent)
            TIME_PICKER -> TimePickerInputViewHolder.from(parent)
            HEADLINE -> HeadlineViewHolder.from(parent)
            AGE_PICKER -> AgePickerViewInputViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val isEnabled = if (isEnabled) item.isEnabled else false
        when (item.inputType) {
            EDIT_TEXT -> (holder as EditTextInputViewHolder).bind(
                item, isEnabled, formValueListener
            )

            DROPDOWN -> (holder as DropDownInputViewHolder).bind(item, isEnabled, formValueListener)
            RADIO -> (holder as RadioInputViewHolder).bind(item, isEnabled, formValueListener)
            DATE_PICKER -> (holder as DatePickerInputViewHolder).bind(
                item, isEnabled, formValueListener
            )

            TEXT_VIEW -> (holder as TextViewInputViewHolder).bind(item)
            IMAGE_VIEW -> (holder as ImageViewInputViewHolder).bind(
                item, imageClickListener, isEnabled
            )

            CHECKBOXES -> (holder as CheckBoxesInputViewHolder).bind(
                item,
                isEnabled,
                formValueListener
            )

            TIME_PICKER -> (holder as TimePickerInputViewHolder).bind(item, isEnabled)
            HEADLINE -> (holder as HeadlineViewHolder).bind(item, formValueListener)
            AGE_PICKER -> (holder as AgePickerViewInputViewHolder).bind(
                item,
                ageClickListener,
                isEnabled
            )
        }
    }

    override fun getItemViewType(position: Int) = getItem(position).inputType.ordinal

    /**
     * Validation Result : -1 -> all good
     * else index of element creating trouble
     * @param recyclerView Optional RecyclerView to sync EditText values before validation
     */
    fun validateInput(resources: Resources, recyclerView: androidx.recyclerview.widget.RecyclerView? = null): Int {
        if (!isEnabled) return -1

        recyclerView?.let { syncAllEditTextValues(it) }

        clearErrorsForValidFields(resources)

        val firstErrorIndex = findFirstFieldWithError()
        if (firstErrorIndex != -1) return firstErrorIndex

        return validateRequiredFields(resources)
    }

    private fun clearErrorsForValidFields(resources: Resources) {
        val requiredError = resources.getString(R.string.form_input_empty_error)
        currentList.forEachIndexed { index, it ->
            if (it.inputType != TEXT_VIEW && it.required) {
                val trimmedValue = it.value?.trim()
                if (!trimmedValue.isNullOrBlank() && it.errorText == requiredError) {
                    it.errorText = null
                    notifyItemChanged(index)
                }
            }
        }
    }

    private fun findFirstFieldWithError(): Int {
        currentList.forEachIndexed { index, it ->
            if (it.inputType != TEXT_VIEW && it.errorText != null) {
                return index
            }
        }
        return -1
    }

    private fun validateRequiredFields(resources: Resources): Int {
        var retVal = -1
        currentList.forEachIndexed { index, it ->
            if (it.inputType != TEXT_VIEW && it.required) {
                val trimmedValue = it.value?.trim()
                if (trimmedValue.isNullOrBlank()) {
                    it.errorText = resources.getString(R.string.form_input_empty_error)
                    notifyItemChanged(index)
                    if (retVal == -1) retVal = index
                }
            }
        }
        return retVal
    }

    fun syncAllEditTextValues(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i)
            val position = recyclerView.getChildAdapterPosition(child)
            if (position != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                val formElement = getItem(position)
                if (formElement.inputType == EDIT_TEXT) {
                    val editText = child.findViewById<android.widget.EditText>(R.id.et)
                    if (editText != null) {
                        val currentText = editText.text?.toString() ?: ""
                        if (currentText != formElement.value) {
                            formElement.value = currentText
                        }
                    }
                }
            }
        }
    }
}
