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
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.AutoCompleteTextView
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
import org.piramalswasthya.cho.coroutines.DispatcherProvider
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
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.model.VillageLocationData
import kotlin.math.pow
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.SpeechToTextContract
import org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity
import org.piramalswasthya.cho.ui.register_patient_activity.scanAadhaar.ScanAadhaarActivity
import org.piramalswasthya.cho.ui.register_patient_activity.RegisterPatientActivity
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
import android.text.InputFilter
import android.util.Log


@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class PatientDetailsFragment : Fragment() , NavigationAdapter {

    @Inject
    lateinit var preferenceDao: PreferenceDao

    @Inject
    lateinit var dispatcherProvider: DispatcherProvider

    private val binding by lazy{
        FragmentPatientDetailsBinding.inflate(layoutInflater)
    }

    private lateinit var viewModel: PatientDetailsViewModel

    private var doAgeToDob = true
    private var patient = Patient()
    private lateinit var villageAdapter :VillageDropdownAdapter
    private var isSettingVillageProgrammatically = false
    private var isAgeChangedInEditMode = false
    private var isProgrammaticChange = false
    private var isReadOnly = false
    private var isEditModeAfterRegistration = false
    private val dobUtil : DateTimeUtil = DateTimeUtil()
    var bool: Boolean = false
    private var currentFileName: String? = null
    private var currentPhotoPath: String? = null
    private var photoURI: Uri? = null

    //facenet
    private val useGpu = false
    private val useXNNPack = true
    private val modelInfo = Models.FACENET
    private lateinit var faceNetModel : FaceNetModel
    private var embeddings: FloatArray? = null
    private lateinit var dialog: AlertDialog
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var statusOfWomanAdapter: DropdownAdapter? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        photoURI?.let { outState.putParcelable("photoURI", it) }
        outState.putString("currentFileName", currentFileName)
        outState.putString("currentPhotoPath", currentPhotoPath)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (savedInstanceState != null) {
            @Suppress("DEPRECATION")
            photoURI = savedInstanceState.getParcelable("photoURI")
            currentFileName = savedInstanceState.getString("currentFileName")
            currentPhotoPath = savedInstanceState.getString("currentPhotoPath")
        }
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

            lifecycleScope.launch(dispatcherProvider.io) {
                faceNetModel = FaceNetModel(requireActivity(), modelInfo, useGpu, useXNNPack)
                withContext(dispatcherProvider.main) {
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
                val uri = photoURI
                if (uri == null) {
                    Toast.makeText(requireContext(), "Photo capture failed. Please try again.", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                // Do NOT show the captured image yet — wait for face detection to pass first

                try {
                    // Initialize MediaPipe Face Detector
                    val baseOptionsBuilder = BaseOptions.builder()
                        .setModelAssetPath("blaze_face_short_range.tflite")

                    val options = FaceDetector.FaceDetectorOptions.builder()
                        .setBaseOptions(baseOptionsBuilder.build())
                        .setMinDetectionConfidence(0.75f)
                        .setRunningMode(RunningMode.IMAGE)
                        .build()

                    val faceDetector = FaceDetector.createFromOptions(requireContext(), options)

                    // Load image from URI, downsampled to avoid OOM on high-res cameras
                    val maxDimension = 1024
                    val imageBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                            val size = info.size
                            val sampleSize = maxOf(size.width, size.height) / maxDimension
                            if (sampleSize > 1) {
                                decoder.setTargetSampleSize(sampleSize)
                            }
                            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                    }.copy(Bitmap.Config.ARGB_8888, true)

                    // Convert to MPImage
                    val mpImage = BitmapImageBuilder(imageBitmap).build()

                    // Detect faces
                    val detectionResult = faceDetector.detect(mpImage)

                    // Handle detection results
                    when {
                        detectionResult.detections().isEmpty() -> {
                            // No face — keep the placeholder, show toast
                            embeddings = null
                            photoURI = null
                            currentFileName = null
                            currentPhotoPath = null
                            Toast.makeText(requireContext(), getString(R.string.no_face_detected), Toast.LENGTH_SHORT).show()
                            binding.ivImgCapture.setImageResource(R.drawable.ic_person)
                            faceDetector.close()
                        }
                        detectionResult.detections().size > 1 -> {
                            embeddings = null
                            photoURI = null
                            currentFileName = null
                            currentPhotoPath = null
                            Toast.makeText(requireContext(), getString(R.string.multiple_faces_detected), Toast.LENGTH_SHORT).show()
                            binding.ivImgCapture.setImageResource(R.drawable.ic_person)
                            faceDetector.close()
                        }
                        else -> {
                            // Face found — now show the captured photo
                            Glide.with(this).load(uri).placeholder(R.drawable.ic_person).circleCrop()
                                .into(binding.ivImgCapture)

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
                                embeddings = null
                                photoURI = null
                                currentFileName = null
                                currentPhotoPath = null
                                Toast.makeText(requireContext(), getString(R.string.invalid_face_detection), Toast.LENGTH_SHORT).show()
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

                            // Clean up detectoar
                            faceDetector.close()

                            // Get face embeddings
                            embeddings = faceNetModel.getFaceEmbedding(faceBitmap)

                            if (embeddings == null) {
                                photoURI = null
                                currentFileName = null
                                currentPhotoPath = null
                                Toast.makeText(requireContext(), getString(R.string.failed_to_generate_face_embeddings), Toast.LENGTH_SHORT).show()
                                binding.ivImgCapture.setImageResource(R.drawable.ic_person)
                                return@registerForActivityResult
                            }

                            lifecycleScope.launch(dispatcherProvider.io) {
                                val matchedPatient = compareFacesL2Norm(embeddings!!)
                                withContext(dispatcherProvider.main) {
                                    if (matchedPatient != null) {
                                        val patientInfo = viewModel.patientRepo.getPatientDisplayListForNurseByPatient(matchedPatient.patientID)
                                        populateForm(patientInfo)
                                        isEditModeAfterRegistration = true
                                        setFormEditable(true)
                                        Toast.makeText(requireContext(), "Existing beneficiary found. You can edit and update.", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(requireContext(), "Face Embeddings Generated", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }

                } catch (e: Exception) {
                    embeddings = null
                    photoURI = null
                    currentFileName = null
                    currentPhotoPath = null
                    Toast.makeText(requireContext(), getString(R.string.face_detection_failed, e.message.orEmpty()), Toast.LENGTH_SHORT).show()
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
            val uri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().packageName + ".provider",
                it
            )
            photoURI = uri
            takePictureLauncher.launch(uri)
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
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[PatientDetailsViewModel::class.java]

        initializeUI()
        setupViewModelObservers()
        handleArguments()
        setupClickListeners()
        setupNameFieldsInputFilters()
    }

    private fun initializeUI() {
        // Initial button state
        updateActivityButtons()
        hideMarriedFields()
        setChangeListeners()
        setAdapters()
        setupVillageDropdown()
        setupStatusOfWomanDropdown()

        enableFullBoxClick(binding.genderDropdown)
        enableFullBoxClick(binding.maritalStatusDropdown)
        enableFullBoxClick(binding.statusOfWomanDropdown)
    }

    private fun setupViewModelObservers() {
        viewModel.isDataSaved.observe(viewLifecycleOwner) { state ->
            if (state == true) {
                if (isEditModeAfterRegistration) {
                    WorkerUtils.triggerAmritSyncWorker(requireContext())
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.patient_edited_successfully_title),
                        Toast.LENGTH_SHORT
                    ).show()
                    requireActivity().finish()
                } else {
                    WorkerUtils.triggerAmritSyncWorker(requireContext())
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.patient_registered_successfully_title),
                        Toast.LENGTH_SHORT
                    ).show()
                    requireActivity().finish()
                }
                viewModel.setIsDataSaved(null)
            }
        }

        sharedViewModel.photoUri.observe(viewLifecycleOwner) { uriString ->
            // Only update when a real photo URI is available (skip the empty string set in edit mode)
            if (!uriString.isNullOrEmpty()) {
                val photoUri = Uri.parse(uriString)
                Glide.with(this).load(photoUri).placeholder(R.drawable.ic_person).circleCrop()
                    .into(binding.ivImgCapture)
            }
        }
        sharedViewModel.faceVector.observe(viewLifecycleOwner) { faceVector ->
            embeddings = faceVector
        }
    }

    private fun handleArguments() {
        val patientInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(
                "patientInfo",
                org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo::class.java
            )
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable("patientInfo") as? org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
        }
        val isEditMode = arguments?.getBoolean("isEditMode", false) ?: false

        if (patientInfo != null) {
            populateForm(patientInfo)
            if (isEditMode) {
                isEditModeAfterRegistration = true
                setFormEditable(true)
            } else {
                setFormEditable(false)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupClickListeners() {
        setupDobClickListener()
        setupSpeechToTextClickListeners()
        setupOtherClickListeners()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupDobClickListener() {
        binding.dateOfBirthText.setEndIconOnClickListener {
            if (binding.dateOfBirth.isEnabled) {
                // Hide keyboard when clicking on date of birth field
                KeyboardUtils.hideKeyboard(binding.dateOfBirth)
                KeyboardUtils.hideKeyboardFromActivity(requireContext())

                dobUtil.showDatePickerDialog(
                    requireContext(),
                    viewModel.selectedDateOfBirth,
                    maxDays = 0,
                    minDays = -(99 * 365 + 25)
                ).show()
            }
        }
    }

    private fun setupSpeechToTextClickListeners() {
        binding.firstNameText.setEndIconOnClickListener {
            if (binding.firstName.isEnabled) speechToTextLauncherForFirstName.launch(Unit)
        }
        binding.lastNameText.setEndIconOnClickListener {
            if (binding.lastName.isEnabled) speechToTextLauncherForLastName.launch(Unit)
        }
        binding.phoneNoText.setEndIconOnClickListener {
            if (binding.phoneNo.isEnabled) speechToTextLauncherForPhoneNumber.launch(Unit)
        }
        binding.spouseNameText.setEndIconOnClickListener {
            if (binding.spouseName.isEnabled) speechToTextLauncherForSpouseName.launch(Unit)
        }
        binding.fatherNameText.setEndIconOnClickListener {
            if (binding.fatherNameEditText.isEnabled) speechToTextLauncherForFatherName.launch(Unit)
        }

        binding.ageAtMarriageText.setEndIconOnClickListener {
            if (binding.ageAtMarriage.isEnabled) speechToTextLauncherForAgeAtMarriage.launch(Unit)
        }
    }

    private fun setupOtherClickListeners() {
        binding.age.setOnClickListener {
            if (binding.age.isEnabled) {
                ageAlertDialog.show()
            }
        }

        binding.fabEdit.setOnClickListener {
            isEditModeAfterRegistration = true
            setFormEditable(true)
        }
    }

    private fun setupNameFieldsInputFilters() {
        val nameFilter = InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                val char = source[i]
                if (!char.isLetter() && char != ' ' && char != '-') {
                    return@InputFilter ""
                }
            }
            null
        }
        binding.firstName.filters = arrayOf(nameFilter)
        binding.lastName.filters = arrayOf(nameFilter)
        binding.fatherNameEditText.filters = arrayOf(nameFilter)
        binding.spouseName.filters = arrayOf(nameFilter)
    }

    private fun setFormEditable(isEditable: Boolean) {
        isReadOnly = !isEditable
        isAgeChangedInEditMode = false
        binding.lastName.isEnabled = isEditable
        binding.phoneNo.isEnabled = isEditable
        binding.statusOfWomanDropdown.isEnabled = isEditable
        binding.age.isEnabled = isEditable
        binding.ageInUnitDropdown.isEnabled = isEditable
        val isCoreEditable = isEditable && !isEditModeAfterRegistration
        binding.firstName.isEnabled = isCoreEditable
        binding.genderDropdown.isEnabled = isCoreEditable
        binding.dateOfBirth.isEnabled = isCoreEditable

        val canEditAgeDependentFields = isEditable && (!isEditModeAfterRegistration || isAgeChangedInEditMode)
        binding.maritalStatusDropdown.isEnabled = canEditAgeDependentFields

        binding.spouseName.isEnabled = isCoreEditable
        binding.villageDropdown.isEnabled = isCoreEditable
        binding.ivImgCapture.isEnabled = isCoreEditable
        binding.tvSubTitlePhoto.visibility = if (isCoreEditable) View.VISIBLE else View.GONE
        binding.btnScanAadhaar.isEnabled = isCoreEditable

        // Disable end icons (mic/dropdown/calendar) for non-editable fields
        binding.firstNameText.isEndIconVisible = binding.firstName.isEnabled
        binding.lastNameText.isEndIconVisible = binding.lastName.isEnabled
        binding.phoneNoText.isEndIconVisible = binding.phoneNo.isEnabled
        val isUnmarried = isUnmarriedStatus(
            viewModel.selectedMaritalStatus?.maritalStatusID,
            viewModel.selectedMaritalStatus?.status
        )
        val isChild = (viewModel.enteredAgeYears ?: 0) < 15
        val canEditFatherName = isEditable && (isUnmarried || isChild || !isEditModeAfterRegistration)
        binding.fatherNameEditText.isEnabled = canEditFatherName
        binding.spouseNameText.isEndIconVisible = binding.spouseName.isEnabled
        binding.fatherNameText.isEndIconVisible = binding.fatherNameEditText.isEnabled
        binding.ageAtMarriageText.isEndIconVisible = binding.ageAtMarriage.isEnabled
        binding.dateOfBirthText.isEndIconVisible = binding.dateOfBirth.isEnabled

        // Handle dropdown icons
        // Using setEndIconVisible(false) might hide the dropdown arrow
        binding.genderText.isEndIconVisible = binding.genderDropdown.isEnabled
        binding.maritalStatusText.isEndIconVisible = binding.maritalStatusDropdown.isEnabled
        binding.statusOfWomanText.isEndIconVisible = binding.statusOfWomanDropdown.isEnabled
        binding.ageInUnitText.isEndIconVisible = binding.ageInUnitDropdown.isEnabled
        binding.villageText.isEndIconVisible = binding.villageDropdown.isEnabled

        // Clear focus if moving to read-only
        if (!isEditable) {
            binding.root.requestFocus()
        }

        // FAB visibility
        binding.fabEdit.visibility = if (isEditable) View.GONE else View.VISIBLE

        // Activity buttons visibility/text
        updateActivityButtons()
    }

    private fun updateActivityButtons() {
        (activity as? RegisterPatientActivity)?.let { act ->
            val bottomNav = act.findViewById<View>(R.id.bottom_navigation)
            val submitBtn = act.findViewById<android.widget.Button>(R.id.btnSubmit)
            val cancelBtn = act.findViewById<android.widget.Button>(R.id.btnCancel)

            bottomNav?.visibility = View.VISIBLE
            if (isReadOnly) {
                cancelBtn?.visibility = View.GONE
                submitBtn?.text = getString(R.string.submit_btn_text)
            } else {
                cancelBtn?.visibility = View.VISIBLE
                if (isEditModeAfterRegistration) {
                    submitBtn?.text = getString(R.string.ok_button)
                } else {
                    submitBtn?.text = getString(R.string.submit_btn_text)
                }
            }
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
            alertBinding.dialogNumberPickerYears.value =  viewModel.enteredAgeYears ?: 0
            alertBinding.dialogNumberPickerMonths.value = viewModel.enteredAgeMonths ?: 0
            alertBinding.dialogNumberPickerDays.value =  viewModel.enteredAgeDays ?: 0
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

                    applyScannedName(userData)


                    applyScannedDob(userData)

                    if (!userData.gender.isNullOrEmpty()){
                        applyScannedGender(userData)
                    }
                    applyScannedPhone(userData)
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
            Toast.makeText(context, getString(R.string.unable_to_fetch_details), Toast.LENGTH_SHORT).show()
        }
        return PatientAadhaarDetails(name, gender,mobileNumber, dateOfBirth)
    }




    private fun applyScannedName(userData: PatientAadhaarDetails) {
        val nameParts = userData.name?.split(" ")
        val firstName = nameParts?.get(0)
        val lastName = nameParts?.getOrNull(nameParts.size - 1)
        binding.firstName.text = Editable.Factory.getInstance().newEditable(firstName ?: "")
        binding.lastName.text = Editable.Factory.getInstance().newEditable(lastName ?: "")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun applyScannedDob(userData: PatientAadhaarDetails) {
        val dob = userData.dateOfBirth
        if (dob.isNullOrEmpty()) return

        // keep same accepted input formats; guard indices to avoid OOB
        val inputDateFormat1 = SimpleDateFormat("dd/MM/yyyy")
        val inputDateFormat2 = SimpleDateFormat("dd-MM-yyyy")
        val inputDateFormat3 = SimpleDateFormat("yyyy-MM-dd")
        val inputDateFormat4 = SimpleDateFormat("yyyy/MM/dd")
        val outputDateFormat = SimpleDateFormat("yyyy-MM-dd")

        val date: Date? = when {
            dob.length > 2 && dob[2] == '/' -> inputDateFormat1.parse(dob)
            dob.length > 2 && dob[2] == '-' -> inputDateFormat2.parse(dob)
            dob.length > 4 && dob[4] == '-' -> inputDateFormat3.parse(dob)
            dob.length > 4 && dob[4] == '/' -> inputDateFormat4.parse(dob)
            else -> null
        }

        if (date != null) {
            val outputDateStr: String = outputDateFormat.format(date)
            val outputDate: Date = outputDateFormat.parse(outputDateStr) as Date
            viewModel.selectedDateOfBirth = outputDate

            dobUtil.showDatePickerDialog(
                requireContext(),
                viewModel.selectedDateOfBirth,
                maxDays = 0,
                minDays = -(99*365 + 25)
            ).show()
        }
    }

    private fun applyScannedGender(userData: PatientAadhaarDetails) {
        val g = userData.gender ?: return
        when (g) {
            "M" -> viewModel.selectedGenderMaster = viewModel.genderMasterList.getOrNull(0)
            "F" -> viewModel.selectedGenderMaster = viewModel.genderMasterList.getOrNull(1)
            else -> viewModel.selectedGenderMaster = viewModel.genderMasterList.getOrNull(2)
        }
        viewModel.selectedGenderMaster?.let {
            binding.genderDropdown.setText(it.genderName, false)
        }
    }

    private fun applyScannedPhone(userData: PatientAadhaarDetails) {
        userData.mobileNumber?.takeIf { it.isNotBlank() }?.let {
            binding.phoneNo.setText(it)
        }
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

    private val speechToTextLauncherForAgeAtMarriage = registerForActivityResult(SpeechToTextContract()) { result ->
        if (result.isNotBlank()) {
            val cleanedResult = result.replace("\\s".toRegex(), "") // Remove all spaces
            // Age at marriage usually doesn't need substring, but we'll clean spaces
            binding.ageAtMarriage.setText(cleanedResult)
        }
    }
    fun watchAllFields(){
        if (!viewModel.isClickedSS.value!!) {
            watchBasicDetailsFields()
            watchStatusOfWomanField()
            watchMaritalStatusField()
            watchSpouseNameField()
            watchFatherNameField()

            viewModel.setIsClickedSS(true)
        }
    }

    private fun watchBasicDetailsFields() {
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
    }

    private fun watchStatusOfWomanField() {
        viewModel.statusOfWomanVal.observe(viewLifecycleOwner) {
            if (binding.statusOfWomanText.visibility == View.VISIBLE) {
                binding.statusOfWomanText.setBoxColor(it, resources.getString(R.string.select_status_of_woman))
            }
        }
    }

    private fun watchMaritalStatusField() {
        viewModel.maritalStatusVal.observe(viewLifecycleOwner) {
            if (binding.maritalStatusText.visibility == View.VISIBLE) {
                binding.maritalStatusText.setBoxColor(it, resources.getString(R.string.select_marital_status))
            }
        }
    }

    private fun watchSpouseNameField() {
        viewModel.spouseNameVal.observe(viewLifecycleOwner) {
            if (binding.spouseNameText.visibility == View.VISIBLE) {
                val genderId = viewModel.selectedGenderMaster?.genderID
                val message = if (genderId == 2) resources.getString(R.string.enter_husband_name)
                else if (genderId == 1) resources.getString(R.string.enter_wife_name)
                else resources.getString(R.string.enter_spouse_name)
                binding.spouseNameText.setBoxColor(it, message)
            }
        }
    }

    private fun watchFatherNameField() {
        viewModel.fatherNameVal.observe(viewLifecycleOwner) {
            if (binding.fatherNameText.visibility == View.VISIBLE) {
                binding.fatherNameText.setBoxColor(it, resources.getString(R.string.enter_father_s_name_error))
            }
        }
    }

    private fun isValidName(name: String): Boolean {
        return name.matches(Regex("^[A-Za-z\\s\\-]+$"))
    }


    private fun hideMarriedFields(){
        binding.maritalStatusText.visibility = View.GONE
        binding.spouseNameText.visibility = View.GONE
        binding.fatherNameText.visibility = View.GONE
        binding.ageAtMarriageText.visibility = View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setMarriedFieldsVisibility() {
        val genderId = viewModel.selectedGenderMaster?.genderID
        val ageInYears = viewModel.enteredAgeYears

        if (viewModel.shouldShowMaritalStatus(genderId, ageInYears)) {
            binding.maritalStatusText.visibility = View.VISIBLE

            // Enable Marital Status if age changed in Edit mode
            if (!isReadOnly && isEditModeAfterRegistration && isAgeChangedInEditMode) {
                binding.maritalStatusDropdown.isEnabled = true
                binding.maritalStatusText.isEndIconVisible = true
            }

            val status = viewModel.selectedMaritalStatus?.status?.lowercase()?.trim()
            when {
                status?.contains("married") == true && !status.contains("unmarried") && !status.contains("never") -> {
                    binding.fatherNameText.visibility = View.GONE
                    binding.spouseNameText.visibility = View.VISIBLE

                    // Update hint based on gender
                    val hintRes = if (genderId == 2) R.string.husband_s_name else if (genderId == 1) R.string.wife_s_name else R.string.spouse_name
                    binding.spouseNameText.hint = getText(hintRes)

                    binding.spouseName.isEnabled = !isReadOnly && (!isEditModeAfterRegistration || isAgeChangedInEditMode)
                    binding.spouseNameText.isEndIconVisible = binding.spouseName.isEnabled

                    val isSpouseNameFilled = binding.spouseName.text?.isNotEmpty() == true && isValidName(binding.spouseName.text.toString())
                    viewModel.setSpouse(isSpouseNameFilled)
                    viewModel.setFatherName(true) // Not mandatory if married
                }
                status?.contains("unmarried") == true || status?.contains("never") == true || status?.contains("single") == true -> {
                    binding.spouseNameText.visibility = View.GONE
                    binding.fatherNameText.visibility = View.VISIBLE
                    binding.spouseName.setText("") // Clear if was married before

                    // Allow editing Father Name if we are in Edit mode
                    binding.fatherNameEditText.isEnabled = !isReadOnly
                    binding.fatherNameText.isEndIconVisible = binding.fatherNameEditText.isEnabled

                    val isFatherNameFilled = binding.fatherNameEditText.text?.isNotEmpty() == true && isValidName(binding.fatherNameEditText.text.toString())
                    viewModel.setFatherName(isFatherNameFilled)
                    viewModel.setSpouse(true) // Not mandatory if not married
                }
                else -> {
                    binding.spouseNameText.visibility = View.GONE
                    binding.fatherNameText.visibility = View.GONE
                    binding.spouseName.setText("") // Clear selection
                    viewModel.setFatherName(true)
                    viewModel.setSpouse(true)
                }
            }
        } else {
            if (!isProgrammaticChange) {
                hideMarriedFields()
                viewModel.selectedMaritalStatus = null
                binding.maritalStatusDropdown.setText("", false)
                viewModel.setMarital(true) // Hidden, so not mandatory
                viewModel.setSpouse(true)
            }

            // Show Father Name for children (Age < 15) and make it mandatory
            if (ageInYears != null && ageInYears < 15) {
                binding.fatherNameText.visibility = View.VISIBLE

                // Allow editing Father Name if we are in Edit mode
                binding.fatherNameEditText.isEnabled = !isReadOnly
                binding.fatherNameText.isEndIconVisible = binding.fatherNameEditText.isEnabled

                val isFatherNameFilled = binding.fatherNameEditText.text?.isNotEmpty() == true && isValidName(binding.fatherNameEditText.text.toString())
                viewModel.setFatherName(isFatherNameFilled)
            } else {
                viewModel.setFatherName(true)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setChangeListeners(){

        // Setup keyboard handling for marital status dropdown
        binding.maritalStatusDropdown.setupDropdownKeyboardHandling()

        binding.maritalStatusDropdown.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position) as? DropdownList
            selectedItem?.let { item ->
                viewModel.selectedMaritalStatus = viewModel.maritalStatusList.find { it.maritalStatusID == item.id }
                viewModel.maritalStatusId = item.id
                viewModel.maritalStatusName = item.display
                binding.maritalStatusDropdown.setText(item.display, false)
                setMarriedFieldsVisibility()
                updateStatusOfWomanVisibility()
            }
        }

        // Setup keyboard handling for gender dropdown
        binding.genderDropdown.setupDropdownKeyboardHandling()

        // Setup keyboard handling for gender dropdown
        binding.genderDropdown.setupDropdownKeyboardHandling()

        binding.genderDropdown.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position) as? DropdownList
            selectedItem?.let { item ->
                viewModel.selectedGenderMaster = viewModel.genderMasterList.find { it.genderID == item.id }
                binding.genderDropdown.setText(item.display, false)
                updateMaritalStatusOptions()
                setMarriedFieldsVisibility()
                updateStatusOfWomanVisibility()
            }
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
                val name = s?.toString()?.trim() ?: ""
                val isValid = name.isNotEmpty() && isValidName(name)
                viewModel.setFirstName(isValid)
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
                val name = s?.toString()?.trim() ?: ""
                // Last name is optional, but if filled, it must be valid
                val isValid = name.isEmpty() || isValidName(name)
                viewModel.setLastName(isValid)
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

        binding.fatherNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val name = s?.toString()?.trim() ?: ""
                val isValid = name.isNotEmpty() && isValidName(name)
                viewModel.setFatherName(isValid)
            }
            override fun afterTextChanged(s: Editable?) {}
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
            if (!isReadOnly && isEditModeAfterRegistration && !isProgrammaticChange) {
                isAgeChangedInEditMode = true
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun afterTextChanged(s: Editable?) {
            setMarriedFieldsVisibility()
            updateStatusOfWomanVisibility()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setAdapters(){
        setupVillageAdapterObserver()
        setupMaritalStatusAdapterObserver()
        setupGenderAdapterObserver()
        setupDateObserver()
    }

    private fun setupVillageAdapterObserver() {
        viewModel.villageVal.observe(viewLifecycleOwner) { state ->
            when (state!!) {
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

                    // Pre-fill if patient data exists
                    if (patient.districtBranchID != null) {
                        viewModel.selectedVillage =
                            viewModel.villageList.find { it.districtBranchID.toInt() == patient.districtBranchID }
                        viewModel.selectedVillage?.let { v ->
                            isSettingVillageProgrammatically = true
                            binding.villageDropdown.setText(v.villageName, false)
                            binding.villageDropdown.dismissDropDown()
                            isSettingVillageProgrammatically = false
                        }
                    }
                }
                else -> {
                    //No-Ops For Now
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupMaritalStatusAdapterObserver() {
        viewModel.maritalStatus.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                PatientDetailsViewModel.NetworkState.SUCCESS -> {
                    val dropdownList =
                        viewModel.maritalStatusList.map { DropdownList(it.maritalStatusID, it.status) }
                    val dropdownAdapter = DropdownAdapter(
                        requireContext(),
                        R.layout.drop_down,
                        dropdownList,
                        binding.maritalStatusDropdown
                    )
                    binding.maritalStatusDropdown.setAdapter(dropdownAdapter)
                    // Ensure keyboard handling is set up (in case it wasn't set earlier)
                    binding.maritalStatusDropdown.setupDropdownKeyboardHandling()

                    // Pre-fill if patient data exists
                    if (patient.maritalStatusID != null) {
                        viewModel.selectedMaritalStatus =
                            viewModel.maritalStatusList.find { it.maritalStatusID == patient.maritalStatusID }
                        viewModel.selectedMaritalStatus?.let { m ->
                            isProgrammaticChange = true
                            viewModel.maritalStatusId = m.maritalStatusID
                            viewModel.maritalStatusName = m.status
                            binding.maritalStatusDropdown.setText(m.status, false)
                            setMarriedFieldsVisibility()
                            updateStatusOfWomanVisibility()

                            // Fix: Ensure dropdown is enabled for Unmarried status in Edit mode
                            if (isEditModeAfterRegistration) {
                                binding.maritalStatusDropdown.isEnabled =
                                    isUnmarriedStatus(m.maritalStatusID, m.status)
                            }

                            isProgrammaticChange = false
                        }
                    }
                }
                else -> {
                    //No-Ops For Now
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupGenderAdapterObserver() {
        viewModel.genderMaster.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                PatientDetailsViewModel.NetworkState.SUCCESS -> {
                    val dropdownList =
                        viewModel.genderMasterList.map { DropdownList(it.genderID, it.genderName) }
                    val dropdownAdapter = DropdownAdapter(
                        requireContext(),
                        R.layout.drop_down,
                        dropdownList,
                        binding.genderDropdown
                    )
                    binding.genderDropdown.setAdapter(dropdownAdapter)
                    // Setup keyboard handling for gender dropdown
                    binding.genderDropdown.setupDropdownKeyboardHandling()

                    // Pre-fill if patient data exists
                    if (patient.genderID != null) {
                        viewModel.selectedGenderMaster =
                            viewModel.genderMasterList.find { it.genderID == patient.genderID }
                        viewModel.selectedGenderMaster?.let { master ->
                            isProgrammaticChange = true
                            binding.genderDropdown.setText(master.genderName, false)
                            setMarriedFieldsVisibility()
                            updateStatusOfWomanVisibility()
                            isProgrammaticChange = false
                        }
                    }
                }
                else -> {
                    //No-Ops For Now
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupDateObserver() {
        dobUtil.selectedDate.observe(viewLifecycleOwner) { date ->
            if (date != null) {
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

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Intentionally left empty: village filtering and state updates are handled in
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setVillageBool(!s.isNullOrEmpty())

                if (::villageAdapter.isInitialized && !isSettingVillageProgrammatically) {
                    villageAdapter.filter.filter(s)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // No post-change work required for village input. Kept intentionally blank
            }
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
            val defaultMaxHeight = (resources.displayMetrics.heightPixels * 0.4).toInt()
            if (::villageAdapter.isInitialized) {
                val itemCount = villageAdapter.count
                if (itemCount > 0) {
                    var itemHeightPx: Int
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
                    dropdown.dropDownHeight = minOf(calculatedHeight, defaultMaxHeight)
                } else {
                    // Prevent large blank popup when filter returns no village rows.
                    dropdown.dropDownHeight = ViewGroup.LayoutParams.WRAP_CONTENT
                }
            } else {
                dropdown.dropDownHeight = defaultMaxHeight
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setAgeToDateOfBirth(){
        viewModel.selectedDateOfBirth = DateTimeUtil.calculateDateOfBirth(viewModel.enteredAgeYears ?: 0, viewModel.enteredAgeMonths ?: 0,
            viewModel.enteredAgeWeeks ?: 0, viewModel.enteredAgeDays ?: 0)
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
        if((viewModel.enteredAgeYears ?: 0) > 0){
            ageString += (viewModel.enteredAgeYears ?: 0).toString() + " years"
        }
        if((viewModel.enteredAgeMonths ?: 0) > 0){
            if(ageString.isNotEmpty()) ageString += ", "
            ageString += (viewModel.enteredAgeMonths ?: 0).toString() + " months"
        }
        if((viewModel.enteredAgeDays ?: 0) > 0){
            if(ageString.isNotEmpty()) ageString += ", "
            ageString += (viewModel.enteredAgeDays ?: 0).toString() + " days"
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
        // Only update face embedding if a new photo was captured
        if (embeddings != null) {
            patient.faceEmbedding = embeddings?.toList()
        }
        if (binding.phoneNo.text.toString().isNullOrEmpty()) {
            patient.phoneNo = null
        } else {
            patient.phoneNo = binding.phoneNo.text.toString()
        }
        patient.genderID = viewModel.selectedGenderMaster?.genderID
        patient.registrationDate = Date()
        // Only update benImage if a new photo was actually taken (currentFileName is set)
        if (currentFileName != null) {
            patient.benImage = ImgUtils.getEncodedStringForBenImage(requireContext(), currentFileName)
        }
        patient.statusOfWomanID = viewModel.selectedStatusOfWoman?.statusID

    }

    private fun setLocationDetails(){
        val locData = preferenceDao.getUserLocationData()
        patient.stateID = locData?.stateId
        patient.districtID = locData?.districtId
        patient.blockID = locData?.blockId
        patient.districtBranchID = viewModel.selectedVillage?.districtBranchID!!.toInt()
    }

    private fun populateForm(patientInfo: org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo) {
        val p = patientInfo.patient
        this.patient = p // Set current patient object

        isProgrammaticChange = true
        binding.firstName.setText(p.firstName)
        binding.lastName.setText(p.lastName)
        binding.phoneNo.setText(p.phoneNo)
        binding.spouseName.setText(p.spouseName)
        binding.fatherNameEditText.setText(p.parentName)

        // Age
        p.age?.let {
            viewModel.enteredAge = it
            viewModel.enteredAgeYears = it
            binding.age.setText("$it years")
        }

        // Age Unit
        p.ageUnitID?.let { id ->
            viewModel.ageUnitList.find { it.id == id }?.let { unit ->
                viewModel.selectedAgeUnit = unit
                binding.ageInUnitDropdown.setText(unit.name, false)
            }
        }

        binding.ageAtMarriage.setText(p.ageAtMarriage?.toString() ?: "")

        // DOB
        p.dob?.let {
            viewModel.selectedDateOfBirth = it
            binding.dateOfBirth.setText(DateTimeUtil.formattedDate(it))
        }

        // Photo: benImage is stored as base64 string with optional "data:image/...;base64," prefix
        p.benImage?.let { img ->
            val base64Data = if (img.contains(",")) img.substringAfter(",") else img
            val bitmap = org.piramalswasthya.cho.utils.ImgUtils.decodeBase64ToBitmap(base64Data)
            if (bitmap != null) {
                com.bumptech.glide.Glide.with(this).load(bitmap).placeholder(R.drawable.ic_person)
                    .circleCrop().into(binding.ivImgCapture)
            } else {
                binding.ivImgCapture.setImageResource(R.drawable.ic_person)
            }
        }
        // Gender
        p.genderID?.let { id ->
            viewModel.genderMasterList.find { it.genderID == id }?.let { master ->
                viewModel.selectedGenderMaster = master
                binding.genderDropdown.setText(master.genderName, false)
            }
        }

        // Marital Status
        p.maritalStatusID?.let { id ->
            viewModel.maritalStatusList.find { it.maritalStatusID == id }?.let { m ->
                viewModel.selectedMaritalStatus = m
                viewModel.maritalStatusId = m.maritalStatusID
                viewModel.maritalStatusName = m.status
                binding.maritalStatusDropdown.setText(m.status, false)
            }
        }

        // Village
        p.districtBranchID?.let { id ->
            viewModel.villageList.find { it.districtBranchID.toInt() == id }?.let { v ->
                viewModel.selectedVillage = v
                isSettingVillageProgrammatically = true
                binding.villageDropdown.setText(v.villageName, false)
                isSettingVillageProgrammatically = false
            }
        }

        // Status of Woman
        p.statusOfWomanID?.let { id ->
            viewModel.statusOfWomanList.find { it.statusID == id }?.let { s ->
                viewModel.selectedStatusOfWoman = s
                binding.statusOfWomanDropdown.setText(s.statusName, false)
                viewModel.setStatusOfWoman(true)
            }
        }

        // Correct visibility states based on populated data
        setMarriedFieldsVisibility()
        updateStatusOfWomanVisibility()

        isProgrammaticChange = false
    }

    override fun getFragmentId(): Int {
        return R.id.fragment_add_patient_location
    }
    fun checkVisibleFieldIsEmpty():Boolean{
        if(!viewModel.firstNameVal.value!! || !viewModel.dobVal.value!! || !viewModel.ageVal.value!! || !viewModel.genderVal.value!! || !viewModel.villageBoolVal.value!! ){
            return false
        }

        if (binding.statusOfWomanText.visibility == View.VISIBLE && !viewModel.statusOfWomanVal.value!!) {
            return false
        }

        // Check Marital Status if visible
        if (binding.maritalStatusText.visibility == View.VISIBLE && !viewModel.maritalStatusVal.value!!) {
            return false
        }

        if (binding.spouseNameText.visibility == View.VISIBLE && !viewModel.spouseNameVal.value!!) {
            return false
        }

        if (binding.fatherNameText.visibility == View.VISIBLE && !viewModel.fatherNameVal.value!!) {
            binding.fatherNameText.setBoxColor(false, resources.getString(R.string.enter_father_s_name_error))
            return false
        }
        return true
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSubmitAction() {
        if (isReadOnly) {
            requireActivity().finish()
            return
        }

        watchAllFields()
        if (checkVisibleFieldIsEmpty()) {
            setPatientDetails()
            setLocationDetails()

            if (isEditModeAfterRegistration) {
                patient.syncState = SyncState.UNSYNCED
                viewModel.updatePatient(patient)
            } else {
                if (patient.patientID.isBlank() || patient.patientID == "null") {
                    // Initial Registration
                    patient.patientID = generateUuid()
                }
                viewModel.insertPatient(patient)
            }
        }
    }


    override fun onCancelAction() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.are_you_sure))
            .setMessage(getString(R.string.cancel_confirmation_message))
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                dialog.dismiss()
                requireActivity().finish()
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


    private suspend fun compareFacesL2Norm(newEmbedding: FloatArray): Patient? {
        var bestMatch: Patient? = null
        var bestDistance = Float.MAX_VALUE

        val patients = viewModel.getAllPatientsForFaceComparison()

        kotlinx.coroutines.withContext(dispatcherProvider.default) {
            for (patient in patients) {
                val patientEmbedding = patient.faceEmbedding?.toFloatArray()
                if (patientEmbedding == null || patientEmbedding.isEmpty()) {
                    continue
                }

                val distance = l2Norm(newEmbedding, patientEmbedding)
                if (distance < bestDistance) {
                    bestDistance = distance
                    bestMatch = patient
                }
            }
        }

        // L2 threshold for FaceNet model
        return if (bestDistance < Models.FACENET.l2Threshold) bestMatch else null
    }

    private fun l2Norm(x1: FloatArray, x2: FloatArray): Float {
        var sum = 0.0f
        for (i in x1.indices) {
            sum += (x1[i] - x2[i]).pow(2)
        }
        return kotlin.math.sqrt(sum)
    }

    private fun enableFullBoxClick(dropdown: AutoCompleteTextView) {
        dropdown.setOnTouchListener { _, event ->
            if (dropdown.isEnabled && event.action == MotionEvent.ACTION_UP) {
                dropdown.showDropDown()
            }
            false
        }

        dropdown.setOnFocusChangeListener { _, hasFocus ->
            if (dropdown.isEnabled && hasFocus) dropdown.showDropDown()
        }
    }
    private fun setupStatusOfWomanDropdown() {
        viewModel.statusOfWoman.observe(viewLifecycleOwner) { state ->
            when (state) {
                PatientDetailsViewModel.NetworkState.SUCCESS -> {
                    updateStatusOfWomanVisibility()

                    // Pre-fill if patient data exists
                    if (patient.statusOfWomanID != null) {
                        val status = viewModel.statusOfWomanList.find { it.statusID == patient.statusOfWomanID }
                        status?.let { s ->
                            isProgrammaticChange = true
                            viewModel.selectedStatusOfWoman = s
                            patient.statusOfWomanID = s.statusID
                            binding.statusOfWomanDropdown.setText(s.statusName, false)
                            viewModel.setStatusOfWoman(true)
                            isProgrammaticChange = false
                        }
                    }
                }
                else -> {
                    //No-Ops For Now
                }
            }
        }

        binding.statusOfWomanDropdown.setupDropdownKeyboardHandling()

        binding.statusOfWomanDropdown.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position) as? DropdownList
            selectedItem?.let { item ->
                viewModel.selectedStatusOfWoman = viewModel.statusOfWomanList.find { it.statusID == item.id }
                patient.statusOfWomanID = viewModel.selectedStatusOfWoman?.statusID
                binding.statusOfWomanDropdown.setText(item.display, false)
                viewModel.setStatusOfWoman(true)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateStatusOfWomanVisibility() {
        val genderId = viewModel.selectedGenderMaster?.genderID
        val ageInYears = viewModel.enteredAgeYears

        if (viewModel.shouldShowStatusOfWoman(genderId, ageInYears)) {
            handleStatusOfWomanVisible(genderId, ageInYears)
        } else {
            handleStatusOfWomanHidden()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleStatusOfWomanVisible(genderId: Int?, ageInYears: Int?) {
        binding.statusOfWomanText.visibility = View.VISIBLE

        val canEdit = !isReadOnly || (isEditModeAfterRegistration && isAgeChangedInEditMode)
        binding.statusOfWomanDropdown.isEnabled = canEdit
        binding.statusOfWomanText.isEndIconVisible = canEdit

        val maritalStatusName = viewModel.selectedMaritalStatus?.status?.trim()
        viewModel.filteredStatusOfWomanList = viewModel.getFilteredStatusOfWomanOptions(
            genderId,
            ageInYears,
            viewModel.selectedMaritalStatus?.maritalStatusID,
            maritalStatusName
        )

        setupStatusOfWomanDropdownContent()

        // Re-validate Father Name if status of woman changes (e.g. to Adolescent Girl)
        val mStatus = viewModel.selectedMaritalStatus?.status?.lowercase()
        val statusOfWoman = viewModel.selectedStatusOfWoman?.statusName?.lowercase()

        if (statusOfWoman == "adolescent" || mStatus == "unmarried") {
            binding.fatherNameText.hint = getText(R.string.father_s_name)
            val isFatherNameFilled = binding.fatherNameEditText.text?.isNotEmpty() == true &&
                    isValidName(binding.fatherNameEditText.text.toString())
            viewModel.setFatherName(isFatherNameFilled)
        }
    }

    private fun handleStatusOfWomanHidden() {
        binding.statusOfWomanText.visibility = View.GONE
        if (!isProgrammaticChange) {
            viewModel.selectedStatusOfWoman = null
            patient.statusOfWomanID = null
        }
        viewModel.setStatusOfWoman(true)
    }

    private fun setupStatusOfWomanDropdownContent() {
        if (viewModel.filteredStatusOfWomanList.isNotEmpty()) {
            val dropdownList = viewModel.filteredStatusOfWomanList.map {
                DropdownList(it.statusID, it.statusName)
            }
            statusOfWomanAdapter = DropdownAdapter(
                requireContext(),
                R.layout.drop_down,
                dropdownList,
                binding.statusOfWomanDropdown
            )
            binding.statusOfWomanDropdown.setAdapter(statusOfWomanAdapter)

            validateCurrentStatusOfWomanSelection()
            restoreStatusOfWomanSelection()
            autoSelectIsSingleOption()
        } else {
            clearStatusOfWomanDropdown()
        }
    }

    private fun validateCurrentStatusOfWomanSelection() {
        // Check if current selection is still valid
        if (viewModel.selectedStatusOfWoman != null &&
            viewModel.filteredStatusOfWomanList.none { it.statusID == viewModel.selectedStatusOfWoman!!.statusID }
        ) {
            viewModel.selectedStatusOfWoman = null
            patient.statusOfWomanID = null
            binding.statusOfWomanDropdown.setText("", false)
            viewModel.setStatusOfWoman(false)
        }
    }

    private fun restoreStatusOfWomanSelection() {
        // Restore text if already selected
        viewModel.selectedStatusOfWoman?.let { s ->
            val oldFlag = isProgrammaticChange
            isProgrammaticChange = true
            binding.statusOfWomanDropdown.setText(s.statusName, false)
            isProgrammaticChange = oldFlag
        }
    }

    private fun autoSelectIsSingleOption() {
        // If only one option and none selected, auto-select it
        if (viewModel.selectedStatusOfWoman == null && viewModel.filteredStatusOfWomanList.size == 1) {
            viewModel.selectedStatusOfWoman = viewModel.filteredStatusOfWomanList[0]
            patient.statusOfWomanID = viewModel.selectedStatusOfWoman?.statusID
            binding.statusOfWomanDropdown.setText(
                viewModel.selectedStatusOfWoman!!.statusName,
                false
            )
            viewModel.setStatusOfWoman(true)
        } else if (viewModel.selectedStatusOfWoman != null) {
            // Already selected, ensure validation is true
            viewModel.setStatusOfWoman(true)
        }
    }

    private fun clearStatusOfWomanDropdown() {
        if (!isProgrammaticChange) {
            // List is empty (e.g. Marital Status not selected yet), clear dropdown
            statusOfWomanAdapter = DropdownAdapter(
                requireContext(),
                R.layout.drop_down,
                emptyList(),
                binding.statusOfWomanDropdown
            )
            binding.statusOfWomanDropdown.setAdapter(statusOfWomanAdapter)
            viewModel.selectedStatusOfWoman = null
            patient.statusOfWomanID = null
            binding.statusOfWomanDropdown.setText("", false)
            viewModel.setStatusOfWoman(false)
        }
    }

    private fun isUnmarriedStatus(maritalStatusId: Int?, maritalStatusName: String?): Boolean {
        if (maritalStatusId == 1) return true
        val currentStatus = maritalStatusName?.lowercase(Locale.ROOT)?.trim().orEmpty()
        return currentStatus.contains("unmarried") ||
            currentStatus.contains("never") ||
            currentStatus.contains("single")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateMaritalStatusOptions() {
        val genderId = viewModel.selectedGenderMaster?.genderID
        val filteredList = viewModel.getFilteredMaritalStatusOptions(genderId)

        val dropdownList = filteredList.map { DropdownList(it.maritalStatusID, it.status) }
        val dropdownAdapter =
            DropdownAdapter(requireContext(), R.layout.drop_down, dropdownList, binding.maritalStatusDropdown)
        binding.maritalStatusDropdown.setAdapter(dropdownAdapter)

        // Restore text if already selected
        viewModel.selectedMaritalStatus?.let {
            val oldFlag = isProgrammaticChange
            isProgrammaticChange = true
            binding.maritalStatusDropdown.setText(it.status, false)
            isProgrammaticChange = oldFlag
        }

        // If current selection is no longer in filtered list, clear it
        if (!isProgrammaticChange && viewModel.selectedMaritalStatus != null && filteredList.none { it.maritalStatusID == viewModel.selectedMaritalStatus!!.maritalStatusID }) {
            viewModel.selectedMaritalStatus = null
            binding.maritalStatusDropdown.setText("", false)
        }
    }
}