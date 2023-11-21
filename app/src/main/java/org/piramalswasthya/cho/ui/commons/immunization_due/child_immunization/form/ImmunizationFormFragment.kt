package org.piramalswasthya.cho.ui.commons.immunization_due.child_immunization.form

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
import org.piramalswasthya.cho.work.WorkerUtils
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
import org.piramalswasthya.cho.ui.commons.immunization_due.child_immunization.form.ImmunizationFormViewModel.State
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.utils.generateUuid
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class ImmunizationFormFragment : Fragment(), NavigationAdapter{
    private var _binding: FragmentNewFormBinding? = null

    private val binding: FragmentNewFormBinding
        get() = _binding!!

    @Inject
    lateinit var userRepo: UserRepo

    val viewModel: ImmunizationFormViewModel by viewModels()

    private lateinit var benVisitInfo: PatientDisplayWithVisitInfo

    val CPHCviewModel: OtherCPHCServicesViewModel by viewModels()

    var fragmentContainerId: Int = 0

    val fragment: Fragment = this

    val jsonFile: String = "patient-visit-details-paginated.json"
    fun navigateNext() {
        submitImmForm()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.btnNext.visibility = View.INVISIBLE
//        binding.btnPrev.visibility = View.INVISIBLE
//        binding.btnSubmitForm.visibility = View.VISIBLE
        benVisitInfo = requireActivity().intent?.getSerializableExtra("benVisitInfo") as PatientDisplayWithVisitInfo

        viewModel.benName.observe(viewLifecycleOwner) {
            binding.tvBenName.text = it
        }
        viewModel.benAgeGender.observe(viewLifecycleOwner) {
            binding.tvAgeGender.text = it
        }
//        binding.btnSubmit.setOnClickListener {
//            submitImmForm()
//        }

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
                    Toast.makeText(context, resources.getString(R.string.save_successful), Toast.LENGTH_LONG).show()
                    saveNurseData()
                }

                State.SAVE_FAILED -> {
                    Toast.makeText(
                        context, resources.getString(R.string.something_wend_wong_contact_testing), Toast.LENGTH_LONG
                    ).show()
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                }
            }
        }

        binding.fabEdit.setOnClickListener {
            viewModel.updateRecordExists(false)
        }

        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->
            notIt?.let { recordExists ->
                if(recordExists){
                    val btnSubmit = activity?.findViewById<Button>(R.id.btnSubmit)
                    btnSubmit?.visibility = View.GONE
                }
//                binding.btnSubmit.visibility = if (recordExists) View.GONE else View.VISIBLE
                binding.fabEdit.visibility = if (recordExists) View.VISIBLE else View.GONE
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                    },
                    isEnabled = !recordExists
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
            category = "Neonatal and Infant Health Care Services",
            reasonForVisit = "New Chief Complaint",
            subCategory = "Neonatal and Infant Health Care Services",
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
            nurseDataSynced = SyncState.UNSYNCED,
            doctorDataSynced = SyncState.SYNCED,
            nurseFlag = 9,
            doctorFlag = 1
        )

        CPHCviewModel.saveNurseDataToDb(visitDB, patientVitals, patientVisitInfoSync)

    }

    private fun submitImmForm() {
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

    override fun onStart() {
        super.onStart()
//        activity?.let {
//            (it as HomeActivity).updateActionBar(R.drawable.ic__immunization, getString(R.string.immunization))
//        }
    }


    override fun getFragmentId(): Int {
        return R.id.fragment_immunization
    }

    override fun onSubmitAction() {
        navigateNext()
    }

    override fun onCancelAction() {
        findNavController().navigateUp()
    }


}