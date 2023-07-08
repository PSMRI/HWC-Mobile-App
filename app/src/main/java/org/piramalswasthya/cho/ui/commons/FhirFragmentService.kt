package org.piramalswasthya.cho.ui.commons

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import com.google.android.fhir.datacapture.QuestionnaireFragment
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.FhirAddPatientFragment
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.FhirAddPatientViewModel

interface FhirFragmentService {

    val fragment : Fragment;

    val viewModel : AndroidViewModel

    companion object {
        const val QUESTIONNAIRE_FILE_PATH_KEY = "questionnaire-file-path-key"
        const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire-fragment-tag"
    }

    fun updateArguments(jsonFile : String) {
        val bundle = Bundle()
        bundle.putString(QUESTIONNAIRE_FILE_PATH_KEY, jsonFile)
        fragment.arguments = bundle
    }

    fun addQuestionnaireFragment() {
        fragment.childFragmentManager.commit {
            add(
                (fragment as FragmentContainerId).fragmentContainerId,
                QuestionnaireFragment.builder().setQuestionnaire((viewModel as FhirQuestionnaireService).questionnaire).build(),
                QUESTIONNAIRE_FRAGMENT_TAG
            )
        }
    }


}