package org.piramalswasthya.cho.ui.ophthalmic_screening

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentOphthalmicScreeningBinding
import org.piramalswasthya.cho.ui.commons.DropdownConst

@AndroidEntryPoint
class OphthalmicScreeningFragment : Fragment() {

    private val viewModel: OphthalmicScreeningViewModel by viewModels()
    private lateinit var binding: FragmentOphthalmicScreeningBinding
    private val args: OphthalmicScreeningFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_ophthalmic_screening, container, false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDropdowns()
        setupClickListeners()
        setupObservers()

        val patientID = args.patientID // Assuming non-nullable in nav graph
        val benVisitNo = args.benVisitNo
        if (patientID != null) {
            viewModel.loadOphthalmicVisit(patientID, benVisitNo)
        }
    }

    private fun setupDropdowns() {
        val vaValues = DropdownConst.visualAcuityList
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, vaValues)

        binding.actvDistVaRight.setAdapter(adapter)
        binding.actvDistVaLeft.setAdapter(adapter)

        binding.actvDistVaRight.setOnItemClickListener { _, _, position, _ ->
            viewModel.setDistVARight(vaValues[position])
        }

        binding.actvDistVaLeft.setOnItemClickListener { _, _, position, _ ->
            viewModel.setDistVALeft(vaValues[position])
        }
    }

    private fun setupClickListeners() {
        binding.btnNext.setOnClickListener {
            viewModel.save {
                Toast.makeText(requireContext(), "Saved successfully", Toast.LENGTH_SHORT).show()
                val benVisitInfo = args.benVisitInfo
                if (benVisitInfo != null) {
                    findNavController().navigate(
                        OphthalmicScreeningFragmentDirections.actionOphthalmicScreeningFragmentToFhirVisitDetailsFragment(
                            benVisitInfo
                        )
                    )
                } else {
                    Toast.makeText(requireContext(), "Error returning to details", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun setupObservers() {
        // Observe any specific events if needed
    }
}
