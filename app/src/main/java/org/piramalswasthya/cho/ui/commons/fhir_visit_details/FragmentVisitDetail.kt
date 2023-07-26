package org.piramalswasthya.cho.ui.commons.fhir_visit_details

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.VisitDetailsInfoBinding
import org.piramalswasthya.cho.ui.commons.FhirFragmentService
import org.piramalswasthya.cho.ui.commons.NavigationAdapter

@AndroidEntryPoint
class FragmentVisitDetail: Fragment(), NavigationAdapter, FhirFragmentService {
    private var _binding: VisitDetailsInfoBinding?= null

    private var units = mutableListOf<String>()
    private val reasons = mutableListOf<String>()

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
        units.addAll(listOf("Minutes","Hours","Days","Weeks","Months"))
        reasons.addAll(listOf("ReVisit","Just Check"))
        _binding = VisitDetailsInfoBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val customAdapter = CustomAdapter(requireContext(), R.layout.drop_down, units,binding.dropdownDurUnit)
        binding.dropdownDurUnit.setAdapter(customAdapter)
//        binding.dropdownDurUnit.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down,units))
        binding.reasonDropDown.setAdapter(ArrayAdapter(requireContext(),R.layout.drop_down,reasons))
        getDetails()
    }

    private fun getDetails(){
        binding.catRadioGroup.setOnCheckedChangeListener{group,checkedId ->
           selectedCat = group[checkedId].toString()
        }

        binding.reasonDropDown.setOnItemClickListener { parent, _, position, _ ->
            reason = parent.getItemAtPosition(position) as String
        }

        binding.subCatRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            selectedSubCat = group[checkedId].toString()
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
                uploadFileToServer(uri)
                val fileName = getFileNameFromUri(uri)
                binding.selectFileText.text = fileName
            }
        }
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
            val displayNameColumnIndex = it.getColumnIndexOrThrow("_display_name")
            val displayName = it.getString(displayNameColumnIndex)
//            val fileSize = it.getLong(displayNameColumnIndex)
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

    }

    override fun getFragmentId(): Int {
       return R.id.visit_details_container
    }

    override fun onSubmitAction() {
        navigateNext()
    }

    override fun onCancelAction() {
        TODO("Not yet implemented")
    }
}