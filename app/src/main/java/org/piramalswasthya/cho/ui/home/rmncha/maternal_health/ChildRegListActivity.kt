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
import org.piramalswasthya.cho.adapter.ChildRegistrationAdapter
import org.piramalswasthya.cho.databinding.ActivityChildRegListBinding
import org.piramalswasthya.cho.model.ChildRegDomain
import org.piramalswasthya.cho.repositories.InfantRegRepo
import timber.log.Timber
import javax.inject.Inject

/**
 * Activity to display list of registered infants for child registration
 * Shows infants that have been registered (isActive = 1) with REGISTER/VIEW buttons
 */
@AndroidEntryPoint
class ChildRegListActivity : AppCompatActivity() {

    @Inject
    lateinit var infantRegRepo: InfantRegRepo

    private lateinit var binding: ActivityChildRegListBinding
    private lateinit var adapter: ChildRegistrationAdapter
    private var allChildren: List<ChildRegDomain> = emptyList()
    private var filteredChildren: List<ChildRegDomain> = emptyList()

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, ChildRegListActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChildRegListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.child_reg_list)

        setupRecyclerView()
        setupSearch()
        observeChildren()
    }

    private fun setupRecyclerView() {
        adapter = ChildRegistrationAdapter(
            ChildRegistrationAdapter.ClickListener { patientID, babyIndex, childPatientID ->
                // Handle click - navigate to Child Registration form or view child details
                if (childPatientID != null) {
                    Toast.makeText(
                        this,
                        "View Child: $childPatientID",
                        Toast.LENGTH_SHORT
                    ).show()
                    // TODO: Navigate to Child Details/View
                } else {
                    Toast.makeText(
                        this,
                        "Register Child: Patient $patientID, Baby Index $babyIndex",
                        Toast.LENGTH_SHORT
                    ).show()
                    // TODO: Navigate to Child Registration Form
                }
            }
        )

        binding.rvChildReg.layoutManager = LinearLayoutManager(this)
        binding.rvChildReg.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterChildren(s?.toString() ?: "")
            }
        })
    }

    private fun observeChildren() {
        lifecycleScope.launch {
            infantRegRepo.getRegisteredInfants().collectLatest { childrenList ->
                allChildren = childrenList.sortedByDescending { 
                    it.infant.createdDate 
                }
                filteredChildren = allChildren
                updateUI()
            }
        }
    }

    private fun filterChildren(query: String) {
        filteredChildren = if (query.isBlank()) {
            allChildren
        } else {
            allChildren.filter { child ->
                val firstName = child.motherPatient.firstName?.lowercase() ?: ""
                val lastName = child.motherPatient.lastName?.lowercase() ?: ""
                val spouseName = child.motherPatient.spouseName?.lowercase() ?: ""
                val phoneNo = child.motherPatient.phoneNo ?: ""
                val beneficiaryID = child.motherPatient.beneficiaryID?.toString() ?: ""
                val babyName = child.customName.lowercase()

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
        if (filteredChildren.isEmpty()) {
            binding.flEmpty.visibility = View.VISIBLE
            binding.rvChildReg.visibility = View.GONE
            binding.tvCount.text = "0 ${getString(R.string.result)}"
        } else {
            binding.flEmpty.visibility = View.GONE
            binding.rvChildReg.visibility = View.VISIBLE
            adapter.submitList(filteredChildren)

            val countText = if (filteredChildren.size == 1) {
                "1 ${getString(R.string.result)}"
            } else {
                "${filteredChildren.size} ${getString(R.string.result)}s"
            }
            binding.tvCount.text = countText
        }

        Timber.d("Displaying ${filteredChildren.size} registered children")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
