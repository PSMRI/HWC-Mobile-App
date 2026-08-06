package org.piramalswasthya.cho.ui.home.rmncha.maternal_health

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
import org.piramalswasthya.cho.adapter.PregnantWomenAdapter
import org.piramalswasthya.cho.databinding.FragmentPregnantWomenRegistrationBinding
import org.piramalswasthya.cho.databinding.IncludeSearchBarWithCameraBinding
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientWithPwrDomain
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import javax.inject.Inject

/**
 * Fragment to display list of Pregnant Women Registration.
 * Shows women of reproductive age (15-49) with their pregnancy registration status.
 */
@AndroidEntryPoint
class PregnantWomenRegistrationFragment : BaseMaternalHealthListFragment<FragmentPregnantWomenRegistrationBinding, PatientWithPwrDomain>() {

    @Inject
    lateinit var maternalHealthRepo: MaternalHealthRepo

    private lateinit var adapter: PregnantWomenAdapter

    // ── Base class contract ──

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentPregnantWomenRegistrationBinding.inflate(inflater, container, false)

    override fun getSearchBarBinding(): IncludeSearchBarWithCameraBinding = binding.searchBarInclude
    override fun getRecyclerView(): RecyclerView = binding.rvPregnantWomen
    override fun getEmptyStateView(): FrameLayout = binding.flEmpty
    override fun getCountTextView(): TextView = binding.tvCount

    override val titleResId = R.string.pregnant_women_registration
    override val listDisplayName = "Pregnant Women list"
    override val logMessage = "Displaying pregnant women"

    override fun extractPatient(item: PatientWithPwrDomain): Patient = item.patient

    override fun submitListToAdapter(list: List<PatientWithPwrDomain>) {
        adapter.submitList(list)
    }

    override fun setupRecyclerView() {
        adapter = PregnantWomenAdapter(
            PregnantWomenAdapter.ClickListener { patientWithPwr ->
                try {
                    val bundle = Bundle().apply {
                        putString("patientID", patientWithPwr.patient.patientID)
                    }
                    androidx.navigation.fragment.NavHostFragment.findNavController(this)
                        .navigate(
                            R.id.action_pregnantWomanRegistrationFragment_to_pregnancyRegistrationFormFragment,
                            bundle
                        )
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Navigation failed: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    timber.log.Timber.e(e, "Failed to navigate to pregnancy registration form")
                }
            }
        )

        binding.rvPregnantWomen.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPregnantWomen.adapter = adapter
    }

    override fun observePatients() {
        viewLifecycleOwner.lifecycleScope.launch {
            maternalHealthRepo.getAllPatientsWithPWR().collectLatest { patientsList ->
                // Filter for women of reproductive age
                allPatients = patientsList
                    .map { it.asDomainModel() }
                    .filter { patient ->
                        val isFemale = patient.patient.genderID == 2
                        val age = patient.patient.age ?: 0
                        val isReproductiveAge = age in 15..49
                        val isPostnatal = patient.patient.statusOfWomanID == 3
                        isFemale && isReproductiveAge && !isPostnatal
                    }
                    .sortedByDescending { it.patient.registrationDate }
                onPatientsLoaded()
            }
        }
    }
}
