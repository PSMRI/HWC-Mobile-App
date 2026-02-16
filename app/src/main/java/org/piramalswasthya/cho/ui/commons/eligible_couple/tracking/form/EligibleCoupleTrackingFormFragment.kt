package org.piramalswasthya.cho.ui.commons.eligible_couple.tracking.form

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.piramalswasthya.cho.adapter.FormInputAdapter
import org.piramalswasthya.cho.databinding.FragmentNewFormBinding
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.EligibleCoupleTrackingCache
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.UserDomain
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.OtherCPHCServicesViewModel
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.utils.generateUuid
import org.piramalswasthya.cho.work.WorkerUtils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class EligibleCoupleTrackingFormFragment : Fragment(), NavigationAdapter {

    companion object {
        private const val DATE_FORMAT_DD_MM_YYYY = "dd-MM-yyyy"
    }

    private var _binding: FragmentNewFormBinding? = null
    private val binding: FragmentNewFormBinding
        get() = _binding!!

    val viewModel: EligibleCoupleTrackingFormViewModel by viewModels()

    @Inject
    lateinit var userRepo: UserRepo

    @Inject
    lateinit var patientRepo: PatientRepo

    private var benVisitInfo: PatientDisplayWithVisitInfo? = null

    val CPHCviewModel: OtherCPHCServicesViewModel by viewModels()

    var fragmentContainerId: Int = 0

    val fragment: Fragment = this

    val jsonFile: String = "patient-visit-details-paginated.json"
    fun navigateNext() {
        submitEligibleTrackingForm()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadBeneficiaryInfo()
        setupFormAdapterAndVisibility()
        observeBasicBeneficiaryInfo()
        setupAntraTableObserver()
        setupClickListeners()
        observeViewModelState()
        observeAlerts()
        
        // Handle Back Press
        val fromVisitDetails = arguments?.getBoolean("fromVisitDetails", false) ?: false
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : androidx.activity.OnBackPressedCallback(fromVisitDetails) {
            override fun handleOnBackPressed() {
                navigateBackToList()
            }
        })
    }

    private fun loadBeneficiaryInfo() {
        lifecycleScope.launch {
            benVisitInfo = try {
                // Try to get from intent (for navigation from visit details)
                requireActivity().intent?.getSerializableExtra("benVisitInfo") as? PatientDisplayWithVisitInfo
                    ?: throw ClassCastException("No benVisitInfo in intent")
            } catch (e: Exception) {
                // Load from database using patientID from ViewModel
                patientRepo.getPatientDisplayListForNurseByPatient(viewModel.patientID)
            }
        }
    }

    private fun setupFormAdapterAndVisibility() {
        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->
            notIt?.let { recordExists ->
//                binding.fabEdit.visibility = if(recordExists) View.VISIBLE else View.GONE
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                        hardCodedListUpdate(formId)
                    }, isEnabled = !recordExists
                )
                // Hide parent activity's bottom navigation to avoid duplicate buttons
                activity?.findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE

                binding.btnSubmit.visibility = if(recordExists) View.GONE else View.VISIBLE
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
    }

    private fun observeBasicBeneficiaryInfo() {
        viewModel.benName.observe(viewLifecycleOwner) {
            binding.tvBenName.text = it
        }
        viewModel.benAgeGender.observe(viewLifecycleOwner) {
            binding.tvAgeGender.text = it
        }
        binding.tvCaseId.text = viewModel.patientID
        binding.llCaseIdRow.visibility = View.VISIBLE
    }

    private fun setupAntraTableObserver() {
        // Populate ANTRA table in view mode
        viewModel.recordExists.observe(viewLifecycleOwner) { recordExists ->
            if (recordExists == true) {
                // In view mode, populate ANTRA table if patient has ANTRA injection history
                viewModel.allEctRecords.observe(viewLifecycleOwner) { ectRecords ->
                    populateAntraTable(ectRecords)
                }
            }
        }
    }

    private fun setupClickListeners() {
        // Wire up submit button from fragment layout
        binding.btnSubmit.setOnClickListener {
            submitEligibleTrackingForm()
        }
    }

    private fun observeViewModelState() {
        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                EligibleCoupleTrackingFormViewModel.State.SAVING -> {
                    binding.btnSubmit.isEnabled = false
                    binding.pbForm.visibility = View.VISIBLE
                }
                EligibleCoupleTrackingFormViewModel.State.SAVE_SUCCESS -> {
                    binding.btnSubmit.isEnabled = true
                    binding.pbForm.visibility = View.GONE
                    // Check for alerts - if no alert, will show toast and navigate
                    // If alert exists, the alert observer will handle it
                    checkForAlerts()
                }
                EligibleCoupleTrackingFormViewModel.State.SAVE_FAILED -> {
                    binding.btnSubmit.isEnabled = true
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.form_save_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    binding.btnSubmit.isEnabled = true
                    binding.pbForm.visibility = View.GONE
                }
            }
        }
    }

    private fun observeAlerts() {
        // Observe alert LiveData
        viewModel.showAlert.observe(viewLifecycleOwner) { alertType ->
            when (alertType) {
                EligibleCoupleTrackingFormViewModel.AlertType.ANTRA_INCENTIVE -> {
                    showAntraIncentiveAlert()
                }
                EligibleCoupleTrackingFormViewModel.AlertType.STERILIZATION_INCENTIVE -> {
                    showSterilizationIncentiveAlert()
                }
                EligibleCoupleTrackingFormViewModel.AlertType.NONE -> {
                    //no ops for now
                }
            }
        }
    }

    private fun checkForAlerts() {
        if(viewModel.isPregnant) {
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.tracking_form_filled_successfully),
                Toast.LENGTH_SHORT
            ).show()
            saveNurseDataInBackground()
            viewModel.resetState()
            navigateToPregnancyRegistration()
            return
        }
        // If no alert to show, proceed directly
        if (viewModel.showAlert.value == EligibleCoupleTrackingFormViewModel.AlertType.NONE) {
            // Show success message
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.tracking_form_filled_successfully),
                Toast.LENGTH_SHORT
            ).show()
            // ECT data is already saved, so visit history will show it
            saveNurseDataInBackground()
            viewModel.resetState()
            navigateBackToList()
        }
        // Alerts will be handled by the observer
    }

    private fun navigateToPregnancyRegistration() {
        val bundle = Bundle().apply {
            putString("patientID", viewModel.patientID)
            putBoolean("fromECT", true)
        }
        
        // Attempt standard navigation first
        val navController = try { findNavController() } catch (e: Exception) { null }
        
        if (navController != null) {
            try {
                navController.navigate(R.id.action_eligibleCoupleTrackingFormFragment_to_pregnantWomanRegistrationFragment, bundle)
                return
            } catch (e: Exception) {
                Timber.e(e, "Standard navigation via NavController failed")
            }
        }
        
        // Fallback: Use manual fragment transaction if NavController is not available or failed
        Timber.i("Performing manual fragment transaction for pregnancy registration")
        try {
            val fragment = org.piramalswasthya.cho.ui.commons.maternal_health.pregnant_women_registration.form.PregnantWomanRegistrationFragment().apply {
                arguments = bundle
            }
            
            requireActivity().supportFragmentManager.commit {
                replace(R.id.fragment_container, fragment)
                addToBackStack(null)
            }
        } catch (ex: Exception) {
            Timber.e(ex, "Manual navigation fallback failed")
            Toast.makeText(requireContext(), "Unable to open registration form", Toast.LENGTH_SHORT).show()
            navigateBackToList()
        }
    }

    private fun navigateBackToList() {
        if (!isAdded || isRemoving) return

        val fromVisitDetails = arguments?.getBoolean("fromVisitDetails", false) ?: false
        Timber.d("Navigating back. fromVisitDetails: $fromVisitDetails")
        
        if (fromVisitDetails) {
            if (benVisitInfo == null) {
                Toast.makeText(requireContext(), "Loading patient data, please wait...", Toast.LENGTH_SHORT).show()
                return
            }
            try {
                // Toast.makeText(requireContext(), "Navigating to Visit Details", Toast.LENGTH_SHORT).show()
                val bundle = Bundle().apply {
                    putSerializable("benVisitInfo", benVisitInfo)
                }
                findNavController().navigate(R.id.action_eligibleCoupleTrackingFormFragment_to_fhirVisitDetailsFragment, bundle)
                return
            } catch (e: Exception) {
                Timber.e(e, "NavController navigate to Visit Details failed")
                Toast.makeText(requireContext(), "Nav Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        try {
            // Try NavController if available
            Timber.d("Navigating Up")
            findNavController().navigateUp()
        } catch (e: Exception) {
            Timber.e(e, "NavController navigateUp failed, falling back to activity back press")
            // Fallback to activity back press
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    private fun showAntraIncentiveAlert() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.incentive_alert_title))
            .setMessage(getString(R.string.antra_incentive_alert))
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                viewModel.resetAlert()

                if (viewModel.state.value == EligibleCoupleTrackingFormViewModel.State.SAVE_SUCCESS) {
                    Toast.makeText(requireContext(), resources.getString(R.string.tracking_form_filled_successfully), Toast.LENGTH_SHORT).show()
                    saveNurseDataInBackground()
                    navigateBackToList()
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun showSterilizationIncentiveAlert() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.incentive_alert_title))
            .setMessage(getString(R.string.sterilization_incentive_alert))
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                viewModel.resetAlert()

                if (viewModel.state.value == EligibleCoupleTrackingFormViewModel.State.SAVE_SUCCESS) {
                    Toast.makeText(requireContext(), resources.getString(R.string.tracking_form_filled_successfully), Toast.LENGTH_SHORT).show()
                    saveNurseDataInBackground()
                    navigateBackToList()
                }
            }
            .setCancelable(false)
            .show()
    }


    private fun saveNurseDataInBackground(){
        val currentBenVisitInfo = benVisitInfo
        if (currentBenVisitInfo == null) {
            Timber.e("benVisitInfo is null, cannot save nurse data")
            return
        }

        lifecycleScope.launch {
            try {
                var benVisitNo = 0
                var createNewBenflow = false
                CPHCviewModel.getLastVisitInfoSync(currentBenVisitInfo.patient.patientID).let {
                    if(it == null){
                        benVisitNo = 1
                    }
                    else if(it.nurseFlag == 1) {
                        benVisitNo = it.benVisitNo
                    }
                    else {
                        benVisitNo = it.benVisitNo + 1
                        createNewBenflow = true
                    }
                }

                val user = userRepo.getLoggedInUser()
                saveNurseData(benVisitNo, createNewBenflow, user, currentBenVisitInfo)

                WorkerUtils.triggerAmritSyncWorker(requireContext())
            } catch (e: Exception) {
                Timber.e(e, "Failed to save nurse data in background")
            }
        }
    }

    private fun saveNurseData(benVisitNo: Int, createNewBenflow: Boolean, user: UserDomain?, benVisitInfo: PatientDisplayWithVisitInfo){

        val visitDB = VisitDB(
            visitId = generateUuid(),
            category = "FP & Contraceptive Services",
            reasonForVisit = "New Chief Complaint",
            subCategory = "FP & Contraceptive Services",
            patientID = benVisitInfo.patient.patientID,
            benVisitNo = benVisitNo,
            benVisitDate =  SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()),
            createdBy = user?.userName
        )

        val patientVitals = PatientVitalsModel(
            patientID = benVisitInfo.patient.patientID,
            benVisitNo = benVisitNo,
        )

        val patientVisitInfoSync = PatientVisitInfoSync(
            patientID = benVisitInfo.patient.patientID,
            benVisitNo = benVisitNo,
            createNewBenFlow = createNewBenflow,
            nurseDataSynced = SyncState.SYNCED,
            doctorDataSynced = SyncState.SYNCED,
            nurseFlag = 9,
            doctorFlag = 1,
            visitCategory = "FP & Contraceptive Services"
        )

        CPHCviewModel.saveNurseDataToDb(visitDB, patientVitals, patientVisitInfoSync)
    }

    private fun submitEligibleTrackingForm() {
        if (validateCurrentPage()) {
            viewModel.saveForm()
        }
    }

    private fun validateCurrentPage(): Boolean {
        val result = binding.form.rvInputForm.adapter?.let {
            (it as FormInputAdapter).validateInput(resources)
        }
        Timber.d("Validation : $result")
        return if (result == -1) true
        else {
            if (result != null) {
                binding.form.rvInputForm.scrollToPosition(result)
            }
            false
        }
    }

    private fun hardCodedListUpdate(formId: Int) {
        binding.form.rvInputForm.adapter?.apply {
            when (formId) {
                1 -> {
                    notifyItemChanged(1)
                    notifyItemChanged(2)
                }
                4, 5, 9, 12 -> {
                    notifyDataSetChanged()
                    if (formId == 9) {
                        lifecycleScope.launch {
                            yield()
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun getFragmentId(): Int {
        return R.id.fragment_fp
    }

    override fun onSubmitAction() {
        navigateNext()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE
        _binding = null
    }

    override fun onCancelAction() {
        navigateBackToList()
    }

    override fun onResume() {
        super.onResume()
        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title =
            getString(R.string.ec_tracking_title)
    }

    private fun populateAntraTable(ectRecords: List<EligibleCoupleTrackingCache>?) {
        val antraRecords = ectRecords?.filter {
            it.methodOfContraception == "ANTRA Injection" && it.antraInjectionDate != null
        }?.sortedBy { it.antraInjectionDate } ?: emptyList()

        if (antraRecords.isEmpty()) {
            binding.llAntraSection.visibility = View.GONE
            return
        }
        binding.llAntraSection.visibility = View.VISIBLE
        val tableLayout = binding.tableAntraHistory
        val childCount = tableLayout.childCount
        if (childCount > 1) {
            tableLayout.removeViews(1, childCount - 1)
        }
        antraRecords.forEach { record ->
            val tableRow = android.widget.TableRow(requireContext())
            tableRow.layoutParams = android.widget.TableLayout.LayoutParams(
                android.widget.TableLayout.LayoutParams.MATCH_PARENT,
                android.widget.TableLayout.LayoutParams.WRAP_CONTENT
            )
            tableRow.setPadding(8, 8, 8, 8)
            val doseTextView = android.widget.TextView(requireContext())
            doseTextView.text = record.antraDose ?: ""
            doseTextView.setPadding(8, 8, 8, 8)
            doseTextView.gravity = android.view.Gravity.CENTER
            doseTextView.layoutParams = android.widget.TableRow.LayoutParams(
                0,
                android.widget.TableRow.LayoutParams.WRAP_CONTENT,
                1f
            )
            tableRow.addView(doseTextView)
            val dateTextView = android.widget.TextView(requireContext())
            dateTextView.text = record.antraInjectionDate?.let {
                SimpleDateFormat(DATE_FORMAT_DD_MM_YYYY, Locale.getDefault()).format(Date(it))
            } ?: ""
            dateTextView.setPadding(8, 8, 8, 8)
            dateTextView.gravity = android.view.Gravity.CENTER
            dateTextView.layoutParams = android.widget.TableRow.LayoutParams(
                0,
                android.widget.TableRow.LayoutParams.WRAP_CONTENT,
                1f
            )
            tableRow.addView(dateTextView)
            val dueDateTextView = android.widget.TextView(requireContext())
            dueDateTextView.text = record.antraInjectionDate?.let { injectionDate ->
                val cal = java.util.Calendar.getInstance()
                cal.timeInMillis = injectionDate

                cal.add(java.util.Calendar.DAY_OF_YEAR, 76)
                val startDate = SimpleDateFormat(DATE_FORMAT_DD_MM_YYYY, Locale.getDefault()).format(cal.time)

                cal.timeInMillis = injectionDate
                cal.add(java.util.Calendar.DAY_OF_YEAR, 120)
                val endDate = SimpleDateFormat(DATE_FORMAT_DD_MM_YYYY, Locale.getDefault()).format(cal.time)

                "$startDate to $endDate"
            } ?: ""
            dueDateTextView.setPadding(8, 8, 8, 8)
            dueDateTextView.gravity = android.view.Gravity.CENTER
            dueDateTextView.layoutParams = android.widget.TableRow.LayoutParams(
                0,
                android.widget.TableRow.LayoutParams.WRAP_CONTENT,
                1f
            )
            tableRow.addView(dueDateTextView)

            tableLayout.addView(tableRow)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}