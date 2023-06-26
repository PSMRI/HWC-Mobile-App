package org.piramalswasthya.cho.ui.register_patient_activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.ActivityHomeBinding
import org.piramalswasthya.cho.databinding.ActivityRegisterPatientBinding
import org.piramalswasthya.cho.list.benificiaryList
import org.piramalswasthya.cho.model.PatientDetails
import org.piramalswasthya.cho.ui.commons.personal_details.PersonalDetailsFragment
import org.piramalswasthya.cho.ui.home.add_patient_fragment.AddPatientFragment
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


        val fragmentPersonalDetails = AddPatientFragment();
        supportFragmentManager.beginTransaction().replace(binding.patientRegistration.id, fragmentPersonalDetails).commit()

        binding.btnSubmit.setOnClickListener {
            benificiaryList.add(patientDetails)
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

    }

}