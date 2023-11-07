package org.piramalswasthya.cho.ui.commons.case_record

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.ChiefComplaintMultiAdapter
import org.piramalswasthya.cho.adapter.DiagnosisAdapter
import org.piramalswasthya.cho.adapter.PrescriptionAdapter
import org.piramalswasthya.cho.adapter.RecyclerViewItemChangeListenerD
import org.piramalswasthya.cho.adapter.RecyclerViewItemChangeListenersP
import org.piramalswasthya.cho.adapter.TempDropdownAdapter
//import org.piramalswasthya.cho.adapter.ReportAdapter
//import org.piramalswasthya.cho.adapter.ReportAdapter
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.CaseRecordCustomLayoutBinding
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.model.CounsellingProvided
import org.piramalswasthya.cho.model.DiagnosisCaseRecord
import org.piramalswasthya.cho.model.DiagnosisValue
import org.piramalswasthya.cho.model.InvestigationCaseRecord
import org.piramalswasthya.cho.model.ItemMasterList
import org.piramalswasthya.cho.model.MasterDb
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.PrescriptionCaseRecord
import org.piramalswasthya.cho.model.PrescriptionTemplateDB
import org.piramalswasthya.cho.model.PrescriptionValues
import org.piramalswasthya.cho.model.PrescriptionValuesForTemplate
import org.piramalswasthya.cho.model.ProceduresMasterData
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.model.VitalsMasterDb
import org.piramalswasthya.cho.repositories.PrescriptionTemplateRepo
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.frequencyMap
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.instructionDropdownList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.medicalReferDropdownVal
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.medicationFrequencyList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.tabletDosageList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.unitVal
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.utils.generateIntFromUuid
import org.piramalswasthya.cho.utils.generateUuid
import org.piramalswasthya.cho.utils.nullIfEmpty
import org.piramalswasthya.cho.work.WorkerUtils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class CaseRecordCustom: Fragment(R.layout.case_record_custom_layout), NavigationAdapter {
    private var _binding: CaseRecordCustomLayoutBinding? = null
    private val binding: CaseRecordCustomLayoutBinding
        get() = _binding!!

    private val viewModel: CaseRecordViewModel by viewModels()
    @Inject
    lateinit var preferenceDao: PreferenceDao
    @Inject
    lateinit var prescriptionTemplateRepo: PrescriptionTemplateRepo

    private val initialItemD = DiagnosisValue()
    private val itemListD = mutableListOf(initialItemD)
    private val initialItemP = PrescriptionValues()
    private val itemListP = mutableListOf(initialItemP)
    private val initialItemTemp = PrescriptionValuesForTemplate()
    private val tempList  = mutableListOf(initialItemTemp)
    private lateinit var dAdapter : DiagnosisAdapter
    private lateinit var chAdapter : ChiefComplaintMultiAdapter
    private lateinit var pAdapter : PrescriptionAdapter
//    private lateinit var rAdapter : ReportAdapter
    private var testNameMap = emptyMap<Int,String>()
    private var referNameMap = emptyMap<Int,String>()
    private val selectedTestName = mutableListOf<Int>()
    var familyM: MaterialCardView? = null
    var selectF: TextView? = null
    private val instructionDropdown= instructionDropdownList
    private val tempDBVal = ArrayList<PrescriptionTemplateDB?>()
    private val formMListVal = ArrayList<ItemMasterList>()
    private var formForFilter = ArrayList<ItemMasterList>()
    private val counsellingTypes = ArrayList<CounsellingProvided>()
    private val procedureDropdown = ArrayList<ProceduresMasterData>()
    private val frequencyListVal = medicationFrequencyList
    private lateinit var tempDropdownAdapter :TempDropdownAdapter
    private val referDropdownVal = medicalReferDropdownVal
    private val unitListVal = unitVal
    private val dosage = tabletDosageList
    private var masterDb: MasterDb? = null
    private lateinit var patientId : String
    private lateinit var benVisitInfo : PatientDisplayWithVisitInfo
    private var patId = ""
    private lateinit var referDropdown: AutoCompleteTextView
    private var doctorFlag = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = CaseRecordCustomLayoutBinding.inflate(inflater, container, false)
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
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)
        super.onViewCreated(view, savedInstanceState)
        familyM = binding.testName
        selectF = binding.selectF
        referDropdown = binding.referDropdownText

        benVisitInfo = requireActivity().intent?.getSerializableExtra("benVisitInfo") as PatientDisplayWithVisitInfo
        val tableLayout = binding.tableLayout
        if(preferenceDao.isUserOnlyDoctorOrMo() || (preferenceDao.isCHO() && preferenceDao.getCHOSecondRole() == "Doctor") ||
            (preferenceDao.isUserSwitchRole() && preferenceDao.getSwitchRole() == "Doctor")) {
            patientId = benVisitInfo.patient.patientID
            patId= benVisitInfo.patient.patientID
            viewModel.getVitalsDB(patId)
            viewModel.getChiefComplaintDB(benVisitInfo.patient.patientID,benVisitInfo.benVisitNo!!)
            if(benVisitInfo.benVisitNo != null){
               viewModel.getLabList(benVisitInfo.patient.patientID, benVisitInfo.benVisitNo!!)
            }
            viewModel.labReportList.observe(viewLifecycleOwner) { labReports ->
                var  nameVal = ""
                var resultVal = ""
                for (labReport in labReports) {
                    val procedureType = labReport.procedure.procedureName
                    procedureType?.let { viewModel.labReportProcedureTypes.add(it) }
                }

                if (labReports.size>0){
                   binding.scrollview.visibility = View.VISIBLE
                   binding.resultHeading.visibility = View.VISIBLE
                   binding.dateOption.visibility = View.VISIBLE
               }
                for (labReport in labReports) {
                    val procedureName = labReport.procedure.procedureName
                    binding.inputDate.setText(labReport.procedure.createdDate)
                    binding.inputDate.inputType = InputType.TYPE_NULL
                    binding.inputDate.isFocusable = false
                    binding.inputDate.isClickable = false
                    for (component in labReport.components) {

                        val tableRowVal = layoutInflater.inflate(R.layout.report_custom_layout, null) as TableRow
                        val componentName = component.componentName
                        val resultValue = component.testResultValue
                        val resultUnit = component.testResultUnit
                        nameVal = "$procedureName- $componentName"
                        if(resultUnit!=null ){
                        resultVal = "${resultValue} ${resultUnit}"}
                        else{
                            resultVal = "${resultValue}"
                        }
                        tableRowVal.findViewById<TextView>(R.id.nameTextView).setText(nameVal)
                        tableRowVal.findViewById<TextView>(R.id.numberTextView).setText(resultVal)
                        tableLayout.addView(tableRowVal)
                    }
                }
            }
        }
        binding.inputTestName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed, but must be implemented
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed, but must be implemented
            }

            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrBlank()) {
                    val text = s.toString()
                    val capitalizedText = text.substring(0, 1).toUpperCase() + text.substring(1)
                    if (text != capitalizedText) {
                        binding.inputTestName.removeTextChangedListener(this) // Avoid infinite loop
                        binding.inputTestName.setText(capitalizedText)
                        binding.inputTestName.setSelection(capitalizedText.length) // Set cursor position to the end
                        binding.inputTestName.addTextChangedListener(this) // Add the listener back
                    }
                }
            }
        })
        lifecycleScope.launch {
            testNameMap = viewModel.getTestNameTypeMap()
        }

        lifecycleScope.launch {
            referNameMap = viewModel.getReferNameTypeMap()
        }
        var chiefComplaintDB = mutableListOf<ChiefComplaintDB>()

        if (preferenceDao.isUserOnlyDoctorOrMo() || (preferenceDao.isCHO() && preferenceDao.getCHOSecondRole() == "Doctor") ||
            (preferenceDao.isUserSwitchRole() && preferenceDao.getSwitchRole() == "Doctor")) {
            viewModel.chiefComplaintDB.observe(viewLifecycleOwner) { chiefComplaintList ->
                // Clear the existing data in chiefComplaintDB
                chiefComplaintDB.clear()

                // Loop through the chiefComplaintList and add data to chiefComplaintDB
                for (chiefComplaintItem in chiefComplaintList) {
                    val chiefC = ChiefComplaintDB(
                        id = "33+${chiefComplaintItem.chiefComplaintId}",
                        chiefComplaintId = chiefComplaintItem.chiefComplaintId,
                        chiefComplaint = chiefComplaintItem.chiefComplaint,
                        duration = chiefComplaintItem.duration,
                        durationUnit = chiefComplaintItem.durationUnit,
                        description = chiefComplaintItem.description,
                        patientID = "",
                        benFlowID = 0
                    )
                    chiefComplaintDB.add(chiefC) // Add the item to the list
                }
                chAdapter.notifyDataSetChanged()
            }
            if(chiefComplaintDB.size==0){
                binding.chiefComplaintHeading.visibility = View.GONE
            }
            else{
                binding.chiefComplaintHeading.visibility = View.VISIBLE
            }
        }else {
           masterDb = arguments?.getSerializable("MasterDb") as? MasterDb

           for (i in 0 until (masterDb?.visitMasterDb?.chiefComplaint?.size ?: 0)) {
               val chiefComplaintItem = masterDb!!.visitMasterDb!!.chiefComplaint!![i]
               val chiefC = ChiefComplaintDB(
                   id = "33+${i}",
                   chiefComplaintId = chiefComplaintItem.id,
                   chiefComplaint = chiefComplaintItem.chiefComplaint,
                   duration = chiefComplaintItem.duration,
                   durationUnit = chiefComplaintItem.durationUnit,
                   description = chiefComplaintItem.description,
                   patientID = "",
                   benFlowID = 0
               )
               chiefComplaintDB.add(chiefC) // Add the item to the list
           }
       }
      chAdapter = ChiefComplaintMultiAdapter(chiefComplaintDB)
        binding.chiefComplaintExtra.adapter = chAdapter
        val layoutManagerC = LinearLayoutManager(requireContext())
        binding.chiefComplaintExtra.layoutManager = layoutManagerC


        viewModel.formMedicineDosage.observe(viewLifecycleOwner) { f ->
            formMListVal.clear()
            formMListVal.addAll(f)

            formForFilter.clear()
            formForFilter.addAll(f)
            pAdapter.notifyDataSetChanged()
        }

//        tempDropdownAdapter = TempDropdownAdapter(
//            requireContext(),
//            R.layout.drop_down,
//            tempDBVal,
//            binding.inputUseTempForFields
//        )
//        binding.inputUseTempForFields.setAdapter(tempDropdownAdapter)


//        viewModel.tempDB.observe(viewLifecycleOwner) { f ->
//            tempDBVal.clear()
//            tempDBVal.addAll(f)
//            convertToPrescriptionValues(tempDBVal)
//            pAdapter.notifyDataSetChanged()
//        }

//        binding.inputUseTempForFields.setOnItemClickListener { parent, _, position, _ ->
//            val selectedString = parent.getItemAtPosition(position) as PrescriptionTemplateDB
//            val form = tempDBVal.filter { it?.templateName == selectedString.templateName }
//            binding.inputUseTempForFields.setText(form[0]?.templateName,false)
//            convertToPrescriptionValues(form)
//        }

        val tempAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line)
        val uniqueTemplateNames = HashSet<String?>()
        binding.inputUseTempForFields.setAdapter(tempAdapter)

        viewModel.tempDB.observe(viewLifecycleOwner) { vc ->
            uniqueTemplateNames.clear()
            uniqueTemplateNames.add("None")
            vc.mapTo(uniqueTemplateNames) { it?.templateName }
            tempAdapter.clear()
            tempAdapter.addAll(uniqueTemplateNames)
            tempAdapter.notifyDataSetChanged()
        }

        binding.inputUseTempForFields.setOnItemClickListener { parent, _, position, _ ->
            itemListP.clear()
            pAdapter.notifyDataSetChanged()
            val selectedString = parent.getItemAtPosition(position) as String
            if (selectedString == "None") {
                itemListP.clear()
                itemListP.add(PrescriptionValues())
                pAdapter.notifyDataSetChanged()
                val inputMethodManager = requireContext().getSystemService(InputMethodManager::class.java)
                inputMethodManager.hideSoftInputFromWindow(binding.inputUseTempForFields.windowToken, 0)
            } else {
                itemListP.clear()
                pAdapter.notifyDataSetChanged()
                lifecycleScope.launch {
                    val selectedTemplates = viewModel.getTemplatesByTemplateName(selectedString)
                    convertToPrescriptionValues(selectedTemplates)
                }
            }
        }

        viewModel.counsellingProvided.observe(viewLifecycleOwner) { f ->
            counsellingTypes.clear()
            counsellingTypes.addAll(f)
            pAdapter.notifyDataSetChanged()
        }
        viewModel.procedureDropdown.observe(viewLifecycleOwner) { f ->
            procedureDropdown.clear()
            procedureDropdown.addAll(f)
            familyM!!.setOnClickListener {
                showDialogWithFamilyMembers(procedureDropdown, viewModel.labReportProcedureTypes)
            }
        }
        binding.saveTemplate.setOnClickListener {
            saveTemp(uniqueTemplateNames)
        }
        binding.deleteTemp.setOnClickListener {

            tempAdapter.notifyDataSetChanged()
            openBottomSheet(uniqueTemplateNames)
        }

        val referAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line)
        binding.referDropdownText.setAdapter(referAdapter)
        viewModel.higherHealthCare.observe(viewLifecycleOwner){vc->
            referAdapter.clear()
            referAdapter.addAll(vc.map{it.institutionName})
            referAdapter.notifyDataSetChanged()
        }

        val counsellingTypesAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line)
        binding.routeDropDownVal.setAdapter(counsellingTypesAdapter)

        viewModel.counsellingProvided.observe(viewLifecycleOwner){ c->
            counsellingTypesAdapter.clear()
            counsellingTypesAdapter.addAll(c.map{it.name})
            counsellingTypesAdapter.notifyDataSetChanged()
        }

        dAdapter = DiagnosisAdapter(
            itemListD,
            object : RecyclerViewItemChangeListenerD {
                override fun onItemChanged() {
                    binding.plusButtonD.isEnabled = !isAnyItemEmptyD()
                }
            }
        )
        binding.diagnosisExtra.adapter = dAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        binding.diagnosisExtra.layoutManager = layoutManager
        dAdapter.notifyItemInserted(itemListD.size - 1)
        binding.plusButtonD.isEnabled = !isAnyItemEmptyD()
        binding.plusButtonD.setOnClickListener {
            val newItem = DiagnosisValue()
            itemListD.add(newItem)
//            dAdapter.notifyItemInserted(itemListD.size - 1)
             view.clearFocus()
            dAdapter.notifyDataSetChanged()
            binding.plusButtonD.isEnabled = !isAnyItemEmptyD()
            binding.plusButtonD.isEnabled = false
        }
        pAdapter = PrescriptionAdapter(
//            tempDBVal,
//            tempList,
            itemListP,
            formMListVal,
            frequencyListVal,
            unitListVal,
            instructionDropdown,
            formForFilter,
            object : RecyclerViewItemChangeListenersP {
                override fun onItemChanged() {
                    binding.plusButtonP.isEnabled = !isAnyItemEmptyP()
                }
            }
        )
        binding.prescriptionExtra.adapter = pAdapter
        val layoutManager2 = LinearLayoutManager(requireContext())
        binding.prescriptionExtra.layoutManager = layoutManager2
        pAdapter.notifyItemInserted(itemListP.size - 1)
        binding.plusButtonP.isEnabled = !isAnyItemEmptyP()
        binding.plusButtonP.setOnClickListener {
            val newItem = PrescriptionValues()
            itemListP.add(newItem)
//            pAdapter.notifyItemInserted(itemListP.size -   1)
            view.clearFocus()
            pAdapter.notifyItemInserted(itemListP.size -   1)
            binding.plusButtonP.isEnabled = !isAnyItemEmptyP()
            binding.plusButtonP.isEnabled = false
        }
        if(preferenceDao.isUserOnlyDoctorOrMo() || (preferenceDao.isCHO() && preferenceDao.getCHOSecondRole() == "Doctor") ||
            (preferenceDao.isUserSwitchRole() && preferenceDao.getSwitchRole() == "Doctor")){
            var bool = true
            viewModel.vitalsDB.observe(viewLifecycleOwner) { vitalsDB ->
                    var vitalDb2 = VitalsMasterDb(
                        height = vitalsDB.height,
                        weight = vitalsDB.weight,
                        bmi = vitalsDB.bmi,
                        waistCircumference = vitalsDB.waistCircumference,
                        temperature = vitalsDB.temperature,
                        pulseRate = vitalsDB.pulseRate,
                        spo2 = vitalsDB.spo2,
                        bpSystolic = vitalsDB.bpSystolic,
                        bpDiastolic =vitalsDB.bpDiastolic,
                        respiratoryRate = vitalsDB.respiratoryRate,
                        rbs = vitalsDB.rbs
                    )
                    bool = false
                    populateVitalsFieldsW(vitalDb2)
            }
            if(bool){
                populateVitalsFields()
            }
        }
        else{
            populateVitalsFields()
        }
    }
        fun convertToPrescriptionValues(prescriptionTemplateDB: List<PrescriptionTemplateDB?>) {
            for (templateDB in prescriptionTemplateDB) {
                val prescriptionValue = templateDB?.let {
                    it?.drugName?.let { it1 ->
                        PrescriptionValues(
                            id =templateDB.drugId,
                            form = it1,
                            frequency = templateDB.frequency ?: "",
                            duration = templateDB.duration ?: "",
                            instruction = templateDB.instruction ?: "",
                            unit = templateDB.unit ?: ""
                        )
                    }
                }
                if (prescriptionValue != null) {
                    itemListP.add(prescriptionValue)
                }
            }
            val inputMethodManager = requireContext().getSystemService(InputMethodManager::class.java)
            inputMethodManager.hideSoftInputFromWindow(binding.inputUseTempForFields.windowToken, 0)
    }
    private lateinit var syncBottomSheet : TemplateListBottomSheetFragment
    private fun openBottomSheet(str: HashSet<String?>) {
        syncBottomSheet = TemplateListBottomSheetFragment(str, prescriptionTemplateRepo)
        if(!syncBottomSheet.isVisible)
            syncBottomSheet.show(childFragmentManager, resources.getString(R.string.sync))
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
    private fun populateVitalsFields() {
        hideNullFields()
        // Check if the masterDb and vitalsMasterDb are not null
        if (masterDb != null && masterDb?.vitalsMasterDb != null) {
            val vitals = masterDb?.vitalsMasterDb
            binding.inputHeight.setText(vitals?.height.toString())
            binding.inputWeight.setText(vitals?.weight.toString())
            binding.inputBmi.setText(vitals?.bmi.toString())
//            binding.inputWaistCircum.setText(vitals?.waistCircumference.toString())
            binding.inputTemperature.setText(vitals?.temperature.toString())
            binding.inputPulseRate.setText(vitals?.pulseRate.toString())
            binding.inputSpo2.setText(vitals?.spo2.toString())
            binding.inputBpDiastolic.setText(vitals?.bpDiastolic.toString())
            binding.inputBpSystolic.setText(vitals?.bpSystolic.toString())
            binding.inputRespiratoryPerMin.setText(vitals?.respiratoryRate.toString())
            binding.inputRBS.setText(vitals?.rbs.toString())
        }
    }
    private fun hideNullFieldsW(vitalsDB: VitalsMasterDb){
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
            (itemH.equals("null") && itemW.equals("null") && itemB.equals("null") && itemC.equals("null") && itemT.equals("null") && itemP.equals("null") && itemS.equals("null") && itemBs.equals("null") && itemBd.equals("null") && itemRs.equals("null") && itemRb.equals("null"))) {
            binding.vitalsExtra.visibility = View.GONE
            binding.vitalsLayout.visibility = View.GONE
        } else {
            binding.vitalsExtra.visibility = View.VISIBLE
            binding.vitalsLayout.visibility = View.VISIBLE
        }
    }

    private fun hideNullFields(){
        var  itemH = masterDb?.vitalsMasterDb?.height.toString()
        var  itemW = masterDb?.vitalsMasterDb?.weight.toString()
        var  itemB = masterDb?.vitalsMasterDb?.bmi.toString()
//        var  itemC = masterDb?.vitalsMasterDb?.waistCircumference.toString()
        var  itemT = masterDb?.vitalsMasterDb?.temperature.toString()
        var  itemP = masterDb?.vitalsMasterDb?.pulseRate.toString()
        var  itemS = masterDb?.vitalsMasterDb?.spo2.toString()
        var  itemBs = masterDb?.vitalsMasterDb?.bpSystolic.toString()
        var  itemBd = masterDb?.vitalsMasterDb?.bpDiastolic.toString()
        var  itemRs = masterDb?.vitalsMasterDb?.respiratoryRate.toString()
        var  itemRb = masterDb?.vitalsMasterDb?.rbs.toString()
        if(itemH.isNullOrEmpty()||itemH.equals("null")){
            binding.heightEditTxt.visibility = View.GONE
        }
        if(itemW.isNullOrEmpty()||itemW.equals("null")){
            binding.weightEditTxt.visibility = View.GONE
        }
        if(itemB.isNullOrEmpty()||itemB.equals("null")){
            binding.bmill.visibility = View.GONE
        }
//        if(itemC.isNullOrEmpty()||itemC.equals("null")){
//            binding.waistCircumEditTxt.visibility = View.GONE
//        }
        if(itemT.isNullOrEmpty()||itemT.equals("null")){
            binding.temperatureEditTxt.visibility = View.GONE
        }
        if(itemP.isNullOrEmpty()||itemP.equals("null")){
            binding.pulseRateEditTxt.visibility = View.GONE
        }
        if(itemS.isNullOrEmpty()||itemS.equals("null")){
            binding.spo2EditTxt.visibility = View.GONE
        }
        if(itemBs.isNullOrEmpty()||itemBs.equals("null")){
            binding.bpSystolicEditTxt.visibility = View.GONE
        }
        if(itemBd.isNullOrEmpty()||itemBd.equals("null")){
            binding.bpDiastolicEditTxt.visibility = View.GONE
        }
        if(itemRs.isNullOrEmpty()||itemRs.equals("null")){
            binding.respiratoryEditTxt.visibility = View.GONE
        }
        if(itemRb.isNullOrEmpty()||itemRb.equals("null")){
            binding.rbsEditTxt.visibility = View.GONE
        }
        if((itemH.isNullOrEmpty() && itemW.isNullOrEmpty() && itemB.isNullOrEmpty() && itemT.isNullOrEmpty() && itemP.isNullOrEmpty() && itemS.isNullOrEmpty() && itemBs.isNullOrEmpty() && itemBd.isNullOrEmpty() && itemRs.isNullOrEmpty() &&itemRb.isNullOrEmpty() )
                    ||
            (itemH.equals("null") && itemW.equals("null") && itemB.equals("null") && itemT.equals("null") && itemP.equals("null") && itemS.equals("null") && itemBs.equals("null") && itemBd.equals("null") && itemRs.equals("null") && itemRb.equals("null"))){
            binding.vitalsExtra.visibility= View.INVISIBLE
        }
    }


    private fun showDialogWithFamilyMembers(
        proceduresMasterData: List<ProceduresMasterData>,
        labReportProcedureTypes: List<String>
    ) {
        val selectedItems = BooleanArray(procedureDropdown.size) { selectedTestName.contains(it) }

        val disabledItems = labReportProcedureTypes.map { type ->
            proceduresMasterData.indexOfFirst { it.procedureName == type }
        }.toSet().toTypedArray()

        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Select Test Name")
            .setCancelable(false)
            .setMultiChoiceItems(
                procedureDropdown.map { it.procedureName }.toTypedArray(),
                selectedItems
            ) { _, which, isChecked ->
                if (isChecked) {
                    if (!disabledItems.contains(which)) {
                        selectedTestName.add(which)
                    } else {
                        Toast.makeText(requireContext(), "Test with result cannot be selected", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    selectedTestName.remove(which)
                }
            }
            .setPositiveButton("Ok") { dialog, which ->
                val selectedRelationTypes = selectedTestName.map { proceduresMasterData[it].procedureName }
                val selectedRelationTypesString = selectedRelationTypes.joinToString(", ")
                binding.selectF.text = selectedRelationTypesString
                binding.selectF.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black))
            }
            .setNeutralButton("Clear all") { dialog, which ->
                selectedTestName.clear()
                Arrays.fill(selectedItems, false)
                val listView = (dialog as? AlertDialog)?.listView
                listView?.clearChoices()
                listView?.requestLayout()
                binding.selectF.text = resources.getString(R.string.select_test_name)
                binding.selectF.setTextColor(ContextCompat.getColor(binding.root.context, R.color.defaultInput))
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }


    fun isAnyItemEmptyD(): Boolean {
        for (item in itemListD) {
            if (item.diagnosis.isEmpty()) {
                return true
            }
        }
        return false
    }
    fun isAnyItemEmptyP(): Boolean {
        for (item in itemListP) {
            if (item.form.isEmpty()||item.frequency.isEmpty()||item.duration.isEmpty()) {
                return true
            }
        }
        return false
    }
    private fun <K, V> findKeyByValue(map: Map<K, V>, value: V): K? {
        return map.entries.find { it.value == value }?.key
    }

    private fun saveNurseAndDoctorData(benVisitNo: Int, createNewBenflow: Boolean){

        val visitDB = VisitDB(
            visitId = generateUuid(),
            category = masterDb?.visitMasterDb?.category.nullIfEmpty(),
            reasonForVisit = masterDb?.visitMasterDb?.reason.nullIfEmpty() ,
            subCategory = masterDb?.visitMasterDb?.subCategory.nullIfEmpty(),
            patientID = patId,
            benVisitNo = benVisitNo,
            benVisitDate =  SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        )


        var chiefComplaints = mutableListOf<ChiefComplaintDB>()
        for (i in 0 until (masterDb?.visitMasterDb?.chiefComplaint?.size ?: 0)) {
            val chiefComplaintItem = masterDb!!.visitMasterDb!!.chiefComplaint!![i]
            val chiefC = ChiefComplaintDB(
                id = generateUuid(),
                chiefComplaintId=chiefComplaintItem.id,
                chiefComplaint = chiefComplaintItem.chiefComplaint.nullIfEmpty(),
                duration =  chiefComplaintItem.duration.nullIfEmpty(),
                durationUnit = chiefComplaintItem.durationUnit.nullIfEmpty(),
                description = chiefComplaintItem.description.nullIfEmpty(),
                patientID = patId,
                benFlowID = null,
                benVisitNo = benVisitNo
            )
            chiefComplaints.add(chiefC)
        }


        val patientVitals = PatientVitalsModel(
            vitalsId = generateUuid(),
            height = masterDb?.vitalsMasterDb?.height.nullIfEmpty(),
            weight = masterDb?.vitalsMasterDb?.weight.nullIfEmpty(),
            bmi = masterDb?.vitalsMasterDb?.bmi.nullIfEmpty(),
            waistCircumference = masterDb?.vitalsMasterDb?.waistCircumference.nullIfEmpty(),
            temperature = masterDb?.vitalsMasterDb?.temperature.nullIfEmpty(),
            pulseRate = masterDb?.vitalsMasterDb?.pulseRate.nullIfEmpty(),
            spo2 = masterDb?.vitalsMasterDb?.spo2.nullIfEmpty(),
            bpDiastolic = masterDb?.vitalsMasterDb?.bpDiastolic.nullIfEmpty(),
            bpSystolic = masterDb?.vitalsMasterDb?.bpSystolic.nullIfEmpty(),
            respiratoryRate = masterDb?.vitalsMasterDb?.respiratoryRate.nullIfEmpty(),
            rbs = masterDb?.vitalsMasterDb?.rbs.nullIfEmpty(),
            patientID = patId,
            benVisitNo = benVisitNo
        )


        var diagnosisList = mutableListOf<DiagnosisCaseRecord>()
        for (i in 0 until itemListD.size) {
            val diagnosisData = itemListD[i]
            if(diagnosisData.diagnosis.isNullOrEmpty()){
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), resources.getString(R.string.diagnosisCannotBeEmpty), Toast.LENGTH_SHORT).show()
                }
                return;
            }
            else {
                var diagnosis = DiagnosisCaseRecord(
                    diagnosisCaseRecordId = generateUuid(),
                    diagnosis = diagnosisData.diagnosis,
                    patientID = patId,
                    benVisitNo = benVisitNo
                )
                diagnosisList.add(diagnosis)
            }
        }


        val testName = binding.selectF.text.toString()
        val testNamesList = testName.split(",").map { it.trim() }
        val idString = testNamesList.joinToString(",") { testNameS ->
            val id = findKeyByValue(testNameMap,testNameS) // Replace with your function to get the ID
            id?.toString() ?: ""
        }

        val externalInvestigation = binding.inputExternalI.text.toString().nullIfEmpty()
        val counsellingTypesVal = binding.routeDropDownVal.text.toString().nullIfEmpty()
        val referVal = binding.referDropdownText.text.toString().nullIfEmpty()
        val referId = findKeyByValue(referNameMap,referVal)
        val investigation = InvestigationCaseRecord(
            investigationCaseRecordId = generateUuid(),
            testIds = idString.nullIfEmpty(),
            externalInvestigation = externalInvestigation,
            counsellingTypes = counsellingTypesVal,
            patientID = patId,
            institutionId = referId,
            benVisitNo = benVisitNo
        )

        val prescriptionList = mutableListOf<PrescriptionCaseRecord>();
        for (i in 0 until itemListP.size) {
            val prescriptionData = itemListP[i]
            var formVal = prescriptionData.id
            var freqVal = prescriptionData.frequency.nullIfEmpty()
            var unitVal = prescriptionData.unit.nullIfEmpty()
            var durVal = prescriptionData.duration.nullIfEmpty()
            var instruction = prescriptionData.instruction.nullIfEmpty()


            if (formVal != null) {
                val frequencyDescription = frequencyMap[freqVal]
                var mappedFrequency: String? = null
                if (frequencyDescription != null) {
                    mappedFrequency = frequencyDescription
                }
                var pres = PrescriptionCaseRecord(
                    prescriptionCaseRecordId = generateUuid(),
                    itemId = formVal,
                    frequency = mappedFrequency,
                    duration = durVal,
                    instruciton = instruction,
                    unit = unitVal,
                    patientID =patId,
                    benVisitNo = benVisitNo
                )
                prescriptionList.add(pres);
            }
        }

        if(idString.nullIfEmpty() == null){
            doctorFlag = 9
        }
        else{
            doctorFlag = 2
        }
        val patientVisitInfoSync = PatientVisitInfoSync(
            patientID = patId,
            benVisitNo = benVisitNo,
            createNewBenFlow = createNewBenflow,
            nurseFlag = 9,
            doctorFlag = doctorFlag,
        )

        viewModel.saveNurseAndDoctorData(visitDB, chiefComplaints, patientVitals, diagnosisList, investigation, prescriptionList, patientVisitInfoSync)

    }

    private fun saveDoctorData(benVisitNo: Int) {

        var diagnosisList = mutableListOf<DiagnosisCaseRecord>()
        for (i in 0 until itemListD.size) {
            val diagnosisData = itemListD[i]
            if(diagnosisData.diagnosis.isNullOrEmpty()){
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), resources.getString(R.string.diagnosisCannotBeEmpty), Toast.LENGTH_SHORT).show()
                }
                return;
            }
            else {
                var diagnosis = DiagnosisCaseRecord(
                    diagnosisCaseRecordId = generateUuid(),
                    diagnosis = diagnosisData.diagnosis,
                    patientID = patId,
                    benVisitNo = benVisitNo
                )
                diagnosisList.add(diagnosis)
            }
        }


        val testName = binding.selectF.text.toString()
        val testNamesList = testName.split(",").map { it.trim() }
        val idString = testNamesList.joinToString(",") { testNameS ->
            val id = findKeyByValue(testNameMap,testNameS) // Replace with your function to get the ID
            id?.toString() ?: ""
        }

        val externalInvestigation = binding.inputExternalI.text.toString().nullIfEmpty()
        val counsellingTypesVal = binding.routeDropDownVal.text.toString().nullIfEmpty()
        val referVal = binding.referDropdownText.text.toString().nullIfEmpty()
        val referId = findKeyByValue(referNameMap,referVal)
        val investigation = InvestigationCaseRecord(
            investigationCaseRecordId = generateUuid(),
            testIds = idString.nullIfEmpty(),
            externalInvestigation = externalInvestigation,
            counsellingTypes = counsellingTypesVal,
            patientID = patId,
            institutionId = referId,
            benVisitNo = benVisitNo
        )


        val prescriptionList = mutableListOf<PrescriptionCaseRecord>();
        for (i in 0 until itemListP.size) {
            val prescriptionData = itemListP[i]
            var formVal = prescriptionData.id
            var freqVal = prescriptionData.frequency.nullIfEmpty()
            var unitVal = prescriptionData.unit.nullIfEmpty()
            var durVal = prescriptionData.duration.nullIfEmpty()
            var instruction = prescriptionData.instruction.nullIfEmpty()
            if (formVal != null) {
                val frequencyDescription = frequencyMap[freqVal]
                var mappedFrequency: String? = null
                if (frequencyDescription != null) {
                    mappedFrequency = frequencyDescription
                }
                var pres = PrescriptionCaseRecord(
                    prescriptionCaseRecordId = generateUuid(),
                    itemId = formVal,
                    frequency = mappedFrequency,
                    duration = durVal,
                    instruciton = instruction,
                    unit = unitVal?:"Day(s)",
                    patientID =patId,
                    benVisitNo = benVisitNo
                )
                prescriptionList.add(pres);
            }
        }
        if(idString.nullIfEmpty() == null){
            doctorFlag = 9
        }
        else{
            doctorFlag = 2
        }

        viewModel.saveDoctorData(diagnosisList, investigation, prescriptionList, benVisitInfo, doctorFlag)

    }
    fun saveTemp(uniqueTemplateNames: HashSet<String?>) {
        var tempNameVal = binding.inputTestName.text.toString()
        if(tempNameVal==null || tempNameVal.equals("null")|| tempNameVal.equals("")){
            requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), resources.getString(R.string.template_null), Toast.LENGTH_SHORT).show()
            }
        }else {
            if (uniqueTemplateNames.contains(binding.inputTestName.text.toString())) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.templte_exists),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                val isNameExists = tempDBVal.any { it?.templateName == tempNameVal }
                if (isNameExists) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            resources.getString(R.string.templte_exists),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val tempId = generateIntFromUuid()
                    val prescriptionTempList = mutableListOf<PrescriptionTemplateDB>();
                    for (i in 0 until itemListP.size) {
                        val prescriptionTemp = itemListP[i]
                        var formName = prescriptionTemp.form
                        var formVal = prescriptionTemp.id
                        var freqVal = prescriptionTemp.frequency.nullIfEmpty()
                        var unitVal = prescriptionTemp.unit.nullIfEmpty()
                        var durVal = prescriptionTemp.duration.nullIfEmpty()
                        var instruction = prescriptionTemp.instruction.nullIfEmpty()

                        if (formVal != null) {
                            var pres = viewModel.userIDVAl?.let {
                                PrescriptionTemplateDB(
                                    id = generateUuid(),
                                    tempID = tempId,
                                    templateName = tempNameVal,
                                    userID = it,
                                    drugName = formName,
                                    drugId = formVal,
                                    frequency = freqVal,
                                    duration = durVal,
                                    unit = unitVal ?: "Day(s)",
                                    instruction = instruction,
                                    deleteStatus = 0
                                )
                            }
                            if (pres != null) {
                                prescriptionTempList.add(pres)
                            }
                            Timber.tag("arr").i("${pres}")
                            if (pres != null) {
                                viewModel.savePrescriptionTemp(pres)
                            }
                        }
                    }
                    if (prescriptionTempList != null) {
                        viewModel.savePrescriptionTempToServer(prescriptionTempList)
                    }
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            resources.getString(R.string.template_save),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    binding.saveTemplate.isEnabled = false
                    binding.saveTemplate.alpha = 0.5f
                }
            }
        }
    }
    override fun getFragmentId(): Int {
        return R.id.case_record_custome_layout
    }

    override fun onSubmitAction() {
        navigateNext()
    }

    override fun onCancelAction() {
        if(preferenceDao.isUserOnlyDoctorOrMo() || (preferenceDao.isCHO() && preferenceDao.getCHOSecondRole() == "Doctor") ||
            (preferenceDao.isUserSwitchRole() && preferenceDao.getSwitchRole() == "Doctor")){
            val intent = Intent(context, HomeActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        else{
            findNavController().navigateUp()
        }
    }
    fun navigateNext() {
        if (preferenceDao.isUserOnlyDoctorOrMo() || (preferenceDao.isCHO() && preferenceDao.getCHOSecondRole() == "Doctor") ||
            (preferenceDao.isUserSwitchRole() && preferenceDao.getSwitchRole() == "Doctor")) {

            val validate = dAdapter.setError()
            if (validate == -1) {
                viewModel.deleteOldDoctorData(benVisitInfo.patient.patientID, benVisitInfo.benVisitNo!!)
                viewModel.isDataDeleted.observe(viewLifecycleOwner) { state ->
                    when (state!!) {
                        true-> {
                            saveDoctorData(benVisitInfo.benVisitNo!!)
                            viewModel.isDataSaved.observe(viewLifecycleOwner){
                                when(it!!){
                                    true ->{
                                        WorkerUtils.triggerAmritSyncWorker(requireContext())
                                        requireActivity().runOnUiThread {
                                            Toast.makeText(requireContext(), resources.getString(R.string.dataSavedCaseRecord), Toast.LENGTH_SHORT).show()
                                        }
                                        val intent = Intent(context, HomeActivity::class.java)
                                        startActivity(intent)
                                        requireActivity().finish()
                                    }
                                    else ->{

//                                        requireActivity().runOnUiThread {
//                                            Toast.makeText(requireContext(), resources.getString(R.string.something_wend_wong), Toast.LENGTH_SHORT).show()
//                                        }
                                    }
                                }
                            }
                        }
                        else -> {
//                            requireActivity().runOnUiThread {
//                                Toast.makeText(requireContext(), resources.getString(R.string.something_wend_wong), Toast.LENGTH_SHORT).show()
//                            }
                        }
                    }
                }
            } else {
                binding.diagnosisExtra.scrollToPosition(validate)
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.diagnosisCannotBeEmpty),
                    Toast.LENGTH_SHORT
                ).show()
            }

        } else {
            CoroutineScope(Dispatchers.Main).launch {
                if(masterDb!!.patientId.toString()!=null) {
                    patId = masterDb!!.patientId.toString()
                }

                val validate = dAdapter.setError()
                if (validate == -1) {
                    var benVisitNo = 0;
                    var createNewBenflow = false;
                    viewModel.getLastVisitInfoSync(patId).let {
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

                    saveNurseAndDoctorData(benVisitNo, createNewBenflow)

                    viewModel.isDataSaved.observe(viewLifecycleOwner){ state->
                        when(state!!){
                            true ->{
                                WorkerUtils.triggerAmritSyncWorker(requireContext())
                                requireActivity().runOnUiThread {
                                    Toast.makeText(requireContext(), resources.getString(R.string.dataSavedCaseRecord), Toast.LENGTH_SHORT).show()
                                }
                                val intent = Intent(context, HomeActivity::class.java)
                                startActivity(intent)
                                requireActivity().finish()
                            }
                            else ->{
//                                requireActivity().runOnUiThread {
//                                    Toast.makeText(requireContext(), resources.getString(R.string.something_wend_wong), Toast.LENGTH_SHORT).show()
//                                }
                            }
                        }

                    }

                } else {
                    showToast()
                }
//                }
            }
        }
    }
    fun showToast(){
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), resources.getString(R.string.diagnosisCannotBeEmpty), Toast.LENGTH_SHORT).show()
        }
    }
}