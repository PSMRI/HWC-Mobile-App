package org.piramalswasthya.cho.ui.commons.history_custom

import org.piramalswasthya.cho.ui.commons.history_custom.dialog.IllnessDialogFragment
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentHistoryCustomBinding
import org.piramalswasthya.cho.model.CovidVaccinationStatusHistory
import org.piramalswasthya.cho.model.AssociateAilmentsHistory
import org.piramalswasthya.cho.model.Immunization
import org.piramalswasthya.cho.model.MedicationHistory
import org.piramalswasthya.cho.model.PastIllnessHistory
import org.piramalswasthya.cho.model.PastSurgeryHistory
import org.piramalswasthya.cho.model.TobaccoAlcoholHistory
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.ui.HistoryFieldsInterface
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments.AAFragments
import org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments.AllergyFragment
import org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments.IllnessFieldsFragment
import org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments.MedicationFragment
import org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments.PastSurgeryFragment
import org.piramalswasthya.cho.ui.commons.history_custom.dialog.AADialogFragment
import org.piramalswasthya.cho.ui.commons.history_custom.dialog.AlcoholDialogFragment
import org.piramalswasthya.cho.ui.commons.history_custom.dialog.AllergyDialogFragment
import org.piramalswasthya.cho.ui.commons.history_custom.dialog.MedicationDialogFragment
import org.piramalswasthya.cho.ui.commons.history_custom.dialog.TobaccoDialogFragment
import java.math.BigDecimal

@AndroidEntryPoint
class HistoryCustomFragment : Fragment(R.layout.fragment_history_custom), NavigationAdapter,HistoryFieldsInterface {

    private val AgeGroup = arrayOf(
        "12-19",
        "19-59",
        "More than 60"
    )
    private val vaccinationStatus = arrayOf(
        "Yes","No"
    )

    private var _binding: FragmentHistoryCustomBinding? = null
    private val binding: FragmentHistoryCustomBinding
        get() = _binding!!

    var addCountIllness: Int = 0
    var deleteCountIllness: Int = 0
    var addCountSurgery: Int = 0
    var deleteCountSurgery: Int = 0
    var addCountAA:Int =0
    var deleteCountAA:Int=0
    var addCountM:Int =0
    var deleteCountM:Int=0
    var addCountAllg:Int =0
    var deleteCountAllg:Int=0
    var illnessTag = mutableListOf<String>()
    var surgeryTag = mutableListOf<String>()
    var aaTag = mutableListOf<String>()
    var mTag = mutableListOf<String>()
    var allgTag = mutableListOf<String>()
    private var illnessMap = emptyMap<Int,String>()
    private var surgeryMap = emptyMap<Int,String>()
    private var doseTypeMap = emptyMap<Int,String>()
    private var vaccineTypeMap = emptyMap<Int,String>()
    private val viewModel:HistoryCustomViewModel by viewModels()
    private lateinit var dropdownAgeG: AutoCompleteTextView
    private lateinit var dropdownVS: AutoCompleteTextView
    private var userInfo: UserCache? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        addCountIllness=0
        deleteCountIllness=0
        addCountSurgery=0
        deleteCountSurgery=0
        addCountAA=0
        deleteCountAA=0
        addCountM=0
        deleteCountM=0

        addCountAllg=0
        deleteCountAllg=0
        _binding = FragmentHistoryCustomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dropdownAgeG = binding.ageGrText
        dropdownVS = binding.vStatusText

        val ageAAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, AgeGroup)
        dropdownAgeG.setAdapter(ageAAdapter)
        val vacAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, vaccinationStatus)
        dropdownVS.setAdapter(vacAdapter)

        val vacTAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line)
        binding.vTypeText.setAdapter(vacTAdapter)

        viewModel.vaccinationTypeDropdown.observe(viewLifecycleOwner){vc->
            vacTAdapter.clear()
            vacTAdapter.addAll(vc.map{it.vaccineType})
            vacTAdapter.notifyDataSetChanged()
        }

        val doseAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line)
        binding.doseTakenText.setAdapter(doseAdapter)

        viewModel.doseTypeDropdown.observe(viewLifecycleOwner){dose->
            doseAdapter.clear()
            doseAdapter.addAll(dose.map{it.doseType})
            doseAdapter.notifyDataSetChanged()
        }

        val tobaccoAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item)
        binding.tobaccoText.setAdapter(tobaccoAdapter)

        viewModel.tobaccoDropdown.observe( viewLifecycleOwner) { tob ->
            tobaccoAdapter.clear()
            tobaccoAdapter.addAll(tob.map { it.habitValue })
            tobaccoAdapter.notifyDataSetChanged()
        }

        val alcoholAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item)
        binding.alcoholText.setAdapter(alcoholAdapter)

        viewModel.alcoholDropdown.observe( viewLifecycleOwner) { alc ->
            alcoholAdapter.clear()
            alcoholAdapter.addAll(alc.map { it.habitValue })
            alcoholAdapter.notifyDataSetChanged()
        }

        binding.btnPreviousHistory.setOnClickListener {
            openIllnessDialogBox()
        }
        binding.btnPreviousHistoryAA.setOnClickListener {
            openAADialogBox()
        }
        binding.btnPreviousHistoryMedical.setOnClickListener {
            openMDialogBox()
        }
        binding.btnPreviousHistoryPersonalT.setOnClickListener {
            openTDialogBox()
        }
        binding.btnPreviousHistoryPersonalA.setOnClickListener {
            openADialogBox()
        }
        binding.btnPreviousHistoryPersonalAlg.setOnClickListener {
            openAllgDialogBox()
        }
        lifecycleScope.launch {
            illnessMap = viewModel.getIllMap()
        }
        lifecycleScope.launch {
            surgeryMap = viewModel.getSurgMap()
        }
        lifecycleScope.launch {
            doseTypeMap = viewModel.getDoseTypeMap()
        }
        lifecycleScope.launch {
            vaccineTypeMap = viewModel.getVaccineTypeMap()
        }
        viewModel.getLoggedInUserDetails()
        viewModel.boolCall.observe(viewLifecycleOwner){
            if(it){
                userInfo = viewModel.loggedInUser
                viewModel.resetBool()
            }
        }
        addIllnessFields(addCountIllness)
        addSurgeryFields(addCountSurgery)
        addAAFields(addCountAA)
        addMFields(addCountM)
        addAllgFields(addCountAllg)
    }

    private fun addAllgFields(count:Int){
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction :FragmentTransaction = fragmentManager.beginTransaction()
        val allgFields = AllergyFragment()
        val tag = "Extra Allg$count"
        allgFields.setFragmentTag(tag)
        allgFields.setListener(this)
        fragmentTransaction.add(binding.personalAlgExtra.id,allgFields,tag)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
        allgTag.add(tag)
        addCountAllg+=1
    }
    private fun addMFields(count:Int){
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction :FragmentTransaction = fragmentManager.beginTransaction()
        val mFields = MedicationFragment()
        val tag = "Extra m$count"
        mFields.setFragmentTag(tag)
        mFields.setListener(this)
        fragmentTransaction.add(binding.medicationExtra.id,mFields,tag)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
        mTag.add(tag)
        addCountM+=1
    }
    private fun addAAFields(count:Int){
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction :FragmentTransaction = fragmentManager.beginTransaction()
        val aaFields = AAFragments()
        val tag = "Extra AA$count"
        aaFields.setFragmentTag(tag)
        aaFields.setListener(this)
        fragmentTransaction.add(binding.associatedAilmentsExtra.id,aaFields,tag)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
        aaTag.add(tag)
        addCountAA+=1
    }

    private fun addSurgeryFields(count:Int){
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction :FragmentTransaction = fragmentManager.beginTransaction()
        val surgeryFields = PastSurgeryFragment()
        val tag = "Extra Surgery$count"
        surgeryFields.setFragmentTag(tag)
        surgeryFields.setListener(this)
        fragmentTransaction.add(binding.pastSurgeryExtra.id,surgeryFields,tag)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
        surgeryTag.add(tag)
        addCountSurgery+=1
    }

    private fun addIllnessFields(count:Int){
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction :FragmentTransaction = fragmentManager.beginTransaction()
        val illnessFields = IllnessFieldsFragment()
        val tag = "Extra Illness_$count"
        illnessFields.setFragmentTag(tag)
        illnessFields.setListener(this)
        fragmentTransaction.add(binding.pastIllnessExtra.id,illnessFields,tag)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
        illnessTag.add(tag)
        addCountIllness+=1
    }
    private fun deleteAllgFields(tag: String){
        val fragmentManager : FragmentManager = requireActivity().supportFragmentManager
        val fragmentToDelete = fragmentManager.findFragmentByTag(tag)
        if (fragmentToDelete != null) {
            fragmentManager.beginTransaction().remove(fragmentToDelete).commit()
            allgTag.remove(tag)
            deleteCountAllg += 1
        }
    }
    private fun deleteAAFields(tag: String){
        val fragmentManager : FragmentManager = requireActivity().supportFragmentManager
        val fragmentToDelete = fragmentManager.findFragmentByTag(tag)
        if (fragmentToDelete != null) {
            fragmentManager.beginTransaction().remove(fragmentToDelete).commit()
            aaTag.remove(tag)
            deleteCountAA += 1
        }
    }
    private fun deleteMFields(tag: String){
        val fragmentManager : FragmentManager = requireActivity().supportFragmentManager
        val fragmentToDelete = fragmentManager.findFragmentByTag(tag)
        if (fragmentToDelete != null) {
            fragmentManager.beginTransaction().remove(fragmentToDelete).commit()
            mTag.remove(tag)
            deleteCountM += 1
        }
    }
    private fun deleteIllnessFields(tag: String){
        val fragmentManager : FragmentManager = requireActivity().supportFragmentManager
        val fragmentToDelete = fragmentManager.findFragmentByTag(tag)
        if (fragmentToDelete != null) {
            fragmentManager.beginTransaction().remove(fragmentToDelete).commit()
            illnessTag.remove(tag)
            deleteCountIllness += 1
        }
    }
    private fun deleteSurgeryFields(tag: String){
        val fragmentManager : FragmentManager = requireActivity().supportFragmentManager
        val fragmentToDelete = fragmentManager.findFragmentByTag(tag)
        if (fragmentToDelete != null) {
            fragmentManager.beginTransaction().remove(fragmentToDelete).commit()
            surgeryTag.remove(tag)
            deleteCountSurgery += 1
        }
    }
    override fun onDeleteButtonClickedAA(fragmentTag: String) {
        if(addCountAA - 1 > deleteCountAA) deleteAAFields(fragmentTag)
    }

    override fun onAddButtonClickedAA(fragmentTag: String) {
        addAAFields(addCountAA)
    }
    override fun onAddButtonClickedAlg(fragmentTag: String) {
        addAllgFields(addCountAllg)
    }

    override fun onDeleteButtonClickedAlg(fragmentTag: String) {
        if(addCountAllg - 1 > deleteCountAllg) deleteAllgFields(fragmentTag)
    }

    override fun onDeleteButtonClickedM(fragmentTag: String) {
        if(addCountM - 1 > deleteCountM) deleteMFields(fragmentTag)
    }

    override fun onAddButtonClickedM(fragmentTag: String) {
        addMFields(addCountM)
    }
    override fun onDeleteButtonClickedSurgery(fragmentTag: String) {
        if(addCountSurgery - 1 > deleteCountSurgery) deleteSurgeryFields(fragmentTag)
    }

    override fun onAddButtonClickedSurgery(fragmentTag: String) {
        addSurgeryFields(addCountSurgery)
    }
    override fun onDeleteButtonClickedIllness(fragmentTag: String) {
        if(addCountIllness - 1 > deleteCountIllness) deleteIllnessFields(fragmentTag)
    }

    override fun onAddButtonClickedIllness(fragmentTag: String) {
        addIllnessFields(addCountIllness)
    }
    private fun openIllnessDialogBox() {

        val dialogFragment = IllnessDialogFragment()
        dialogFragment.show(parentFragmentManager, "illness_dialog_box")
    }
    private fun openAADialogBox() {
        val dialogFragment = AADialogFragment()
        dialogFragment.show(parentFragmentManager, "aa_dialog_box")
    }
    private fun openTDialogBox() {
        val dialogFragment = TobaccoDialogFragment()
        dialogFragment.show(parentFragmentManager, "fragment_tobacco_dialog")
    }
    private fun openADialogBox() {
        val dialogFragment = AlcoholDialogFragment()
        dialogFragment.show(parentFragmentManager, "fragment_alcohol_dialog")
    }
    private fun openAllgDialogBox() {
        val dialogFragment = AllergyDialogFragment()
        dialogFragment.show(parentFragmentManager, "fragment_allergy_dialog")
    }
    private fun openMDialogBox() {

        val dialogFragment = MedicationDialogFragment()
        dialogFragment.show(parentFragmentManager, "medication_dialog_box")
    }
    override fun getFragmentId(): Int {
        return R.id.fragment_history_custom
    }

    override fun onSubmitAction() {
        navigateNext()
    }

    override fun onCancelAction() {
        findNavController().navigateUp()
    }
     fun navigateNext(){
//        addMedicationDataToCatche()
//        addPastIllnessAndSurgeryData()
//        addCovidData()
//        addMedicationData()
//        addTobAndAlcDataToCatche()
//        addTobAndAlcData()
//         addAssociateAilmentsData()
//         addAssociateAilmentsDataToCatche()
//         addCovidVaccinationDataToCatche()
//         addPastHistoryDataToCatche()
////        findNavController().navigate(
////            HistoryCustomFragmentDirections.actionHistoryCustomFragmentToFhirVitalsFragment()
////        )
    }
//    private fun addCovidData(){
//        val immunization = Immunization()
//        val vaccineStatusVal = binding.vStatusText
//        val vaccineTypeVal = binding.vTypeText
//        val doseVal = binding.doseTakenText
//        if(vaccineStatusVal?.text?.isNotEmpty()!! && vaccineTypeVal?.text?.isNotEmpty()!! && doseVal.text.isNotEmpty())
//        {
//            val vaccId = findKeyByValue(vaccineTypeMap,vaccineTypeVal.text.toString())
//            val doseId = findKeyByValue(doseTypeMap,doseVal.text.toString())
//
//            immunization.status = if (vaccineStatusVal.text.toString() == "Yes") Immunization.ImmunizationStatus.COMPLETED else Immunization.ImmunizationStatus.NOTDONE
//            var bool = immunization.status==Immunization.ImmunizationStatus.COMPLETED
//            immunization.occurrence = StringType("Unknown")
//            val vaccineCoding = Coding()
//            vaccineCoding.system = "http://hl7.org/fhir/sid/cvx"
//            vaccineCoding.code = "213"
//            if(bool) {
//                val vaccineCode = CodeableConcept()
//                vaccineCode.coding = listOf(vaccineCoding)
//                vaccineCode.text = vaccId.toString()
//                immunization.vaccineCode = vaccineCode
//
//                val protocolApplied = Immunization.ImmunizationProtocolAppliedComponent()
//                protocolApplied.doseNumber = StringType(doseId.toString())
//                immunization.protocolApplied = listOf(protocolApplied)
//            }
//            val patientReference = Reference()
//            patientReference.reference = "Patient/11090786"
//            immunization.patient = patientReference
//
//            addExtensionsToImmunizationResources(immunization)
//            viewModel.saveCovidDetailsInfo(immunization)
//        }
//    }
    private fun <K, V> findKeyByValue(map: Map<K, V>, value: V): K? {
        return map.entries.find { it.value == value }?.key
    }
    private fun addAssociateAilmentsDataToCatche() {
        val count = binding.associatedAilmentsExtra.childCount

        for (i in 0 until count) {
            val childView: View? = binding.associatedAilmentsExtra?.getChildAt(i)
            val associateAilmentVal = childView?.findViewById<AutoCompleteTextView>(R.id.aaText)?.text.toString()
            val durationVal = childView?.findViewById<TextInputEditText>(R.id.inputDuration)?.text.toString()
            val unitDurationVal = childView?.findViewById<AutoCompleteTextView>(R.id.dropdownDurUnit)?.text.toString()
            val familyVal = childView?.findViewById<TextView>(R.id.selectF)?.text.toString()
            val inactiveVal = childView?.findViewById<CheckBox>(R.id.inactiveCheckbox)
            val isChecked = inactiveVal?.isChecked ?: false
            val otherAA =  childView?.findViewById<TextInputEditText>(R.id.otherTextField)?.text.toString()
            val associateAilmentsHistory = AssociateAilmentsHistory(
                associateAilmentsId = "56",
                associateAilment = associateAilmentVal,
                associateAilmentOther = otherAA,
                duration = durationVal,
                durationUnit = unitDurationVal,
                familyMembers = familyVal,
                inactiveVisit = isChecked
            )
            viewModel.saveAssociateAilmentsHistoryToCache(associateAilmentsHistory)
        }
    }
    private fun addCovidVaccinationDataToCatche() {

        val vaccineStatusVal = binding.vStatusText.text.toString()
        val vaccineTypeVal = binding.vTypeText.text.toString()
        val doseVal = binding.doseTakenText.text.toString()
            val covidVaccinationStatusHistory = CovidVaccinationStatusHistory(
                covidVaccinationStatusHistoryId = "29",
                vaccinationStatus = vaccineStatusVal,
                vaccineType = vaccineTypeVal,
                doseTaken = doseVal
            )
            viewModel.saveCovidVaccinationStatusHistoryToCache(covidVaccinationStatusHistory)
    }
    private fun addPastHistoryDataToCatche() {
        val illnessCount = binding.pastIllnessExtra.childCount
        val surgeryCount = binding.pastSurgeryExtra.childCount

        for (i in 0 until illnessCount) {
            val childView: View? = binding.pastIllnessExtra?.getChildAt(i)
            val illnessVal = childView?.findViewById<AutoCompleteTextView>(R.id.illnessText)?.text.toString()
            val durationVal = childView?.findViewById<TextInputEditText>(R.id.inputDuration)?.text.toString()
            val unitDurationVal = childView?.findViewById<AutoCompleteTextView>(R.id.dropdownDurUnit)?.text.toString()

            val pastIllnessHistory = PastIllnessHistory(
                pastIllnessHistoryId = "29"+i,
                pastIllnessHistory = illnessVal,
                duration = durationVal,
                durationUnit = unitDurationVal
            )
            viewModel.savePastIllnessHistoryToCache(pastIllnessHistory)
        }
        for (i in 0 until surgeryCount) {
            val childView: View? = binding.pastSurgeryExtra?.getChildAt(i)
            val surgeryVal = childView?.findViewById<AutoCompleteTextView>(R.id.surgeryText)?.text.toString()
            val durationVal = childView?.findViewById<TextInputEditText>(R.id.inputDuration)?.text.toString()
            val unitDurationVal = childView?.findViewById<AutoCompleteTextView>(R.id.dropdownDurUnit)?.text.toString()

            val pastSurgeryHistory = PastSurgeryHistory(
                pastSurgeryHistoryId = "29"+i,
                pastSurgeryHistory = surgeryVal,
                duration = durationVal,
                durationUnit = unitDurationVal
            )
            viewModel.savePastSurgeryHistoryToCache(pastSurgeryHistory)
        }
    }
    private fun addMedicationDataToCatche() {
        val count = binding.medicationExtra.childCount

        for (i in 0 until count) {
            val childView: View? = binding.medicationExtra?.getChildAt(i)
            val currentMVal = childView?.findViewById<TextInputEditText>(R.id.currentMText)?.text.toString()
            val durationVal = childView?.findViewById<TextInputEditText>(R.id.inputDuration)?.text.toString()
            val unitDurationVal = childView?.findViewById<AutoCompleteTextView>(R.id.dropdownDurUnit)?.text.toString()

            val medicationHistory = MedicationHistory(
                medicationHistoryId = "29",
                currentMedication = currentMVal,
                duration = durationVal,
                durationUnit = unitDurationVal
            )
            viewModel.saveMedicationHistoryToCache(medicationHistory)
        }
    }
    private fun addTobAndAlcDataToCatche()  {
            val alcoholVal = binding.alcoholText.text.toString()
            val tobaccoVal = binding.tobaccoText.text.toString()
            val tobaccoAlcoholHistory = TobaccoAlcoholHistory(
               tobaccoAndAlcoholId = "2",
                tobacco = tobaccoVal,
                alcohol = alcoholVal
            )
            viewModel.saveTobAndAlcHistoryToCache(tobaccoAlcoholHistory)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
