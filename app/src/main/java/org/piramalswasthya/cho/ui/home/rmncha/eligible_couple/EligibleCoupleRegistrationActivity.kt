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
import org.piramalswasthya.cho.databinding.ActivityEligibleCoupleRegistrationBinding
import org.piramalswasthya.cho.model.PatientWithEcrDomain
import org.piramalswasthya.cho.repositories.EcrRepo
import org.piramalswasthya.cho.utils.filterPatientsByQuery
import org.piramalswasthya.cho.utils.setupSearchTextWatcher
import org.piramalswasthya.cho.utils.updateListUI
import org.piramalswasthya.cho.utils.setupToolbarWithBack
import timber.log.Timber
import javax.inject.Inject

/**
 * Activity to display list of Eligible Couples
 * Shows women of reproductive age with their EC registration status
 */
@AndroidEntryPoint
class EligibleCoupleRegistrationActivity : AppCompatActivity() {

    @Inject
    lateinit var ecrRepo: EcrRepo

    private lateinit var binding: ActivityEligibleCoupleRegistrationBinding
    private lateinit var adapter: ECRegistrationAdapter
    private var allPatients: List<PatientWithEcrDomain> = emptyList()
    private var filteredPatients: List<PatientWithEcrDomain> = emptyList()

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, EligibleCoupleRegistrationActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEligibleCoupleRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setupToolbarWithBack(binding.toolbar, getString(R.string.eligible_couple_list))

        setupRecyclerView()
        setupSearch()
        observePatients()
    }

    private fun setupRecyclerView() {
        adapter = ECRegistrationAdapter(
            ECRegistrationAdapter.ClickListener { patientWithEcr ->
                // Handle click - navigate to registration/view form
                if (patientWithEcr.ecr == null) {
                    // Navigate to registration form
                    Toast.makeText(
                        this,
                        "Register: ${patientWithEcr.patient.firstName}",
                        Toast.LENGTH_SHORT
                    ).show()
                    // TODO: Navigate to EC Registration Form
                } else {
                    // Navigate to view/edit form
                    Toast.makeText(
                        this,
                        "View: ${patientWithEcr.patient.firstName}",
                        Toast.LENGTH_SHORT
                    ).show()
                    // TODO: Navigate to EC View/Edit Form
                }
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
                // Filter for eligible couples (females of reproductive age)
                allPatients = patientsList
                    .map { it.asDomainModel() }
                    .filter { patient ->
                        // Filter criteria:
                        // 1. Female gender (genderID = 2)
                        // 2. Age between 15-49 years (reproductive age)
                        val isFemale = patient.patient.genderID == 2
                        val age = patient.patient.age ?: 0
                        val isReproductiveAge = age in 15..49

                        isFemale && isReproductiveAge
                    }
                    .sortedByDescending { it.patient.registrationDate }

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
            logMessage = "Displaying eligible couples"
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
