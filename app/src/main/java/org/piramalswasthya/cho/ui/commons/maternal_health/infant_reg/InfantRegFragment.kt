package org.piramalswasthya.cho.ui.commons.maternal_health.infant_reg

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
class InfantRegFragment : Fragment() {

    private var _binding: FragmentNewFormBinding? = null
    private val binding: FragmentNewFormBinding
        get() = _binding!!

    private val viewModel: InfantRegViewModel by viewModels()

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
                        if (it.isNotEmpty())
                            adapter.submitList(it)
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
            submitInfantRegForm()
        }

        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                InfantRegViewModel.State.IDLE -> {
                }

                InfantRegViewModel.State.SAVING -> {
                    binding.llContent.visibility = View.GONE
                    binding.pbForm.visibility = View.VISIBLE
                }

                InfantRegViewModel.State.SAVE_SUCCESS -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    WorkerUtils.triggerAmritSyncWorker(requireContext())
                    Toast.makeText(
                        requireContext(),
                        "Infant registration saved successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigateUp()
                }

                InfantRegViewModel.State.SAVE_FAILED -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Failed to save infant registration",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun submitInfantRegForm() {
        if (validateCurrentPage()) {
            viewModel.saveForm()
        }
    }

    private fun validateCurrentPage(): Boolean {
        val result =
            (binding.form.rvInputForm.adapter as? FormInputAdapter)?.validateInput(resources)
        return if (result == -1) true
        else {
            result?.let {
                binding.form.rvInputForm.scrollToPosition(it)
            }
            false
        }
    }

    private fun hardCodedListUpdate(formId: Int) {
        binding.form.rvInputForm.adapter?.apply {
            when (formId) {
                11 -> notifyDataSetChanged()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.title = "Infant Registration"
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
