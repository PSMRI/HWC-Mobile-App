package org.piramalswasthya.cho.ui.commons.fhir_visit_details

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentFhirVisitDetailsBinding
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.web_view_activity.WebViewActivity

//R.layout.fragment_fhir_visit_details


@AndroidEntryPoint
class FhirVisitDetailsFragment : Fragment(R.layout.fragment_fhir_visit_details), NavigationAdapter {

    private var _binding: FragmentFhirVisitDetailsBinding? = null

    private val binding: FragmentFhirVisitDetailsBinding
        get() = _binding!!

    val viewModel: FhirVisitDetailsViewModel by viewModels()

    var fragment: Fragment = this;

    var fragmentContainerId = 0;

    val jsonFile : String = "patient-visit-details-paginated.json"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFhirVisitDetailsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentContainerId = binding.fragmentContainer.id
//        updateArguments()
        if (savedInstanceState == null) {
//            addQuestionnaireFragment()
        }
//        observeEntitySaveAction("Inputs are missing.", "Visit details is saved.")
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

    fun navigateNext() {
//        findNavController().navigate(
//            FhirVisitDetailsFragmentDirections.actionFhirVisitDetailsFragmentToFhirVitalsFragment()
//        )
    }

}