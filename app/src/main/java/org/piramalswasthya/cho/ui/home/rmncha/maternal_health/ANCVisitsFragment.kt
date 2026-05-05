package org.piramalswasthya.cho.ui.home.rmncha.maternal_health

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import org.piramalswasthya.cho.adapter.ANCVisitsAdapter
import org.piramalswasthya.cho.databinding.FragmentAncVisitsBinding
import org.piramalswasthya.cho.databinding.IncludeSearchBarWithCameraBinding
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientWithPwrDomain
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Fragment to display list of ANC Visits.
 * Shows registered pregnant women with LMP >= 5 weeks (eligible for ANC visits).
 */
@AndroidEntryPoint
class ANCVisitsFragment : BaseMaternalHealthListFragment<FragmentAncVisitsBinding, PatientWithPwrDomain>() {

    @Inject
    lateinit var maternalHealthRepo: MaternalHealthRepo

    private lateinit var adapter: ANCVisitsAdapter

    companion object {
        private const val MIN_ANC_WEEKS = 5
        private const val MIN_ANC_DAYS = MIN_ANC_WEEKS * 7

        private const val ARG_PATIENT_ID = "patientID"

        fun newInstance(patientID: String? = null): ANCVisitsFragment {
            return ANCVisitsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PATIENT_ID, patientID)
                }
            }
        }
    }

    // ── Base class contract ──

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentAncVisitsBinding.inflate(inflater, container, false)

    override fun getSearchBarBinding(): IncludeSearchBarWithCameraBinding = binding.searchBarInclude
    override fun getRecyclerView(): RecyclerView = binding.rvAncVisits
    override fun getEmptyStateView(): FrameLayout = binding.flEmpty
    override fun getCountTextView(): TextView = binding.tvCount

    override val titleResId = R.string.anc_visits
    override val listDisplayName = "ANC Visits list"
    override val logMessage = "Displaying ANC-eligible pregnant women"

    override fun extractPatient(item: PatientWithPwrDomain): Patient = item.patient

    override fun submitListToAdapter(list: List<PatientWithPwrDomain>) {
        adapter.submitList(list)
    }

    override fun setupRecyclerView() {
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
                },
                clickedCall = { patientWithPwr ->
                    dialBeneficiary(patientWithPwr.patient.phoneNo)
                }
            )
        )

        binding.rvAncVisits.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAncVisits.adapter = adapter
    }

    override fun observePatients() {
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

                onPatientsLoaded()
            }
        }
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
