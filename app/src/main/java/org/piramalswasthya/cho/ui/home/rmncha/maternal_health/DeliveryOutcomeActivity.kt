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
import org.piramalswasthya.cho.adapter.DeliveryOutcomeAdapter
import org.piramalswasthya.cho.databinding.ActivityDeliveryOutcomeBinding
import org.piramalswasthya.cho.model.PatientWithPwrDomain
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
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
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.delivery_outcome)

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
            binding.rvDeliveryOutcome.visibility = View.GONE
            binding.tvCount.text = "0 ${getString(R.string.result)}"
        } else {
            binding.flEmpty.visibility = View.GONE
            binding.rvDeliveryOutcome.visibility = View.VISIBLE
            adapter.submitList(filteredPatients)

            val countText = if (filteredPatients.size == 1) {
                "1 ${getString(R.string.result)}"
            } else {
                "${filteredPatients.size} ${getString(R.string.result)}s"
            }
            binding.tvCount.text = countText
        }

        Timber.d("Displaying ${filteredPatients.size} delivered women")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
