package org.piramalswasthya.cho.ui.register_patient_activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.databinding.ActivityRegisterPatientBinding
import org.piramalswasthya.cho.model.PatientDetails
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.FhirAddPatientFragment

@AndroidEntryPoint
class RegisterPatientActivity : AppCompatActivity() {

    val patientDetails = PatientDetails()

    private var _binding: ActivityRegisterPatientBinding? = null

    private val binding: ActivityRegisterPatientBinding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterPatientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragmentPersonalDetails = FhirAddPatientFragment();
        supportFragmentManager.beginTransaction().replace(binding.patientRegistration.id, fragmentPersonalDetails).commit()

//        binding.btnSubmit.setOnClickListener {
//            benificiaryList.add(patientDetails)
//            val intent = Intent(this, HomeActivity::class.java)
//            startActivity(intent)
//        }

    }

}