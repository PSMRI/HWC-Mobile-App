package org.piramalswasthya.cho.ui.home.rmncha.maternal_health

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.PNCMotherAdapter
import org.piramalswasthya.cho.databinding.FragmentPncMotherListBinding
import org.piramalswasthya.cho.databinding.IncludeSearchBarWithCameraBinding
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientWithPncDomain
import org.piramalswasthya.cho.repositories.PncRepo
import javax.inject.Inject

/**
 * Fragment to display list of PNC Mothers.
 * Shows women who have delivered and are eligible for PNC visits (within 42 days or not completed all visits).
 */
@AndroidEntryPoint
class PNCMotherListFragment : BaseMaternalHealthListFragment<FragmentPncMotherListBinding, PatientWithPncDomain>() {

    @Inject
    lateinit var pncRepo: PncRepo

    private lateinit var adapter: PNCMotherAdapter

    // ── Base class contract ──

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentPncMotherListBinding.inflate(inflater, container, false)

    override fun getSearchBarBinding(): IncludeSearchBarWithCameraBinding = binding.searchBarInclude
    override fun getRecyclerView(): RecyclerView = binding.rvPncMothers
    override fun getEmptyStateView(): FrameLayout = binding.flEmpty
    override fun getCountTextView(): TextView = binding.tvCount

    override val titleResId = R.string.pnc_mother_list
    override val listDisplayName = "PNC Mother list"
    override val logMessage = "Displaying PNC mothers"

    override fun extractPatient(item: PatientWithPncDomain): Patient = item.patient

    override fun submitListToAdapter(list: List<PatientWithPncDomain>) {
        adapter.submitList(list)
    }

    override fun setupRecyclerView() {
        adapter = PNCMotherAdapter(
            clickListener = PNCMotherAdapter.ClickListener(
                onAddVisit = { patientWithPnc -> navigateToAddPncVisit(patientWithPnc) },
                onViewVisits = { patientWithPnc -> showPncVisitsBottomSheet(patientWithPnc) }
            ),
            pncRepo = pncRepo
        )
        binding.rvPncMothers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPncMothers.adapter = adapter
    }

    override fun observePatients() {
        lifecycleScope.launch {
            pncRepo.getAllPNCMothers().collectLatest { patientsList ->
                allPatients = patientsList
                    .map { it.asDomainModel() }
                    .sortedByDescending { it.deliveryOutcome?.dateOfDelivery ?: it.patient.registrationDate?.time ?: 0L }
                onPatientsLoaded()
            }
        }
    }

    // ── Fragment-specific logic ──

    private fun navigateToAddPncVisit(patientWithPnc: PatientWithPncDomain) {
        lifecycleScope.launch {
            val lastVisitNumber = pncRepo.getLastVisitNumber(patientWithPnc.patient.patientID) ?: 0
            val availableVisits = listOf(1, 3, 7, 14, 21, 28, 42).filter { it > lastVisitNumber }

            if (availableVisits.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.all_pnc_visits_completed), Toast.LENGTH_SHORT).show()
                return@launch
            }

            val nextVisitNumber = availableVisits.first()

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
}
