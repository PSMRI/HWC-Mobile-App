package org.piramalswasthya.cho.ui.home_activity.rmnch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.databinding.FragmentRmnchBinding
import org.piramalswasthya.cho.ui.commons.maternal_health.delivery_outcome.DeliveryOutcomeFragment
import org.piramalswasthya.cho.ui.commons.maternal_health.delivery_outcome.list.DeliveryOutcomeListAdapter
import org.piramalswasthya.cho.ui.commons.maternal_health.infant_reg.InfantRegListFragment
import org.piramalswasthya.cho.ui.commons.maternal_health.infant_reg.dashboard.InfantRegDashboardAdapter

@AndroidEntryPoint
class RMNCHFragment : Fragment() {

    private var _binding: FragmentRmnchBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MaternalHealthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRmnchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Setup Delivery Outcome List
        val deliveryAdapter = DeliveryOutcomeListAdapter { patientID ->
            // Open Delivery Outcome form in a dialog or new fragment
            openDeliveryOutcomeForm(patientID)
        }
        binding.rvDeliveryOutcome.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = deliveryAdapter
        }
        
        // Setup Infant Registration List
        val infantAdapter = InfantRegDashboardAdapter { patientID ->
            // Open Infant Registration list
            openInfantRegList(patientID)
        }
        binding.rvInfantReg.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = infantAdapter
        }
        
        // Observe Delivery Outcome List
        lifecycleScope.launch {
            viewModel.deliveryOutcomeList.collect { list ->
                deliveryAdapter.submitList(list)
                binding.tvDeliveryCount.text = list.size.toString()
            }
        }
        
        // Observe Infant Registration List
        lifecycleScope.launch {
            viewModel.infantRegList.collect { list ->
                infantAdapter.submitList(list)
                binding.tvInfantCount.text = list.size.toString()
            }
        }
    }
    
    private fun openDeliveryOutcomeForm(patientID: String) {
        // Create fragment with arguments
        val fragment = DeliveryOutcomeFragment()
        val bundle = Bundle().apply {
            putString("patientID", patientID)
        }
        fragment.arguments = bundle
        
        // Replace current fragment
        parentFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack("delivery_outcome")
            .commit()
    }
    
    private fun openInfantRegList(patientID: String) {
        // Create fragment with arguments
        val fragment = InfantRegListFragment()
        val bundle = Bundle().apply {
            putString("patientID", patientID)
        }
        fragment.arguments = bundle
        
        // Replace current fragment
        parentFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack("infant_reg_list")
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
