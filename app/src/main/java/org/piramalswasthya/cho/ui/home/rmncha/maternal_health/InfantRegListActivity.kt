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
import org.piramalswasthya.cho.adapter.InfantRegistrationAdapter
import org.piramalswasthya.cho.databinding.ActivityInfantRegListBinding
import org.piramalswasthya.cho.model.InfantRegDomain
import org.piramalswasthya.cho.repositories.InfantRegRepo
import timber.log.Timber
import javax.inject.Inject

/**
 * Activity to display list of infants eligible for registration
 * Shows one entry per baby based on liveBirth count from DeliveryOutcome
 */
@AndroidEntryPoint
class InfantRegListActivity : AppCompatActivity() {

    @Inject
    lateinit var infantRegRepo: InfantRegRepo

    private lateinit var binding: ActivityInfantRegListBinding
    private lateinit var adapter: InfantRegistrationAdapter
    private var allInfants: List<InfantRegDomain> = emptyList()
    private var filteredInfants: List<InfantRegDomain> = emptyList()

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, InfantRegListActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfantRegListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.infant_reg_list)

        setupRecyclerView()
        setupSearch()
        observeInfants()
    }

    private fun setupRecyclerView() {
        adapter = InfantRegistrationAdapter(
            InfantRegistrationAdapter.ClickListener { patientID, babyIndex ->
                // Handle click - navigate to Infant Registration form
                Toast.makeText(
                    this,
                    "Infant Registration: Patient $patientID, Baby Index $babyIndex",
                    Toast.LENGTH_SHORT
                ).show()
                // TODO: Navigate to Infant Registration Form
            }
        )

        binding.rvInfantReg.layoutManager = LinearLayoutManager(this)
        binding.rvInfantReg.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed before text changes
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No action needed during text changes - handled in afterTextChanged
            }

            override fun afterTextChanged(s: Editable?) {
                filterInfants(s?.toString() ?: "")
            }
        })
    }

    private fun observeInfants() {
        lifecycleScope.launch {
            infantRegRepo.getListForInfantReg().collectLatest { infantsList ->
                allInfants = infantsList.sortedByDescending { 
                    it.deliveryOutcome.dateOfDelivery ?: 0L 
                }
                filteredInfants = allInfants
                updateUI()
            }
        }
    }

    private fun filterInfants(query: String) {
        filteredInfants = if (query.isBlank()) {
            allInfants
        } else {
            allInfants.filter { infant ->
                val firstName = infant.motherPatient.firstName?.lowercase() ?: ""
                val lastName = infant.motherPatient.lastName?.lowercase() ?: ""
                val spouseName = infant.motherPatient.spouseName?.lowercase() ?: ""
                val phoneNo = infant.motherPatient.phoneNo ?: ""
                val beneficiaryID = infant.motherPatient.beneficiaryID?.toString() ?: ""
                val babyName = infant.customName.lowercase()

                val searchQuery = query.lowercase()

                firstName.contains(searchQuery) ||
                        lastName.contains(searchQuery) ||
                        spouseName.contains(searchQuery) ||
                        phoneNo.contains(searchQuery) ||
                        beneficiaryID.contains(searchQuery) ||
                        babyName.contains(searchQuery)
            }
        }
        updateUI()
    }

    private fun updateUI() {
        if (filteredInfants.isEmpty()) {
            binding.flEmpty.visibility = View.VISIBLE
            binding.rvInfantReg.visibility = View.GONE
            binding.tvCount.text = "0 ${getString(R.string.result)}"
        } else {
            binding.flEmpty.visibility = View.GONE
            binding.rvInfantReg.visibility = View.VISIBLE
            adapter.submitList(filteredInfants)

            val countText = if (filteredInfants.size == 1) {
                "1 ${getString(R.string.result)}"
            } else {
                "${filteredInfants.size} ${getString(R.string.result)}s"
            }
            binding.tvCount.text = countText
        }

        Timber.d("Displaying ${filteredInfants.size} infants for registration")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
