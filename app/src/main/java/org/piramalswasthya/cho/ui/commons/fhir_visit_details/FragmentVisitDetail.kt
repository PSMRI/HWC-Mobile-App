package org.piramalswasthya.cho.ui.commons.fhir_visit_details

import android.app.Activity
import android.content.ClipDescription
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.SubCategoryAdapter
import org.piramalswasthya.cho.databinding.VisitDetailsInfoBinding
import org.piramalswasthya.cho.model.ChiefComplaintMaster
import org.piramalswasthya.cho.model.ChiefComplaintValues
import org.piramalswasthya.cho.model.SubVisitCategory
import org.piramalswasthya.cho.ui.ChiefComplaintInterface
import org.piramalswasthya.cho.ui.commons.FhirFragmentService
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.web_view_activity.WebViewActivity

@AndroidEntryPoint
class FragmentVisitDetail: Fragment(), NavigationAdapter, FhirFragmentService, ChiefComplaintInterface{

    override var fragmentContainerId = 0

    override val fragment = this
    override val viewModel: VisitDetailViewModel by viewModels()

    override val jsonFile = "patient-visit-details-paginated.json"



    private var _binding: VisitDetailsInfoBinding?= null

    private var units = mutableListOf("Hours","Days","Weeks","Months","Years")
    private var chiefComplaints = ArrayList<ChiefComplaintMaster>()

    private var subCatOptions = ArrayList<SubVisitCategory>()

    private lateinit var subCatAdapter: SubCategoryAdapter
    private var isFileSelected: Boolean = false
    private var isFileUploaded: Boolean = false

    private var addCount: Int = 0
    private var deleteCount: Int = 0



  private val binding :VisitDetailsInfoBinding
        get() {
            return _binding!!
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        addCount = 0
        deleteCount = 0
        _binding = VisitDetailsInfoBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subCatAdapter = SubCategoryAdapter(requireContext(),R.layout.drop_down,subCatOptions)
        binding.subCatInput.setAdapter(subCatAdapter)

        viewModel.subCatVisitList.observe( viewLifecycleOwner) { subCats ->
            subCatOptions.clear()
            subCatOptions.addAll(subCats)
            subCatAdapter.notifyDataSetChanged()
        }

        binding.subCatInput.setOnItemClickListener { parent, view, position, id ->
            var subCat = parent.getItemAtPosition(position) as SubVisitCategory
            binding.subCatInput.setText(subCat?.name,false)
        }

        binding.subCatInput.threshold = 1

        viewModel.chiefComplaintMaster.observe(viewLifecycleOwner){ chiefComplaintsList ->
            chiefComplaints.clear()
            chiefComplaints.addAll(chiefComplaintsList)
        }

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.radioButton1 ->{
                    binding.radioGroup2.visibility = View.VISIBLE
                    binding.reasonText.visibility = View.VISIBLE
                }
                else ->{
                    binding.radioGroup2.visibility = View.GONE
                    binding.reasonText.visibility = View.GONE
                }
            }
        }
        binding.selectFileBtn.setOnClickListener {
            openFilePicker()
        }
        binding.uploadFileBtn.setOnClickListener {
            Toast.makeText(requireContext(),"You have uploaded the file",Toast.LENGTH_SHORT).show()
            isFileUploaded = true
        }

        binding.plusButton.setOnClickListener {
            addExtraChiefComplaint(addCount)
        }

        addExtraChiefComplaint(addCount)
    }
    private fun addExtraChiefComplaint(count: Int){
        val fragmentManager : FragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction : FragmentTransaction = fragmentManager.beginTransaction()
        val chiefFragment = ChiefComplaintFragment(chiefComplaints,units,binding.chiefComplaintExtra)
        val tag = "Extra_Complaint_$count"
        chiefFragment.setFragmentTag(tag)
        chiefFragment.setListener(this)
        fragmentTransaction.add(binding.chiefComplaintExtra.id, chiefFragment, tag)
        fragmentTransaction.addToBackStack(null) // Optional: Add the transaction to the back stack
        fragmentTransaction.commit()
        addCount += 1
    }
    private fun deleteExtraChiefComplaint(tag: String){
        val fragmentManager : FragmentManager = requireActivity().supportFragmentManager
        val fragmentToDelete = fragmentManager.findFragmentByTag(tag)
        if (fragmentToDelete != null) {
            fragmentManager.beginTransaction().remove(fragmentToDelete).commit()
            deleteCount += 1
        }
    }

    override fun onDeleteButtonClicked(fragmentTag: String) {
        if(addCount - 1 > deleteCount) deleteExtraChiefComplaint(fragmentTag)
    }

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
//                uploadFileToServer(uri)
                val fileSize = getFileSizeFromUri(uri)
                if(fileSize > 5242880) {
                    Toast.makeText(requireContext(), "Please select file less than 5MB", Toast.LENGTH_SHORT)
                        .show()
                    binding.selectFileText.text = "Selected File"
                    binding.uploadFileBtn.isEnabled = false
                    isFileSelected = false
                    isFileUploaded = false
                }
                else {
                    val fileName = getFileNameFromUri(uri)
                    binding.selectFileText.text = fileName
                    binding.uploadFileBtn.isEnabled = true
                    isFileSelected = true
                }
            }
        }
    }

    private fun getFileSizeFromUri(uri: Uri): Long {
        val contentResolver = requireActivity().contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    val size = it.getLong(sizeIndex)
                    it.close()
                    return size
                }
                it.close()
            }
        }
        return 0 // Return 0 if file size information is not available
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*" // You can restrict the file type here if needed
        }
        filePickerLauncher.launch(intent)
    }

    private fun uploadFileToServer(fileUri: Uri) {
        Toast.makeText(requireContext(),"Uri $fileUri", Toast.LENGTH_LONG).show()
    }

    private fun getFileNameFromUri(uri: Uri): String {
        val cursor = requireActivity().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            it.moveToFirst()
            val displayNameColumnIndex = it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
            val displayName = it.getString(displayNameColumnIndex)
            it.close()
            return displayName
        }
        return "Unknown"
    }



    override fun navigateNext() {
        val count = binding.chiefComplaintExtra.childCount
        var chiefComplaintsList = mutableListOf<ChiefComplaintValues>()
        for(i in 0..count.minus(1)){
            val childView: View? = binding.chiefComplaintExtra?.getChildAt(i)
            val chiefComplaintVal = childView?.findViewById<AutoCompleteTextView>(R.id.chiefComplaintDropDowns)
            val durationVal = childView?.findViewById<TextInputEditText>(R.id.inputDuration)
            val unitDurationVal = childView?.findViewById<AutoCompleteTextView>(R.id.dropdownDurUnit)
            val descVal = childView?.findViewById<TextInputEditText>(R.id.descInputText)

            if(chiefComplaintVal?.text?.isNotEmpty()!! &&
                durationVal?.text?.isNotEmpty()!! &&
                unitDurationVal?.text?.isNotEmpty()!! &&
                descVal?.text?.isNotEmpty()!!){
                var chiefComplaintValues = ChiefComplaintValues(
                    chiefComplaintVal?.text.toString(),
                    unitDurationVal?.text.toString(),
                    descVal?.text.toString(),
                    durationVal?.text.toString()?.toInt()!!)
                chiefComplaintsList.add(chiefComplaintValues)
            }
        }
        if(isFileSelected && isFileUploaded) {
            findNavController().navigate(
                FragmentVisitDetailDirections.actionFhirVisitDetailsFragmentToFhirVitalsFragment()
            )
        } else if(!isFileSelected){
            findNavController().navigate(
                FragmentVisitDetailDirections.actionFhirVisitDetailsFragmentToFhirVitalsFragment()
            )
        } else {
            Toast.makeText(requireContext(),"Please Upload the Selected File",Toast.LENGTH_SHORT).show()
        }
    }

    override fun getFragmentId(): Int {
       return R.id.fragment_visit_details_info
    }

    override fun onSubmitAction() {
        navigateNext()
    }

    override fun onCancelAction() {
        val intent = Intent(context, WebViewActivity::class.java)
        startActivity(intent)
    }
}