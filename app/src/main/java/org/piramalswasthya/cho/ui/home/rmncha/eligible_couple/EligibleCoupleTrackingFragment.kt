package org.piramalswasthya.cho.ui.home.rmncha.eligible_couple

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.ECRegistrationAdapter
import org.piramalswasthya.cho.databinding.FragmentEligibleCoupleTrackingBinding
import org.piramalswasthya.cho.model.PatientWithEcrDomain
import org.piramalswasthya.cho.repositories.EcrRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.utils.filterPatientsByQuery
import org.piramalswasthya.cho.utils.setupSearchTextWatcher
import org.piramalswasthya.cho.utils.updateListUI
import javax.inject.Inject

/**
 * Fragment to display list of Eligible Couples for Tracking
 * Shows eligible couples directly from Patient table filtered by statusOfWomanID = 1
 * Loads ECR and latest visit data for display
 */
@AndroidEntryPoint
class EligibleCoupleTrackingFragment : Fragment() {

    @Inject
    lateinit var patientRepo: PatientRepo

    @Inject
    lateinit var ecrRepo: EcrRepo

    private var _binding: FragmentEligibleCoupleTrackingBinding? = null
    private val binding: FragmentEligibleCoupleTrackingBinding
        get() = _binding!!

    private lateinit var adapter: ECRegistrationAdapter
    private var allPatients: List<PatientWithEcrDomain> = emptyList()
    private var filteredPatients: List<PatientWithEcrDomain> = emptyList()

    companion object {
        // Status of Woman ID for Eligible Couple (as per user requirement)
        const val STATUS_ELIGIBLE_COUPLE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEligibleCoupleTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        observePatients()
    }

    private fun setupRecyclerView() {
        adapter = ECRegistrationAdapter(
            ECRegistrationAdapter.ClickListener(
                onAddVisit = { patientWithEcr ->
                    // Navigate to tracking form for new visit
                    val patientId = patientWithEcr.patient.patientID
                    
                    // Navigate to Eligible Couple Tracking Form Fragment
                    val fragment = org.piramalswasthya.cho.ui.commons.eligible_couple.tracking.form.EligibleCoupleTrackingFormFragment().apply {
                        arguments = Bundle().apply {
                            putString("patientID", patientId)
                            putLong("createdDate", 0L) // 0L means new visit
                        }
                    }
                    
                    requireActivity().supportFragmentManager.commit {
                        replace(R.id.fragment_container, fragment)
                        addToBackStack(null)
                    }
                },
                onViewVisit = { patientWithEcr ->
                    // Show visit history bottom sheet
                    val bottomSheet = EligibleCoupleVisitHistoryBottomSheet.newInstance(
                        patientWithEcr.patient.patientID
                    )
                    bottomSheet.show(childFragmentManager, "VisitHistoryBottomSheet")
                }
            )
        )

        binding.rvEligibleCouples.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEligibleCouples.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.setupSearchTextWatcher { query ->
            filterPatients(query)
        }
    }

    private fun observePatients() {
        lifecycleScope.launch {
            patientRepo.getPatientListFlow().collectLatest { patientsList ->
                // Filter eligible couples directly from Patient table
                // Show only patients with statusOfWomanID = 1 (Eligible Couple)
                val filteredPatientsList = patientsList
                    .filter { patientDisplay ->
                        // Filter criteria:
                        // 1. Female gender (genderID = 2)
                        // 2. Age between 15-49 years (reproductive age)
                        // 3. statusOfWomanID = 1 (Eligible Couple)
                        val isFemale = patientDisplay.patient.genderID == 2
                        val age = patientDisplay.patient.age ?: 0
                        val isReproductiveAge = age in 15..49
                        val statusOfWomanID = patientDisplay.patient.statusOfWomanID
                        val isEligibleCouple = statusOfWomanID == STATUS_ELIGIBLE_COUPLE

                        isFemale && isReproductiveAge && isEligibleCouple
                    }

                // Load ECR data and latest visit date for each patient in parallel
                allPatients = withContext(Dispatchers.IO) {
                    filteredPatientsList.map { patientDisplay ->
                        async {
                            // Load ECR data if available
                            val ecr = ecrRepo.getSavedECR(patientDisplay.patient.patientID)
                            // Load latest visit to check for positive pregnancy test
                            val latestVisit = ecrRepo.getLatestEctByBenId(patientDisplay.patient.patientID)
                            
                            // Check if latest visit has positive pregnancy test
                            val hasPositivePregnancyTest = latestVisit?.let { visit ->
                                visit.isPregnancyTestDone == "Yes" && visit.pregnancyTestResult == "Positive"
                            } ?: false
                            
                            // Return null if pregnancy test is positive (to filter out)
                            if (hasPositivePregnancyTest) {
                                null
                            } else {
                                PatientWithEcrDomain(
                                    patient = patientDisplay.patient,
                                    ecr = ecr
                                ).apply {
                                    lastVisitDate = latestVisit?.visitDate
                                }
                            }
                        }
                    }.awaitAll()
                }.filterNotNull() // Remove null entries (patients with positive pregnancy test)
                 .sortedByDescending { it.patient.registrationDate?.time ?: 0L }

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
            recyclerView = binding.rvEligibleCouples,
            countTextView = binding.tvCount,
            resultString = getString(R.string.result),
            logMessage = "Displaying eligible couples for tracking"
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
