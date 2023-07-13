package org.piramalswasthya.cho.ui.commons.fhir_add_patient

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater

import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint

import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentFhirAddPatientBinding
import org.piramalswasthya.cho.ui.commons.FhirFragmentService
import org.piramalswasthya.cho.ui.commons.FhirQuestionnaireService
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.home_activity.HomeActivity


/** A fragment class to show patient registration screen. */

@AndroidEntryPoint
class FhirAddPatientFragment : Fragment(R.layout.fragment_fhir_add_patient), FhirFragmentService, NavigationAdapter {

    private var _binding: FragmentFhirAddPatientBinding? = null

    private val binding: FragmentFhirAddPatientBinding
        get() = _binding!!

    override val viewModel: FhirAddPatientViewModel by viewModels()

    override var fragment: Fragment = this;

    override var fragmentContainerId = 0;

    override val jsonFile : String = "new-patient-registration-paginated.json"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFhirAddPatientBinding.inflate(layoutInflater, container, false)
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
    }

    override fun getFragmentId(): Int {
        return R.id.fragment_fhir_add_patient;
    }

    override fun onSubmitAction() {
        saveEntity()
        navigateNext()
    }

    override fun onCancelAction() {
        val intent = Intent(context, HomeActivity::class.java)
        startActivity(intent)
    }

    override fun navigateNext() {
        var pat = viewModel.pat
        findNavController().navigate(
            FhirAddPatientFragmentDirections.actionFhirAddPatientFragmentToFhirLocationFragment(pat)
        )
    }

}
