package org.piramalswasthya.cho.ui.commons.maternal_health.infant_reg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentListBinding

@AndroidEntryPoint
class InfantRegListFragment : Fragment() {
    
    private var _binding: FragmentListBinding? = null
    private val binding: FragmentListBinding
        get() = _binding!!
        
    private val viewModel: InfantRegListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val adapter = InfantRegListAdapter { babyIndex ->
            navigateToInfantRegForm(babyIndex)
        }
        
        binding.rvList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvList.adapter = adapter

        lifecycleScope.launch {
            viewModel.infantList.collect { list ->
                if (list.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.rvList.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    binding.rvList.visibility = View.VISIBLE
                }
                adapter.submitList(list)
            }
        }
    }

    private fun navigateToInfantRegForm(babyIndex: Int) {
        val action = InfantRegListFragmentDirections.actionInfantRegListFragmentToInfantRegFragment(
            patientID = viewModel.patientID,
            babyIndex = babyIndex
        )
        findNavController().navigate(action)
    }

    override fun onStart() {
        super.onStart()
        activity?.title = "Infant Registration List"
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
