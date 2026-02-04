package org.piramalswasthya.cho.ui.home.rmncha.child_care

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.InfantListAdapter
import org.piramalswasthya.cho.databinding.ActivityInfantListBinding
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.utils.filterPatientsByQuery
import org.piramalswasthya.cho.utils.setupSearchTextWatcher
import org.piramalswasthya.cho.utils.updateListUI
import org.piramalswasthya.cho.utils.setupToolbarWithBack
import javax.inject.Inject

/**
 * Activity to display list of infants (age <= 61 days)
 * Shows all registered infants for child care services
 */
@AndroidEntryPoint
class InfantListActivity : AppCompatActivity() {

    @Inject
    lateinit var patientRepo: PatientRepo

    private lateinit var binding: ActivityInfantListBinding
    private lateinit var adapter: InfantListAdapter
    private var allInfants: List<PatientDisplay> = emptyList()
    private var filteredInfants: List<PatientDisplay> = emptyList()

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, InfantListActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfantListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setupToolbarWithBack(binding.toolbar, getString(R.string.infant_list))

        setupRecyclerView()
        setupSearch()
        observeInfants()
    }

    private fun setupRecyclerView() {
        adapter = InfantListAdapter(
            InfantListAdapter.ClickListener { patient ->
                // Handle click - navigate to infant details/form
                // TODO: Navigate to infant form or details screen
            }
        )

        binding.rvInfantList.layoutManager = LinearLayoutManager(this)
        binding.rvInfantList.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.setupSearchTextWatcher { query ->
            filterInfants(query)
        }
    }

    private fun observeInfants() {
        lifecycleScope.launch {
            patientRepo.getInfantList().collectLatest { infantsList ->
                allInfants = infantsList.sortedByDescending { it.patient.dob?.time ?: 0L }
                filteredInfants = allInfants
                updateUI()
            }
        }
    }

    private fun filterInfants(query: String) {
        filteredInfants = allInfants.filterPatientsByQuery(query) { it.patient }
        updateUI()
    }

    private fun updateUI() {
        adapter.submitList(filteredInfants)
        updateListUI(
            filteredList = filteredInfants,
            emptyStateView = binding.flEmpty,
            recyclerView = binding.rvInfantList,
            countTextView = binding.tvCount,
            resultString = getString(R.string.result),
            logMessage = "Displaying infants"
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
