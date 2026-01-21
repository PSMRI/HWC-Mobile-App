package org.piramalswasthya.cho.ui.home_activity.rmnch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.databinding.FragmentMaternalHealthBinding
import org.piramalswasthya.cho.ui.commons.maternal_health.delivery_outcome.list.DeliveryOutcomeListAdapter
import org.piramalswasthya.cho.ui.commons.maternal_health.infant_reg.dashboard.InfantRegDashboardAdapter

@AndroidEntryPoint
class MaternalHealthFragment : Fragment() {

    private var _binding: FragmentMaternalHealthBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MaternalHealthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMaternalHealthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Setup Delivery Outcome List
        val deliveryAdapter = DeliveryOutcomeListAdapter { patientID ->
            val action = MaternalHealthFragmentDirections
                .actionMaternalHealthFragmentToDeliveryOutcomeFragment(patientID)
            findNavController().navigate(action)
        }
        binding.rvDeliveryOutcome.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = deliveryAdapter
        }
        
        // Setup Infant Registration List
        val infantAdapter = InfantRegDashboardAdapter { patientID ->
            val action = MaternalHealthFragmentDirections
                .actionMaternalHealthFragmentToInfantRegListFragment(patientID)
            findNavController().navigate(action)
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
                binding.tvDeliveryEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                binding.rvDeliveryOutcome.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
            }
        }
        
        // Observe Infant Registration List
        lifecycleScope.launch {
            viewModel.infantRegList.collect { list ->
                infantAdapter.submitList(list)
                binding.tvInfantCount.text = list.size.toString()
                binding.tvInfantEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                binding.rvInfantReg.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
