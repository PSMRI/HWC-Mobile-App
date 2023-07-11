package org.piramalswasthya.cho.ui.commons.fhir_add_patient.location_fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
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
    }

    override fun onCancelAction() {

    }

    override fun navigateNext() {
        Log.i("navigate up Pressed", "asdsad")
    }
}