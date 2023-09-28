package org.piramalswasthya.cho.ui.commons.case_record

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.ChiefComplaintMultiAdapter
import org.piramalswasthya.cho.adapter.DiagnosisAdapter
import org.piramalswasthya.cho.adapter.PrescriptionAdapter
import org.piramalswasthya.cho.adapter.RecyclerViewItemChangeListenerD
import org.piramalswasthya.cho.adapter.RecyclerViewItemChangeListenersP
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.CaseRecordCustomLayoutBinding
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.model.CounsellingProvided
import org.piramalswasthya.cho.model.DiagnosisCaseRecord
import org.piramalswasthya.cho.model.DiagnosisValue
import org.piramalswasthya.cho.model.InvestigationCaseRecord
import org.piramalswasthya.cho.model.ItemMasterList
import org.piramalswasthya.cho.model.MasterDb
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.PrescriptionCaseRecord
import org.piramalswasthya.cho.model.PrescriptionValues
import org.piramalswasthya.cho.model.ProceduresMasterData
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.model.VitalsMasterDb
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.instructionDropdownList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.medicalReferDropdownVal
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.medicationFrequencyList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.tabletDosageList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.unitVal
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.utils.setBoxColor
import org.piramalswasthya.cho.utils.generateUuid
import org.piramalswasthya.cho.utils.nullIfEmpty
import org.piramalswasthya.cho.utils.setBoxColor
import java.util.Arrays
import javax.inject.Inject

@AndroidEntryPoint
class CaseRecordCustom: Fragment(R.layout.case_record_custom_layout), NavigationAdapter {
    private var _binding: CaseRecordCustomLayoutBinding? = null
    private val binding: CaseRecordCustomLayoutBinding
        get() = _binding!!

    private val viewModel: CaseRecordViewModel by viewModels()
    @Inject
    lateinit var preferenceDao: PreferenceDao
    private val initialItemD = DiagnosisValue()
    private val itemListD = mutableListOf(initialItemD)
    private val initialItemP = PrescriptionValues()
    private val itemListP = mutableListOf(initialItemP)
    private lateinit var dAdapter : DiagnosisAdapter
    private lateinit var chAdapter : ChiefComplaintMultiAdapter
    private lateinit var pAdapter : PrescriptionAdapter
    private var testNameMap = emptyMap<Int,String>()
    private var referNameMap = emptyMap<Int,String>()
    private val selectedTestName = mutableListOf<Int>()
    var familyM: MaterialCardView? = null
    var selectF: TextView? = null
    private val instructionDropdown= instructionDropdownList
    private val formMListVal = ArrayList<ItemMasterList>()
    private val counsellingTypes = ArrayList<CounsellingProvided>()
    private val procedureDropdown = ArrayList<ProceduresMasterData>()
    private val frequencyListVal = medicationFrequencyList
    private val referDropdownVal = medicalReferDropdownVal
    private val unitListVal = unitVal
    private val dosage = tabletDosageList
    private var masterDb: MasterDb? = null
    private lateinit var patientId : String
    private var patId = ""
    private lateinit var referDropdown: AutoCompleteTextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = CaseRecordCustomLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        familyM = binding.testName
        selectF = binding.selectF
        referDropdown = binding.referDropdownText
        if(preferenceDao.isUserOnlyDoctorOrMo()) {
            patientId = requireActivity().intent?.extras?.getString("patientId")!!
            patId= patientId
            viewModel.getVitalsDB(patId)
            viewModel.getChiefComplaintDB(patId)
        }

        lifecycleScope.launch {
            testNameMap = viewModel.getTestNameTypeMap()
        }
        lifecycleScope.launch {
            referNameMap = viewModel.getReferNameTypeMap()
        }
        var chiefComplaintDB = mutableListOf<ChiefComplaintDB>()

        if (preferenceDao.isUserOnlyDoctorOrMo()) {
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
                        beneficiaryID = 0,
                        beneficiaryRegID = 0,
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
                   beneficiaryID = 0,
                   beneficiaryRegID = 0,
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
            pAdapter.notifyDataSetChanged()
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
                showDialogWithFamilyMembers(procedureDropdown)
            }
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
            itemListP,
            formMListVal,
            frequencyListVal,
            unitListVal,
            instructionDropdown,
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
        if(preferenceDao.isUserOnlyDoctorOrMo()){
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
    private fun populateVitalsFieldsW(vitals: VitalsMasterDb) {
        hideNullFieldsW(vitals)
        binding.inputHeight.setText(vitals?.height.toString())
        binding.inputWeight.setText(vitals?.weight.toString())
        binding.inputBmi.setText(vitals.bmi.toString())
        binding.inputWaistCircum.setText(vitals.waistCircumference.toString())
        binding.inputTemperature.setText(vitals.temperature.toString())
        binding.inputPulseRate.setText(vitals.pulseRate.toString())
        binding.inputSpo2.setText(vitals.spo2.toString())
        binding.inputBpDiastolic.setText(vitals.bpDiastolic.toString())
        binding.inputBpSystolic.setText(vitals.bpSystolic.toString())
        binding.inputRespiratoryPerMin.setText(vitals.respiratoryRate.toString())
        binding.inputRbs.setText(vitals.rbs.toString())
    }
    private fun populateVitalsFields() {
        hideNullFields()
        // Check if the masterDb and vitalsMasterDb are not null
        if (masterDb != null && masterDb?.vitalsMasterDb != null) {
            val vitals = masterDb?.vitalsMasterDb
            binding.inputHeight.setText(vitals?.height.toString())
            binding.inputWeight.setText(vitals?.weight.toString())
            binding.inputBmi.setText(vitals?.bmi.toString())
            binding.inputWaistCircum.setText(vitals?.waistCircumference.toString())
            binding.inputTemperature.setText(vitals?.temperature.toString())
            binding.inputPulseRate.setText(vitals?.pulseRate.toString())
            binding.inputSpo2.setText(vitals?.spo2.toString())
            binding.inputBpDiastolic.setText(vitals?.bpDiastolic.toString())
            binding.inputBpSystolic.setText(vitals?.bpSystolic.toString())
            binding.inputRespiratoryPerMin.setText(vitals?.respiratoryRate.toString())
            binding.inputRbs.setText(vitals?.rbs.toString())
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

        if (itemC.isNullOrEmpty() || itemC.equals("null")) {
            binding.waistCircumEditTxt.visibility = View.GONE
        } else {
            binding.waistCircumEditTxt.visibility = View.VISIBLE
        }

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
        var  itemC = masterDb?.vitalsMasterDb?.waistCircumference.toString()
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
        if(itemC.isNullOrEmpty()||itemC.equals("null")){
            binding.waistCircumEditTxt.visibility = View.GONE
        }
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
        if((itemH.isNullOrEmpty() && itemW.isNullOrEmpty() && itemB.isNullOrEmpty() && itemC.isNullOrEmpty() && itemT.isNullOrEmpty() && itemP.isNullOrEmpty() && itemS.isNullOrEmpty() && itemBs.isNullOrEmpty() && itemBd.isNullOrEmpty() && itemRs.isNullOrEmpty()
            && itemRb.isNullOrEmpty()) ||
            (itemH.equals("null") && itemW.equals("null") && itemB.equals("null") && itemC.equals("null") && itemT.equals("null") && itemP.equals("null") && itemS.equals("null") && itemBs.equals("null") && itemBd.equals("null") && itemRs.equals("null") && itemRb.equals("null"))){
            binding.vitalsExtra.visibility= View.INVISIBLE
        }
    }


    private fun showDialogWithFamilyMembers(proceduresMasterData: List<ProceduresMasterData>) {
        val selectedItems = BooleanArray(procedureDropdown.size) { selectedTestName.contains(it) }

        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Select Test Name")
            .setCancelable(false)
            .setMultiChoiceItems(
                procedureDropdown.map { it.procedureName }.toTypedArray(),
                selectedItems
            ) { _, which, isChecked ->
                if (isChecked) {
                    selectedTestName.add(which)
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
    private fun addCaseRecordDataToCatche() {
        // save diagnosis
        for (i in 0 until itemListD.size) {
            val diagnosisData = itemListD[i]
            if (diagnosisData.diagnosis.isNotEmpty()) {
                var diagnosis = DiagnosisCaseRecord(
                    diagnosisCaseRecordId = generateUuid(),
                    diagnosis= diagnosisData.diagnosis,
                    patientID = patId
                )
                viewModel.saveDiagnosisToCache(diagnosis)
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
            institutionId = referId
        )
        viewModel.saveInvestigationToCache(investigation)
        //save investigation
        for (i in 0 until itemListP.size) {
            val prescriptionData = itemListP[i]
            var formVal = prescriptionData.id
            var freqVal = prescriptionData.frequency.nullIfEmpty()
            var unitVal = prescriptionData.unit.nullIfEmpty()
            var durVal = prescriptionData.duration.nullIfEmpty()
            var instruction = prescriptionData.instruction.nullIfEmpty()
                var pres = PrescriptionCaseRecord(
                    prescriptionCaseRecordId = generateUuid(),
                    itemId = formVal,
                    frequency = freqVal,
                    duration = durVal,
                    instruciton = instruction,
                    unit = unitVal,
                    patientID =patId
                 )
                viewModel.savePrescriptionToCache(pres)
            }
    }
    private fun addVitalsDataToCache(lastVisitNo: Int){
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
            benVisitNo = lastVisitNo+1
        )
        viewModel.savePatientVitalInfoToCache(patientVitals)
    }
    private fun addVisitRecordDataToCache(lastVisitNo: Int){
        val visitDB = VisitDB(
            visitId = generateUuid(),
            category = masterDb?.visitMasterDb?.category.nullIfEmpty(),
            reasonForVisit = masterDb?.visitMasterDb?.reason.nullIfEmpty() ,
            subCategory = masterDb?.visitMasterDb?.subCategory.nullIfEmpty(),
            patientID = patId,
            benVisitNo = lastVisitNo+1
        )

        viewModel.saveVisitDbToCatche(visitDB)
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
                beneficiaryID = null,
                beneficiaryRegID=null,
                benFlowID=null,
                benVisitNo = lastVisitNo+1
            )
            viewModel.saveChiefComplaintDbToCatche(chiefC)
        }
    }

    private fun addPatientVisitInfoSyncToCache(lastVisitNo: Int){
        val patientVisitInfoSync = PatientVisitInfoSync(
            patientID = patId,
            benVisitNo = lastVisitNo + 1,
            createNewBenFlow = true,
        )
        viewModel.savePatientVisitInfoSync(patientVisitInfoSync)
    }

    override fun getFragmentId(): Int {
        return R.id.case_record_custome_layout
    }

    override fun onSubmitAction() {
        navigateNext()
    }

    override fun onCancelAction() {
        if(preferenceDao.isUserOnlyDoctorOrMo()){
            val intent = Intent(context, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        else{
            findNavController().navigateUp()
        }
    }
    fun navigateNext() {
        if (preferenceDao.isUserOnlyDoctorOrMo()) {
            addCaseRecordDataToCatche()
            val validate = dAdapter.setError()
            if (validate == -1) {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.dataSavedCaseRecord),
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(context, HomeActivity::class.java)
                startActivity(intent)
            } else {
                binding.diagnosisExtra.scrollToPosition(validate)
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.diagnosisCannotBeEmpty),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                if(masterDb!!.patientId.toString()!=null) {
                    patId = masterDb!!.patientId.toString()
                }
                val hasUnSyncedNurseData = viewModel.hasUnSyncedNurseData(patId)
                if (hasUnSyncedNurseData) {
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.unsyncedNurseData),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val lastVisitNo = viewModel.getLastVisitNo(patId)
                    addPatientVisitInfoSyncToCache(lastVisitNo)
                    addVisitRecordDataToCache(lastVisitNo)
                    addVitalsDataToCache(lastVisitNo)
                    addCaseRecordDataToCatche()
                    val validate = dAdapter.setError()
                    if (validate == -1) {
                        Toast.makeText(
                            requireContext(),
                            resources.getString(R.string.dataSavedCaseRecord),
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(context, HomeActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            resources.getString(R.string.diagnosisCannotBeEmpty),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}