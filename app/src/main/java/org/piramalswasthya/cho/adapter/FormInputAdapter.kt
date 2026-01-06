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
            Timber.d("${oldItem.id}   ${oldItem.errorText} ${newItem.errorText}")
            return oldItem.errorText == newItem.errorText
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
                binding.et.isClickable = false
                binding.et.isFocusable = false
                handleHintLength(item)
                binding.form = item
                binding.et.setText(item.value)
                binding.executePendingBindings()
                return
            } else {
                binding.et.isClickable = true
                binding.et.isFocusable = true
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


            //binding.et.setText(item.value.value)
            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(editable: Editable?) {
//                    editable?.length?.let {
//                        if (it > item.etMaxLength) {
////                            editable.delete(item.etMaxLength + 1, it)
//                            "This field cannot have more than ${item.etMaxLength} characters".let {
//                                item.errorText = it
//                                binding.tilEditText.error = it
//                            }
//                            return
//                        } else
//                            item.errorText = null
//                    }
                    item.value = editable?.toString()
                    Timber.d("editable : $editable Current value : ${item.value}  isNull: ${item.value == null} isEmpty: ${item.value == ""}")
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
            if (!isEnabled) {
                binding.rg.isClickable = false
                binding.rg.isFocusable = false
            }
//            binding.rg.isEnabled = isEnabled
            binding.invalidateAll()
            binding.form = item

            binding.rg.removeAllViews()

            binding.rg.apply {
                item.entries?.let { items ->
                    orientation = item.orientation ?: LinearLayout.HORIZONTAL
                    weightSum = items.size.toFloat()
                    items.forEach {
                        val rdBtn = RadioButton(this.context)
                        rdBtn.layoutParams = RadioGroup.LayoutParams(
                            RadioGroup.LayoutParams.WRAP_CONTENT,
                            RadioGroup.LayoutParams.WRAP_CONTENT,
                            1.0F
                        ).apply {
                            gravity = Gravity.CENTER_HORIZONTAL
                        }
                        rdBtn.id = View.generateViewId()
                        val colorStateList = ColorStateList(
                            arrayOf<IntArray>(
                                intArrayOf(-android.R.attr.state_checked),
                                intArrayOf(android.R.attr.state_checked)
                            ), intArrayOf(
                                binding.root.resources.getColor(
                                    android.R.color.darker_gray,
                                    binding.root.context.theme
                                ),  // disabled
                                binding.root.resources.getColor(
                                    android.R.color.darker_gray,
                                    binding.root.context.theme
                                ) // enabled
                            )
                        )

                        if (!isEnabled) rdBtn.buttonTintList = colorStateList
                        rdBtn.text = it
                        addView(rdBtn)
                        if (item.value == it) rdBtn.isChecked = true
                        rdBtn.setOnCheckedChangeListener { _, b ->
                            if (b) {
                                item.value = it
                                if (item.hasDependants || item.hasAlertError) {
                                    Timber.d(
                                        "listener trigger : ${item.id} ${
                                            item.entries!!.indexOf(
                                                it
                                            )
                                        } $it"
                                    )
                                    formValueListener?.onValueChanged(
                                        item, item.entries!!.indexOf(it)
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
                }
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
                        if (!isEnabled) {
                            cbx.isClickable = false
                            cbx.isFocusable = false
                        }
                        cbx.id = View.generateViewId()
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) cbx.setTextAppearance(
                            context, android.R.style.TextAppearance_Material_Medium
                        )
                        else cbx.setTextAppearance(android.R.style.TextAppearance_Material_Subhead)
                        cbx.text = it
                        addView(cbx)
                        if (item.value?.contains(it) == true) cbx.isChecked = true
                        cbx.setOnCheckedChangeListener { _, b ->
                            if (b) {
                                if (item.value != null) item.value = item.value + it
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
                                    item.value = item.value?.replace(it, "")
                                }
                            }
                            formValueListener?.onValueChanged(
                                item, index * (if (b) 1 else -1)
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
                binding.et.isFocusable = false
                binding.et.isClickable = false
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
                    thisYear = value.substring(6).toInt()
                    thisMonth = value.substring(3, 5).trim().toInt() - 1
                    thisDay = value.substring(0, 2).trim().toInt()
                }
                val datePickerDialog = DatePickerDialog(
                    it.context, { _, year, month, day ->
                        val millis = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, day)
                        }.timeInMillis
                        if (item.min != null && millis < item.min!!) {
                            item.value = getDateString(item.min)
                        } else if (item.max != null && millis > item.max!!)
                            item.value = getDateString(item.max)
                        else
                            item.value = getDateString(millis)
//                            "${if (day > 9) day else "0$day"}-${if (month > 8) month + 1 else "0${month + 1}"}-$year"
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
                val minute: Int
                if (item.value == null) {
                    val currentTime = Calendar.getInstance()
                    hour = currentTime.get(Calendar.HOUR_OF_DAY)
                    minute = currentTime.get(Calendar.MINUTE)
                } else {
                    hour = item.value!!.substringBefore(":").toInt()
                    minute = item.value!!.substringAfter(":").toInt()
                    Timber.d("Time picker hour min : $hour $minute")
                }
                val mTimePicker = TimePickerDialog(it.context, { _, hourOfDay, minuteOfHour ->
                    item.value = "$hourOfDay:$minuteOfHour"
                    binding.invalidateAll()

                }, hour, minute, false)
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
     */
    fun validateInput(resources: Resources): Int {
        var retVal = -1
        if (!isEnabled) return retVal
        currentList.forEachIndexed { index, it ->
            Timber.d("Error text for ${it.title} ${it.errorText}")
            if (it.inputType != TEXT_VIEW && it.errorText != null) {
                retVal = index
                return@forEachIndexed
            }
        }
        Timber.d("Validation : $retVal")
        if (retVal != -1) return retVal
        currentList.forEachIndexed { index, it ->
            if (it.inputType != TEXT_VIEW && it.required) {
                if (it.value.isNullOrBlank()) {
                    Timber.d("validateInput called for item $it, with index ${index}")
                    it.errorText = resources.getString(R.string.form_input_empty_error)
                    notifyItemChanged(index)
                    if (retVal == -1) retVal = index
                }
            }
            /*            if(it.regex!=null){
                            Timber.d("Regex not null")
                            retVal= false
                        }*/
        }
        return retVal
    }
}