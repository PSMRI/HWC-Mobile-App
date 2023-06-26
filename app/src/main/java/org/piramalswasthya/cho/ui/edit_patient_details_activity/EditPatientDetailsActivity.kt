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
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.ActivityEditPatientDetailsBinding
import org.piramalswasthya.cho.list.benificiaryList
import org.piramalswasthya.cho.model.PatientDetails
import org.piramalswasthya.cho.ui.commons.personal_details.PersonalDetailsFragment
import org.piramalswasthya.cho.ui.edit_patient_details_activity.prescription.PrescriptionFragment
import org.piramalswasthya.cho.ui.edit_patient_details_activity.visit_details.VisitDetailsFragment
import org.piramalswasthya.cho.ui.edit_patient_details_activity.vitals_form.VitalsFormFragment
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.ui.register_patient_activity.RegisterPatientActivity


@AndroidEntryPoint
class EditPatientDetailsActivity: AppCompatActivity() {

    private lateinit var viewModel: EditPatientDetailsViewModel

    private var currFragment: Int = R.id.fragment_personal_details;

    private var _binding : ActivityEditPatientDetailsBinding? = null

    private val defaultValue = -1

    private val binding  : ActivityEditPatientDetailsBinding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityEditPatientDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(EditPatientDetailsViewModel::class.java)


        var patientDetails = PatientDetails()
        val index: Int = intent.getIntExtra("index", defaultValue)

        if(index != defaultValue){
            patientDetails = benificiaryList[index];
        }

        val fragment = PersonalDetailsFragment(patientDetails);
        supportFragmentManager.beginTransaction().replace(binding.patientDetalis.id, fragment).commit()

        binding.btnSubmit.setOnClickListener {
            when (currFragment){
                R.id.fragment_personal_details -> {
                    val fragmentVisitDetails = VisitDetailsFragment(patientDetails);
                    supportFragmentManager.beginTransaction().replace(binding.patientDetalis.id, fragmentVisitDetails).commit()
                    currFragment = R.id.fragment_visit_details
                    binding.btnSubmit.text = resources.getString(R.string.next)
                    binding.btnCancel.visibility = View.VISIBLE
                }
                R.id.fragment_visit_details -> {
                    val fragmentVitalsForm = VitalsFormFragment(patientDetails);
                    supportFragmentManager.beginTransaction().replace(binding.patientDetalis.id, fragmentVitalsForm).commit()
                    currFragment = R.id.fragment_vitals_form
                    binding.btnSubmit.text = resources.getString(R.string.submit_to_doctor_text)
                    binding.btnCancel.visibility = View.VISIBLE
                }
                R.id.fragment_vitals_form -> {
                    val fragmentPrescription = PrescriptionFragment(patientDetails);
                    supportFragmentManager.beginTransaction().replace(binding.patientDetalis.id, fragmentPrescription).commit()
                    currFragment = R.id.fragment_prescription
                    binding.btnSubmit.text = resources.getString(R.string.submit)
                    binding.btnCancel.visibility = View.VISIBLE
                }
                R.id.fragment_prescription -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        binding.btnCancel.setOnClickListener {
            when (currFragment){
                R.id.fragment_personal_details -> {

                }
                R.id.fragment_visit_details -> {
                    val fragmentPersonalDetails = PersonalDetailsFragment(patientDetails);
                    supportFragmentManager.beginTransaction().replace(binding.patientDetalis.id, fragmentPersonalDetails).commit()
                    currFragment = R.id.fragment_personal_details
                    binding.btnSubmit.text = resources.getString(R.string.next)
                    binding.btnCancel.visibility = View.GONE
                }
                R.id.fragment_vitals_form -> {
                    val fragmentVisitDetails = VisitDetailsFragment(patientDetails);
                    supportFragmentManager.beginTransaction().replace(binding.patientDetalis.id, fragmentVisitDetails).commit()
                    currFragment = R.id.fragment_visit_details
                    binding.btnSubmit.text = resources.getString(R.string.next)
                    binding.btnCancel.visibility = View.VISIBLE
                }
                R.id.fragment_prescription -> {
                    val fragmentVitalsForm = VitalsFormFragment(patientDetails);
                    supportFragmentManager.beginTransaction().replace(binding.patientDetalis.id, fragmentVitalsForm).commit()
                    currFragment = R.id.fragment_vitals_form
                    binding.btnSubmit.text = resources.getString(R.string.submit_to_doctor_text)
                    binding.btnCancel.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
