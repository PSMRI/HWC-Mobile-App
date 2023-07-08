package org.piramalswasthya.cho.ui.commons.fhir_add_patient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.fragment.NavHostFragment
import com.google.android.fhir.datacapture.QuestionnaireFragment
import dagger.hilt.android.AndroidEntryPoint

import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentChoLoginBinding
import org.piramalswasthya.cho.databinding.FragmentFhirAddPatientBinding
import org.piramalswasthya.cho.databinding.FragmentOutreachBinding
import org.piramalswasthya.cho.ui.commons.FhirFragmentService
import org.piramalswasthya.cho.ui.commons.FragmentContainerId
import timber.log.Timber


/** A fragment class to show patient registration screen. */

@AndroidEntryPoint
class FhirAddPatientFragment : Fragment(R.layout.fragment_fhir_add_patient), FragmentContainerId, FhirFragmentService {

    private var _binding: FragmentFhirAddPatientBinding? = null

    private val binding: FragmentFhirAddPatientBinding
        get() = _binding!!

    override val viewModel: FhirAddPatientViewModel by viewModels()

    override var fragment: Fragment = this;

    override var fragmentContainerId = 0;

    private val jsonFile : String = "new-patient-registration-paginated.json"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFhirAddPatientBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentContainerId = binding.fragmentContainer.id

//        fhirFragmentService = FhirFragmentService(this, this.viewModel)

        updateArguments(jsonFile)
        if (savedInstanceState == null) {
            addQuestionnaireFragment()
        }

//        observePatientSaveAction()

//        val submitButton = view.findViewById<Button>(R.id.btn_submit)
//        submitButton.setOnClickListener {
//            onSubmitAction()
//        }
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        super.onCreateOptionsMenu(menu, inflater)
//        inflater.inflate(R.menu.add_patient_fragment_menu, menu)
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.action_add_patient_submit -> {
//                onSubmitAction()
//                true
//            }
//            android.R.id.home -> {
//                NavHostFragment.findNavController(this).navigateUp()
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

//    private fun setUpActionBar() {
//        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
//            title = requireContext().getString(R.string.add_patient)
//            setDisplayHomeAsUpEnabled(true)
//        }
//    }

//    private fun updateArguments() {
//        val bundle = Bundle()
//        bundle.putString(QUESTIONNAIRE_FILE_PATH_KEY, "new-patient-registration-paginated.json")
//        arguments = bundle
//    }
//
//
//    private fun addQuestionnaireFragment() {
//        childFragmentManager.commit {
//            add(
//                R.id.add_patient_container,
//                QuestionnaireFragment.builder().setQuestionnaire(viewModel.questionnaire).build(),
//                QUESTIONNAIRE_FRAGMENT_TAG
//            )
//        }
//    }

    fun onSubmitAction() {
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
