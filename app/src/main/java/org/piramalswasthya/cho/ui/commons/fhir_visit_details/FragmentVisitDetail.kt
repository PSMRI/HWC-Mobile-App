package org.piramalswasthya.cho.ui.commons.fhir_visit_details

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Color
import android.util.Log
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.hl7.fhir.r4.model.Annotation
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.ResourceType
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.SubCategoryAdapter
import org.piramalswasthya.cho.databinding.VisitDetailsInfoBinding
import org.piramalswasthya.cho.fhir_utils.FhirExtension
import org.piramalswasthya.cho.fhir_utils.extension_names.createdBy
import org.piramalswasthya.cho.fhir_utils.extension_names.duration
import org.piramalswasthya.cho.fhir_utils.extension_names.parkingPlaceID
import org.piramalswasthya.cho.fhir_utils.extension_names.providerServiceMapId
import org.piramalswasthya.cho.fhir_utils.extension_names.vanID
import org.piramalswasthya.cho.model.ChiefComplaintMaster
import org.piramalswasthya.cho.model.ChiefComplaintValues
import org.piramalswasthya.cho.model.MasterDb
import org.piramalswasthya.cho.model.SubVisitCategory
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.model.VisitMasterDb
import org.piramalswasthya.cho.ui.commons.FhirFragmentService
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.SpeechToTextContract

@AndroidEntryPoint
class FragmentVisitDetail : Fragment(), NavigationAdapter, FhirFragmentService,EndIconClickListener {

    override var fragmentContainerId = 0
    private lateinit var patientId : String

    override val fragment = this
    override val viewModel: VisitDetailViewModel by viewModels()

    override val jsonFile = "patient-visit-details-paginated.json"

    private var usernameEs : String = ""
    private var passwordEs : String = ""
    private var userInfo: UserCache? = null

    private var _binding: VisitDetailsInfoBinding? = null

    private var units = mutableListOf("Hours", "Days", "Weeks", "Months", "Years")
    private var chiefComplaints = ArrayList<ChiefComplaintMaster>()
    private var chiefComplaintsForFilter = ArrayList<ChiefComplaintMaster>()

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
    private var base64String = ""
    private var currDurationPos = -1
    private var currDescPos = -1
    private var currChiefPos = -1
    private var catBool: Boolean = false
    private var subCat: Boolean = false
    private val bundle = Bundle()


    private lateinit var adapter: VisitDetailAdapter

    private val initialItem = ChiefComplaintValues()
    private val itemList = mutableListOf(initialItem)
    private val enCounterExtension: FhirExtension = FhirExtension(ResourceType.Encounter)
    private val conditionExtension: FhirExtension = FhirExtension(ResourceType.Condition)


    private val binding: VisitDetailsInfoBinding
        get() {
            return _binding!!
        }
    private val speechToTextLauncherForDuration = registerForActivityResult(SpeechToTextContract()) { result ->
        if (result.isNotBlank() && result.isNumeric()) {
            val pattern = "\\d{2}".toRegex()
            val match = pattern.find(result)
            val firstTwoDigits = match?.value
            if(result.toInt() > 0) updateDurationText(firstTwoDigits!!)
        }
    }

    // method to check string contains numeric val
    private fun String.isNumeric(): Boolean {
        return try { this.toDouble()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }

    private val speechToTextLauncherForDesc = registerForActivityResult(SpeechToTextContract()) { result ->
        if (result.isNotBlank()) {
           updateDescText(result)
        }
    }
    private val speechToTextLauncherForChiefMaster = registerForActivityResult(SpeechToTextContract()) { result ->
        if (result.isNotBlank()) {
            updateChiefText(result)
        }
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
        subCatAdapter = SubCategoryAdapter(requireContext(), R.layout.dropdown_subcategory,R.id.tv_dropdown_item_text, subCatOptions.map { it.name })
        binding.subCatInput.setAdapter(subCatAdapter)
        // calling to get LoggedIn user Details
        viewModel.getLoggedInUserDetails()
        viewModel.boolCall.observe(viewLifecycleOwner) {
            if (it) {
                userInfo = viewModel.loggedInUser
                viewModel.resetBool()
            }
        }
        viewModel.subCatVisitList.observe(viewLifecycleOwner) { subCats ->
            subCatOptions.clear()
            subCatOptions.addAll(subCats)
            subCatAdapter.addAll(subCatOptions.map { it.name })
            subCatAdapter.notifyDataSetChanged()
        }

        binding.subCatInput.setOnItemClickListener { parent, _, position, _ ->
//            var subCat = parent.getItemAtPosition(position) as SubVisitCategory
            var subCat = parent.getItemAtPosition(position)
            binding.subCatInput.setText(subCat.toString(), false)
            binding.subCatDropDown.apply {
                boxStrokeColor = resources.getColor(R.color.purple)
                hintTextColor = defaultHintTextColor }
        }

        if (requireArguments().getString("patientId") != null) {
            patientId = requireArguments().getString("patientId")!!
        }

        binding.subCatInput.threshold = 1

        viewModel.chiefComplaintMaster.observe(viewLifecycleOwner) { chiefComplaintsList ->
            chiefComplaints.clear()
            chiefComplaints.addAll(chiefComplaintsList)

            chiefComplaintsForFilter.clear()
            chiefComplaintsForFilter.addAll(chiefComplaintsList)
        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
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
                }

                else -> {
                    binding.radioButton4.text.toString()
                }
            }
        }
        binding.selectFileBtn.setOnClickListener {
            openFilePicker()
        }
        binding.uploadFileBtn.setOnClickListener {
            Toast.makeText(requireContext(), resources.getString(R.string.toast_file_uploaded), Toast.LENGTH_SHORT)
                .show()
            isFileUploaded = true
            binding.uploadFileBtn.text = "Uploaded"
            binding.uploadFileBtn.isEnabled = false
        }
        if (viewModel.fileName.isNotEmpty() && viewModel.base64String.isNotEmpty()) {
            binding.uploadFileBtn.text = "Uploaded"
            binding.selectFileText.setText(viewModel.fileName)
        }
        adapter = VisitDetailAdapter(
            itemList,
            units,
            chiefComplaints,
            object : RecyclerViewItemChangeListener {
                override fun onItemChanged() {
                    binding.plusButton.isEnabled = !isAnyItemEmpty()
                }
            },
            chiefComplaintsForFilter,
            this
        )
        binding.rv.adapter = adapter
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rv.layoutManager = layoutManager
        adapter.notifyItemInserted(itemList.size - 1)

        binding.plusButton.isEnabled = !isAnyItemEmpty()
        binding.plusButton.setOnClickListener {
            val newItem = ChiefComplaintValues()
            itemList.add(newItem)
            adapter.notifyItemInserted(itemList.size - 1)
            binding.plusButton.isEnabled = false
        }
    }

    fun isAnyItemEmpty(): Boolean {
        for (item in itemList) {
            if (item.chiefComplaint.isEmpty() || item.duration.isEmpty() || item.durationUnit.isEmpty() || item.description.isEmpty()) {
                return true
            }
        }
        return false
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
                            resources.getString(R.string.toast_file_size_max),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        binding.uploadFileBtn.text = "Upload File"
                        binding.uploadFileBtn.isEnabled = false
                        binding.selectFileText.setTextColor(Color.BLACK)
                        isFileSelected = false
                        isFileUploaded = false
                    } else {
                        convertFileToBase64String(uri, fileSize)
                        val fileName = getFileNameFromUri(uri)
                        binding.selectFileText.setText(fileName)
                        binding.uploadFileBtn.isEnabled = true
                        binding.uploadFileBtn.text = "Upload File"
                        isFileSelected = true
                        isFileUploaded = false
                        viewModel.setBase64Str(base64String, fileName)
                    }
                }
            }
        }

    private fun convertFileToBase64String(uri: Uri, fileSize: Long) {
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

         // initially calling checkAndAddCatSubCat() but now changed to
         // validation on category and Subcategory
        catBool = if (binding.radioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(requireContext(), resources.getString(R.string.toast_cat_select), Toast.LENGTH_SHORT).show()
            false
        } else true


        if (binding.subCatInput.text.isNullOrEmpty()) {
            if(catBool) binding.subCatInput.requestFocus()
            binding.subCatDropDown.apply {
                boxStrokeColor = Color.RED
                hintTextColor = ColorStateList.valueOf(Color.RED)
            }
            if(catBool) Toast.makeText(requireContext(), resources.getString(R.string.toast_sub_cat_select), Toast.LENGTH_SHORT).show()
            subCat = false
        } else {
            subCategory = binding.subCatInput.text.toString()
            subCat = true
        }

        if(subCat && catBool) createEncounterResource()

        // calling to add Chief Complaints
        val chiefData =  addChiefComplaintsData()

        setVisitMasterData()


        if (catBool && subCat && isFileSelected && isFileUploaded && chiefData) {
            if (encounter != null) viewModel.saveVisitDetailsInfo(encounter!!, listOfConditions)
            findNavController().navigate(
                R.id.action_fhirVisitDetailsFragment_to_customVitalsFragment,bundle
            )
        } else if (!isFileSelected && catBool && subCat && chiefData) {
            if (encounter != null) viewModel.saveVisitDetailsInfo(encounter!!, listOfConditions)
            findNavController().navigate(
                R.id.action_fhirVisitDetailsFragment_to_customVitalsFragment,bundle
            )
        } else if(isFileSelected && !isFileUploaded && catBool && subCat && chiefData) {
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.toast_upload_file),
                Toast.LENGTH_SHORT
            ).show()
        }

    }
    private fun setVisitMasterData() {
        val masterDb = MasterDb()
        val visitMasterDb = VisitMasterDb()

        val selectedCategoryRadioButtonId = binding.radioGroup.checkedRadioButtonId
        val selectedReasonRadioButtonId = binding.radioGroup2.checkedRadioButtonId

        val selectedCategoryRadioButton = view?.findViewById<RadioButton>(selectedCategoryRadioButtonId)
        val selectedReasonRadioButton = view?.findViewById<RadioButton>(selectedReasonRadioButtonId)

        visitMasterDb.category = selectedCategoryRadioButton?.text.toString()
        visitMasterDb.reason = selectedReasonRadioButton?.text.toString()
Log.d("aryan","   ${masterDb?.visitMasterDb?.chiefComplaint?.size}")
        val subCategory = binding.subCatInput.text.toString()
        visitMasterDb.subCategory = subCategory

        val chiefComplaintList = mutableListOf<ChiefComplaintValues>()
        for (i in 0 until itemList.size) {
            val chiefComplaintData = itemList[i]

            if (chiefComplaintData.chiefComplaint.isNotEmpty() &&
                chiefComplaintData.duration.isNotEmpty() &&
                chiefComplaintData.durationUnit.isNotEmpty() &&
                chiefComplaintData.description.isNotEmpty()
            ) {
                var cc = ChiefComplaintValues(
                    chiefComplaint = chiefComplaintData.chiefComplaint,
                    duration = chiefComplaintData.duration,
                    durationUnit = chiefComplaintData.durationUnit,
                    description = chiefComplaintData.description
                )
                chiefComplaintList.add(cc)
            }
        }

        visitMasterDb.chiefComplaint = chiefComplaintList
        masterDb.visitMasterDb = visitMasterDb
        bundle.putSerializable("MasterDb", masterDb)
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
        if (userInfo != null) {
            encounter.addExtension( enCounterExtension.getExtenstion(
                enCounterExtension.getUrl(vanID),
                enCounterExtension.getStringType(userInfo!!.vanId.toString()) ) )

            encounter.addExtension( enCounterExtension.getExtenstion(
                enCounterExtension.getUrl(parkingPlaceID),
                enCounterExtension.getStringType(userInfo!!.parkingPlaceId.toString()) ) )

            encounter.addExtension( enCounterExtension.getExtenstion(
                enCounterExtension.getUrl(providerServiceMapId),
                enCounterExtension.getStringType(userInfo!!.serviceMapId.toString()) ) )

            encounter.addExtension( enCounterExtension.getExtenstion(
                enCounterExtension.getUrl(createdBy),
                enCounterExtension.getStringType(userInfo!!.userName) ) )
        }
    }

    private fun addChiefComplaintsData(): Boolean {
        // get all the ChiefComplaint data from list and convert that to fhir resource
        for (i in 0 until itemList.size) {
            val chiefComplaintData = itemList[i]
            if(chiefComplaintData.chiefComplaint.isEmpty()){
                if(catBool && subCat) Toast.makeText(requireContext(), resources.getString(R.string.toast_msg_chief), Toast.LENGTH_SHORT).show()
                return false
            }
            if(chiefComplaintData.duration.isEmpty()){
                if(catBool && subCat) Toast.makeText(requireContext(), resources.getString(R.string.toast_msg_duration), Toast.LENGTH_SHORT).show()
                return false
            }
            if(chiefComplaintData.durationUnit.isEmpty()){
                if(catBool && subCat) Toast.makeText(requireContext(), resources.getString(R.string.toast_msg_duration_unit), Toast.LENGTH_SHORT).show()
                return false
            }

            if (chiefComplaintData.chiefComplaint.isNotEmpty() &&
                chiefComplaintData.duration.isNotEmpty() &&
                chiefComplaintData.durationUnit.isNotEmpty() &&
                chiefComplaintData.description.isNotEmpty()
            ) {

                // Creating the "Condition" resource
                val condition = Condition()

                // Set the code for the chief complaint
                val chiefComplaint = Coding()
                chiefComplaint.system =
                    "http://snomed.info/sct"
                chiefComplaint.code = chiefComplaintData.id.toString()
                chiefComplaint.display = chiefComplaintData.chiefComplaint
                condition.code = CodeableConcept().addCoding(chiefComplaint)

                // Set the note for the description
                val note = Annotation()
                note.text = chiefComplaintData.description
                condition.note = listOf(note)

                //set subject to condition
                val ref = Reference("give here reg/ben-reg Id")
                condition.subject = ref

                // calling this addExtensionsToConditionResources() method to add van,parking, providerServiceMapId and duration extension
                addExtensionsToConditionResources(condition, chiefComplaintData)
                listOfConditions.add(condition)
            }
        }
        return true
    }

    private fun addExtensionsToConditionResources(
        condition: Condition,
        chiefComplaintValues: ChiefComplaintValues
    ) {
        if (userInfo != null) {
            condition.addExtension(
                conditionExtension.getExtenstion(
                    conditionExtension.getUrl(duration),
                    conditionExtension.getCoding(chiefComplaintValues.durationUnit, chiefComplaintValues.duration)))

            condition.addExtension( conditionExtension.getExtenstion(
                conditionExtension.getUrl(vanID),
                conditionExtension.getStringType(userInfo!!.vanId.toString()) ) )

            condition.addExtension( conditionExtension.getExtenstion(
                conditionExtension.getUrl(parkingPlaceID),
                conditionExtension.getStringType(userInfo!!.parkingPlaceId.toString()) ) )

            condition.addExtension( conditionExtension.getExtenstion(
                conditionExtension.getUrl(providerServiceMapId),
                conditionExtension.getStringType(userInfo!!.serviceMapId.toString()) ) )

            condition.addExtension( conditionExtension.getExtenstion(
                conditionExtension.getUrl(createdBy),
                conditionExtension.getStringType(userInfo!!.userName) ) )
        }
    }

    override fun getFragmentId(): Int {
        return R.id.fragment_visit_details_info
    }

    override fun onSubmitAction() {
        navigateNext()
    }

    override fun onCancelAction() {
//        val intent = Intent(context, WebViewActivity::class.java)
//        intent.putExtra("patientId", patientId);
//        startActivity(intent)
//        callLoginDialog()
    }

    //methods for voice to text conversion and update the input fields
    override fun onEndIconDurationClick(position: Int) {
        speechToTextLauncherForDuration.launch(Unit)
        currDurationPos = position
    }
    private fun updateDurationText(duration:String){
       if(currDurationPos != -1) {
           itemList[currDurationPos].duration = duration
           adapter.notifyItemChanged(currDurationPos)
       }
    }
    private fun updateDescText(desc: String){
        if(currDescPos != -1) {
            itemList[currDescPos].description = desc
            adapter.notifyItemChanged(currDescPos)
        }
    }

    override fun onEndIconDescClick(position: Int) {
        speechToTextLauncherForDesc.launch(Unit)
        currDescPos = position
    }
    private fun updateChiefText(chief: String){
        if(currChiefPos != -1) {
            itemList[currChiefPos].chiefComplaint = chief
            adapter.notifyItemChanged(currChiefPos)
        }
    }
    override fun onEndIconChiefClick(position: Int) {
        speechToTextLauncherForChiefMaster.launch(Unit)
        currChiefPos = position
    }
}