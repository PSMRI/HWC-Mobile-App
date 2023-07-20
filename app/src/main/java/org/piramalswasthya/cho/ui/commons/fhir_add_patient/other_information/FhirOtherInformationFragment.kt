package org.piramalswasthya.cho.ui.commons.fhir_add_patient.other_information

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.hl7.fhir.r4.model.Patient
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentFhirOtherInformationBinding
import org.piramalswasthya.cho.ui.commons.FhirFragmentService
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.home_activity.HomeActivity

@AndroidEntryPoint
class FhirOtherInformationFragment : Fragment(R.layout.fragment_fhir_other_information), FhirFragmentService, NavigationAdapter{

    private var _binding: FragmentFhirOtherInformationBinding? = null

    private val binding: FragmentFhirOtherInformationBinding
        get() = _binding!!

    override val viewModel: FhirOtherInformationViewModel by viewModels()

    override var fragment: Fragment = this;

    override var fragmentContainerId = 0;

    override val jsonFile : String = "other_information.json"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFhirOtherInformationBinding.inflate(layoutInflater, container, false)
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
//        viewModel.patient = FhirOtherInformationFragmentArgs.fromBundle(requireArguments()).patientDetails
    }

    override fun getFragmentId(): Int {
        return R.id.fragment_fhir_add_patient_other_details;
    }

    override fun onSubmitAction() {
        saveEntity()
//        navigateNext()
    }

    override fun onCancelAction() {
//        findNavController().navigate(
//            FhirOtherInformationFragmentDirections.actionFhirOtherInformationFragmentToFhirLocationFragment()
//        )
    }

    override fun navigateNext() {
        val intent = Intent(context, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}