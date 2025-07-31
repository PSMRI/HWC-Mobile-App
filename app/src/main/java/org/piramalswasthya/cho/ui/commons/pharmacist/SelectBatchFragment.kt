package org.piramalswasthya.cho.ui.commons.pharmacist

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.SelectBatchAdapter
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentSelectBatchBinding
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PrescriptionBatchDTO
import org.piramalswasthya.cho.model.PrescriptionDTO
import org.piramalswasthya.cho.model.PrescriptionItemDTO
import org.piramalswasthya.cho.model.ProcedureDTO
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import javax.inject.Inject
@AndroidEntryPoint
class SelectBatchFragment : Fragment(R.layout.fragment_select_batch), NavigationAdapter {

    private var data: String? = ""
    private var prescriptionItemDTO: PrescriptionItemDTO? = null
    private var prescriptionDTO: PrescriptionDTO? = null
    private var _binding: FragmentSelectBatchBinding? = null

    private val binding: FragmentSelectBatchBinding
        get() {
            return _binding!!
        }

    var fragment: Fragment = this;
    @Inject
    lateinit var preferenceDao: PreferenceDao
    var fragmentContainerId = 0;
    private var userInfo: UserCache? = null
    lateinit var viewModel: PharmacistFormViewModel

    val jsonFile : String = "vitals-page.json"


    private var dtos: List<ProcedureDTO>? = null
    private lateinit var benVisitInfo : PatientDisplayWithVisitInfo

    private var itemAdapter : SelectBatchAdapter? = null

    private val bundle = Bundle()
    var batch: List<PrescriptionBatchDTO> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectBatchBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(PharmacistFormViewModel::class.java)

        data = arguments?.getString("batchList")
        if(data==null){
            Toast.makeText(requireContext(), "Medicine not available", Toast.LENGTH_SHORT).show()
        }
        val data2 = arguments?.getString("prescriptionDTO")
        val data3 = arguments?.getString("prescriptionItemDTO")
         benVisitInfo = arguments?.getSerializable("benVisitInfo") as PatientDisplayWithVisitInfo

         prescriptionDTO = Gson().fromJson(data2, PrescriptionDTO::class.java)
         prescriptionItemDTO = Gson().fromJson(data3, PrescriptionItemDTO::class.java)
        val batchType = object : TypeToken<List<PrescriptionBatchDTO>>() {}.type
         batch = Gson().fromJson(data, batchType)
        binding.prescribedValue.text = prescriptionItemDTO?.qtyPrescribed.toString()
        Log.d("BatchJSON", (batch ?: "Data is null").toString())
        Log.d("BatchJSON", (prescriptionItemDTO ?: "Data is null").toString())
        itemAdapter = context?.let { it ->
            SelectBatchAdapter(
                it
            )
        }
        binding.rvBatch.adapter = itemAdapter

        itemAdapter?.submitList(batch)


    }

    fun getResultStr(count:Int?):String{
        if(count==1||count==0){
            return getString(R.string.patient_cnt_display)
        }
        return getString(R.string.patients_cnt_display)
    }


    fun showErrorDialog(context: Context, title: String ="Warning", message: String) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    fun navigateNext() {
        requireActivity().finish()
    }

    override fun getFragmentId(): Int {
        return R.id.select;
    }

    override fun onSubmitAction() {
        val selectedBatches = batch.filter { it.isSelected }
        val totalDispensed = selectedBatches.sumOf { it.dispenseQuantity }
        val prescribedQty = prescriptionItemDTO?.qtyPrescribed!!
        when {
            totalDispensed > prescribedQty -> {
                showErrorDialog(requireContext(),"Warning","Dispense quantity can not be more than total quantity prescribed")
            }
            selectedBatches.any { it.dispenseQuantity > it.qty } -> {
                showErrorDialog(requireContext(),"Warning","One or more batches exceed quantity in hand")

            }
            else -> {
                viewModel.savePharmacistDataforManual(prescriptionDTO, benVisitInfo)
                viewModel.isDataSaved.observe(viewLifecycleOwner){ state ->
                    when (state!!) {
                        true -> {
                            navigateNext()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    override fun onCancelAction() {
        findNavController().navigate(
            R.id.pharmacistFormFragment, bundle
        )
    }


}