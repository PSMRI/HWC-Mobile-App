package org.piramalswasthya.cho.ui.home.rmncha.eligible_couple

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.adapter.ECTrackingAdapter
import org.piramalswasthya.cho.databinding.BottomSheetEligibleCoupleVisitHistoryBinding
import org.piramalswasthya.cho.repositories.EcrRepo
import org.piramalswasthya.cho.ui.commons.eligible_couple.tracking.form.EligibleCoupleTrackingFormFragment
import javax.inject.Inject

@AndroidEntryPoint
class EligibleCoupleVisitHistoryBottomSheet : BottomSheetDialogFragment() {

    @Inject
    lateinit var ecrRepo: EcrRepo

    private var _binding: BottomSheetEligibleCoupleVisitHistoryBinding? = null
    private val binding: BottomSheetEligibleCoupleVisitHistoryBinding
        get() = _binding!!

    private lateinit var patientID: String
    private lateinit var adapter: ECTrackingAdapter

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
        _binding = BottomSheetEligibleCoupleVisitHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadVisitHistory()
    }

    private fun setupRecyclerView() {
        adapter = ECTrackingAdapter(
            ECTrackingAdapter.ECTrackViewClickListener { benId, visitDate ->
                // Navigate to form with specific visit date
                val fragment = EligibleCoupleTrackingFormFragment().apply {
                    arguments = Bundle().apply {
                        putString("patientID", benId)
                        putLong("createdDate", visitDate)
                    }
                }
                
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(org.piramalswasthya.cho.R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
                
                dismiss()
            }
        )

        binding.rvVisitHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvVisitHistory.adapter = adapter
    }

    private fun loadVisitHistory() {
        lifecycleScope.launch {
            val visits = ecrRepo.getAllECT(patientID)
            adapter.submitList(visits.sortedByDescending { it.visitDate })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
