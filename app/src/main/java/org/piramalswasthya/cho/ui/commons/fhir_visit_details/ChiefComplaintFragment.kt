package org.piramalswasthya.cho.ui.commons.fhir_visit_details

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.ExtraChiefComplaintLayoutBinding
import org.piramalswasthya.cho.model.ChiefComplaintMaster
import org.piramalswasthya.cho.ui.ChiefComplaintInterface

@AndroidEntryPoint
class ChiefComplaintFragment(private var chiefComplaintList: List<ChiefComplaintMaster>,
                             private var units: List<String>, private var linearLayout: LinearLayout) : Fragment() {
    private var _binding: ExtraChiefComplaintLayoutBinding? = null
    private lateinit var chiefComplaintAdapter : ChiefComplaintAdapter
    private var chiefComplaintListener: ChiefComplaintInterface? = null
    private val binding: ExtraChiefComplaintLayoutBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ExtraChiefComplaintLayoutBinding.inflate(inflater,container,false)
        return binding.root
    }
    private var fragmentTag: String? = null

    // Function to set the fragment's tag when adding it to the FragmentManager
    fun setFragmentTag(tag: String) {
        fragmentTag = tag
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chiefComplaintAdapter = ChiefComplaintAdapter(requireContext(), R.layout.drop_down, chiefComplaintList,binding.chiefComplaintDropDowns)
        binding.chiefComplaintDropDowns.setAdapter(chiefComplaintAdapter)
        binding.chiefComplaintDropDowns.setOnItemClickListener { parent, view, position, id ->
            var chiefComplaint = parent.getItemAtPosition(position) as ChiefComplaintMaster
            binding.chiefComplaintDropDowns.setText(chiefComplaint?.chiefComplaint,false)
        }
        binding.dropdownDurUnit.setAdapter(ArrayAdapter(requireContext(),R.layout.drop_down,units))
        binding.dropdownDurUnit.setOnItemClickListener { parent, _, position, _ ->
            var unit = parent.getItemAtPosition(position) as String
            binding.dropdownDurUnit.setText(unit,false)
        }
        binding.deleteButton.setOnClickListener {
            fragmentTag?.let {
                chiefComplaintListener?.onDeleteButtonClicked(it)
            }
        }
        linearLayout.addOnLayoutChangeListener { view, i, i2, i3, i4, i5, i6, i7, i8 ->
            var count = linearLayout.childCount
            binding.deleteButton.isEnabled = count > 1
        }
        binding.resetButton.isEnabled = false
        binding.descInputText.addTextChangedListener(inputTextWatcher)
        binding.dropdownDurUnit.addTextChangedListener(inputTextWatcher)
        binding.inputDuration.addTextChangedListener(inputTextWatcher)
        binding.chiefComplaintDropDowns.addTextChangedListener(inputTextWatcher)
        binding.resetButton.setOnClickListener {
            binding.descInputText.text?.clear()
            binding.dropdownDurUnit.text?.clear()
            binding.inputDuration.text?.clear()
            binding.chiefComplaintDropDowns.text?.clear()
        }
    }

    fun setListener(listener: ChiefComplaintInterface) {
        this.chiefComplaintListener = listener
    }

    private val inputTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // No action needed
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            updateResetButtonState()
        }

        override fun afterTextChanged(s: Editable?) {
            // No action needed
        }
    }
    private fun updateResetButtonState() {
        val description = binding.descInputText.text.toString().trim()
        val durationUnit = binding.dropdownDurUnit.text.toString().trim()
        val duration = binding.inputDuration.text.toString().trim()
        val chiefComplaint = binding.chiefComplaintDropDowns.text.toString().trim()

        binding.resetButton.isEnabled = description.isNotEmpty() ||
                durationUnit.isNotEmpty() ||
                duration.isNotEmpty() ||
                chiefComplaint.isNotEmpty()
    }
}