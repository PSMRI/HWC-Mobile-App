package org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.IllnessAdapter
import org.piramalswasthya.cho.adapter.SubCategoryAdapter
import org.piramalswasthya.cho.databinding.FragmentIllnessFieldsBinding
import org.piramalswasthya.cho.model.IllnessDropdown
import org.piramalswasthya.cho.model.SubVisitCategory
import org.piramalswasthya.cho.ui.HistoryFieldsInterface
import org.piramalswasthya.cho.ui.commons.fhir_visit_details.VisitDetailViewModel

@AndroidEntryPoint
class IllnessFieldsFragment(): Fragment() {

    private val TimePeriodAgo = arrayOf(
        "Day(s)",
        "Week(s)",
        "Month(s)",
        "Year(s)"
    )
    private var _binding: FragmentIllnessFieldsBinding? = null
    private val binding: FragmentIllnessFieldsBinding
        get() = _binding!!

    val viewModel: IllnessFieldViewModel by viewModels()
    private lateinit var dropdownTimePeriodAgo: AutoCompleteTextView
    private var historyListener: HistoryFieldsInterface? = null
    private var illnessOption = ArrayList<IllnessDropdown>()
    private lateinit var illnessAdapter: IllnessAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIllnessFieldsBinding.inflate(inflater, container,false)
       return binding.root
    }
    private var fragmentTag :String? = null
    fun setFragmentTag(tag:String){
        fragmentTag = tag
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Aryan","HIIII")
        val illnessAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item)
        //AlcoholAdapter(requireContext(), R.layout.drop_down,alcoholOption)
        binding.illnessText.setAdapter(illnessAdapter)

        viewModel.illnessDropdown.observe( viewLifecycleOwner) { alc ->
//            alcoholOption.clear()
//            alcoholOption.addAll(alc)
            illnessAdapter.clear()
            illnessAdapter.addAll(alc.map { it.illnessType })
            illnessAdapter.notifyDataSetChanged()
        }

        dropdownTimePeriodAgo = binding.dropdownDurUnit
        val timePeriodAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line,TimePeriodAgo)
        dropdownTimePeriodAgo.setAdapter(timePeriodAdapter)


        binding.deleteButton.setOnClickListener {
            fragmentTag?.let {
                historyListener?.onDeleteButtonClickedIllness(it)
            }
        }
        binding.plusButton.setOnClickListener {
            fragmentTag?.let {
                historyListener?.onAddButtonClickedIllness(it)
            }
        }

        binding.plusButton.isEnabled = false
        binding.resetButton.isEnabled = false
        binding.dropdownDurUnit.addTextChangedListener(inputTextWatcher)
        binding.inputDuration.addTextChangedListener(inputTextWatcher)
        binding.illnessText.addTextChangedListener(inputTextWatcher)
        binding.resetButton.setOnClickListener {
            binding.dropdownDurUnit.text?.clear()
            binding.inputDuration.text?.clear()
            binding.illnessText.text?.clear()
        }
    }

    fun setListener(listener: HistoryFieldsInterface) {
        this.historyListener = listener
    }

    private val inputTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // No action needed
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            updateAddAndResetButtonState()
        }

        override fun afterTextChanged(s: Editable?) {
            // No action needed
        }
    }

    private fun updateAddAndResetButtonState() {
        val durationUnit = binding.dropdownDurUnit.text.toString().trim()
        val duration = binding.inputDuration.text.toString().trim()
        val chiefComplaint = binding.illnessText.text.toString().trim()
        binding.plusButton.isEnabled = durationUnit.isNotEmpty()&&duration.isNotEmpty()&&chiefComplaint.isNotEmpty()
        binding.resetButton.isEnabled = durationUnit.isNotEmpty()&&duration.isNotEmpty()&&chiefComplaint.isNotEmpty()
    }

}