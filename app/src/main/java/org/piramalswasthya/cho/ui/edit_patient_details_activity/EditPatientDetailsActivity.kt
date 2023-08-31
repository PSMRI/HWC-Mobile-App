package org.piramalswasthya.cho.ui.edit_patient_details_activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.ActivityEditPatientDetailsBinding
import org.piramalswasthya.cho.ui.commons.NavigationAdapter


@AndroidEntryPoint
class EditPatientDetailsActivity: AppCompatActivity() {

    private lateinit var viewModel: EditPatientDetailsViewModel

    private lateinit var currFragment: NavigationAdapter

    private lateinit var navHostFragment: NavHostFragment

    private var _binding : ActivityEditPatientDetailsBinding? = null

    private val defaultValue = -1

    private val binding  : ActivityEditPatientDetailsBinding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        val patientId = intent.getStringExtra("patientId")
        val args = Bundle()
        args.putString("patientId", patientId)

        super.onCreate(savedInstanceState)
        _binding = ActivityEditPatientDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(EditPatientDetailsViewModel::class.java)

        navHostFragment = supportFragmentManager.findFragmentById(binding.patientDetalis.id) as NavHostFragment
        navHostFragment.navController.setGraph(R.navigation.nav_edit_patient, args);

        navHostFragment.navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.fhirVisitDetailsFragment -> {
                    binding.headerTextEditPatient.text = resources.getString(R.string.visit_details)
                    binding.btnSubmit.text = resources.getString(R.string.next)
                    binding.btnCancel.text = resources.getString(R.string.esanjeevni)
                }
//                R.id.historyCustomFragment -> {
//                    binding.headerTextEditPatient.text = resources.getString(R.string.history_text)
//                    binding.btnSubmit.text = resources.getString(R.string.next)
//                    binding.btnCancel.text = resources.getString(R.string.cancel)
//                }
                R.id.customVitalsFragment ->{
                    binding.headerTextEditPatient.text = resources.getString(R.string.vitals_text)
                    binding.btnSubmit.text = resources.getString(R.string.submit_to_doctor_text)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }
//                R.id.examinationFragment ->{
//                    binding.headerTextEditPatient.text = resources.getString(R.string.examination_text)
//                    binding.btnSubmit.text = resources.getString(R.string.submit)
//                    binding.btnCancel.text = resources.getString(R.string.cancel)
//                }
                R.id.fhirPrescriptionFragment -> {
                    binding.headerTextEditPatient.text = resources.getString(R.string.prescription_text)
                    binding.btnSubmit.text = resources.getString(R.string.submit)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }
                R.id.fhirRevisitFormFragment ->{
                    binding.headerTextEditPatient.text = resources.getString(R.string.revisit_details_text)
                    binding.btnSubmit.text = resources.getString(R.string.next)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }
                R.id.caseRecordCustom ->{
                    binding.headerTextEditPatient.text = resources.getString(R.string.case_record_text)
                    binding.btnSubmit.text = resources.getString(R.string.next)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }
            }
        }

        binding.btnSubmit.setOnClickListener {
            currFragment = navHostFragment.childFragmentManager.primaryNavigationFragment as NavigationAdapter
            currFragment.onSubmitAction()
        }

        binding.btnCancel.setOnClickListener {
            currFragment = navHostFragment.childFragmentManager.primaryNavigationFragment as NavigationAdapter
            currFragment.onCancelAction()
        }

    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
