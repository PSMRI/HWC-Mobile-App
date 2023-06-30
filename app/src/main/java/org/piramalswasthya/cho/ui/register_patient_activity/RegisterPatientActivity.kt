package org.piramalswasthya.cho.ui.register_patient_activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.databinding.ActivityRegisterPatientBinding
import org.piramalswasthya.cho.model.PatientDetails
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.FhirAddPatientFragment
import org.piramalswasthya.cho.ui.commons.fhir_examination_form.FhirExaminationFormFragment
import org.piramalswasthya.cho.ui.commons.fhir_patient_vitals.FhirVitalsFragment
import org.piramalswasthya.cho.ui.commons.fhir_revisit_form.FhirRevisitFormFragment
import org.piramalswasthya.cho.ui.visit_details_activity.VisitDetailsActivity

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

        val fragmentPersonalDetails = FhirAddPatientFragment()
        supportFragmentManager.beginTransaction().replace(binding.patientRegistration.id, fragmentPersonalDetails).commit()

        binding.btnSubmitReg.setOnClickListener {
//            benificiaryList.add(patientDetails)
            val intent = Intent(this, VisitDetailsActivity::class.java)
            startActivity(intent)
        }

    }

}