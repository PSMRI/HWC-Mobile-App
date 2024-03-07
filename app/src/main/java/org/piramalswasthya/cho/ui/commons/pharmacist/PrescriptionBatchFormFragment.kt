package org.piramalswasthya.cho.ui.commons.pharmacist



import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.fragment.findNavController
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentPrescriptionBatchFormBinding
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PrescriptionBatchDTO
import org.piramalswasthya.cho.model.PrescriptionDTO
import org.piramalswasthya.cho.model.PrescriptionItemDTO
import org.piramalswasthya.cho.model.ProcedureDTO
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import javax.inject.Inject

@AndroidEntryPoint
class PrescriptionBatchFormFragment : Fragment(R.layout.fragment_prescription_batch_form), NavigationAdapter {

    private var _binding: FragmentPrescriptionBatchFormBinding? = null

    private val binding: FragmentPrescriptionBatchFormBinding
        get() {
            return _binding!!
        }

    var fragment: Fragment = this;
    @Inject
    lateinit var preferenceDao: PreferenceDao
    var fragmentContainerId = 0;
    private var userInfo: UserCache? = null

    val jsonFile : String = "vitals-page.json"

    lateinit var viewModel: PrescriptionBatchFormViewModel

    private var dtos: List<ProcedureDTO>? = null
    private lateinit var benVisitInfo : PatientDisplayWithVisitInfo


    private val bundle = Bundle()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Create the ComposeView
        _binding = FragmentPrescriptionBatchFormBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val data = arguments?.getString("batchList")
        if(data==null){
            Toast.makeText(requireContext(), "Medicine not available", Toast.LENGTH_SHORT).show()
        }
        val data2 = arguments?.getString("prescriptionDTO")
        val data3 = arguments?.getString("prescriptionItemDTO")
        val prescriptionDTO = Gson().fromJson(data2, PrescriptionDTO::class.java)
        val prescriptionItemDTO = Gson().fromJson(data3, PrescriptionItemDTO::class.java)
        val batch = Gson().fromJson(data, PrescriptionBatchDTO::class.java)

        binding.consultantValue.text = prescriptionDTO.consultantName
        binding.visitCodeValue.text = prescriptionDTO.visitCode.toString()
        binding.prescriptionIdValue.text = prescriptionDTO.prescriptionID.toString()

        binding.prescribedValue.text = prescriptionItemDTO.qtyPrescribed.toString()
        binding.dispensedValue.text = prescriptionItemDTO.qtyPrescribed.toString()
        if(batch!=null){
            binding.batchValue.text = batch.batchNo
            binding.quantityInHandValue.text = batch.qty.toString()
            binding.dispensedQuantityValue.text = prescriptionItemDTO.qtyPrescribed.toString()
            binding.expiryDateValue.text = batch.expiryDate
        }

        binding.btnOk.setOnClickListener{
//            requireActivity().finish()
            onCancelAction()
        }
    }

    fun getResultStr(count:Int?):String{
        if(count==1||count==0){
            return getString(R.string.patient_cnt_display)
        }
        return getString(R.string.patients_cnt_display)
    }

    override fun getFragmentId(): Int {
        return R.id.fragment_batch_form;
    }

    override fun onSubmitAction() {
        var isValidData = true
    }

    override fun onCancelAction() {
        findNavController().navigate(
            R.id.action_prescriptionBatchFormFragment_to_pharmacistFormFragment, bundle
        )
    }

    fun navigateNext() {
        requireActivity().finish()
    }

}