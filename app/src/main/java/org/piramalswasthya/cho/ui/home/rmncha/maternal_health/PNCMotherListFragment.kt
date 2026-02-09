package org.piramalswasthya.cho.ui.home.rmncha.maternal_health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.PNCMotherAdapter
import org.piramalswasthya.cho.databinding.FragmentPncMotherListBinding
import org.piramalswasthya.cho.model.PatientWithPncDomain
import org.piramalswasthya.cho.repositories.PncRepo
import org.piramalswasthya.cho.utils.filterPatientsByQuery
import org.piramalswasthya.cho.utils.setupSearchTextWatcher
import org.piramalswasthya.cho.utils.updateListUI
import javax.inject.Inject

/**
 * Fragment to display list of PNC Mothers
 * Shows women who have delivered and are eligible for PNC visits (within 42 days or not completed all visits)
 */
@AndroidEntryPoint
class PNCMotherListFragment : Fragment() {

    @Inject
    lateinit var pncRepo: PncRepo

    private var _binding: FragmentPncMotherListBinding? = null
    private val binding: FragmentPncMotherListBinding
        get() = _binding!!

    private lateinit var adapter: PNCMotherAdapter
    private var allPatients: List<PatientWithPncDomain> = emptyList()
    private var filteredPatients: List<PatientWithPncDomain> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPncMotherListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        observePatients()
    }

    private fun setupRecyclerView() {
        adapter = PNCMotherAdapter(
            PNCMotherAdapter.ClickListener(
                onAddVisit = { patientWithPnc ->
                    navigateToAddPncVisit(patientWithPnc)
                },
                onViewVisits = { patientWithPnc ->
                    showPncVisitsBottomSheet(patientWithPnc)
                }
            )
        )

        binding.rvPncMothers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPncMothers.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.setupSearchTextWatcher { query ->
            filterPatients(query)
        }
    }

    private fun observePatients() {
        lifecycleScope.launch {
            pncRepo.getAllPNCMothers().collectLatest { patientsList ->
                // Filter for PNC-eligible mothers
                // PNC module opens only after Date of Discharge is entered in Delivery Outcome
                allPatients = patientsList
                    .map { it.asDomainModel() }
                    .filter { domain ->
                        // Check if Date of Discharge is entered
                        val hasDischargeDate = domain.deliveryOutcome?.dateOfDischarge != null
                        // Additional eligibility check
                        hasDischargeDate && domain.isEligibleForPNC()
                    }
                    .sortedByDescending { it.deliveryOutcome?.dateOfDelivery ?: 0L }

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
            recyclerView = binding.rvPncMothers,
            countTextView = binding.tvCount,
            resultString = getString(R.string.result),
            logMessage = "Displaying PNC mothers"
        )
    }

    private fun navigateToAddPncVisit(patientWithPnc: PatientWithPncDomain) {
        lifecycleScope.launch {
            // Check if delivery date exists
            val deliveryDateTimestamp = patientWithPnc.deliveryOutcome?.dateOfDischarge
            if (deliveryDateTimestamp == null) {
                Toast.makeText(requireContext(), "Delivery date not found", Toast.LENGTH_SHORT).show()
                return@launch
            }
            
            // Calculate days since delivery (convert timestamp to LocalDate)
            val deliveryDate = java.time.Instant.ofEpochMilli(deliveryDateTimestamp)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
            val daysSinceDelivery = java.time.temporal.ChronoUnit.DAYS.between(
                deliveryDate,
                java.time.LocalDate.now()
            ).toInt()
            
            // Calculate next visit number
            val lastVisitNumber = pncRepo.getLastVisitNumber(patientWithPnc.patient.patientID) ?: 0
            val availableVisits = listOf(1, 3, 7, 14, 21, 28, 42).filter { it > lastVisitNumber }
            
            if (availableVisits.isEmpty()) {
                Toast.makeText(requireContext(), "All PNC visits completed", Toast.LENGTH_SHORT).show()
                return@launch
            }
            
            val nextVisitNumber = availableVisits.first()
            
            // Check if enough days have elapsed for next visit
            if (daysSinceDelivery < nextVisitNumber) {
                Toast.makeText(
                    requireContext(),
                    "PNC Visit Day $nextVisitNumber not yet due (${daysSinceDelivery} days since delivery)",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }
            
            // Navigate to PNC form
            val intent = android.content.Intent(
                requireContext(),
                org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity::class.java
            ).apply {
                putExtra("navigateTo", "PNC")
                putExtra("patientID", patientWithPnc.patient.patientID)
                putExtra("visitNumber", nextVisitNumber)
            }
            startActivity(intent)
        }
    }

    private fun showPncVisitsBottomSheet(patientWithPnc: PatientWithPncDomain) {
        val bottomSheet = PncBottomSheetFragment.newInstance(patientWithPnc.patient.patientID)
        bottomSheet.show(childFragmentManager, "PncVisitsBottomSheet")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
