package org.piramalswasthya.cho.ui.commons.fhir_visit_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.ExtraChiefComplaintLayoutBinding
import org.piramalswasthya.cho.model.ChiefComplaint
import org.piramalswasthya.cho.model.ChiefComplaintMaster

@AndroidEntryPoint
class ChiefComplaintFragment(private var chiefComplaintList: List<ChiefComplaintMaster>,
                             private var units: List<String>) : Fragment() {
    private var _binding: ExtraChiefComplaintLayoutBinding? = null
    private lateinit var chiefComplaintAdapter : ChiefComplaintAdapter

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
    }
}