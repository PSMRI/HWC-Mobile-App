package org.piramalswasthya.cho.ui.commons.fhir_visit_details

import android.annotation.SuppressLint
import android.content.Intent
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
import org.piramalswasthya.cho.databinding.FragmentFhirVitalsBinding
import org.piramalswasthya.cho.model.ModelObject
import org.piramalswasthya.cho.model.NetworkBody
import org.piramalswasthya.cho.ui.commons.FhirFragmentService
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.FhirAddPatientFragment
import org.piramalswasthya.cho.ui.commons.fhir_patient_vitals.FhirVitalsFragmentDirections
import org.piramalswasthya.cho.ui.commons.fhir_patient_vitals.FhirVitalsViewModel
import org.piramalswasthya.cho.ui.login_activity.cho_login.ChoLoginFragmentDirections
import org.piramalswasthya.cho.ui.login_activity.username.UsernameFragmentDirections
import org.piramalswasthya.cho.ui.register_patient_activity.RegisterPatientActivity
import org.piramalswasthya.cho.ui.web_view_activity.WebViewActivity
import timber.log.Timber
import java.security.MessageDigest
import javax.inject.Inject

//R.layout.fragment_fhir_visit_details


@AndroidEntryPoint
class FhirVisitDetailsFragment : Fragment(R.layout.fragment_fhir_visit_details), FhirFragmentService, NavigationAdapter {

    private var _binding: FragmentFhirVisitDetailsBinding? = null

    private val binding: FragmentFhirVisitDetailsBinding
        get() = _binding!!

    override val viewModel: FhirVisitDetailsViewModel by viewModels()

    override var fragment: Fragment = this;

    override var fragmentContainerId = 0;

    override val jsonFile : String = "patient-visit-details-paginated.json"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFhirVisitDetailsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentContainerId = binding.fragmentContainer.id
        updateArguments()
        if (savedInstanceState == null) {
            addQuestionnaireFragment()
        }
        observeEntitySaveAction("Inputs are missing.", "Visit details is saved.")
    }

    override fun getFragmentId(): Int {
        return R.id.fragment_fhir_visit_details;
    }

    override fun onSubmitAction() {
        navigateNext()
    }

    override fun onCancelAction() {
        val intent = Intent(context, WebViewActivity::class.java)
        startActivity(intent)
    }

    override fun navigateNext() {
//        findNavController().navigate(
//            FhirVisitDetailsFragmentDirections.actionFhirVisitDetailsFragmentToFhirVitalsFragment()
//        )
    }

}