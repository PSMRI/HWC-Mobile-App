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
import androidx.recyclerview.widget.RecyclerView
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
                    handleFormFieldChange(formId)
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

    /**
     * Handle form field changes and update UI accordingly
     */
    private fun handleFormFieldChange(formId: Int) {
        val layoutManager = binding.form.rvInputForm.layoutManager as? LinearLayoutManager ?: return
        val adapter = binding.form.rvInputForm.adapter ?: return
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        val offset = binding.form.rvInputForm.getChildAt(0)?.top ?: 0

        when (formId) {
            14 -> handleComplicationsFieldChange(adapter)
            15, 16 -> handleAnthropometryFieldChange(adapter)
            18 -> handlePreExistingConditionsFieldChange(adapter)
            19, 21, 23 -> handleTestResultFieldChange(formId, adapter, layoutManager, firstVisiblePosition, offset)
            5 -> handleLmpFieldChange(adapter)
            10 -> handleGravidaFieldChange(adapter)
        }
    }

    /**
     * Handle complications field change
     */
    private fun handleComplicationsFieldChange(adapter: RecyclerView.Adapter<*>) {
        val complicationsIndex = viewModel.getIndexOfComplications()
        if (complicationsIndex >= 0) {
            adapter.notifyItemChanged(complicationsIndex)
        }
    }

    /**
     * Handle anthropometry (height/weight) field change
     */
    private fun handleAnthropometryFieldChange(adapter: RecyclerView.Adapter<*>) {
        val bmiPosition = viewModel.getIndexOfBmi()
        if (bmiPosition >= 0) {
            adapter.notifyItemChanged(bmiPosition)
        }
    }

    /**
     * Handle pre-existing conditions field change
     */
    private fun handlePreExistingConditionsFieldChange(adapter: RecyclerView.Adapter<*>) {
        val preExistingConditionsIndex = viewModel.getIndexOfPreExistingConditions()
        if (preExistingConditionsIndex >= 0) {
            adapter.notifyItemChanged(preExistingConditionsIndex)
        }
    }

    /**
     * Handle test result field change (VDRL/RPR, HIV, HBsAg)
     */
    private fun handleTestResultFieldChange(
        formId: Int,
        adapter: RecyclerView.Adapter<*>, // Corrected this line
        layoutManager: LinearLayoutManager,
        firstVisiblePosition: Int,
        offset: Int
    ) {
        // Force immediate UI update for date fields
        lifecycleScope.launch {
            delay(100)
            adapter.notifyDataSetChanged()
            layoutManager.scrollToPositionWithOffset(firstVisiblePosition, offset)
        }

        // Handle test date field update
        val testDateFieldId = getTestDateFieldId(formId)
        if (testDateFieldId != -1) {
            val testDateIndex = viewModel.getIndexOfTestDate(testDateFieldId)
            if (testDateIndex >= 0) {
                adapter.notifyItemChanged(testDateIndex)
            } else {
                adapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * Get test date field ID based on test result field ID
     */
    private fun getTestDateFieldId(testResultFieldId: Int): Int {
        return when (testResultFieldId) {
            19 -> 20 // vdrlRprDate.id
            21 -> 22 // hivTestDate.id
            23 -> 24 // hbsAgTestDate.id
            else -> -1
        }
    }

    /**
     * Handle LMP field change
     */
    private fun handleLmpFieldChange(adapter: RecyclerView.Adapter<*>) {
        adapter.notifyItemChanged(viewModel.getIndexOfEdd())
        adapter.notifyItemChanged(viewModel.getIndexOfGestationalAge())
        adapter.notifyItemChanged(viewModel.getIndexOfTrimester())
    }

    /**
     * Handle Gravida field change
     */
    private fun handleGravidaFieldChange(adapter: RecyclerView.Adapter<*>) {
        adapter.notifyItemChanged(viewModel.getIndexOfPara())
        adapter.notifyDataSetChanged()
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
                        findNavController().navigate(R.id.eligibleCoupleTrackingFormFragment)
                        viewModel.clearNavigation()
                    }

                    is PregnancyRegistrationFormViewModel.NavigationEvent.ToVitalsAndPrescription -> {
                        val action =
                            PregnantWomanRegistrationFragmentDirections.actionPregnantWomanRegistrationFragmentToCustomVitalsFragment()
                        findNavController().navigate(action)
                        viewModel.clearNavigation()
                    }
                    else -> {
                        //Do nothing
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
    override fun onSubmitAction() {
        submitRegistrationForm()
    }

    override fun onCancelAction() {
        if (viewModel.recordExists.value == true) {
            val action = PregnantWomanRegistrationFragmentDirections.actionPregnantWomanRegistrationFragmentToPatientHomeFragment()
            findNavController().navigate(action)
        } else {
            findNavController().navigateUp()
        }
    }

    override fun getFragmentId(): Int = R.id.pregnantWomanRegistrationFragment

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
