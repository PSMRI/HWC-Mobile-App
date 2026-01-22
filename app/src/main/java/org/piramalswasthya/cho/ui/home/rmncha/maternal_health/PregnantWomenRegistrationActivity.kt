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
import org.piramalswasthya.cho.adapter.PregnantWomenAdapter
import org.piramalswasthya.cho.databinding.ActivityPregnantWomenRegistrationBinding
import org.piramalswasthya.cho.model.PatientWithPwrDomain
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import timber.log.Timber
import javax.inject.Inject

/**
 * Activity to display list of Pregnant Women Registration
 * Shows women of reproductive age (15-49) with their pregnancy registration status
 */
@AndroidEntryPoint
class PregnantWomenRegistrationActivity : AppCompatActivity() {

    @Inject
    lateinit var maternalHealthRepo: MaternalHealthRepo

    private lateinit var binding: ActivityPregnantWomenRegistrationBinding
    private lateinit var adapter: PregnantWomenAdapter
    private var allPatients: List<PatientWithPwrDomain> = emptyList()
    private var filteredPatients: List<PatientWithPwrDomain> = emptyList()

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, PregnantWomenRegistrationActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPregnantWomenRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.pregnant_women_registration)

        setupRecyclerView()
        setupSearch()
        observePatients()
    }

    private fun setupRecyclerView() {
        adapter = PregnantWomenAdapter(
            PregnantWomenAdapter.ClickListener { patientWithPwr ->
                // Handle click - navigate to pregnancy registration form
                if (patientWithPwr.pwr == null) {
                    // Navigate to registration form
                    Toast.makeText(
                        this,
                        "Register Pregnancy: ${patientWithPwr.patient.firstName}",
                        Toast.LENGTH_SHORT
                    ).show()
                    // TODO: Navigate to Pregnancy Registration Form
                } else {
                    // Navigate to view/edit form
                    Toast.makeText(
                        this,
                        "View Pregnancy: ${patientWithPwr.patient.firstName}",
                        Toast.LENGTH_SHORT
                    ).show()
                    // TODO: Navigate to Pregnancy View/Edit Form
                }
            }
        )

        binding.rvPregnantWomen.layoutManager = LinearLayoutManager(this)
        binding.rvPregnantWomen.adapter = adapter
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
            maternalHealthRepo.getAllPatientsWithPWR().collectLatest { patientsList ->
                // Filter for women of reproductive age
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
            binding.rvPregnantWomen.visibility = View.GONE
            binding.tvCount.text = "0 ${getString(R.string.result)}"
        } else {
            binding.flEmpty.visibility = View.GONE
            binding.rvPregnantWomen.visibility = View.VISIBLE
            adapter.submitList(filteredPatients)

            val countText = if (filteredPatients.size == 1) {
                "1 ${getString(R.string.result)}"
            } else {
                "${filteredPatients.size} ${getString(R.string.result)}s"
            }
            binding.tvCount.text = countText
        }

        Timber.d("Displaying ${filteredPatients.size} pregnant women")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
