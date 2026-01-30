package org.piramalswasthya.cho.ui.home.rmncha.maternal_health

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.ANCVisitsAdapter
import org.piramalswasthya.cho.databinding.ActivityAncVisitsBinding
import org.piramalswasthya.cho.model.PatientWithPwrDomain
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import org.piramalswasthya.cho.utils.filterPatientsByQuery
import org.piramalswasthya.cho.utils.setupSearchTextWatcher
import org.piramalswasthya.cho.utils.updateListUI
import org.piramalswasthya.cho.utils.setupToolbarWithBack
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Activity to display list of ANC Visits
 * Shows registered pregnant women with LMP >= 5 weeks (eligible for ANC visits)
 */
@AndroidEntryPoint
class ANCVisitsActivity : AppCompatActivity() {

    @Inject
    lateinit var maternalHealthRepo: MaternalHealthRepo

    private lateinit var binding: ActivityAncVisitsBinding
    private lateinit var adapter: ANCVisitsAdapter
    private var allPatients: List<PatientWithPwrDomain> = emptyList()
    private var filteredPatients: List<PatientWithPwrDomain> = emptyList()

    companion object {
        private const val MIN_ANC_WEEKS = 5 // Minimum weeks for ANC eligibility
        private const val MIN_ANC_DAYS = MIN_ANC_WEEKS * 7 // 35 days

        fun getIntent(context: Context): Intent {
            return Intent(context, ANCVisitsActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAncVisitsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setupToolbarWithBack(binding.toolbar, getString(R.string.anc_visits))

        setupRecyclerView()
        setupSearch()
        observePatients()
    }

    private fun setupRecyclerView() {
        adapter = ANCVisitsAdapter(
            ANCVisitsAdapter.ClickListener(
                clickedAddANC = { patientWithPwr ->
                    Toast.makeText(
                        this,
                        "Add ANC Visit: ${patientWithPwr.patient.firstName}",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                clickedANCVisits = { patientWithPwr ->
                    Toast.makeText(
                        this,
                        "View ANC Visits: ${patientWithPwr.patient.firstName}",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                clickedAddPMSMA = { patientWithPwr ->
                    Toast.makeText(
                        this,
                        "Add PMSMA: ${patientWithPwr.patient.firstName}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        )

        binding.rvAncVisits.layoutManager = LinearLayoutManager(this)
        binding.rvAncVisits.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.setupSearchTextWatcher { query ->
            filterPatients(query)
        }
    }

    private fun observePatients() {
        lifecycleScope.launch {
            maternalHealthRepo.getAllPatientsWithPWR().collectLatest { patientsList ->
                // Filter for ANC-eligible pregnant women
                allPatients = patientsList
                    .map { it.asDomainModel() }
                    .filter { patient ->
                        // Filter criteria:
                        // 1. Must have active pregnancy registration
                        val hasActivePWR = patient.pwr != null && patient.isActive()
                        
                        // 2. Female gender (genderID = 2)
                        val isFemale = patient.patient.genderID == 2
                        
                        // 3. Age between 15-49 years (reproductive age)
                        val age = patient.patient.age ?: 0
                        val isReproductiveAge = age in 15..49
                        
                        // 4. LMP must be >= 5 weeks (35 days) ago - eligible for ANC
                        val isEligibleForANC = patient.pwr?.lmpDate?.let { lmpDate ->
                            val daysSinceLMP = TimeUnit.MILLISECONDS.toDays(
                                System.currentTimeMillis() - lmpDate
                            )
                            daysSinceLMP >= MIN_ANC_DAYS
                        } ?: false

                        hasActivePWR && isFemale && isReproductiveAge && isEligibleForANC
                    }
                    .sortedByDescending { it.pwr?.dateOfRegistration ?: 0L }

                filteredPatients = allPatients
                updateUI()
            }
        }
    }

    private fun filterPatients(query: String) {
        filteredPatients = allPatients.filterPatientsByQuery(query) { it.patient }
        updateUI()
    }

    private fun updateUI() {
        adapter.submitList(filteredPatients)
        updateListUI(
            filteredList = filteredPatients,
            emptyStateView = binding.flEmpty,
            recyclerView = binding.rvAncVisits,
            countTextView = binding.tvCount,
            resultString = getString(R.string.result),
            logMessage = "Displaying ANC-eligible pregnant women"
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
