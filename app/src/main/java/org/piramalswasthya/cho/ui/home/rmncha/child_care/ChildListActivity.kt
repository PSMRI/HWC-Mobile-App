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
import org.piramalswasthya.cho.adapter.ChildListAdapter
import org.piramalswasthya.cho.databinding.ActivityChildListBinding
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.utils.filterPatientsByQuery
import org.piramalswasthya.cho.utils.setupSearchTextWatcher
import org.piramalswasthya.cho.utils.updateListUI
import org.piramalswasthya.cho.utils.setupToolbarWithBack
import javax.inject.Inject

/**
 * Activity to display list of children (age 2-5 years)
 * Shows all registered children for child care services
 */
@AndroidEntryPoint
class ChildListActivity : AppCompatActivity() {

    @Inject
    lateinit var patientRepo: PatientRepo

    private lateinit var binding: ActivityChildListBinding
    private lateinit var adapter: ChildListAdapter
    private var allChildren: List<PatientDisplay> = emptyList()
    private var filteredChildren: List<PatientDisplay> = emptyList()

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, ChildListActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChildListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setupToolbarWithBack(binding.toolbar, getString(R.string.child_list))

        setupRecyclerView()
        setupSearch()
        observeChildren()
    }

    private fun setupRecyclerView() {
        adapter = ChildListAdapter(
            ChildListAdapter.ClickListener { patient ->
                // Handle click - navigate to child details/form
                // TODO: Navigate to child form or details screen
            }
        )

        binding.rvChildList.layoutManager = LinearLayoutManager(this)
        binding.rvChildList.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.setupSearchTextWatcher { query ->
            filterChildren(query)
        }
    }

    private fun observeChildren() {
        lifecycleScope.launch {
            patientRepo.getChildList().collectLatest { childrenList ->
                allChildren = childrenList.sortedByDescending { it.patient.dob?.time ?: 0L }
                filteredChildren = allChildren
                updateUI()
            }
        }
    }

    private fun filterChildren(query: String) {
        filteredChildren = allChildren.filterPatientsByQuery(query) { it.patient }
        updateUI()
    }

    private fun updateUI() {
        adapter.submitList(filteredChildren)
        updateListUI(
            filteredList = filteredChildren,
            emptyStateView = binding.flEmpty,
            recyclerView = binding.rvChildList,
            countTextView = binding.tvCount,
            resultString = getString(R.string.result),
            logMessage = "Displaying children"
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
