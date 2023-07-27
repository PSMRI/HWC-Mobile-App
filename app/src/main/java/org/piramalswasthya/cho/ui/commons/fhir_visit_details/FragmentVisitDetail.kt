package org.piramalswasthya.cho.ui.commons.fhir_visit_details

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.Color
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.VisitDetailsInfoBinding
import org.piramalswasthya.cho.ui.commons.FhirFragmentService
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.web_view_activity.WebViewActivity
import java.io.File

@AndroidEntryPoint
class FragmentVisitDetail: Fragment(), NavigationAdapter, FhirFragmentService {
    private var _binding: VisitDetailsInfoBinding?= null

    private var units = mutableListOf<String>()
    private var chiefComplaints = mutableListOf<String>()


    private val subCatOptions = listOf(
        "",
        "Newborn & Infant OPD",
        "Basic Oral Health Care Services",
        "Care for Eye, Ear & Nose and Throat Problems",
        "Child & Adolescent OPD Care",
        "Elderly OPD Health Care",
        "Management of Common Communicable Diseases and Outpatient Care for Acute Simple Illnesses & Minor Ailments",
        "Management of Communicable Diseases including National Health Programs",
        "Palliative Outpatient Care (Symptom Management)",
        "Reproductive Health OPD Care")


    private var unit = ""
    private var reason = ""
    private var selectedCat = ""
    private var selectedSubCat = ""
    private var duration = ""
    private var desc = ""




  private val binding :VisitDetailsInfoBinding
        get() {
            return _binding!!
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        units.addAll(listOf("","Minutes","Hours","Days","Weeks","Months"))
        chiefComplaints.addAll(
            listOf(
                "",
                "Abdominal Bloating",
                "Abdominal Distention",
                "Abdominal Mass",
                "Abdominal Pain",
                "Abdominal Rigidity",
                "Abdominal Swelling",
                "Abnormally Colored Urine",
                "Abrasions",
                "Absence of Speech",
                "Absent Fetal Movements",
                "Acne"
            )
        )
        _binding = VisitDetailsInfoBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val customAdapter = CustomAdapter(requireContext(), R.layout.drop_down, chiefComplaints,binding.chiefComplaintDropDowns)
        binding.chiefComplaintDropDowns.setAdapter(customAdapter)
        binding.subCatInput.setAdapter(ArrayAdapter(requireContext(),R.layout.drop_down,subCatOptions))
        binding.dropdownDurUnit.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down,units))

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

        duration = binding.inputDuration.text.toString()

        binding.dropdownDurUnit.setOnItemClickListener { parent, _, position, _ ->
            unit = parent.getItemAtPosition(position) as String

            binding.dropdownDuration.hint = unit
        }

        desc = binding.descInputText.text.toString()

        binding.selectFileBtn.setOnClickListener {
            openFilePicker()
        }
    }


    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
//                uploadFileToServer(uri)
                val fileSize = getFileSizeFromUri(uri)
                if(fileSize > 5242880) {
                    Toast.makeText(requireContext(), "Please select file less than 5MB", Toast.LENGTH_LONG)
                        .show()
                    binding.selectFileText.text = "Selected File"
                }
                else {
                    val fileName = getFileNameFromUri(uri)
                    binding.selectFileText.text = fileName
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

    override var fragmentContainerId = 0

    override val fragment = this
    override val viewModel: VisitDetailViewModel by viewModels()

    override val jsonFile = "patient-visit-details-paginated.json"

    override fun navigateNext() {
        findNavController().navigate(
            FragmentVisitDetailDirections.actionFhirVisitDetailsFragmentToFhirVitalsFragment()
        )
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