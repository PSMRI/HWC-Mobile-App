package org.piramalswasthya.cho.ui.commons.maternal_health.pregnant_women_registration.form

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
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
import org.piramalswasthya.cho.databinding.FragmentPregnancyRegistrationFormBinding
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.model.InputType
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class PregnantWomanRegistrationFragment : Fragment(), NavigationAdapter {

    private var _binding: FragmentPregnancyRegistrationFormBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var userRepo: UserRepo

    private val viewModel: PregnancyRegistrationFormViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPregnancyRegistrationFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onCancelAction()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val onBackPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onCancelAction()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )

        setupForm()
        observeViewModel()

        viewModel.benName.observe(viewLifecycleOwner) { name ->
            binding.tvBenName.text = name ?: ""
        }
        viewModel.benAgeGender.observe(viewLifecycleOwner) { ageGender ->
            binding.tvAgeGender.text = ageGender ?: ""
        }

        binding.tvCaseId.text = viewModel.patientID ?: ""
        binding.llCaseIdRow.visibility = View.VISIBLE

        binding.btnSubmit.setOnClickListener {
            submitRegistrationForm()
        }

        binding.btnCancel.setOnClickListener {
            onCancelAction()
        }

        binding.fabEdit.setOnClickListener {
            viewModel.setRecordExist(false)
        }
    }

    private fun setupForm() {
        viewModel.recordExists.observe(viewLifecycleOwner) { exists ->
            // Skip UI updates if we are in process of saving/navigating to prevent flickering
            if (viewModel.state.value == PregnancyRegistrationFormViewModel.State.SAVING) return@observe
            
            val isReadOnly = viewModel.isReadOnly.value ?: false
            val title = if (exists) {
                getString(R.string.title_view_pregnancy_registration)
            } else {
                getString(R.string.title_register_pregnancy)
            }
            (activity as? AppCompatActivity)?.supportActionBar?.title = title
            activity?.findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
            
            binding.fabEdit.visibility = if (exists && !isReadOnly) View.VISIBLE else View.GONE
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
                viewModel.formList.collect { list ->
                    if (list.isNotEmpty()) {
                        val processedList = if (exists) {
                            list.map { item ->
                                if (item.inputType == InputType.CHECKBOXES) {
                                    item.copy(inputType = InputType.TEXT_VIEW)
                                } else item
                            }
                        } else list
                        adapter.submitList(processedList)
                    }
                }
            }
        }
        
        viewModel.isReadOnly.observe(viewLifecycleOwner) { isReadOnly ->
            if (viewModel.state.value == PregnancyRegistrationFormViewModel.State.SAVING) return@observe
            
            val exists = viewModel.recordExists.value ?: false
            binding.fabEdit.visibility = if (exists && !isReadOnly) View.VISIBLE else View.GONE
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
                    
                    // Immediately update activity title to Vitals for faster feedback
                    requireActivity().findViewById<android.widget.TextView>(R.id.header_text_register_patient)?.text = getString(R.string.vitals_text)
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
                        if (navigationEvent.showSuccessToast) {
                            Toast.makeText(context, "Registration Successful", Toast.LENGTH_LONG).show()
                        }
                        val action = PregnantWomanRegistrationFragmentDirections
                            .actionPregnantWomanRegistrationFragmentToEligibleCoupleTrackingFormFragment(
                                patientID = navigationEvent.patientID,
                                createdDate = 0L
                            )
                        findNavController().navigate(action)
                        viewModel.clearNavigation()
                    }

                    is PregnancyRegistrationFormViewModel.NavigationEvent.ToVitalsAndPrescription -> {
                        val action =
                            PregnantWomanRegistrationFragmentDirections.actionPregnantWomanRegistrationFragmentToCustomVitalsFragment()
                        findNavController().navigate(action)
                        viewModel.clearNavigation()
                    }

                    is PregnancyRegistrationFormViewModel.NavigationEvent.ToVitalsActivity -> {
                        // Force content to be hidden during navigation to prevent flickering
                        binding.llContent.visibility = View.GONE
                        binding.pbForm.visibility = View.VISIBLE
                        
                        if (navigationEvent.showSuccessToast) {
                            Toast.makeText(context, "Registration Successful", Toast.LENGTH_LONG).show()
                        }
                        
                        val activity = requireActivity()
                        if (activity is org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity) {
                            // If already in EditPatientDetailsActivity, just navigate to the Vitals fragment
                            val masterDb = org.piramalswasthya.cho.model.MasterDb(navigationEvent.benVisitInfo.patient.patientID).apply {
                                visitMasterDb.category = "RMNCH"
                                visitMasterDb.subCategory = org.piramalswasthya.cho.ui.commons.DropdownConst.careAndPreg
                                visitMasterDb.reason = org.piramalswasthya.cho.ui.commons.DropdownConst.anc
                            }
                            findNavController().navigate(
                                R.id.customVitalsFragment,
                                Bundle().apply {
                                    putSerializable("MasterDb", masterDb)
                                }
                            )
                        } else {
                            // Otherwise start the activity
                            val intent = android.content.Intent(
                                activity,
                                org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity::class.java
                            )
                            intent.putExtra("benVisitInfo", navigationEvent.benVisitInfo)
                            intent.putExtra("navigateTo", "VITALS")
                            startActivity(intent)
                            activity.finish()
                        }
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

        if (!viewModel.validateHeight()) {
            val heightIndex = viewModel.getIndexOfHeight()
            if (heightIndex >= 0) {
                adapter.notifyItemChanged(heightIndex)
                binding.form.rvInputForm.scrollToPosition(heightIndex)
                return false
            }
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
        val fromVisitDetails = arguments?.getBoolean("fromVisitDetails", false) ?: false
        if (fromVisitDetails) {
            try {
                findNavController().popBackStack(R.id.fhirVisitDetailsFragment, false)
            } catch (e: Exception) {
                Timber.e(e, "Navigation back to Visit Details failed")
                if (!findNavController().navigateUp()) {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
            return
        }

        arguments?.getBoolean("fromECT", false)?.let { fromECT ->
            if (fromECT) {
                try {
                    findNavController().popBackStack(R.id.fhirVisitDetailsFragment, false)
                } catch (e: Exception) {
                    Timber.e(e, "Navigation to Visit Details failed")
                    if (!findNavController().navigateUp()) {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
                return
            }
        }

        if (!findNavController().navigateUp()) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun getFragmentId(): Int = R.id.pregnantWomanRegistrationFragment

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
