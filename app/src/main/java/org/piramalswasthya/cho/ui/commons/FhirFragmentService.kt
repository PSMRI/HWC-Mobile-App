package org.piramalswasthya.cho.ui.commons

import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.NavHostFragment
import com.google.android.fhir.datacapture.QuestionnaireFragment


interface FhirFragmentService {

    var fragmentContainerId : Int

    val fragment : Fragment

    val viewModel : ViewModel

    val jsonFile : String

    companion object {
        const val QUESTIONNAIRE_FILE_PATH_KEY = "questionnaire-file-path-key"
        const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire-fragment-tag"
    }

    fun updateArguments() {
        val bundle = Bundle()
        bundle.putString(QUESTIONNAIRE_FILE_PATH_KEY, jsonFile)
        fragment.arguments = bundle
    }

    fun addQuestionnaireFragment() {
        fragment.childFragmentManager.commit {
            add(
                fragmentContainerId,
                QuestionnaireFragment.builder().setQuestionnaire((viewModel as FhirQuestionnaireService).questionnaire).build(),
                QUESTIONNAIRE_FRAGMENT_TAG
            )
        }
    }

    fun saveEntity(){
        val questionnaireFragment = fragment.childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment
        (viewModel as FhirQuestionnaireService).saveEntity(questionnaireFragment.getQuestionnaireResponse())
    }

    fun navigateNext()

    fun observeEntitySaveAction(failedText: String, successText: String) {
        (viewModel as FhirQuestionnaireService).isEntitySaved.observe(fragment.viewLifecycleOwner) {
            if (!it) {
                Toast.makeText(fragment.requireContext(), failedText, Toast.LENGTH_SHORT).show()
                return@observe
            }
            Toast.makeText(fragment.requireContext(), successText, Toast.LENGTH_SHORT).show()
            navigateNext()
        }
    }

}