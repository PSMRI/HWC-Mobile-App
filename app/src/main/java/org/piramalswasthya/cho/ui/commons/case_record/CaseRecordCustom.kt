package org.piramalswasthya.cho.ui.commons.case_record

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.internal.notify
import org.hl7.fhir.r4.model.Annotation
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.Reference
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.DiagnosisAdapter
import org.piramalswasthya.cho.adapter.PrescriptionAdapter
import org.piramalswasthya.cho.adapter.RecyclerViewItemChangeListenerD
import org.piramalswasthya.cho.adapter.RecyclerViewItemChangeListenersP
import org.piramalswasthya.cho.databinding.CaseRecordCustomLayoutBinding
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.model.CounsellingTypes
import org.piramalswasthya.cho.model.DiagnosisCaseRecord
import org.piramalswasthya.cho.model.DiagnosisValue
import org.piramalswasthya.cho.model.InvestigationCaseRecord
import org.piramalswasthya.cho.model.ItemMasterList
import org.piramalswasthya.cho.model.MasterDb
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.PrescriptionCaseRecord
import org.piramalswasthya.cho.model.PrescriptionValues
import org.piramalswasthya.cho.model.ProceduresMasterData
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.ui.commons.DropdownConst
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.medicalTestList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.medicationFormsList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.medicationFrequencyList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.medicationRouteList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.tabletDosageList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.unitVal
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
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
    private lateinit var pAdapter : PrescriptionAdapter
    private val selectedTestName = mutableListOf<Int>()
    var familyM: MaterialCardView? = null
    var selectF: TextView? = null
    private val formMListVal = ArrayList<ItemMasterList>()
    private val counsellingTypes = ArrayList<CounsellingTypes>()
    private val procedureDropdown = ArrayList<ProceduresMasterData>()
    private val frequencyListVal = medicationFrequencyList
    private val unitListVal = unitVal
    private var masterDb: MasterDb? = null

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

        masterDb = arguments?.getSerializable("MasterDb") as? MasterDb

        viewModel.formMedicineDosage.observe(viewLifecycleOwner) { f ->
            formMListVal.clear()
            formMListVal.addAll(f)
            pAdapter.notifyDataSetChanged()
        }
        viewModel.counsellingTypes.observe(viewLifecycleOwner) { f ->
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
            dAdapter.notifyItemInserted(itemListD.size - 1)
            binding.plusButtonD.isEnabled = !isAnyItemEmptyD()
            binding.plusButtonD.isEnabled = false
        }
        pAdapter = PrescriptionAdapter(
            itemListP,
            formMListVal,
            frequencyListVal,
            unitListVal,
            counsellingTypes,
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
            pAdapter.notifyItemInserted(itemListP.size - 1)
            binding.plusButtonP.isEnabled = !isAnyItemEmptyP()
            binding.plusButtonP.isEnabled = false
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
                    diagnosisCaseRecordId = "33+${i}",
                   diagnosis= diagnosisData.diagnosis
                )
                viewModel.saveDiagnosisToCache(diagnosis)
            }
        }
        val testName = binding.selectF.text.toString()
        val externalInvestigation = binding.inputExternalI.text.toString()
        val investigation = InvestigationCaseRecord(
            investigationCaseRecordId = "33",
            testName = testName,
            externalInvestigation = externalInvestigation
        )
        viewModel.saveInvestigationToCache(investigation)
        //save investigation
        for (i in 0 until itemListP.size) {
            val prescriptionData = itemListP[i]
            var freq = ""
            if (prescriptionData.form.isNotEmpty()||prescriptionData.frequency.isNotEmpty()||prescriptionData.unit.isNotEmpty()) {
                if(prescriptionData.frequency.equals("1-0-0")||prescriptionData.frequency.equals("0-1-0")||prescriptionData.frequency.equals("0-0-1"))
                    freq = "Once Daily"
                if(prescriptionData.frequency.equals("1-1-0")||prescriptionData.frequency.equals("0-1-1")||prescriptionData.frequency.equals("1-0-1"))
                    freq = "Twice Daily"
                if(prescriptionData.frequency.equals("1-1-1"))
                    freq = "Thrice Daily"
                if(prescriptionData.frequency.equals("1-1-1-1"))
                    freq = "Four Times in a Day"
                var pres = PrescriptionCaseRecord(
                    prescriptionCaseRecordId = "33+${i}",
                    form = prescriptionData.form,
                    frequency = freq ,
                    duration = prescriptionData.duration,
                    instruciton = prescriptionData.instruction,
                    unit = prescriptionData.unit,
                    counsellingTypes = prescriptionData.counsellingTypes
                    )
                viewModel.savePrescriptionToCache(pres)
            }
        }
    }
    private fun addVitalsDataToCache(){
        val patientVitals = PatientVitalsModel(
            vitalsId = "1",
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
            rbs = masterDb?.vitalsMasterDb?.rbs
        )
        viewModel.savePatientVitalInfoToCache(patientVitals)
    }
    private fun addVisitRecordDataToCache(){
        val visitDB = VisitDB(
            visitId = "33",
            category = masterDb?.visitMasterDb?.category ?: "",
            reasonForVisit = masterDb?.visitMasterDb?.reason ?: "",
            subCategory = masterDb?.visitMasterDb?.subCategory ?: ""
        )

        viewModel.saveVisitDbToCatche(visitDB)
        for (i in 0 until (masterDb?.visitMasterDb?.chiefComplaint?.size ?: 0)) {
            val chiefComplaintItem = masterDb!!.visitMasterDb!!.chiefComplaint!![i]
            val chiefC = ChiefComplaintDB(
                id = "33+${i}",
                chiefComplaint = chiefComplaintItem.chiefComplaint,
                duration =  chiefComplaintItem.duration,
                durationUnit = chiefComplaintItem.durationUnit,
                description = chiefComplaintItem.description
            )
            viewModel.saveChiefComplaintDbToCatche(chiefC)
        }
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
        val intent = Intent(context, HomeActivity::class.java)
        startActivity(intent)
    }

}