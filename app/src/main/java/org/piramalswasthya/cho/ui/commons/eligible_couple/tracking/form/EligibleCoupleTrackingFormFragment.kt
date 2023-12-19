package org.piramalswasthya.cho.ui.commons.eligible_couple.tracking.form

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

    private lateinit var benVisitInfo: PatientDisplayWithVisitInfo

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

        benVisitInfo = requireActivity().intent?.getSerializableExtra("benVisitInfo") as PatientDisplayWithVisitInfo

        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->
            notIt?.let { recordExists ->
//                binding.fabEdit.visibility = if(recordExists) View.VISIBLE else View.GONE
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                        hardCodedListUpdate(formId)
                    }, isEnabled = !recordExists
                )
                if(recordExists){
                    val btnSubmit = activity?.findViewById<Button>(R.id.btnSubmit)
                    btnSubmit?.visibility = View.GONE
                }
//                binding.btnSubmit.isEnabled = !recordExists
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
//            submitEligibleTrackingForm()
//        }

        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                EligibleCoupleTrackingFormViewModel.State.SAVE_SUCCESS -> {
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.tracking_form_filled_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                    saveNurseData()
//                    navigateToNextScreen()
//                    saveNurseData()
                }

                else -> {}
            }
        }
    }

    private fun navigateToNextScreen() {
        if (viewModel.isPregnant) {
//            findNavController().navigate(
//                EligibleCoupleTrackingFormFragmentDirections.actionEligibleCoupleTrackingFormFragmentToPregnancyRegistrationFormFragment(
//                    benId = viewModel.benId
//                )
//            )
            viewModel.resetState()
        } else {
            findNavController().navigateUp()
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.tracking_form_filled_successfully),
                Toast.LENGTH_SHORT
            ).show()
            viewModel.resetState()
        }
    }

    private fun saveNurseData(){
        CoroutineScope(Dispatchers.Main).launch {
            var benVisitNo = 0;
            var createNewBenflow = false;
            CPHCviewModel.getLastVisitInfoSync(benVisitInfo.patient.patientID).let {
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

            saveNurseData(benVisitNo, createNewBenflow, user)

            CPHCviewModel.isDataSaved.observe(viewLifecycleOwner){
                when(it!!){
                    true ->{
                        WorkerUtils.triggerAmritSyncWorker(requireContext())
                        val intent = Intent(context, HomeActivity::class.java)
                        startActivity(intent)
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
//        activity?.let {
//            (it as HomeActivity).updateActionBar(
//                R.drawable.ic__eligible_couple,
//                getString(R.string.eligible_couple_tracking_form)
//            )
//        }
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


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}