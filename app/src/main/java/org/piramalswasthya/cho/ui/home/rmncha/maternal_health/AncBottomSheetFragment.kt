package org.piramalswasthya.cho.ui.home.rmncha.maternal_health

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.adapter.AncVisitBottomSheetAdapter
import org.piramalswasthya.cho.databinding.BottomSheetAncVisitsBinding
import org.piramalswasthya.cho.model.PregnantWomanAncCache
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import javax.inject.Inject

@AndroidEntryPoint
class AncBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAncVisitsBinding? = null
    private val binding: BottomSheetAncVisitsBinding
        get() = _binding!!

    @Inject
    lateinit var maternalHealthRepo: MaternalHealthRepo

    private var patientID: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAncVisitsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        patientID = arguments?.getString("patientID")

        val adapter = AncVisitBottomSheetAdapter(
            AncVisitBottomSheetAdapter.AncVisitClickListener { ancVisit ->
                // Navigate to the specific ANC visit form
                val intent = Intent(
                    requireContext(),
                    org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity::class.java
                ).apply {
                    putExtra("navigateTo", "ANC")
                    putExtra("patientID", patientID)
                    putExtra("visitNumber", ancVisit.visitNumber)
                    putExtra("isOldVisit", ancVisit.weight != null) // Read-only if completed
                }
                startActivity(intent)
                dismiss()
            }
        )

        binding.rvAncVisits.adapter = adapter
        binding.rvAncVisits.layoutManager = LinearLayoutManager(requireContext())
        
        // Add divider
        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        binding.rvAncVisits.addItemDecoration(divider)

        // Load ANC visits
        loadAncVisits(adapter)
    }

    private fun loadAncVisits(adapter: AncVisitBottomSheetAdapter) {
        lifecycleScope.launch {
            patientID?.let { id ->
                // Get pregnancy registration to access LMP date
                val pwr = maternalHealthRepo.getActiveRegistrationRecord(id)
                val lmpDate = pwr?.lmpDate ?: 0L
                
                val ancVisits = maternalHealthRepo.getAllActiveAncRecords(id)
                    .sortedBy { it.visitNumber }
                
                // Pass LMP date and all visits to adapter for status calculation
                adapter.updateData(ancVisits, lmpDate)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(patientID: String): AncBottomSheetFragment {
            return AncBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString("patientID", patientID)
                }
            }
        }
    }
}
