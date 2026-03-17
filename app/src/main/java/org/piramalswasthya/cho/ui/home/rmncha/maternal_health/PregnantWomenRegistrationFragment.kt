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
import org.piramalswasthya.cho.adapter.PregnantWomenAdapter
import org.piramalswasthya.cho.databinding.FragmentPregnantWomenRegistrationBinding
import org.piramalswasthya.cho.model.PatientWithPwrDomain
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import org.piramalswasthya.cho.utils.filterPatientsByQuery
import org.piramalswasthya.cho.utils.setupSearchTextWatcher
import org.piramalswasthya.cho.utils.updateListUI
import javax.inject.Inject

/**
 * Fragment to display list of Pregnant Women Registration
 * Shows women of reproductive age (15-49) with their pregnancy registration status
 */
@AndroidEntryPoint
class PregnantWomenRegistrationFragment : Fragment() {

    @Inject
    lateinit var maternalHealthRepo: MaternalHealthRepo

    private var _binding: FragmentPregnantWomenRegistrationBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: PregnantWomenAdapter
    private var allPatients: List<PatientWithPwrDomain> = emptyList()
    private var filteredPatients: List<PatientWithPwrDomain> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPregnantWomenRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        observePatients()
    }

    override fun onResume() {
        super.onResume()
        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title =
            getString(R.string.pregnant_women_registration)
    }

    private fun setupRecyclerView() {
        adapter = PregnantWomenAdapter(
            PregnantWomenAdapter.ClickListener { patientWithPwr ->
                // Handle click - navigate to pregnancy registration form with patient ID
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
                    // Fallback: show toast if navigation fails
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

    private fun setupSearch() {
        binding.searchView.setupSearchTextWatcher { query ->
            filterPatients(query)
        }
    }

    private fun observePatients() {
        viewLifecycleOwner.lifecycleScope.launch {
            maternalHealthRepo.getAllPatientsWithPWR().collectLatest { patientsList ->
                // Filter for women of reproductive age
                allPatients = patientsList
                    .map { it.asDomainModel() }
                    .filter { patient ->
                        // Filter criteria:
                        // 1. Female gender (genderID = 2)
                        // 2. Age between 15-49 years (reproductive age)
                        val isFemale = patient.patient.genderID == 2
                        val age = patient.patient.age ?: 0
                        val isReproductiveAge = age in 15..49
                        val isPostnatal = patient.patient.statusOfWomanID == 3

                        isFemale && isReproductiveAge && !isPostnatal
                    }
                    .sortedByDescending { it.patient.registrationDate }

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
        // Safe access to binding - if view is destroyed, _binding will be null
        if (_binding == null) return
        
        adapter.submitList(filteredPatients)
        updateListUI(
            filteredList = filteredPatients,
            emptyStateView = binding.flEmpty,
            recyclerView = binding.rvPregnantWomen,
            countTextView = binding.tvCount,
            resultString = getString(R.string.result),
            logMessage = "Displaying pregnant women"
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
