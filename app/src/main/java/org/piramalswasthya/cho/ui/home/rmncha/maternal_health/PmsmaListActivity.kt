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
import org.piramalswasthya.cho.adapter.PmsmaListAdapter
import org.piramalswasthya.cho.databinding.ActivityPmsmaListBinding
import org.piramalswasthya.cho.model.PmsmaDomain
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import timber.log.Timber
import javax.inject.Inject

/**
 * Activity to display list of women eligible for PMSMA (Pradhan Mantri Surakshit Matritva Abhiyan)
 * Shows women registered for pregnancy (active PWR)
 */
@AndroidEntryPoint
class PmsmaListActivity : AppCompatActivity() {

    @Inject
    lateinit var maternalHealthRepo: MaternalHealthRepo

    private lateinit var binding: ActivityPmsmaListBinding
    private lateinit var adapter: PmsmaListAdapter
    private var allPmsmaWomen: List<PmsmaDomain> = emptyList()
    private var filteredPmsmaWomen: List<PmsmaDomain> = emptyList()

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, PmsmaListActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPmsmaListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.e_pmsma_list)

        setupRecyclerView()
        setupSearch()
        observePmsmaWomen()
    }

    private fun setupRecyclerView() {
        adapter = PmsmaListAdapter(
            PmsmaListAdapter.ClickListener { patientID ->
                // Handle click - navigate to PMSMA form/view
                Toast.makeText(
                    this,
                    "View PMSMA: $patientID",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        binding.rvPmsmaList.layoutManager = LinearLayoutManager(this)
        binding.rvPmsmaList.adapter = adapter
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
                filterPmsmaWomen(s?.toString() ?: "")
            }
        })
    }

    private fun observePmsmaWomen() {
        lifecycleScope.launch {
            maternalHealthRepo.getRegisteredPmsmaWomenList().collectLatest { pmsmaList ->
                allPmsmaWomen = pmsmaList.sortedByDescending { 
                    it.pwr?.createdDate ?: 0L 
                }
                filteredPmsmaWomen = allPmsmaWomen
                updateUI()
            }
        }
    }

    private fun filterPmsmaWomen(query: String) {
        filteredPmsmaWomen = if (query.isBlank()) {
            allPmsmaWomen
        } else {
            allPmsmaWomen.filter { pmsma ->
                val firstName = pmsma.patient.firstName?.lowercase() ?: ""
                val lastName = pmsma.patient.lastName?.lowercase() ?: ""
                val spouseName = pmsma.patient.spouseName?.lowercase() ?: ""
                val phoneNo = pmsma.patient.phoneNo ?: ""
                val beneficiaryID = pmsma.patient.beneficiaryID?.toString() ?: ""

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
        if (filteredPmsmaWomen.isEmpty()) {
            binding.flEmpty.visibility = View.VISIBLE
            binding.rvPmsmaList.visibility = View.GONE
            binding.tvCount.text = "0 ${getString(R.string.result)}"
        } else {
            binding.flEmpty.visibility = View.GONE
            binding.rvPmsmaList.visibility = View.VISIBLE
            adapter.submitList(filteredPmsmaWomen)

            val countText = if (filteredPmsmaWomen.size == 1) {
                "1 ${getString(R.string.result)}"
            } else {
                "${filteredPmsmaWomen.size} ${getString(R.string.result)}s"
            }
            binding.tvCount.text = countText
        }

        Timber.d("Displaying ${filteredPmsmaWomen.size} PMSMA eligible women")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
