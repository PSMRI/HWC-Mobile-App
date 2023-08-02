package org.piramalswasthya.cho.ui.commons.fhir_visit_details

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.ExtraChiefComplaintLayoutBinding
import org.piramalswasthya.cho.model.ChiefComplaint
import org.piramalswasthya.cho.model.ChiefComplaintMaster
import org.piramalswasthya.cho.ui.ChiefComplaintInterface

@AndroidEntryPoint
class ChiefComplaintFragment(private var chiefComplaintList: List<ChiefComplaintMaster>,
                             private var units: List<String>) : Fragment() {
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
        val parentFragment = parentFragment as? FragmentVisitDetail
        val countValue = parentFragment?.clickedCount ?: 0
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
//            chiefComplaintListener?.counter(-1)
        }
        binding.plusButton.setOnClickListener {
            fragmentTag?.let {
                chiefComplaintListener?.onAddButtonClicked(it)
            }
            chiefComplaintListener?.counter(1)
        }

        binding.plusButton.isEnabled = false
        binding.descInputText.addTextChangedListener(inputTextWatcher)
        binding.resetButton.setOnClickListener {
            binding.descInputText.text?.clear()
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
            updateAddButtonState()
        }

        override fun afterTextChanged(s: Editable?) {
            // No action needed
        }
    }

    private fun updateAddButtonState() {
        val description = binding.descInputText.text.toString().trim()
        binding.plusButton.isEnabled = description.isNotEmpty()
    }


}