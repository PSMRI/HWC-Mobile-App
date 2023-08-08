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
        super.onCreate(savedInstanceState)
        _binding = ActivityEditPatientDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(EditPatientDetailsViewModel::class.java)

        val patientId = intent.getStringExtra("patientId")

        navHostFragment = supportFragmentManager.findFragmentById(binding.patientDetalis.id) as NavHostFragment

        val args = Bundle()
        args.putString("patientId", patientId)

        navHostFragment.navController.setGraph(R.navigation.nav_edit_patient, args);

//        var patientDetails = PatientDetails()
//        val index: Int = intent.getIntExtra("index", defaultValue)
//
//        if(index != defaultValue){
//            patientDetails = benificiaryList[index];
//        }

//        val fragment = FhirVisitDetailsFragment();
//        supportFragmentManager.beginTransaction().replace(binding.patientDetalis.id, fragment).commit()

        binding.btnSubmit.setOnClickListener {

            currFragment = navHostFragment.childFragmentManager.primaryNavigationFragment as NavigationAdapter

            when (currFragment.getFragmentId()){
                R.id.fragment_visit_details_info -> {
                    binding.headerTextEditPatient.text = resources.getString(R.string.vitals_text)
                    binding.btnSubmit.text = resources.getString(R.string.submit_to_doctor_text)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }
                R.id.fragment_fhir_vitals -> {
                    binding.headerTextEditPatient.text = resources.getString(R.string.prescription_text)
                    binding.btnSubmit.text = resources.getString(R.string.submit)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }
                R.id.fragment_fhir_prescription -> {
                    binding.headerTextEditPatient.text = resources.getString(R.string.revisit_details_text)
                    binding.btnSubmit.text = resources.getString(R.string.submit)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }
                R.id.fragment_fhir_revisit_form -> {

                }
            }

            currFragment.onSubmitAction()

        }

        binding.btnCancel.setOnClickListener {

            currFragment = navHostFragment.childFragmentManager.primaryNavigationFragment as NavigationAdapter

            when (currFragment.getFragmentId()){
                R.id.fragment_visit_details_info -> {

                }
                R.id.fragment_fhir_vitals -> {
                    binding.headerTextEditPatient.text = resources.getString(R.string.visit_details)
                    binding.btnSubmit.text = resources.getString(R.string.next)
                    binding.btnCancel.text = resources.getString(R.string.esanjeevni)
                }
                R.id.fragment_fhir_prescription -> {
                    binding.headerTextEditPatient.text = resources.getString(R.string.vitals_text)
                    binding.btnSubmit.text = resources.getString(R.string.submit_to_doctor_text)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }
                R.id.fragment_fhir_revisit_form -> {
                    binding.headerTextEditPatient.text = resources.getString(R.string.vitals_text)
                    binding.btnSubmit.text = resources.getString(R.string.submit)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }
            }

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
