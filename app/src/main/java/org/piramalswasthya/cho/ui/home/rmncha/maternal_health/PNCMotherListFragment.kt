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
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.databinding.FragmentPncMotherListBinding
import org.piramalswasthya.cho.model.PatientWithPncDomain
import org.piramalswasthya.cho.repositories.PncRepo
import org.piramalswasthya.cho.utils.FaceSearchHelper
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

    @Inject
    lateinit var patientDao: PatientDao

    private var _binding: FragmentPncMotherListBinding? = null
    private val binding: FragmentPncMotherListBinding
        get() = _binding!!

    private lateinit var adapter: PNCMotherAdapter
    private var allPatients: List<PatientWithPncDomain> = emptyList()
    private var filteredPatients: List<PatientWithPncDomain> = emptyList()

    private val faceSearchHelper by lazy {
        FaceSearchHelper(
            fragment = this,
            patientDao = patientDao,
            onSpeechResult = { text -> binding.searchBarInclude.search.setText(text) },
            onFaceMatchResult = { matchedPatient ->
                if (matchedPatient != null) {
                    filteredPatients = allPatients.filter { it.patient.patientID == matchedPatient.patientID }
                    adapter.submitList(filteredPatients)
                    binding.tvCount.text = "1 ${getString(R.string.result)}"
                    binding.tvCount.visibility = View.VISIBLE
                    binding.rvPncMothers.visibility = View.VISIBLE
                    binding.flEmpty.visibility = View.GONE
                    Toast.makeText(requireContext(), "1 matching patient found", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "No matching patient found", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPncMotherListBinding.inflate(inflater, container, false)
        faceSearchHelper
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
            clickListener = PNCMotherAdapter.ClickListener(
                onAddVisit = { patientWithPnc ->
                    navigateToAddPncVisit(patientWithPnc)
                },
                onViewVisits = { patientWithPnc ->
                    showPncVisitsBottomSheet(patientWithPnc)
                }
            ),
            pncRepo = pncRepo
        )

        binding.rvPncMothers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPncMothers.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchBarInclude.search.setupSearchTextWatcher { query ->
            filterPatients(query)
        }
        binding.searchBarInclude.searchTil.setEndIconOnClickListener {
            faceSearchHelper.launchSpeechToText()
        }
        binding.searchBarInclude.cameraIcon.setOnClickListener {
            faceSearchHelper.launchCameraSearch()
        }
    }

    private fun observePatients() {
        lifecycleScope.launch {
            pncRepo.getAllPNCMothers().collectLatest { patientsList ->
                // PNC module opens only after Date of Discharge is entered in Delivery Outcome
                allPatients = patientsList
                    .map { it.asDomainModel() }
                    .sortedByDescending { it.deliveryOutcome?.dateOfDelivery ?: it.patient.registrationDate?.time ?: 0L }

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
            // Calculate next visit number
            val lastVisitNumber = pncRepo.getLastVisitNumber(patientWithPnc.patient.patientID) ?: 0
            val availableVisits = listOf(1, 3, 7, 14, 21, 28, 42).filter { it > lastVisitNumber }
            
            if (availableVisits.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.all_pnc_visits_completed), Toast.LENGTH_SHORT).show()
                return@launch
            }
            
            val nextVisitNumber = availableVisits.first()
            
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

    override fun onResume() {
        super.onResume()
        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title =
            getString(R.string.pnc_mother_list)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
