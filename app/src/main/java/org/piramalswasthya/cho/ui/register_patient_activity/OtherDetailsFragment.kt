package org.piramalswasthya.cho.ui.register_patient_activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.piramalswasthya.cho.databinding.FragmentOtherDetailsHostBinding
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.other_information.FhirOtherInformationFragment

class OtherDetailsFragment: Fragment() {
    private var _binding: FragmentOtherDetailsHostBinding? = null

    private val binding : FragmentOtherDetailsHostBinding
        get() {
           return _binding!!
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOtherDetailsHostBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fhirOtherDetailFragment = FhirOtherInformationFragment()
        childFragmentManager.beginTransaction().replace(binding.patientOtherDetails.id,fhirOtherDetailFragment).commit()

        binding.btnCancel.setOnClickListener {
            findNavController().navigate(
                OtherDetailsFragmentDirections.actionOtherDetailsFragmentToLocationDetailsFragment()
            )
        }
        binding.btnSubmit.setOnClickListener {
            findNavController().navigate(
                OtherDetailsFragmentDirections.actionOtherDetailsFragmentToHomeFragment()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}