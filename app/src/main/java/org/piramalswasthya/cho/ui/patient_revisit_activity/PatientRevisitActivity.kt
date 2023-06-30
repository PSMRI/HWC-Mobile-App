package org.piramalswasthya.cho.ui.patient_revisit_activity


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.databinding.ActivityPatientRevisitBinding
import org.piramalswasthya.cho.ui.commons.fhir_revisit_form.FhirRevisitFormFragment

@AndroidEntryPoint
class PatientRevisitActivity : AppCompatActivity() {

    private var _binding: ActivityPatientRevisitBinding? = null

    private val binding: ActivityPatientRevisitBinding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPatientRevisitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragmentRevisitDetails = FhirRevisitFormFragment()
        supportFragmentManager.beginTransaction().replace(binding.patientRevisitContainer.id, fragmentRevisitDetails).commit()

//        binding.btnSubmit.setOnClickListener {
//            benificiaryList.add(patientDetails)
//            val intent = Intent(this, HomeActivity::class.java)
//            startActivity(intent)
//        }

    }

}