package org.piramalswasthya.cho.ui.home.rmncha

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.PatientItemAdapter
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.ActivityAllBeneficiariesBinding
import org.piramalswasthya.cho.network.ESanjeevaniApiService
import org.piramalswasthya.cho.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.cho.ui.commons.personal_details.PersonalDetailsViewModel
import org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity
import timber.log.Timber
import javax.inject.Inject

/**
 * Activity to display All Beneficiaries list
 * Reuses the same adapter and card design as PersonalDetailsFragment
 */
@AndroidEntryPoint
class AllBeneficiariesActivity : AppCompatActivity() {

    @Inject
    lateinit var apiService: ESanjeevaniApiService

    @Inject
    lateinit var preferenceDao: PreferenceDao

    private lateinit var binding: ActivityAllBeneficiariesBinding
    private lateinit var viewModel: PersonalDetailsViewModel
    private var itemAdapter: PatientItemAdapter? = null
    private var patientCount = 0

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, AllBeneficiariesActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllBeneficiariesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.all_beneficiaries)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[PersonalDetailsViewModel::class.java]

        // Set up patient list
        setupPatientList()
        observePatients()
    }

    private fun setupPatientList() {
        itemAdapter = PatientItemAdapter(
            apiService,
            this,
            clickListener = PatientItemAdapter.BenClickListener(
                { benVisitInfo ->
                    // Main click on patient card - navigate to patient details
                    when {
                        preferenceDao.isRegistrarSelected() -> {
                            // No-Op: Registrar sees nothing here for now
                        }

                        benVisitInfo.nurseFlag == 9 && benVisitInfo.doctorFlag == 2 && preferenceDao.isDoctorSelected() -> {
                            Toast.makeText(
                                this,
                                getString(R.string.pendingForLabtech),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        benVisitInfo.nurseFlag == 9 && benVisitInfo.doctorFlag == 9 && preferenceDao.isDoctorSelected() -> {
                            val intent = Intent(
                                this, EditPatientDetailsActivity::class.java
                            ).apply {
                                putExtra("benVisitInfo", benVisitInfo)
                                putExtra("viewRecord", true)
                                putExtra("isFlowComplete", true)
                            }
                            startActivity(intent)
                        }

                        else -> {
                            val intent = Intent(
                                this, EditPatientDetailsActivity::class.java
                            ).apply {
                                putExtra("benVisitInfo", benVisitInfo)
                                putExtra("viewRecord", false)
                                putExtra("isFlowComplete", false)
                            }
                            startActivity(intent)
                        }
                    }
                },
                { benVisitInfo ->
                    // ABHA button click
                    val intent = Intent(this, AbhaIdActivity::class.java)
                    intent.putExtra("benRegId", benVisitInfo.patient.beneficiaryRegID)
                    startActivity(intent)
                },
                { benVisitInfo ->
                    // eSanjeevani button click
                    Toast.makeText(this, "eSanjeevani - Coming soon", Toast.LENGTH_SHORT).show()
                    Timber.d("eSanjeevani clicked")
                },
                { benVisitInfo ->
                    // Download prescription button click
                    Toast.makeText(this, "Download Prescription - Coming soon", Toast.LENGTH_SHORT).show()
                    Timber.d("Download prescription clicked")
                },
                { benVisitInfo ->
                    // Sync button click
                    Toast.makeText(this, "Sync - Coming soon", Toast.LENGTH_SHORT).show()
                    Timber.d("Sync clicked")
                }
            ),
            showAbha = true
        )

        binding.patientListContainer.patientList.adapter = itemAdapter
    }

    private fun observePatients() {
        viewModel.patientObserver.observe(this) { state ->
            when (state!!) {
                PersonalDetailsViewModel.NetworkState.SUCCESS -> {
                    // Patient list loaded successfully
                    Timber.d("Patients loaded successfully")
                }

                PersonalDetailsViewModel.NetworkState.FAILURE -> {
                    Timber.e("Failed to load patients")
                }

                PersonalDetailsViewModel.NetworkState.LOADING -> {
                    Timber.d("Loading patients...")
                }

                else -> {
                    // No action needed for other states
                }
            }
        }

        // Observe patient list based on user role
        when {
            preferenceDao.isRegistrarSelected() || preferenceDao.isNurseSelected() -> {
                lifecycleScope.launch {
                    viewModel.patientListForNurse?.collect { patientList ->
                        val sortedList = patientList.sortedByDescending { item ->
                            item.patient.registrationDate
                        }
                        patientCount = sortedList.size
                        itemAdapter?.submitList(sortedList)
                        binding.patientListContainer.patientCount.text =
                            sortedList.size.toString() + getResultStr(sortedList.size)
                    }
                }
            }

            preferenceDao.isDoctorSelected() -> {
                lifecycleScope.launch {
                    viewModel.patientListForDoctor?.collect { patientList ->
                        val sortedList = patientList.sortedByDescending { item ->
                            item.patient.registrationDate
                        }
                        patientCount = sortedList.size
                        itemAdapter?.submitList(sortedList)
                        binding.patientListContainer.patientCount.text =
                            sortedList.size.toString() + getResultStr(sortedList.size)
                    }
                }
            }

            preferenceDao.isLabSelected() -> {
                lifecycleScope.launch {
                    viewModel.patientListForLab?.collect { patientList ->
                        val sortedList = patientList.sortedByDescending { item ->
                            item.patient.registrationDate
                        }
                        patientCount = sortedList.size
                        itemAdapter?.submitList(sortedList)
                        binding.patientListContainer.patientCount.text =
                            sortedList.size.toString() + getResultStr(sortedList.size)
                    }
                }
            }

            preferenceDao.isPharmaSelected() -> {
                lifecycleScope.launch {
                    viewModel.patientListForPharmacist?.collect { patientList ->
                        val sortedList = patientList.sortedByDescending { item ->
                            item.patient.registrationDate
                        }
                        patientCount = sortedList.size
                        itemAdapter?.submitList(sortedList)
                        binding.patientListContainer.patientCount.text =
                            itemAdapter?.itemCount.toString() + getResultStr(itemAdapter?.itemCount)
                    }
                }
            }
        }
    }

    private fun getResultStr(count: Int?): String {
        return if (count == null || count == 1) {
            " ${getString(R.string.result)}"
        } else {
            " ${getString(R.string.result)}s"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
