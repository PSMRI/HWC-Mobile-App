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
import org.piramalswasthya.cho.adapter.AbortionListAdapter
import org.piramalswasthya.cho.databinding.ActivityAbortionListBinding
import org.piramalswasthya.cho.model.AbortionDomain
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import timber.log.Timber
import javax.inject.Inject

/**
 * Activity to display list of women with abortion records
 * Shows women who have aborted pregnancies (isAborted = 1 and abortionDate is not null)
 */
@AndroidEntryPoint
class AbortionListActivity : AppCompatActivity() {

    @Inject
    lateinit var maternalHealthRepo: MaternalHealthRepo

    private lateinit var binding: ActivityAbortionListBinding
    private lateinit var adapter: AbortionListAdapter
    private var allAbortions: List<AbortionDomain> = emptyList()
    private var filteredAbortions: List<AbortionDomain> = emptyList()

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, AbortionListActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAbortionListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.abortion_list)

        setupRecyclerView()
        setupSearch()
        observeAbortions()
    }

    private fun setupRecyclerView() {
        adapter = AbortionListAdapter(
            AbortionListAdapter.ClickListener(
                clickedView = { patientID ->
                    // Handle VIEW click - navigate to abortion form/view
                    Toast.makeText(
                        this,
                        "View Abortion: $patientID",
                        Toast.LENGTH_SHORT
                    ).show()
                    // TODO: Navigate to Abortion Form/View
                },
                clickedAdd = { patientID ->
                    // Handle ADD click - navigate to abortion form
                    Toast.makeText(
                        this,
                        "Add Abortion: $patientID",
                        Toast.LENGTH_SHORT
                    ).show()
                    // TODO: Navigate to Abortion Form
                }
            )
        )

        binding.rvAbortionList.layoutManager = LinearLayoutManager(this)
        binding.rvAbortionList.adapter = adapter
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
                filterAbortions(s?.toString() ?: "")
            }
        })
    }

    private fun observeAbortions() {
        lifecycleScope.launch {
            maternalHealthRepo.getAbortionPregnantWomanList().collectLatest { abortionsList ->
                allAbortions = abortionsList.sortedByDescending { 
                    it.abortionDate ?: 0L 
                }
                filteredAbortions = allAbortions
                updateUI()
            }
        }
    }

    private fun filterAbortions(query: String) {
        filteredAbortions = if (query.isBlank()) {
            allAbortions
        } else {
            allAbortions.filter { abortion ->
                val firstName = abortion.patient.firstName?.lowercase() ?: ""
                val lastName = abortion.patient.lastName?.lowercase() ?: ""
                val spouseName = abortion.patient.spouseName?.lowercase() ?: ""
                val phoneNo = abortion.patient.phoneNo ?: ""
                val beneficiaryID = abortion.patient.beneficiaryID?.toString() ?: ""

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
        if (filteredAbortions.isEmpty()) {
            binding.flEmpty.visibility = View.VISIBLE
            binding.rvAbortionList.visibility = View.GONE
            binding.tvCount.text = "0 ${getString(R.string.result)}"
        } else {
            binding.flEmpty.visibility = View.GONE
            binding.rvAbortionList.visibility = View.VISIBLE
            adapter.submitList(filteredAbortions)

            val countText = if (filteredAbortions.size == 1) {
                "1 ${getString(R.string.result)}"
            } else {
                "${filteredAbortions.size} ${getString(R.string.result)}s"
            }
            binding.tvCount.text = countText
        }

        Timber.d("Displaying ${filteredAbortions.size} abortion records")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
