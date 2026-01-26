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
import org.piramalswasthya.cho.adapter.DeliveryOutcomeAdapter
import org.piramalswasthya.cho.databinding.ActivityDeliveryOutcomeBinding
import org.piramalswasthya.cho.model.PatientWithPwrDomain
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import org.piramalswasthya.cho.utils.filterPatientsByQuery
import org.piramalswasthya.cho.utils.setupSearchTextWatcher
import org.piramalswasthya.cho.utils.updateListUI
import org.piramalswasthya.cho.utils.setupToolbarWithBack
import timber.log.Timber
import javax.inject.Inject

/**
 * Activity to display list of Delivery Outcomes
 * Shows pregnant women who have delivered (pregnantWomanDelivered = true in ANC)
 */
@AndroidEntryPoint
class DeliveryOutcomeActivity : AppCompatActivity() {

    @Inject
    lateinit var maternalHealthRepo: MaternalHealthRepo

    private lateinit var binding: ActivityDeliveryOutcomeBinding
    private lateinit var adapter: DeliveryOutcomeAdapter
    private var allPatients: List<PatientWithPwrDomain> = emptyList()
    private var filteredPatients: List<PatientWithPwrDomain> = emptyList()

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, DeliveryOutcomeActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeliveryOutcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setupToolbarWithBack(binding.toolbar, getString(R.string.delivery_outcome))

        setupRecyclerView()
        setupSearch()
        observePatients()
    }

    private fun setupRecyclerView() {
        adapter = DeliveryOutcomeAdapter(
            DeliveryOutcomeAdapter.ClickListener { patientWithPwr ->
                // Handle click - navigate to delivery outcome form
                Toast.makeText(
                    this,
                    "View Delivery Outcome: ${patientWithPwr.patient.firstName}",
                    Toast.LENGTH_SHORT
                ).show()
                // TODO: Navigate to Delivery Outcome Form
            }
        )

        binding.rvDeliveryOutcome.layoutManager = LinearLayoutManager(this)
        binding.rvDeliveryOutcome.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.setupSearchTextWatcher { query ->
            filterPatients(query)
        }
    }

    private fun observePatients() {
        lifecycleScope.launch {
            maternalHealthRepo.getAllDeliveredWomen().collectLatest { patientsList ->
                // Filter for delivered women (already filtered by DAO query)
                allPatients = patientsList
                    .map { it.asDomainModel() }
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
            recyclerView = binding.rvDeliveryOutcome,
            countTextView = binding.tvCount,
            resultString = getString(R.string.result),
            logMessage = "Displaying delivered women"
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
