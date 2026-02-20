package org.piramalswasthya.cho.ui.commons.maternal_health.neonatal_outcome.form

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.asLiveData
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.FormInputAdapter
import org.piramalswasthya.cho.databinding.FragmentNeonatalOutcomeBinding
import org.piramalswasthya.cho.ui.commons.maternal_health.neonatal_outcome.form.NeonatalOutcomeFormViewModel.State
import timber.log.Timber

@AndroidEntryPoint
class NeonatalOutcomeFormFragment : Fragment() {

    private var _binding: FragmentNeonatalOutcomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NeonatalOutcomeFormViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNeonatalOutcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = FormInputAdapter(
            formValueListener = FormInputAdapter.FormValueListener { id, index ->
                viewModel.onFieldValueChanged(id, index)
            }
        )

        binding.rvForm.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.rvForm.adapter = adapter

        viewModel.formElements.asLiveData().observe(viewLifecycleOwner) { elements ->
            adapter.submitList(elements)
        }

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                State.IDLE -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                }
                State.LOADING -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSave.isEnabled = false
                }
                State.SAVING -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSave.isEnabled = false
                }
                State.SAVE_SUCCESS -> {
                    Toast.makeText(requireContext(), "Neonatal outcomes saved successfully", Toast.LENGTH_SHORT).show()
                    navigateToVitals()
                }
                State.SAVE_FAILED -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            viewModel.saveForm()
        }

        binding.btnCancel.setOnClickListener {
            safeNavigateBack()
        }
    }

    private fun navigateToVitals() {
        try {
            // Navigate to vitals screen
            findNavController().navigateUp()
        } catch (e: Exception) {
            Timber.e(e, "Navigation error")
            // Fallback: just go back
            safeNavigateBack()
        }
    }

    /**
     * Safely navigate back, handling both NavController-hosted and
     * FragmentManager-hosted scenarios.
     */
    private fun safeNavigateBack() {
        try {
            findNavController().popBackStack()
        } catch (e: IllegalStateException) {
            if (!parentFragmentManager.popBackStackImmediate()) {
                requireActivity().finish()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
