package org.piramalswasthya.cho.ui.home.rmncha.child_care

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.ChildrenUnderFiveYearsAdapter
import org.piramalswasthya.cho.databinding.ActivityChildrenUnderFiveYearsListBinding
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.utils.filterPatientsByQuery
import org.piramalswasthya.cho.utils.setupSearchTextWatcher
import org.piramalswasthya.cho.utils.updateListUI
import org.piramalswasthya.cho.utils.setupToolbarWithBack
import javax.inject.Inject

/**
 * Activity to display list of children under 5 years (age <= 5 years).
 * This combines infants and young children into a single list for child care services.
 */
@AndroidEntryPoint
class ChildrenUnderFiveYearsActivity : AppCompatActivity() {

    @Inject
    lateinit var patientRepo: PatientRepo

    private lateinit var binding: ActivityChildrenUnderFiveYearsListBinding
    private lateinit var adapter: ChildrenUnderFiveYearsAdapter
    private var allChildren: List<PatientDisplay> = emptyList()
    private var filteredChildren: List<PatientDisplay> = emptyList()

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, ChildrenUnderFiveYearsActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChildrenUnderFiveYearsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setupToolbarWithBack(binding.toolbar, getString(R.string.children_under_5_years))

        setupRecyclerView()
        setupSearch()
        observeChildrenUnderFive()
    }

    private fun setupRecyclerView() {
        adapter = ChildrenUnderFiveYearsAdapter(
            ChildrenUnderFiveYearsAdapter.ClickListener(
                onCheckSam = { _ -> /* TODO: Hook up Check SAM flow when implemented */ },
                onOrs = { _ -> /* TODO: Hook up ORS flow when implemented */ },
                onIfa = { _ -> /* TODO: Hook up IFA flow when implemented */ }
            )
        )

        binding.rvChildrenUnderFiveList.layoutManager = LinearLayoutManager(this)
        binding.rvChildrenUnderFiveList.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.setupSearchTextWatcher { query ->
            filterChildren(query)
        }
    }

    private fun observeChildrenUnderFive() {
        lifecycleScope.launch {
            patientRepo.getChildrenUnderFiveList().collectLatest { childrenList ->
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
            recyclerView = binding.rvChildrenUnderFiveList,
            countTextView = binding.tvCount,
            resultString = getString(R.string.result),
            logMessage = "Displaying children under 5 years"
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

