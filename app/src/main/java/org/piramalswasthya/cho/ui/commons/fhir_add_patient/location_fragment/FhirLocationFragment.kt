package org.piramalswasthya.cho.ui.commons.fhir_add_patient.location_fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R

import org.piramalswasthya.cho.databinding.FragmentFhirLocationBinding
import org.piramalswasthya.cho.ui.commons.FhirFragmentService
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.FhirLocationViewModel

@AndroidEntryPoint
class FhirLocationFragment : Fragment(R.layout.fragment_fhir_location), FhirFragmentService, NavigationAdapter{

    private var _binding: FragmentFhirLocationBinding? = null

    private val binding: FragmentFhirLocationBinding
        get() = _binding!!

    override val viewModel: FhirLocationViewModel by viewModels()

    override var fragment: Fragment = this;

    override var fragmentContainerId = 0;

    override val jsonFile : String = "location_information.json"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFhirLocationBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentContainerId = binding.fragmentContainer.id
        updateArguments()
        if (savedInstanceState == null) {
            addQuestionnaireFragment()
        }
        observeEntitySaveAction("Inputs are missing.", "Patient is saved.")
//        viewModel.patient = FhirLocationFragmentArgs.fromBundle(requireArguments()).patientDetails
    }

    override fun getFragmentId(): Int {
        return R.id.fragment_fhir_add_patient_location;
    }

    override fun onSubmitAction() {
        saveEntity()
//        navigateNext()
    }

    override fun onCancelAction() {
        findNavController().navigate(
            FhirLocationFragmentDirections.actionFhirLocationFragmentToFhirAddPatientFragment()
        )
    }

    override fun navigateNext() {
//        var locationDetails = viewModel.registerLocation
//        var patient = Patient()
//        var address = Address()
//        address.city = locationDetails.city
//        address.state = locationDetails.state
//        var addressList : List<Address>
//        addressList = mutableListOf(address)
//        patient.gender = pat.gender
//        patient.name = pat.name
//        patient.contact = pat.contact
//        patient.birthDate = pat.birthDate
//        patient.address = addressList
        findNavController().navigate(
            FhirLocationFragmentDirections.actionFhirLocationFragmentToFhirOtherInformationFragment()
        )
    }
}