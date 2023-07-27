package org.piramalswasthya.cho.ui.commons.fhir_patient_vitals

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.fhir.datacapture.QuestionnaireFragment
import dagger.hilt.android.AndroidEntryPoint
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentFhirAddPatientBinding
import org.piramalswasthya.cho.databinding.FragmentFhirVitalsBinding
import org.piramalswasthya.cho.ui.commons.FhirFragmentService
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.FhirAddPatientFragment
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.FhirAddPatientViewModel
//import org.piramalswasthya.cho.ui.commons.fhir_visit_details.FhirVisitDetailsFragmentDirections
import timber.log.Timber

@AndroidEntryPoint
class FhirVitalsFragment : Fragment(R.layout.fragment_fhir_vitals), FhirFragmentService, NavigationAdapter {

    private var _binding: FragmentFhirVitalsBinding? = null

    private val binding: FragmentFhirVitalsBinding
        get() = _binding!!

    override val viewModel: FhirVitalsViewModel by viewModels()

    override var fragment: Fragment = this;

    override var fragmentContainerId = 0;

    override val jsonFile : String = "vitals-page.json"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFhirVitalsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentContainerId = binding.fragmentContainer.id
        updateArguments()
        if (savedInstanceState == null) {
            addQuestionnaireFragment()
        }
        observeEntitySaveAction("Inputs are missing.", "Vitals is saved.")
    }

    override fun getFragmentId(): Int {
        return R.id.fragment_fhir_vitals;
    }

    override fun onSubmitAction() {
        navigateNext()
    }

    override fun onCancelAction() {
//        findNavController().navigate(
//            FhirVitalsFragmentDirections.actionFhirVitalsFragmentToFhirVisitDetailsFragment()
//        )
        findNavController().navigateUp()
    }

    override fun navigateNext() {
        findNavController().navigate(
            FhirVitalsFragmentDirections.actionFhirVitalsFragmentToFhirPrescriptionFragment()
        )
    }

}