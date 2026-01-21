package org.piramalswasthya.cho.ui.commons.maternal_health.delivery_outcome.list

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentDeliveryOutcomeListBinding

@AndroidEntryPoint
class DeliveryOutcomeListFragment : Fragment() {

    private var _binding: FragmentDeliveryOutcomeListBinding? = null
    private val binding: FragmentDeliveryOutcomeListBinding
        get() = _binding!!

    private val viewModel: DeliveryOutcomeListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeliveryOutcomeListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val adapter = DeliveryOutcomeListAdapter { patientID ->
            findNavController().navigate(
                DeliveryOutcomeListFragmentDirections
                    .actionDeliveryOutcomeListFragmentToDeliveryOutcomeFragment(patientID)
            )
        }
        
        binding.rvDeliveryOutcome.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDeliveryOutcome.adapter = adapter

        lifecycleScope.launch {
            viewModel.patientList.collect { list ->
                if (list.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.rvDeliveryOutcome.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    binding.rvDeliveryOutcome.visibility = View.VISIBLE
                }
                adapter.submitList(list)
            }
        }

        // Search functionality
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.filterText(s?.toString() ?: "")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
