package org.piramalswasthya.cho.ui.register_patient_activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentRegisterPatientBinding
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.FhirAddPatientFragment
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.location_fragment.FhirLocationFragment

class RegisterPatientFragment : Fragment() {

    private var _binding: FragmentRegisterPatientBinding? = null
    private val binding: FragmentRegisterPatientBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterPatientBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentAddPatient = FhirAddPatientFragment()
        childFragmentManager.beginTransaction().replace(binding.patientRegistration.id, fragmentAddPatient).commit()
        binding.btnCancel.setOnClickListener {
            findNavController().navigate(RegisterPatientFragmentDirections.actionRegisterPatientFragmentToHomeFragment())
        }
        binding.btnSubmit.setOnClickListener {
//            fragmentAddPatient.onSubmitAction()
            findNavController().navigate(RegisterPatientFragmentDirections.actionRegisterPatientFragmentToLocationDetailsFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}