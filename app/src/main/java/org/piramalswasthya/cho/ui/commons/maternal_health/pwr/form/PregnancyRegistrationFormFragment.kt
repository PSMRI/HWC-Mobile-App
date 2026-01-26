package org.piramalswasthya.cho.ui.commons.maternal_health.pregnant_women_registration.form

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.FormInputAdapter
import org.piramalswasthya.cho.databinding.FragmentNewFormBinding
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class PregnantWomanRegistrationFragment : Fragment(), NavigationAdapter {

    private var _binding: FragmentNewFormBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var userRepo: UserRepo

    private val viewModel: PregnancyRegistrationFormViewModel by viewModels()

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

        requireActivity().title = getString(R.string.icon_title_pmr)
        setupForm()
        observeViewModel()

        binding.btnSubmit.setOnClickListener {
            submitRegistrationForm()
        }

        binding.fabEdit.setOnClickListener {
            viewModel.setRecordExist(false)
        }
    }

    private fun setupForm() {
        viewModel.recordExists.observe(viewLifecycleOwner) { exists ->
            binding.fabEdit.visibility = if (exists) View.VISIBLE else View.GONE
            binding.btnSubmit.visibility = if (exists) View.GONE else View.VISIBLE

            val adapter = FormInputAdapter(
                formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                    viewModel.updateListOnValueChanged(formId, index)
                    val layoutManager = binding.form.rvInputForm.layoutManager as LinearLayoutManager
                    val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
                    val offset = binding.form.rvInputForm.getChildAt(0)?.top ?: 0

                    // Specifically handle complications field to update immediately
                    if (formId == 14) { // complicationsInPreviousPregnancy.id (updated from 13)
                        // Find the position of the complications field
                        val complicationsIndex = viewModel.getIndexOfComplications()
                        if (complicationsIndex >= 0) {
                            // Notify item changed to refresh the view
                            binding.form.rvInputForm.adapter?.notifyItemChanged(complicationsIndex)
                        }
                    }
                    if (formId == 19 || formId == 21 || formId == 23) {
                        // Force immediate UI update for date fields
                        lifecycleScope.launch {
                            delay(100)
                            binding.form.rvInputForm.adapter?.notifyDataSetChanged()

                            // Restore scroll position
                            layoutManager.scrollToPositionWithOffset(firstVisiblePosition, offset)
                        }
                    }
                    if (formId == 18) { // preExistingConditions.id (updated from 17)
                        // Find the position of the complications field
                        val preExistingConditionsIndex = viewModel.getIndexOfPreExistingConditions()
                        if (preExistingConditionsIndex >= 0) {
                            // Notify item changed to refresh the view
                            binding.form.rvInputForm.adapter?.notifyItemChanged(preExistingConditionsIndex)
                        }
                    }
                    if (formId == 15 || formId == 16) { // height or weight fields (updated from 14,15)
                        val bmiPosition = viewModel.getIndexOfBmi()
                        if (bmiPosition >= 0) {
                            binding.form.rvInputForm.adapter?.notifyItemChanged(bmiPosition)
                        }
                    }

                    if (formId == 5) { // lmp.id (updated from 4)
                        binding.form.rvInputForm.adapter?.apply {
                            notifyItemChanged(viewModel.getIndexOfEdd())
                            notifyItemChanged(viewModel.getIndexOfGestationalAge())
                            notifyItemChanged(viewModel.getIndexOfTrimester())
                        }
                    }
                    if (formId == 10) { // gravida.id (updated from 9)
                        binding.form.rvInputForm.adapter?.apply {
                            notifyItemChanged(viewModel.getIndexOfPara())
                        }
                    }

                    // Handle VDRL/RPR, HIV, and HBsAg results for showing/hiding date fields
                    if (formId == 19 || formId == 21 || formId == 23) { // vdrlRprResult.id, hivResult.id, hbsAgResult.id
                        val testResultFieldId = formId
                        val testDateFieldId = when (formId) {
                            19 -> 20 // vdrlRprDate.id
                            21 -> 22 // hivTestDate.id
                            23 -> 24 // hbsAgTestDate.id
                            else -> -1
                        }

                        if (testDateFieldId != -1) {
                            val testDateIndex = viewModel.getIndexOfTestDate(testDateFieldId)
                            if (testDateIndex >= 0) {
                                binding.form.rvInputForm.adapter?.notifyItemChanged(testDateIndex)
                            } else {
                                // If date field should be shown but isn't in list yet, refresh the whole list
                                binding.form.rvInputForm.adapter?.notifyDataSetChanged()
                            }
                        }
                    }
                },
                isEnabled = !exists
            )

            binding.form.rvInputForm.adapter = adapter

            lifecycleScope.launch {
                viewModel.formList.collect {
                    if (it.isNotEmpty()) adapter.submitList(it)
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                PregnancyRegistrationFormViewModel.State.IDLE -> Unit

                PregnancyRegistrationFormViewModel.State.SAVING -> {
                    binding.llContent.visibility = View.GONE
                    binding.pbForm.visibility = View.VISIBLE
                }

                PregnancyRegistrationFormViewModel.State.SAVE_SUCCESS -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(context, "Registration Successful", Toast.LENGTH_LONG).show()
                }

                PregnancyRegistrationFormViewModel.State.SAVE_FAILED -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(context, "Save failed", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Observe alerts from ViewModel
        viewModel.showAlert.observe(viewLifecycleOwner) { message ->
            message?.let { alertMessage ->
                // Show the alert dialog
                AlertDialog.Builder(requireContext())
                    .setTitle("Alert")
                    .setMessage(alertMessage)
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        // Clear the alert after showing
                        viewModel.clearAlert()
                    }
                    .setCancelable(false)
                    .show()

                // Log for debugging
                Timber.d("Alert shown: $alertMessage")
            }
        }

        // Observe navigation events
        viewModel.navigateTo.observe(viewLifecycleOwner) { event ->
            event?.let { navigationEvent ->
                when (navigationEvent) {
                    is PregnancyRegistrationFormViewModel.NavigationEvent.ToEligibleCouple -> {
                        // Navigate to Eligible Couple screen
                        // Replace with your actual navigation ID
                        // findNavController().navigate(R.id.eligibleCoupleFragment)
                        Timber.d("Navigate to Eligible Couple")
                        viewModel.clearNavigation()
                    }

                    is PregnancyRegistrationFormViewModel.NavigationEvent.ToVitalsAndPrescription -> {
                        // Navigate to Vitals & Prescription screen
                        // Replace with your actual navigation ID
                        // findNavController().navigate(R.id.vitalsAndPrescriptionFragment)
                        Timber.d("Navigate to Vitals and Prescription")
                        viewModel.clearNavigation()
                    }
                }
            }
        }
    }

    private fun submitRegistrationForm() {
        if (validateForm()) {
            viewModel.saveForm()
        }
    }

    private fun validateForm(): Boolean {
        val adapter = binding.form.rvInputForm.adapter as? FormInputAdapter
        if (adapter == null) {
            Timber.e("Form adapter is null")
            return false
        }

        val result = adapter.validateInput(resources)
        Timber.d("Validation result = $result")

        return if (result == -1) {
            true
        } else {
            binding.form.rvInputForm.scrollToPosition(result)
            false
        }
    }

    private fun navigateToNextScreen() {
        findNavController().navigate(R.id.pregnantWomanRegistrationFragment)
    }

    override fun onSubmitAction() {
        submitRegistrationForm()
    }

    override fun onCancelAction() {
        findNavController().navigateUp()
    }

    override fun getFragmentId(): Int = R.id.pregnantWomanRegistrationFragment

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}