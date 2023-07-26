package org.piramalswasthya.cho.ui.commons.fhir_revisit_form

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.fhir.datacapture.QuestionnaireFragment
import dagger.hilt.android.AndroidEntryPoint
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentFhirRevisitFormBinding
import org.piramalswasthya.cho.databinding.FragmentFhirVitalsBinding
import org.piramalswasthya.cho.ui.commons.FhirFragmentService
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.fhir_patient_vitals.FhirVitalsFragmentDirections
import org.piramalswasthya.cho.ui.commons.fhir_patient_vitals.FhirVitalsViewModel
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 * Use the [FhirRevisitFormFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class FhirRevisitFormFragment : Fragment(R.layout.fragment_fhir_revisit_form), FhirFragmentService, NavigationAdapter {

    private var _binding: FragmentFhirRevisitFormBinding? = null

    private val binding: FragmentFhirRevisitFormBinding
        get() = _binding!!

    override val viewModel: FhirRevisitViewModel by viewModels()

    override var fragment: Fragment = this;

    override var fragmentContainerId = 0;

    override val jsonFile : String = "revisit_form.json"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFhirRevisitFormBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentContainerId = binding.fragmentContainer.id
        updateArguments()
        if (savedInstanceState == null) {
            addQuestionnaireFragment()
        }
        observeEntitySaveAction("Inputs are missing.", "Revisit form is saved.")
    }

    override fun getFragmentId(): Int {
        return R.id.fragment_fhir_revisit_form;
    }

    override fun onSubmitAction() {
        navigateNext()
    }

    override fun onCancelAction() {
        findNavController().navigate(
            FhirRevisitFormFragmentDirections.actionFhirRevisitFormFragmentToFhirPrescriptionFragment()
        )
    }

    override fun navigateNext() {
        val intent = Intent(context, HomeActivity::class.java)
        startActivity(intent)
    }

}