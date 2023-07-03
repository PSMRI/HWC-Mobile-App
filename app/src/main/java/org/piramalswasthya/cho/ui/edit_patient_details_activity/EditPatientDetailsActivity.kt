package org.piramalswasthya.cho.ui.edit_patient_details_activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.ActivityEditPatientDetailsBinding
import org.piramalswasthya.cho.list.benificiaryList
import org.piramalswasthya.cho.model.PatientDetails
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.FhirAddPatientFragment
import org.piramalswasthya.cho.ui.commons.fhir_patient_vitals.FhirVitalsFragment
import org.piramalswasthya.cho.ui.commons.fhir_revisit_form.FhirRevisitFormFragment
import org.piramalswasthya.cho.ui.commons.fhir_visit_details.FhirVisitDetailsFragment
import org.piramalswasthya.cho.ui.commons.personal_details.PersonalDetailsFragment
import org.piramalswasthya.cho.ui.edit_patient_details_activity.prescription.PrescriptionFragment
import org.piramalswasthya.cho.ui.edit_patient_details_activity.visit_details.VisitDetailsFragment
import org.piramalswasthya.cho.ui.edit_patient_details_activity.vitals_form.VitalsFormFragment
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.ui.register_patient_activity.RegisterPatientActivity


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

        navHostFragment = supportFragmentManager.findFragmentById(binding.patientDetalis.id) as NavHostFragment


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

            Log.d("aaaaaaaaaaaa",R.id.fragment_fhir_visit_details.toString())
            Log.d("aaaaaaaaaaaa",currFragment.getFragmentId().toString())

            when (currFragment.getFragmentId()){
                R.id.fragment_fhir_visit_details -> {
                    binding.headerTextEditPatient.text = resources.getString(R.string.vitals_text)
                    binding.btnSubmit.text = resources.getString(R.string.submit_to_doctor_text)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }
                R.id.fragment_fhir_vitals -> {
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

            Log.d("aaaaaaaaaaaa",R.id.fragment_fhir_visit_details.toString())
            Log.d("aaaaaaaaaaaa",currFragment.getFragmentId().toString())

            when (currFragment.getFragmentId()){
                R.id.fragment_fhir_visit_details -> {

                }
                R.id.fragment_fhir_vitals -> {
                    binding.headerTextEditPatient.text = resources.getString(R.string.visit_details)
                    binding.btnSubmit.text = resources.getString(R.string.next)
                    binding.btnCancel.text = resources.getString(R.string.esanjeevni)
                }
                R.id.fragment_fhir_revisit_form -> {
                    binding.headerTextEditPatient.text = resources.getString(R.string.vitals_text)
                    binding.btnSubmit.text = resources.getString(R.string.submit_to_doctor_text)
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
