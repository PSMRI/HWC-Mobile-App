package org.piramalswasthya.cho.ui.commons.eligible_couple.tracking.form

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.adapter.FormInputAdapter
import org.piramalswasthya.cho.databinding.FragmentNewFormBinding
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
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
import javax.inject.Inject

@AndroidEntryPoint
class EligibleCoupleTrackingFormFragment : Fragment(), NavigationAdapter {

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

        // Load benVisitInfo from patientID - try intent first, then load from DB
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

        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->
            notIt?.let { recordExists ->
//                binding.fabEdit.visibility = if(recordExists) View.VISIBLE else View.GONE
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                        hardCodedListUpdate(formId)
                    }, isEnabled = !recordExists
                )
                // Hide submit button if record exists (view mode)
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

        viewModel.benName.observe(viewLifecycleOwner) {
            binding.tvBenName.text = it
        }
        viewModel.benAgeGender.observe(viewLifecycleOwner) {
            binding.tvAgeGender.text = it
        }

        // Wire up submit button from fragment layout
        binding.btnSubmit.setOnClickListener {
            submitEligibleTrackingForm()
        }

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
            // Save nurse data in background, but navigate back immediately
            // ECT data is already saved, so visit history will show it
            saveNurseDataInBackground()
            // Navigate back to eligible couple tracking list
            viewModel.resetState()
            navigateBackToList()
        }
        // Alerts will be handled by the observer
    }

    private fun navigateToPregnancyRegistration() {
        try {
            // Navigate to MaternalHealthNavHostFragment with arguments to open PWR form
            val fragment = org.piramalswasthya.cho.ui.home.rmncha.maternal_health.MaternalHealthNavHostFragment().apply {
                arguments = Bundle().apply {
                    putInt("destinationId", R.id.pregnancyRegistrationFormFragment)
                    putString("patientID", viewModel.patientID)
                    putBoolean("fromECT", true)
                }
            }
            
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
                
        } catch (e: Exception) {
            Timber.e(e, "Failed to navigate to Pregnancy Registration form")
            navigateBackToList()
        }
    }

    private fun navigateBackToList() {
        if (!isAdded || isRemoving) return
        
        try {
            // Try standard navigation first
            androidx.navigation.fragment.NavHostFragment.findNavController(this).navigateUp()
        } catch (e: IllegalStateException) {
            Timber.e(e, "NavController not found for ECT form, falling back to activity back press")
            try {
                // Fallback to manual back press if NavController is missing
                activity?.onBackPressedDispatcher?.onBackPressed()
            } catch (inner: Exception) {
                Timber.e(inner, "Critical failure during manual back press fallback")
            }
        } catch (e: Exception) {
            Timber.e(e, "Generic failure during navigateBackToList")
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
                
                // Only navigate if form was actually submitted and saved (post-save alert)
                if (viewModel.state.value == EligibleCoupleTrackingFormViewModel.State.SAVE_SUCCESS) {
                    // Show success message
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.tracking_form_filled_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                    // Save nurse data in background, but navigate back immediately
                    saveNurseDataInBackground()
                    navigateBackToList()
                }
                // Else (immediate alert on selection): just dismiss and reset, staying on form
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
                
                // Only navigate if form was actually submitted and saved (post-save alert)
                if (viewModel.state.value == EligibleCoupleTrackingFormViewModel.State.SAVE_SUCCESS) {
                    // Show success message
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.tracking_form_filled_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                    // Save nurse data in background, but navigate back immediately
                    saveNurseDataInBackground()
                    navigateBackToList()
                }
                // Else (immediate alert on selection): just dismiss and reset, staying on form
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
                var benVisitNo = 0;
                var createNewBenflow = false;
                CPHCviewModel.getLastVisitInfoSync(currentBenVisitInfo.patient.patientID).let {
                    if(it == null){
                        benVisitNo = 1;
                    }
                    else if(it.nurseFlag == 1) {
                        benVisitNo = it.benVisitNo
                    }
                    else {
                        benVisitNo = it.benVisitNo + 1
                        createNewBenflow = true;
                    }
                }

                val user = userRepo.getLoggedInUser()
                saveNurseData(benVisitNo, createNewBenflow, user, currentBenVisitInfo)
                
                // Trigger sync worker
                WorkerUtils.triggerAmritSyncWorker(requireContext())
            } catch (e: Exception) {
                Timber.e(e, "Failed to save nurse data in background")
            }
        }
    }

    fun saveNurseData(benVisitNo: Int, createNewBenflow: Boolean, user: UserDomain?, benVisitInfo: PatientDisplayWithVisitInfo){

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
            createNewBenFlow = false,
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
                4,5 -> {
                    notifyDataSetChanged()
                    //notifyItemChanged(viewModel.getIndexOfIsPregnant())
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

    override fun onCancelAction() {
        findNavController().navigateUp()
    }


    override fun onResume() {
        super.onResume()
        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title =
            getString(R.string.ec_tracking_title)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}