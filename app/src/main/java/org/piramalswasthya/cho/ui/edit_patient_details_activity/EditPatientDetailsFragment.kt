package org.piramalswasthya.cho.ui.edit_patient_details_activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.ActivityEditPatientDetailsBinding
import org.piramalswasthya.cho.ui.commons.NavigationAdapter

@AndroidEntryPoint
class EditPatientDetailsFragment : Fragment() {

    private lateinit var viewModel: EditPatientDetailsViewModel

    private lateinit var currFragment: NavigationAdapter

    private lateinit var navHostFragment: NavHostFragment

    private var _binding: ActivityEditPatientDetailsBinding? = null

    private val defaultValue = -1

    private val binding: ActivityEditPatientDetailsBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityEditPatientDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[EditPatientDetailsViewModel::class.java]

        navHostFragment =
            childFragmentManager.findFragmentById(binding.patientDetalis.id) as NavHostFragment

        binding.btnSubmit.setOnClickListener {

            currFragment =
                navHostFragment.childFragmentManager.primaryNavigationFragment as NavigationAdapter

            Log.d("aaaaaaaaaaaa", R.id.fragment_fhir_visit_details.toString())
            Log.d("aaaaaaaaaaaa", currFragment.getFragmentId().toString())

            when (currFragment.getFragmentId()) {
                R.id.fragment_fhir_visit_details -> {
                    binding.headerTextEditPatient.text =
                        resources.getString(R.string.vitals_text)
                    binding.btnSubmit.text = resources.getString(R.string.submit_to_doctor_text)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }
                R.id.fragment_fhir_vitals -> {
                    binding.headerTextEditPatient.text =
                        resources.getString(R.string.prescription_text)
                    binding.btnSubmit.text = resources.getString(R.string.submit)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }
                R.id.fragment_fhir_prescription -> {
                    binding.headerTextEditPatient.text =
                        resources.getString(R.string.revisit_details_text)
                    binding.btnSubmit.text = resources.getString(R.string.submit)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }
                R.id.fragment_fhir_revisit_form -> {

                }
            }

            currFragment.onSubmitAction()

        }

        binding.btnCancel.setOnClickListener {

            currFragment =
                navHostFragment.childFragmentManager.primaryNavigationFragment as NavigationAdapter

            Log.d("aaaaaaaaaaaa", R.id.fragment_fhir_visit_details.toString())
            Log.d("aaaaaaaaaaaa", currFragment.getFragmentId().toString())

            when (currFragment.getFragmentId()) {
                R.id.fragment_fhir_visit_details -> {

                }
                R.id.fragment_fhir_vitals -> {
                    binding.headerTextEditPatient.text =
                        resources.getString(R.string.visit_details)
                    binding.btnSubmit.text = resources.getString(R.string.next)
                    binding.btnCancel.text = resources.getString(R.string.esanjeevni)
                }
                R.id.fragment_fhir_prescription -> {
                    binding.headerTextEditPatient.text =
                        resources.getString(R.string.vitals_text)
                    binding.btnSubmit.text = resources.getString(R.string.submit_to_doctor_text)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }
                R.id.fragment_fhir_revisit_form -> {
                    binding.headerTextEditPatient.text =
                        resources.getString(R.string.vitals_text)
                    binding.btnSubmit.text = resources.getString(R.string.submit)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }
            }

            currFragment.onCancelAction()

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
