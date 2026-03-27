package org.piramalswasthya.cho.ui.home.rmncha.child_care

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.InfantRegistrationAdapter
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.databinding.FragmentInfantListBinding
import org.piramalswasthya.cho.model.InfantRegDomain
import org.piramalswasthya.cho.repositories.InfantRegRepo
import org.piramalswasthya.cho.utils.FaceSearchHelper
import org.piramalswasthya.cho.utils.setupSearchTextWatcher
import org.piramalswasthya.cho.utils.updateListUI
import javax.inject.Inject

/**
 * Fragment to display list of infants eligible for registration
 * Shows infants grouped by mother with "Nth baby of [Mother's Name]" format
 */
@AndroidEntryPoint
class InfantListFragment : Fragment() {

    @Inject
    lateinit var infantRegRepo: InfantRegRepo

    @Inject
    lateinit var patientDao: PatientDao

    private var _binding: FragmentInfantListBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: InfantRegistrationAdapter
    private var allInfants: List<InfantRegDomain> = emptyList()
    private var filteredInfants: List<InfantRegDomain> = emptyList()

    private val faceSearchHelper by lazy {
        FaceSearchHelper(
            fragment = this,
            patientDao = patientDao,
            isCameraSearchEnabled = false,
            onSpeechResult = { text -> binding.searchBarInclude.search.setText(text) }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfantListBinding.inflate(inflater, container, false)
        faceSearchHelper
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupSearch()
        observeInfants()
    }

    private fun setupRecyclerView() {
        adapter = InfantRegistrationAdapter(
            InfantRegistrationAdapter.ClickListener { motherPatientID: String, babyIndex: Int ->
                // Navigate to ChildRegistrationFragment
                val fragment = ChildRegistrationFragment()
                val args = Bundle().apply {
                    putString("patientID", motherPatientID)
                    putInt("babyIndex", babyIndex)
                }
                fragment.arguments = args
                
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        )

        binding.rvInfantList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInfantList.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchBarInclude.search.setupSearchTextWatcher { query ->
            filterInfants(query)
        }
        binding.searchBarInclude.searchTil.setEndIconOnClickListener {
            faceSearchHelper.launchSpeechToText()
        }
        binding.searchBarInclude.cameraIcon.visibility = View.GONE
    }

    private fun observeInfants() {
        viewLifecycleOwner.lifecycleScope.launch {
            infantRegRepo.getListForInfantReg().collectLatest { infantsList: List<InfantRegDomain> ->
                allInfants = infantsList.sortedByDescending { it.deliveryOutcome.dateOfDelivery ?: 0L }
                filteredInfants = allInfants
                updateUI()
            }
        }
    }

    private fun filterInfants(query: String) {
        filteredInfants = if (query.isBlank()) {
            allInfants
        } else {
            allInfants.filter { infant ->
                infant.getMotherFullName().contains(query, ignoreCase = true) ||
                        infant.motherPatient.patientID.contains(query, ignoreCase = true)
            }
        }
        updateUI()
    }

    private fun updateUI() {
        adapter.submitList(filteredInfants)
        updateListUI(
            filteredList = filteredInfants,
            emptyStateView = binding.flEmpty,
            recyclerView = binding.rvInfantList,
            countTextView = binding.tvCount,
            resultString = getString(R.string.result),
            logMessage = "Displaying registered infants"
        )
    }

    override fun onResume() {
        super.onResume()
        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = getString(R.string.infant_list)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
