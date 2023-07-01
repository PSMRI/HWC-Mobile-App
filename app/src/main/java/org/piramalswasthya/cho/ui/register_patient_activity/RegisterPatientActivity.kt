package org.piramalswasthya.cho.ui.register_patient_activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.hl7.fhir.r4.model.HumanName
import org.hl7.fhir.r4.model.Patient
import org.piramalswasthya.cho.databinding.ActivityRegisterPatientBinding
import org.piramalswasthya.cho.model.PatientDetails
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.FhirAddPatientFragment
import org.piramalswasthya.cho.ui.commons.fhir_visit_details.FhirVisitDetailsFragment
import java.util.UUID

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

        var patient = Patient()
        patient.id = generateUuid()
        var name = HumanName();
        name.family = "Paul"
        patient.name.add(0, name)



        val fragmentPersonalDetails = FhirVisitDetailsFragment();
        supportFragmentManager.beginTransaction().replace(binding.patientRegistration.id, fragmentPersonalDetails).commit()

        binding.btnSubmit.setOnClickListener {
            fragmentPersonalDetails.onSubmitAction()
//            benificiaryList.add(patientDetails)
//            val intent = Intent(this, HomeActivity::class.java)
//            startActivity(intent)
        }

    }

    private fun generateUuid(): String {
        return UUID.randomUUID().toString()
    }

}