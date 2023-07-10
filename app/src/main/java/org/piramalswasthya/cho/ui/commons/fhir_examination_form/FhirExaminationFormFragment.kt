package org.piramalswasthya.cho.ui.commons.fhir_examination_form

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.google.android.fhir.datacapture.QuestionnaireFragment
import dagger.hilt.android.AndroidEntryPoint
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentFhirAddPatientBinding
import org.piramalswasthya.cho.databinding.FragmentFhirExaminationFormBinding
import org.piramalswasthya.cho.ui.commons.FhirFragmentService
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.FhirAddPatientViewModel
import timber.log.Timber

@AndroidEntryPoint
class FhirExaminationFormFragment : Fragment(R.layout.fragment_fhir_examination_form), FhirFragmentService, NavigationAdapter {

    private var _binding: FragmentFhirExaminationFormBinding? = null

    private val binding: FragmentFhirExaminationFormBinding
        get() = _binding!!

    override val viewModel: FhirExaminationFormViewModel by viewModels()

    override var fragment: Fragment = this;

    override var fragmentContainerId = 0;

    override val jsonFile : String = "examination_form.json"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFhirExaminationFormBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentContainerId = binding.fragmentContainer.id
        updateArguments()
        if (savedInstanceState == null) {
            addQuestionnaireFragment()
        }
        observeEntitySaveAction("Inputs are missing.", "Examination form is saved.")
    }

    override fun getFragmentId(): Int {
        return R.id.fragment_fhir_examination_form;
    }

    override fun onSubmitAction() {

    }

    override fun onCancelAction() {

    }

    override fun navigateNext() {

    }

}