package org.piramalswasthya.cho.ui.commons.case_record

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.ChiefComplaintMultiAdapter
import org.piramalswasthya.cho.adapter.DiagnosisAdapter
import org.piramalswasthya.cho.adapter.PrescriptionAdapter
import org.piramalswasthya.cho.adapter.RecyclerViewItemChangeListenerD
import org.piramalswasthya.cho.adapter.RecyclerViewItemChangeListenersP
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
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.instructionDropdownList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.medicalReferDropdownVal
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.medicationFrequencyList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.tabletDosageList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.unitVal
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.utils.setBoxColor
import org.piramalswasthya.cho.utils.generateUuid
import org.piramalswasthya.cho.utils.setBoxColor
import java.util.Arrays

@AndroidEntryPoint
class CaseRecordCustom: Fragment(R.layout.case_record_custom_layout), NavigationAdapter {
    private var _binding: CaseRecordCustomLayoutBinding? = null
    private val binding: CaseRecordCustomLayoutBinding
        get() = _binding!!

    private val viewModel: CaseRecordViewModel by viewModels()

    private val initialItemD = DiagnosisValue()
    private val itemListD = mutableListOf(initialItemD)
    private val initialItemP = PrescriptionValues()
    private val itemListP = mutableListOf(initialItemP)
    private lateinit var dAdapter : DiagnosisAdapter
    private lateinit var chAdapter : ChiefComplaintMultiAdapter
    private lateinit var pAdapter : PrescriptionAdapter
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
        masterDb = arguments?.getSerializable("MasterDb") as? MasterDb
        var chiefComplaintDB = mutableListOf<ChiefComplaintDB>() // Create an empty mutable list

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
//        val referAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line)
//        binding.referDropdownText.setAdapter(referAdapter)
//
//        viewModel.counsellingTypes.observe(viewLifecycleOwner){c->
//            counsellingTypesAdapter.clear()
//            counsellingTypesAdapter.addAll(c.map{it.counsellingType})
//            counsellingTypesAdapter.notifyDataSetChanged()
//        }
        val ageAAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, referDropdownVal)
        referDropdown.setAdapter(ageAAdapter)

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
            dosage,
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
        populateVitalsFields()
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
        if(itemH.isNullOrEmpty()){
            binding.heightEditTxt.visibility = View.GONE
        }
        if(itemW.isNullOrEmpty()){
            binding.weightEditTxt.visibility = View.GONE
        }
        if(itemB.isNullOrEmpty()){
            binding.bmill.visibility = View.GONE
        }
        if(itemC.isNullOrEmpty()){
            binding.waistCircumEditTxt.visibility = View.GONE
        }
        if(itemT.isNullOrEmpty()){
            binding.temperatureEditTxt.visibility = View.GONE
        }
        if(itemP.isNullOrEmpty()){
            binding.pulseRateEditTxt.visibility = View.GONE
        }
        if(itemS.isNullOrEmpty()){
            binding.spo2EditTxt.visibility = View.GONE
        }
        if(itemBs.isNullOrEmpty()){
            binding.bpSystolicEditTxt.visibility = View.GONE
        }
        if(itemBd.isNullOrEmpty()){
            binding.bpDiastolicEditTxt.visibility = View.GONE
        }
        if(itemRs.isNullOrEmpty()){
            binding.respiratoryEditTxt.visibility = View.GONE
        }
        if(itemRb.isNullOrEmpty()){
            binding.rbsEditTxt.visibility = View.GONE
        }
        if(itemH.isNullOrEmpty() && itemW.isNullOrEmpty() && itemB.isNullOrEmpty() && itemC.isNullOrEmpty() && itemT.isNullOrEmpty() && itemP.isNullOrEmpty() && itemS.isNullOrEmpty() && itemBs.isNullOrEmpty() && itemBd.isNullOrEmpty() && itemRs.isNullOrEmpty() && itemRb.isNullOrEmpty()){
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
            if (item.form.isEmpty()||item.frequency.isEmpty()||item.duration.isEmpty()||item.unit.isEmpty()) {
                return true
            }
        }
        return false
    }
    private fun addCaseRecordDataToCatche() {
        // save diagnosis
        for (i in 0 until itemListD.size) {
            val diagnosisData = itemListD[i]
            if (diagnosisData.diagnosis.isNotEmpty()) {
                var diagnosis = DiagnosisCaseRecord(
                    diagnosisCaseRecordId = generateUuid(),
                    diagnosis= diagnosisData.diagnosis,
                    patientID = masterDb!!.patientId
                )
                viewModel.saveDiagnosisToCache(diagnosis)
            }
        }
        val testName = binding.selectF.text.toString()
        val externalInvestigation = binding.inputExternalI.text.toString()
        val counsellingTypesVal = binding.routeDropDownVal.text.toString()
        val referVal = binding.referDropdownText.text.toString()
        val investigation = InvestigationCaseRecord(
            investigationCaseRecordId = generateUuid(),
            testName = testName,
            externalInvestigation = externalInvestigation,
            counsellingTypes = counsellingTypesVal,
            patientID = masterDb!!.patientId,
            refer = referVal
        )
        viewModel.saveInvestigationToCache(investigation)
        //save investigation
        for (i in 0 until itemListP.size) {
            val prescriptionData = itemListP[i]
            var freq = ""
            if (prescriptionData.form.isNotEmpty()||prescriptionData.frequency.isNotEmpty()||prescriptionData.unit.isNotEmpty()) {
                var pres = PrescriptionCaseRecord(
                    prescriptionCaseRecordId = generateUuid(),
                    form = prescriptionData.form,
                    frequency = prescriptionData.frequency ,
                    duration = prescriptionData.duration,
                    instruciton = prescriptionData.instruction,
                    unit = prescriptionData.unit,
                    patientID = masterDb!!.patientId
                 )
                viewModel.savePrescriptionToCache(pres)
            }
        }
    }
    private fun addVitalsDataToCache(){
        val patientVitals = PatientVitalsModel(
            vitalsId = generateUuid(),
            height = masterDb?.vitalsMasterDb?.height,
            weight = masterDb?.vitalsMasterDb?.weight,
            bmi = masterDb?.vitalsMasterDb?.bmi,
            waistCircumference = masterDb?.vitalsMasterDb?.waistCircumference,
            temperature = masterDb?.vitalsMasterDb?.temperature,
            pulseRate = masterDb?.vitalsMasterDb?.pulseRate,
            spo2 = masterDb?.vitalsMasterDb?.spo2,
            bpDiastolic = masterDb?.vitalsMasterDb?.bpDiastolic,
            bpSystolic = masterDb?.vitalsMasterDb?.bpSystolic,
            respiratoryRate = masterDb?.vitalsMasterDb?.respiratoryRate,
            rbs = masterDb?.vitalsMasterDb?.rbs,
            patientID = masterDb!!.patientId
        )
        viewModel.savePatientVitalInfoToCache(patientVitals)
    }
    private fun addVisitRecordDataToCache(){
        val visitDB = VisitDB(
            visitId = generateUuid(),
            category = masterDb?.visitMasterDb?.category,
            reasonForVisit = masterDb?.visitMasterDb?.reason,
            subCategory = masterDb?.visitMasterDb?.subCategory,
            patientID = masterDb!!.patientId
        )

        viewModel.saveVisitDbToCatche(visitDB)
        for (i in 0 until (masterDb?.visitMasterDb?.chiefComplaint?.size ?: 0)) {
            val chiefComplaintItem = masterDb!!.visitMasterDb!!.chiefComplaint!![i]
            val chiefC = ChiefComplaintDB(
                id = generateUuid(),
                chiefComplaintId = chiefComplaintItem.id,
                chiefComplaint = chiefComplaintItem.chiefComplaint,
                duration =  chiefComplaintItem.duration,
                durationUnit = chiefComplaintItem.durationUnit,
                description = chiefComplaintItem.description,
                patientID = masterDb!!.patientId
            )
            viewModel.saveChiefComplaintDbToCatche(chiefC)
        }
    }

    private fun addPatientVisitInfoSyncToCache(){
        val patientVisitInfoSync = PatientVisitInfoSync(masterDb!!.patientId)
        viewModel.savePatientVisitInfoSync(patientVisitInfoSync)
    }

    override fun getFragmentId(): Int {
        return R.id.case_record_custome_layout
    }

    override fun onSubmitAction() {
        navigateNext()
    }

    override fun onCancelAction() {
        findNavController().navigateUp()
    }
    fun navigateNext(){

        addVisitRecordDataToCache()
        addVitalsDataToCache()
        addCaseRecordDataToCatche()
        addPatientVisitInfoSyncToCache()
        val validate = dAdapter.setError()
        if(validate==-1) {
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.dataSavedCaseRecord),
                Toast.LENGTH_SHORT
            ).show()
            val intent = Intent(context, HomeActivity::class.java)
            startActivity(intent)
        }else{
            binding.diagnosisExtra.scrollToPosition(validate)
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.diagnosisCannotBeEmpty),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}