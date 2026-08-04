package org.piramalswasthya.cho.ui.home.rmncha.maternal_health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.DeliveryOutcomeAdapter
import org.piramalswasthya.cho.databinding.FragmentDeliveryOutcomeBinding
import org.piramalswasthya.cho.databinding.IncludeSearchBarWithCameraBinding
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientWithPwrDomain
import org.piramalswasthya.cho.repositories.DeliveryOutcomeRepo
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import org.piramalswasthya.cho.ui.commons.maternal_health.delivery_outcome.form.DeliveryOutcomeFormFragment
import javax.inject.Inject

/**
 * Fragment to display list of Delivery Outcomes.
 * Shows pregnant women who have delivered (pregnantWomanDelivered = true in ANC).
 */
@AndroidEntryPoint
class DeliveryOutcomeFragment : BaseMaternalHealthListFragment<FragmentDeliveryOutcomeBinding, PatientWithPwrDomain>() {

    @Inject
    lateinit var maternalHealthRepo: MaternalHealthRepo

    @Inject
    lateinit var deliveryOutcomeRepo: DeliveryOutcomeRepo

    private lateinit var adapter: DeliveryOutcomeAdapter

    // ── Base class contract ──

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentDeliveryOutcomeBinding.inflate(inflater, container, false)

    override fun getSearchBarBinding(): IncludeSearchBarWithCameraBinding = binding.searchBarInclude
    override fun getRecyclerView(): RecyclerView = binding.rvDeliveryOutcome
    override fun getEmptyStateView(): FrameLayout = binding.flEmpty
    override fun getCountTextView(): TextView = binding.tvCount

    override val titleResId = R.string.delivery_outcome
    override val listDisplayName = "Delivery Outcome list"
    override val logMessage = "Displaying delivered women"

    override fun extractPatient(item: PatientWithPwrDomain): Patient = item.patient

    override fun submitListToAdapter(list: List<PatientWithPwrDomain>) {
        adapter.submitList(list)
    }

    // ── Fragment-specific: back button handling ──

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
        adapter = DeliveryOutcomeAdapter(
            DeliveryOutcomeAdapter.ClickListener { patientWithPwr ->
                val fragment = DeliveryOutcomeFormFragment().apply {
                    arguments = Bundle().apply {
                        putString("patientID", patientWithPwr.patient.patientID)
                        putInt("visitNumber", 1)
                    }
                }
                requireActivity().supportFragmentManager.commit {
                    replace(R.id.fragment_container, fragment)
                    addToBackStack(null)
                }
            }
        )

        binding.rvDeliveryOutcome.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDeliveryOutcome.adapter = adapter
    }

    override fun observePatients() {
        viewLifecycleOwner.lifecycleScope.launch {
            combine(
                maternalHealthRepo.getAllDeliveredWomen(),
                maternalHealthRepo.getNeonatalOutcomeEligibleWomenPatientIDs()
            ) { patientsList, filledDOIds ->
                allPatients = withContext(Dispatchers.IO) {
                    patientsList.map { patientWithPwr ->
                        async {
                            val domainModel = patientWithPwr.asDomainModel()
                            val patientID = patientWithPwr.patient.patientID
                            val isFilled = filledDOIds.contains(patientID)
                            domainModel.isDeliveryOutcomeFilled = isFilled
                            domainModel.deliveryOutcomeSyncState =
                                if (isFilled) deliveryOutcomeRepo.getDeliveryOutcome(patientID)?.syncState else null
                            domainModel
                        }
                    }.awaitAll()
                }.sortedByDescending { it.pwr?.dateOfRegistration ?: 0L }

                onPatientsLoaded()
            }.collectLatest { }
        }
    }
}
