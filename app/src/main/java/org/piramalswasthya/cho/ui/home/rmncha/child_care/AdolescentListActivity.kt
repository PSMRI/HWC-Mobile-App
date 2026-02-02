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
import org.piramalswasthya.cho.adapter.AdolescentListAdapter
import org.piramalswasthya.cho.databinding.ActivityAdolescentListBinding
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.utils.filterPatientsByQuery
import org.piramalswasthya.cho.utils.setupSearchTextWatcher
import org.piramalswasthya.cho.utils.updateListUI
import org.piramalswasthya.cho.utils.setupToolbarWithBack
import javax.inject.Inject

/**
 * Activity to display list of adolescents (age 6-14 years)
 * Shows all registered adolescents for child care services
 */
@AndroidEntryPoint
class AdolescentListActivity : AppCompatActivity() {

    @Inject
    lateinit var patientRepo: PatientRepo

    private lateinit var binding: ActivityAdolescentListBinding
    private lateinit var adapter: AdolescentListAdapter
    private var allAdolescents: List<PatientDisplay> = emptyList()
    private var filteredAdolescents: List<PatientDisplay> = emptyList()

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, AdolescentListActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdolescentListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setupToolbarWithBack(binding.toolbar, getString(R.string.adolescent_list))

        setupRecyclerView()
        setupSearch()
        observeAdolescents()
    }

    private fun setupRecyclerView() {
        adapter = AdolescentListAdapter(
            AdolescentListAdapter.ClickListener { patient ->
                // Handle click - navigate to adolescent details/form
                // TODO: Navigate to adolescent form or details screen
            }
        )

        binding.rvAdolescentList.layoutManager = LinearLayoutManager(this)
        binding.rvAdolescentList.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.setupSearchTextWatcher { query ->
            filterAdolescents(query)
        }
    }

    private fun observeAdolescents() {
        lifecycleScope.launch {
            patientRepo.getAdolescentList().collectLatest { adolescentsList ->
                allAdolescents = adolescentsList.sortedByDescending { it.patient.dob?.time ?: 0L }
                filteredAdolescents = allAdolescents
                updateUI()
            }
        }
    }

    private fun filterAdolescents(query: String) {
        filteredAdolescents = allAdolescents.filterPatientsByQuery(query) { it.patient }
        updateUI()
    }

    private fun updateUI() {
        adapter.submitList(filteredAdolescents)
        updateListUI(
            filteredList = filteredAdolescents,
            emptyStateView = binding.flEmpty,
            recyclerView = binding.rvAdolescentList,
            countTextView = binding.tvCount,
            resultString = getString(R.string.result),
            logMessage = "Displaying adolescents"
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
