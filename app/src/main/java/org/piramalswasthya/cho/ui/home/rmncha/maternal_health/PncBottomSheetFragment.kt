package org.piramalswasthya.cho.ui.home.rmncha.maternal_health

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.adapter.PncVisitAdapter
import org.piramalswasthya.cho.databinding.BottomSheetPncVisitsBinding
import org.piramalswasthya.cho.repositories.PncRepo
import javax.inject.Inject

@AndroidEntryPoint
class PncBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetPncVisitsBinding? = null
    private val binding: BottomSheetPncVisitsBinding
        get() = _binding!!

    @Inject
    lateinit var pncRepo: PncRepo

    private var patientID: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetPncVisitsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        patientID = arguments?.getString("patientID")

        val adapter = PncVisitAdapter(
            PncVisitAdapter.PncVisitClickListener { patientId, visitNumber ->
                // Navigate to the specific PNC visit form
                val intent = Intent(
                    requireContext(),
                    org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity::class.java
                ).apply {
                    putExtra("navigateTo", "PNC")
                    putExtra("patientID", patientId)
                    putExtra("visitNumber", visitNumber)
                }
                startActivity(intent)
                dismiss()
            }
        )

        binding.rvPncVisits.adapter = adapter
        binding.rvPncVisits.layoutManager = LinearLayoutManager(requireContext())
        
        // Add divider
        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        binding.rvPncVisits.addItemDecoration(divider)

        // Load PNC visits
        loadPncVisits(adapter)
    }

    private fun loadPncVisits(adapter: PncVisitAdapter) {
        lifecycleScope.launch {
            patientID?.let { id ->
                val pncVisits = pncRepo.getAllPNCsByPatId(id)
                    .sortedBy { it.pncPeriod }
                
                adapter.submitList(pncVisits)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(patientID: String): PncBottomSheetFragment {
            return PncBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString("patientID", patientID)
                }
            }
        }
    }
}
