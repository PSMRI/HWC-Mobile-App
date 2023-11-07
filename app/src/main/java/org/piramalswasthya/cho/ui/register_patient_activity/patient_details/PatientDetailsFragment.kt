package org.piramalswasthya.cho.ui.register_patient_activity.patient_details

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.VillageDropdownAdapter
import org.piramalswasthya.cho.adapter.dropdown_adapters.DropdownAdapter
import org.piramalswasthya.cho.adapter.model.DropdownList
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.AlertAgePickerBinding
import org.piramalswasthya.cho.databinding.FragmentPatientDetailsBinding
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientAadhaarDetails
import org.piramalswasthya.cho.model.VillageLocationData
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.SpeechToTextContract
import org.piramalswasthya.cho.ui.register_patient_activity.scanAadhaar.ScanAadhaarActivity
import org.piramalswasthya.cho.utils.DateTimeUtil
import org.piramalswasthya.cho.utils.ImgUtils
import org.piramalswasthya.cho.utils.generateUuid
import org.piramalswasthya.cho.utils.setBoxColor
import org.piramalswasthya.cho.work.WorkerUtils
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import timber.log.Timber
import java.io.File
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class PatientDetailsFragment : Fragment() , NavigationAdapter {

    @Inject
    lateinit var preferenceDao: PreferenceDao

    private val binding by lazy{
        FragmentPatientDetailsBinding.inflate(layoutInflater)
    }

    private lateinit var viewModel: PatientDetailsViewModel

    private var doAgeToDob = true;
    private var patient = Patient();
    private lateinit var villageAdapter :VillageDropdownAdapter
    private var villageList = ArrayList<VillageLocationData>()
    private var villageListFilter = ArrayList<VillageLocationData>()
    private val dobUtil : DateTimeUtil = DateTimeUtil()
    var bool: Boolean = false
    private var currentFileName: String? = null
    private var currentPhotoPath: String? = null
    private lateinit var  photoURI: Uri

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.ivImgCapture.setOnClickListener {
            checkAndRequestCameraPermission()
        }
        scanCode()

        return binding.root
    }
    @RequiresApi(Build.VERSION_CODES.P)
    private fun checkAndRequestCameraPermission() {
        if (checkSelfPermission(requireContext(),Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(requireContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE
            )  == PackageManager.PERMISSION_GRANTED
        ) {
            // Camera permission is granted, proceed to take a picture
            takePicture()
        } else {
            // Camera permission is not granted, request it
            requestCameraPermission()
        }
    }
    private fun requestCameraPermission() {
        val permission = arrayOf<String>(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE )
        requestPermissions(permission, 112)
    }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { result: Boolean ->
            if (result) {
                // Picture was taken successfully, update the ImageView with the captured image
                if (photoURI == null)
                    binding.ivImgCapture.setImageResource(R.drawable.ic_person)
                else {
                    Glide.with(this).load(photoURI).placeholder(R.drawable.ic_person).circleCrop()
                        .into(binding.ivImgCapture)
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun takePicture() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: Exception) {
            null
        }

        photoFile?.also {
            photoURI = FileProvider.getUriForFile(
                requireContext(),
                "org.piramalswasthya.cho.provider",
                it
            )
            takePictureLauncher.launch(photoURI)

        }
    }
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir("images")
        currentFileName = "JPEG_${timeStamp}_.jpeg"
        var file = File(storageDir, currentFileName)

        // Ensure the file doesn't already exist
        var counter = 1
        while (file.exists()) {
            currentFileName = "JPEG_${timeStamp}_$counter.jpeg"
            file = File(storageDir, currentFileName)
            counter++
        }

        return file.apply {
            // Save a file path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }

    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 112) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can proceed to open the camera
                takePicture()
            } else {
                Toast.makeText(requireContext(), getString(R.string.permission_to_access_the_camera_denied), Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this).get(PatientDetailsViewModel::class.java)
        hideMarriedFields()
        setChangeListeners()
        setAdapters()
//        villageAdapter = VillageDropdownAdapter(
//            context = requireContext(),
//           resource = R.layout.drop_down,
//            dataList = viewModel.villageList,
//            autoCompleteTextView = binding.villageDropdown,
////            dataListConst = viewModel.villageListFilter
//        )
//        binding.villageDropdown.setAdapter(villageAdapter)



        binding.firstNameText.setEndIconOnClickListener {
            speechToTextLauncherForFirstName.launch(Unit)
        }
        binding.lastNameText.setEndIconOnClickListener {
            speechToTextLauncherForLastName.launch(Unit)
        }
//        binding.ageText.setEndIconOnClickListener {
//            speechToTextLauncherForAge.launch(Unit)
//        }
        binding.phoneNoText.setEndIconOnClickListener {
            speechToTextLauncherForPhoneNumber.launch(Unit)
        }
        binding.spouseNameText.setEndIconOnClickListener {
            speechToTextLauncherForSpouseName.launch(Unit)
        }
        binding.fatherNameText.setEndIconOnClickListener {
            speechToTextLauncherForFatherName.launch(Unit)
        }

        binding.age.setOnClickListener {
            ageAlertDialog.findViewById<NumberPicker>(R.id.dialog_number_picker_years)?.value = viewModel.enteredAgeYears!!
            ageAlertDialog.findViewById<NumberPicker>(R.id.dialog_number_picker_months)?.value = viewModel.enteredAgeMonths!!
            ageAlertDialog.findViewById<NumberPicker>(R.id.dialog_number_picker_weeks)?.value = viewModel.enteredAgeWeeks!!
            ageAlertDialog.findViewById<NumberPicker>(R.id.dialog_number_picker_days)?.value = viewModel.enteredAgeDays!!
            ageAlertDialog.show()
        }

    }

    private val ageAlertDialog by lazy {
        val alertBinding = AlertAgePickerBinding.inflate(layoutInflater,binding.root,false)
        alertBinding.dialogNumberPickerYears.minValue = 0
        alertBinding.dialogNumberPickerYears.maxValue = 200

        alertBinding.dialogNumberPickerMonths.minValue = 0
        alertBinding.dialogNumberPickerMonths.maxValue = 11

        alertBinding.dialogNumberPickerWeeks.minValue = 0
        alertBinding.dialogNumberPickerWeeks.maxValue = 4       // Assuming a maximum of 4 weeks in a month

        alertBinding.dialogNumberPickerDays.minValue = 0
        alertBinding.dialogNumberPickerDays.maxValue = 6

        val alert = MaterialAlertDialogBuilder(requireContext())
            .setView(alertBinding.root)
            .create()

        alertBinding.btnOk.setOnClickListener {
            viewModel.enteredAgeYears = alertBinding.dialogNumberPickerYears.value
            viewModel.enteredAgeMonths = alertBinding.dialogNumberPickerMonths.value
            viewModel.enteredAgeWeeks = alertBinding.dialogNumberPickerWeeks.value
            viewModel.enteredAgeDays = alertBinding.dialogNumberPickerDays.value

            setAgeToDateOfBirth()

            if(viewModel.enteredAgeYears != 0){
                viewModel.enteredAge = viewModel.enteredAgeYears
                viewModel.selectedAgeUnit = viewModel.ageUnitList[2]
            }else if(viewModel.enteredAgeMonths != 0){
                viewModel.enteredAge = viewModel.enteredAgeMonths
                viewModel.selectedAgeUnit = viewModel.ageUnitList[1]
            }else if(viewModel.enteredAgeWeeks != 0){
                viewModel.enteredAge = viewModel.enteredAgeWeeks
                viewModel.selectedAgeUnit = viewModel.ageUnitList[3]
            }else{
                viewModel.enteredAge = viewModel.enteredAgeDays
                viewModel.selectedAgeUnit = viewModel.ageUnitList[0]
            }
            binding.age.text = Editable.Factory.getInstance().newEditable(viewModel.enteredAge!!.toString())
            viewModel.selectedAgeUnitEnum = viewModel.ageUnitEnumMap[viewModel.selectedAgeUnit]
            binding.ageInUnitDropdown.setText(viewModel.ageUnitMap[viewModel.selectedAgeUnitEnum]?.name ?: "", false)
            alert.dismiss()
        }
        alertBinding.btnCancel.setOnClickListener {
            alert.cancel()
        }
        alert.setOnShowListener {
            alertBinding.dialogNumberPickerYears.value =  viewModel.enteredAgeYears!!
            alertBinding.dialogNumberPickerMonths.value = viewModel.enteredAgeMonths!!
            alertBinding.dialogNumberPickerWeeks.value = viewModel.enteredAgeWeeks!!
            alertBinding.dialogNumberPickerDays.value =  viewModel.enteredAgeDays!!
        }
        alert
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun scanCode() {
        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == 2) {
                val scannedData = result.data?.getStringExtra("data")
                if (scannedData != null) {
                    val userData = parseUserData(scannedData)

                    val nameParts = userData.name?.split(" ")
                    val firstName = nameParts?.get(0)
                    val lastName = nameParts?.get(nameParts.size - 1)
                    binding.firstName.text =
                        Editable.Factory.getInstance().newEditable(firstName ?: "")
                    binding.lastName.text =
                        Editable.Factory.getInstance().newEditable(lastName ?: "")

                    val inputDateFormat1 = SimpleDateFormat("dd/MM/yyyy")
                    val inputDateFormat2 = SimpleDateFormat("dd-MM-yyyy")
                    val inputDateFormat3 = SimpleDateFormat("yyyy-MM-dd")
                    val inputDateFormat4 = SimpleDateFormat("yyyy/MM/dd")

                    val outputDateFormat = SimpleDateFormat("yyyy-MM-dd")

                    if (!userData.dateOfBirth.isNullOrEmpty()) {
                        var date: Date? = null
                        if(userData.dateOfBirth[2].toString()=="/") {
                            date =
                                userData.dateOfBirth.let { inputDateFormat1.parse(it) } as Date
                        }else if(userData.dateOfBirth[2].toString()=="-"){
                            date = userData.dateOfBirth.let { inputDateFormat2.parse(it) } as Date
                        }
                        else if(userData.dateOfBirth[4].toString()=="-"){
                            date = userData.dateOfBirth.let { inputDateFormat3.parse(it) } as Date
                        } else if(userData.dateOfBirth[4].toString()=="/"){
                            date = userData.dateOfBirth.let { inputDateFormat4.parse(it) } as Date
                        }
                        if(date!=null) {
                            val outputDateStr: String = outputDateFormat.format(date)
                            val outputDate: Date = outputDateFormat.parse(outputDateStr) as Date

                            viewModel.selectedDateOfBirth = outputDate

                            dobUtil.showDatePickerDialog(
                                requireContext(),
                                viewModel.selectedDateOfBirth
                            )
                        }

                    }
                    if (!userData.gender.isNullOrEmpty()){
                        when (userData.gender) {
                            "M" -> {
                                viewModel.selectedGenderMaster = viewModel.genderMasterList[0]
                                binding.genderDropdown.setText(
                                    viewModel.selectedGenderMaster!!.genderName,
                                    false
                                )
                            }

                            "F" -> {
                                viewModel.selectedGenderMaster = viewModel.genderMasterList[1]
                                binding.genderDropdown.setText(
                                    viewModel.selectedGenderMaster!!.genderName,
                                    false
                                )
                            }

                            else -> {
                                viewModel.selectedGenderMaster = viewModel.genderMasterList[2]
                                binding.genderDropdown.setText(
                                    viewModel.selectedGenderMaster!!.genderName,
                                    false
                                )
                            }
                        }
                    }
                    if(!userData.mobileNumber.isNullOrEmpty()){
                        binding.phoneNo.setText(userData.mobileNumber)
                    }
                }
            }
        }

        binding.btnScanAadhaar.setOnClickListener {
            resultLauncher.launch(Intent(requireContext(), ScanAadhaarActivity::class.java))
        }
    }

    private fun parseUserData(xmlData: String):PatientAadhaarDetails {
        val xml = XmlPullParserFactory.newInstance().newPullParser()
        xml.setInput(StringReader(xmlData))

        var name = ""
        var gender = ""
        var dateOfBirth = ""
        var mobileNumber = ""
        try {
            while (xml.eventType != XmlPullParser.END_DOCUMENT) {
                when (xml.eventType) {
                    XmlPullParser.START_TAG -> {
                        when (xml.name) {
                            "QPDB" -> {
                                name = xml.getAttributeValue(null, "n")
                                gender = xml.getAttributeValue(null, "g")
                                mobileNumber = xml.getAttributeValue(null, "m")
                                dateOfBirth = xml.getAttributeValue(null, "d")
                            }

                            "PrintLetterBarcodeData" -> {
                                name = xml.getAttributeValue(null, "name")
                                gender = xml.getAttributeValue(null, "gender")
                                dateOfBirth = xml.getAttributeValue(null, "dob")

                            }
//                            else->{}
                        }
                    }
                }
                xml.next()
            }

        }catch (e:Exception){
            Toast.makeText(context, "Unable to fetch details", Toast.LENGTH_SHORT).show()
        }
        return PatientAadhaarDetails(name, gender,mobileNumber, dateOfBirth)
    }

    private val speechToTextLauncherForFirstName = registerForActivityResult(SpeechToTextContract()) { result ->
        if (result.isNotBlank() && result.isNotEmpty() && !result.any { it.isDigit() }) {
            binding.firstName.setText(result)
        }
    }
    private val speechToTextLauncherForLastName = registerForActivityResult(SpeechToTextContract()) { result ->
        if (result.isNotBlank() && result.isNotEmpty() && !result.any { it.isDigit() }) {
            binding.lastName.setText(result)
        }
    }
    private val speechToTextLauncherForSpouseName = registerForActivityResult(SpeechToTextContract()) { result ->
        if (result.isNotBlank() && result.isNotEmpty() && !result.any { it.isDigit() }) {
            binding.spouseName.setText(result)
        }
    }
    private val speechToTextLauncherForAge = registerForActivityResult(SpeechToTextContract()) { result ->
        if (result.isNotBlank() && result.isNumeric()) {
            val pattern = "\\d{2}".toRegex()
            val match = pattern.find(result)
            val firstTwoDigits = match?.value
            if(result.toInt() > 0) binding.age.setText(result)
        }
    }
    private val speechToTextLauncherForFatherName = registerForActivityResult(SpeechToTextContract()) { result ->
        if (result.isNotBlank() && result.isNotEmpty() && !result.any { it.isDigit() }) {
            binding.fatherNameEditText.setText(result)
        }
    }
    private fun String.isNumeric(): Boolean {
        return try { this.toDouble()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }
    private val speechToTextLauncherForPhoneNumber = registerForActivityResult(SpeechToTextContract()) { result ->
        if (result.isNotBlank()) {
            val cleanedResult = result.replace("\\s".toRegex(), "") // Remove all spaces
            val last10Digits = if (cleanedResult.length >= 10) cleanedResult.substring(0,10) else cleanedResult
            binding.phoneNo.setText(last10Digits)
        }
    }
    fun watchAllFields(){
        if (!viewModel.isClickedSS.value!!) {
            viewModel.firstNameVal.observe(viewLifecycleOwner) {
                binding.firstNameText.setBoxColor(it, resources.getString(R.string.enter_your_first_name))
            }
//            viewModel.lastNameVal.observe(viewLifecycleOwner) {
//                binding.lastNameText.setBoxColor(it, resources.getString(R.string.enter_last_name))
//            }
            viewModel.dobVal.observe(viewLifecycleOwner) {
                binding.dateOfBirthText.setBoxColor(it, resources.getString(R.string.fill_dob))
            }
            viewModel.ageVal.observe(viewLifecycleOwner) {
                binding.ageText.setBoxColor(it, resources.getString(R.string.enter_your_age))
            }
            viewModel.ageInUnitVal.observe(viewLifecycleOwner) {
                binding.ageInUnitText.setBoxColor(it, resources.getString(R.string.select_age_in_unit))
            }
//            viewModel.maritalStatusVal.observe(viewLifecycleOwner) {
//                binding.maritalStatusText.setBoxColor(it,resources.getString(R.string.select_mariital_status))
//            }
//            viewModel.spouseNameVal.observe(viewLifecycleOwner) {
//                binding.spouseNameText.setBoxColor(it, resources.getString(R.string.enter_spouse_name))
//            }
//            viewModel.ageAtMarraigeVal.observe(viewLifecycleOwner) {
//                binding.ageAtMarriageText.setBoxColor(it, resources.getString(R.string.enter_age_at_marriage))
//            }
//            binding.phoneNoText.setBoxColor(false, resources.getString(R.string.enter_a_valid_phone_number))
//            viewModel.phoneN.observe(viewLifecycleOwner) {
//                Timber.d("phone nimber ${it?.reason}")
//                    binding.phoneNoText.setBoxColor(it.boolean,it.reason)
//            }
            viewModel.genderVal.observe(viewLifecycleOwner) {
                binding.genderText.setBoxColor(it, resources.getString(R.string.select_gender))
            }
            viewModel.villageBoolVal.observe(viewLifecycleOwner) {
                binding.villageText.setBoxColor(it, resources.getString(R.string.select_village))
            }
            viewModel.setIsClickedSS(true)
        }
    }


    private fun hideMarriedFields(){
        binding.maritalStatusText.visibility = View.GONE
        binding.spouseNameText.visibility = View.GONE
        binding.ageAtMarriageText.visibility = View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setMarriedFieldsVisibility(){
        if(viewModel.selectedDateOfBirth != null){
            bool = DateTimeUtil.calculateAgeInYears(viewModel.selectedDateOfBirth!!) >= 11
            viewModel.setAgeGreaterThan11(bool)
            viewModel
            if(bool) {
                binding.maritalStatusText.visibility = View.VISIBLE
                if (viewModel.selectedMaritalStatus != null && viewModel.selectedMaritalStatus!!.status.lowercase() == "married") {
                    binding.spouseNameText.visibility = View.VISIBLE
                    binding.ageAtMarriageText.visibility = View.VISIBLE
                } else {
                    binding.spouseNameText.visibility = View.GONE
                    binding.ageAtMarriageText.visibility = View.GONE
                }
            }
            else
                hideMarriedFields()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setChangeListeners(){
//        binding.ageInUnitDropdown.setOnItemClickListener { parent, _, position, _ ->
//            viewModel.selectedAgeUnit = viewModel.ageUnitList[position];
//            viewModel.selectedAgeUnitEnum = viewModel.ageUnitEnumMap[viewModel.selectedAgeUnit]
//            binding.ageInUnitDropdown.setText(viewModel.selectedAgeUnit!!.name, false)
//            setAgeToDateOfBirth()
//        }

        binding.maritalStatusDropdown.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectedMaritalStatus = viewModel.maritalStatusList[position];
            binding.maritalStatusDropdown.setText(viewModel.selectedMaritalStatus!!.status, false)
//            setMarriedFieldsVisibility()
        }

        binding.genderDropdown.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectedGenderMaster = viewModel.genderMasterList[position];
            binding.genderDropdown.setText(viewModel.selectedGenderMaster!!.genderName, false)
        }
        binding.villageDropdown.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectedVillage = parent.getItemAtPosition(position) as VillageLocationData
            binding.villageDropdown.setText(viewModel.selectedVillage!!.villageName, false)
        }

        binding.dateOfBirth.setOnClickListener {
            dobUtil.showDatePickerDialog(requireContext(), viewModel.selectedDateOfBirth).show()
        }

        binding.age.addTextChangedListener(ageTextWatcher)

        binding.ageInUnitDropdown.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed in this case
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                setMarriedFieldsVisibility()
                val isAgeInUnitFilled = s?.isNotEmpty() == true
                viewModel.setAgeUnit(isAgeInUnitFilled)
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed in this case
            }
        })
        binding.firstName.addTextChangedListener (object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isDobFilled = s?.isNotEmpty() == true // Check if not empty
                viewModel.setFirstName(isDobFilled) // Update LiveData
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.lastName.addTextChangedListener (object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isDobFilled = s?.isNotEmpty() == true // Check if not empty
                viewModel.setLastName(isDobFilled) // Update LiveData
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.dateOfBirth.addTextChangedListener (object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isDobFilled = s?.isNotEmpty() == true // Check if not empty
                viewModel.setDob(isDobFilled) // Update LiveData
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.age.addTextChangedListener (object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val isDobFilled = s?.isNotEmpty() == true // Check if not empty
                if (!s.isNullOrBlank() && s.toString().toInt() != 0)
                    viewModel.setAge(isDobFilled) // Update LiveData
            }

            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrBlank() && s.toString().toInt() == 0) s.clear()

            }
        })
        binding.ageInUnitDropdown.addTextChangedListener (object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isDobFilled = s?.isNotEmpty() == true // Check if not empty
                viewModel.setAgeUnit(isDobFilled) // Update LiveData
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.genderDropdown.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isGenderFilled = s?.isNotEmpty() == true // Check if not empty
                viewModel.setGender(isGenderFilled) // Update LiveData
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.villageDropdown.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val isVillageFilled = s?.isNotEmpty() == true // Check if not empty
                viewModel.setVillageBool(isVillageFilled) // Update LiveData
                villageAdapter.notifyDataSetChanged()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.phoneNo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isPhoneNumberFilled = s?.isNotEmpty() == true
                if(isPhoneNumberFilled){
                    isValidPhoneNumber(s.toString().trim())
                }
                else{
                    viewModel.setPhoneN(true, "null")
                }
                binding.phoneNoText.setBoxColor(false, resources.getString(R.string.enter_a_valid_phone_number))
                viewModel.phoneN.observe(viewLifecycleOwner) {
                    Timber.d("phone nimber ${it?.reason}")
                    binding.phoneNoText.setBoxColor(it.boolean,it.reason)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.maritalStatusDropdown.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isMaritalStatusFilled = s?.isNotEmpty() == true // Check if not empty
                viewModel.setMarital(isMaritalStatusFilled) // Update LiveData
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.spouseName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isSpouseNameFilled = s?.isNotEmpty() == true // Check if not empty
                viewModel.setSpouse(isSpouseNameFilled) // Update LiveData
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.ageAtMarriage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isAgeAtMarriageFilled = s?.isNotEmpty() == true // Check if not empty
                viewModel.setMaritalAge(isAgeAtMarriageFilled) // Update LiveData
            }

            override fun afterTextChanged(s: Editable?) {}
        })

    }



    private fun isValidPhoneNumber(phoneNumber: String) {
        var char = phoneNumber.get(0)

        if(char.equals('9') || char.equals('8') || char.equals('7') || char.equals('6')) {
            Log.d("aryan","${char}")
            if (phoneNumber.length == 10 && phoneNumber.matches(Regex("\\d+"))) {
                if (isNotRepeatableNumber(phoneNumber)) {
                    viewModel.setPhoneN(true, "null")
                } else {
                    viewModel.setPhoneN(false, resources.getString(R.string.enter_a_valid_phone_number))
                }
            } else {
                viewModel.setPhoneN(false,  resources.getString(R.string.enter_a_valid_phone_number))
            }
        }else{
            viewModel.setPhoneN(false,  resources.getString(R.string.enter_a_valid_phone_number))
        }
    }
    fun isNotRepeatableNumber(input: String): Boolean {

        val digits = input.toCharArray()

        for (i in 1 until digits.size) {
            if (digits[i] != digits[0]) {
                return true
            }
        }
        return false
    }
    private val ageTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            setAgeToDateOfBirth()
            val isAgeFilled = s?.isNotEmpty() == true
            viewModel.setAge(isAgeFilled)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun afterTextChanged(s: Editable?) {
//            setMarriedFieldsVisibility()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setAdapters(){
//        viewModel.ageUnit.observe(viewLifecycleOwner) { state ->
//            when (state!!){
//                PatientDetailsViewModel.NetworkState.SUCCESS -> {
//                    val dropdownList = viewModel.ageUnitList.map { it -> DropdownList(it.id, it.name) }
//                    val dropdownAdapter = DropdownAdapter(requireContext(), R.layout.drop_down, dropdownList, binding.ageInUnitDropdown)
//                    binding.ageInUnitDropdown.setAdapter(dropdownAdapter)
//                    binding.ageInUnitDropdown.setText("Years",false)
//                }
//                else -> {
//
//                }
//            }
//        }
        viewModel.villageVal.observe(viewLifecycleOwner) { state ->
            when (state!!){
                PatientDetailsViewModel.NetworkState.SUCCESS -> {
//                    val dropdownList = viewModel.villageList.map { it -> DropdownList(it.districtBranchID.toInt(), it.villageName) }
//                    val dropdownAdapter = DropdownAdapter(requireContext(), R.layout.drop_down, dropdownList, binding.villageDropdown)
                    villageAdapter = VillageDropdownAdapter(
                        requireContext(),
                        R.layout.drop_down,
                        viewModel.villageList,
                        binding.villageDropdown,
                        viewModel.villageListFilter
                    )
                    binding.villageDropdown.setAdapter(villageAdapter)
                }
                else -> {

                }
            }
        }

        viewModel.maritalStatus.observe(viewLifecycleOwner) { state ->
            when (state!!){
                PatientDetailsViewModel.NetworkState.SUCCESS -> {
                    val dropdownList = viewModel.maritalStatusList.map { it -> DropdownList(it.maritalStatusID, it.status) }
                    val dropdownAdapter = DropdownAdapter(requireContext(), R.layout.drop_down, dropdownList, binding.maritalStatusDropdown)
                    binding.maritalStatusDropdown.setAdapter(dropdownAdapter)
                }
                else -> {

                }
            }
        }

        viewModel.genderMaster.observe(viewLifecycleOwner) { state ->
            when (state!!){
                PatientDetailsViewModel.NetworkState.SUCCESS -> {
                    val dropdownList = viewModel.genderMasterList.map { it -> DropdownList(it.genderID, it.genderName) }
                    val dropdownAdapter = DropdownAdapter(requireContext(), R.layout.drop_down, dropdownList, binding.genderDropdown)
                    binding.genderDropdown.setAdapter(dropdownAdapter)
                }
                else -> {

                }
            }
        }

        dobUtil.selectedDate.observe(viewLifecycleOwner) { date ->
            if(date != null){
                setDateOfBirthToAge(date);
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setAgeToDateOfBirth(){
        viewModel.enteredAge = binding.age.text.toString().trim().toIntOrNull()
//        if(viewModel.enteredAge != null && viewModel.selectedAgeUnitEnum != null && doAgeToDob){
//            viewModel.selectedDateOfBirth = DateTimeUtil.calculateDateOfBirth(viewModel.enteredAge!!, viewModel.selectedAgeUnitEnum!!);
            viewModel.selectedDateOfBirth = DateTimeUtil.calculateDateOfBirth(viewModel.enteredAgeYears!!, viewModel.enteredAgeMonths!!,
                viewModel.enteredAgeWeeks!!, viewModel.enteredAgeDays!!);
            binding.dateOfBirth.setText(DateTimeUtil.formattedDate(viewModel.selectedDateOfBirth!!))
//            setMarriedFieldsVisibility()
//        }
        doAgeToDob = true;
    }


//    @RequiresApi(Build.VERSION_CODES.O)
//    fun setDateOfBirthToAge(date: Date){
//        val age = DateTimeUtil.calculateAge(date);
//        viewModel.enteredAge = age.value
//        viewModel.selectedDateOfBirth = date
//        viewModel.selectedAgeUnitEnum = age.unit
//        viewModel.selectedAgeUnit = viewModel.ageUnitMap[age.unit]
//        doAgeToDob = false;
//        binding.age.setText(age.value.toString())
//        binding.ageInUnitDropdown.setText(viewModel.ageUnitMap[age.unit]?.name ?: "", false)
//        binding.dateOfBirth.setText(DateTimeUtil.formattedDate(date))
//    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setDateOfBirthToAge(date: Date){
        val age = DateTimeUtil.calculateAgePicker(date);
//        viewModel.enteredAge = age.value
        viewModel.enteredAgeYears = age.years
        viewModel.enteredAgeMonths = age.months
        viewModel.enteredAgeWeeks = age.weeks
        viewModel.enteredAgeDays = age.days



        if(viewModel.enteredAgeYears != 0){
            viewModel.enteredAge = viewModel.enteredAgeYears
            viewModel.selectedAgeUnit = viewModel.ageUnitList[2]
        }else if(viewModel.enteredAgeMonths != 0){
            viewModel.enteredAge = viewModel.enteredAgeMonths
            viewModel.selectedAgeUnit = viewModel.ageUnitList[1]
        }else if(viewModel.enteredAgeWeeks != 0){
            viewModel.enteredAge = viewModel.enteredAgeWeeks
            viewModel.selectedAgeUnit = viewModel.ageUnitList[3]
        }else{
            viewModel.enteredAge = viewModel.enteredAgeDays
            viewModel.selectedAgeUnit = viewModel.ageUnitList[0]
        }
        binding.age.text = Editable.Factory.getInstance().newEditable(viewModel.enteredAge!!.toString())
        viewModel.selectedAgeUnitEnum = viewModel.ageUnitEnumMap[viewModel.selectedAgeUnit]
        binding.ageInUnitDropdown.setText(viewModel.ageUnitMap[viewModel.selectedAgeUnitEnum]?.name ?: "", false)

        viewModel.selectedDateOfBirth = date
//        viewModel.selectedAgeUnitEnum = age.unit
//        viewModel.selectedAgeUnit = viewModel.ageUnitMap[age.unit]
        doAgeToDob = false;
//        binding.age.setText(age.value.toString())
//        binding.ageInUnitDropdown.setText(viewModel.ageUnitMap[age.unit]?.name ?: "", false)
        binding.dateOfBirth.setText(DateTimeUtil.formattedDate(date))

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setPatientDetails(){
        patient.firstName = binding.firstName.text.toString().trim()
        patient.lastName = binding.lastName.text.toString().trim()
        patient.dob = viewModel.selectedDateOfBirth;
        patient.age = viewModel.enteredAge;
        patient.ageUnitID = viewModel.selectedAgeUnit?.id
        patient.parentName = binding.fatherNameEditText.text.toString().trim()
//        patient.maritalStatusID = viewModel.selectedMaritalStatus?.maritalStatusID
//        patient.spouseName = when(viewModel.selectedMaritalStatus?.status?.lowercase()){
//            "married" -> binding.spouseName.text.toString();
//            else -> null
//        }
//        patient.ageAtMarriage = when(viewModel.selectedMaritalStatus?.status?.lowercase()){
//            "married" -> binding.ageAtMarriage.text.toString().toIntOrNull();
//            else -> null
//        }
//        patient.phoneNo = binding.phoneNo.text.toString()
        if (binding.phoneNo.text.toString().isNullOrEmpty()) {
            patient.phoneNo = null
        } else {
            patient.phoneNo = binding.phoneNo.text.toString()
        }
        patient.genderID = viewModel.selectedGenderMaster?.genderID
        patient.registrationDate = Date()
        patient.benImage = ImgUtils.getEncodedStringForBenImage(requireContext(), currentFileName)
    }

    private fun setLocationDetails(){
        val locData = preferenceDao.getUserLocationData()
        patient.stateID = locData?.stateId
        patient.districtID = locData?.districtId
        patient.blockID = locData?.blockId
        patient.districtBranchID = viewModel.selectedVillage?.districtBranchID!!.toInt()
    }
    override fun getFragmentId(): Int {
        return R.id.fragment_add_patient_location;
    }
    fun checkVisibleFieldIsEmpty():Boolean{
        if(!viewModel.firstNameVal.value!! || !viewModel.dobVal.value!! || !viewModel.ageVal.value!! || !viewModel.genderVal.value!! || !viewModel.villageBoolVal.value!! ){
            return false
        }
//        if(viewModel.ageGreaterThan11.value!!){
//            if (!viewModel.maritalStatusVal.value!! ){
//                return false
//            }
//            else{
//                if(viewModel.selectedMaritalStatus!!.status.lowercase() == "married"){
//                    if(!viewModel.ageAtMarraigeVal.value!! || !viewModel.spouseNameVal.value!!){
//                        return false
//                    }
//                }
//            }
//        }
        return true
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSubmitAction() {
        watchAllFields()
        if (checkVisibleFieldIsEmpty()) {
            setPatientDetails()
            setLocationDetails()
            patient.patientID = generateUuid()
            viewModel.insertPatient(patient)
            viewModel.isDataSaved.observe(viewLifecycleOwner){ state ->
                when(state!!){
                    true -> {
                        WorkerUtils.triggerAmritSyncWorker(requireContext())
                        requireActivity().finish()
                        Toast.makeText(requireContext(), getString(R.string.patient_registered_successfully), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
//                        Toast.makeText(requireContext(), getString(R.string.something_wend_wong), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCancelAction() {
        requireActivity().finish()
    }

}

