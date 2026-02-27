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
import org.piramalswasthya.cho.CHOApplication
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.ActivityEditPatientDetailsBinding
import org.piramalswasthya.cho.helpers.MyContextWrapper
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.ui.commons.patient_home.PatientHomeFragmentDirections
import timber.log.Timber
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
        (application as CHOApplication).addActivity(this)

        super.onCreate(savedInstanceState)
        _binding = ActivityEditPatientDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(EditPatientDetailsViewModel::class.java)

        navHostFragment = supportFragmentManager.findFragmentById(binding.patientDetalis.id) as NavHostFragment

        handleInitialNavigation()
        setupNavigationListener()
        setupUIListeners()
    }

    private fun handleInitialNavigation() {
        val navigateTo = intent.getStringExtra("navigateTo")
        when {
            navigateTo == "ANC" -> {
                val patientID = intent.getStringExtra("patientID") ?: ""
                val visitNumber = intent.getIntExtra("visitNumber", 1)
                val isOldVisit = intent.getBooleanExtra("isOldVisit", false)

                navHostFragment.navController.navigate(
                    R.id.pwAncFormFragment,
                    Bundle().apply {
                        putString("patientID", patientID)
                        putInt("visitNumber", visitNumber)
                        putBoolean("isOldVisit", isOldVisit)
                    }
                )
            }

            navigateTo == "PNC" -> {
                val patientID = intent.getStringExtra("patientID") ?: ""
                val visitNumber = intent.getIntExtra("visitNumber", 1)

                navHostFragment.navController.navigate(
                    R.id.pncFormFragment,
                    Bundle().apply {
                        putString("patientID", patientID)
                        putInt("visitNumber", visitNumber)
                    }
                )
            }

            preferenceDao.isDoctorSelected() -> {
                val viewRecord = intent?.getBooleanExtra("viewRecord", false) ?: false
                val isFlowComplete = intent?.getBooleanExtra("isFlowComplete", false) ?: false
                val isFollowupVisit = intent?.getBooleanExtra("isFollowupVisit", false) ?: false
                navHostFragment.navController
                    .navigate(
                        R.id.caseRecordCustom, Bundle().apply {
                            putBoolean("viewRecord", viewRecord)
                            putBoolean("isFlowComplete", isFlowComplete)
                            putBoolean("isFollowupVisit", isFollowupVisit)
                            putSerializable(
                                "benVisitInfo",
                                (intent?.getSerializableExtra("benVisitInfo") as? PatientDisplayWithVisitInfo)
                            )
                        }
                    )
            }

            preferenceDao.isLabSelected() -> {
                (intent?.getSerializableExtra("benVisitInfo") as? PatientDisplayWithVisitInfo)?.let {
                    navHostFragment.navController.navigate(
                        PatientHomeFragmentDirections.actionPatientHomeFragmentToLabTechnicianFormFragment(
                            it
                        )
                    )
                }
            }

            preferenceDao.isPharmaSelected() -> {
                (intent?.getSerializableExtra("benVisitInfo") as? PatientDisplayWithVisitInfo)?.let {
                    navHostFragment.navController.navigate(
                        PatientHomeFragmentDirections.actionPatientHomeFragmentToPharmacistFormFragment(
                            it
                        )
                    )
                }
            }

            navigateTo == "VITALS" -> {
                val benVisitInfo =
                    intent?.getSerializableExtra("benVisitInfo") as? PatientDisplayWithVisitInfo
                if (benVisitInfo != null) {
                    val masterDb =
                        org.piramalswasthya.cho.model.MasterDb(benVisitInfo.patient.patientID).apply {
                            visitMasterDb.category = "RMNCH"
                            visitMasterDb.subCategory =
                                org.piramalswasthya.cho.ui.commons.DropdownConst.careAndPreg
                            visitMasterDb.reason =
                                org.piramalswasthya.cho.ui.commons.DropdownConst.anc
                        }
                    navHostFragment.navController.navigate(
                        R.id.customVitalsFragment,
                        Bundle().apply {
                            putSerializable("MasterDb", masterDb)
                        }
                    )
                }
            }

            else -> {
                val benVisitInfo =
                    intent?.getSerializableExtra("benVisitInfo") as? PatientDisplayWithVisitInfo
                if (benVisitInfo != null) {
                    navHostFragment.navController.navigate(
                        PatientHomeFragmentDirections.actionPatientHomeFragmentToFhirVisitDetailsFragment(
                            benVisitInfo
                        )
                    )
                } else {
                    Timber.e("No benVisitInfo provided for navigation")
                }
            }
        }
    }

    private fun setupNavigationListener() {
        navHostFragment.navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.fhirVisitDetailsFragment -> {
                    binding.bottomNavigation.visibility = View.GONE
                    binding.headerTextRegisterPatient.text =
                        resources.getString(R.string.visit_details)
                    binding.btnSubmit.text = resources.getString(R.string.next)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }

                R.id.customVitalsFragment -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                    binding.headerTextRegisterPatient.text =
                        resources.getString(R.string.vitals_text)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                    if (preferenceDao.isUserCHO()) {
                        binding.btnSubmit.text = resources.getString(R.string.next)
                    } else {
                        binding.btnSubmit.text =
                            resources.getString(R.string.submit_to_doctor_text)
                    }
                }

                R.id.pregnantWomanRegistrationFragment, R.id.pregnancyRegistrationFormFragment -> {
                    binding.headerTextRegisterPatient.text =
                        resources.getString(R.string.title_register_pregnancy)
                    binding.bottomNavigation.visibility = View.GONE
                    binding.linearLayout.visibility = View.GONE
                    binding.btnSubmit.text = resources.getString(R.string.submit)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }

                R.id.cbacFragment -> {
                    binding.headerTextRegisterPatient.text = resources.getString(R.string.cbac)
                    binding.bottomNavigation.visibility = View.GONE
                    binding.btnSubmit.text = resources.getString(R.string.submit)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }

                R.id.pwAncFormFragment -> {
                    binding.headerTextRegisterPatient.text = resources.getString(R.string.anc)
                    binding.bottomNavigation.visibility = View.VISIBLE
                    binding.btnSubmit.text = resources.getString(R.string.submit)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }

                R.id.pncFormFragment -> {
                    binding.headerTextRegisterPatient.text = resources.getString(R.string.pnc)
                    binding.bottomNavigation.visibility = View.VISIBLE
                    binding.btnSubmit.text = resources.getString(R.string.submit)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }

                R.id.immunizationFormFragment -> {
                    binding.headerTextRegisterPatient.text =
                        resources.getString(R.string.immunization)
                    binding.bottomNavigation.visibility = View.VISIBLE
                    binding.btnSubmit.text = resources.getString(R.string.submit)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }

                R.id.eligibleCoupleTrackingFormFragment -> {
                    binding.headerTextRegisterPatient.text =
                        resources.getString(R.string.eligible_couple_tracking)
                    binding.bottomNavigation.visibility = View.GONE
                    binding.linearLayout.visibility = View.GONE
                    binding.btnSubmit.text = resources.getString(R.string.submit)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }

                R.id.caseRecordCustom -> {
                    binding.headerTextRegisterPatient.text =
                        resources.getString(R.string.case_record_text)
                    binding.btnSubmit.text = resources.getString(R.string.submit)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }

                R.id.labTechnicianFormFragment -> {
                    binding.headerTextRegisterPatient.text =
                        resources.getString(R.string.lab_record_text)
                    binding.btnSubmit.text = resources.getString(R.string.submit)
                    binding.btnCancel.text = resources.getString(R.string.cancel)
                }

                R.id.selectBatchFragment -> {
                    binding.headerTextRegisterPatient.text = getString(R.string.batch_selection)
                    binding.btnSubmit.text = resources.getString(R.string.save)
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

    private fun setupUIListeners() {
        binding.homeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            this.finish()
        }

        binding.btnSubmit.setOnClickListener {
            currFragment =
                navHostFragment.childFragmentManager.primaryNavigationFragment as NavigationAdapter
            currFragment.onSubmitAction()
        }

        binding.btnCancel.setOnClickListener {
            currFragment =
                navHostFragment.childFragmentManager.primaryNavigationFragment as NavigationAdapter
            currFragment.onCancelAction()
        }

        viewModel.submitActive.observe(this) {
            binding.btnSubmit.isEnabled = it
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        (application as CHOApplication).activityList.remove(this)
    }
}
