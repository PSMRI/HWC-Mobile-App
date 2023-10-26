package org.piramalswasthya.cho.ui.commons.pharmacist



import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.PharmacistItemAdapter
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentPharmacistFormBinding
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PrescriptionDTO
import org.piramalswasthya.cho.model.PrescriptionItemDTO
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.ui.commons.FhirFragmentService
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class PharmacistFormFragment : Fragment(R.layout.fragment_pharmacist_form), FhirFragmentService, NavigationAdapter {

    private var _binding: FragmentPharmacistFormBinding? = null

    private val binding: FragmentPharmacistFormBinding
        get() {
            return _binding!!
        }

    override var fragment: Fragment = this;
    @Inject
    lateinit var preferenceDao: PreferenceDao
    override var fragmentContainerId = 0;
    private var userInfo: UserCache? = null
    private lateinit var navHostFragment: PrescriptionItemDTO

    override val jsonFile : String = "vitals-page.json"

    override lateinit var viewModel: PharmacistFormViewModel

    private var dtos: PrescriptionDTO? = null

    private var itemAdapter : PharmacistItemAdapter? = null

    private lateinit var benVisitInfo : PatientDisplayWithVisitInfo
    private var patientCount : Int = 0

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
        binding.selectProgram.setOnCheckedChangeListener { _, programId ->
            when (programId){
                binding.btnManualIssue.id -> {
                    dtos?.issueType = "Manual Issue"
                }
                binding.btnSystemIssue.id -> {
                    dtos?.issueType = "System Issue"
                }
            }
        }
        viewModel = ViewModelProvider(this).get(PharmacistFormViewModel::class.java)
        viewModel.prescriptionObserver.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                PharmacistFormViewModel.NetworkState.SUCCESS -> {
                    var result = ""
//                    if(itemAdapter?.itemCount==0||itemAdapter?.itemCount==1) {
//                        result = "Prescription"
//                    }
//                    else {
//                        result = "Prescriptions"
//                    }
                    itemAdapter = context?.let { it ->
                        PharmacistItemAdapter(
                            it,
                            clickListener = PharmacistItemAdapter.PharmacistClickListener { prescription ->
                                val bundle = Bundle()
                                bundle.putString("prescriptionItemDTO", Gson().toJson(prescription))
                                if(prescription.batchList!= null && prescription.batchList.isNotEmpty()){
                                    bundle.putString("batchList", Gson().toJson(prescription.batchList?.get(0)))

                                    bundle.putString("prescriptionDTO", Gson().toJson(dtos))
//                                    Timber.d("*******************Babs DTO************** ",bundle)
//                                    Log.i("Location From home is", "${test!!}")
                                    val batchFragment = PrescriptionBatchFormFragment()
                                    batchFragment.arguments = bundle
                                    findNavController().navigate(
                                        R.id.action_pharmacistFormFragment_to_prescriptionBatchFormFragment, bundle
                                    )
                                }
                                else{
                                    Toast.makeText(requireContext(), "Medicine not available", Toast.LENGTH_SHORT).show()
                                }
//                                requireActivity().supportFragmentManager.beginTransaction().replace(R.id.patient_detalis, batchFragment).commit()
//                                parentFragmentManager.beginTransaction().apply {
//                                    replace(R.id.patient_detalis, batchFragment).commit()
//                                }

                            }
                        )
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
//                            Timber.d("*******************Babs DTO************** ",it)
                            binding.consultantValue.text = it.consultantName
                            binding.visitCodeValue.text = it.visitCode.toString()
                            binding.prescriptionIdValue.text = it.prescriptionID.toString()

//                            itemAdapter?.submitList(it.itemList)

                            it.itemList.let { it ->
//                                Timber.d("*******************DAta Prescription DTO************** ",it)
                                itemAdapter?.submitList(it)
                                binding.pharmacistListContainer.prescriptionCount.text =
                                    itemAdapter?.itemCount.toString() + getResultStr(itemAdapter?.itemCount)
                                if (it != null) {
                                    patientCount = it.size
                                }
                            }
                        }


//                        viewModel.prescriptions?.value?.itemList.let { it ->
//                            Timber.d("*******************DAta Prescription DTO************** ",it)
//                            itemAdapter?.submitList(it?.sortedByDescending { it.id})
//                            binding.pharmacistListContainer.prescriptionCount.text =
//                                itemAdapter?.itemCount.toString() + getResultStr(itemAdapter?.itemCount)
//                            if (it != null) {
//                                patientCount = it.size
//                            }
//                        }
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
        viewModel.savePharmacistData(dtos, benVisitInfo)
        viewModel.isDataSaved.observe(viewLifecycleOwner){ state ->
            when (state!!) {
                true -> {
                    navigateNext()
                }
                else -> {}
            }
        }
//        var isValidData = true
//        if (isValidData) {
//
////            navigateNext()
//        } else {
//            Toast.makeText(requireContext(), "in valid data entered", Toast.LENGTH_SHORT).show()
//        }
    }

    override fun onCancelAction() {
        val intent = Intent(context, HomeActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    override fun navigateNext() {
//        findNavController().navigate(
//            R.id.action_labTechnicianFormFragment_to_patientHomeFragment, bundle
//        )
        val intent = Intent(context, HomeActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

}