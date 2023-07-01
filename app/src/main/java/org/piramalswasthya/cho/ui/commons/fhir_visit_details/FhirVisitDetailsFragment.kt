package org.piramalswasthya.cho.ui.commons.fhir_visit_details

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.google.android.fhir.datacapture.QuestionnaireFragment
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentFhirAddPatientBinding
import org.piramalswasthya.cho.databinding.FragmentFhirVisitDetailsBinding
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.FhirAddPatientFragment
import timber.log.Timber

//R.layout.fragment_fhir_visit_details

class FhirVisitDetailsFragment : Fragment() {

    private var _binding: FragmentFhirVisitDetailsBinding? = null

    private val binding: FragmentFhirVisitDetailsBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFhirVisitDetailsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    private val viewModel: FhirVisitDetailsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.d("initiated")
        super.onViewCreated(view, savedInstanceState)
        setUpActionBar()
        setHasOptionsMenu(true)
        updateArguments()
        if (savedInstanceState == null) {
            addQuestionnaireFragment()
        }
//        observePatientSaveAction()
    }

    private fun setUpActionBar() {
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = requireContext().getString(R.string.add_patient)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun updateArguments() {
        arguments = Bundle()
        requireArguments().putString(QUESTIONNAIRE_FILE_PATH_KEY, "patient-visit-details-paginated.json")
    }

    private fun addQuestionnaireFragment() {
        childFragmentManager.commit {
            add(
                R.id.patient_visit_details_container,
                QuestionnaireFragment.builder().setQuestionnaire(viewModel.questionnaire).build(),
                FhirAddPatientFragment.QUESTIONNAIRE_FRAGMENT_TAG
            )
        }
    }

    public fun onSubmitAction() {
        Log.i("first", "first")
        val questionnaireFragment =
            childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment
        savePatient(questionnaireFragment.getQuestionnaireResponse())
    }

    private fun savePatient(questionnaireResponse: QuestionnaireResponse) {
        viewModel.savePatient(questionnaireResponse)
    }

    private fun observePatientSaveAction() {
        viewModel.isPatientSaved.observe(viewLifecycleOwner) {
            if (!it) {
                Toast.makeText(requireContext(), "Inputs are missing.", Toast.LENGTH_SHORT).show()
                return@observe
            }
            Toast.makeText(requireContext(), "Patient is saved.", Toast.LENGTH_SHORT).show()
            NavHostFragment.findNavController(this).navigateUp()
        }
    }

    companion object {
        const val QUESTIONNAIRE_FILE_PATH_KEY = "questionnaire-file-path-key"
        const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire-fragment-tag"
    }

}