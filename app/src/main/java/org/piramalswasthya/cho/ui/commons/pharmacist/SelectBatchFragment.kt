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
            Toast.makeText(requireContext(), "No batch data received. Please try refreshing.", Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
            return
        }
        
        val data2 = arguments?.getString("prescriptionDTO")
        val data3 = arguments?.getString("prescriptionItemDTO")
        benVisitInfo = arguments?.getSerializable("benVisitInfo") as PatientDisplayWithVisitInfo

        prescriptionDTO = Gson().fromJson(data2, PrescriptionDTO::class.java)
        prescriptionItemDTO = Gson().fromJson(data3, PrescriptionItemDTO::class.java)
        
        try {
            val batchType = object : TypeToken<List<PrescriptionBatchDTO>>() {}.type
            batch = Gson().fromJson(data, batchType)
            
            if (batch.isEmpty()) {
                Toast.makeText(requireContext(), "No batches available for this medicine.", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
                return
            }
            
        } catch (e: Exception) {
            Log.e("SelectBatch", "Error parsing batch data", e)
            Toast.makeText(requireContext(), "Error loading batch data. Please try again.", Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
            return
        }
        
        binding.prescribedValue.text = prescriptionItemDTO?.qtyPrescribed.toString()
        Log.d("BatchJSON", "Loaded ${batch.size} batches for ${prescriptionItemDTO?.genericDrugName}")
        
        itemAdapter = context?.let { it ->
            SelectBatchAdapter(it)
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
        val prescribedQty = prescriptionItemDTO?.qtyPrescribed ?: 0
        
        activity?.findViewById<View>(R.id.btnSubmit)?.isEnabled = false
        
        when {
            selectedBatches.isEmpty() -> {
                showErrorDialog(requireContext(), "No Selection", "Please select at least one batch to dispense.")
                activity?.findViewById<View>(R.id.btnSubmit)?.isEnabled = true
            }
            totalDispensed <= 0 -> {
                showErrorDialog(requireContext(), "Invalid Quantity", "Please enter a valid dispense quantity for selected batches.")
                activity?.findViewById<View>(R.id.btnSubmit)?.isEnabled = true
            }
            totalDispensed > prescribedQty -> {
                showErrorDialog(requireContext(), "Quantity Exceeded", "Total dispense quantity ($totalDispensed) cannot exceed prescribed quantity ($prescribedQty).")
                activity?.findViewById<View>(R.id.btnSubmit)?.isEnabled = true
            }
            selectedBatches.any { it.dispenseQuantity > it.qty } -> {
                val exceededBatch = selectedBatches.first { it.dispenseQuantity > it.qty }
                showErrorDialog(requireContext(), "Stock Exceeded", "Batch ${exceededBatch.batchNo} has only ${exceededBatch.qty} units available, but ${exceededBatch.dispenseQuantity} units requested.")
                activity?.findViewById<View>(R.id.btnSubmit)?.isEnabled = true
            }
            selectedBatches.any { it.dispenseQuantity <= 0 } -> {
                showErrorDialog(requireContext(), "Invalid Quantity", "All selected batches must have a dispense quantity greater than 0.")
                activity?.findViewById<View>(R.id.btnSubmit)?.isEnabled = true
            }
            else -> {
                // Update the prescription item with selected batches
                prescriptionItemDTO?.batchList = batch
                
                viewModel.isDataSaved.removeObservers(viewLifecycleOwner)
                viewModel.savePharmacistDataforManual(prescriptionDTO, benVisitInfo)
                viewModel.isDataSaved.observe(viewLifecycleOwner) { state ->
                    when (state!!) {
                        true -> {
                            viewModel.isDataSaved.removeObservers(viewLifecycleOwner)
                            Toast.makeText(requireContext(), "Medicine dispensed successfully", Toast.LENGTH_SHORT).show()
                            navigateNext()
                        }
                        else -> {
                            showErrorDialog(requireContext(), "Dispensing Failed", "Failed to dispense medicine. Please try again.")
                            activity?.findViewById<View>(R.id.btnSubmit)?.isEnabled = true
                        }
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
