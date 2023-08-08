package org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.AllergyAdapter
import org.piramalswasthya.cho.adapter.IllnessAdapter
import org.piramalswasthya.cho.databinding.FragmentAllergyBinding
import org.piramalswasthya.cho.databinding.FragmentIllnessFieldsBinding
import org.piramalswasthya.cho.model.AllergicReactionDropdown
import org.piramalswasthya.cho.model.IllnessDropdown
import org.piramalswasthya.cho.ui.HistoryFieldsInterface

@AndroidEntryPoint
class AllergyFragment : Fragment() {
    private val allergyStatus = arrayOf(
        "Yes",
        "No",
        "Discontinued"
    )
    private val allergyType = arrayOf(
      "Drugs","Food","Environmental"
    )

    private var _binding: FragmentAllergyBinding? = null
    private val binding: FragmentAllergyBinding
        get() = _binding!!

    private lateinit var dropdownAStatus: AutoCompleteTextView
    private lateinit var dropdownAType: AutoCompleteTextView
    private var historyListener: HistoryFieldsInterface? = null
    private var allergyOption = ArrayList<AllergicReactionDropdown>()
    private lateinit var allergyAdapter: AllergyAdapter
    val viewModel: AllergyFieldViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAllergyBinding.inflate(inflater, container,false)
        return binding.root
    }
    private var fragmentTag :String? = null
    fun setFragmentTag(tag:String){
        fragmentTag = tag
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dropdownAStatus = binding.allergySText
        dropdownAType = binding.allergyTText

        val AStatusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, allergyStatus)
        dropdownAStatus.setAdapter(AStatusAdapter)
        val ATypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, allergyType)
        dropdownAType.setAdapter(ATypeAdapter)
        allergyAdapter = AllergyAdapter(requireContext(), R.layout.drop_down,allergyOption)
        binding.allergRText.setAdapter(allergyAdapter)

        viewModel.allergyDropdown.observe( viewLifecycleOwner) { allg ->
            allergyOption.clear()
            allergyOption.addAll(allg)
            allergyAdapter.notifyDataSetChanged()
        }


        binding.deleteButton.setOnClickListener {
            fragmentTag?.let {
                historyListener?.onDeleteButtonClickedAlg(it)
            }
        }
        binding.plusButton.setOnClickListener {
            fragmentTag?.let {
                historyListener?.onAddButtonClickedAlg(it)
            }
        }

        binding.plusButton.isEnabled = false
        binding.resetButton.isEnabled = false
        binding.allergySText.addTextChangedListener(inputTextWatcher)
        binding.allergyTText.addTextChangedListener(inputTextWatcher)
        binding.inputAlgName.addTextChangedListener(inputTextWatcher)
        binding.allergRText.addTextChangedListener(inputTextWatcher)
        binding.geneticDText.addTextChangedListener(inputTextWatcher)
        binding.resetButton.setOnClickListener {
            binding.allergySText.text?.clear()
            binding.allergyTText.text?.clear()
            binding.inputAlgName.text?.clear()
            binding.allergRText.text?.clear()
            binding.geneticDText.text?.clear()
        }

        binding.allergRText.addTextChangedListener(object : TextWatcher {
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
        val allgStatus = binding.allergySText.text.toString().trim()
        val allgType = binding.allergyTText.text.toString().trim()
        val allgName = binding.inputAlgName.text.toString().trim()
        val typeAllg = binding.inputAlgName.text.toString().trim()
        val genD = binding.inputAlgName.text.toString().trim()
        binding.plusButton.isEnabled = allgStatus.isNotEmpty()&&allgType.isNotEmpty()&&allgName.isNotEmpty()&&typeAllg.isNotEmpty()&&genD.isNotEmpty()
        binding.resetButton.isEnabled = allgStatus.isNotEmpty()&&allgType.isNotEmpty()&&allgName.isNotEmpty()&&typeAllg.isNotEmpty()&&genD.isNotEmpty()
    }


}