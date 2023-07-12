package org.piramalswasthya.cho.ui.register_patient_activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.databinding.ActivityRegisterPatientBinding
import org.piramalswasthya.cho.model.PatientDetails
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.FhirAddPatientFragment
import org.piramalswasthya.cho.ui.home_activity.HomeActivity

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

        val fragmentAddPatient = FhirAddPatientFragment();
        supportFragmentManager.beginTransaction().replace(binding.patientRegistration.id, fragmentAddPatient).commit()

        binding.btnSubmit.setOnClickListener {
//            benificiaryList.add(patientDetails)
            fragmentAddPatient.onSubmitAction()
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

    }

}