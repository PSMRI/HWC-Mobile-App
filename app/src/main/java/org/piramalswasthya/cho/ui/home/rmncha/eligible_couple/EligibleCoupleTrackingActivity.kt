package org.piramalswasthya.cho.ui.home.rmncha.eligible_couple

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
import org.piramalswasthya.cho.adapter.ECRegistrationAdapter
import org.piramalswasthya.cho.databinding.ActivityEligibleCoupleTrackingBinding
import org.piramalswasthya.cho.model.PatientWithEcrDomain
import org.piramalswasthya.cho.repositories.EcrRepo
import org.piramalswasthya.cho.utils.filterPatientsByQuery
import org.piramalswasthya.cho.utils.setupSearchTextWatcher
import org.piramalswasthya.cho.utils.updateListUI
import org.piramalswasthya.cho.utils.setupToolbarWithBack
import javax.inject.Inject

/**
 * Activity to display list of Eligible Couples for Tracking
 * Shows only registered women (with ECR data) for status tracking
 */
@AndroidEntryPoint
class EligibleCoupleTrackingActivity : AppCompatActivity() {

    @Inject
    lateinit var ecrRepo: EcrRepo

    private lateinit var binding: ActivityEligibleCoupleTrackingBinding
    private lateinit var adapter: ECRegistrationAdapter
    private var allPatients: List<PatientWithEcrDomain> = emptyList()
    private var filteredPatients: List<PatientWithEcrDomain> = emptyList()

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, EligibleCoupleTrackingActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEligibleCoupleTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setupToolbarWithBack(binding.toolbar, getString(R.string.eligible_couple_tracking))

        setupRecyclerView()
        setupSearch()
        observePatients()
    }

    private fun setupRecyclerView() {
        adapter = ECRegistrationAdapter(
            ECRegistrationAdapter.ClickListener { patientWithEcr ->
                // Handle click - navigate to tracking/view form
                Toast.makeText(
                    this,
                    "Track: ${patientWithEcr.patient.firstName}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        binding.rvEligibleCouples.layoutManager = LinearLayoutManager(this)
        binding.rvEligibleCouples.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.setupSearchTextWatcher { query ->
            filterPatients(query)
        }
    }

    private fun observePatients() {
        lifecycleScope.launch {
            ecrRepo.getAllPatientsWithECR().collectLatest { patientsList ->
                // Filter for registered eligible couples (must have ECR data)
                allPatients = patientsList
                    .map { it.asDomainModel() }
                    .filter { patient ->
                        // Filter criteria:
                        // 1. Must have ECR registration data (registered couples only)
                        // 2. Female gender (genderID = 2)
                        // 3. Age between 15-49 years (reproductive age)
                        val hasECR = patient.ecr != null
                        val isFemale = patient.patient.genderID == 2
                        val age = patient.patient.age ?: 0
                        val isReproductiveAge = age in 15..49

                        hasECR && isFemale && isReproductiveAge
                    }
                    .sortedByDescending { it.ecr?.dateOfReg ?: 0L }

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
            recyclerView = binding.rvEligibleCouples,
            countTextView = binding.tvCount,
            resultString = getString(R.string.result),
            logMessage = "Displaying eligible couples for tracking"
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
