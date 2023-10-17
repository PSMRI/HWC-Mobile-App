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
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Annotation
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.ResourceType
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.ChiefComplaintMultiAdapter
import org.piramalswasthya.cho.adapter.SubCategoryAdapter
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.VisitDetailsInfoBinding
import org.piramalswasthya.cho.fhir_utils.FhirExtension
import org.piramalswasthya.cho.fhir_utils.extension_names.createdBy
import org.piramalswasthya.cho.fhir_utils.extension_names.duration
import org.piramalswasthya.cho.fhir_utils.extension_names.parkingPlaceID
import org.piramalswasthya.cho.fhir_utils.extension_names.providerServiceMapId
import org.piramalswasthya.cho.fhir_utils.extension_names.vanID
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.model.ChiefComplaintMaster
import org.piramalswasthya.cho.model.ChiefComplaintValues
import org.piramalswasthya.cho.model.MasterDb
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.SubVisitCategory
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.model.VisitMasterDb
import org.piramalswasthya.cho.model.VitalsMasterDb
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.mutualVisitUnitsVal
import org.piramalswasthya.cho.ui.commons.FhirFragmentService
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.SpeechToTextContract
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.utils.generateUuid
import org.piramalswasthya.cho.utils.nullIfEmpty
import org.piramalswasthya.cho.work.WorkerUtils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class FragmentVisitDetail : Fragment(), NavigationAdapter, FhirFragmentService,
    EndIconClickListener {

    override var fragmentContainerId = 0
    private lateinit var benVisitInfo: PatientDisplayWithVisitInfo
    private lateinit var patientId: String

    override val fragment = this
    override val viewModel: VisitDetailViewModel by viewModels()

    override val jsonFile = "patient-visit-details-paginated.json"

    private var usernameEs: String = ""
    private var passwordEs: String = ""
    private var userInfo: UserCache? = null

    private var _binding: VisitDetailsInfoBinding? = null
    private var chiefComplaints = ArrayList<ChiefComplaintMaster>()
    private var chiefComplaintsForFilter = ArrayList<ChiefComplaintMaster>()
    private var units = mutualVisitUnitsVal
    private var subCatOptions = ArrayList<SubVisitCategory>()

    private lateinit var subCatAdapter: SubCategoryAdapter
    private var isFileSelected: Boolean = false
    private var isFileUploaded: Boolean = false

    @Inject
    lateinit var preferenceDao: PreferenceDao
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
    private var masterDb: MasterDb? = null
    var heightValue: String? = null
    var weightValue: String? = null
    var bmiValue: String? = null
    var waistCircumferenceValue: String? = null
    var temperatureValue: String? = null
    var pulseRateValue: String? = null
    var spo2Value: String? = null
    var bpSystolicValue: String? = null
    var bpDiastolicValue: String? = null
    var respiratoryValue: String? = null
    var rbsValue: String? = null

    private lateinit var adapter: VisitDetailAdapter

    private val initialItem = ChiefComplaintValues()
    private val itemList = mutableListOf(initialItem)
    private val enCounterExtension: FhirExtension = FhirExtension(ResourceType.Encounter)
    private val conditionExtension: FhirExtension = FhirExtension(ResourceType.Condition)
    private lateinit var chAdapter: ChiefComplaintMultiAdapter
    var chiefComplaintDB2 = mutableListOf<ChiefComplaintDB>()
    private val binding: VisitDetailsInfoBinding
        get() {
            return _binding!!
        }
    private val speechToTextLauncherForDuration =
        registerForActivityResult(SpeechToTextContract()) { result ->
            if (result.isNotBlank() && result.isNumeric()) {
                val pattern = "\\d{2}".toRegex()
                val match = pattern.find(result)
                val firstTwoDigits = match?.value
                if (result.toInt() > 0) updateDurationText(firstTwoDigits!!)
            }
        }

    // method to check string contains numeric val
    private fun String.isNumeric(): Boolean {
        return try {
            this.toDouble()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }

    fun isWithinThreeDays(dateString: String?): Boolean {
        if (dateString.isNullOrEmpty()) {
            return false
        }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        try {
            val visitDate = dateFormat.parse(dateString)

            val currentDate = Calendar.getInstance().time

            val difference = visitDate.time - currentDate.time

            val differenceInDays = difference / (1000 * 60 * 60 * 24)

            return differenceInDays <= 3
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private val speechToTextLauncherForDesc =
        registerForActivityResult(SpeechToTextContract()) { result ->
            if (result.isNotBlank()) {
                updateDescText(result)
            }
        }
    private val speechToTextLauncherForChiefMaster =
        registerForActivityResult(SpeechToTextContract()) { result ->
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

    private val onBackPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onCancelAction()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (preferenceDao.isLoginTypeOutReach()) {
            binding.radioButton1.isChecked = false
            binding.radioButton2.isChecked = true
//            category = binding.radioButton2.text.toString()
            category = binding.radioButton2.tag.toString()
            reason = binding.radioButton3.text.toString()
            binding.subCatDropDown.visibility = View.VISIBLE
        } else {
            binding.radioButton2.isChecked = false
            binding.radioButton1.isChecked = true
//            category = binding.radioButton1.text.toString()
            category = binding.radioButton1.tag.toString()
//            binding.radioGroup2.visibility = View.VISIBLE
//            binding.reasonText.visibility = View.VISIBLE
            binding.subCatDropDown.visibility = View.GONE
//            category = binding.radioButton1.text.toString()
            category = binding.radioButton1.tag.toString()
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
        super.onViewCreated(view, savedInstanceState)
        subCatAdapter = SubCategoryAdapter(
            requireContext(),
            R.layout.dropdown_subcategory,
            R.id.tv_dropdown_item_text,
            subCatOptions.map { it.name })
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
                hintTextColor = defaultHintTextColor
            }
        }

        benVisitInfo =
            requireActivity().intent?.getSerializableExtra("benVisitInfo") as PatientDisplayWithVisitInfo
        if (benVisitInfo.patient != null) {
            viewModel.setPatientId(benVisitInfo.patient.patientID)
            masterDb?.patientId = benVisitInfo.patient.patientID
            patientId = benVisitInfo.patient.patientID
        }
        try {

            viewModel.getChiefComplaintDB(benVisitInfo.patient.patientID)

        } catch (e: Exception) {
            Log.d("arr", "$e")
        }
        if (benVisitInfo.benVisitNo != null) {
            viewModel.getTheProcedure(
                patientID = benVisitInfo.patient.patientID,
                benVisitNo = benVisitInfo.benVisitNo!!
            )
        }
        binding.usePrevious.setOnClickListener{
            goToEnd()
        }
        lifecycleScope.launch {
            viewModel.getVitalsDB(benVisitInfo.patient.patientID)
//            viewModel.getLastDate(patientId)
        }
        viewModel.lastVisitDate.observe(viewLifecycleOwner) {
            viewModel.setIsFollowUp(isWithinThreeDays(it))
            makeFollowUpDefault()
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
//                    binding.radioGroup2.visibility = View.VISIBLE
//                    binding.reasonText.visibility = View.VISIBLE
                    binding.subCatDropDown.visibility = View.GONE
//                    category = binding.radioButton1.text.toString()
                    category = binding.radioButton1.tag.toString()
                }

                else -> {
//                    binding.radioGroup2.visibility = View.GONE
//                    binding.reasonText.visibility = View.GONE
                    binding.subCatDropDown.visibility = View.VISIBLE
//                    category = binding.radioButton2.text.toString()
                    category = binding.radioButton2.tag.toString()
                }
            }
        }
        binding.radioGroup2.setOnCheckedChangeListener { _, checkedId ->
            reason = when (checkedId) {
                R.id.radioButton3 -> {
                    binding.chf.visibility = View.VISIBLE
                    binding.chiefComplaintExtra.visibility = View.VISIBLE
                    binding.chiefComplaintHeading.visibility = View.GONE
                    binding.chiefComplaintExtra2.visibility = View.GONE
                    binding.vitalsHeading.visibility = View.GONE
                    binding.vitalsLayout.visibility = View.GONE
                    binding.usePrevious.visibility = View.GONE
//                    binding.radioButton3.text.toString()
                    binding.radioButton3.tag.toString()
                }

                else -> {
                    chiefAndVitalsDataFill()
                    binding.radioButton4.tag.toString()
//                    binding.radioButton4.text.toString()
                }
            }
        }
//        binding.selectFileBtn.setOnClickListener {
//            openFilePicker()
//        }
//        binding.uploadFileBtn.setOnClickListener {
//            Toast.makeText(requireContext(), resources.getString(R.string.toast_file_uploaded), Toast.LENGTH_SHORT)
//                .show()
//            isFileUploaded = true
//            binding.uploadFileBtn.text = "Uploaded"
//            binding.uploadFileBtn.isEnabled = false
//        }
//        if (viewModel.fileName.isNotEmpty() && viewModel.base64String.isNotEmpty()) {
//            binding.uploadFileBtn.text = "Uploaded"
//            binding.selectFileText.setText(viewModel.fileName)
//        }
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

    private fun extractFormValues() {
        heightValue = binding.inputHeight.text?.toString()?.trim()
        weightValue = binding.inputWeight.text?.toString()?.trim()
        bmiValue = binding.inputBmi.text?.toString()?.trim()
//        waistCircumferenceValue = binding.inputWaistCircum.text?.toString()?.trim()
        temperatureValue = binding.inputTemperature.text?.toString()?.trim()
        pulseRateValue = binding.inputPulseRate.text?.toString()?.trim()
        spo2Value = binding.inputSpo2.text?.toString()?.trim()
        bpSystolicValue = binding.inputBpSystolic.text?.toString()?.trim()
        bpDiastolicValue = binding.inputBpDiastolic.text?.toString()?.trim()
        respiratoryValue = binding.inputRespiratoryPerMin.text?.toString()?.trim()
        rbsValue = binding.inputRBS.text?.toString()?.trim()
    }

    fun saveNurseData(benVisitNo: Int, createNewBenflow: Boolean){
        val selectedCategoryRadioButtonId = binding.radioGroup.checkedRadioButtonId
        val selectedReasonRadioButtonId = binding.radioGroup2.checkedRadioButtonId
        val selectedCategoryRadioButton =
            view?.findViewById<RadioButton>(selectedCategoryRadioButtonId)
        val selectedReasonRadioButton = view?.findViewById<RadioButton>(selectedReasonRadioButtonId)
        val subCategory = binding.subCatInput.text.toString()
        val visitDB = VisitDB(
            visitId = generateUuid(),
            category = selectedCategoryRadioButton?.tag.toString(),
            reasonForVisit = selectedReasonRadioButton?.tag.toString(),
            subCategory =subCategory,
            patientID = patientId,
            benVisitNo = benVisitNo,
            benVisitDate =  SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        )

        var chiefComplaints = mutableListOf<ChiefComplaintDB>()
        viewModel.chiefComplaintDB.observe(viewLifecycleOwner) { chiefComplaintList ->
            for (chiefComplaintItem in chiefComplaintList) {
                val chiefC = ChiefComplaintDB(
                    id = generateUuid(),
                    chiefComplaintId = chiefComplaintItem.chiefComplaintId,
                    chiefComplaint = chiefComplaintItem.chiefComplaint.nullIfEmpty(),
                    duration = chiefComplaintItem.duration.nullIfEmpty(),
                    durationUnit = chiefComplaintItem.durationUnit.nullIfEmpty(),
                    description = chiefComplaintItem.description.nullIfEmpty(),
                    patientID = patientId,
                    benVisitNo = benVisitNo,
                    benFlowID = null
                )
                chiefComplaints.add(chiefC)
            }
        }

        val patientVitals = PatientVitalsModel(
            vitalsId = generateUuid(),
            height = heightValue.nullIfEmpty(),
            weight = weightValue.nullIfEmpty(),
            bmi = bmiValue.nullIfEmpty(),
            waistCircumference = waistCircumferenceValue.nullIfEmpty(),
            temperature = temperatureValue.nullIfEmpty(),
            pulseRate = pulseRateValue.nullIfEmpty(),
            spo2 = spo2Value.nullIfEmpty(),
            bpDiastolic = bpDiastolicValue.nullIfEmpty(),
            bpSystolic = bpSystolicValue.nullIfEmpty(),
            respiratoryRate = respiratoryValue.nullIfEmpty(),
            rbs = rbsValue.nullIfEmpty(),
            patientID = patientId,
            benVisitNo = benVisitNo,
        )

        val patientVisitInfoSync = PatientVisitInfoSync(
            patientID = patientId,
            benVisitNo = benVisitNo,
            createNewBenFlow = createNewBenflow,
            nurseDataSynced = SyncState.UNSYNCED,
            doctorDataSynced = SyncState.SYNCED,
            nurseFlag = 9,
            doctorFlag = 1
        )

        viewModel.saveNurseDataToDb(visitDB, chiefComplaints, patientVitals, patientVisitInfoSync)

    }
    fun goToEnd(){
        extractFormValues()
        setVisitMasterDataAndVitalsForFollow()
        if(preferenceDao.isUserOnlyNurseOrCHO()){
            CoroutineScope(Dispatchers.Main).launch {
                var benVisitNo = 0;
                var createNewBenflow = false;
                viewModel.getLastVisitInfoSync(patientId).let {
                    if(it == null){
                        benVisitNo = 1;
                    }
                    else if(it.nurseFlag == 1) {
                        benVisitNo = it.benVisitNo
                    }
                    else {
                        benVisitNo = it.benVisitNo + 1
                        createNewBenflow = true;
                    }
                }
                extractFormValues()
                setVitalsMasterData()
                saveNurseData(benVisitNo, createNewBenflow)

                viewModel.isDataSaved.observe(viewLifecycleOwner){
                    when(it!!){
                        true ->{
                            WorkerUtils.triggerAmritSyncWorker(requireContext())
                            val intent = Intent(context, HomeActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                        }
                        else ->{
//                            requireActivity().runOnUiThread {
//                                Toast.makeText(requireContext(), resources.getString(R.string.something_wend_wong), Toast.LENGTH_SHORT).show()
//                            }
                        }
                    }
                }

            }
        }
        else{
                findNavController().navigate(
                    R.id.action_fhirVisitDetailsFragment_to_caseRecordCustom, bundle
                )
        }
    }
    private fun setVitalsMasterData() {
        var vitalDb = VitalsMasterDb(
            height = heightValue.nullIfEmpty(),
            weight = weightValue.nullIfEmpty(),
            bmi = bmiValue.nullIfEmpty(),
            waistCircumference = waistCircumferenceValue.nullIfEmpty(),
            temperature = temperatureValue.nullIfEmpty(),
            pulseRate = pulseRateValue.nullIfEmpty(),
            spo2 = spo2Value.nullIfEmpty(),
            bpSystolic = bpSystolicValue.nullIfEmpty(),
            bpDiastolic = bpDiastolicValue.nullIfEmpty(),
            respiratoryRate = respiratoryValue.nullIfEmpty(),
            rbs = rbsValue.nullIfEmpty()
        )
        masterDb?.vitalsMasterDb = vitalDb
        bundle.putSerializable("MasterDb", masterDb)
    }

    private fun hideNullFieldsW(vitalsDB: VitalsMasterDb) {
        val itemH = vitalsDB.height
        val itemW = vitalsDB.weight
        val itemB = vitalsDB.bmi
        val itemC = vitalsDB.waistCircumference
        val itemT = vitalsDB.temperature
        val itemP = vitalsDB.pulseRate
        val itemS = vitalsDB.spo2
        val itemBs = vitalsDB.bpSystolic
        val itemBd = vitalsDB.bpDiastolic
        val itemRs = vitalsDB.respiratoryRate
        val itemRb = vitalsDB.rbs
        if (itemH.isNullOrEmpty() || itemH.equals("null")) {
            binding.heightEditTxt.visibility = View.GONE
        } else {
            binding.heightEditTxt.visibility = View.VISIBLE
        }

        if (itemW.isNullOrEmpty() || itemW.equals("null")) {
            binding.weightEditTxt.visibility = View.GONE
        } else {
            binding.weightEditTxt.visibility = View.VISIBLE
        }

        if (itemB.isNullOrEmpty() || itemB.equals("null")) {
            binding.bmill.visibility = View.GONE
        } else {
            binding.bmill.visibility = View.VISIBLE
        }

//        if (itemC.isNullOrEmpty() || itemC.equals("null")) {
//            binding.waistCircumEditTxt.visibility = View.GONE
//        } else {
//            binding.waistCircumEditTxt.visibility = View.VISIBLE
//        }

        if (itemT.isNullOrEmpty() || itemT.equals("null")) {
            binding.temperatureEditTxt.visibility = View.GONE
        } else {
            binding.temperatureEditTxt.visibility = View.VISIBLE
        }

        if (itemP.isNullOrEmpty() || itemP.equals("null")) {
            binding.pulseRateEditTxt.visibility = View.GONE
        } else {
            binding.pulseRateEditTxt.visibility = View.VISIBLE
        }

        if (itemS.isNullOrEmpty() || itemS.equals("null")) {
            binding.spo2EditTxt.visibility = View.GONE
        } else {
            binding.spo2EditTxt.visibility = View.VISIBLE
        }

        if (itemBs.isNullOrEmpty() || itemBs.equals("null")) {
            binding.bpSystolicEditTxt.visibility = View.GONE
        } else {
            binding.bpSystolicEditTxt.visibility = View.VISIBLE
        }

        if (itemBd.isNullOrEmpty() || itemBd.equals("null")) {
            binding.bpDiastolicEditTxt.visibility = View.GONE
        } else {
            binding.bpDiastolicEditTxt.visibility = View.VISIBLE
        }

        if (itemRs.isNullOrEmpty() || itemRs.equals("null")) {
            binding.respiratoryEditTxt.visibility = View.GONE
        } else {
            binding.respiratoryEditTxt.visibility = View.VISIBLE
        }

        if (itemRb.isNullOrEmpty() || itemRb.equals("null")) {
            binding.rbsEditTxt.visibility = View.GONE
        } else {
            binding.rbsEditTxt.visibility = View.VISIBLE
        }

        if ((itemH.isNullOrEmpty() && itemW.isNullOrEmpty() && itemB.isNullOrEmpty() && itemC.isNullOrEmpty() && itemT.isNullOrEmpty() && itemP.isNullOrEmpty() && itemS.isNullOrEmpty() && itemBs.isNullOrEmpty() && itemBd.isNullOrEmpty() && itemRs.isNullOrEmpty() && itemRb.isNullOrEmpty()) ||
            (itemH.equals("null") && itemW.equals("null") && itemB.equals("null") && itemC.equals("null") && itemT.equals(
                "null"
            ) && itemP.equals("null") && itemS.equals("null") && itemBs.equals("null") && itemBd.equals(
                "null"
            ) && itemRs.equals("null") && itemRb.equals("null"))
        ) {
            binding.vitalsHeading.visibility = View.GONE
            binding.vitalsLayout.visibility = View.GONE
        } else {
            binding.vitalsHeading.visibility = View.VISIBLE
            binding.vitalsLayout.visibility = View.VISIBLE
        }
    }

    private fun populateVitalsFieldsW(vitals: VitalsMasterDb) {
        hideNullFieldsW(vitals)
        binding.inputHeight.setText(vitals?.height.toString())
        binding.inputWeight.setText(vitals?.weight.toString())
        binding.inputBmi.setText(vitals.bmi.toString())
//        binding.inputWaistCircum.setText(vitals.waistCircumference.toString())
        binding.inputTemperature.setText(vitals.temperature.toString())
        binding.inputPulseRate.setText(vitals.pulseRate.toString())
        binding.inputSpo2.setText(vitals.spo2.toString())
        binding.inputBpDiastolic.setText(vitals.bpDiastolic.toString())
        binding.inputBpSystolic.setText(vitals.bpSystolic.toString())
        binding.inputRespiratoryPerMin.setText(vitals.respiratoryRate.toString())
//        binding.inputRbs.setText(vitals.rbs.toString())
    }

    fun chiefAndVitalsDataFill() {
        binding.chf.visibility = View.GONE
        binding.chiefComplaintExtra.visibility = View.GONE
        binding.chiefComplaintHeading.visibility = View.VISIBLE
        binding.chiefComplaintExtra2.visibility = View.VISIBLE
        binding.vitalsHeading.visibility = View.VISIBLE
        binding.vitalsLayout.visibility = View.VISIBLE
        binding.usePrevious.visibility = View.VISIBLE
        viewModel.chiefComplaintDB.observe(viewLifecycleOwner) { chiefComplaintList ->
            chiefComplaintDB2.clear()
            for (chiefComplaintItem in chiefComplaintList) {
                val chiefC = ChiefComplaintDB(
                    id = "33+${chiefComplaintItem.chiefComplaintId}",
                    chiefComplaintId = chiefComplaintItem.chiefComplaintId,
                    chiefComplaint = chiefComplaintItem.chiefComplaint,
                    duration = chiefComplaintItem.duration,
                    durationUnit = chiefComplaintItem.durationUnit,
                    description = chiefComplaintItem.description,
                    patientID = patientId,
                    benFlowID = 0
                )
                chiefComplaintDB2.add(chiefC) // Add the item to the list
            }
        }
        chAdapter = ChiefComplaintMultiAdapter(chiefComplaintDB2)
        binding.chiefComplaintExtra2.adapter = chAdapter
        val layoutManagerC = LinearLayoutManager(requireContext())
        binding.chiefComplaintExtra2.layoutManager = layoutManagerC

        if (chiefComplaintDB2.size == 0) {
            binding.chiefComplaintHeading.visibility = View.GONE
        } else {
            binding.chiefComplaintHeading.visibility = View.VISIBLE
        }
        var bool = true
        var vitalsDB = viewModel.vitalsDB
        var vitalDb2 = VitalsMasterDb(
            height = vitalsDB?.height,
            weight = vitalsDB?.weight,
            bmi = vitalsDB?.bmi,
            waistCircumference = vitalsDB?.waistCircumference,
            temperature = vitalsDB?.temperature,
            pulseRate = vitalsDB?.pulseRate,
            spo2 = vitalsDB?.spo2,
            bpSystolic = vitalsDB?.bpSystolic,
            bpDiastolic = vitalsDB?.bpDiastolic,
            respiratoryRate = vitalsDB?.respiratoryRate,
            rbs = vitalsDB?.rbs
        )
        bool = false
        populateVitalsFieldsW(vitalDb2)
        if (bool) {
            binding.vitalsHeading.visibility = View.GONE
            binding.vitalsLayout.visibility = View.GONE
        }
    }

    fun makeFollowUpDefault() {
        if (viewModel.getIsFollowUp()) {
            binding.radioButton3.isChecked = false
            binding.radioButton4.isChecked = true
            chiefAndVitalsDataFill()
//            reason = binding.radioButton4.text.toString()
            reason = binding.radioButton4.tag.toString()
        } else {
            binding.radioButton3.isChecked = true
            binding.radioButton4.isChecked = false
            binding.chf.visibility = View.VISIBLE
            binding.chiefComplaintExtra.visibility = View.VISIBLE
            binding.chiefComplaintHeading.visibility = View.GONE
            binding.chiefComplaintExtra2.visibility = View.GONE
            binding.vitalsHeading.visibility = View.GONE
            binding.vitalsLayout.visibility = View.GONE
            binding.usePrevious.visibility = View.GONE
//            reason = binding.radioButton4.text.toString()
            reason = binding.radioButton3.tag.toString()
        }
    }

    fun isAnyItemEmpty(): Boolean {
        for (item in itemList) {
            if (item.chiefComplaint!!.isEmpty() || item.duration!!.isEmpty()) {
                return true
            }
        }
        return false
    }

//    private val filePickerLauncher =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                val data: Intent? = result.data
//                data?.data?.let { uri ->
////                uploadFileToServer(uri)
//                    val fileSize = getFileSizeFromUri(uri)
//                    if (fileSize > 5242880) {
//                        Toast.makeText(
//                            requireContext(),
//                            resources.getString(R.string.toast_file_size_max),
//                            Toast.LENGTH_SHORT
//                        )
//                            .show()
//                        binding.uploadFileBtn.text = "Upload File"
//                        binding.uploadFileBtn.isEnabled = false
//                        binding.selectFileText.setTextColor(Color.BLACK)
//                        isFileSelected = false
//                        isFileUploaded = false
//                    } else {
//                        convertFileToBase64String(uri, fileSize)
//                        val fileName = getFileNameFromUri(uri)
//                        binding.selectFileText.setText(fileName)
//                        binding.uploadFileBtn.isEnabled = true
//                        binding.uploadFileBtn.text = "Upload File"
//                        isFileSelected = true
//                        isFileUploaded = false
//                        viewModel.setBase64Str(base64String, fileName)
//                    }
//                }
//            }
//        }

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
//        filePickerLauncher.launch(intent)
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
        extractFormValues()
        if (viewModel.getIsFollowUp()) {
//            val chiefData = addChiefComplaintsData()
            setVisitMasterDataForFollow()
            findNavController().navigate(
                R.id.action_fhirVisitDetailsFragment_to_customVitalsFragment, bundle
            )
        } else {
            // initially calling checkAndAddCatSubCat() but now changed to
            // validation on category and Subcategory
            catBool = if (binding.radioGroup.checkedRadioButtonId == -1) {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.toast_cat_select),
                    Toast.LENGTH_SHORT
                ).show()
                false
            } else true


//        if (binding.subCatInput.text.isNullOrEmpty()) {
//            if(catBool) binding.subCatInput.requestFocus()
//            binding.subCatDropDown.apply {
//                boxStrokeColor = Color.RED
//                hintTextColor = ColorStateList.valueOf(Color.RED)
//            }
//            if(catBool) Toast.makeText(requireContext(), resources.getString(R.string.toast_sub_cat_select), Toast.LENGTH_SHORT).show()
//            subCat = false
//        } else {
            subCategory = binding.subCatInput.text.toString()
            subCat = true
            //}

            if (catBool) createEncounterResource()

            // calling to add Chief Complaints
            val chiefData = addChiefComplaintsData()

            setVisitMasterData()

            if (catBool && isFileSelected && isFileUploaded && chiefData) {
                if (encounter != null) viewModel.saveVisitDetailsInfo(encounter!!, listOfConditions)
                findNavController().navigate(
                    R.id.action_fhirVisitDetailsFragment_to_customVitalsFragment, bundle
                )
            } else if (!isFileSelected && catBool && chiefData) {
                if (encounter != null) viewModel.saveVisitDetailsInfo(encounter!!, listOfConditions)
                findNavController().navigate(
                    R.id.action_fhirVisitDetailsFragment_to_customVitalsFragment, bundle
                )
            } else if (isFileSelected && !isFileUploaded && catBool && chiefData) {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.toast_upload_file),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setVisitMasterDataAndVitalsForFollow(){
        val visitMasterDb = VisitMasterDb()
        val selectedCategoryRadioButtonId = binding.radioGroup.checkedRadioButtonId
        val selectedReasonRadioButtonId = binding.radioGroup2.checkedRadioButtonId

        val selectedCategoryRadioButton =
            view?.findViewById<RadioButton>(selectedCategoryRadioButtonId)
        val selectedReasonRadioButton = view?.findViewById<RadioButton>(selectedReasonRadioButtonId)

        visitMasterDb.category = selectedCategoryRadioButton?.tag.toString()
        visitMasterDb.reason = selectedReasonRadioButton?.tag.toString()
        val subCategory = binding.subCatInput.text.toString()
        visitMasterDb.subCategory = subCategory

        val chiefComplaintList2 = mutableListOf<ChiefComplaintValues>()
        viewModel.chiefComplaintDB.observe(viewLifecycleOwner) { chiefComplaintList ->
            for (chiefComplaintData in chiefComplaintList) {
                var cc = ChiefComplaintValues(
                    id = chiefComplaintData.chiefComplaintId,
                    chiefComplaint = chiefComplaintData.chiefComplaint.nullIfEmpty(),
                    duration = chiefComplaintData.duration.nullIfEmpty(),
                    durationUnit = chiefComplaintData.durationUnit.nullIfEmpty(),
                    description = chiefComplaintData.description.nullIfEmpty()
                )
                chiefComplaintList2.add(cc)
            }
        }

        visitMasterDb.chiefComplaint = chiefComplaintList2
        masterDb?.visitMasterDb = visitMasterDb

        var vitalDb = VitalsMasterDb(
            height = heightValue.nullIfEmpty(),
            weight = weightValue.nullIfEmpty(),
            bmi = bmiValue.nullIfEmpty(),
            waistCircumference = waistCircumferenceValue.nullIfEmpty(),
            temperature = temperatureValue.nullIfEmpty(),
            pulseRate = pulseRateValue.nullIfEmpty(),
            spo2 = spo2Value.nullIfEmpty(),
            bpSystolic = bpSystolicValue.nullIfEmpty(),
            bpDiastolic = bpDiastolicValue.nullIfEmpty(),
            respiratoryRate = respiratoryValue.nullIfEmpty(),
            rbs = rbsValue.nullIfEmpty()
        )
        masterDb?.vitalsMasterDb = vitalDb
        bundle.putSerializable("MasterDb", masterDb)
    }

    private fun setVisitMasterDataForFollow() {
        val masterDb = MasterDb(patientId)
        val visitMasterDb = VisitMasterDb()

        val selectedCategoryRadioButtonId = binding.radioGroup.checkedRadioButtonId
        val selectedReasonRadioButtonId = binding.radioGroup2.checkedRadioButtonId

        val selectedCategoryRadioButton =
            view?.findViewById<RadioButton>(selectedCategoryRadioButtonId)
        val selectedReasonRadioButton = view?.findViewById<RadioButton>(selectedReasonRadioButtonId)

        visitMasterDb.category = selectedCategoryRadioButton?.tag.toString()
        visitMasterDb.reason = selectedReasonRadioButton?.tag.toString()
        val subCategory = binding.subCatInput.text.toString()
        visitMasterDb.subCategory = subCategory

        val chiefComplaintList2 = mutableListOf<ChiefComplaintValues>()
        viewModel.chiefComplaintDB.observe(viewLifecycleOwner) { chiefComplaintList ->
            for (chiefComplaintData in chiefComplaintList) {
                var cc = ChiefComplaintValues(
                    id = chiefComplaintData.chiefComplaintId,
                    chiefComplaint = chiefComplaintData.chiefComplaint.nullIfEmpty(),
                    duration = chiefComplaintData.duration.nullIfEmpty(),
                    durationUnit = chiefComplaintData.durationUnit.nullIfEmpty(),
                    description = chiefComplaintData.description.nullIfEmpty()
                )
                chiefComplaintList2.add(cc)
            }
        }

        visitMasterDb.chiefComplaint = chiefComplaintList2
        masterDb.visitMasterDb = visitMasterDb
        bundle.putSerializable("MasterDb", masterDb)
    }

    private fun setVisitMasterData() {
        val masterDb = MasterDb(patientId)
        val visitMasterDb = VisitMasterDb()

        val selectedCategoryRadioButtonId = binding.radioGroup.checkedRadioButtonId
        val selectedReasonRadioButtonId = binding.radioGroup2.checkedRadioButtonId

        val selectedCategoryRadioButton =
            view?.findViewById<RadioButton>(selectedCategoryRadioButtonId)
        val selectedReasonRadioButton = view?.findViewById<RadioButton>(selectedReasonRadioButtonId)

        visitMasterDb.category = selectedCategoryRadioButton?.tag.toString()
        visitMasterDb.reason = selectedReasonRadioButton?.tag.toString()
        val subCategory = binding.subCatInput.text.toString()
        visitMasterDb.subCategory = subCategory

        val chiefComplaintList = mutableListOf<ChiefComplaintValues>()
        for (i in 0 until itemList.size) {
            val chiefComplaintData = itemList[i]

            if (chiefComplaintData.chiefComplaint!!.isNotEmpty() &&
                chiefComplaintData.duration!!.isNotEmpty()
            ) {
                var cc = ChiefComplaintValues(
                    id = chiefComplaintData.id,
                    chiefComplaint = chiefComplaintData.chiefComplaint,
                    duration = chiefComplaintData.duration,
                    durationUnit = chiefComplaintData.durationUnit,
                    description = chiefComplaintData.description.nullIfEmpty()
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
            encounter.addExtension(
                enCounterExtension.getExtenstion(
                    enCounterExtension.getUrl(vanID),
                    enCounterExtension.getStringType(userInfo!!.vanId.toString())
                )
            )

            encounter.addExtension(
                enCounterExtension.getExtenstion(
                    enCounterExtension.getUrl(parkingPlaceID),
                    enCounterExtension.getStringType(userInfo!!.parkingPlaceId.toString())
                )
            )

            encounter.addExtension(
                enCounterExtension.getExtenstion(
                    enCounterExtension.getUrl(providerServiceMapId),
                    enCounterExtension.getStringType(userInfo!!.serviceMapId.toString())
                )
            )

            encounter.addExtension(
                enCounterExtension.getExtenstion(
                    enCounterExtension.getUrl(createdBy),
                    enCounterExtension.getStringType(userInfo!!.userName)
                )
            )
        }
    }

    private fun addChiefComplaintsData(): Boolean {
        // get all the ChiefComplaint data from list and convert that to fhir resource
        for (i in 0 until itemList.size) {
            val chiefComplaintData = itemList[i]
            if (chiefComplaintData.chiefComplaint!!.isEmpty()) {
                if (catBool && subCat) Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.toast_msg_chief),
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            if (chiefComplaintData.duration!!.isEmpty()) {
                if (catBool && subCat) Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.toast_msg_duration),
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }

            if (chiefComplaintData.chiefComplaint!!.isNotEmpty() &&
                chiefComplaintData.duration!!.isNotEmpty()
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
                    conditionExtension.getCoding(
                        chiefComplaintValues.durationUnit!!,
                        chiefComplaintValues.duration!!
                    )
                )
            )

            condition.addExtension(
                conditionExtension.getExtenstion(
                    conditionExtension.getUrl(vanID),
                    conditionExtension.getStringType(userInfo!!.vanId.toString())
                )
            )

            condition.addExtension(
                conditionExtension.getExtenstion(
                    conditionExtension.getUrl(parkingPlaceID),
                    conditionExtension.getStringType(userInfo!!.parkingPlaceId.toString())
                )
            )

            condition.addExtension(
                conditionExtension.getExtenstion(
                    conditionExtension.getUrl(providerServiceMapId),
                    conditionExtension.getStringType(userInfo!!.serviceMapId.toString())
                )
            )

            condition.addExtension(
                conditionExtension.getExtenstion(
                    conditionExtension.getUrl(createdBy),
                    conditionExtension.getStringType(userInfo!!.userName)
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
        val intent = Intent(context, HomeActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    //methods for voice to text conversion and update the input fields
    override fun onEndIconDurationClick(position: Int) {
        speechToTextLauncherForDuration.launch(Unit)
        currDurationPos = position
    }

    private fun updateDurationText(duration: String) {
        if (currDurationPos != -1) {
            itemList[currDurationPos].duration = duration
            adapter.notifyItemChanged(currDurationPos)
        }
    }

    private fun updateDescText(desc: String) {
        if (currDescPos != -1) {
            itemList[currDescPos].description = desc
            adapter.notifyItemChanged(currDescPos)
        }
    }

    override fun onEndIconDescClick(position: Int) {
        speechToTextLauncherForDesc.launch(Unit)
        currDescPos = position
    }

    private fun updateChiefText(chief: String) {
        if (currChiefPos != -1) {
            itemList[currChiefPos].chiefComplaint = chief
            adapter.notifyItemChanged(currChiefPos)
        }
    }

    override fun onEndIconChiefClick(position: Int) {
        speechToTextLauncherForChiefMaster.launch(Unit)
        currChiefPos = position
    }
}