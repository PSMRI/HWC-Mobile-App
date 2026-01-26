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
import org.piramalswasthya.cho.adapter.PNCMotherAdapter
import org.piramalswasthya.cho.databinding.ActivityPncMotherListBinding
import org.piramalswasthya.cho.model.PatientWithPncDomain
import org.piramalswasthya.cho.repositories.PncRepo
import org.piramalswasthya.cho.utils.filterPatientsByQuery
import org.piramalswasthya.cho.utils.setupSearchTextWatcher
import org.piramalswasthya.cho.utils.updateListUI
import org.piramalswasthya.cho.utils.setupToolbarWithBack
import javax.inject.Inject

/**
 * Activity to display list of PNC Mothers
 * Shows women who have delivered and are eligible for PNC visits (within 42 days or not completed all visits)
 */
@AndroidEntryPoint
class PNCMotherListActivity : AppCompatActivity() {

    @Inject
    lateinit var pncRepo: PncRepo

    private lateinit var binding: ActivityPncMotherListBinding
    private lateinit var adapter: PNCMotherAdapter
    private var allPatients: List<PatientWithPncDomain> = emptyList()
    private var filteredPatients: List<PatientWithPncDomain> = emptyList()

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, PNCMotherListActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPncMotherListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setupToolbarWithBack(binding.toolbar, getString(R.string.pnc_mother_list))

        setupRecyclerView()
        setupSearch()
        observePatients()
    }

    private fun setupRecyclerView() {
        adapter = PNCMotherAdapter(
            PNCMotherAdapter.ClickListener { patientWithPnc ->
                // Handle click - navigate to PNC form
                Toast.makeText(
                    this,
                    "View PNC: ${patientWithPnc.patient.firstName}",
                    Toast.LENGTH_SHORT
                ).show()
                // TODO: Navigate to PNC Form
            }
        )

        binding.rvPncMothers.layoutManager = LinearLayoutManager(this)
        binding.rvPncMothers.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.setupSearchTextWatcher { query ->
            filterPatients(query)
        }
    }

    private fun observePatients() {
        lifecycleScope.launch {
            pncRepo.getAllPNCMothers().collectLatest { patientsList ->
                // Filter for PNC-eligible mothers (already filtered by DAO query)
                allPatients = patientsList
                    .map { it.asDomainModel() }
                    .filter { it.isEligibleForPNC() } // Additional filter check
                    .sortedByDescending { it.deliveryOutcome?.dateOfDelivery ?: 0L }

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
            recyclerView = binding.rvPncMothers,
            countTextView = binding.tvCount,
            resultString = getString(R.string.result),
            logMessage = "Displaying PNC mothers"
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
