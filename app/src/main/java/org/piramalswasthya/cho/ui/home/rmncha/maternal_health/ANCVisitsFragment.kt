package org.piramalswasthya.cho.ui.home.rmncha.maternal_health

import android.content.Intent
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
import org.piramalswasthya.cho.adapter.ANCVisitsAdapter
import org.piramalswasthya.cho.databinding.FragmentAncVisitsBinding
import org.piramalswasthya.cho.model.PatientWithPwrDomain
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import org.piramalswasthya.cho.utils.filterPatientsByQuery
import org.piramalswasthya.cho.utils.setupSearchTextWatcher
import org.piramalswasthya.cho.utils.updateListUI
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Fragment to display list of ANC Visits.
 * Shows registered pregnant women with LMP >= 5 weeks (eligible for ANC visits).
 */
@AndroidEntryPoint
class ANCVisitsFragment : Fragment() {

    @Inject
    lateinit var maternalHealthRepo: MaternalHealthRepo

    private var _binding: FragmentAncVisitsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ANCVisitsAdapter
    private var allPatients: List<PatientWithPwrDomain> = emptyList()
    private var filteredPatients: List<PatientWithPwrDomain> = emptyList()

    companion object {
        private const val MIN_ANC_WEEKS = 5 // Minimum weeks for ANC eligibility
        private const val MIN_ANC_DAYS = MIN_ANC_WEEKS * 7 // 35 days

        private const val ARG_PATIENT_ID = "patientID"

        fun newInstance(patientID: String? = null): ANCVisitsFragment {
            return ANCVisitsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PATIENT_ID, patientID)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAncVisitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        observePatients()
    }

    private fun setupRecyclerView() {
        adapter = ANCVisitsAdapter(
            ANCVisitsAdapter.ClickListener(
                clickedAddANC = { patientWithPwr ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        val patientID = patientWithPwr.patient.patientID
                        val allAncRecords = maternalHealthRepo.getAllActiveAncRecords(patientID)

                        // Find the highest visit number that is COMPLETED (weight != null)
                        val maxCompletedVisit = allAncRecords
                            .filter { it.weight != null }
                            .maxOfOrNull { it.visitNumber } ?: 0

                        val nextVisitNumber = maxCompletedVisit + 1
                        val isHRP = patientWithPwr.pwr?.isHrp ?: false

                        // HRP allows more than 4 visits. Non-HRP is capped at 4.
                        if (maxCompletedVisit < 4 || isHRP) {
                            val intent = Intent(
                                requireContext(),
                                org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity::class.java
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
                },
                clickedANCVisits = { patientWithPwr ->
                    val bottomSheet = AncBottomSheetFragment.newInstance(patientWithPwr.patient.patientID)
                    bottomSheet.show(childFragmentManager, "ANC_VISITS")
                },
                clickedAddPMSMA = { patientWithPwr ->
                    Toast.makeText(
                        requireContext(),
                        "Add PMSMA: ${patientWithPwr.patient.firstName}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        )

        binding.rvAncVisits.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAncVisits.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.setupSearchTextWatcher { query ->
            filterPatients(query)
        }
    }

    private fun observePatients() {
        viewLifecycleOwner.lifecycleScope.launch {
            maternalHealthRepo.getAllPatientsWithPWR().collectLatest { patientsList ->
                allPatients = patientsList
                    .map { it.asDomainModel() }
                    .filter { patient ->
                        val hasActivePWR = patient.pwr != null && patient.isActive()
                        val isFemale = patient.patient.genderID == 2
                        val age = patient.patient.age ?: 0
                        val isReproductiveAge = age in 15..49
                        val isEligibleForANC = patient.pwr?.lmpDate?.let { lmpDate ->
                            val daysSinceLMP = TimeUnit.MILLISECONDS.toDays(
                                System.currentTimeMillis() - lmpDate
                            )
                            daysSinceLMP >= MIN_ANC_DAYS
                        } ?: false

                        hasActivePWR && isFemale && isReproductiveAge && isEligibleForANC
                    }
                    .sortedByDescending { it.pwr?.dateOfRegistration ?: 0L }

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
        if (_binding == null) return
        adapter.submitList(filteredPatients)
        updateListUI(
            filteredList = filteredPatients,
            emptyStateView = binding.flEmpty,
            recyclerView = binding.rvAncVisits,
            countTextView = binding.tvCount,
            resultString = getString(R.string.result),
            logMessage = "Displaying ANC-eligible pregnant women"
        )
    }

    override fun onResume() {
        super.onResume()
        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title =
            getString(R.string.anc_visits)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
