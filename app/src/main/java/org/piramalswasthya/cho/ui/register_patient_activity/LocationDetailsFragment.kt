package org.piramalswasthya.cho.ui.register_patient_activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.piramalswasthya.cho.databinding.FragmentLocationHostBinding
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.location_fragment.FhirLocationFragment

class LocationDetailsFragment: Fragment() {
    private var _binding: FragmentLocationHostBinding? = null
    private val binding: FragmentLocationHostBinding
        get() {
           return _binding!!
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLocationHostBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fhirPatientLocationFragment = FhirLocationFragment()
        childFragmentManager.beginTransaction().replace(binding.patientLocation.id,fhirPatientLocationFragment).commit()
        binding.btnSubmit.setOnClickListener {
            findNavController().navigate(
                LocationDetailsFragmentDirections.actionLocationDetailsFragmentToOtherDetailsFragment()
            )
        }
        binding.btnCancel.setOnClickListener {
            findNavController().navigate(
                LocationDetailsFragmentDirections.actionLocationDetailsFragmentToRegisterPatientFragment()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}