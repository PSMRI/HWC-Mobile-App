package org.piramalswasthya.cho.ui.commons.maternal_health.delivery_outcome.form

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.FormInputAdapter
import org.piramalswasthya.cho.databinding.FragmentNewFormBinding
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.ui.commons.maternal_health.delivery_outcome.form.DeliveryOutcomeFormViewModel.Alert
import org.piramalswasthya.cho.ui.commons.maternal_health.delivery_outcome.form.DeliveryOutcomeFormViewModel.State
import org.piramalswasthya.cho.ui.home.rmncha.maternal_health.ANCVisitsActivity

@AndroidEntryPoint
class DeliveryOutcomeFormFragment : Fragment() {

    private var _binding: FragmentNewFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DeliveryOutcomeFormViewModel by viewModels()

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
        binding.fabEdit.visibility = View.GONE
        binding.btnSubmit.text = getString(R.string.do_next)
        binding.btnSubmit.visibility = View.VISIBLE
        
        // Show form title for Delivery Outcome
        binding.tvFormTitle.visibility = View.VISIBLE
        binding.tvFormTitle.text = getString(R.string.delivery_outcome)

        var adapter: FormInputAdapter? = null
        viewModel.recordExists.observe(viewLifecycleOwner) { recordExists ->
            if (adapter == null) {
                adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                    },
                    isEnabled = !(recordExists == true)
                )
                binding.form.rvInputForm.adapter = adapter
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.formList.collect { list ->
                            if (list.isNotEmpty()) adapter?.submitList(list)
                        }
                    }
                }
            }
        }

        // Observe patient details
        viewModel.benName.observe(viewLifecycleOwner) { binding.tvBenName.text = it }
        viewModel.benAgeGender.observe(viewLifecycleOwner) { binding.tvAgeGender.text = it }
        
        // Observe and display Case ID
        viewModel.caseId.observe(viewLifecycleOwner) { caseId ->
            caseId?.let {
                binding.llCaseIdRow.visibility = View.VISIBLE
                binding.tvCaseId.text = it
            }
        }
        
        // View ANC History link - show and set click listener
        binding.tvViewAncHistory.visibility = View.VISIBLE
        binding.tvViewAncHistory.setOnClickListener {
            val intent = ANCVisitsActivity.getIntent(requireContext(), viewModel.patientID)
            startActivity(intent)
        }

        // Observe mother condition specific alerts (PPH, Hysterectomy, Maternal Death)
        viewModel.alert.observe(viewLifecycleOwner) { alert ->
            alert?.let {
                viewModel.clearAlert()
                MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog)
                    .setTitle(getString(R.string.alert_popup))
                    .setMessage(
                        when (it) {
                            is Alert.InformDistrictNodalOfficer -> it.message
                            is Alert.IntensivePncMonitoring -> it.message
                            is Alert.HysterectomyNote -> it.message
                        }
                    )
                    .setPositiveButton(getString(android.R.string.ok)) { d, _ -> d.dismiss() }
                    .show()
            }
        }
        
        // Observe general alerts (date, place, gestational age, unskilled delivery, etc.)
        lifecycleScope.launch {
            viewModel.alertMessageFlow.collect { message ->
                message?.let {
                    if (isAdded) {
                        MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog)
                            .setTitle(getString(R.string.alert_popup))
                            .setMessage(it)
                            .setPositiveButton(getString(android.R.string.ok)) { dialog, _ -> 
                                dialog.dismiss()
                                viewModel.clearAlertMessage()
                            }
                            .setOnDismissListener {
                                viewModel.clearAlertMessage()
                            }
                            .show()
                    }
                }
            }
        }

        binding.btnSubmit.setOnClickListener { onNextClicked() }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                State.IDLE -> { }
                State.SAVING -> {
                    binding.llContent.visibility = View.GONE
                    binding.pbForm.visibility = View.VISIBLE
                }
                State.SAVE_SUCCESS_NAVIGATE_VITALS -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    viewModel.resetState()
                    navigateToVitals()
                }
                State.SAVE_FAILED -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(requireContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                    viewModel.resetState()
                }
                else -> { }
            }
        }
    }

    private fun onNextClicked() {
        val adapter = binding.form.rvInputForm.adapter as? FormInputAdapter ?: return
        val invalidIndex = adapter.validateInput(resources)
        if (invalidIndex == -1) {
            viewModel.saveForm()
        } else {
            invalidIndex.let { binding.form.rvInputForm.scrollToPosition(it) }
        }
    }

    private fun navigateToVitals() {
        val benVisitInfo = (activity?.intent?.getSerializableExtra("benVisitInfo") as? PatientDisplayWithVisitInfo)
        if (benVisitInfo != null) {
            try {
                val bundle = Bundle().apply { putSerializable("benVisitInfo", benVisitInfo) }
                findNavController().navigate(
                    R.id.action_deliveryOutcomeFormFragment_to_customVitalsFragment,
                    bundle
                )
            } catch (e: IllegalStateException) {
                // Not in a navigation graph, use fragment transaction or finish
                requireActivity().supportFragmentManager.popBackStack()
            }
        } else {
            // No visit info, just go back
            try {
                findNavController().navigateUp()
            } catch (e: IllegalStateException) {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
