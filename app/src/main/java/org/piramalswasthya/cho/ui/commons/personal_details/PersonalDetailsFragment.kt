package org.piramalswasthya.cho.ui.commons.personal_details

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import org.piramalswasthya.cho.facenet.FaceNetModel
import org.piramalswasthya.cho.facenet.Models
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.PatientItemAdapter
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentPersonalDetailsBinding
import org.piramalswasthya.cho.model.BenFlow
import org.piramalswasthya.cho.model.NetworkBody
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.network.ESanjeevaniApiService
import org.piramalswasthya.cho.network.interceptors.TokenESanjeevaniInterceptor
import org.piramalswasthya.cho.repositories.CaseRecordeRepo
import org.piramalswasthya.cho.repositories.VisitReasonsAndCategoriesRepo
import org.piramalswasthya.cho.repositories.VitalsRepo
import org.piramalswasthya.cho.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.cho.ui.commons.SpeechToTextContract
import org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity
import org.piramalswasthya.cho.ui.home.HomeViewModel
import org.piramalswasthya.cho.ui.register_patient_activity.RegisterPatientActivity
import org.piramalswasthya.cho.ui.web_view_activity.WebViewActivity
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects
import javax.inject.Inject
import kotlin.collections.set
import kotlin.math.pow


@AndroidEntryPoint
class PersonalDetailsFragment : Fragment() {
    @Inject
    lateinit var apiService : ESanjeevaniApiService
    private lateinit var viewModel: PersonalDetailsViewModel
    private lateinit var homeviewModel: HomeViewModel
    private var itemAdapter : PatientItemAdapter? = null
    private var usernameEs : String = ""
    private var passwordEs : String = ""
    private var errorEs : String = ""
    private var network : Boolean = false
    private var currentFileName: String? = null
    private lateinit var  photoURI: Uri
    private var currentPhotoPath: String? = null
    //facenet
    private val useGpu = false
    private val useXNNPack = true
    private val modelInfo = Models.FACENET
    private lateinit var faceNetModel : FaceNetModel
    private var embeddings: FloatArray? = null
    private lateinit var dialog: AlertDialog

    private val benFlowMap = mutableMapOf<Int, BenFlow>()
    private var benFlowListCache: List<BenFlow> = emptyList()
    private var isFollowupVisit: Boolean? = null

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
    private var _binding: FragmentPersonalDetailsBinding? = null
    private var patientCount : Int = 0

    private val binding
        get() = _binding!!

    private val abhaDisclaimer by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.beneficiary_abha_number))
            .setMessage("it")
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        HomeViewModel.resetSearchBool()
        _binding = FragmentPersonalDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HomeViewModel.searchBool.observe(viewLifecycleOwner){
                bool ->
            when(bool!!) {
                true ->{
                    binding.search.requestFocus()
                    activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

                }
                else -> {}
            }

        }
        binding.cameraIcon.setOnClickListener{

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
        viewModel = ViewModelProvider(this).get(PersonalDetailsViewModel::class.java)
        viewModel.patientObserver.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                PersonalDetailsViewModel.NetworkState.SUCCESS -> {
                    var result = ""
                    if(itemAdapter?.itemCount==0||itemAdapter?.itemCount==1) {
                        result = getString(R.string.patient_cnt_display)
                    }
                    else {
                        result = getString(R.string.patients_cnt_display)
                    }
                    itemAdapter = context?.let { it ->
                        PatientItemAdapter(
                            apiService,
                            it,
                            clickListener = PatientItemAdapter.BenClickListener(
                                {
                                        benVisitInfo ->

                                    benVisitInfo.patient.beneficiaryID?.let { beneficiaryID ->
                                        viewModel.getVisitReasonByBenFlowID(beneficiaryID)
                                    } ?: Timber.d("benFlowID is null, cannot get VisitReason")


                                    if(preferenceDao.isRegistrarSelected()){

                                    }
                                    else if( benVisitInfo.nurseFlag == 9 && benVisitInfo.doctorFlag == 2 && preferenceDao.isDoctorSelected() ){
                                        Toast.makeText(
                                            requireContext(),
                                            resources.getString(R.string.pendingForLabtech),
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    }
                                    else if( benVisitInfo.nurseFlag == 9 && benVisitInfo.doctorFlag == 9 && preferenceDao.isDoctorSelected() ){
//                                        Toast.makeText(
//                                            requireContext(),
//                                            resources.getString(R.string.flowCompleted),
//                                            Toast.LENGTH_SHORT
//                                        ).show()
                                        var modifiedInfo = benVisitInfo
                                        if(preferenceDao.isNurseSelected()){
                                            modifiedInfo = PatientDisplayWithVisitInfo(benVisitInfo)
                                        }

                                        viewModel.benFlows.observe(viewLifecycleOwner) { benFlowList ->
                                            if (benFlowList.isNullOrEmpty()) return@observe

                                            val distinctList =
                                                benFlowList.distinctBy { it.benVisitNo }

                                            benFlowMap.clear()
                                            distinctList.forEach { benFlow ->
                                                benFlow.benVisitNo?.let { visitNo ->
                                                    benFlowMap[visitNo] = benFlow
                                                }
                                            }

                                            benFlowListCache = benFlowMap.values.toList()

                                            isFollowupVisit =
                                                benFlowListCache.lastOrNull()?.VisitReason == "Follow Up"

                                            val intent = Intent(
                                                context,
                                                EditPatientDetailsActivity::class.java
                                            )
                                            intent.putExtra("benVisitInfo", modifiedInfo);
                                            intent.putExtra("viewRecord", true)
                                            intent.putExtra("isFlowComplete", true)
                                            intent.putExtra("isFollowupVisit", isFollowupVisit)
                                            startActivity(intent)
                                            requireActivity().finish()
                                        }
                                    }
                                    else{
                                        viewModel.benFlows.observe(viewLifecycleOwner) { benFlowList ->
                                            if (benFlowList.isNullOrEmpty()) return@observe

                                            val distinctList =
                                                benFlowList.distinctBy { it.benVisitNo }

                                            benFlowMap.clear()
                                            distinctList.forEach { benFlow ->
                                                benFlow.benVisitNo?.let { visitNo ->
                                                    benFlowMap[visitNo] = benFlow
                                                }
                                            }

                                            benFlowListCache = benFlowMap.values.toList()

                                            isFollowupVisit =
                                                benFlowListCache.lastOrNull()?.VisitReason == "Follow Up"

                                        var modifiedInfo = benVisitInfo
                                        if(preferenceDao.isNurseSelected()){
                                            modifiedInfo = PatientDisplayWithVisitInfo(benVisitInfo)
                                        }
                                        val intent = Intent(context, EditPatientDetailsActivity::class.java)
                                        intent.putExtra("benVisitInfo", modifiedInfo);
                                        intent.putExtra("viewRecord", false)
                                        intent.putExtra("isFlowComplete", false)
                                        intent.putExtra("isFollowupVisit", isFollowupVisit)
                                        startActivity(intent)
                                        requireActivity().finish()
                                    }
                                    }
                                },
                                {
                                        benVisitInfo ->

                                    checkAndGenerateABHA(benVisitInfo)
                                },
                                {
                                        benVisitInfo -> callLoginDialog(benVisitInfo)
                                },
                                {
                                        benVisitInfo ->
                                    lifecycleScope.launch {
                                        generatePDF(benVisitInfo)
                                    }

                                },
                                {
                                        benVisitInfo ->  openDialog(benVisitInfo)
                                }
                            ),
                            showAbha = true
                        )
                    }

                    binding.patientListContainer.patientList.adapter = itemAdapter

                    if(preferenceDao.isRegistrarSelected() || preferenceDao.isNurseSelected()){
                        lifecycleScope.launch {
                            viewModel.patientListForNurse?.collect { it ->
                                itemAdapter?.submitList(it.sortedByDescending { it.patient.registrationDate})
                                binding.patientListContainer.patientCount.text =
                                    it.size.toString() + getResultStr(it.size)
                                patientCount = it.size
                            }
                        }
                    }
                    else if(preferenceDao.isDoctorSelected()){
                        lifecycleScope.launch {
                            viewModel.patientListForDoctor?.collect { it ->
                                itemAdapter?.submitList(it.sortedByDescending { it.patient.registrationDate})
                                binding.patientListContainer.patientCount.text =
                                    it.size.toString() + getResultStr(it.size)
                                patientCount = it.size
                            }
                        }
                    }
                    else if(preferenceDao.isLabSelected()){
                        lifecycleScope.launch {
                            viewModel.patientListForLab?.collect { it ->
                                itemAdapter?.submitList(it.sortedByDescending { it.patient.registrationDate})
                                binding.patientListContainer.patientCount.text =
                                    it.size.toString() + getResultStr(it.size)
                                patientCount = it.size
                            }
                        }
                    }
                    else if(preferenceDao.isPharmaSelected()){
                        lifecycleScope.launch {
                            viewModel.patientListForPharmacist?.collect { it ->
                                itemAdapter?.submitList(it.sortedByDescending { it.patient.registrationDate})
                                binding.patientListContainer.patientCount.text =
                                    itemAdapter?.itemCount.toString() + getResultStr(itemAdapter?.itemCount)
                                patientCount = it.size
                            }
                        }
                    }

                }

                else -> {

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
                if (b)
                    (searchView as EditText).addTextChangedListener(searchTextWatcher)
                else
                    (searchView as EditText).removeTextChangedListener(searchTextWatcher)

            }
        }
    }

    private lateinit var syncBottomSheet : SyncBottomSheetFragment
    private fun openDialog(benVisitInfo: PatientDisplayWithVisitInfo) {
        syncBottomSheet = SyncBottomSheetFragment(benVisitInfo)
        if(!syncBottomSheet.isVisible)
            syncBottomSheet.show(childFragmentManager, resources.getString(R.string.sync))
        Timber.tag("sync").i("${benVisitInfo}")
    }

    var pageHeight = 1120
    var pageWidth = 792


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
                val highspeed = FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .build()
                val detector = FaceDetection.getClient(highspeed)
                val image = InputImage.fromFilePath(requireContext(), photoURI)
                detector.process(image)
                    .addOnSuccessListener { faces ->
                        if (faces.isEmpty()) {
                            Toast.makeText(requireContext(), "No face detected", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        } else if (faces.size > 1) {
                            Toast.makeText(requireContext(), "Multiple faces detected", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        } else {
                            val face = faces[0]
                            val boundingBox = face.boundingBox
                            val imageBitmap = MediaStore.Images.Media.getBitmap(
                                requireContext().contentResolver,
                                photoURI
                            )
                            val faceBitmap = Bitmap.createBitmap(
                                imageBitmap,
                                boundingBox.left,
                                boundingBox.top,
                                boundingBox.width(),
                                boundingBox.height()
                            )
                            embeddings = faceNetModel.getFaceEmbedding(faceBitmap)
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
                                    binding.patientListContainer.patientCount.text = "1 Matched Patient"
                                    Toast.makeText(requireContext(), "1 matching patient found", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(requireContext(), "No matching patient found", Toast.LENGTH_SHORT).show()
                                    searchPrompt.show()
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("FaceDetection", "Face detection failed", e)
                        Toast.makeText(requireContext(), "Face detection failed", Toast.LENGTH_SHORT).show()
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
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.note_ben_reg))
            .setMessage(getString(R.string.no_patient_found))
            .setPositiveButton("Search") { dialog, _ ->
                dialog.dismiss()
                HomeViewModel.setSearchBool()
            }
            .setNegativeButton("Proceed with Registration"){dialog, _->
                val intent = Intent(context, RegisterPatientActivity::class.java).apply {
                putExtra("photoUri", photoURI.toString())
                putExtra("facevector", embeddings)
            }
                startActivity(intent)
                dialog.dismiss()
                HomeViewModel.resetSearchBool()
            }
            .create()
    }

    private suspend fun generatePDF(benVisitInfo: PatientDisplayWithVisitInfo) {
        val patientName = (benVisitInfo.patient.firstName?:"") + " " + (benVisitInfo.patient.lastName?:"")
        val prescriptions = caseRecordeRepo.getPrescriptionCaseRecordeByPatientIDAndBenVisitNo(patientID =
        benVisitInfo.patient.patientID,benVisitNo = benVisitInfo.benVisitNo!!)
        val chiefComplaints = visitReasonsAndCategoriesRepo.getChiefComplaintDBByPatientId(patientID =
        benVisitInfo.patient.patientID,benVisitNo = benVisitInfo.benVisitNo!!)
        val vitals = vitalsRepo.getPatientVitalsByPatientIDAndBenVisitNo(patientID =
        benVisitInfo.patient.patientID,benVisitNo = benVisitInfo.benVisitNo!!)
//        Log.d("prescriptionMsg", prescriptions.toString())

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
        val spaceBetweenNameAndPrescription = 30F
        val leftSideX = 50F
        val rightSideX = 400F
        val extraSpace = 10F
        val middleX = 220F
        val bottomRightX = 400F
        val yPosition = 270F

        // Set up Paint for text
        val textPaint: Paint = Paint().apply {
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
        currentY += lineHeight

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
        y+=30

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
        canvas.drawText("S.No.", xPosition+extraSpace, y, subheading)
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
        y+=30

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
        canvas.drawText("Vitals Name", xPosition+leftSideX, y, subheading)
        canvas.drawText("Vitals Value", xPosition+leftSideX + vitalsColumnWidth, y, subheading)

        // Move down to the first row
        y += rowHeight

        // Function to draw Vitals Name and Value
        fun drawVitals(vitalsName: String, vitalsValue: String) {
            drawTextWithWrapping(canvas, vitalsName, xPosition+leftSideX, y, vitalsColumnWidth, content)
            drawTextWithWrapping(canvas, vitalsValue, xPosition+leftSideX + vitalsColumnWidth, y, vitalsColumnWidth, content)
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
        canvas.drawText("S.No.", xPosition+extraSpace, y, subheading)
        canvas.drawText("Medication", xPosition + columnWidth, y, subheading)
        canvas.drawText("Frequency", xPosition + 2 * columnWidth, y, subheading)
        canvas.drawText("Duration", xPosition + 3 * columnWidth, y, subheading)
//        canvas.drawText("Quantity", xPosition + 4 * columnWidth, y, subheading)
//        canvas.drawText("Instructions", xPosition + 5 * columnWidth, y, subheading)
        canvas.drawText("Instructions", xPosition + 4 * columnWidth, y, subheading)

        // Move down to the first row
        y += rowHeight // Reassign y

        // Iterate through the list of prescriptions and draw each as a row
        if (!prescriptions.isNullOrEmpty()) {
            var count:Int = 0
            for (prescription in prescriptions) {
                // Draw each field with a fixed width
                if(prescription!=null) {
                    count++
                    drawTextWithWrapping(
                        canvas,
                        count.toString(),
                        xPosition,
                        y,
                        columnWidth,
                        content
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
//                    drawTextWithWrapping(
//                        canvas,
//                        prescription.quantityInHand.toString(),
//                        xPosition + 4 * columnWidth,
//                        y,
//                        columnWidth,
//                        content
//                    )
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
        val fileName : String =  "Prescription_$patientName"+"_${timeStamp}_.pdf"

        val outputStream: OutputStream
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            outputStream = createPdfForApi33(fileName)
        } else {
            val downloadsDirectory: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDirectory, fileName)

            outputStream = FileOutputStream(file)

        }

        try {
            pdfDocument.writeTo(outputStream)

            Toast.makeText(requireContext(), "PDF file generated for Prescription.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()

            Toast.makeText(requireContext(), "Failed to generate PDF file", Toast.LENGTH_SHORT)
                .show()
        }
        pdfDocument.close()
    }

    private fun drawTextWithWrapping(canvas: Canvas, text: String?, x: Float, y: Float, maxWidth: Float, paint: Paint) {
        var yPos = y
        val textLines = wrapText(text?:"", paint, maxWidth)
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

    private fun createPdfForApi33(fileName:String): OutputStream {
        val outst: OutputStream
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val pdfUri: Uri? = requireContext().contentResolver.insert(
            MediaStore.Files.getContentUri("external"),
            contentValues
        )
        outst = pdfUri?.let { requireContext().contentResolver.openOutputStream(it) }!!
        Objects.requireNonNull(outst)
        return outst
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun checkPermissions() {

        val permissions =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                )
            } else {
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        if (!hasPermission(permissions[0])) {
            permissionLauncher.launch(permissions)
        }
    }

    private var permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        var isGranted = true
        for (item in it){
            if (!item.value) {
                isGranted = false
            }
        }
        if (isGranted) {
            Toast.makeText(requireContext(), "Permissions Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Permissions Denied", Toast.LENGTH_SHORT).show()

        }
    }

    private val searchTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun afterTextChanged(p0: Editable?) {
            viewModel.filterText(p0?.toString() ?: "")
            binding.patientListContainer.patientCount.text =
                patientCount.toString() + getResultStr(patientCount)
            Log.d("arr","${patientCount}")
        }

    }
    fun getResultStr(count:Int?):String{
        if(count==1||count==0){
            return getString(R.string.patient_cnt_display)
        }
        return getString(R.string.patients_cnt_display)
    }
    private val speechToTextLauncherForSearchByName = registerForActivityResult(SpeechToTextContract()) { result ->
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
                    }.create()
                    .show()
            }
        } else{
            network = isInternetAvailable(requireContext())
            val dialogView =
                LayoutInflater.from(context).inflate(R.layout.dialog_esanjeevani_login, null)
            val dialog = context?.let {
                MaterialAlertDialogBuilder(it)
                    .setTitle("eSanjeevani Login")
                    .setView(dialogView)
                    .setNegativeButton("Cancel") { dialog, _ ->
                        // Handle cancel button click
                        dialog.dismiss()
                    }
                    .create()
            }
            dialog?.show()
            val loginBtn = dialogView.findViewById<MaterialButton>(R.id.loginButton)
            val rememberMeEsanjeevani = dialogView.findViewById<CheckBox>(R.id.cb_remember_es)
            if (network) {
                // Internet is available
                dialogView.findViewById<ConstraintLayout>(R.id.cl_error_es).visibility = View.GONE
                dialogView.findViewById<LinearLayout>(R.id.ll_login_es).visibility = View.VISIBLE
                val rememberedUsername : String? = viewModel.fetchRememberedUsername()
                val rememberedPassword : String? = viewModel.fetchRememberedPassword()
                if(!rememberedUsername.isNullOrBlank() && !rememberedPassword.isNullOrBlank()){
                    dialogView.findViewById<TextInputEditText>(R.id.et_username_es).text = Editable.Factory.getInstance().newEditable(rememberedUsername)
                    dialogView.findViewById<TextInputEditText>(R.id.et_password_es).text = Editable.Factory.getInstance().newEditable(rememberedPassword)
                    rememberMeEsanjeevani.isChecked = true
                }
            } else {
                dialogView.findViewById<LinearLayout>(R.id.ll_login_es).visibility = View.GONE
                dialogView.findViewById<ConstraintLayout>(R.id.cl_error_es).visibility = View.VISIBLE
            }


            loginBtn.setOnClickListener {

                usernameEs =
                    dialogView.findViewById<TextInputEditText>(R.id.et_username_es).text.toString()
                        .trim()
                passwordEs =
                    dialogView.findViewById<TextInputEditText>(R.id.et_password_es).text.toString()
                        .trim()
                if(rememberMeEsanjeevani.isChecked){
                    viewModel.rememberUserEsanjeevani(usernameEs,passwordEs)
                }else{
                    viewModel.forgetUserEsanjeevani()
                }
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        var passWord = encryptSHA512(encryptSHA512(passwordEs) + encryptSHA512("token"))

                        var networkBody = NetworkBody(
                            usernameEs,
                            passWord,
                            "token",
                            "11001"
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
                                val token = responseToken.model?.access_token;
                                if (token != null) {
                                    TokenESanjeevaniInterceptor.setToken(token)
                                }
                                val intent = Intent(context, WebViewActivity::class.java)
                                intent.putExtra("patientId", benVisitInfo.patient.patientID);
                                intent.putExtra("usernameEs", usernameEs);
                                intent.putExtra("passwordEs", passwordEs);
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
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            return networkCapabilities != null &&
                    (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }
    private fun checkAndGenerateABHA(benVisitInfo: PatientDisplayWithVisitInfo) {
        Log.d("checkAndGenerateABHA click listener","checkAndGenerateABHA click listener")
        viewModel.fetchAbha(benVisitInfo.patient.beneficiaryID!!)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}
