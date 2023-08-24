package org.piramalswasthya.cho.ui.register_patient_activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.ActivityRegisterPatientBinding
import org.piramalswasthya.cho.model.PatientDetails
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.FhirAddPatientFragment
import org.piramalswasthya.cho.ui.home_activity.HomeActivity

@AndroidEntryPoint
class RegisterPatientActivity : AppCompatActivity() {

    val patientDetails = PatientDetails()

    private var _binding: ActivityRegisterPatientBinding? = null

    private val binding: ActivityRegisterPatientBinding
        get() = _binding!!

    private lateinit var currFragment: NavigationAdapter

    private lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterPatientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navHostFragment = supportFragmentManager.findFragmentById(binding.patientRegistration.id) as NavHostFragment

        navHostFragment.navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.patientDetailsFragment -> {
                    binding.headerTextRegisterPatient.text = resources.getString(R.string.personal_details)
                    binding.btnSubmit.text = resources.getString(R.string.next)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }
                R.id.fragmentLocation -> {
                    binding.headerTextRegisterPatient.text = resources.getString(R.string.location_details)
                    binding.btnSubmit.text = resources.getString(R.string.next)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }
                R.id.otherInformationsFragment ->{
                    binding.headerTextRegisterPatient.text = resources.getString(R.string.other_info)
                    binding.btnSubmit.text = resources.getString(R.string.submit)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }
            }
        }

        binding.btnSubmit.setOnClickListener {
            currFragment = navHostFragment.childFragmentManager.primaryNavigationFragment as NavigationAdapter
            currFragment.onSubmitAction()
        }

        binding.btnCancel.setOnClickListener {
            currFragment = navHostFragment.childFragmentManager.primaryNavigationFragment as NavigationAdapter
            currFragment.onCancelAction()
        }

    }

    override fun onBackPressed() {
        finish()
    }

}