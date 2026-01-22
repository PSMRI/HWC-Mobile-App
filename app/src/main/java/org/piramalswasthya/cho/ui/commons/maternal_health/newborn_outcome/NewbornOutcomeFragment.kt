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
        when (formId) {
            11 -> handleSexSelection(index) // Baby1Sex
            14 -> handleBirthWeight() // Baby1BirthWeight
            10 -> handleOutcomeAtBirth(index) // Baby1OutcomeAtBirth
            19 -> handleCurrentStatus(index) // Baby1CurrentStatus
            18 -> handleComplications() // Baby1Complications
            26 -> handleBirthCertificate(index) // Baby1BirthCertificate
        }
        
        binding.form.rvInputForm.adapter?.apply {
            when (formId) {
                else -> {}
            }
        }
    }

    private fun handleSexSelection(index: Int) {
        // If Ambiguous (index 2) → show alert for specialist referral
        if (index == 2) {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Specialist Referral Required")
                .setMessage("Ambiguous sex detected. Please refer to pediatric specialist for evaluation.")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun handleBirthWeight() {
        viewModel.getBirthWeight()?.let { weight ->
            val message = when {
                weight < 1000 -> "⚠️ ELBW Alert: Extremely Low Birth Weight (<1000g). Immediate SNCU referral required."
                weight < 1500 -> "⚠️ VLBW Alert: Very Low Birth Weight (<1500g). Specialist care needed."
                weight < 2500 -> "⚠️ LBW Alert: Low Birth Weight (<2500g). Close monitoring required."
                weight >= 4000 -> "⚠️ Macrosomia Alert: Birth weight ≥4000g. Screen mother for GDM."
                else -> null
            }
            
            message?.let {
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Birth Weight Alert")
                    .setMessage(it)
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    private fun handleOutcomeAtBirth(index: Int) {
        // If Stillbirth (index 1 or 2) or Died during delivery (index 3)
        if (index in 1..3) {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Stillbirth/Death Audit Required")
                .setMessage("Please complete the stillbirth/neonatal death audit form as per protocol.")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun handleCurrentStatus(index: Int) {
        when (index) {
            1, 2 -> { // Admitted (SNCU/NICU) or Admitted (General ward)
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("PNC Counseling Required")
                    .setMessage("Baby is admitted. Please provide PNC counseling to mother regarding baby's condition and follow-up care.")
                    .setPositiveButton("OK", null)
                    .show()
            }
            3 -> { // Died
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Neonatal Death Audit Required")
                    .setMessage("Please complete the neonatal death audit form immediately.")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    private fun handleComplications() {
        viewModel.hasComplications()?.let { hasComplications ->
            if (hasComplications) {
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Immediate Review Required")
                    .setMessage("Newborn complications detected. Immediate pediatric/specialist review required.")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    private fun handleBirthCertificate(index: Int) {
        // If "No (Not applied)" (index 2)
        if (index == 2) {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Birth Registration Required")
                .setMessage("Please inform the family that birth registration is a legal requirement within 21 days of birth.")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
