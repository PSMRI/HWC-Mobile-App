package org.piramalswasthya.cho.ui.home.rmncha.maternal_health

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
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
import timber.log.Timber
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
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.pnc_mother_list)

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
        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterPatients(s?.toString() ?: "")
            }
        })
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
        filteredPatients = if (query.isBlank()) {
            allPatients
        } else {
            allPatients.filter { patient ->
                val firstName = patient.patient.firstName?.lowercase() ?: ""
                val lastName = patient.patient.lastName?.lowercase() ?: ""
                val spouseName = patient.patient.spouseName?.lowercase() ?: ""
                val phoneNo = patient.patient.phoneNo ?: ""
                val beneficiaryID = patient.patient.beneficiaryID?.toString() ?: ""

                val searchQuery = query.lowercase()

                firstName.contains(searchQuery) ||
                        lastName.contains(searchQuery) ||
                        spouseName.contains(searchQuery) ||
                        phoneNo.contains(searchQuery) ||
                        beneficiaryID.contains(searchQuery)
            }
        }
        updateUI()
    }

    private fun updateUI() {
        if (filteredPatients.isEmpty()) {
            binding.flEmpty.visibility = View.VISIBLE
            binding.rvPncMothers.visibility = View.GONE
            binding.tvCount.text = "0 ${getString(R.string.result)}"
        } else {
            binding.flEmpty.visibility = View.GONE
            binding.rvPncMothers.visibility = View.VISIBLE
            adapter.submitList(filteredPatients)

            val countText = if (filteredPatients.size == 1) {
                "1 ${getString(R.string.result)}"
            } else {
                "${filteredPatients.size} ${getString(R.string.result)}s"
            }
            binding.tvCount.text = countText
        }

        Timber.d("Displaying ${filteredPatients.size} PNC mothers")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
