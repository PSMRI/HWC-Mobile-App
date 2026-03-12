package org.piramalswasthya.cho.ui.commons.pharmacist



import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.PharmacistItemAdapter
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.AlertAbhaCcMappingBinding
import org.piramalswasthya.cho.databinding.AlertCcMappingBinding
import org.piramalswasthya.cho.databinding.FragmentPharmacistFormBinding
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PrescriptionDTO
import org.piramalswasthya.cho.model.PrescriptionItemDTO
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity
import org.piramalswasthya.cho.ui.home.HomeViewModel
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.ui.register_patient_activity.RegisterPatientActivity
import org.piramalswasthya.cho.work.WorkerUtils
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class PharmacistFormFragment : Fragment(R.layout.fragment_pharmacist_form), NavigationAdapter {

    private var _binding: FragmentPharmacistFormBinding? = null

    private val binding: FragmentPharmacistFormBinding
        get() {
            return _binding!!
        }

    var fragment: Fragment = this;
    @Inject
    lateinit var preferenceDao: PreferenceDao
    private var userInfo: UserCache? = null
    private lateinit var navHostFragment: PrescriptionItemDTO

    lateinit var viewModel: PharmacistFormViewModel

    private lateinit var ccMappingAlertBinding: AlertCcMappingBinding

    private lateinit var ccMappingAlertAbhaBinding: AlertAbhaCcMappingBinding

    private var dtos: PrescriptionDTO? = null

    private var itemAdapter : PharmacistItemAdapter? = null

    private lateinit var benVisitInfo : PatientDisplayWithVisitInfo
    private var patientCount : Int = 0

    private var visitCode = 0L

    private val args: PharmacistFormFragmentArgs by lazy {
        PharmacistFormFragmentArgs.fromBundle(requireArguments())
    }

    private val bundle = Bundle()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Create the ComposeView
        _binding = FragmentPharmacistFormBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        benVisitInfo = requireActivity().intent?.getSerializableExtra("benVisitInfo") as PatientDisplayWithVisitInfo
        dtos?.issueType = "System Issue"
        viewModel = ViewModelProvider(this).get(PharmacistFormViewModel::class.java)
        viewModel.prescriptionObserver.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                PharmacistFormViewModel.NetworkState.SUCCESS -> {
                    itemAdapter = context?.let { it ->
                        PharmacistItemAdapter(
                            it,
                            dtos?.issueType ?: "",
                            networkAvailabilityCheck = { viewModel.isNetworkAvailable() },
                            clickListener = PharmacistItemAdapter.PharmacistClickListener(clickedSelectBatch = { prescription ->
                                val bundle = Bundle()
                                bundle.putString("prescriptionItemDTO", Gson().toJson(prescription))

                                // Use real-time batch fetching instead of relying on existing batchList
                                lifecycleScope.launch {
                                    try {
                                        // Check network availability before proceeding
                                        if (!viewModel.isNetworkAvailable()) {
                                            Toast.makeText(requireContext(),
                                                getString(R.string.network_required_manual_batch),
                                                Toast.LENGTH_LONG).show()
                                            return@launch
                                        }

                                        val availableBatches = viewModel.getBatchesForMedicine(prescription.drugID)

                                        if (availableBatches.isNotEmpty()) {
                                            bundle.putString("batchList", Gson().toJson(availableBatches))
                                            bundle.putString("prescriptionDTO", Gson().toJson(dtos))
                                            bundle.putSerializable("benVisitInfo", benVisitInfo)

                                            findNavController().navigate(
                                                R.id.action_pharmacistFormFragment_to_selectBatchFragment, bundle
                                            )
                                        } else {
                                            // Show retry dialog instead of just error message
                                            Toast.makeText(requireContext(), getString(R.string.no_batches_available), Toast.LENGTH_LONG).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(requireContext(), "Error loading batch data. Please try again.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                                clickedViewBatch = { prescription ->
                                    // View Batch functionality is commented out for System Issue mode
                                    // This was the original implementation for System Issue
                                    /*
                                    val bundle = Bundle()
                                    bundle.putString("prescriptionItemDTO", Gson().toJson(prescription))
                                    if(prescription.batchList!= null && prescription.batchList.isNotEmpty()){
                                        bundle.putString("batchList", Gson().toJson(prescription.batchList?.get(0)))

                                        bundle.putString("prescriptionDTO", Gson().toJson(dtos))
                                        val batchFragment = PrescriptionBatchFormFragment()
                                        batchFragment.arguments = bundle
                                        findNavController().navigate(
                                            R.id.action_pharmacistFormFragment_to_prescriptionBatchFormFragment, bundle
                                        )
                                    }
                                    else{
                                        Toast.makeText(requireContext(), getString(R.string.medicine_not_available), Toast.LENGTH_SHORT).show()
                                    }
                                    */

                                    // For now, just show a message that this feature is disabled
                                    Toast.makeText(requireContext(),
                                        "View Batch is disabled in System Issue mode",
                                        Toast.LENGTH_SHORT).show()
                                }
                            )
                        )
                    }
                    binding.selectProgram.setOnCheckedChangeListener { _, programId ->
                        when (programId){
                            binding.btnManualIssue.id -> {
                                dtos?.issueType = "Manual Issue"
                                itemAdapter?.updateIssueType("Manual Issue")
                                // Check network availability and show message if needed
                                if (!viewModel.isNetworkAvailable()) {
                                    Toast.makeText(requireContext(),
                                        getString(R.string.network_required_manual_batch) + ". Please connect to internet.",
                                        Toast.LENGTH_LONG).show()
                                }
                            }
                            binding.btnSystemIssue.id -> {
                                dtos?.issueType = "System Issue"
                                itemAdapter?.updateIssueType("System Issue")
                                Toast.makeText(requireContext(),
                                    getString(R.string.system_issue_auto_allocation),
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }


                    binding.pharmacistListContainer.pharmacistList.adapter = itemAdapter
                    lifecycleScope.launch {
                        viewModel.downloadPrescription(benVisitInfo = benVisitInfo)
                        viewModel.getPrescription(benVisitInfo = benVisitInfo)
                        dtos?.let { viewModel.getAllocationItemForPharmacist(it) }
                    }

                    viewModel.prescriptions.observe(viewLifecycleOwner) {
                        dtos = viewModel.prescriptions?.value
                        viewModel.prescriptions?.value?.let {it->
                            binding.consultantValue.text = it.consultantName
                            binding.visitCodeValue.text = it.visitCode.toString()
                            binding.prescriptionIdValue.text = it.prescriptionID.toString()

                            visitCode = it.visitCode

                            it.itemList.let { it ->
                                itemAdapter?.submitList(it)
                                binding.pharmacistListContainer.prescriptionCount.text =
                                    itemAdapter?.itemCount.toString() + getResultStr(itemAdapter?.itemCount)
                                if (it != null) {
                                    patientCount = it.size
                                }
                            }
                        }
                    }

                }

                else -> {

                }
            }
//        }
        }

    }

    fun getResultStr(count:Int?):String{
        if(count==1||count==0){
            return " Prescription"
        }
        return " Prescriptions"
    }

    override fun getFragmentId(): Int {
        return R.id.fragment_pharmacist_form;
    }

    override fun onSubmitAction() {
        Timber.d("submit button", dtos)
        if (!isManualIssueReadyForSubmit(dtos)) {
            Toast.makeText(requireContext(), getString(R.string.no_batch_selected), Toast.LENGTH_SHORT).show()
            return
        }
        activity?.findViewById<View>(R.id.btnSubmit)?.isEnabled = false
        viewModel.isDataSaved.removeObservers(viewLifecycleOwner)
        viewModel.savePharmacistData(dtos, benVisitInfo)
        viewModel.isDataSaved.observe(viewLifecycleOwner){ state ->
            when (state!!) {
                true -> {
                    viewModel.isDataSaved.removeObservers(viewLifecycleOwner)
                    careContextPrompt.show()
//                    navigateNext()
                }
                else -> {
                    activity?.findViewById<View>(R.id.btnSubmit)?.isEnabled = true
                }
            }
        }
    }

    override fun onCancelAction() {
        val intent = Intent(context, HomeActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun isManualIssueReadyForSubmit(dtos: PrescriptionDTO?): Boolean {
        if (dtos?.issueType != "Manual Issue") return true
        val items = dtos.itemList ?: return false
        return items.all { item ->
            val batches = item.batchList
            if (batches.isNullOrEmpty()) return@all false
            batches.any { it.isSelected && it.dispenseQuantity > 0 }
        }
    }

    private val careContextPrompt by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.info))
            .setMessage(getString(R.string.want_care_context))
            .setPositiveButton("Yes") { dialog, _ ->
//                dialog.dismiss()
                viewModel.getBenHealthId(visitCode, benVisitInfo.patient.beneficiaryID, benVisitInfo.patient.beneficiaryRegID)

                viewModel.isBenHealthInfoFetched.observe(viewLifecycleOwner) { state ->
                    dialog.dismiss()
                    when(state!!) {
                        true -> {
                            filterAbhaCcMapping
                            ccMappingAlertBinding.btnOk.visibility = View.GONE
                            ccMappingAlertBinding.tvNrf.visibility = View.GONE
                            ccMappingAlertBinding.btnGenerate.visibility = View.VISIBLE
                            ccMappingAlertBinding.btnCancel.visibility = View.VISIBLE
                            ccMappingAlertBinding.abhaBox.visibility = View.VISIBLE

                            ccMappingAlertBinding.tvvAbhaNumber.text = viewModel.benHealthInfo?.healthIdNumber
                            ccMappingAlertBinding.tvvAbha.text = viewModel.benHealthInfo?.healthId
                            ccMappingAlertBinding.tvvAbhaCreated.text = viewModel.benHealthInfo?.createdDate

                            filterAbhaCcMapping.show()

                        }
                        else -> {
                            filterAbhaCcMapping
                            ccMappingAlertBinding.tvNrf.visibility = View.VISIBLE
                            filterAbhaCcMapping.show()
//                    Toast.makeText(requireContext(), "There was some error in fetching beneficiary health data, please try again later", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }
            .setNegativeButton("No") {dialog, _->
                dialog.dismiss()
                navigateNext()
            }
            .setCancelable(false)
            .create()
    }

    private val cancelCareContextPrompt by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.info))
            .setMessage(getString(R.string.cancel_care_context))
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("No") {dialog, _->
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()
    }

    private val otpSentDialog by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.success))
            .setMessage(getString(R.string.otp_sent))
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
                val abhaDialog = abhaCcMapping
                abhaDialog.show()
            }
            .setCancelable(false)
            .create()
    }

    private val otpVerifiedDialog by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.success))
            .setMessage(getString(R.string.otp_sent))
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
                navigateNext()
            }
            .setCancelable(false)
            .create()
    }

    private val filterAbhaCcMapping by lazy {
        ccMappingAlertBinding = AlertCcMappingBinding.inflate(layoutInflater, binding.root, false)

        val alert = MaterialAlertDialogBuilder(requireContext()).setView(ccMappingAlertBinding.root)
            .setCancelable(false)
            .create()

        ccMappingAlertBinding.btnOk.setOnClickListener {
            alert.dismiss()
            navigateNext()
        }
        ccMappingAlertBinding.btnCancel.setOnClickListener {
            alert.dismiss()
            navigateNext()
        }
        ccMappingAlertBinding.btnGenerate.setOnClickListener {
            ccMappingAlertBinding.btnGenerate.isEnabled = false
            viewModel.generateOTPForCareContext()

            viewModel.isOtpGenerated.observe(viewLifecycleOwner) { state ->
                ccMappingAlertBinding.btnGenerate.isEnabled = true
                alert.dismiss()
                Toast.makeText(requireContext(), getString(R.string.processing), Toast.LENGTH_SHORT).show()
                when(state!!) {
                    true -> {
                        otpSentDialog.show()
                    }
                    else -> {
//                        Toast.makeText(requireContext(), "There was some error in sending OTP, please try again later", Toast.LENGTH_LONG).show()
//                        navigateNext()
                    }
                }
            }

        }

        alert
    }

    private val abhaCcMapping by lazy {
        ccMappingAlertAbhaBinding = AlertAbhaCcMappingBinding.inflate(layoutInflater, binding.root, false)

        val alert = MaterialAlertDialogBuilder(requireContext()).setView(ccMappingAlertAbhaBinding.root)
            .setCancelable(false)
            .create()

        ccMappingAlertAbhaBinding.btnCancel.setOnClickListener {
            alert.dismiss()
            navigateNext()
        }

        ccMappingAlertAbhaBinding.btnSubmit.setOnClickListener {
            if (ccMappingAlertAbhaBinding.ettOtp.text.isNullOrEmpty()) {
                ccMappingAlertAbhaBinding.etOtp.error = "Please Enter OTP"
            } else {
                viewModel.validateOTPAndCreateCareContext(
                    ccMappingAlertAbhaBinding.ettOtp.text.toString(),
                    benVisitInfo.patient.beneficiaryID!!,
                    visitCode,
                    benVisitInfo.visitCategory!!
                )

                viewModel.isOtpVerified.observe(viewLifecycleOwner) { state ->
                    alert.dismiss()
                    Toast.makeText(requireContext(), getString(R.string.processing), Toast.LENGTH_SHORT).show()
                    when(state!!) {
                        true -> {
                            otpVerifiedDialog
                            otpVerifiedDialog.setMessage(viewModel.response2?.response)
                            otpVerifiedDialog.show()
                        }
                        else -> {
//                            Toast.makeText(requireContext(), "There was some error in verifying OTP, please try again later", Toast.LENGTH_LONG).show()
//                        navigateNext()
                        }
                    }
                }

            }
        }

        alert
    }

    fun navigateNext() {
        val intent = Intent(context, HomeActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

}
