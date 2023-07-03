package org.piramalswasthya.cho.ui.commons.fhir_visit_details

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.network.AmritApiService
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.fhir.datacapture.QuestionnaireFragment
import org.hl7.fhir.r4.model.QuestionnaireResponse
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentFhirVisitDetailsBinding
import org.piramalswasthya.cho.model.ModelObject
import org.piramalswasthya.cho.model.NetworkBody
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.FhirAddPatientFragment
import org.piramalswasthya.cho.ui.commons.fhir_visit_details.FhirVisitDetailsViewModel.LoadState.*
import org.piramalswasthya.cho.ui.login_activity.username.UsernameFragmentDirections
import timber.log.Timber
import java.security.MessageDigest
import javax.inject.Inject

//R.layout.fragment_fhir_visit_details

@AndroidEntryPoint
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
        observePatientSaveAction()
        // button for navigate to web-view page of eSanjeevani
        binding.btnWebview.setOnClickListener {
            Timber.tag("URL").d("erere")
            var user = "Cdac@1234";
            var token = "token"
            var passWord = encryptSHA512(encryptSHA512(user) + encryptSHA512(token))

            //creating object using encrypted Password and other details
            var networkBody = NetworkBody(
                "8501258162",
                passWord,
                "token",
                "11001"
            )
            Timber.tag("Request").d("$networkBody")
            // calling getAuthRefIdForWebView() in coroutine scope for getting referenceId
            viewModel.launchESanjeenvani(networkBody)
        }

        viewModel.loadState.observe(viewLifecycleOwner){
            it?.let {
                Timber.d("Loaded at loadState : $it")
                findNavController().navigate(
                    FhirVisitDetailsFragmentD.actionWebviewFragment(it)
                )
                viewModel.resetLoadState()
            }

            }
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
    private fun encryptSHA512(input: String): String {
        val digest = MessageDigest.getInstance("SHA-512")
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

}