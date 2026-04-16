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
import org.piramalswasthya.cho.adapter.ChildRegistrationAdapter
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.databinding.FragmentChildRegListBinding
import org.piramalswasthya.cho.model.ChildRegDomain
import org.piramalswasthya.cho.repositories.InfantRegRepo
import org.piramalswasthya.cho.utils.FaceSearchHelper
import org.piramalswasthya.cho.utils.setupSearchTextWatcher
import org.piramalswasthya.cho.utils.updateListUI
import javax.inject.Inject

@AndroidEntryPoint
class ChildRegistrationListFragment : Fragment() {

    @Inject
    lateinit var infantRegRepo: InfantRegRepo

    @Inject
    lateinit var patientDao: PatientDao

    private var _binding: FragmentChildRegListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ChildRegistrationAdapter
    private var allChildren: List<ChildRegDomain> = emptyList()
    private var filteredChildren: List<ChildRegDomain> = emptyList()

    private val faceSearchHelper by lazy {
        FaceSearchHelper(
            fragment = this,
            patientDao = patientDao,
            onSpeechResult = { text -> binding.searchBarInclude.search.setText(text) },
            onFaceMatchResult = { matchedPatient ->
                if (matchedPatient != null) {
                    filteredChildren = allChildren.filter {
                        it.motherPatient.patientID == matchedPatient.patientID ||
                        it.childPatient?.patientID == matchedPatient.patientID
                    }
                    updateUI()
                    if (filteredChildren.isNotEmpty()) {
                        Toast.makeText(requireContext(), "${filteredChildren.size} matching record(s) found", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Patient not found in this Child Registration list", Toast.LENGTH_LONG).show()
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
        _binding = FragmentChildRegListBinding.inflate(inflater, container, false)
        faceSearchHelper
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        syncChildListFromServer()
        observeChildren()
    }

    private fun syncChildListFromServer() {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching { infantRegRepo.pullChildrenFromServer() }
        }
    }

    private fun setupRecyclerView() {
        adapter = ChildRegistrationAdapter(
            ChildRegistrationAdapter.ClickListener { motherPatientID, babyIndex, childPatientID ->
                val fragment = ChildBeneficiaryRegistrationFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_MOTHER_PATIENT_ID, motherPatientID)
                        putInt(ARG_BABY_INDEX, babyIndex)
                        putString(ARG_CHILD_PATIENT_ID, childPatientID)
                    }
                }

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        )

        binding.rvChildRegList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChildRegList.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchBarInclude.search.setupSearchTextWatcher { query ->
            filterChildren(query)
        }
        binding.searchBarInclude.searchTil.setEndIconOnClickListener {
            faceSearchHelper.launchSpeechToText()
        }
        binding.searchBarInclude.cameraIcon.setOnClickListener {
            faceSearchHelper.launchCameraSearch()
        }
    }

    private fun observeChildren() {
        viewLifecycleOwner.lifecycleScope.launch {
            infantRegRepo.getRegisteredInfants().collectLatest { childrenList ->
                allChildren = childrenList.sortedByDescending { it.infant.updatedDate }
                filteredChildren = allChildren
                updateUI()
            }
        }
    }

    private fun filterChildren(query: String) {
        if (query.isBlank()) {
            filteredChildren = allChildren
            updateUI()
            return
        }

        val normalized = query.trim().lowercase()
        filteredChildren = allChildren.filter { child ->
            val childName = child.childPatient?.let {
                "${it.firstName.orEmpty()} ${it.lastName.orEmpty()}".trim().lowercase()
            }.orEmpty()
            val motherName = child.getMotherFullName().lowercase()
            val motherPhone = child.motherPatient.phoneNo.orEmpty().lowercase()
            val motherBenId = child.motherPatient.beneficiaryID?.toString().orEmpty()
            val childBenId = child.childPatient?.beneficiaryID?.toString().orEmpty()

            childName.contains(normalized) ||
                    child.customName.lowercase().contains(normalized) ||
                    motherName.contains(normalized) ||
                    motherPhone.contains(normalized) ||
                    motherBenId.contains(normalized) ||
                    childBenId.contains(normalized)
        }
        updateUI()
    }

    private fun updateUI() {
        adapter.submitList(filteredChildren)
        updateListUI(
            filteredList = filteredChildren,
            emptyStateView = binding.flEmpty,
            recyclerView = binding.rvChildRegList,
            countTextView = binding.tvCount,
            resultString = getString(R.string.result),
            logMessage = "Displaying child registration records"
        )
    }

    override fun onResume() {
        super.onResume()
        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title =
            getString(R.string.child_reg_list)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_MOTHER_PATIENT_ID = "motherPatientID"
        const val ARG_BABY_INDEX = "babyIndex"
        const val ARG_CHILD_PATIENT_ID = "childPatientID"
    }
}
