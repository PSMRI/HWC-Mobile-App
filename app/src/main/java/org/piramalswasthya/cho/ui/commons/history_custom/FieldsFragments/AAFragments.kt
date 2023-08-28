package org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentAAFragmentsBinding
import org.piramalswasthya.cho.ui.HistoryFieldsInterface
import java.util.Arrays

@AndroidEntryPoint
class AAFragments : Fragment() {

    private val TimePeriodAgo = arrayOf(
        "Days",
        "Weeks",
        "Months",
        "Years"
    )
    private var _binding: FragmentAAFragmentsBinding? = null
    private val binding: FragmentAAFragmentsBinding
        get() = _binding!!

    private lateinit var dropdownAA: AutoCompleteTextView
    private lateinit var dropdownTimePeriodAgo: AutoCompleteTextView
    val viewModel: AssociatedAilmentsViewModel by viewModels()
    private var historyListener: HistoryFieldsInterface? = null
    private val selectedFamilyMembers = mutableListOf<Int>()
    var familyM: MaterialCardView? = null
    var selectF: TextView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAAFragmentsBinding.inflate(inflater, container,false)
        return binding.root
    }
    private var fragmentTag :String? = null
    fun setFragmentTag(tag:String){
        fragmentTag = tag
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        familyM = binding.familyM
        selectF = binding.selectF

        familyM!!.setOnClickListener {
            showFamilyDialog()
        }

        dropdownAA = binding.aaText
        dropdownTimePeriodAgo = binding.dropdownDurUnit

        val timePeriodAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line,TimePeriodAgo)
        dropdownTimePeriodAgo.setAdapter(timePeriodAdapter)

        val aaAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item)
        binding.aaText.setAdapter(aaAdapter)

        viewModel.associateAilmentsDropdown.observe( viewLifecycleOwner) { aa ->
            aaAdapter.clear()
            aaAdapter.addAll(aa.map { it.assocateAilments })
            aaAdapter.notifyDataSetChanged()
        }

        binding.deleteButton.setOnClickListener {
            fragmentTag?.let {
                historyListener?.onDeleteButtonClickedAA(it)
            }
        }
        binding.plusButton.setOnClickListener {
            fragmentTag?.let {
                historyListener?.onAddButtonClickedAA(it)
            }
        }

        binding.plusButton.isEnabled = false
        binding.resetButton.isEnabled = false
        binding.dropdownDurUnit.addTextChangedListener(inputTextWatcher)
        binding.inputDuration.addTextChangedListener(inputTextWatcher)
        binding.aaText.addTextChangedListener(inputTextWatcher)
        binding.resetButton.setOnClickListener {
            binding.dropdownDurUnit.text?.clear()
            binding.inputDuration.text?.clear()
            binding.aaText.text?.clear()
        }

        binding.aaText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Check if the selected item is "Other"
                val selectedOption = s.toString()
                val isOtherSelected = selectedOption.equals("Other", ignoreCase = true)

                // Show or hide the otherTextField based on the selection
                if (isOtherSelected) {
                    // Show the otherTextField
                    binding.otherTextFieldLayout.visibility = View.VISIBLE
                } else {
                    // Hide the otherTextField
                    binding.otherTextFieldLayout.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }


    private fun showFamilyDialog() {
        val benRelationTypeList = mutableListOf<String>()
        viewModel.familyDropdown.observe(viewLifecycleOwner) { familyMembers ->
            benRelationTypeList.clear()
            benRelationTypeList.addAll(familyMembers.map { it.benRelationshipType })
            showDialogWithFamilyMembers(benRelationTypeList)
        }
    }

    private fun showDialogWithFamilyMembers(familyMembers: List<String>) {
        val selectedItems = BooleanArray(familyMembers.size) { selectedFamilyMembers.contains(it) }

        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Select Family Member")
            .setCancelable(false)
            .setMultiChoiceItems(
                familyMembers.toTypedArray(),
                selectedItems
            ) { _, which, isChecked ->
                if (isChecked) {
                    selectedFamilyMembers.add(which)
                } else {
                    selectedFamilyMembers.remove(which)
                }
            }
            .setPositiveButton("Ok") { dialog, which ->
                val selectedRelationTypes = selectedFamilyMembers.map { familyMembers[it] }
                val selectedRelationTypesString = selectedRelationTypes.joinToString(", ")
                binding.selectF.text = selectedRelationTypesString
                binding.selectF.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black))
            }
            .setNeutralButton("Clear all") { dialog, which ->
                selectedFamilyMembers.clear()
                Arrays.fill(selectedItems, false)
                val listView = (dialog as? AlertDialog)?.listView
                listView?.clearChoices()
                listView?.requestLayout()
                binding.selectF.text = resources.getString(R.string.select_f_h_of_member)
                binding.selectF.setTextColor(ContextCompat.getColor(binding.root.context, R.color.defaultInput))
            }

        val alertDialog = builder.create()
        alertDialog.show()
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
        val chiefComplaint = binding.aaText.text.toString().trim()
        val familym = binding.inputDuration.text.toString().trim()
        val inC = binding.aaText.text.toString().trim()
        binding.plusButton.isEnabled = durationUnit.isNotEmpty()&&duration.isNotEmpty()&&chiefComplaint.isNotEmpty()&&familym.isNotEmpty()&&inC.isNotEmpty()
        binding.resetButton.isEnabled = durationUnit.isNotEmpty()&&duration.isNotEmpty()&&chiefComplaint.isNotEmpty()&&familym.isNotEmpty()&&inC.isNotEmpty()

    }

}