package org.piramalswasthya.cho.ui.commons.personal_details

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.PatientItemAdapter
import org.piramalswasthya.cho.adapter.ApiSearchAdapter
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.database.room.dao.VillageMasterDao
import org.piramalswasthya.cho.database.room.dao.StateMasterDao
import org.piramalswasthya.cho.database.room.dao.DistrictMasterDao
import org.piramalswasthya.cho.database.room.dao.BlockMasterDao
import org.piramalswasthya.cho.model.VillageMaster
import org.piramalswasthya.cho.model.StateMaster
import org.piramalswasthya.cho.model.DistrictMaster
import org.piramalswasthya.cho.model.BlockMaster
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentPersonalDetailsBinding
import org.piramalswasthya.cho.facenet.FaceNetModel
import org.piramalswasthya.cho.facenet.Models
import org.piramalswasthya.cho.model.NetworkBody
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.ESanjeevaniApiService
import org.piramalswasthya.cho.network.interceptors.TokenESanjeevaniInterceptor
import org.piramalswasthya.cho.repositories.CaseRecordeRepo
import org.piramalswasthya.cho.repositories.VisitReasonsAndCategoriesRepo
import org.piramalswasthya.cho.repositories.VitalsRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.cho.ui.commons.SpeechToTextContract
import org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity
import org.piramalswasthya.cho.ui.home.HomeViewModel
import org.piramalswasthya.cho.ui.register_patient_activity.RegisterPatientActivity
import org.piramalswasthya.cho.ui.web_view_activity.WebViewActivity
import timber.log.Timber
import org.json.JSONObject
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.ui.register_patient_activity.patient_details.PatientDetailsViewModel
import org.piramalswasthya.cho.utils.generateUuid
import org.piramalswasthya.cho.utils.DateTimeUtil
import org.piramalswasthya.cho.work.WorkerUtils
import android.os.Build
import org.piramalswasthya.cho.utils.NetworkConnection
import javax.inject.Inject
import kotlin.math.pow


@AndroidEntryPoint
class PersonalDetailsFragment : Fragment() {
    @Inject
    lateinit var apiService: ESanjeevaniApiService
    @Inject
    lateinit var amritApiService: AmritApiService
    private lateinit var viewModel: PersonalDetailsViewModel
    private lateinit var viewModelPatientDetails: PatientDetailsViewModel
    private lateinit var networkConnection: NetworkConnection
    private var isNetworkAvailable = false
    private var itemAdapter: PatientItemAdapter? = null
    private var apiSearchAdapter: ApiSearchAdapter? = null
    private var usernameEs: String = ""
    private var passwordEs: String = ""
    private var errorEs: String = ""
    private var network: Boolean = false
    private var currentFileName: String? = null
    private lateinit var photoURI: Uri
    private var currentPhotoPath: String? = null
    private var isShowingSearchResults: Boolean = false
    private var currentSearchQuery: String = ""
    private var searchJob: Job? = null

    private val prescriptionChannelId = "prescription_download"

    //facenet
    private val useGpu = false
    private val useXNNPack = true
    private val modelInfo = Models.FACENET
    private lateinit var faceNetModel: FaceNetModel
    private var embeddings: FloatArray? = null
    private lateinit var dialog: AlertDialog

    @Inject
    lateinit var preferenceDao: PreferenceDao

    @Inject
    lateinit var patientDao: PatientDao

    @Inject
    lateinit var caseRecordeRepo: CaseRecordeRepo

    @Inject
    lateinit var visitReasonsAndCategoriesRepo: VisitReasonsAndCategoriesRepo

    @Inject
    lateinit var vitalsRepo: VitalsRepo

    @Inject
    lateinit var patientRepo: PatientRepo

    @Inject
    lateinit var userRepo: UserRepo

    @Inject
    lateinit var villageMasterDao: VillageMasterDao

    @Inject
    lateinit var stateMasterDao: StateMasterDao

    @Inject
    lateinit var districtMasterDao: DistrictMasterDao

    @Inject
    lateinit var blockMasterDao: BlockMasterDao

    private var _binding: FragmentPersonalDetailsBinding? = null
    private var patientCount: Int = 0

    private val binding
        get() = _binding!!

    private val abhaDisclaimer by lazy {
        AlertDialog.Builder(requireContext()).setTitle(getString(R.string.beneficiary_abha_number))
            .setMessage("it")
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        HomeViewModel.resetSearchBool()
        _binding = FragmentPersonalDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        networkConnection = NetworkConnection(requireContext())

        networkConnection.observe(viewLifecycleOwner) { isConnected ->
            isNetworkAvailable = isConnected
        }

        viewModelPatientDetails = ViewModelProvider(this)[PatientDetailsViewModel::class.java]
        HomeViewModel.searchBool.observe(viewLifecycleOwner) { bool ->
            when (bool!!) {
                true -> {
                    binding.search.requestFocus()
                    activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

                }

                else -> {
                    //No-Ops for now
                }
            }

        }

        binding.cameraIcon.setOnClickListener {

//            initialise the facenet model
            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_progress, null)
            val imageView: ImageView? = dialogView.findViewById(R.id.loading_gif)
            imageView?.let {
                Glide.with(this).load(R.drawable.face).into(it)
            }
            val builder = AlertDialog.Builder(context)
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

        binding.searchTil.setEndIconOnClickListener {
            speechToTextLauncherForSearchByName.launch(Unit)
        }
        viewModel = ViewModelProvider(this)[PersonalDetailsViewModel::class.java]
        viewModel.patientObserver.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                PersonalDetailsViewModel.NetworkState.SUCCESS -> {
                    itemAdapter = context?.let {
                        PatientItemAdapter(
                            apiService,
                            it,
                            clickListener = PatientItemAdapter.BenClickListener({ benVisitInfo ->
                                if (isShowingSearchResults) {
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        val patient = benVisitInfo.patient
                                        val regId = patient.beneficiaryRegID
                                        if (regId != null) {
                                            val existing = patientDao.getPatientByBenRegId(regId)
                                            if (existing == null) {
                                                patientDao.insertPatient(patient)
                                            } else {
                                                patient.patientID = existing.patientID
                                                patientDao.updatePatient(patient)
                                            }
                                        } else {
                                            patientDao.insertPatient(patient)
                                        }
                                        withContext(Dispatchers.Main) {
                                            isShowingSearchResults = false
                                            binding.search.setText("")
                                            binding.patientListContainer.patientList.adapter = itemAdapter
                                        }
                                    }
                                    return@BenClickListener
                                }
                                when {
                                    preferenceDao.isRegistrarSelected() -> {
                                        // No-Op: Registrar sees nothing here for now
                                    }

                                    benVisitInfo.nurseFlag == 9 && benVisitInfo.doctorFlag == 2 && preferenceDao.isDoctorSelected() -> {

                                        Toast.makeText(
                                            requireContext(),
                                            resources.getString(R.string.pendingForLabtech),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    benVisitInfo.nurseFlag == 9 && benVisitInfo.doctorFlag == 9 && preferenceDao.isDoctorSelected() -> {

                                        var modifiedInfo = benVisitInfo
                                        if (preferenceDao.isNurseSelected()) {
                                            modifiedInfo = PatientDisplayWithVisitInfo(benVisitInfo)
                                        }

                                        val intent = Intent(
                                            context, EditPatientDetailsActivity::class.java
                                        ).apply {
                                            putExtra("benVisitInfo", modifiedInfo)
                                            putExtra("viewRecord", true)
                                            putExtra("isFlowComplete", true)
                                        }
                                        startActivity(intent)
                                        requireActivity().finish()
                                    }

                                    else -> {

                                        var modifiedInfo = benVisitInfo
                                        if (preferenceDao.isNurseSelected()) {
                                            modifiedInfo = PatientDisplayWithVisitInfo(benVisitInfo)
                                        }

                                        val intent = Intent(
                                            context, EditPatientDetailsActivity::class.java
                                        ).apply {
                                            putExtra("benVisitInfo", modifiedInfo)
                                            putExtra("viewRecord", false)
                                            putExtra("isFlowComplete", false)
                                        }
                                        startActivity(intent)
                                        requireActivity().finish()
                                    }
                                }

                            }, { benVisitInfo ->

                                checkAndGenerateABHA(benVisitInfo)
                            }, { benVisitInfo ->
                                callLoginDialog(benVisitInfo)
                            }, { benVisitInfo ->
                                lifecycleScope.launch {
                                    generatePDF(benVisitInfo)
                                }

                            }, { benVisitInfo ->
                                openDialog(benVisitInfo)
                            }),
                            showAbha = true
                        )
                    }

                    binding.patientListContainer.patientList.adapter = itemAdapter

                    apiSearchAdapter = ApiSearchAdapter(requireContext()) { selectedPatient ->
                        binding.search.text?.clear()
                        isShowingSearchResults = false
                        viewModel.filterText("")
                        binding.patientListContainer.patientList.adapter = itemAdapter
                        val currentCount = itemAdapter?.itemCount ?: patientCount
                        binding.patientListContainer.patientCount.text =
                            currentCount.toString() + getResultStr(currentCount)
                        savePatientFromSearch(selectedPatient)
                    }

                    viewModelPatientDetails.isDataSaved.observe(viewLifecycleOwner) { state ->
                        if (state == true) {
                            WorkerUtils.triggerAmritSyncWorker(requireContext())

                            Toast.makeText(
                                requireContext(),
                                getString(R.string.patient_registered_successfully),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    when {
                        preferenceDao.isRegistrarSelected() || preferenceDao.isNurseSelected() -> {
                            lifecycleScope.launch {
                                viewModel.patientListForNurse?.collect {
                                    itemAdapter?.submitList(it.sortedByDescending { item ->
                                        item.patient.registrationDate
                                    })
                                    patientCount = it.size
                                    withContext(Dispatchers.Main) {
                                        if (!isShowingSearchResults) {
                                            binding.patientListContainer.patientList.adapter = itemAdapter
                                            binding.patientListContainer.patientCount.text =
                                                it.size.toString() + getResultStr(it.size)
                                        }
                                    }
                                }
                            }
                        }

                        preferenceDao.isDoctorSelected() -> {
                            lifecycleScope.launch {
                                viewModel.patientListForDoctor?.collect {
                                    itemAdapter?.submitList(it.sortedByDescending { item ->
                                        item.patient.registrationDate
                                    })
                                    patientCount = it.size
                                    withContext(Dispatchers.Main) {
                                        if (!isShowingSearchResults) {
                                            binding.patientListContainer.patientList.adapter = itemAdapter
                                            binding.patientListContainer.patientCount.text =
                                                it.size.toString() + getResultStr(it.size)
                                        }
                                    }
                                }
                            }
                        }

                        preferenceDao.isLabSelected() -> {
                            lifecycleScope.launch {
                                viewModel.patientListForLab?.collect {
                                    itemAdapter?.submitList(it.sortedByDescending { item ->
                                        item.patient.registrationDate
                                    })
                                    patientCount = it.size
                                    withContext(Dispatchers.Main) {
                                        if (!isShowingSearchResults) {
                                            binding.patientListContainer.patientList.adapter = itemAdapter
                                            binding.patientListContainer.patientCount.text =
                                                it.size.toString() + getResultStr(it.size)
                                        }
                                    }
                                }
                            }
                        }

                        preferenceDao.isPharmaSelected() -> {
                            lifecycleScope.launch {
                                viewModel.patientListForPharmacist?.collect {
                                    itemAdapter?.submitList(it.sortedByDescending { item ->
                                        item.patient.registrationDate
                                    })
                                    patientCount = it.size
                                    withContext(Dispatchers.Main) {
                                        if (!isShowingSearchResults) {
                                            binding.patientListContainer.patientList.adapter = itemAdapter
                                            binding.patientListContainer.patientCount.text =
                                                itemAdapter?.itemCount.toString() + getResultStr(itemAdapter?.itemCount)
                                        }
                                    }
                                }
                            }
                        }
                    }

                }

                else -> {
                    //No-Ops for now
                }
            }

            viewModel.abha.observe(viewLifecycleOwner) {
                it.let {
                    if (it != null) {
                        abhaDisclaimer.setMessage(it)
                        abhaDisclaimer.show()
                    }
                }
            }

            viewModel.benRegId.observe(viewLifecycleOwner) {
                if (it != null) {
                    val intent = Intent(requireActivity(), AbhaIdActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    intent.putExtra("benId", viewModel.benId.value)
                    intent.putExtra("benRegId", it)
                    requireActivity().startActivity(intent)
                    viewModel.resetBenRegId()
                }
            }

            binding.search.setOnFocusChangeListener { searchView, b ->
                if (b) (searchView as EditText).addTextChangedListener(searchTextWatcher)
                else (searchView as EditText).removeTextChangedListener(searchTextWatcher)

            }
        }
    }

    private fun savePatientFromSearch(apiPatient: PatientDisplayWithVisitInfo) {
        lifecycleScope.launch {
            try {
                val beneficiaryID = apiPatient.patient.beneficiaryID
                if (beneficiaryID != null) {
                    val existingPatient = withContext(Dispatchers.IO) {
                        patientDao.getBen(beneficiaryID)
                    }
                    
                    if (existingPatient != null) {
                        withContext(Dispatchers.Main) {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Patient Already Exists")
                                .setMessage("A patient with Beneficiary ID $beneficiaryID already exists in the system.")
                                .setPositiveButton("OK", null)
                                .show()
                        }
                        return@launch
                    }
                }
                
                var stateID = apiPatient.patient.stateID
                var districtID = apiPatient.patient.districtID
                var blockID = apiPatient.patient.blockID
                var districtBranchID = apiPatient.patient.districtBranchID
                val villageName = apiPatient.villageName
                
                if (districtBranchID == null) {
                    val locData = preferenceDao.getUserLocationData()
                    stateID = locData?.stateId
                    districtID = locData?.districtId
                    blockID = locData?.blockId
                    districtBranchID = viewModelPatientDetails.selectedVillage?.districtBranchID?.toInt()
                }
                
                if (districtBranchID != null && blockID != null && districtID != null && stateID != null && !villageName.isNullOrBlank()) {
                    withContext(Dispatchers.IO) {
                        if (stateMasterDao.getStateById(stateID) == null) {
                            stateMasterDao.insertStates(
                                StateMaster(
                                    stateID = stateID,
                                    stateName = "",
                                    govtLGDStateID = null
                                )
                            )
                        }
                        
                        if (districtMasterDao.getDistrictById(districtID) == null) {
                            districtMasterDao.insertDistrict(
                                DistrictMaster(
                                    districtID = districtID,
                                    stateID = stateID,
                                    govtLGDStateID = null,
                                    govtLGDDistrictID = null,
                                    districtName = ""
                                )
                            )
                        }
                        
                        if (blockMasterDao.getBlockById(blockID) == null) {
                            blockMasterDao.insertBlock(
                                BlockMaster(
                                    blockID = blockID,
                                    districtID = districtID,
                                    govtLGDDistrictID = null,
                                    govLGDSubDistrictID = null,
                                    blockName = ""
                                )
                            )
                        }
                        
                        val existingVillage = villageMasterDao.getVillageById(districtBranchID)
                        if (existingVillage == null || existingVillage.villageName.isNullOrBlank()) {
                            villageMasterDao.insertVillage(
                                VillageMaster(
                                    districtBranchID = districtBranchID,
                                    blockID = blockID,
                                    govtLGDVillageID = null,
                                    govtLGDSubDistrictID = null,
                                    villageName = villageName
                                )
                            )
                        } else if (existingVillage.villageName != villageName) {
                            villageMasterDao.insertVillage(
                                existingVillage.copy(villageName = villageName)
                            )
                        }
                    }
                }
                
                val patientToSave = Patient(
                    patientID = generateUuid(),
                    firstName = apiPatient.patient.firstName,
                    lastName = apiPatient.patient.lastName,
                    beneficiaryRegID = apiPatient.patient.beneficiaryRegID,
                    beneficiaryID = apiPatient.patient.beneficiaryID,
                    syncState = SyncState.UNSYNCED,
                    registrationDate = Date(),
                    phoneNo = apiPatient.patient.phoneNo,
                    genderID = apiPatient.patient.genderID,
                    dob = apiPatient.patient.dob,
                    age = apiPatient.patient.age,
                    ageUnitID = apiPatient.patient.ageUnitID,
                    maritalStatusID = apiPatient.patient.maritalStatusID,
                    spouseName = apiPatient.patient.spouseName,
                    parentName = apiPatient.patient.parentName,
                    stateID = stateID,
                    districtID = districtID,
                    blockID = blockID,
                    districtBranchID = districtBranchID,
                    communityID = apiPatient.patient.communityID,
                    religionID = apiPatient.patient.religionID,
                    benImage = apiPatient.patient.benImage,
                    isNewAbha = apiPatient.patient.isNewAbha,
                    healthIdDetails = apiPatient.patient.healthIdDetails,
                    faceEmbedding = apiPatient.patient.faceEmbedding
                )
                
                var saveSuccess = false
                try {
                    withContext(Dispatchers.IO) {
                        patientRepo.insertPatient(patientToSave)
                    }
                    saveSuccess = true
                } catch (e: android.database.sqlite.SQLiteConstraintException) {
                    val patientWithNullForeignKeys = patientToSave.copy(
                        ageUnitID = null,
                        maritalStatusID = null,
                        communityID = null,
                        religionID = null
                    )
                    
                    try {
                        withContext(Dispatchers.IO) {
                            patientRepo.insertPatient(patientWithNullForeignKeys)
                        }
                        saveSuccess = true
                    } catch (e2: Exception) {
                        withContext(Dispatchers.Main) {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Error Saving Patient")
                                .setMessage("Failed to save patient due to missing data. Please ensure all data is synced.")
                                .setPositiveButton("OK", null)
                                .show()
                        }
                        return@launch
                    }
                }
                
                if (saveSuccess) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Patient saved successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        viewModel.filterText("")
                    }
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Error saving patient: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private lateinit var syncBottomSheet: SyncBottomSheetFragment
    private fun openDialog(benVisitInfo: PatientDisplayWithVisitInfo) {
        syncBottomSheet = SyncBottomSheetFragment(benVisitInfo)
        if (!syncBottomSheet.isVisible) syncBottomSheet.show(
            childFragmentManager, resources.getString(R.string.sync)
        )
        Timber.tag("sync").i("${benVisitInfo}")
    }

    var pageHeight = 1120
    var pageWidth = 792


    @RequiresApi(Build.VERSION_CODES.P)
    private fun checkAndRequestCameraPermission() {
        if (checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(
                requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Camera permission is granted, proceed to take a picture
            takePicture()
        } else {
            // Camera permission is not granted, request it
            requestCameraPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun requestCameraPermission() {
        val permission =
            arrayOf<String>(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissionLauncher.launch(permission)
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

    private fun takePicture() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: Exception) {
            null
        }

        photoFile?.also {
            photoURI = FileProvider.getUriForFile(
                requireContext(), requireContext().packageName + ".provider", it
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

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { result: Boolean ->
            if (result) {
                try {
                    // Initialize MediaPipe Face Detector
                    val baseOptionsBuilder =
                        BaseOptions.builder().setModelAssetPath("blaze_face_short_range.tflite")

                    val options = FaceDetector.FaceDetectorOptions.builder()
                        .setBaseOptions(baseOptionsBuilder.build()).setMinDetectionConfidence(0.5f)
                        .setRunningMode(RunningMode.IMAGE).build()

                    val faceDetector = FaceDetector.createFromOptions(requireContext(), options)

                    // Load image from URI
                    val imageBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source =
                            ImageDecoder.createSource(requireContext().contentResolver, photoURI)
                        ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true)
                    } else {
                        @Suppress("DEPRECATION") MediaStore.Images.Media.getBitmap(
                            requireContext().contentResolver, photoURI
                        )
                    }

                    // Convert to MPImage
                    val mpImage = BitmapImageBuilder(imageBitmap).build()

                    // Detect faces
                    val detectionResult = faceDetector.detect(mpImage)

                    // Handle detection results
                    when {
                        detectionResult.detections().isEmpty() -> {
                            Toast.makeText(requireContext(), "No face detected", Toast.LENGTH_SHORT)
                                .show()
                            faceDetector.close()
                        }

                        detectionResult.detections().size > 1 -> {
                            Toast.makeText(
                                requireContext(), "Multiple faces detected", Toast.LENGTH_SHORT
                            ).show()
                            faceDetector.close()
                        }

                        else -> {
                            val detection = detectionResult.detections()[0]
                            val boundingBox = detection.boundingBox()

                            // Check for degenerate bounding box before coercion
                            if (boundingBox.right <= boundingBox.left || boundingBox.bottom <= boundingBox.top) {
                                Toast.makeText(
                                    requireContext(), "Invalid face detection", Toast.LENGTH_SHORT
                                ).show()
                                faceDetector.close()
                                return@registerForActivityResult
                            }

// Ensure bounding box stays within image bounds
                            val left = boundingBox.left.toInt().coerceAtLeast(0)
                            val top = boundingBox.top.toInt().coerceAtLeast(0)
                            val right = boundingBox.right.toInt().coerceAtMost(imageBitmap.width)
                            val bottom = boundingBox.bottom.toInt().coerceAtMost(imageBitmap.height)

                            val width = (right - left).coerceAtLeast(1)
                            val height = (bottom - top).coerceAtLeast(1)

// No need to validate width/height again — coercion guarantees ≥ 1


                            // Crop face from image
                            val faceBitmap = Bitmap.createBitmap(
                                imageBitmap, left, top, width, height
                            )

                            // Clean up detector
                            faceDetector.close()

                            // Get face embeddings
                            embeddings = faceNetModel.getFaceEmbedding(faceBitmap)

                            // Compare faces and find matching patient
                            lifecycleScope.launch {
                                val matchedPatient = compareFacesL2Norm(embeddings!!)
                                if (matchedPatient != null) {
                                    val visitInfo = PatientVisitInfoSync()
                                    val benVisitInfo = PatientDisplayWithVisitInfo(
                                        matchedPatient,
                                        genderName = null,
                                        villageName = null,
                                        ageUnit = null,
                                        maritalStatus = null,
                                        nurseDataSynced = visitInfo.nurseDataSynced,
                                        doctorDataSynced = visitInfo.doctorDataSynced,
                                        createNewBenFlow = visitInfo.createNewBenFlow,
                                        prescriptionID = visitInfo.prescriptionID,
                                        benVisitNo = visitInfo.benVisitNo,
                                        visitCategory = visitInfo.visitCategory,
                                        benFlowID = visitInfo.benFlowID,
                                        nurseFlag = visitInfo.nurseFlag,
                                        doctorFlag = visitInfo.doctorFlag,
                                        labtechFlag = visitInfo.labtechFlag,
                                        pharmacist_flag = visitInfo.pharmacist_flag,
                                        visitDate = visitInfo.visitDate,
                                        referDate = visitInfo.referDate,
                                        referTo = visitInfo.referTo,
                                        referralReason = visitInfo.referralReason
                                    )
                                    itemAdapter?.submitList(listOf(benVisitInfo))
                                    binding.patientListContainer.patientCount.text =
                                        "1 Matched Patient"
                                    Toast.makeText(
                                        requireContext(),
                                        "1 matching patient found",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "No matching patient found",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    searchPrompt.show()
                                }
                            }
                        }
                    }

                } catch (e: Exception) {
                    Log.e("FaceDetection", "Face detection failed", e)
                    Toast.makeText(
                        requireContext(), "Face detection failed: ${e.message}", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


    private suspend fun compareFacesL2Norm(newEmbedding: FloatArray): Patient? {
        var bestMatch: Patient? = null
        var bestDistance = Float.MAX_VALUE

        val patients = withContext(Dispatchers.IO) {
            patientDao.getAllPatients()
        }
        withContext(Dispatchers.Default) {
            for (patient in patients) {
                // Ensure that the faceEmbedding is not null and not empty, and convert it to FloatArray
                val patientEmbedding = patient.faceEmbedding?.toFloatArray()
                if (patientEmbedding == null || patientEmbedding.isEmpty()) {
                    continue
                }

                val distance = L2Norm(newEmbedding, patientEmbedding)
                if (distance < bestDistance) {
                    bestDistance = distance
                    bestMatch = patient
                }
            }
        }

        val threshold = Models.FACENET.l2Threshold
        return if (bestDistance < threshold) bestMatch else null
    }

    private fun L2Norm(x1: FloatArray, x2: FloatArray): Float {
        var sum = 0.0f
        for (i in x1.indices) {
            sum += (x1[i] - x2[i]).pow(2)
        }
        return kotlin.math.sqrt(sum)
    }

    private val searchPrompt by lazy {
        MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.note_ben_reg))
            .setMessage(getString(R.string.no_patient_found))
            .setPositiveButton("Search") { dialog, _ ->
                dialog.dismiss()
                HomeViewModel.setSearchBool()
            }.setNegativeButton("Proceed with Registration") { dialog, _ ->
                val intent = Intent(context, RegisterPatientActivity::class.java).apply {
                    putExtra("photoUri", photoURI.toString())
                    putExtra("facevector", embeddings)
                }
                startActivity(intent)
                dialog.dismiss()
                HomeViewModel.resetSearchBool()
            }.create()
    }

    private suspend fun generatePDF(benVisitInfo: PatientDisplayWithVisitInfo) {
        val patientName =
            (benVisitInfo.patient.firstName ?: "") + " " + (benVisitInfo.patient.lastName ?: "")
        val prescriptions = caseRecordeRepo.getPrescriptionCaseRecordeByPatientIDAndBenVisitNo(
            patientID = benVisitInfo.patient.patientID, benVisitNo = benVisitInfo.benVisitNo!!
        )
        val chiefComplaints = visitReasonsAndCategoriesRepo.getChiefComplaintDBByPatientId(
            patientID = benVisitInfo.patient.patientID, benVisitNo = benVisitInfo.benVisitNo
        )
        val vitals = vitalsRepo.getPatientVitalsByPatientIDAndBenVisitNo(
            patientID = benVisitInfo.patient.patientID, benVisitNo = benVisitInfo.benVisitNo
        )

        val pdfDocument: PdfDocument = PdfDocument()

        val heading: Paint = Paint()
        val content: Paint = Paint()
        val subheading: Paint = Paint()

        val myPageInfo: PdfDocument.PageInfo? =
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()

        val myPage: PdfDocument.Page = pdfDocument.startPage(myPageInfo)
        val canvas: Canvas = myPage.canvas

        // Set up initial positions for the table
        val xPosition = 75F
        var y = 270F // Declare y as a var
        val rowHeight = 50F
        val leftSideX = 50F
        val extraSpace = 10F

        // Set up Paint for text
        Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 15F
            color = ContextCompat.getColor(requireContext(), android.R.color.black)
            textAlign = Paint.Align.LEFT
        }

        content.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL))
        content.textSize = 15F
        content.color = ContextCompat.getColor(requireContext(), android.R.color.black)
        content.textAlign = Paint.Align.CENTER

        subheading.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        subheading.textSize = 16F
        subheading.color = ContextCompat.getColor(requireContext(), android.R.color.black)
        subheading.textAlign = Paint.Align.LEFT

        heading.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL))
        heading.textSize = 40F
        heading.color = ContextCompat.getColor(requireContext(), android.R.color.black)
        heading.textAlign = Paint.Align.CENTER

        val spaceAfterHeading = 20F
        canvas.drawText("Prescription", 396F, 100F + spaceAfterHeading, heading)

        val leftX = 50F // where labels start
        var currentY = 180F
        val lineHeight = 25F

// Left side labels and values
        val leftLabels = listOf("Name:", "Age:", "Gender:", "Mobile:")
        val leftValues = listOf(
            patientName,
            "${benVisitInfo.patient.age} ${benVisitInfo.ageUnit}",
            benVisitInfo.genderName,
            benVisitInfo.patient.phoneNo ?: "N/A"
        )

        val leftLabelWidth = leftLabels.maxOf { subheading.measureText(it) }
        val leftValueX = leftX + leftLabelWidth + 10F // 10F padding

        for (i in leftLabels.indices) {
            canvas.drawText(leftLabels[i], leftX, currentY, subheading)
            canvas.drawText("${leftValues[i]}", leftValueX, currentY, subheading)
            currentY += lineHeight
        }

        canvas.drawLine(leftX, currentY, pageWidth - leftX, currentY, subheading)
        //currentY += lineHeight

        subheading.textAlign = Paint.Align.LEFT
        val rightX = pageWidth - 300F
        var rightY = 180F

        val rightLabels = listOf("Date:", "Beneficiary Reg ID:", "Consultation ID:")
        val rightValues = listOf(
            benVisitInfo.visitDate ?: "N/A",
            "${benVisitInfo.patient.beneficiaryRegID}",
            "${benVisitInfo.benVisitNo}"
        )

        val rightLabelWidth = rightLabels.maxOf { subheading.measureText(it) }
        val rightValueX = rightX + rightLabelWidth + 10F // 10F padding

        for (i in rightLabels.indices) {
            canvas.drawText(rightLabels[i], rightX, rightY, subheading)
            canvas.drawText("${rightValues[i]}", rightValueX, rightY, subheading)
            rightY += lineHeight
        }

        val spaceAfterLine = 20F

        // Define fixed column widths
        val columnWidth = 150F
        y += spaceAfterLine
        y += 30

        val chiefComplaintHeader = "Chief Complaints"
        val chiefComplaintHeaderSize = 25F // Adjust the size as needed
        val chiefComplaintHeaderX = (pageWidth / 2).toFloat() // Center the heading
        canvas.drawText(chiefComplaintHeader, chiefComplaintHeaderX, y, subheading.apply {
            textSize = chiefComplaintHeaderSize
            textAlign = Paint.Align.CENTER
        })

// Move down to the first row of Chief Complaints
        y += rowHeight

// Define fixed column widths for Chief Complaints
        val chiefComplaintColumnWidth = 150F

// Draw table header for Chief Complaints
        canvas.drawText("S.No.", xPosition + extraSpace, y, subheading)
        canvas.drawText("Chief Complaint", xPosition + chiefComplaintColumnWidth, y, subheading)
        canvas.drawText("Duration", xPosition + 2 * chiefComplaintColumnWidth, y, subheading)
        canvas.drawText("Duration Unit", xPosition + 3 * chiefComplaintColumnWidth, y, subheading)
        canvas.drawText("Description", xPosition + 4 * chiefComplaintColumnWidth, y, subheading)

// Move down to the first row
        y += rowHeight // Reassign y

// Iterate through the list of Chief Complaints and draw each as a row
        if (!chiefComplaints.isNullOrEmpty()) {
            var chiefComplaintCount: Int = 0
            for (chiefComplaint in chiefComplaints) {
                // Draw each field with a fixed width
                if (chiefComplaint != null) {
                    chiefComplaintCount++
                    drawTextWithWrapping(
                        canvas,
                        chiefComplaintCount.toString(),
                        xPosition,
                        y,
                        chiefComplaintColumnWidth,
                        content
                    )
                    drawTextWithWrapping(
                        canvas,
                        chiefComplaint.chiefComplaint ?: "",
                        xPosition + chiefComplaintColumnWidth,
                        y,
                        chiefComplaintColumnWidth,
                        content
                    )
                    drawTextWithWrapping(
                        canvas,
                        chiefComplaint.duration ?: "",
                        xPosition + 2 * chiefComplaintColumnWidth,
                        y,
                        chiefComplaintColumnWidth,
                        content
                    )
                    drawTextWithWrapping(
                        canvas,
                        chiefComplaint.durationUnit ?: "",
                        xPosition + 3 * chiefComplaintColumnWidth,
                        y,
                        chiefComplaintColumnWidth,
                        content
                    )
                    drawTextWithWrapping(
                        canvas,
                        chiefComplaint.description ?: "",
                        xPosition + 4 * chiefComplaintColumnWidth,
                        y,
                        chiefComplaintColumnWidth,
                        content
                    )

                    // Move down to the next row
                    y += rowHeight // Reassign y
                }
            }
        }

        canvas.drawLine(leftSideX, y, pageWidth - leftSideX, y, subheading)
        y += spaceAfterLine
        y += 30

        // Add a heading for the Vitals section
        val vitalsSectionHeader = "Vitals"
        val vitalsSectionHeaderSize = 25F
        val vitalsSectionHeaderX = (pageWidth / 2).toFloat()
        canvas.drawText(vitalsSectionHeader, vitalsSectionHeaderX, y, subheading.apply {
            textSize = vitalsSectionHeaderSize
            textAlign = Paint.Align.CENTER
        })

        // Move down to the first row of Vitals
        y += rowHeight

        // Define fixed column widths for Vitals
        val vitalsColumnWidth = 200F

        // Draw table header for Vitals
        canvas.drawText("Vitals Name", xPosition + leftSideX, y, subheading)
        canvas.drawText("Vitals Value", xPosition + leftSideX + vitalsColumnWidth, y, subheading)

        // Move down to the first row
        y += rowHeight

        // Function to draw Vitals Name and Value
        fun drawVitals(vitalsName: String, vitalsValue: String) {
            drawTextWithWrapping(
                canvas, vitalsName, xPosition + leftSideX, y, vitalsColumnWidth, content
            )
            drawTextWithWrapping(
                canvas,
                vitalsValue,
                xPosition + leftSideX + vitalsColumnWidth,
                y,
                vitalsColumnWidth,
                content
            )
            y += rowHeight
        }

        // Draw Vitals based on the available data
        with(vitals) {
            this?.height?.let { drawVitals("Height", it) }
            this?.weight?.let { drawVitals("Weight", it) }
            this?.bmi?.let { drawVitals("BMI", it) }
            this?.waistCircumference?.let { drawVitals("Waist Circumference", it) }
            this?.temperature?.let { drawVitals("Temperature", it) }
            this?.pulseRate?.let { drawVitals("Pulse Rate", it) }
            this?.spo2?.let { drawVitals("SpO2", it) }
            this?.bpSystolic?.let { drawVitals("BP Systolic", it) }
            this?.bpDiastolic?.let { drawVitals("BP Diastolic", it) }
            this?.respiratoryRate?.let { drawVitals("Respiratory Rate", it) }
            this?.rbs?.let { drawVitals("RBS", it) }
        }

// Draw heading for the next section
        val nextSectionHeader = "Prescription" // Replace with your desired heading
        val nextSectionHeaderSize = 25F // Adjust the size as needed
        val nextSectionHeaderX = (pageWidth / 2).toFloat() // Center the heading
        canvas.drawText(nextSectionHeader, nextSectionHeaderX, y, subheading.apply {
            textSize = nextSectionHeaderSize
            textAlign = Paint.Align.CENTER
        })
        y += rowHeight


        // Draw table header
        canvas.drawText("S.No.", xPosition + extraSpace, y, subheading)
        canvas.drawText("Medication", xPosition + columnWidth, y, subheading)
        canvas.drawText("Frequency", xPosition + 2 * columnWidth, y, subheading)
        canvas.drawText("Duration", xPosition + 3 * columnWidth, y, subheading)
        canvas.drawText("Instructions", xPosition + 4 * columnWidth, y, subheading)

        // Move down to the first row
        y += rowHeight // Reassign y

        // Iterate through the list of prescriptions and draw each as a row
        if (!prescriptions.isNullOrEmpty()) {
            var count: Int = 0
            for (prescription in prescriptions) {
                // Draw each field with a fixed width
                if (prescription != null) {
                    count++
                    drawTextWithWrapping(
                        canvas, count.toString(), xPosition, y, columnWidth, content
                    )
                    drawTextWithWrapping(
                        canvas,
                        prescription.itemName,
                        xPosition + columnWidth,
                        y,
                        columnWidth,
                        content
                    )
                    drawTextWithWrapping(
                        canvas,
                        prescription.frequency ?: "",
                        xPosition + 2 * columnWidth,
                        y,
                        columnWidth,
                        content
                    )
                    if (prescription.unit.isNullOrEmpty()) {
                        drawTextWithWrapping(
                            canvas,
                            (prescription.duration) ?: "",
                            xPosition + 3 * columnWidth,
                            y,
                            columnWidth,
                            content
                        )
                    } else {
                        drawTextWithWrapping(
                            canvas,
                            (prescription.duration + " " + prescription.unit),
                            xPosition + 3 * columnWidth,
                            y,
                            columnWidth,
                            content
                        )
                    }

                    drawTextWithWrapping(
                        canvas,
                        prescription.instructions,
                        xPosition + 4 * columnWidth,
                        y,
                        columnWidth,
                        content
                    )

                    // Move down to the next row
                    y += rowHeight // Reassign y
                }
            }
        }

        pdfDocument.finishPage(myPage)

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName: String = "Prescription_$patientName" + "_${timeStamp}_.pdf"

        showDownloadingNotification(fileName)

        val outputStream: OutputStream
        var pdfUri: Uri? = null
        var file: File? = null
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val result = createPdfForApi33(fileName)
            outputStream = result.first
            pdfUri = result.second
        } else {
            val downloadsDirectory: File =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            file = File(downloadsDirectory, fileName)
            outputStream = FileOutputStream(file)
        }

        try {
            pdfDocument.writeTo(outputStream)
            outputStream.close()

            if (pdfUri == null && file != null) {
                pdfUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().packageName + ".provider",
                    file
                )
            }

            dismissNotification(0)
            pdfUri?.let {
                showDownloadCompleteNotification(fileName, it)
            }

//            Toast.makeText(
//                requireContext(), "PDF file generated for Prescription.", Toast.LENGTH_SHORT
//            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
            dismissNotification(0)
            Toast.makeText(requireContext(), "Failed to generate PDF file", Toast.LENGTH_SHORT)
                .show()
        } finally {
            pdfDocument.close()
        }
    }

    private fun drawTextWithWrapping(
        canvas: Canvas, text: String?, x: Float, y: Float, maxWidth: Float, paint: Paint
    ) {
        var yPos = y
        val textLines = wrapText(text ?: "", paint, maxWidth)
        for (line in textLines) {
            canvas.drawText(line, x, yPos, paint)
            yPos += paint.textSize
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val result = mutableListOf<String>()
        val words = text.split(" ")
        var currentLine = ""
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val lineWidth = paint.measureText(testLine)
            if (lineWidth <= maxWidth) {
                currentLine = testLine
            } else {
                result.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) {
            result.add(currentLine)
        }
        return result
    }

    private fun createPdfForApi33(fileName: String): Pair<OutputStream, Uri> {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val pdfUri: Uri? = requireContext().contentResolver.insert(
            MediaStore.Files.getContentUri("external"), contentValues
        )
        val outst = pdfUri?.let { requireContext().contentResolver.openOutputStream(it) }!!
        Objects.requireNonNull(outst)
        Objects.requireNonNull(pdfUri)
        return Pair(outst, pdfUri)
    }

    private fun showDownloadingNotification(fileName: String) {
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                prescriptionChannelId,
                "Prescription Download",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(requireContext(), prescriptionChannelId)
            .setContentTitle("Downloading Prescription")
            .setContentText("Generating PDF: $fileName")
            .setSmallIcon(R.drawable.ic_download)
            .setProgress(100, 0, true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(0, notification)
    }

    private fun showDownloadCompleteNotification(fileName: String, uri: Uri) {
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                prescriptionChannelId,
                "Prescription Download",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(requireContext(), prescriptionChannelId)
            .setContentTitle("Download Complete")
            .setContentText("Prescription PDF: $fileName")
            .setSmallIcon(R.drawable.ic_download)
            .setAutoCancel(true)
            .setOngoing(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(
                PendingIntent.getActivity(
                    requireContext(),
                    0,
                    Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

        notificationManager.notify(1, notification)
    }

    private fun dismissNotification(notificationId: Int) {
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }

    private val searchTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            //No-Ops for now
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            //No-Ops for now
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun afterTextChanged(p0: Editable?) {
            val query = p0?.toString()?.trim().orEmpty()
            searchJob?.cancel()
            currentSearchQuery = query

            if (query.isBlank()) {
                isShowingSearchResults = false
                viewModel.filterText("")
                binding.patientListContainer.patientList.adapter = itemAdapter
                val count = itemAdapter?.itemCount ?: patientCount
                binding.patientListContainer.patientCount.text =
                    count.toString() + getResultStr(count)
                return
            }

            searchJob = lifecycleScope.launch(Dispatchers.IO) {

                val local = patientDao.getPatientList()
                val localMatches = local.filter {
                    val fn = it.patient.firstName.orEmpty()
                    val ln = it.patient.lastName.orEmpty()
                    val rid = it.patient.beneficiaryRegID?.toString().orEmpty()
                    "$fn $ln".contains(query, true) ||
                            fn.contains(query, true) ||
                            ln.contains(query, true) ||
                            rid.contains(query)
                }

                if (!isNetworkAvailable) {
                    withContext(Dispatchers.Main) {
                        if (currentSearchQuery == query) {
                            isShowingSearchResults = false
                            viewModel.filterText(query)
                            binding.patientListContainer.patientList.adapter = itemAdapter
                            binding.patientListContainer.patientCount.text =
                                localMatches.size.toString() + getResultStr(localMatches.size)
                        }
                    }
                    return@launch
                }

                try {
                    val response = amritApiService.quickSearchES(
                        mapOf("search" to query)
                    )

                    val body = response.body()?.string().orEmpty()
                    val root = JSONObject(body)
                    val dataArr = root.optJSONArray("data") ?: JSONArray()

                    if (dataArr.length() == 0) {
                        withContext(Dispatchers.Main) {
                            if (currentSearchQuery == query) {
                                isShowingSearchResults = false
                                viewModel.filterText(query)
                                binding.patientListContainer.patientList.adapter = itemAdapter
                                binding.patientListContainer.patientCount.text =
                                    localMatches.size.toString() + getResultStr(localMatches.size)
                            }
                        }
                        return@launch
                    }

                    val list = mutableListOf<PatientDisplayWithVisitInfo>()
                    for (i in 0 until dataArr.length()) {
                        val obj = dataArr.getJSONObject(i)

                        val firstName = obj.optString("firstName")
                        val lastName = obj.optString("lastName")
                        val beneficiaryRegID = obj.optLong("beneficiaryRegID")

                        var beneficiaryID: Long? = null
                        if (obj.has("beneficiaryID") && !obj.isNull("beneficiaryID")) {
                            try {
                                beneficiaryID = if (obj.opt("beneficiaryID") is String) {
                                    obj.optString("beneficiaryID").toLongOrNull()
                                } else {
                                    obj.optLong("beneficiaryID")
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "Error parsing beneficiaryID")
                            }
                        }

                        var dob: Date? = null
                        if (obj.has("dob") && !obj.isNull("dob")) {
                            val dobString = obj.optString("dob", "")
                            if (dobString.isNotEmpty() && dobString != "null" && dobString.lowercase() != "null") {
                                try {
                                    dob = DateTimeUtil.formatUTCToDate(dobString)
                                    Timber.d("Successfully parsed DOB: $dobString -> $dob")
                                } catch (e: Exception) {
                                    Timber.e(e, "Error parsing DOB: $dobString")
                                }
                            }
                        }

                        var genderID: Int? = null
                        if (obj.has("genderID")) {
                            genderID = obj.optInt("genderID")
                        } else if (obj.has("m_gender")) {
                            val mGender = obj.optJSONObject("m_gender")
                            genderID = mGender?.optInt("genderID")
                        }

                        val iBendemographics = obj.optJSONObject("i_bendemographics")
                        var stateID: Int? = null
                        var districtID: Int? = null
                        var blockID: Int? = null
                        var districtBranchID: Int? = null
                        var villageName: String? = null

                        if (iBendemographics != null) {
                            if (iBendemographics.has("stateID")) {
                                stateID = iBendemographics.optInt("stateID")
                            } else if (iBendemographics.has("m_state")) {
                                val mState = iBendemographics.optJSONObject("m_state")
                                stateID = mState?.optInt("stateID")
                            }

                            if (iBendemographics.has("districtID")) {
                                districtID = iBendemographics.optInt("districtID")
                            } else if (iBendemographics.has("m_district")) {
                                val mDistrict = iBendemographics.optJSONObject("m_district")
                                districtID = mDistrict?.optInt("districtID")
                            }

                            if (iBendemographics.has("blockID")) {
                                blockID = iBendemographics.optInt("blockID")
                            } else if (iBendemographics.has("m_districtblock")) {
                                val mDistrictBlock = iBendemographics.optJSONObject("m_districtblock")
                                blockID = mDistrictBlock?.optInt("blockID")
                            }

                            if (iBendemographics.has("villageID")) {
                                districtBranchID = iBendemographics.optInt("villageID")
                            } else if (iBendemographics.has("districtBranchID")) {
                                districtBranchID = iBendemographics.optInt("districtBranchID")
                            }

                            villageName = iBendemographics.optString("villageName")
                        }

                        var phoneNo: String? = null
                        if (obj.has("benPhoneMaps")) {
                            val benPhoneMaps = obj.optJSONArray("benPhoneMaps")
                            if (benPhoneMaps != null && benPhoneMaps.length() > 0) {
                                val phoneMap = benPhoneMaps.getJSONObject(0)
                                phoneNo = phoneMap.optString("phoneNo")
                            }
                        }

                        val age = if (obj.has("age")) obj.optInt("age") else null

                        val spouseName = obj.optString("spouseName").takeIf { it.isNotEmpty() }

                        val parentName = obj.optString("fatherName").takeIf { it.isNotEmpty() }

                        val patient = Patient(
                            patientID = generateUuid(),
                            firstName = firstName,
                            lastName = lastName,
                            beneficiaryRegID = beneficiaryRegID,
                            beneficiaryID = beneficiaryID,
                            syncState = SyncState.UNSYNCED,
                            dob = dob,
                            genderID = genderID,
                            age = age,
                            phoneNo = phoneNo,
                            spouseName = spouseName,
                            parentName = parentName,
                            stateID = stateID,
                            districtID = districtID,
                            blockID = blockID,
                            districtBranchID = districtBranchID
                        )

                        list.add(
                            PatientDisplayWithVisitInfo(
                                patient = patient,
                                genderName = obj.optString("genderName"),
                                villageName = villageName,
                                ageUnit = null,
                                maritalStatus = null,
                                nurseDataSynced = null,
                                doctorDataSynced = null,
                                createNewBenFlow = null,
                                prescriptionID = null,
                                benVisitNo = null,
                                visitCategory = null,
                                benFlowID = null,
                                nurseFlag = null,
                                doctorFlag = null,
                                labtechFlag = null,
                                pharmacist_flag = null,
                                visitDate = null,
                                referDate = null,
                                referTo = null,
                                referralReason = null
                            )
                        )
                    }

                    withContext(Dispatchers.Main) {
                        if (currentSearchQuery == query) {
                            isShowingSearchResults = true
                            apiSearchAdapter?.submitList(list)
                            binding.patientListContainer.patientList.adapter = apiSearchAdapter
                            binding.patientListContainer.patientCount.text =
                                list.size.toString() + getResultStr(list.size)
                        }
                    }

                } catch (e: Exception) {
                    Timber.e(e, "API error → fallback to local")

                    withContext(Dispatchers.Main) {
                        if (currentSearchQuery == query) {
                            isShowingSearchResults = false
                            viewModel.filterText(query)
                            binding.patientListContainer.patientList.adapter = itemAdapter
                            binding.patientListContainer.patientCount.text =
                                localMatches.size.toString() + getResultStr(localMatches.size)
                        }
                    }
                }
            }
        }
    }

    fun getResultStr(count: Int?): String {
        if (count == 1 || count == 0) {
            return getString(R.string.patient_cnt_display)
        }
        return getString(R.string.patients_cnt_display)
    }

    private val speechToTextLauncherForSearchByName =
        registerForActivityResult(SpeechToTextContract()) { result ->
            if (result.isNotBlank() && result.isNotEmpty() && !result.any { it.isDigit() }) {
                binding.search.setText(result)
                binding.search.addTextChangedListener(searchTextWatcher)
            }
        }

    private fun encryptSHA512(input: String): String {
        val digest = MessageDigest.getInstance("SHA-512")
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun callLoginDialog(benVisitInfo: PatientDisplayWithVisitInfo) {
        if (benVisitInfo.patient.phoneNo.isNullOrEmpty()) {
            context?.let {
                MaterialAlertDialogBuilder(it).setTitle(getString(R.string.alert_popup))
                    .setMessage(getString(R.string.phone_no_not_found))
                    .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                        dialog.dismiss()
                    }.create().show()
            }
        } else {
            network = isInternetAvailable(requireContext())
            val dialogView =
                LayoutInflater.from(context).inflate(R.layout.dialog_esanjeevani_login, null)
            val dialog = context?.let {
                MaterialAlertDialogBuilder(it).setTitle("eSanjeevani Login").setView(dialogView)
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }.create()
            }
            dialog?.show()
            val loginBtn = dialogView.findViewById<MaterialButton>(R.id.loginButton)
            val rememberMeEsanjeevani = dialogView.findViewById<CheckBox>(R.id.cb_remember_es)
            if (network) {
                dialogView.findViewById<ConstraintLayout>(R.id.cl_error_es).visibility = View.GONE
                dialogView.findViewById<LinearLayout>(R.id.ll_login_es).visibility = View.VISIBLE
                val rememberedUsername: String? = viewModel.fetchRememberedUsername()
                val rememberedPassword: String? = viewModel.fetchRememberedPassword()
                if (!rememberedUsername.isNullOrBlank() && !rememberedPassword.isNullOrBlank()) {
                    dialogView.findViewById<TextInputEditText>(R.id.et_username_es).text =
                        Editable.Factory.getInstance().newEditable(rememberedUsername)
                    dialogView.findViewById<TextInputEditText>(R.id.et_password_es).text =
                        Editable.Factory.getInstance().newEditable(rememberedPassword)
                    rememberMeEsanjeevani.isChecked = true
                }
            } else {
                dialogView.findViewById<LinearLayout>(R.id.ll_login_es).visibility = View.GONE
                dialogView.findViewById<ConstraintLayout>(R.id.cl_error_es).visibility =
                    View.VISIBLE
            }


            loginBtn.setOnClickListener {

                usernameEs =
                    dialogView.findViewById<TextInputEditText>(R.id.et_username_es).text.toString()
                        .trim()
                passwordEs =
                    dialogView.findViewById<TextInputEditText>(R.id.et_password_es).text.toString()
                        .trim()
                if (rememberMeEsanjeevani.isChecked) {
                    viewModel.rememberUserEsanjeevani(usernameEs, passwordEs)
                } else {
                    viewModel.forgetUserEsanjeevani()
                }
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        var passWord =
                            encryptSHA512(encryptSHA512(passwordEs) + encryptSHA512("token"))

                        var networkBody = NetworkBody(
                            usernameEs, passWord, "token", "11001"
                        )
                        val errorTv = dialogView.findViewById<MaterialTextView>(R.id.tv_error_es)
                        network = isInternetAvailable(requireContext())
                        if (!network) {
                            errorTv.text = requireContext().getString(R.string.network_error)
                            errorTv.visibility = View.VISIBLE
                        } else {
                            errorTv.text = ""
                            errorTv.visibility = View.GONE
                            val responseToken = apiService.getJwtToken(networkBody)
                            if (responseToken.message == "Success") {
                                val token = responseToken.model?.access_token
                                if (token != null) {
                                    TokenESanjeevaniInterceptor.setToken(token)
                                }
                                val intent = Intent(context, WebViewActivity::class.java)
                                intent.putExtra("patientId", benVisitInfo.patient.patientID)
                                intent.putExtra("usernameEs", usernameEs)
                                intent.putExtra("passwordEs", passwordEs)
                                context?.startActivity(intent)
                                dialog?.dismiss()
                            } else {
                                errorEs = responseToken.message
                                errorTv.text = errorEs
                                errorTv.visibility = View.VISIBLE
                            }
                        }
                    } catch (e: Exception) {
                        Timber.d("GHere is error $e")
                    }
                }
            }
        }
    }

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null && (networkCapabilities.hasTransport(
            NetworkCapabilities.TRANSPORT_WIFI
        ) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    private fun checkAndGenerateABHA(benVisitInfo: PatientDisplayWithVisitInfo) {
        Log.d("checkAndGenerateABHA click listener", "checkAndGenerateABHA click listener")
        viewModel.fetchAbha(benVisitInfo.patient.beneficiaryID!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
