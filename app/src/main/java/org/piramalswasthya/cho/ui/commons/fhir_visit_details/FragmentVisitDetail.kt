package org.piramalswasthya.cho.ui.commons.fhir_visit_details

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Annotation
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.Reference
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.SubCategoryAdapter
import org.piramalswasthya.cho.databinding.VisitDetailsInfoBinding
import org.piramalswasthya.cho.model.ChiefComplaintMaster
import org.piramalswasthya.cho.model.ChiefComplaintValues
import org.piramalswasthya.cho.model.SubVisitCategory
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.ui.ChiefComplaintInterface
import org.piramalswasthya.cho.ui.commons.FhirExtension
import org.piramalswasthya.cho.ui.commons.FhirFragmentService
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.web_view_activity.WebViewActivity

@AndroidEntryPoint
class FragmentVisitDetail : Fragment(), NavigationAdapter, FhirFragmentService,
    ChiefComplaintInterface {

    override var fragmentContainerId = 0

    override val fragment = this
    override val viewModel: VisitDetailViewModel by viewModels()

    override val jsonFile = "patient-visit-details-paginated.json"


    private var loggedInUser: UserCache? = null

    private var _binding: VisitDetailsInfoBinding? = null

    private var units = mutableListOf("Hours", "Days", "Weeks", "Months", "Years")
    private var chiefComplaints = ArrayList<ChiefComplaintMaster>()

    private var subCatOptions = ArrayList<SubVisitCategory>()

    private lateinit var subCatAdapter: SubCategoryAdapter
    private var isFileSelected: Boolean = false
    private var isFileUploaded: Boolean = false

    private var addCount: Int = 0
    private var deleteCount: Int = 0

    private var category: String = ""
    private var subCategory: String = ""
    private var reason: String = ""
    private var encounter = Encounter()
    private var listOfConditions = mutableListOf<Condition>()
    private var chiefMap = emptyMap<Int,String>()
    private var base64String = ""


    private val binding: VisitDetailsInfoBinding
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
        _binding = VisitDetailsInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subCatAdapter = SubCategoryAdapter(requireContext(), R.layout.drop_down, subCatOptions)
        lifecycleScope.launch {
            chiefMap = viewModel.getChiefMap()
        }
        binding.subCatInput.setAdapter(subCatAdapter)
        // calling to get LoggedIn user Details
        viewModel.getLoggedInUserDetails()
        viewModel.boolCall.observe(viewLifecycleOwner){
            if(it){
                loggedInUser = viewModel.loggedInUser
                viewModel.resetBool()
            }
        }
        viewModel.subCatVisitList.observe(viewLifecycleOwner) { subCats ->
            subCatOptions.clear()
            subCatOptions.addAll(subCats)
            subCatAdapter.notifyDataSetChanged()
        }

        binding.subCatInput.setOnItemClickListener { parent, view, position, id ->
            var subCat = parent.getItemAtPosition(position) as SubVisitCategory
            binding.subCatInput.setText(subCat?.name, false)
        }

        binding.subCatInput.threshold = 1

        viewModel.chiefComplaintMaster.observe(viewLifecycleOwner) { chiefComplaintsList ->
            chiefComplaints.clear()
            chiefComplaints.addAll(chiefComplaintsList)
        }

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radioButton1 -> {
                    binding.radioGroup2.visibility = View.VISIBLE
                    binding.reasonText.visibility = View.VISIBLE
                    category = binding.radioButton1.text.toString()
                }

                else -> {
                    binding.radioGroup2.visibility = View.GONE
                    binding.reasonText.visibility = View.GONE
                    category = binding.radioButton2.text.toString()
                }
            }
        }
        binding.radioGroup2.setOnCheckedChangeListener { _, checkedId ->
            reason = when (checkedId) {
                R.id.radioButton3 -> {
                    binding.radioButton3.text.toString()
                } else -> {
                    binding.radioButton4.text.toString()
                }
            }
        }
        binding.selectFileBtn.setOnClickListener {
            openFilePicker()
        }
        binding.uploadFileBtn.setOnClickListener {
            Toast.makeText(requireContext(), "You have uploaded the file", Toast.LENGTH_SHORT)
                .show()
            isFileUploaded = true
        }

        binding.plusButton.setOnClickListener {
            addExtraChiefComplaint(addCount)
        }

        addExtraChiefComplaint(addCount)
    }

    private fun addExtraChiefComplaint(count: Int) {
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val chiefFragment =
            ChiefComplaintFragment(chiefComplaints, units, binding.chiefComplaintExtra)
        val tag = "Extra_Complaint_$count"
        chiefFragment.setFragmentTag(tag)
        chiefFragment.setListener(this)
        fragmentTransaction.add(binding.chiefComplaintExtra.id, chiefFragment, tag)
        fragmentTransaction.addToBackStack(null) // Optional: Add the transaction to the back stack
        fragmentTransaction.commit()
        addCount += 1
    }

    private fun deleteExtraChiefComplaint(tag: String) {
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val fragmentToDelete = fragmentManager.findFragmentByTag(tag)
        if (fragmentToDelete != null) {
            fragmentManager.beginTransaction().remove(fragmentToDelete).commit()
            deleteCount += 1
        }
    }

    override fun onDeleteButtonClicked(fragmentTag: String) {
        if (addCount - 1 > deleteCount) deleteExtraChiefComplaint(fragmentTag)
    }

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { uri ->
//                uploadFileToServer(uri)
                    val fileSize = getFileSizeFromUri(uri)
                    if (fileSize > 5242880) {
                        Toast.makeText(
                            requireContext(),
                            "Please select file less than 5MB",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        binding.selectFileText.text = "Selected File"
                        binding.uploadFileBtn.isEnabled = false
                        isFileSelected = false
                        isFileUploaded = false
                    } else {
                        convertFileToBase64String(uri,fileSize)
                        val fileName = getFileNameFromUri(uri)
                        binding.selectFileText.text = fileName
                        binding.uploadFileBtn.isEnabled = true
                        isFileSelected = true
                    }
                }
            }
        }

    private fun convertFileToBase64String(uri: Uri, fileSize: Long){
        val contentResolver = requireActivity().contentResolver
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.use {
            val byteArray = ByteArray(fileSize.toInt())
            val bytesRead = it.read(byteArray)
            if (bytesRead > 0) {
                base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)
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
        Toast.makeText(requireContext(), "Uri $fileUri", Toast.LENGTH_LONG).show()
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
        // calling to add Cat and SubCat
        val catBool = checkAndAddCatSubCat()

        // calling to add Chief Complaints
        addChiefComplaintsData()

        if (catBool && isFileSelected && isFileUploaded) {
            if(encounter != null) viewModel.saveVisitDetailsInfo(encounter!!,listOfConditions)
            findNavController().navigate(
                FragmentVisitDetailDirections.actionFhirVisitDetailsFragmentToHistoryCustomFragment()
            )
        } else if (!isFileSelected && catBool)  {
            if(encounter != null) viewModel.saveVisitDetailsInfo(encounter!!,listOfConditions)
            findNavController().navigate(
                FragmentVisitDetailDirections.actionFhirVisitDetailsFragmentToHistoryCustomFragment()
            )
        } else {
            Toast.makeText(
                requireContext(),
                "Please Upload the Selected File",
                Toast.LENGTH_SHORT
            ).show()
        }

    }
    private fun checkAndAddCatSubCat(): Boolean {
        if (binding.subCatInput.text.isNullOrEmpty())
            binding.subCatInput.requestFocus()
        else
            subCategory = binding.subCatInput.text.toString()

        if (binding.radioGroup.checkedRadioButtonId == -1)
            Toast.makeText(requireContext(), "Please Select Category", Toast.LENGTH_SHORT).show()

        if (subCategory.isNotEmpty() && category.isNotEmpty()) {
            createEncounterResource()
            return true
        }
        return false
    }
    private fun createEncounterResource() {
        // Set Encounter type
        val encounterType = Coding()
        encounterType.system =
            "http://snomed.info/sct"
        encounterType.code = "Category"
        encounterType.display = category
        encounter.type = listOf(CodeableConcept().addCoding(encounterType))

        // Set Service Type
        val serviceType = Coding()
        serviceType.system =
            "http://snomed.info/sct"
        serviceType.code = "SubCategory"
        serviceType.display = subCategory
        encounter.serviceType = CodeableConcept().addCoding(serviceType)

        val classVal = Coding()
        classVal.system = "http://terminology.hl7.org/CodeSystem/v3-ActCode"
        classVal.code = "AMB"
        classVal.display = "ambulatory"
        encounter.class_ = classVal

        encounter.status = Encounter.EncounterStatus.INPROGRESS
        encounter!!.reasonCode = listOf(CodeableConcept().setText(reason))

        // add extensions
        addExtensionsToEncounter(encounter)
    }

    private fun addExtensionsToEncounter(encounter: Encounter) {
        if(loggedInUser != null) {
            encounter.addExtension(
                FhirExtension.getExtenstion(
                    FhirExtension.getUrl("vanID", "Encounter#Encounter"),
                    FhirExtension.getStringType(loggedInUser!!.vanId.toString())
                )
            )

            encounter.addExtension(
                FhirExtension.getExtenstion(
                    FhirExtension.getUrl("parkingPlaceID", "Encounter#Encounter"),
                    FhirExtension.getStringType(loggedInUser!!.parkingPlaceId.toString())
                )
            )

            encounter.addExtension(
                FhirExtension.getExtenstion(
                    FhirExtension.getUrl("providerServiceMapId", "Encounter#Encounter"),
                    FhirExtension.getStringType(loggedInUser!!.serviceMapId.toString())
                )
            )

            encounter.addExtension(
                FhirExtension.getExtenstion(
                    FhirExtension.getUrl("createdBy", "Encounter#Encounter"),
                    FhirExtension.getStringType(loggedInUser!!.userName)
                )
            )
        }
    }
    private fun <K, V> findKeyByValue(map: Map<K, V>, value: V): K? {
        return map.entries.find { it.value == value }?.key
    }

    private fun addChiefComplaintsData() {
        // get all the layouts and corresponding fields values
        val count = binding.chiefComplaintExtra.childCount
        for (i in 0..count.minus(1)) {
            val childView: View? = binding.chiefComplaintExtra?.getChildAt(i)
            val chiefComplaintVal =
                childView?.findViewById<AutoCompleteTextView>(R.id.chiefComplaintDropDowns)
            val durationVal = childView?.findViewById<TextInputEditText>(R.id.inputDuration)
            val unitDurationVal =
                childView?.findViewById<AutoCompleteTextView>(R.id.dropdownDurUnit)
            val descVal = childView?.findViewById<TextInputEditText>(R.id.descInputText)

            if (chiefComplaintVal?.text?.isNotEmpty()!! &&
                durationVal?.text?.isNotEmpty()!! &&
                unitDurationVal?.text?.isNotEmpty()!! &&
                descVal?.text?.isNotEmpty()!!
            ) {
                val id = findKeyByValue(chiefMap,chiefComplaintVal?.text?.toString())
                var chiefComplaintValues = ChiefComplaintValues(
                    chiefComplaintVal?.text.toString(),
                    unitDurationVal?.text.toString(),
                    descVal?.text.toString(),
                    durationVal?.text.toString()?.toInt()!!
                )

                // Creating the "Condition" resource
                val condition = Condition()

                // Set the code for the chief complaint
                val chiefComplaint = Coding()
                chiefComplaint.system =
                    "http://snomed.info/sct"
                chiefComplaint.code = id.toString()
                chiefComplaint.display = chiefComplaintValues.chiefComplaint
                condition.code = CodeableConcept().addCoding(chiefComplaint)

                // Set the note for the description
                val note = Annotation()
                note.text = chiefComplaintValues.description
                condition.note = listOf(note)

                //set subject to condition
                val ref = Reference("give here reg/ben-reg Id")
                condition.subject = ref

                // calling this addExtensionsToConditionResources() method to add van,parking, providerServiceMapId and duration extension
                addExtensionsToConditionResources(condition, chiefComplaintValues)
                listOfConditions.add(condition)
            }
        }
    }

    private fun addExtensionsToConditionResources(
        condition: Condition,
        chiefComplaintValues: ChiefComplaintValues
    ) {
        if (loggedInUser != null) {
            condition.addExtension(
                FhirExtension.getExtenstion(
                    FhirExtension.getUrl("duration", "Condition#Condition"),
                    FhirExtension.getCoding(
                        chiefComplaintValues.durationUnit,
                        chiefComplaintValues.duration.toString()
                    )
                )
            )

            condition.addExtension(
                FhirExtension.getExtenstion(
                    FhirExtension.getUrl("vanID", "Condition#Condition"),
                    FhirExtension.getStringType(loggedInUser!!.vanId.toString())
                )
            )

            condition.addExtension(
                FhirExtension.getExtenstion(
                    FhirExtension.getUrl("parkingPlaceID", "Condition#Condition"),
                    FhirExtension.getStringType(loggedInUser!!.parkingPlaceId.toString())
                )
            )

            condition.addExtension(
                FhirExtension.getExtenstion(
                    FhirExtension.getUrl("providerServiceMapId", "Condition#Condition"),
                    FhirExtension.getStringType(loggedInUser!!.serviceMapId.toString())
                )
            )

            condition.addExtension(
                FhirExtension.getExtenstion(
                    FhirExtension.getUrl("createdBy", "Condition#Condition"),
                    FhirExtension.getStringType(loggedInUser!!.userName)
                )
            )
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