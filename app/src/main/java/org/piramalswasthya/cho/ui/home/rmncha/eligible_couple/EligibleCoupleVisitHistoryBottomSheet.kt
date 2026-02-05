package org.piramalswasthya.cho.ui.home.rmncha.eligible_couple

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.ECTrackingAdapter
import org.piramalswasthya.cho.databinding.BottomSheetEcVisitHistoryBinding
import org.piramalswasthya.cho.repositories.EcrRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import timber.log.Timber
import javax.inject.Inject

/**
 * Bottom Sheet to display visit history for Eligible Couple Tracking
 * Shows all past visits with their dates and allows viewing details
 */
@AndroidEntryPoint
class EligibleCoupleVisitHistoryBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetEcVisitHistoryBinding? = null
    private val binding: BottomSheetEcVisitHistoryBinding
        get() = _binding!!

    @Inject
    lateinit var ecrRepo: EcrRepo

    @Inject
    lateinit var patientRepo: PatientRepo

    private lateinit var patientID: String

    companion object {
        private const val ARG_PATIENT_ID = "patient_id"

        fun newInstance(patientID: String): EligibleCoupleVisitHistoryBottomSheet {
            return EligibleCoupleVisitHistoryBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_PATIENT_ID, patientID)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        patientID = arguments?.getString(ARG_PATIENT_ID) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetEcVisitHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadPatientInfo()
        loadVisitHistory()
    }

    private fun setupRecyclerView() {
        val adapter = ECTrackingAdapter(
            ECTrackingAdapter.ECTrackViewClickListener { benId, visitDate ->
                // Navigate to tracking form in view mode
                val fragment = org.piramalswasthya.cho.ui.commons.eligible_couple.tracking.form.EligibleCoupleTrackingFormFragment().apply {
                    arguments = Bundle().apply {
                        putString("patientID", benId)
                        putLong("createdDate", visitDate)
                    }
                }

                requireActivity().supportFragmentManager.commit {
                    replace(R.id.fragment_container, fragment)
                    addToBackStack(null)
                }
                
                // Dismiss bottom sheet after navigation
                dismiss()
            }
        )

        binding.rvVisitHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
            
            // Add divider for better visual separation
            addItemDecoration(
                DividerItemDecoration(requireContext(), LinearLayout.VERTICAL)
            )
        }
    }

    private fun loadPatientInfo() {
        lifecycleScope.launch {
            try {
                val patientDisplay = patientRepo.getPatientDisplay(patientID)
                patientDisplay?.let { patient ->
                    val name = "${patient.patient.firstName} ${patient.patient.lastName ?: ""}".trim()
                    val ageGender = "${patient.patient.age} ${patient.ageUnit?.name ?: ""} | ${patient.gender?.genderName ?: ""}".trim()
                    
                    binding.tvPatientName.text = name
                    binding.tvPatientAgeGender.text = ageGender
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load patient info for $patientID")
            }
        }
    }

    private fun loadVisitHistory() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.rvVisitHistory.visibility = View.GONE
                binding.tvEmptyState.visibility = View.GONE

                val visits = ecrRepo.getAllECT(patientID)
                    .sortedByDescending { it.visitDate } // Most recent first

                if (visits.isEmpty()) {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.tvCount.text = getString(R.string.no_visits_found)
                } else {
                    (binding.rvVisitHistory.adapter as? ECTrackingAdapter)?.submitList(visits)
                    binding.progressBar.visibility = View.GONE
                    binding.rvVisitHistory.visibility = View.VISIBLE
                    
                    // Update count
                    val count = visits.size
                    binding.tvCount.text = resources.getQuantityString(
                        R.plurals.visit_count,
                        count,
                        count
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load visit history for $patientID")
                binding.progressBar.visibility = View.GONE
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.tvEmptyState.text = getString(R.string.error_loading_visits)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
