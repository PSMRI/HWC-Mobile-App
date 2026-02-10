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
import org.piramalswasthya.cho.adapter.ChildrenUnderFiveYearsAdapter
import org.piramalswasthya.cho.databinding.FragmentChildrenUnderFiveYearsListBinding
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.utils.filterPatientsByQuery
import org.piramalswasthya.cho.utils.setupSearchTextWatcher
import org.piramalswasthya.cho.utils.updateListUI
import javax.inject.Inject

/**
 * Fragment to display list of children under 5 years (age <= 5 years).
 * This combines infants and young children into a single list for child care services.
 */
@AndroidEntryPoint
class ChildrenUnderFiveYearsFragment : Fragment() {

    @Inject
    lateinit var patientRepo: PatientRepo

    private var _binding: FragmentChildrenUnderFiveYearsListBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: ChildrenUnderFiveYearsAdapter
    private var allChildren: List<PatientDisplay> = emptyList()
    private var filteredChildren: List<PatientDisplay> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChildrenUnderFiveYearsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupSearch()
        observeChildrenUnderFive()
    }

    private fun setupRecyclerView() {
        adapter = ChildrenUnderFiveYearsAdapter(
            ChildrenUnderFiveYearsAdapter.ClickListener(
                onCheckSam = { _ -> /* TODO: Hook up Check SAM flow when implemented */ },
                onOrs = { _ -> /* TODO: Hook up ORS flow when implemented */ },
                onIfa = { _ -> /* TODO: Hook up IFA flow when implemented */ }
            )
        )

        binding.rvChildrenUnderFiveList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChildrenUnderFiveList.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.setupSearchTextWatcher { query ->
            filterChildren(query)
        }
    }

    private fun observeChildrenUnderFive() {
        lifecycleScope.launch {
            patientRepo.getChildrenUnderFiveList().collectLatest { childrenList ->
                allChildren = childrenList.sortedByDescending { it.patient.dob?.time ?: 0L }
                filteredChildren = allChildren
                updateUI()
            }
        }
    }

    private fun filterChildren(query: String) {
        filteredChildren = allChildren.filterPatientsByQuery(query) { it.patient }
        updateUI()
    }

    private fun updateUI() {
        adapter.submitList(filteredChildren)
        updateListUI(
            filteredList = filteredChildren,
            emptyStateView = binding.flEmpty,
            recyclerView = binding.rvChildrenUnderFiveList,
            countTextView = binding.tvCount,
            resultString = getString(R.string.result),
            logMessage = "Displaying children under 5 years"
        )
    }

    override fun onResume() {
        super.onResume()
        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = getString(R.string.children_under_5_years)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
