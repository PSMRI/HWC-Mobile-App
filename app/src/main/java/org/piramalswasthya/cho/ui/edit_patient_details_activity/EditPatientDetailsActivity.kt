package org.piramalswasthya.cho.ui.edit_patient_details_activity

import android.content.Intent
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.ActivityEditPatientDetailsBinding
import org.piramalswasthya.cho.helpers.MyContextWrapper
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.ui.commons.patient_home.PatientHomeFragmentDirections
import javax.inject.Inject


@AndroidEntryPoint
class EditPatientDetailsActivity: AppCompatActivity() {
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

    private lateinit var viewModel: EditPatientDetailsViewModel

    private lateinit var currFragment: NavigationAdapter

    private lateinit var navHostFragment: NavHostFragment

    @Inject
    lateinit var preferenceDao: PreferenceDao

    private var _binding : ActivityEditPatientDetailsBinding? = null

    private val defaultValue = -1

    private val binding  : ActivityEditPatientDetailsBinding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
//        val patientId = intent.getStringExtra("patientId")
//        val args = Bundle()
//        args.putString("patientId", patientId)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        super.onCreate(savedInstanceState)
        _binding = ActivityEditPatientDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(EditPatientDetailsViewModel::class.java)

      if(preferenceDao.isUserOnlyDoctorOrMo() || (preferenceDao.isCHO() && preferenceDao.getCHOSecondRole() == "Doctor")){
          binding.patientDetalis.visibility= View.GONE
          binding.onlyDoctor.visibility=View.VISIBLE
      }
//        navHostFragment.navController.setGraph(R.navigation.nav_edit_patient, args);
       if(preferenceDao.isUserOnlyDoctorOrMo() || (preferenceDao.isCHO() && preferenceDao.getCHOSecondRole() == "Doctor")){
           navHostFragment = supportFragmentManager.findFragmentById(binding.onlyDoctor.id) as NavHostFragment
           navHostFragment.navController.addOnDestinationChangedListener { controller, destination, arguments ->
               when (destination.id) {
                   R.id.caseRecordCustom ->{
//                       binding.headerTextEditPatient.text = resources.getString(R.string.case_record_text)
                       binding.headerTextRegisterPatient.text = resources.getString(R.string.case_record_text)
                       binding.btnSubmit.text = resources.getString(R.string.submit)
                       binding.btnCancel.text = resources.getString(R.string.cancel)
                   }
               }
           }
       }else {
           navHostFragment = supportFragmentManager.findFragmentById(binding.patientDetalis.id) as NavHostFragment
           if (preferenceDao.isStartingLabTechnician() || (preferenceDao.isCHO() && preferenceDao.getCHOSecondRole() == "Lab Technician")) {
               navHostFragment.navController
                   .navigate(PatientHomeFragmentDirections.actionPatientHomeFragmentToLabTechnicianFormFragment(
                       (intent?.getSerializableExtra("benVisitInfo") as PatientDisplayWithVisitInfo)
                   ))
           }
           else if (preferenceDao.isPharmacist() || (preferenceDao.isCHO() && preferenceDao.getCHOSecondRole() == "Pharmacist")) {
               navHostFragment.navController
                   .navigate(PatientHomeFragmentDirections.actionPatientHomeFragmentToPharmacistFormFragment(
                       (intent?.getSerializableExtra("benVisitInfo") as PatientDisplayWithVisitInfo)
                   ))
           }
           else {
               navHostFragment.navController
                   .navigate(PatientHomeFragmentDirections.actionPatientHomeFragmentToFhirVisitDetailsFragment(
                       (intent?.getSerializableExtra("benVisitInfo") as PatientDisplayWithVisitInfo)
                   ))
           }
           navHostFragment.navController.addOnDestinationChangedListener { controller, destination, arguments ->
               when (destination.id) {
                   R.id.fhirVisitDetailsFragment -> {
                       binding.headerTextRegisterPatient.text =
                           resources.getString(R.string.visit_details)
                       binding.btnSubmit.text = resources.getString(R.string.next)
                       binding.btnCancel.text = resources.getString(R.string.cancel)
                   }
//                R.id.historyCustomFragment -> {
//                    binding.headerTextEditPatient.text = resources.getString(R.string.history_text)
//                    binding.btnSubmit.text = resources.getString(R.string.next)
//                    binding.btnCancel.text = resources.getString(R.string.cancel)
//                }
                   R.id.customVitalsFragment -> {
//                       binding.headerTextEditPatient.text =
//                           resources.getString(R.string.vitals_text)
                       binding.headerTextRegisterPatient.text = resources.getString(R.string.vitals_text)
                       binding.btnCancel.text = resources.getString(R.string.cancel)
                       if (preferenceDao.isUserNurseOrCHOAndDoctorOrMo() && !preferenceDao.isCHO()) {
                           binding.btnSubmit.text = resources.getString(R.string.next)
                       }
                       else {
                           binding.btnSubmit.text =
                               resources.getString(R.string.submit_to_doctor_text)
                       }
                   }
//                R.id.examinationFragment ->{
//                    binding.headerTextEditPatient.text = resources.getString(R.string.examination_text)
//                    binding.btnSubmit.text = resources.getString(R.string.submit)
//                    binding.btnCancel.text = resources.getString(R.string.cancel)
//                }
//                R.id.fhirPrescriptionFragment -> {
//                    binding.headerTextEditPatient.text = resources.getString(R.string.prescription_text)
//                    binding.btnSubmit.text = resources.getString(R.string.submit)
//                    binding.btnCancel.text = resources.getString(R.string.cancel)
//                }
//                R.id.fhirRevisitFormFragment ->{
//                    binding.headerTextEditPatient.text = resources.getString(R.string.revisit_details_text)
//                    binding.btnSubmit.text = resources.getString(R.string.next)
//                    binding.btnCancel.text = resources.getString(R.string.cancel)
//                }
                   R.id.caseRecordCustom -> {
//                       binding.headerTextEditPatient.text =
//                           resources.getString(R.string.case_record_text)
                       binding.headerTextRegisterPatient.text = resources.getString(R.string.case_record_text)
                       binding.btnSubmit.text = resources.getString(R.string.submit)
                       binding.btnCancel.text = resources.getString(R.string.cancel)
                   }
                   R.id.labTechnicianFormFragment -> {
                       binding.headerTextRegisterPatient.text =
                           resources.getString(R.string.lab_record_text)
                       binding.btnSubmit.text = resources.getString(R.string.submit)
                       binding.btnCancel.text = resources.getString(R.string.cancel)
                   }
                   R.id.pharmacistFormFragment -> {
                       binding.headerTextRegisterPatient.text =
                           resources.getString(R.string.pharmacist_record_text)
                       binding.btnSubmit.text = resources.getString(R.string.submit)
                       binding.btnCancel.text = resources.getString(R.string.cancel)
                   }
               }
           }
       }

        binding.homeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
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

        viewModel.submitActive.observe(this) {
            binding.btnSubmit.isEnabled = it
        }
    }

//    override fun onBackPressed() {
//        finish()
//    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
