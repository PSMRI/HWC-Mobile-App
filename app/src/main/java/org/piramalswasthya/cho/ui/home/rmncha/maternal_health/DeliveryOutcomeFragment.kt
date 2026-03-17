package org.piramalswasthya.cho.ui.home.rmncha.maternal_health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.DeliveryOutcomeAdapter
import org.piramalswasthya.cho.databinding.FragmentDeliveryOutcomeBinding
import org.piramalswasthya.cho.model.PatientWithPwrDomain
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import org.piramalswasthya.cho.utils.filterPatientsByQuery
import org.piramalswasthya.cho.utils.setupSearchTextWatcher
import org.piramalswasthya.cho.utils.updateListUI
import org.piramalswasthya.cho.ui.commons.maternal_health.delivery_outcome.form.DeliveryOutcomeFormFragment
import javax.inject.Inject

/**
 * Fragment to display list of Delivery Outcomes
 * Shows pregnant women who have delivered (pregnantWomanDelivered = true in ANC)
 */
@AndroidEntryPoint
class DeliveryOutcomeFragment : Fragment() {

    @Inject
    lateinit var maternalHealthRepo: MaternalHealthRepo

    private var _binding: FragmentDeliveryOutcomeBinding? = null
    private val binding: FragmentDeliveryOutcomeBinding
        get() = _binding!!

    private lateinit var adapter: DeliveryOutcomeAdapter
    private var allPatients: List<PatientWithPwrDomain> = emptyList()
    private var filteredPatients: List<PatientWithPwrDomain> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeliveryOutcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle back button press
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        )

        setupRecyclerView()
        setupSearch()
        observePatients()
    }

    private fun setupRecyclerView() {
        adapter = DeliveryOutcomeAdapter(
            DeliveryOutcomeAdapter.ClickListener { patientWithPwr ->
                // Navigate to DeliveryOutcomeFormFragment using fragment transaction
                val fragment = DeliveryOutcomeFormFragment().apply {
                    arguments = Bundle().apply {
                        putString("patientID", patientWithPwr.patient.patientID)
                        putInt("visitNumber", 1)
                    }
                }
                requireActivity().supportFragmentManager.commit {
                    replace(R.id.fragment_container, fragment)
                    addToBackStack(null)
                }
            }
        )

        binding.rvDeliveryOutcome.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDeliveryOutcome.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.setupSearchTextWatcher { query ->
            filterPatients(query)
        }
    }

    private fun observePatients() {
        viewLifecycleOwner.lifecycleScope.launch {
            combine(
                maternalHealthRepo.getAllDeliveredWomen(),
                maternalHealthRepo.getNeonatalOutcomeEligibleWomenPatientIDs()
            ) { patientsList, filledDOIds ->
                allPatients = patientsList
                    .map {
                        val domainModel = it.asDomainModel()
                        domainModel.isDeliveryOutcomeFilled = filledDOIds.contains(it.patient.patientID)
                        domainModel
                    }
                    .sortedByDescending { it.pwr?.dateOfRegistration ?: 0L }

                filteredPatients = allPatients
                updateUI()
            }.collectLatest { }
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
            recyclerView = binding.rvDeliveryOutcome,
            countTextView = binding.tvCount,
            resultString = getString(R.string.result),
            logMessage = "Displaying delivered women"
        )
    }

    override fun onResume() {
        super.onResume()
        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title =
            getString(R.string.delivery_outcome)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
