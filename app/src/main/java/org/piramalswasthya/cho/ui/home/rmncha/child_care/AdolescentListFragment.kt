package org.piramalswasthya.cho.ui.home.rmncha.child_care

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import android.widget.Toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.AdolescentListAdapter
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.databinding.FragmentAdolescentListBinding
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.utils.FaceSearchHelper
import org.piramalswasthya.cho.utils.filterPatientsByQuery
import org.piramalswasthya.cho.utils.setupSearchTextWatcher
import org.piramalswasthya.cho.utils.updateListUI
import javax.inject.Inject

/**
 * Fragment to display list of adolescents (age 10-19 years)
 * Shows all registered adolescents for child care services
 */
@AndroidEntryPoint
class AdolescentListFragment : Fragment() {

    @Inject
    lateinit var patientRepo: PatientRepo

    @Inject
    lateinit var patientDao: PatientDao

    private var _binding: FragmentAdolescentListBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: AdolescentListAdapter
    private var allAdolescents: List<PatientDisplay> = emptyList()
    private var filteredAdolescents: List<PatientDisplay> = emptyList()

    private val faceSearchHelper by lazy {
        FaceSearchHelper(
            fragment = this,
            patientDao = patientDao,
            onSpeechResult = { text -> binding.searchBarInclude.search.setText(text) },
            onFaceMatchResult = { matchedPatient ->
                if (matchedPatient != null) {
                    filteredAdolescents = allAdolescents.filter { it.patient.patientID == matchedPatient.patientID }
                    updateUI()
                    if (filteredAdolescents.isNotEmpty()) {
                        Toast.makeText(requireContext(), "1 matching record found", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Patient not found in this Adolescent list", Toast.LENGTH_LONG).show()
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
        _binding = FragmentAdolescentListBinding.inflate(inflater, container, false)
        faceSearchHelper
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupSearch()
        observeAdolescents()
    }

    private fun setupRecyclerView() {
        adapter = AdolescentListAdapter(
            AdolescentListAdapter.ClickListener { patient ->
                // Handle click - navigate to adolescent details/form
                // TODO: Navigate to adolescent form or details screen
            }
        )

        binding.rvAdolescentList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAdolescentList.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchBarInclude.search.setupSearchTextWatcher { query ->
            filterAdolescents(query)
        }
        binding.searchBarInclude.searchTil.setEndIconOnClickListener {
            faceSearchHelper.launchSpeechToText()
        }
        binding.searchBarInclude.cameraIcon.setOnClickListener {
            faceSearchHelper.launchCameraSearch()
        }
    }

    private fun observeAdolescents() {
        lifecycleScope.launch {
            patientRepo.getAdolescentList().collectLatest { adolescentsList ->
                allAdolescents = adolescentsList.sortedByDescending { it.patient.dob?.time ?: 0L }
                filteredAdolescents = allAdolescents
                updateUI()
            }
        }
    }

    private fun filterAdolescents(query: String) {
        filteredAdolescents = allAdolescents.filterPatientsByQuery(query) { it.patient }
        updateUI()
    }

    private fun updateUI() {
        adapter.submitList(filteredAdolescents)
        updateListUI(
            filteredList = filteredAdolescents,
            emptyStateView = binding.flEmpty,
            recyclerView = binding.rvAdolescentList,
            countTextView = binding.tvCount,
            resultString = getString(R.string.result),
            logMessage = "Displaying adolescents"
        )
    }
    
    override fun onResume() {
        super.onResume()
        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = getString(R.string.adolescent_list)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
