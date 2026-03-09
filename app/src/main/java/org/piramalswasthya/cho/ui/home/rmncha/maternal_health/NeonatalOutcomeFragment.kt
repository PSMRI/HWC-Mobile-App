package org.piramalswasthya.cho.ui.home.rmncha.maternal_health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.InfantRegistrationAdapter
import org.piramalswasthya.cho.databinding.FragmentNeonatalOutcomeListBinding
import org.piramalswasthya.cho.model.InfantRegDomain
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import org.piramalswasthya.cho.utils.filterPatientsByQuery
import org.piramalswasthya.cho.utils.setupSearchTextWatcher
import org.piramalswasthya.cho.utils.updateListUI
import org.piramalswasthya.cho.ui.commons.maternal_health.neonatal_outcome.form.NeonatalOutcomeFormFragment
import org.piramalswasthya.cho.repositories.NeonatalOutcomeRepo
import org.piramalswasthya.cho.repositories.DeliveryOutcomeRepo
import org.piramalswasthya.cho.repositories.InfantRegRepo
import javax.inject.Inject

/**
 * Fragment to display list of women eligible for Neonatal Outcomes
 * Shows women who have a saved Delivery Outcome record
 */
@AndroidEntryPoint
class NeonatalOutcomeFragment : Fragment() {

    @Inject
    lateinit var infantRegRepo: InfantRegRepo
    
    @Inject
    lateinit var deliveryOutcomeRepo: DeliveryOutcomeRepo

    private var _binding: FragmentNeonatalOutcomeListBinding? = null
    private val binding: FragmentNeonatalOutcomeListBinding
        get() = _binding!!

    private lateinit var adapter: InfantRegistrationAdapter
    private var allInfants: List<InfantRegDomain> = emptyList()
    private var filteredInfants: List<InfantRegDomain> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNeonatalOutcomeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Handle back button press
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        )

        setupRecyclerView()
        setupSearch()
        observePatients()
    }

    private fun setupRecyclerView() {
        adapter = InfantRegistrationAdapter(
            InfantRegistrationAdapter.ClickListener { motherPatientID: String, babyIndex: Int ->
                lifecycleScope.launch {
                    val deliveryOutcome = deliveryOutcomeRepo.getDeliveryOutcome(motherPatientID)
                    if (deliveryOutcome != null) {
                        // Navigate to NeonatalOutcomeFormFragment using fragment transaction
                        val fragment = NeonatalOutcomeFormFragment().apply {
                            arguments = Bundle().apply {
                                putLong("deliveryOutcomeId", deliveryOutcome.id)
                                putString("patientID", motherPatientID)
                                putInt("babyIndex", babyIndex)
                            }
                        }
                        requireActivity().supportFragmentManager.commit {
                            replace(R.id.fragment_container, fragment)
                            addToBackStack(null)
                        }
                    } else {
                        android.widget.Toast.makeText(context,
                            context?.getString(R.string.delivery_record_not_found), android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        binding.rvNeonatalOutcome.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNeonatalOutcome.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.setupSearchTextWatcher { query ->
            filterPatients(query)
        }
    }

    private fun observePatients() {
        lifecycleScope.launch {
            infantRegRepo.getListForInfantReg().collectLatest { infantsList: List<InfantRegDomain> ->
                allInfants = infantsList.sortedByDescending { it.deliveryOutcome.dateOfDelivery ?: 0L }
                filteredInfants = allInfants
                updateUI()
            }
        }
    }

    private fun filterPatients(query: String) {
        filteredInfants = if (query.isBlank()) {
            allInfants
        } else {
            allInfants.filter { infant ->
                val motherName = infant.getMotherFullName().lowercase()
                motherName.contains(query.lowercase())
            }
        }
        updateUI()
    }

    private fun updateUI() {
        adapter.submitList(filteredInfants)
        updateListUI(
            filteredList = filteredInfants,
            emptyStateView = binding.flEmpty,
            recyclerView = binding.rvNeonatalOutcome,
            countTextView = binding.tvCount,
            resultString = getString(R.string.result),
            logMessage = "Displaying infants for neonatal outcome"
        )
    }

    override fun onResume() {
        super.onResume()
        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title =
            getString(R.string.neonatal_outcome)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
