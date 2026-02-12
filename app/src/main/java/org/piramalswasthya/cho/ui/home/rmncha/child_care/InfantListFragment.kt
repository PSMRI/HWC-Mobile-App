package org.piramalswasthya.cho.ui.home.rmncha.child_care

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.InfantListAdapter
import org.piramalswasthya.cho.databinding.FragmentInfantListBinding
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.utils.filterPatientsByQuery
import org.piramalswasthya.cho.utils.setupSearchTextWatcher
import org.piramalswasthya.cho.utils.updateListUI
import javax.inject.Inject

/**
 * Fragment to display list of infants (age <= 61 days)
 * Shows all registered infants for child care services
 */
@AndroidEntryPoint
class InfantListFragment : Fragment() {

    @Inject
    lateinit var patientRepo: PatientRepo

    private var _binding: FragmentInfantListBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: InfantListAdapter
    private var allInfants: List<PatientDisplay> = emptyList()
    private var filteredInfants: List<PatientDisplay> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfantListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupSearch()
        observeInfants()
    }

    private fun setupRecyclerView() {
        adapter = InfantListAdapter(
            InfantListAdapter.ClickListener { patient ->
                // Handle click - navigate to infant details/form
                // TODO: Navigate to infant form or details screen
            }
        )

        binding.rvInfantList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInfantList.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.setupSearchTextWatcher { query ->
            filterInfants(query)
        }
    }

    private fun observeInfants() {
        lifecycleScope.launch {
            patientRepo.getInfantList().collectLatest { infantsList ->
                allInfants = infantsList.sortedByDescending { it.patient.dob?.time ?: 0L }
                filteredInfants = allInfants
                updateUI()
            }
        }
    }

    private fun filterInfants(query: String) {
        filteredInfants = allInfants.filterPatientsByQuery(query) { it.patient }
        updateUI()
    }

    private fun updateUI() {
        adapter.submitList(filteredInfants)
        updateListUI(
            filteredList = filteredInfants,
            emptyStateView = binding.flEmpty,
            recyclerView = binding.rvInfantList,
            countTextView = binding.tvCount,
            resultString = getString(R.string.result),
            logMessage = "Displaying infants"
        )
    }

    override fun onResume() {
        super.onResume()
        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = getString(R.string.infant_list)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
