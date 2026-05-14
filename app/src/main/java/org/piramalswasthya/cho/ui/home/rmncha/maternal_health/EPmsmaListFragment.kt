package org.piramalswasthya.cho.ui.home.rmncha.maternal_health

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.PmsmaListAdapter
import org.piramalswasthya.cho.databinding.FragmentEPmsmaListBinding
import org.piramalswasthya.cho.databinding.IncludeSearchBarWithCameraBinding
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PmsmaDomain
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity
import javax.inject.Inject

/**
 * Fragment to display the e-PMSMA List.
 * Shows pregnant women flagged as high-risk on any active ANC visit
 * (PregnantWomanAncCache.anyHighRisk = true).
 */
@AndroidEntryPoint
class EPmsmaListFragment : BaseMaternalHealthListFragment<FragmentEPmsmaListBinding, PmsmaDomain>() {

    @Inject
    lateinit var maternalHealthRepo: MaternalHealthRepo

    private lateinit var adapter: PmsmaListAdapter

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentEPmsmaListBinding.inflate(inflater, container, false)

    override fun getSearchBarBinding(): IncludeSearchBarWithCameraBinding = binding.searchBarInclude
    override fun getRecyclerView(): RecyclerView = binding.rvEPmsma
    override fun getEmptyStateView(): FrameLayout = binding.flEmpty
    override fun getCountTextView(): TextView = binding.tvCount

    override val titleResId = R.string.e_pmsma_list
    override val listDisplayName = "e-PMSMA List"
    override val logMessage = "Displaying e-PMSMA women"

    override fun extractPatient(item: PmsmaDomain): Patient = item.patient

    override fun submitListToAdapter(list: List<PmsmaDomain>) {
        adapter.submitList(list)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        )
    }

    override fun setupRecyclerView() {
        adapter = PmsmaListAdapter(
            PmsmaListAdapter.ClickListener(
                clickedAdd = { item -> onAddVisit(item) },
                clickedView = { item ->
                    AncBottomSheetFragment.newInstance(item.patient.patientID)
                        .show(childFragmentManager, "ANC_VISITS")
                }
            )
        )
        binding.rvEPmsma.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEPmsma.adapter = adapter
    }

    override fun observePatients() {
        viewLifecycleOwner.lifecycleScope.launch {
            maternalHealthRepo.getEPmsmaWomenList().collectLatest { list ->
                allPatients = list.sortedByDescending { it.pwr?.dateOfRegistration ?: 0L }
                onPatientsLoaded()
            }
        }
    }

    /**
     * Mirrors ANCVisitsFragment.clickedAddANC — same eligibility rules and same
     * EditPatientDetailsActivity navigation, just driven from the e-PMSMA row.
     */
    private fun onAddVisit(item: PmsmaDomain) {
        viewLifecycleOwner.lifecycleScope.launch {
            val patientID = item.patient.patientID
            val allAncRecords = maternalHealthRepo.getAllActiveAncRecords(patientID)
            val maxCompletedVisit = allAncRecords
                .filter { it.weight != null }
                .maxOfOrNull { it.visitNumber } ?: 0
            val nextVisitNumber = maxCompletedVisit + 1
            // Source-of-truth for the e-PMSMA list is ANC anyHighRisk (see
            // MaternalHealthDao.getHighRiskAncPatientIDs), so gate add-visit on
            // the same flag — pwr.isHrp can be unset/stale.
            val isHRP = allAncRecords.any { it.anyHighRisk == true }

            if (maxCompletedVisit < 4 || isHRP) {
                val intent = Intent(
                    requireContext(),
                    EditPatientDetailsActivity::class.java
                ).apply {
                    putExtra("navigateTo", "ANC")
                    putExtra("patientID", patientID)
                    putExtra("visitNumber", nextVisitNumber)
                    putExtra("isOldVisit", false)
                }
                startActivity(intent)
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.all_4_anc_visits_completed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
