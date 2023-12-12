package org.piramalswasthya.cho.ui.commons.fhir_visit_details

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.AncVisitAdapter
import org.piramalswasthya.cho.adapter.CHOCaseRecordItemAdapter
import org.piramalswasthya.cho.adapter.ChiefComplaintMultiAdapter
import org.piramalswasthya.cho.adapter.ECTrackingAdapter
import org.piramalswasthya.cho.adapter.PncVisitAdapter
import org.piramalswasthya.cho.adapter.SubCategoryAdapter
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.VisitDetailsInfoBinding
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.model.ChiefComplaintMaster
import org.piramalswasthya.cho.model.ChiefComplaintValues
import org.piramalswasthya.cho.model.EligibleCoupleTrackingCache
import org.piramalswasthya.cho.model.MasterDb
import org.piramalswasthya.cho.model.PNCVisitCache
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.SubVisitCategory
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.model.UserDomain
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.model.VisitMasterDb
import org.piramalswasthya.cho.model.VitalsMasterDb
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.ui.commons.DropdownConst
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.mutualVisitUnitsVal
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.SpeechToTextContract
import org.piramalswasthya.cho.ui.commons.immunization_due.child_immunization.list.ChildImmunizationListViewModel
import org.piramalswasthya.cho.ui.commons.immunization_due.child_immunization.list.ChildImmunizationVaccineBottomSheetFragment
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.utils.DateTimeUtil
import org.piramalswasthya.cho.utils.generateUuid
import org.piramalswasthya.cho.utils.nullIfEmpty
import org.piramalswasthya.cho.work.WorkerUtils
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class FragmentVisitDetail : Fragment(), NavigationAdapter,
    EndIconClickListener {

    var fragmentContainerId = 0
    private lateinit var benVisitInfo: PatientDisplayWithVisitInfo
    private lateinit var patientId: String

    val fragment = this

    @Inject
    lateinit var userRepo: UserRepo

    val viewModel: VisitDetailViewModel by viewModels()

    val jsonFile = "patient-visit-details-paginated.json"

    private var usernameEs: String = ""
    private var passwordEs: String = ""
    private var userInfo: UserCache? = null

    private var _binding: VisitDetailsInfoBinding? = null
    private var chiefComplaints = ArrayList<ChiefComplaintMaster>()
    private var chiefComplaintsForFilter = ArrayList<ChiefComplaintMaster>()
    private var units = mutualVisitUnitsVal
    private var subCatOptions = ArrayList<SubVisitCategory>()

//    private lateinit var subCatAdapter: SubCategoryAdapter
    private var isFileSelected: Boolean = false
    private var isFileUploaded: Boolean = false

    @Inject
    lateinit var preferenceDao: PreferenceDao
    private var addCount: Int = 0
    private var deleteCount: Int = 0
    private var category: String = ""
    private var subCategory: String = ""
    private var reason: String = ""
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
    var lmpDate: Date?  = null
    var deliveryDate: Date?  = null
    var lmpDateDisablity = false


    private val lmpDateUtil : DateTimeUtil = DateTimeUtil()
    private val deliveryDateUtil : DateTimeUtil = DateTimeUtil()

    private lateinit var adapter: VisitDetailAdapter

    private var lastAncVisit: Int = 0

    private val initialItem = ChiefComplaintValues()
    private val itemList = mutableListOf(initialItem)
    private lateinit var chAdapter: ChiefComplaintMultiAdapter
    var chiefComplaintDB2 = mutableListOf<ChiefComplaintDB>()

    private val childImmunizationListViewModel: ChildImmunizationListViewModel by viewModels()

    private val bottomSheet: ChildImmunizationVaccineBottomSheetFragment by lazy { ChildImmunizationVaccineBottomSheetFragment() }

    private var pncList = mutableListOf<PNCVisitCache>()

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
        Log.v("tag on", "on create view")
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

    fun ageCheckForChild(dob: Date?): Boolean{
        if(dob == null){
            return false
        }

        val minAge = 0L
        val maxAge = 365L*24*60*60*1000
        val ageGap = System.currentTimeMillis() - dob.time

        return (ageGap > minAge) && (ageGap <= maxAge)
    }

    fun ageCheckForFemale(dob: Date?): Boolean{
        if(dob == null){
            return false
        }

        val minAge = 366L*24*60*60*1000
        val maxAge = 365L*60*24*60*60*1000
        val ageGap = System.currentTimeMillis() - dob.time

        return (ageGap > minAge) && (ageGap <= maxAge)
    }

    private fun setSubCategoryDropdown(){
        viewModel.selectedSubCat = ""
        binding.subCatInput.setText(viewModel.selectedSubCat, false)
        if( ageCheckForChild(benVisitInfo.patient.dob) ){
            val subCatAdapter = SubCategoryAdapter(
                requireContext(),
                R.layout.dropdown_subcategory,
                R.id.tv_dropdown_item_text,
                DropdownConst.age_0_to_1)
            binding.subCatInput.setAdapter(subCatAdapter)
//            viewModel.selectedSubCat = DropdownConst.age_0_to_1[0]
//            binding.subCatInput.setText(viewModel.selectedSubCat, false)
//            setReasonForVisitDropdown(viewModel.selectedSubCat)
        }
        else if( ageCheckForFemale(benVisitInfo.patient.dob) && benVisitInfo.genderName?.lowercase() == "female"){
            val subCatAdapter = SubCategoryAdapter(
                requireContext(),
                R.layout.dropdown_subcategory,
                R.id.tv_dropdown_item_text,
                DropdownConst.female_1_to_59)
            binding.subCatInput.setAdapter(subCatAdapter)
//            viewModel.selectedSubCat = DropdownConst.female_1_to_59[0]
//            binding.subCatInput.setText(viewModel.selectedSubCat, false)
//            setReasonForVisitDropdown(viewModel.selectedSubCat)
        }

    }

    private fun setReasonForVisitDropdown(subCat: String){

        Log.d("Reason for visit is ", "Working " + subCat)
        if(subCat == DropdownConst.careAndPreg){
            val subCatAdapter = SubCategoryAdapter(
                requireContext(),
                R.layout.dropdown_subcategory,
                R.id.tv_dropdown_item_text,
                listOf(DropdownConst.anc, DropdownConst.pnc)
            )
            binding.reasonForVisitInput.setAdapter(subCatAdapter)
//            viewModel.selectedReasonForVisit = DropdownConst.anc
//            binding.reasonForVisitInput.setText(viewModel.selectedReasonForVisit, false)
        }
        else if(subCat == DropdownConst.fpAndOtherRep){
            val subCatAdapter = SubCategoryAdapter(
                requireContext(),
                R.layout.dropdown_subcategory,
                R.id.tv_dropdown_item_text,
                listOf(DropdownConst.fpAndCs)
            )
            binding.reasonForVisitInput.setAdapter(subCatAdapter)
//            viewModel.selectedReasonForVisit = DropdownConst.fpAndCs
//            binding.reasonForVisitInput.setText(viewModel.selectedReasonForVisit, false)
        }
        else if(subCat == DropdownConst.neonatalAndInfant){
            val subCatAdapter = SubCategoryAdapter(
                requireContext(),
                R.layout.dropdown_subcategory,
                R.id.tv_dropdown_item_text,
                listOf(DropdownConst.immunization)
            )
            binding.reasonForVisitInput.setAdapter(subCatAdapter)
//            viewModel.selectedReasonForVisit = DropdownConst.immunization
//            binding.reasonForVisitInput.setText(viewModel.selectedReasonForVisit, false)
        }
        else{
            viewModel.selectedReasonForVisit = ""
            binding.reasonForVisitInput.setText(viewModel.selectedReasonForVisit, false)
        }
    }

    fun removeVisibility(){
        binding.lmpDate.visibility = View.GONE
        binding.deliveryDate.visibility = View.GONE
        binding.rvAnc.visibility = View.GONE
        binding.rvPnc.visibility = View.GONE
        binding.rvEct.visibility = View.GONE
    }

    fun setVisibility(){
        val reasonForVisit = binding.reasonForVisitInput.text.toString()
        if(reasonForVisit == DropdownConst.anc){
//            viewModel.activePwrRecord.observe(viewLifecycleOwner){
            if(viewModel.activePwrRecord == null && lmpDate == null){
                binding.lmpDate.visibility = View.VISIBLE
            }
//            }
            binding.deliveryDate.visibility = View.GONE
            binding.rvAnc.visibility = View.VISIBLE
            binding.rvPnc.visibility = View.GONE
            binding.rvEct.visibility = View.GONE
        }
        else if(reasonForVisit == DropdownConst.pnc){
            binding.lmpDate.visibility = View.GONE
            viewModel.activeDeliveryRecord.observe(viewLifecycleOwner){
                if(it == null && deliveryDate == null){
                    binding.deliveryDate.visibility = View.VISIBLE
                }
            }
            binding.rvAnc.visibility = View.GONE
            binding.rvPnc.visibility = View.VISIBLE
            binding.rvEct.visibility = View.GONE

        }
        else if(reasonForVisit == DropdownConst.fpAndCs){
            binding.lmpDate.visibility = View.GONE
            binding.deliveryDate.visibility = View.GONE
            binding.rvAnc.visibility = View.GONE
            binding.rvPnc.visibility = View.GONE
            binding.rvEct.visibility = View.VISIBLE
        }
        else{
            removeVisibility()
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?){
        Log.v("tag on", "onViewStateRestored")
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onStart(){
        Log.v("tag on", "onStart")
        super.onStart()
    }

    override fun onResume(){
        Log.v("tag on", "onResume")
        super.onResume()
        setSubCategoryDropdown()
        setReasonForVisitDropdown(viewModel.selectedSubCat)
    }

    override fun onPause(){
        Log.v("tag on", "onPause")
        super.onPause()
    }

    override fun onStop(){
        Log.v("tag on", "onStop")
        super.onStop()
    }

    override fun onDestroyView(){
        Log.v("tag on", "onDestroyView")
        super.onDestroyView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        Log.v("tag on", "on view created")
        setVisibility()
//        viewModel.selectedSubCat = "";
//        viewModel.selectedReasonForVisit = ""
//        binding.subCatInput.setText(viewModel.selectedSubCat, false)
//        binding.reasonForVisitInput.setText(viewModel.selectedReasonForVisit, false)

        if(!preferenceDao.isUserCHO()){
            binding.patientList.visibility = View.GONE
        }

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

        val selectedCategoryRadioButtonId = binding.radioGroup.checkedRadioButtonId
        val selectedCategoryRadioButton = view?.findViewById<RadioButton>(selectedCategoryRadioButtonId)
        val selectedCategory = selectedCategoryRadioButton?.tag.toString()

        if(selectedCategory == "General OPD"){
            binding.reasonText.visibility = View.VISIBLE
            binding.radioGroup2.visibility = View.VISIBLE
            binding.subCatDropDown.visibility = View.GONE
            binding.reasonForVisitDropDown.visibility = View.GONE
        }
        else{
            binding.reasonText.visibility = View.GONE
            binding.radioGroup2.visibility = View.GONE
            binding.subCatDropDown.visibility = View.VISIBLE
            binding.reasonForVisitDropDown.visibility = View.VISIBLE
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
        super.onViewCreated(view, savedInstanceState)
//        subCatAdapter = SubCategoryAdapter(
//            requireContext(),
//            R.layout.dropdown_subcategory,
//            R.id.tv_dropdown_item_text,
//            subCatOptions.map { it.name })
//        binding.subCatInput.setAdapter(subCatAdapter)
        // calling to get LoggedIn user Details
        viewModel.getLoggedInUserDetails()
        viewModel.boolCall.observe(viewLifecycleOwner) {
            if (it) {
                userInfo = viewModel.loggedInUser
                viewModel.resetBool()
            }
        }

//        viewModel.subCatVisitList.observe(viewLifecycleOwner) { subCats ->
//            subCatOptions.clear()
//            subCatOptions.addAll(subCats)
//            subCatAdapter.addAll(subCatOptions.map { it.name })
//            subCatAdapter.notifyDataSetChanged()
//        }

        binding.subCatInput.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectedSubCat = parent.getItemAtPosition(position) as String
            binding.subCatInput.setText(viewModel.selectedSubCat, false)
            viewModel.selectedReasonForVisit = ""
            binding.reasonForVisitInput.setText(viewModel.selectedReasonForVisit, false)
            setVisibility()
            setReasonForVisitDropdown(viewModel.selectedSubCat)
            binding.subCatDropDown.apply {
                boxStrokeColor = resources.getColor(R.color.purple)
                hintTextColor = defaultHintTextColor
            }
        }


        binding.reasonForVisitInput.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectedReasonForVisit = parent.getItemAtPosition(position) as String
            binding.reasonForVisitInput.setText(viewModel.selectedReasonForVisit, false)
            setVisibility()
            if(viewModel.selectedReasonForVisit == DropdownConst.anc){
                binding.rvAnc.visibility = View.VISIBLE
//                viewModel.activePwrRecord.observe(viewLifecycleOwner){
                    if(viewModel.activePwrRecord == null && lmpDate == null){
                        binding.lmpDate.visibility = View.VISIBLE
                    }
//                }
            }
            else if(viewModel.selectedReasonForVisit == DropdownConst.pnc){
                binding.rvPnc.visibility = View.VISIBLE
                viewModel.activeDeliveryRecord.observe(viewLifecycleOwner){
                    if(it == null && deliveryDate == null){
                        binding.deliveryDate.visibility = View.VISIBLE
                    }
                }
            }
            else if(viewModel.selectedReasonForVisit == DropdownConst.fpAndCs){
                binding.rvEct.visibility = View.VISIBLE
//                viewModel.activeDeliveryRecord.observe(viewLifecycleOwner){
//                    if(it == null && deliveryDate == null){
//                        binding.deliveryDate.visibility = View.VISIBLE
//                    }
//                }
            }
        }

        binding.lmpDate.setOnClickListener {
            lmpDateUtil.showDatePickerDialog(
                requireContext(), lmpDate,
                maxDays = 0, minDays = -280,
            ).show()
        }

        lmpDateUtil.selectedDate.observe(viewLifecycleOwner) { date ->
            if(date != null){
                lmpDate = date
                binding.lmpDate.setText(DateTimeUtil.formattedDate(date))
            }
        }

        binding.deliveryDate.setOnClickListener {
            deliveryDateUtil.showDatePickerDialog(
                requireContext(), deliveryDate,
                maxDays = 0, minDays = -365,
            ).show()
        }

        deliveryDateUtil.selectedDate.observe(viewLifecycleOwner) { date ->
            if(date != null){
                deliveryDate = date
                binding.deliveryDate.setText(DateTimeUtil.formattedDate(date))
            }
        }


        benVisitInfo = requireActivity().intent?.getSerializableExtra("benVisitInfo") as PatientDisplayWithVisitInfo

        viewModel.setPatientId(benVisitInfo.patient.patientID)
        masterDb?.patientId = benVisitInfo.patient.patientID
        patientId = benVisitInfo.patient.patientID

        setSubCategoryDropdown()
        setReasonForVisitDropdown(viewModel.selectedSubCat)

        viewModel.init(benVisitInfo.patient.patientID)

        binding.patientList.adapter =
            CHOCaseRecordItemAdapter(CHOCaseRecordItemAdapter.BenClickListener{

                if(it.doctorFlag == 2){
                    Toast.makeText(
                        requireContext(),
                        "Pending for Lab Technician",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else if(it.doctorFlag == 9){
                    Toast.makeText(
                        requireContext(),
                        "Flow completed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else{
                    val submitDoctorData = Bundle()
                    submitDoctorData.putBoolean("submitDoctorData", true)
                    submitDoctorData.putSerializable("benVisitInfo", it)

                    findNavController().navigate(
                        R.id.action_fhirVisitDetailsFragment_to_caseRecordCustom, submitDoctorData
                    )

                }

//                val submitDoctorData = Bundle()
//                submitDoctorData.putBoolean("submitDoctorData", true)
//                submitDoctorData.putSerializable("benVisitInfo", it)
//
//                findNavController().navigate(
//                    R.id.action_fhirVisitDetailsFragment_to_caseRecordCustom, submitDoctorData
//                )

            })

        binding.rvAnc.adapter =
            AncVisitAdapter(AncVisitAdapter.AncVisitClickListener { benId, visitNumber ->
                findNavController().navigate(
                    FragmentVisitDetailDirections.actionFhirVisitDetailsFragmentToPwAncFormFragment(
                        benId, visitNumber, true
                    )
                )
            })

        binding.rvPnc.adapter =
            PncVisitAdapter(PncVisitAdapter.PncVisitClickListener { benId, visitNumber ->
                findNavController().navigate(
                    FragmentVisitDetailDirections.actionFhirVisitDetailsFragmentToPncFormFragment(
                        benId, visitNumber
                    )
                )
            })

        binding.rvEct.adapter =
            ECTrackingAdapter(ECTrackingAdapter.ECTrackViewClickListener { benId, createdDate ->
                findNavController().navigate(
                    FragmentVisitDetailDirections.actionFhirVisitDetailsFragmentToEligibleCoupleTrackingFormFragment(
                        patientID = benId, createdDate = createdDate
                    )
                )
            })


        viewModel.allActiveAncRecords.observe(viewLifecycleOwner){
            (binding.rvAnc.adapter as AncVisitAdapter).submitList(it)
        }

        viewModel.allActivePncRecords.observe(viewLifecycleOwner){
            (binding.rvPnc.adapter as PncVisitAdapter).submitList(it)
        }

        viewModel.allEctRecords.observe(viewLifecycleOwner){
            (binding.rvEct.adapter as ECTrackingAdapter).submitList(it)
        }

        viewModel.lastAncVisitNumber.observe(viewLifecycleOwner){
            lastAncVisit = it ?: 0
        }

        lifecycleScope.launch {
            viewModel.getPatientDisplayListForDoctorByPatient(benVisitInfo.patient.patientID).collect{
                (binding.patientList.adapter as CHOCaseRecordItemAdapter).submitList(it)
            }
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
        lifecycleScope.launch {
            viewModel.chiefComplaintMaster.collect { chiefComplaintsList ->
                chiefComplaints.clear()
                chiefComplaints.addAll(chiefComplaintsList)

                chiefComplaintsForFilter.clear()
                chiefComplaintsForFilter.addAll(chiefComplaintsList)
            }
        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->

            when (checkedId) {
                R.id.radioButton1 -> {
                    removeVisibility()
//                    binding.radioGroup2.visibility = View.VISIBLE
//                    binding.reasonText.visibility = View.VISIBLE
                    binding.reasonText.visibility = View.VISIBLE
                    binding.radioGroup2.visibility = View.VISIBLE
                    binding.subCatDropDown.visibility = View.GONE
                    binding.reasonForVisitDropDown.visibility = View.GONE
                    binding.layyy.visibility = View.VISIBLE
//                    binding.usePrevious.visibility = View.VISIBLE
//                    category = binding.radioButton1.text.toString()
                    category = binding.radioButton1.tag.toString()
                }

                else -> {
//                    binding.radioGroup2.visibility = View.GONE
//                    binding.reasonText.visibility = View.GONE
                    setVisibility()
                    binding.reasonText.visibility = View.GONE
                    binding.radioGroup2.visibility = View.GONE
                    binding.subCatDropDown.visibility = View.VISIBLE
                    binding.reasonForVisitDropDown.visibility = View.VISIBLE
                    binding.layyy.visibility = View.GONE
                    binding.usePrevious.visibility = View.GONE
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
        heightValue = binding.inputHeight.text?.toString()?.trim().nullIfEmpty()
        weightValue = binding.inputWeight.text?.toString()?.trim().nullIfEmpty()
        bmiValue = binding.inputBmi.text?.toString()?.trim().nullIfEmpty()
//        waistCircumferenceValue = binding.inputWaistCircum.text?.toString()?.trim()
        temperatureValue = binding.inputTemperature.text?.toString()?.trim().nullIfEmpty()
        pulseRateValue = binding.inputPulseRate.text?.toString()?.trim().nullIfEmpty()
        spo2Value = binding.inputSpo2.text?.toString()?.trim().nullIfEmpty()
        bpSystolicValue = binding.inputBpSystolic.text?.toString()?.trim().nullIfEmpty()
        bpDiastolicValue = binding.inputBpDiastolic.text?.toString()?.trim().nullIfEmpty()
        respiratoryValue = binding.inputRespiratoryPerMin.text?.toString()?.trim().nullIfEmpty()
        rbsValue = binding.inputRBS.text?.toString()?.trim().nullIfEmpty()
    }

    fun saveNurseData(benVisitNo: Int, createNewBenflow: Boolean, user: UserDomain?){
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
            subCategory = subCategory.nullIfEmpty(),
            patientID = patientId,
            benVisitNo = benVisitNo,
            benVisitDate =  SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()),
            createdBy = user?.userName
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
        var vitalsDB = viewModel.vitalsDB
        val patientVitals = PatientVitalsModel(
            vitalsId = generateUuid(),
            height = vitalsDB?.height,
            weight = vitalsDB?.weight,
            bmi = vitalsDB?.bmi,
            waistCircumference = vitalsDB?.waistCircumference,
            temperature = vitalsDB?.temperature,
            pulseRate = vitalsDB?.pulseRate,
            spo2 = vitalsDB?.spo2,
            bpDiastolic = vitalsDB?.bpDiastolic,
            bpSystolic = vitalsDB?.bpSystolic,
            respiratoryRate = vitalsDB?.respiratoryRate,
            rbs = vitalsDB?.rbs,
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
            doctorFlag = 1,
            visitDate = Date(),
        )

        viewModel.saveNurseDataToDb(visitDB, chiefComplaints, patientVitals, patientVisitInfoSync)

    }

    fun goToEnd(){
        extractFormValues()
        setVisitMasterDataAndVitalsForFollow()
        if(!preferenceDao.isUserCHO()){
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

                val user = userRepo.getLoggedInUser()

                saveNurseData(benVisitNo, createNewBenflow, user)

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
        masterDb?.vitalsMasterDb = vitalDb2
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
            (itemH.equals("null") && itemW.equals("null") && itemB.equals("null") && itemC.equals("null") && itemT.equals("null") && itemP.equals("null") && itemS.equals("null") && itemBs.equals("null")
                    && itemBd.equals("null") && itemRs.equals("null") && itemRb.equals("null"))
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
        binding.inputRBS.setText(vitals.rbs.toString())
    }

    fun chiefAndVitalsDataFill() {
        binding.chf.visibility = View.GONE
        binding.chiefComplaintExtra.visibility = View.GONE
        binding.chiefComplaintHeading.visibility = View.VISIBLE
        binding.chiefComplaintExtra2.visibility = View.VISIBLE
        binding.vitalsHeading.visibility = View.VISIBLE
        binding.vitalsLayout.visibility = View.VISIBLE
//        binding.usePrevious.visibility = View.VISIBLE
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
        if (chiefComplaintDB2.size==0){
                binding.usePrevious.visibility = View.GONE
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


    @RequiresApi(Build.VERSION_CODES.O)
    fun navigateNext() {
        val selectedCategoryRadioButtonId = binding.radioGroup.checkedRadioButtonId
        val selectedCategoryRadioButton =
            view?.findViewById<RadioButton>(selectedCategoryRadioButtonId)
        val selectedCategory = selectedCategoryRadioButton?.tag.toString()
        if(selectedCategory == "Other CPHC Services"){
            val reasonForVisit = binding.reasonForVisitInput.text.toString()
            if(reasonForVisit == DropdownConst.anc){

//                viewModel.activePwrRecord.observe(viewLifecycleOwner) { it1->
                    if(viewModel.activePwrRecord == null && lmpDate == null){
                        Toast.makeText(
                            requireContext(),
                            "Select LMP Date",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else if(viewModel.activePwrRecord == null){
                        viewModel.savePregnantWomanRegistration(benVisitInfo.patient.patientID, lmpDate!!)
//                        lmpDateDisablity = true
                        binding.lmpDate.setOnClickListener {}
                        viewModel.isLMPDateSaved.observe(viewLifecycleOwner){it2->
                            if(it2){
                                checkAndNavigateAnc()
                            }
                        }
                    }
                    else{
                        checkAndNavigateAnc()
                    }
//                }
            }
            else if(reasonForVisit == DropdownConst.pnc){
                viewModel.lastPncVisitNumber.observe(viewLifecycleOwner){
                    val visitNumber = (it ?: 0) + 1
                    viewModel.activeDeliveryRecord.observe(viewLifecycleOwner){it1->
                        if(it1 == null && deliveryDate == null){
                            Toast.makeText(
                                requireContext(),
                                "Select Delivery Date",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else if(it1 == null){
                            viewModel.saveDeliveryOutcome(benVisitInfo.patient.patientID, deliveryDate!!)
                            viewModel.isDeliveryDateSaved.observe(viewLifecycleOwner){it2->
                                if(it2){
                                    findNavController().navigate(
                                        FragmentVisitDetailDirections.actionFhirVisitDetailsFragmentToPncFormFragment(
                                            benVisitInfo.patient.patientID, visitNumber
                                        )
                                    )
                                }
                            }
                        }
                        else{
                            findNavController().navigate(
                                FragmentVisitDetailDirections.actionFhirVisitDetailsFragmentToPncFormFragment(
                                    benVisitInfo.patient.patientID, visitNumber
                                )
                            )
                        }
                    }

                }
            }
            else if(reasonForVisit == DropdownConst.immunization){
                childImmunizationListViewModel.updateBottomSheetData(
                    benVisitInfo.patient.patientID
                )
                if (!bottomSheet.isVisible)
                    bottomSheet.show(childFragmentManager, "ImM")
            }
            else if(reasonForVisit == DropdownConst.fpAndCs){
                checkAndNavigateEct()
            }
        }
        else {
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

                // calling to add Chief Complaints
                val chiefData = addChiefComplaintsData()

                setVisitMasterData()

                if (catBool && isFileSelected && isFileUploaded && chiefData) {
                    findNavController().navigate(
                        R.id.action_fhirVisitDetailsFragment_to_customVitalsFragment, bundle
                    )
                } else if (!isFileSelected && catBool && chiefData) {
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
    }

    @SuppressLint("SuspiciousIndentation")
    private fun checkAndNavigateAnc(){
        val minGap : Long = (28.toLong() * 24 * 60 * 60 * 1000)
        val fiveWeeks : Long = (35.toLong() * 24 * 60 * 60 * 1000)
//        viewModel.lastAnc.observe(viewLifecycleOwner){
            if(viewModel.lastAnc != null && System.currentTimeMillis() - viewModel.lastAnc!!.ancDate < minGap){
                Toast.makeText(
                    requireContext(),
                    "ANC found within 28 days",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else if(viewModel.lastAnc == null && System.currentTimeMillis() - viewModel.activePwrRecord!!.lmpDate <= fiveWeeks) {
                Toast.makeText(
                    requireContext(),
                    "LMP date found " + DateTimeUtil.formatCustDate(viewModel.activePwrRecord!!.lmpDate) + ". Gap with first ANC should be at least 5 weeks",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else{
                findNavController().navigate(
                    FragmentVisitDetailDirections.actionFhirVisitDetailsFragmentToPwAncFormFragment(
                        benVisitInfo.patient.patientID, lastAncVisit + 1, false
                    )
                )
            }
//        }
    }

    fun getYearAndMonthFromEpoch(epochMillis: Long): Pair<Int, Int> {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = epochMillis
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is zero-based
        return Pair(year, month)
    }

    fun checkValid(lastEct: EligibleCoupleTrackingCache): Boolean{
        val lastEctPair = getYearAndMonthFromEpoch(lastEct.visitDate)
        val currDatePair = getYearAndMonthFromEpoch(System.currentTimeMillis())
        if(currDatePair.first == lastEctPair.first && currDatePair.second == lastEctPair.second){
            return true;
        }
        return false;
    }

    private fun checkAndNavigateEct(){
        viewModel.lastEct.observe(viewLifecycleOwner){
            if(it != null && checkValid(it)){
                Toast.makeText(
                    requireContext(),
                    "Eligible Couple tracking is done for this month",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else{
                findNavController().navigate(
                    FragmentVisitDetailDirections.actionFhirVisitDetailsFragmentToEligibleCoupleTrackingFormFragment(
                        benVisitInfo.patient.patientID, 0L
                    )
                )
            }
        }
    }

    private fun setVisitMasterDataAndVitalsForFollow(){
        val masterDb2 = MasterDb(patientId)
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
        masterDb2.visitMasterDb = visitMasterDb

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
        Log.d("kkkk","${vitalDb2.height}")
        masterDb2.vitalsMasterDb = vitalDb2
        bundle.putSerializable("MasterDb", masterDb2)
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
        visitMasterDb.reason = when(visitMasterDb.category){
            "General OPD" -> selectedReasonRadioButton?.tag.toString()
            else -> binding.reasonForVisitInput.text.toString()
        }
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

            }
        }
        return true
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

fun AutoCompleteTextView.showDropdown(adapter: ArrayAdapter<String>?) {
    if(!TextUtils.isEmpty(this.text.toString())){
        adapter?.filter?.filter(null)
    }
}