package org.piramalswasthya.cho.ui.register_patient_activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.piramalswasthya.cho.CHOApplication
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.ActivityRegisterPatientBinding
import org.piramalswasthya.cho.facenet.SharedViewModel
import org.piramalswasthya.cho.helpers.MyContextWrapper
import org.piramalswasthya.cho.model.PatientDetails
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.home_activity.HomeActivity

@AndroidEntryPoint
class RegisterPatientActivity : AppCompatActivity() {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WrapperEntryPoint {
        val preferenceDao: PreferenceDao
    }
    override fun attachBaseContext(newBase: Context) {
        val pref = EntryPointAccessors.fromApplication(
            newBase,
            WrapperEntryPoint::class.java
        ).preferenceDao
        super.attachBaseContext(
            MyContextWrapper.wrap(
                newBase,
                newBase.applicationContext,
                pref.getCurrentLanguage().symbol
            ))
    }
    val patientDetails = PatientDetails()

    private var _binding: ActivityRegisterPatientBinding? = null

    private val binding: ActivityRegisterPatientBinding
        get() = _binding!!

    private lateinit var currFragment: NavigationAdapter

    private lateinit var navHostFragment: NavHostFragment

    private val sharedViewModel: SharedViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterPatientBinding.inflate(layoutInflater)
        setContentView(binding.root)
        (application as CHOApplication).addActivity(this)
        val photoUriString = intent.getStringExtra("photoUri")
        val faceVector = intent.getFloatArrayExtra("facevector")
        sharedViewModel.setPhotoUri(photoUriString ?: "")
        sharedViewModel.setFaceVector(faceVector ?: floatArrayOf())

        navHostFragment = supportFragmentManager.findFragmentById(binding.patientRegistration.id) as NavHostFragment

        navHostFragment.navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.patientDetailsFragment -> {
                    binding.headerTextRegisterPatient.text = resources.getString(R.string.personal_information)
                    binding.btnSubmit.text = resources.getString(R.string.submit_btn_text)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }
            }
        }

        binding.homeButton.setOnClickListener {
            this.finish()
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
    override fun onDestroy() {
        super.onDestroy()
        (application as CHOApplication).activityList.remove(this)
    }
}