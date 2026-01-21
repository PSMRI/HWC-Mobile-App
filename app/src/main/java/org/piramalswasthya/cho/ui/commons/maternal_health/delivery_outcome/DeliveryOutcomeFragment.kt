package org.piramalswasthya.cho.ui.commons.maternal_health.delivery_outcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.adapter.FormInputAdapter
import org.piramalswasthya.cho.databinding.FragmentNewFormBinding
import org.piramalswasthya.cho.work.WorkerUtils

@AndroidEntryPoint
class DeliveryOutcomeFragment : Fragment() {

    private var _binding: FragmentNewFormBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DeliveryOutcomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->
            notIt?.let { recordExists ->
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                        hardCodedListUpdate(formId)
                    },
                    isEnabled = !recordExists
                )
                binding.btnSubmit.isEnabled = !recordExists
                binding.form.rvInputForm.adapter = adapter
                lifecycleScope.launch { 
                    viewModel.formList.collect { 
                        if (it.isNotEmpty()) adapter.submitList(it) 
                    } 
                }
            }
        }
        
        viewModel.patientName.observe(viewLifecycleOwner) { binding.tvBenName.text = it }
        viewModel.patientAgeGender.observe(viewLifecycleOwner) { binding.tvAgeGender.text = it }
        binding.btnSubmit.setOnClickListener { submitDeliveryOutcomeForm() }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
               is DeliveryOutcomeViewModel.State.SAVING -> toggleLoading(true)
               is DeliveryOutcomeViewModel.State.SAVE_SUCCESS -> handleSaveSuccess(state.shouldNavigateToInfantReg)
               is DeliveryOutcomeViewModel.State.SAVE_FAILED -> {
                   toggleLoading(false)
                   Toast.makeText(requireContext(), "Failed to save delivery outcome", Toast.LENGTH_SHORT).show()
               }
                else -> {}
            }
        }
    }

    private fun toggleLoading(isLoading: Boolean) {
        binding.llContent.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.pbForm.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun handleSaveSuccess(shouldNavigateToInfantReg: Boolean) {
        toggleLoading(false)
        WorkerUtils.triggerAmritSyncWorker(requireContext())
        Toast.makeText(requireContext(), "Delivery outcome saved successfully", Toast.LENGTH_SHORT).show()
        
        if (shouldNavigateToInfantReg) {
            navigateToInfantRegList()
        } else {
            findNavController().navigateUp()
        }
    }

    private fun navigateToInfantRegList() {
        val action = DeliveryOutcomeFragmentDirections.actionDeliveryOutcomeFragmentToInfantRegListFragment(
            patientID = viewModel.patientID
        )
        findNavController().navigate(action)
    }

    private fun submitDeliveryOutcomeForm() {
        if (validateCurrentPage()) viewModel.saveForm()
    }

    private fun validateCurrentPage(): Boolean {
        val result = (binding.form.rvInputForm.adapter as? FormInputAdapter)?.validateInput(resources)
        return if (result == -1) true else {
            result?.let { binding.form.rvInputForm.scrollToPosition(it) }
            false
        }
    }

    private fun hardCodedListUpdate(formId: Int) {
        binding.form.rvInputForm.adapter?.apply {
            when (formId) {
                1, 6, 10, 11, 12 -> notifyDataSetChanged()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.title = "Delivery Outcome"
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
