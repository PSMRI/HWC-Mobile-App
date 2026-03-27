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
import org.piramalswasthya.cho.adapter.AbortionListAdapter
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.databinding.FragmentAbortionListBinding
import org.piramalswasthya.cho.model.AbortionDomain
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import org.piramalswasthya.cho.utils.FaceSearchHelper
import org.piramalswasthya.cho.utils.filterPatientsByQuery
import org.piramalswasthya.cho.utils.setupSearchTextWatcher
import org.piramalswasthya.cho.utils.updateListUI
import timber.log.Timber
import javax.inject.Inject

/**
 * Fragment to display list of women with abortion records.
 * Shows women who have aborted pregnancies (isAborted = 1 and abortionDate is not null).
 */
@AndroidEntryPoint
class AbortionListFragment : Fragment() {

    @Inject
    lateinit var maternalHealthRepo: MaternalHealthRepo

    @Inject
    lateinit var patientDao: PatientDao

    private var _binding: FragmentAbortionListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AbortionListAdapter
    private var allAbortions: List<AbortionDomain> = emptyList()
    private var filteredAbortions: List<AbortionDomain> = emptyList()

    private val faceSearchHelper by lazy {
        FaceSearchHelper(
            fragment = this,
            patientDao = patientDao,
            onSpeechResult = { text -> binding.searchBarInclude.search.setText(text) },
            onFaceMatchResult = { matchedPatient ->
                if (matchedPatient != null) {
                    filteredAbortions = allAbortions.filter { it.patient.patientID == matchedPatient.patientID }
                    updateUI()
                    if (filteredAbortions.isNotEmpty()) {
                        Toast.makeText(requireContext(), "${filteredAbortions.size} matching record(s) found", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Patient not found in this Abortion list", Toast.LENGTH_LONG).show()
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
        _binding = FragmentAbortionListBinding.inflate(inflater, container, false)
        faceSearchHelper
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        observeAbortions()
    }

    private fun setupRecyclerView() {
        adapter = AbortionListAdapter(
            AbortionListAdapter.ClickListener(
                clickedView = { patientID ->
                    Toast.makeText(
                        requireContext(),
                        "View Abortion: $patientID",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                clickedAdd = { patientID ->
                    Toast.makeText(
                        requireContext(),
                        "Add Abortion: $patientID",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        )

        binding.rvAbortionList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAbortionList.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchBarInclude.search.setupSearchTextWatcher { query ->
            filterAbortions(query)
        }
        binding.searchBarInclude.searchTil.setEndIconOnClickListener {
            faceSearchHelper.launchSpeechToText()
        }
        binding.searchBarInclude.cameraIcon.setOnClickListener {
            faceSearchHelper.launchCameraSearch()
        }
    }

    private fun filterAbortions(query: String) {
        filteredAbortions = allAbortions.filterPatientsByQuery(query) { it.patient }
        updateUI()
    }

    private fun updateUI() {
        if (_binding == null) return
        adapter.submitList(filteredAbortions)
        updateListUI(
            filteredList = filteredAbortions,
            emptyStateView = binding.flEmpty,
            recyclerView = binding.rvAbortionList,
            countTextView = binding.tvCount,
            resultString = getString(R.string.result),
            logMessage = "Displaying abortion records"
        )
    }

    private fun observeAbortions() {
        viewLifecycleOwner.lifecycleScope.launch {
            maternalHealthRepo.getAbortionPregnantWomanList().collectLatest { abortionsList ->
                allAbortions = abortionsList.sortedByDescending {
                    it.abortionDate ?: 0L
                }
                filteredAbortions = allAbortions
                updateUI()
            }
        }
    }



    override fun onResume() {
        super.onResume()
        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title =
            getString(R.string.abortion_list)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
