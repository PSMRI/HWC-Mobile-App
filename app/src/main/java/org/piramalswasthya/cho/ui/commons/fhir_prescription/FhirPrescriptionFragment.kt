package org.piramalswasthya.cho.ui.commons.fhir_prescription

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.fhir.datacapture.QuestionnaireFragment
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.fhir_patient_vitals.FhirVitalsFragmentDirections
import org.piramalswasthya.cho.ui.commons.fhir_patient_vitals.FhirVitalsViewModel
import timber.log.Timber


class FhirPrescriptionFragment : Fragment(R.layout.fragment_fhir_prescription), NavigationAdapter {
    private val viewModel: FhirPrescriptionViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.d("initiated")
        super.onViewCreated(view, savedInstanceState)
        setUpActionBar()
        setHasOptionsMenu(true)
        updateArguments()
        if (savedInstanceState == null) {
            addQuestionnaireFragment()
        }
        observePatientSaveAction()
    }

    private fun setUpActionBar() {
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = "Patient Vitals"
            setDisplayHomeAsUpEnabled(true)
        }
    }


    private fun updateArguments() {
        arguments = Bundle()
        requireArguments().putString(QUESTIONNAIRE_FILE_PATH_KEY, "prescription_form.json")
    }

    private fun observePatientSaveAction() {
        viewModel.isPatientSaved.observe(viewLifecycleOwner) {
            if (!it) {
                Toast.makeText(requireContext(), "Inputs are missing.", Toast.LENGTH_SHORT).show()
                return@observe
            }
            Toast.makeText(requireContext(), "Patient is saved.", Toast.LENGTH_SHORT).show()
//            NavHostFragment.findNavController(this).navigateUp()
        }
    }

    private fun addQuestionnaireFragment() {
        childFragmentManager.commit {
            add(
                R.id.prescription_container,
                QuestionnaireFragment.builder().setQuestionnaire(viewModel.questionnaire).build(),
                QUESTIONNAIRE_FRAGMENT_TAG
            )
        }
    }


//    private fun onSubmitAction() {
//        val questionnaireFragment =
//            childFragmentManager.findFragmentByTag(FhirVitalsFragment.QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment
//        savePatient(questionnaireFragment.getQuestionnaireResponse())
//    }

    private fun savePatient(questionnaireResponse: QuestionnaireResponse) {
        viewModel.savePatient(questionnaireResponse)
    }





    companion object {
        const val QUESTIONNAIRE_FILE_PATH_KEY = "questionnaire-file-path-key"
        const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire-fragment-tag"
    }

    override fun getFragmentId(): Int {
        return R.id.fragment_fhir_prescription;
    }

    override fun onSubmitAction() {
//        val questionnaireFragment =
//            childFragmentManager.findFragmentByTag(FhirVitalsFragment.QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment
//        savePatient(questionnaireFragment.getQuestionnaireResponse())
        findNavController().navigate(
            FhirPrescriptionFragmentDirections.actionFhirPrescriptionFragmentToFhirRevisitFormFragment()
        )
    }

    override fun onCancelAction() {
//        requireActivity().supportFragmentManager.popBackStack()
//        getFragmentManager()?.popBackStack()
        findNavController().navigate(
            FhirPrescriptionFragmentDirections.actionFhirPrescriptionFragmentToFhirVitalsFragment()
        )
    }
}