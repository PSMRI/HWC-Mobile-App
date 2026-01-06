package org.piramalswasthya.cho.ui.register_patient_activity.patient_details

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.VillageDropdownAdapter
import org.piramalswasthya.cho.adapter.dropdown_adapters.DropdownAdapter
import org.piramalswasthya.cho.adapter.model.DropdownList
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.AlertAgePickerBinding
import org.piramalswasthya.cho.databinding.FragmentPatientDetailsBinding
import org.piramalswasthya.cho.facenet.FaceNetModel
import org.piramalswasthya.cho.facenet.Models
import org.piramalswasthya.cho.facenet.SharedViewModel
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientAadhaarDetails
import org.piramalswasthya.cho.model.VillageLocationData
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.SpeechToTextContract
import org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity
import org.piramalswasthya.cho.ui.register_patient_activity.scanAadhaar.ScanAadhaarActivity
import org.piramalswasthya.cho.utils.DateTimeUtil
import org.piramalswasthya.cho.utils.ImgUtils
import org.piramalswasthya.cho.utils.generateUuid
import org.piramalswasthya.cho.utils.setBoxColor
import org.piramalswasthya.cho.utils.setupDropdownKeyboardHandling
import org.piramalswasthya.cho.utils.KeyboardUtils
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
import android.text.InputType
import android.view.inputmethod.InputMethodManager


@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class PatientDetailsFragment : Fragment() , NavigationAdapter {

    @Inject
    lateinit var preferenceDao: PreferenceDao

    private val binding by lazy{
        FragmentPatientDetailsBinding.inflate(layoutInflater)
    }

    private lateinit var viewModel: PatientDetailsViewModel

    private var doAgeToDob = true
    private var patient = Patient()
    private lateinit var villageAdapter :VillageDropdownAdapter
    private var isSettingVillageProgrammatically = false
    private val dobUtil : DateTimeUtil = DateTimeUtil()
    var bool: Boolean = false
    private var currentFileName: String? = null
    private var currentPhotoPath: String? = null
    private lateinit var  photoURI: Uri

    //facenet
    private val useGpu = false
    private val useXNNPack = true
    private val modelInfo = Models.FACENET
    private lateinit var faceNetModel : FaceNetModel
    private var embeddings: FloatArray? = null
    private lateinit var dialog: AlertDialog
    private val sharedViewModel: SharedViewModel by activityViewModels()

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.ivImgCapture.setOnClickListener {
            if (::dialog.isInitialized && dialog.isShowing) {
                dialog.dismiss()
            }
            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_progress, null)
            val imageView: ImageView? = dialogView.findViewById(R.id.loading_gif)
            imageView?.let {
                Glide.with(this).load(R.drawable.face).into(it)
            }
            val builder = AlertDialog.Builder(requireContext())
            builder.setView(dialogView)
            builder.setCancelable(false)
            dialog = builder.create()
            dialog.show()

            lifecycleScope.launch(Dispatchers.IO) {
                faceNetModel = FaceNetModel(requireActivity(), modelInfo, useGpu, useXNNPack)
                withContext(Dispatchers.Main) {
                    if (isAdded) {
                        dialog.dismiss()
                        checkAndRequestCameraPermission()

                    }
                }
            }
        }
        scanCode()

        return binding.root
    }
    @RequiresApi(Build.VERSION_CODES.P)
    private fun checkAndRequestCameraPermission() {
        if (checkSelfPermission(requireContext(),Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Camera permission is granted, proceed to take a picture
            takePicture()
        } else {
            // Camera permission is not granted, request it
            requestCameraPermission()
        }
    }
    @RequiresApi(Build.VERSION_CODES.P)
    private fun requestCameraPermission() {
        val permission = arrayOf<String>(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE )
        permissionLauncher.launch(permission)
    }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { result: Boolean ->
            if (result) {
                // Picture was taken successfully, update the ImageView with the captured image
                Glide.with(this).load(photoURI).placeholder(R.drawable.ic_person).circleCrop()
                    .into(binding.ivImgCapture)

                try {
                    // Initialize MediaPipe Face Detector
                    val baseOptionsBuilder = BaseOptions.builder()
                        .setModelAssetPath("blaze_face_short_range.tflite")

                    val options = FaceDetector.FaceDetectorOptions.builder()
                        .setBaseOptions(baseOptionsBuilder.build())
                        .setMinDetectionConfidence(0.5f)
                        .setRunningMode(RunningMode.IMAGE)
                        .build()

                    val faceDetector = FaceDetector.createFromOptions(requireContext(), options)

                    // Load image from URI
                    val imageBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(requireContext().contentResolver, photoURI)
                        ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true)
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(requireContext().contentResolver, photoURI)
                    }

                    // Convert to MPImage
                    val mpImage = BitmapImageBuilder(imageBitmap).build()

                    // Detect faces
                    val detectionResult = faceDetector.detect(mpImage)

                    // Handle detection results
                    when {
                        detectionResult.detections().isEmpty() -> {
                            Toast.makeText(requireContext(), "No face detected", Toast.LENGTH_SHORT).show()
                            binding.ivImgCapture.setImageResource(R.drawable.ic_person)
                            faceDetector.close()
                        }
                        detectionResult.detections().size > 1 -> {
                            Toast.makeText(requireContext(), "Multiple faces detected", Toast.LENGTH_SHORT).show()
                            binding.ivImgCapture.setImageResource(R.drawable.ic_person)
                            faceDetector.close()
                        }
                        else -> {
                            val detection = detectionResult.detections()[0]
                            val boundingBox = detection.boundingBox()

                            // Ensure bounding box is within image bounds (convert Float to Int)
                            val left = boundingBox.left.toInt().coerceAtLeast(0)
                            val top = boundingBox.top.toInt().coerceAtLeast(0)
                            val right = boundingBox.right.toInt().coerceAtMost(imageBitmap.width)
                            val bottom = boundingBox.bottom.toInt().coerceAtMost(imageBitmap.height)
                            val width = (right - left).coerceAtLeast(1)
                            val height = (bottom - top).coerceAtLeast(1)

                            // Validate dimensions
                            if (width <= 0 || height <= 0 || left >= imageBitmap.width || top >= imageBitmap.height) {
                                Toast.makeText(requireContext(), "Invalid face detection", Toast.LENGTH_SHORT).show()
                                binding.ivImgCapture.setImageResource(R.drawable.ic_person)
                                faceDetector.close()
                                return@registerForActivityResult
                            }

                            // Crop face from image
                            val faceBitmap = Bitmap.createBitmap(
                                imageBitmap,
                                left,
                                top,
                                width,
                                height
                            )

                            // Clean up detector
                            faceDetector.close()

                            // Get face embeddings
                            embeddings = faceNetModel.getFaceEmbedding(faceBitmap)
                            Toast.makeText(requireContext(), "Face Embeddings Generated", Toast.LENGTH_SHORT).show()
                        }
                    }

                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Face detection failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.ivImgCapture.setImageResource(R.drawable.ic_person)
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
                requireContext().packageName + ".provider",
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

        var counter = 1
        while (file.exists()) {
            currentFileName = "JPEG_${timeStamp}_$counter.jpeg"
            file = File(storageDir, currentFileName)
            counter++
        }

        return file.apply {
            currentPhotoPath = absolutePath
        }

    }

    @RequiresApi(Build.VERSION_CODES.P)
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                takePicture()
            } else {
                Toast.makeText(requireContext(), getString(R.string.permission_to_access_the_camera_denied), Toast.LENGTH_SHORT).show()
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[PatientDetailsViewModel::class.java]
        hideMarriedFields()
        setChangeListeners()
        setAdapters()
        setupVillageDropdown()

        sharedViewModel.photoUri.observe(viewLifecycleOwner) { uriString ->
            val photoUri = Uri.parse(uriString)
            Glide.with(this).load(photoUri).placeholder(R.drawable.ic_person).circleCrop().into(binding.ivImgCapture)}
        sharedViewModel.faceVector.observe(viewLifecycleOwner) { faceVector ->
            embeddings = faceVector
        }


        binding.firstNameText.setEndIconOnClickListener {
            speechToTextLauncherForFirstName.launch(Unit)
        }
        binding.lastNameText.setEndIconOnClickListener {
            speechToTextLauncherForLastName.launch(Unit)
        }
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
            ageAlertDialog.show()
        }
    }

    private val ageAlertDialog by lazy {
        val alertBinding = AlertAgePickerBinding.inflate(layoutInflater,binding.root,false)
        alertBinding.dialogNumberPickerYears.minValue = 0
        alertBinding.dialogNumberPickerYears.maxValue = 99

        alertBinding.dialogNumberPickerMonths.minValue = 0
        alertBinding.dialogNumberPickerMonths.maxValue = 11

        alertBinding.dialogNumberPickerDays.minValue = 0
        alertBinding.dialogNumberPickerDays.maxValue = 29

        val alert = MaterialAlertDialogBuilder(requireContext())
            .setView(alertBinding.root)
            .create()

        alertBinding.btnOk.setOnClickListener {
            alertBinding.dialogNumberPickerYears.clearFocus()
            alertBinding.dialogNumberPickerMonths.clearFocus()
            alertBinding.dialogNumberPickerDays.clearFocus()

            viewModel.enteredAgeYears = alertBinding.dialogNumberPickerYears.value
            viewModel.enteredAgeMonths = alertBinding.dialogNumberPickerMonths.value
            viewModel.enteredAgeDays = alertBinding.dialogNumberPickerDays.value

            setAgeToDateOfBirth()

            when {
                viewModel.enteredAgeYears != 0 -> {
                    viewModel.enteredAge = viewModel.enteredAgeYears
                    viewModel.selectedAgeUnit = viewModel.ageUnitList[2]
                }
                viewModel.enteredAgeMonths != 0 -> {
                    viewModel.enteredAge = viewModel.enteredAgeMonths
                    viewModel.selectedAgeUnit = viewModel.ageUnitList[1]
                }
                viewModel.enteredAgeWeeks != 0 -> {
                    viewModel.enteredAge = viewModel.enteredAgeWeeks
                    viewModel.selectedAgeUnit = viewModel.ageUnitList[3]
                }
                else -> {
                    viewModel.enteredAge = viewModel.enteredAgeDays
                    viewModel.selectedAgeUnit = viewModel.ageUnitList[0]
                }
            }

            var ageString = ""
            if(viewModel.enteredAgeYears!! > 0){
                ageString += viewModel.enteredAgeYears!!.toString() + " years"
            }
            if(viewModel.enteredAgeMonths!! > 0){
                if(ageString.isNotEmpty()) ageString += ", "
                ageString += viewModel.enteredAgeMonths!!.toString() + " months"
            }
            if(viewModel.enteredAgeDays!! > 0){
                if(ageString.isNotEmpty()) ageString += ", "
                ageString += viewModel.enteredAgeDays!!.toString() + " days"
            }

            binding.age.setText(ageString)
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
                        val date: Date? = when {
                            userData.dateOfBirth[2].toString() == "/" -> {
                                inputDateFormat1.parse(userData.dateOfBirth)
                            }
                            userData.dateOfBirth[2].toString() == "-" -> {
                                inputDateFormat2.parse(userData.dateOfBirth)
                            }
                            userData.dateOfBirth[4].toString() == "-" -> {
                                inputDateFormat3.parse(userData.dateOfBirth)
                            }
                            userData.dateOfBirth[4].toString() == "/" -> {
                                inputDateFormat4.parse(userData.dateOfBirth)
                            }
                            else -> {
                                null
                            }
                        } as Date

                        val outputDateStr: String = outputDateFormat.format(date)
                        val outputDate: Date = outputDateFormat.parse(outputDateStr) as Date

                        viewModel.selectedDateOfBirth = outputDate

                        dobUtil.showDatePickerDialog(
                            requireContext(),
                            viewModel.selectedDateOfBirth,
                            maxDays = 0,
                            minDays = -(99*365 + 25)
                        )

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

    private val speechToTextLauncherForFatherName = registerForActivityResult(SpeechToTextContract()) { result ->
        if (result.isNotBlank() && result.isNotEmpty() && !result.any { it.isDigit() }) {
            binding.fatherNameEditText.setText(result)
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

            viewModel.dobVal.observe(viewLifecycleOwner) {
                binding.dateOfBirthText.setBoxColor(it, resources.getString(R.string.fill_dob))
            }
            viewModel.ageVal.observe(viewLifecycleOwner) {
                binding.ageText.setBoxColor(it, resources.getString(R.string.enter_your_age))
            }
            viewModel.ageInUnitVal.observe(viewLifecycleOwner) {
                binding.ageInUnitText.setBoxColor(it, resources.getString(R.string.select_age_in_unit))
            }
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
        binding.fatherNameText.visibility = View.GONE
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
                    binding.fatherNameText.visibility = View.GONE
                    binding.spouseNameText.visibility = View.VISIBLE

                } else if (viewModel.selectedMaritalStatus != null && viewModel.selectedMaritalStatus!!.status.lowercase() == "unmarried") {
                    binding.spouseNameText.visibility = View.GONE
                    binding.fatherNameText.visibility = View.VISIBLE
                } else {
                    binding.spouseNameText.visibility = View.GONE
                    binding.fatherNameText.visibility = View.GONE
                }
            }
            else
                hideMarriedFields()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setChangeListeners(){

        // Setup keyboard handling for marital status dropdown
        binding.maritalStatusDropdown.setupDropdownKeyboardHandling()
        
        binding.maritalStatusDropdown.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedMaritalStatus = viewModel.maritalStatusList[position]
            viewModel.maritalStatusId = viewModel.maritalStatusList[position].maritalStatusID
            viewModel.maritalStatusName = viewModel.maritalStatusList[position].status
            binding.maritalStatusDropdown.setText(viewModel.selectedMaritalStatus!!.status, false)
            setMarriedFieldsVisibility()
        }

        // Setup keyboard handling for gender dropdown
        binding.genderDropdown.setupDropdownKeyboardHandling()
        
        binding.genderDropdown.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedGenderMaster = viewModel.genderMasterList[position]
            binding.genderDropdown.setText(viewModel.selectedGenderMaster!!.genderName, false)
        }

        binding.dateOfBirth.setOnClickListener {
            // Hide keyboard when clicking on date of birth field
            KeyboardUtils.hideKeyboard(binding.dateOfBirth)
            KeyboardUtils.hideKeyboardFromActivity(requireContext())
            
            dobUtil.showDatePickerDialog(
                requireContext(),
                viewModel.selectedDateOfBirth,
                maxDays = 0,
                minDays = -(99*365 + 25)
            ).show()
        }

        binding.age.addTextChangedListener(ageTextWatcher)

        binding.ageInUnitDropdown.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed in this case
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setMarriedFieldsVisibility()
                val isAgeInUnitFilled = s?.isNotEmpty() == true
                viewModel.setAgeUnit(isAgeInUnitFilled)
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed in this case
            }
        })
        binding.firstName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //No-Ops For Now
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isDobFilled = s?.isNotEmpty() == true
                viewModel.setFirstName(isDobFilled)
            }

            override fun afterTextChanged(s: Editable?) {
                //No-Ops For Now
            }
        })
        binding.lastName.addTextChangedListener (object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //No-Ops For Now
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isDobFilled = s?.isNotEmpty() == true
                viewModel.setLastName(isDobFilled)
            }

            override fun afterTextChanged(s: Editable?) {
                //No-Ops For Now
            }
        })
        binding.dateOfBirth.addTextChangedListener (object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //No-Ops For Now
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isDobFilled = s?.isNotEmpty() == true
                viewModel.setDob(isDobFilled)
            }

            override fun afterTextChanged(s: Editable?) {
                //No-Ops For Now
            }
        })
        binding.age.addTextChangedListener (object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //No-Ops For Now
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isDobFilled = s?.isNotEmpty() == true
                viewModel.setAge(isDobFilled)
            }

            override fun afterTextChanged(s: Editable?) {
                //No-Ops For Now
            }
        })
        binding.ageInUnitDropdown.addTextChangedListener (object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //No-Ops For Now
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isDobFilled = s?.isNotEmpty() == true
                viewModel.setAgeUnit(isDobFilled)
            }

            override fun afterTextChanged(s: Editable?) {
                //No-Ops For Now
            }
        })
        binding.genderDropdown.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //No-Ops For Now
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isGenderFilled = s?.isNotEmpty() == true
                viewModel.setGender(isGenderFilled)
            }

            override fun afterTextChanged(s: Editable?) {
                //No-Ops For Now
            }
        })


        binding.phoneNo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //No-Ops For Now
            }

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
            override fun afterTextChanged(s: Editable?) {
                //No-Ops For Now
            }
        })

        binding.maritalStatusDropdown.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //No-Ops For Now
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isMaritalStatusFilled = s?.isNotEmpty() == true
                viewModel.setMarital(isMaritalStatusFilled)
            }

            override fun afterTextChanged(s: Editable?) {
                //No-Ops For Now
            }
        })

        binding.spouseName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //No-Ops For Now
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isSpouseNameFilled = s?.isNotEmpty() == true
                viewModel.setSpouse(isSpouseNameFilled)
            }

            override fun afterTextChanged(s: Editable?) {
                //No-Ops For Now
            }
        })

        binding.ageAtMarriage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                  //No-Ops For Now
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isAgeAtMarriageFilled = s?.isNotEmpty() == true // Check if not empty
                viewModel.setMaritalAge(isAgeAtMarriageFilled) // Update LiveData
            }

            override fun afterTextChanged(s: Editable?) {
                //No-Ops For Now
            }
        })

    }



    private fun isValidPhoneNumber(phoneNumber: String) {
        var char = phoneNumber[0]

        if(char=='9' || char=='8' || char=='7' || char=='6') {
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
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            //No-Ops For Now
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            setAgeToDateOfBirth()
            val isAgeFilled = s?.isNotEmpty() == true
            viewModel.setAge(isAgeFilled)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun afterTextChanged(s: Editable?) {
            setMarriedFieldsVisibility()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setAdapters(){
        viewModel.villageVal.observe(viewLifecycleOwner) { state ->
            when (state!!){
                PatientDetailsViewModel.NetworkState.SUCCESS -> {
                    villageAdapter = VillageDropdownAdapter(
                        requireContext(),
                        R.layout.drop_down,
                        viewModel.villageList,
                        binding.villageDropdown,
                        viewModel.villageListFilter
                    )
                    villageAdapter.onDataUpdated = {
                        setVillageDropdownMaxHeight()
                    }
                    binding.villageDropdown.setAdapter(villageAdapter)
                    setVillageDropdownMaxHeight()
                }
                else -> {
                    //No-Ops For Now
                }
            }
        }

        viewModel.maritalStatus.observe(viewLifecycleOwner) { state ->
            when (state!!){
                PatientDetailsViewModel.NetworkState.SUCCESS -> {
                    val dropdownList = viewModel.maritalStatusList.map { DropdownList(it.maritalStatusID, it.status) }
                    val dropdownAdapter = DropdownAdapter(requireContext(), R.layout.drop_down, dropdownList, binding.maritalStatusDropdown)
                    binding.maritalStatusDropdown.setAdapter(dropdownAdapter)
                    // Ensure keyboard handling is set up (in case it wasn't set earlier)
                    binding.maritalStatusDropdown.setupDropdownKeyboardHandling()
                }
                else -> {
                    //No-Ops For Now
                }
            }
        }

        viewModel.genderMaster.observe(viewLifecycleOwner) { state ->
            when (state!!){
                PatientDetailsViewModel.NetworkState.SUCCESS -> {
                    val dropdownList = viewModel.genderMasterList.map { DropdownList(it.genderID, it.genderName) }
                    val dropdownAdapter = DropdownAdapter(requireContext(), R.layout.drop_down, dropdownList, binding.genderDropdown)
                    binding.genderDropdown.setAdapter(dropdownAdapter)
                    // Setup keyboard handling for gender dropdown
                    binding.genderDropdown.setupDropdownKeyboardHandling()
                }
                else -> {
                    //No-Ops For Now
                }
            }
        }

        dobUtil.selectedDate.observe(viewLifecycleOwner) { date ->
            if(date != null){
                setDateOfBirthToAge(date)
            }
        }

    }
    private fun setupVillageDropdown() {

        val dropdown = binding.villageDropdown

        dropdown.apply {
            threshold = 0
            inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            isFocusable = true
            isFocusableInTouchMode = true
            isCursorVisible = true
        }

        // Setup keyboard handling for dropdown
        dropdown.setupDropdownKeyboardHandling()

        dropdown.setOnClickListener {
            // Hide keyboard when dropdown is clicked
            KeyboardUtils.hideKeyboard(dropdown)
            KeyboardUtils.hideKeyboardFromActivity(requireContext())
            
            if (::villageAdapter.isInitialized) {
                villageAdapter.shouldAutoShowDropdown = true
                isSettingVillageProgrammatically = true
                villageAdapter.filter.filter("")
                isSettingVillageProgrammatically = false
            } else {
                dropdown.showDropDown()
            }
        }

        dropdown.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectedVillage =
                parent.getItemAtPosition(position) as VillageLocationData

            if (::villageAdapter.isInitialized) {
                villageAdapter.shouldAutoShowDropdown = false
            }
            isSettingVillageProgrammatically = true
            
            dropdown.setText(viewModel.selectedVillage!!.villageName, false)
            dropdown.dismissDropDown()
            hideKeyboard(dropdown)
            isSettingVillageProgrammatically = false
        }

        dropdown.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setVillageBool(!s.isNullOrEmpty())

                if (::villageAdapter.isInitialized && !isSettingVillageProgrammatically) {
                    villageAdapter.filter.filter(s)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }


    private fun hideKeyboard(view: View) {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setVillageDropdownMaxHeight() {
        val dropdown = binding.villageDropdown

        dropdown.post {
            if (::villageAdapter.isInitialized) {
                val itemCount = villageAdapter.count
                if (itemCount > 0) {
                    var itemHeightPx = 0
                    try {
                        val tempParent = android.widget.FrameLayout(requireContext())
                        val itemView = villageAdapter.getDropDownView(0, null, tempParent)
                        itemView.measure(
                            View.MeasureSpec.makeMeasureSpec(dropdown.width, View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                        )
                        itemHeightPx = itemView.measuredHeight
                    } catch (e: Exception) {
                        val itemHeightDp = 50f
                        itemHeightPx = (itemHeightDp * resources.displayMetrics.density).toInt()
                    }
                    val bottomPaddingDp = 16f
                    val bottomPaddingPx = (bottomPaddingDp * resources.displayMetrics.density).toInt()
                    val calculatedHeight = (itemCount * itemHeightPx) + bottomPaddingPx
                    val maxHeight = (resources.displayMetrics.heightPixels * 0.4).toInt()
                    dropdown.dropDownHeight = minOf(calculatedHeight, maxHeight)
                } else {
                    val maxHeight = (resources.displayMetrics.heightPixels * 0.4).toInt()
                    dropdown.dropDownHeight = maxHeight
                }
            } else {
                val maxHeight = (resources.displayMetrics.heightPixels * 0.4).toInt()
                dropdown.dropDownHeight = maxHeight
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setAgeToDateOfBirth(){
        viewModel.selectedDateOfBirth = DateTimeUtil.calculateDateOfBirth(viewModel.enteredAgeYears!!, viewModel.enteredAgeMonths!!,
            viewModel.enteredAgeWeeks!!, viewModel.enteredAgeDays!!)
        binding.dateOfBirth.setText(DateTimeUtil.formattedDate(viewModel.selectedDateOfBirth!!))
        setMarriedFieldsVisibility()

        doAgeToDob = true
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun setDateOfBirthToAge(date: Date){
        val age = DateTimeUtil.calculateAgePicker(date)
        viewModel.enteredAgeYears = age.years
        viewModel.enteredAgeMonths = age.months
        viewModel.enteredAgeWeeks = age.weeks
        viewModel.enteredAgeDays = age.days

        when {
            viewModel.enteredAgeYears != 0 -> {
                viewModel.enteredAge = viewModel.enteredAgeYears
                viewModel.selectedAgeUnit = viewModel.ageUnitList[2]
            }
            viewModel.enteredAgeMonths != 0 -> {
                viewModel.enteredAge = viewModel.enteredAgeMonths
                viewModel.selectedAgeUnit = viewModel.ageUnitList[1]
            }
            viewModel.enteredAgeWeeks != 0 -> {
                viewModel.enteredAge = viewModel.enteredAgeWeeks
                viewModel.selectedAgeUnit = viewModel.ageUnitList[3]
            }
            else -> {
                viewModel.enteredAge = viewModel.enteredAgeDays
                viewModel.selectedAgeUnit = viewModel.ageUnitList[0]
            }
        }

        var ageString = ""
        if(viewModel.enteredAgeYears!! > 0){
            ageString += viewModel.enteredAgeYears!!.toString() + " years"
        }
        if(viewModel.enteredAgeMonths!! > 0){
            if(ageString.isNotEmpty()) ageString += ", "
            ageString += viewModel.enteredAgeMonths!!.toString() + " months"
        }
        if(viewModel.enteredAgeDays!! > 0){
            if(ageString.isNotEmpty()) ageString += ", "
            ageString += viewModel.enteredAgeDays!!.toString() + " days"
        }

        binding.age.setText(ageString)
        viewModel.selectedAgeUnitEnum = viewModel.ageUnitEnumMap[viewModel.selectedAgeUnit]
        binding.ageInUnitDropdown.setText(viewModel.ageUnitMap[viewModel.selectedAgeUnitEnum]?.name ?: "", false)

        viewModel.selectedDateOfBirth = date
        doAgeToDob = false
        binding.dateOfBirth.setText(DateTimeUtil.formattedDate(date))

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setPatientDetails(){
        patient.firstName = binding.firstName.text.toString().trim()
        patient.lastName = binding.lastName.text.toString().trim()
        patient.dob = viewModel.selectedDateOfBirth
        patient.age = viewModel.enteredAge
        patient.maritalStatusID = viewModel.maritalStatusId
        patient.ageUnitID = viewModel.selectedAgeUnit?.id
        patient.parentName = binding.fatherNameEditText.text.toString().trim()
        patient.spouseName = binding.spouseName.text.toString().trim()
        patient.faceEmbedding = embeddings?.toList()
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
        return R.id.fragment_add_patient_location
    }
    fun checkVisibleFieldIsEmpty():Boolean{
        if(!viewModel.firstNameVal.value!! || !viewModel.dobVal.value!! || !viewModel.ageVal.value!! || !viewModel.genderVal.value!! || !viewModel.villageBoolVal.value!! ){
            return false
        }

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
                        if(preferenceDao.isUserCHO()){
                            val intent = Intent(context, EditPatientDetailsActivity::class.java)
                            intent.putExtra("benVisitInfo", viewModel.benVisitInfo)
                            startActivity(intent)
                        }
                        else{
                            requireActivity().finish()
                        }
                        Toast.makeText(requireContext(), getString(R.string.patient_registered_successfully), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        //No-Ops For Now we can display error message like something went wrng
                    }
                }
            }
        }
    }

    override fun onCancelAction() {
        requireActivity().finish()
    }

}

