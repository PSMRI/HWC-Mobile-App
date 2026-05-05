package org.piramalswasthya.cho.ui.home.rmncha.eligible_couple

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.ECRegistrationAdapter
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.databinding.FragmentEligibleCoupleTrackingBinding
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientWithEcrDomain
import org.piramalswasthya.cho.repositories.EcrRepo
import org.piramalswasthya.cho.utils.FaceSearchHelper
import org.piramalswasthya.cho.utils.filterPatientsByQuery
import org.piramalswasthya.cho.utils.setupSearchTextWatcher
import org.piramalswasthya.cho.utils.updateListUI
import javax.inject.Inject

/**
 * Fragment to display list of Eligible Couples for Tracking.
 * Uses reusable [FaceSearchHelper] for voice and camera search.
 */
@AndroidEntryPoint
class EligibleCoupleTrackingFragment : Fragment() {

    @Inject
    lateinit var ecrRepo: EcrRepo

    @Inject
    lateinit var patientDao: PatientDao

    private var _binding: FragmentEligibleCoupleTrackingBinding? = null
    private val binding: FragmentEligibleCoupleTrackingBinding
        get() = _binding!!

    private lateinit var adapter: ECRegistrationAdapter
    private var allPatients: List<PatientWithEcrDomain> = emptyList()
    private var filteredPatients: List<PatientWithEcrDomain> = emptyList()
    private var currentPatientsList: List<Patient>? = null

    /**
     * Reusable face-search helper — handles speech-to-text and camera face matching.
     * Must be initialised before onViewCreated (activity-result launchers requirement).
     */
    private val faceSearchHelper by lazy {
        FaceSearchHelper(
            fragment = this,
            patientDao = patientDao,
            onSpeechResult = { recognisedText ->
                binding.searchBarInclude.search.setText(recognisedText)
            },
            onFaceMatchResult = { matchedPatient ->
                if (matchedPatient != null) {
                    filteredPatients = allPatients.filter {
                        it.patient.patientID == matchedPatient.patientID
                    }
                    updateUI()
                    if (filteredPatients.isNotEmpty()) {
                        Toast.makeText(requireContext(), "1 matching record found", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Patient not found in this Eligible Couple list", Toast.LENGTH_LONG).show()
                    }
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
        _binding = FragmentEligibleCoupleTrackingBinding.inflate(inflater, container, false)
        // Touch the lazy property so the activity-result launchers are registered now
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
        adapter = ECRegistrationAdapter(
            ECRegistrationAdapter.ClickListener(
                onAddVisit = { patientWithEcr ->
                    val lastVisit = patientWithEcr.lastVisitDate
                    if (lastVisit != null && lastVisit > 0) {
                        val calendar = java.util.Calendar.getInstance()
                        calendar.timeInMillis = lastVisit
                        val lastMonth = calendar.get(java.util.Calendar.MONTH)
                        val lastYear = calendar.get(java.util.Calendar.YEAR)

                        calendar.timeInMillis = System.currentTimeMillis()
                        val currentMonth = calendar.get(java.util.Calendar.MONTH)
                        val currentYear = calendar.get(java.util.Calendar.YEAR)

                        if (lastMonth == currentMonth && lastYear == currentYear) {
                            Toast.makeText(
                                requireContext(),
                                "Eligible Couple tracking is done for this month",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@ClickListener
                        }
                    }

                    val patientId = patientWithEcr.patient.patientID
                    val fragment = org.piramalswasthya.cho.ui.commons.eligible_couple.tracking.form.EligibleCoupleTrackingFormFragment().apply {
                        arguments = Bundle().apply {
                            putString("patientID", patientId)
                            putLong("createdDate", 0L)
                        }
                    }

                    requireActivity().supportFragmentManager.commit {
                        replace(R.id.fragment_container, fragment)
                        addToBackStack("ECT_FORM_TRANSACTION")
                    }
                },
                onViewVisit = { patientWithEcr ->
                    val bottomSheet = EligibleCoupleVisitHistoryBottomSheet.newInstance(
                        patientWithEcr.patient.patientID
                    )
                    bottomSheet.show(childFragmentManager, "VisitHistoryBottomSheet")
                },
                onCall = { patientWithEcr ->
                    dialBeneficiary(patientWithEcr.patient.phoneNo)
                }
            )
        )

        binding.rvEligibleCouples.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEligibleCouples.adapter = adapter
    }

    private fun setupSearch() {
        // Text search
        binding.searchBarInclude.search.setupSearchTextWatcher { query ->
            filterPatients(query)
        }

        // Voice search (mic end-icon)
        binding.searchBarInclude.searchTil.setEndIconOnClickListener {
            faceSearchHelper.launchSpeechToText()
        }

        // Camera face search
        binding.searchBarInclude.cameraIcon.setOnClickListener {
            faceSearchHelper.launchCameraSearch()
        }
    }

    // ── Data loading & filtering ──

    private fun observePatients() {
        lifecycleScope.launch {
            ecrRepo.getPatientsForTrackingList().collectLatest { patientsList ->
                currentPatientsList = patientsList
                processPatientsList(patientsList)
            }
        }
    }

    private suspend fun processPatientsList(patientsList: List<Patient>) {
        val filteredPatientsList = patientsList

        allPatients = withContext(Dispatchers.IO) {
            filteredPatientsList.map { patient ->
                async {
                    val ecr = ecrRepo.getSavedECR(patient.patientID)
                    val latestVisit = ecrRepo.getLatestEctByBenId(patient.patientID)
                    PatientWithEcrDomain(
                        patient = patient,
                        ecr = ecr
                    ).apply {
                        lastVisitDate = latestVisit?.visitDate
                        methodOfContraception = latestVisit?.methodOfContraception
                        antraInjectionDate = latestVisit?.antraInjectionDate
                        lmpDateFromTracking = latestVisit?.lmpDate
                        antraNextDueDate = if (latestVisit?.methodOfContraception == "ANTRA Injection") {
                            latestVisit.antraDueDate
                        } else {
                            null
                        }
                    }
                }
            }.awaitAll()
        }.filterNotNull()
            .sortedByDescending { it.patient.registrationDate?.time ?: 0L }

        val currentQuery = if (_binding != null) binding.searchBarInclude.search.text.toString() else ""
        filterPatients(currentQuery)
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
            recyclerView = binding.rvEligibleCouples,
            countTextView = binding.tvCount,
            resultString = getString(R.string.result),
            logMessage = "Displaying eligible couples for tracking"
        )
    }

    override fun onResume() {
        super.onResume()
        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title =
            getString(R.string.ec_tracking_title)

        currentPatientsList?.let {
            lifecycleScope.launch {
                processPatientsList(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun dialBeneficiary(phoneNumber: String?) {
        val sanitizedNumber = phoneNumber?.trim().orEmpty()
        if (sanitizedNumber.isBlank()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.mobile_number_not_present),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        startActivity(
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$sanitizedNumber"))
        )
    }
}
