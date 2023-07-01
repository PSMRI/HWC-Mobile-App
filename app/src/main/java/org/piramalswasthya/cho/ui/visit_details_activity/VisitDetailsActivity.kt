package org.piramalswasthya.cho.ui.visit_details_activity

import android.content.Intent
import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.databinding.ActivityVisitDetailsBinding
import org.piramalswasthya.cho.ui.commons.fhir_visit_details.FhirVisitDetailsFragment
import org.piramalswasthya.cho.ui.patient_vitals_activity.PatientVitalsActivity
//import org.piramalswasthya.cho.ui.visit_details_activity.ui.theme.CHOTheme

@AndroidEntryPoint
class VisitDetailsActivity : AppCompatActivity() {


    private var _binding: ActivityVisitDetailsBinding? = null

    private val binding: ActivityVisitDetailsBinding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityVisitDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragmentVisitDetails = FhirVisitDetailsFragment()
        supportFragmentManager.beginTransaction().replace(binding.visitDetailsContainer.id, fragmentVisitDetails).commit()

        binding.btnSubmitVisitDetails.setOnClickListener {
//            benificiaryList.add(patientDetails)
            val intent = Intent(this, PatientVitalsActivity::class.java)
            startActivity(intent)
        }

    }

}