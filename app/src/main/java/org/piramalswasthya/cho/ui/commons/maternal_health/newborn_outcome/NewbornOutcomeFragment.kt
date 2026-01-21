package org.piramalswasthya.cho.ui.commons.maternal_health.newborn_outcome

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

/**
 * Newborn Outcome Fragment - Comprehensive neonatal health capture
 */
@AndroidEntryPoint
class NewbornOutcomeFragment : Fragment() {

    private var _binding: FragmentNewFormBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NewbornOutcomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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
                        handleFormValueChange(formId, index)
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

        viewModel.patientName.observe(viewLifecycleOwner) { 
            binding.tvBenName.text = it 
        }
        
        viewModel.patientAgeGender.observe(viewLifecycleOwner) { 
            binding.tvAgeGender.text = it 
        }

        binding.btnSubmit.setOnClickListener { 
            submitNewbornOutcomeForm() 
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is NewbornOutcomeViewModel.State.SAVING -> toggleLoading(true)
                is NewbornOutcomeViewModel.State.SAVE_SUCCESS -> handleSaveSuccess()
                is NewbornOutcomeViewModel.State.SAVE_FAILED -> {
                    toggleLoading(false)
                    Toast.makeText(requireContext(), "Failed to save newborn outcome", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun toggleLoading(isLoading: Boolean) {
        binding.llContent.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.pbForm.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun handleSaveSuccess() {
        toggleLoading(false)
        WorkerUtils.triggerAmritSyncWorker(requireContext())
        Toast.makeText(requireContext(), "Newborn outcome saved successfully", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    private fun submitNewbornOutcomeForm() {
        if (validateCurrentPage()) {
            viewModel.saveForm()
        }
    }

    private fun validateCurrentPage(): Boolean {
        val result = (binding.form.rvInputForm.adapter as? FormInputAdapter)?.validateInput(resources)
        return if (result == -1) true else {
            result?.let { binding.form.rvInputForm.scrollToPosition(it) }
            false
        }
    }

    /**
     * Handle conditional form logic based on field changes
     */
    private fun handleFormValueChange(formId: Int, index: Int) {
        // TODO: Add alerts for medical conditions:
        // - Birth weight < 2500g → LBW alert
        // - Sex = "Ambiguous" → Specialist referral alert
        // - Outcome = Stillbirth → Audit form alert
        // - Status = "Died" → Neonatal death audit alert
        // - Status = "Admitted" → PNC counseling alert
        // - Birth certificate = "No" → Legal requirement alert
        
        binding.form.rvInputForm.adapter?.apply {
            when (formId) {
                // Add specific field IDs here if needed for UI updates
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
