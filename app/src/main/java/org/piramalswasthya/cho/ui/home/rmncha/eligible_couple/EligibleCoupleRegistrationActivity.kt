package org.piramalswasthya.cho.ui.home.rmncha.eligible_couple

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
import org.piramalswasthya.cho.adapter.ECRegistrationAdapter
import org.piramalswasthya.cho.databinding.ActivityEligibleCoupleRegistrationBinding
import org.piramalswasthya.cho.model.PatientWithEcrDomain
import org.piramalswasthya.cho.repositories.EcrRepo
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
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.eligible_couple_list)

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
            binding.rvEligibleCouples.visibility = View.GONE
            binding.tvCount.text = "0 ${getString(R.string.result)}"
        } else {
            binding.flEmpty.visibility = View.GONE
            binding.rvEligibleCouples.visibility = View.VISIBLE
            adapter.submitList(filteredPatients)

            val countText = if (filteredPatients.size == 1) {
                "1 ${getString(R.string.result)}"
            } else {
                "${filteredPatients.size} ${getString(R.string.result)}s"
            }
            binding.tvCount.text = countText
        }

        Timber.d("Displaying ${filteredPatients.size} eligible couples")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
