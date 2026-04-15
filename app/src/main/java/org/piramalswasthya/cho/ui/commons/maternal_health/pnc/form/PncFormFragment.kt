package org.piramalswasthya.cho.ui.commons.maternal_health.pnc.form


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.FormInputAdapter
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.databinding.FragmentNewFormBinding
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.UserDomain
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.OtherCPHCServicesViewModel

import org.piramalswasthya.cho.ui.commons.maternal_health.pnc.form.PncFormViewModel.State
import org.piramalswasthya.cho.utils.generateUuid
import org.piramalswasthya.cho.work.WorkerUtils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class PncFormFragment() : Fragment(), NavigationAdapter{

    private var _binding: FragmentNewFormBinding? = null
    private val binding: FragmentNewFormBinding
        get() = _binding!!

    @Inject
    lateinit var userRepo: UserRepo

    val viewModel: PncFormViewModel by viewModels()

    private var benVisitInfo: PatientDisplayWithVisitInfo? = null

    val CPHCviewModel: OtherCPHCServicesViewModel by viewModels()

    var fragmentContainerId: Int = 0

    val fragment: Fragment = this

    val jsonFile: String = "patient-visit-details-paginated.json"
    fun navigateNext() {
        submitAncForm()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val pallorFormId = 22
    private val severePallorIndex = 3
    private val vaginalBleedingFormId = 23
    private val heavyBleedingIndex = 1
    private val foulSmellingDischargeIndex = 2
    private val maternalSymptomsFormId = 20

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        benVisitInfo = requireActivity().intent?.getSerializableExtra("benVisitInfo") as? PatientDisplayWithVisitInfo

        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->
            notIt?.let { recordExists ->
                binding.fabEdit.visibility = /*if (recordExists) View.VISIBLE else */View.GONE
                if(recordExists){
                    val btnSubmit = activity?.findViewById<Button>(R.id.btnSubmit)
                    btnSubmit?.visibility = View.GONE
                }
//                binding.btnSubmit.visibility = if (recordExists) View.GONE else View.VISIBLE
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                        hardCodedListUpdate(formId)
                        if (formId == pallorFormId && index == severePallorIndex) {
                            showReferralAlertToFacility(R.string.pnc_referral_alert_severe_pallor)
                        }
                        if (formId == vaginalBleedingFormId &&
                            (index == heavyBleedingIndex || index == foulSmellingDischargeIndex)
                        ) {
                            showReferralAlertToFacility(R.string.pnc_referral_alert_vaginal_bleeding)
                        }
                        if (formId == maternalSymptomsFormId) {
                            val pncAdapter = binding.form.rvInputForm.adapter as? FormInputAdapter
                            val rowPosition = pncAdapter?.currentList?.indexOfFirst { it.id == maternalSymptomsFormId } ?: -1
                            if (rowPosition >= 0) {
                                pncAdapter?.notifyItemChanged(rowPosition)
                            }

                            // Count actual symptoms selected (exclude "None") for referral alert
                            val maternalSymptomsItem = pncAdapter?.currentList?.find { it.id == maternalSymptomsFormId }
                            val currentValue = maternalSymptomsItem?.value
                            val selectedSymptoms = currentValue
                                ?.split(",")
                                ?.map { it.trim() }
                                ?.filter { it.isNotBlank() && !it.equals("None", ignoreCase = true) }
                                ?: emptyList()
                            if (selectedSymptoms.size >= 2) {
                                showReferralAlertToFacility(R.string.pnc_referral_alert_multiple_symptoms)
                            }
                        }

                        if (formId == pallorFormId || formId == vaginalBleedingFormId || formId == maternalSymptomsFormId || formId == 19 /* anyDangerSign */) {
                            val pncAdapter = binding.form.rvInputForm.adapter as? FormInputAdapter
                            val referralIndex = pncAdapter?.currentList?.indexOfFirst { it.id == 9 /* referralFacility */ } ?: -1
                            if (referralIndex >= 0) {
                                pncAdapter?.notifyItemChanged(referralIndex)
                            }
                        }
                    }, isEnabled = !recordExists
                )
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
//        binding.btnSubmit.setOnClickListener {
//            submitAncForm()
//        }
        binding.fabEdit.setOnClickListener {
            viewModel.setRecordExist(false)
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                State.IDLE -> {
                }

                State.SAVING -> {
                    binding.llContent.visibility = View.GONE
                    binding.pbForm.visibility = View.VISIBLE
                }

                State.SAVE_SUCCESS -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(context, getString(R.string.save_successful_toast), Toast.LENGTH_LONG).show()
                    WorkerUtils.triggerPncSync(requireContext())
                    
                    // Finish activity to return to PNC list
                    requireActivity().finish()
                }

                State.SAVE_FAILED -> {
                    Toast.makeText(
                        context, "Something wend wong! Contact testing!", Toast.LENGTH_LONG
                    ).show()
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                }
            }
        }
    }

    private fun saveNurseData(){
        CoroutineScope(Dispatchers.Main).launch {
            var benVisitNo = 0;
            var createNewBenflow = false;
            benVisitInfo?.let { visitInfo ->
                CPHCviewModel.getLastVisitInfoSync(visitInfo.patient.patientID).let {
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
            }

            val user = userRepo.getLoggedInUser()

            saveNurseData(benVisitNo, createNewBenflow, user)

            CPHCviewModel.isDataSaved.observe(viewLifecycleOwner){
                when(it!!){
                    true ->{
                        WorkerUtils.triggerAmritSyncWorker(requireContext())
                        requireActivity().finish()
                    }
                    else ->{

                    }
                }
            }

        }
    }

    fun saveNurseData(benVisitNo: Int, createNewBenflow: Boolean, user: UserDomain?){
        val visitDB = VisitDB(
            visitId = generateUuid(),
            category = "PNC",
            reasonForVisit = "New Chief Complaint",
            subCategory = "PNC",
            patientID = benVisitInfo?.patient?.patientID ?: "",
            benVisitNo = benVisitNo,
            benVisitDate =  SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()),
            createdBy = user?.userName
        )

        val patientVitals = PatientVitalsModel(
            patientID = benVisitInfo?.patient?.patientID ?: "",
            benVisitNo = benVisitNo,
        )

        val patientVisitInfoSync = PatientVisitInfoSync(
            patientID = benVisitInfo?.patient?.patientID ?: "",
            benVisitNo = benVisitNo,
            createNewBenFlow = false,
            nurseDataSynced = SyncState.SYNCED,
            doctorDataSynced = SyncState.SYNCED,
            nurseFlag = 9,
            doctorFlag = 1,
            visitCategory = "PNC"
        )

        CPHCviewModel.saveNurseDataToDb(visitDB, patientVitals, patientVisitInfoSync)

    }

    private fun submitAncForm() {
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

                1 -> notifyItemChanged(1)

            }
        }
    }
    override fun onStart() {
        super.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun getFragmentId(): Int {
        return R.id.fragment_pnc
    }

    override fun onSubmitAction() {
        navigateNext()
    }

    override fun onCancelAction() {
        requireActivity().finish()
    }

    private fun showReferralAlertToFacility(@StringRes messageResId: Int) {
        if (!isAdded || context == null) return
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.alert_title))
            .setMessage(getString(messageResId))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

}
