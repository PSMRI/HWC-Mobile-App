package org.piramalswasthya.cho.ui.patient_vitals_activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.databinding.ActivityPatientVitalsBinding
import org.piramalswasthya.cho.ui.commons.fhir_patient_vitals.FhirVitalsFragment
import org.piramalswasthya.cho.ui.patient_revisit_activity.PatientRevisitActivity

@AndroidEntryPoint
class PatientVitalsActivity : AppCompatActivity() {

    private var _binding: ActivityPatientVitalsBinding? = null

    private val binding: ActivityPatientVitalsBinding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPatientVitalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragmentPatientVitals = FhirVitalsFragment()
        supportFragmentManager.beginTransaction().replace(binding.patientVitalsContainer.id, fragmentPatientVitals).commit()

        binding.btnSubmitVitals.setOnClickListener {
//            benificiaryList.add(patientDetails)
            val intent = Intent(this, PatientRevisitActivity::class.java)
            startActivity(intent)
        }

    }

}