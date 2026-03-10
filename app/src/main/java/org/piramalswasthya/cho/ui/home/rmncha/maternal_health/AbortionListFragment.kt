package org.piramalswasthya.cho.ui.home.rmncha.maternal_health

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import org.piramalswasthya.cho.databinding.FragmentAbortionListBinding
import org.piramalswasthya.cho.model.AbortionDomain
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
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

    private var _binding: FragmentAbortionListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AbortionListAdapter
    private var allAbortions: List<AbortionDomain> = emptyList()
    private var filteredAbortions: List<AbortionDomain> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAbortionListBinding.inflate(inflater, container, false)
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
        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No-op
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No-op
            }

            override fun afterTextChanged(s: Editable?) {
                filterAbortions(s?.toString() ?: "")
            }
        })
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

    private fun filterAbortions(query: String) {
        filteredAbortions = if (query.isBlank()) {
            allAbortions
        } else {
            allAbortions.filter { abortion ->
                val firstName = abortion.patient.firstName?.lowercase() ?: ""
                val lastName = abortion.patient.lastName?.lowercase() ?: ""
                val spouseName = abortion.patient.spouseName?.lowercase() ?: ""
                val phoneNo = abortion.patient.phoneNo ?: ""
                val beneficiaryID = abortion.patient.beneficiaryID?.toString() ?: ""

                val searchQuery = query.lowercase()

                firstName.contains(searchQuery) ||
                    lastName.contains(searchQuery) ||
                    spouseName.contains(searchQuery) ||
                    phoneNo.contains(searchQuery) ||
                    beneficiaryID.contains(searchQuery)
            }
        }
        updateUI()
    }

    private fun updateUI() {
        if (_binding == null) return
        if (filteredAbortions.isEmpty()) {
            binding.flEmpty.visibility = View.VISIBLE
            binding.rvAbortionList.visibility = View.GONE
            binding.tvCount.text = "0 ${getString(R.string.result)}"
        } else {
            binding.flEmpty.visibility = View.GONE
            binding.rvAbortionList.visibility = View.VISIBLE
            adapter.submitList(filteredAbortions)

            val countText = if (filteredAbortions.size == 1) {
                "1 ${getString(R.string.result)}"
            } else {
                "${filteredAbortions.size} ${getString(R.string.result)}s"
            }
            binding.tvCount.text = countText
        }

        Timber.d("Displaying ${filteredAbortions.size} abortion records")
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
