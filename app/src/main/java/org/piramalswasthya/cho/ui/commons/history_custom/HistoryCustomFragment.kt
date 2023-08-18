package org.piramalswasthya.cho.ui.commons.history_custom

import org.piramalswasthya.cho.ui.commons.history_custom.dialog.IllnessDialogFragment
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import org.piramalswasthya.cho.fhir_utils.extension_names.createdBy
import org.piramalswasthya.cho.fhir_utils.extension_names.parkingPlaceID
import org.piramalswasthya.cho.fhir_utils.extension_names.providerServiceMapId
import org.piramalswasthya.cho.fhir_utils.extension_names.vanID
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ca.uhn.fhir.context.RuntimeSearchParam
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.Dosage
import org.hl7.fhir.r4.model.Duration
import org.hl7.fhir.r4.model.MedicationRequest
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.ResourceType
import org.hl7.fhir.r4.model.StringType
import org.hl7.fhir.r4.model.Timing
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentHistoryCustomBinding
import org.piramalswasthya.cho.fhir_utils.FhirExtension
import org.piramalswasthya.cho.fhir_utils.extension_names.createdBy
import org.piramalswasthya.cho.fhir_utils.extension_names.duration
import org.piramalswasthya.cho.fhir_utils.extension_names.parkingPlaceID
import org.piramalswasthya.cho.fhir_utils.extension_names.providerServiceMapId
import org.piramalswasthya.cho.fhir_utils.extension_names.vanID
import org.piramalswasthya.cho.model.ChiefComplaintValues
import org.piramalswasthya.cho.model.PastSurgeryValues
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.model.pastIllnessValues
import org.piramalswasthya.cho.ui.HistoryFieldsInterface
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments.AAFragments
import org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments.AlcoholFragment
import org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments.AllergyFragment
import org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments.IllnessFieldsFragment
import org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments.MedicationFragment
import org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments.PastSurgeryFragment
import org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments.TobaccoFragment
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
    private val vaccType = arrayOf(
                "Covaxine",
                "Covishield",
                "Sputnik",
                "Corbevax"
    )

    private val doseTaken = arrayOf(
        "1st Dose",
        "2st Dose",
        "Precautionary/Booster Dose"
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
    var addCountTob:Int =0
    var deleteCountTob:Int=0
    var addCountAlc:Int =0
    var deleteCountAlc:Int=0
    var addCountAllg:Int =0
    var deleteCountAllg:Int=0
    var illnessTag = mutableListOf<String>()
    var surgeryTag = mutableListOf<String>()
    var aaTag = mutableListOf<String>()
    var mTag = mutableListOf<String>()
    var tobTag = mutableListOf<String>()
    var alcTag = mutableListOf<String>()
    var allgTag = mutableListOf<String>()
    private var illnessMap = emptyMap<Int,String>()
    private var surgeryMap = emptyMap<Int,String>()
    private val observationExtension: FhirExtension = FhirExtension(ResourceType.Observation)

    private val viewModel:HistoryCustomViewModel by viewModels()
    private lateinit var dropdownAgeG: AutoCompleteTextView
    private lateinit var dropdownVS: AutoCompleteTextView
    private lateinit var dropdownVT: AutoCompleteTextView
    private lateinit var dropdownDT: AutoCompleteTextView
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
        addCountTob=0
        deleteCountTob=0
        addCountAlc=0
        deleteCountAlc=0
        addCountAllg=0
        deleteCountAllg=0
        _binding = FragmentHistoryCustomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dropdownAgeG = binding.ageGrText
        dropdownVS = binding.vStatusText
        dropdownVT = binding.vTypeText
        dropdownDT = binding.doseTakenText
        val ageAAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, AgeGroup)
        dropdownAgeG.setAdapter(ageAAdapter)
        val vacAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, vaccinationStatus)
        dropdownVS.setAdapter(vacAdapter)
        val vacTAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, vaccType)
        dropdownVT.setAdapter(vacTAdapter)
        val doseAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, doseTaken)
        dropdownDT.setAdapter(doseAdapter)
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
        addTobFields(addCountTob)
        addAlcFields(addCountAlc)
        addAllgFields(addCountAllg)
    }
    private fun addAlcFields(count:Int){
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction :FragmentTransaction = fragmentManager.beginTransaction()
        val aFields = AlcoholFragment()
        val tag = "Extra Alc$count"
        aFields.setFragmentTag(tag)
        aFields.setListener(this)
        fragmentTransaction.add(binding.personalAExtra.id,aFields,tag)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
        alcTag.add(tag)
        addCountAlc+=1
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
    private fun addTobFields(count:Int){
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction :FragmentTransaction = fragmentManager.beginTransaction()
        val mFields = TobaccoFragment()
        val tag = "Extra tob$count"
        mFields.setFragmentTag(tag)
        mFields.setListener(this)
        fragmentTransaction.add(binding.personalTExtra.id,mFields,tag)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
        tobTag.add(tag)
        addCountTob+=1
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
    private fun deleteTFields(tag: String){
        val fragmentManager : FragmentManager = requireActivity().supportFragmentManager
        val fragmentToDelete = fragmentManager.findFragmentByTag(tag)
        if (fragmentToDelete != null) {
            fragmentManager.beginTransaction().remove(fragmentToDelete).commit()
            tobTag.remove(tag)
            deleteCountTob += 1
        }
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
    private fun deleteAlcFields(tag: String){
        val fragmentManager : FragmentManager = requireActivity().supportFragmentManager
        val fragmentToDelete = fragmentManager.findFragmentByTag(tag)
        if (fragmentToDelete != null) {
            fragmentManager.beginTransaction().remove(fragmentToDelete).commit()
            alcTag.remove(tag)
            deleteCountAlc += 1
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

    override fun onDeleteButtonClickedAlcohol(fragmentTag: String) {
        if(addCountAlc - 1 > deleteCountAlc) deleteAlcFields(fragmentTag)
    }

    override fun onAddButtonClickedAlg(fragmentTag: String) {
        addAllgFields(addCountAllg)
    }

    override fun onDeleteButtonClickedAlg(fragmentTag: String) {
        if(addCountAllg - 1 > deleteCountAllg) deleteAllgFields(fragmentTag)
    }

    override fun onAddButtonClickedAlcohol(fragmentTag: String) {
        addAlcFields(addCountAlc)
    }

    override fun onDeleteButtonClickedTobacco(fragmentTag: String) {
        if(addCountTob - 1 > deleteCountTob) deleteTFields(fragmentTag)
    }

    override fun onAddButtonClickedTobacco(fragmentTag: String) {
        addTobFields(addCountTob)
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
        addPastIllnessAndSurgeryData()
        addMedicationData()
        findNavController().navigate(
            HistoryCustomFragmentDirections.actionHistoryCustomFragmentToFhirVitalsFragment()
        )
    }
    private fun <K, V> findKeyByValue(map: Map<K, V>, value: V): K? {
        return map.entries.find { it.value == value }?.key
    }
    private fun addMedicationData() {
        val medicationRequest = MedicationRequest()

        val count = binding.medicationExtra.childCount
        val dosageInstructions = mutableListOf<Dosage>()

        for (i in 0 until count) {
            val childView: View? = binding.medicationExtra?.getChildAt(i)
            val currentMVal = childView?.findViewById<TextInputEditText>(R.id.currentMText)?.text.toString()
            val durationVal = childView?.findViewById<TextInputEditText>(R.id.inputDuration)?.text.toString().toBigDecimalOrNull()
            val unitDurationVal = childView?.findViewById<AutoCompleteTextView>(R.id.dropdownDurUnit)?.text.toString()

            if (durationVal != null) {
                val dosageInstruction = Dosage().apply {
                    timing = Timing().apply {
                        duration = Duration().apply {
                            value = durationVal
                            unit = unitDurationVal
                        }
                    }
                }
                dosageInstructions.add(dosageInstruction)
            }
        }

        medicationRequest.medicationReference = Reference().apply {
            reference = "Medication/medicationId" // Replace with actual reference
        }

        medicationRequest.dosageInstruction = dosageInstructions

        addExtensionsToMedicationRequestResources(medicationRequest)
        viewModel.saveMedicationDetailsInfo(medicationRequest)
    }



    private fun addPastIllnessAndSurgeryData() {
        val observationResource = Observation()
        // Create category
        val categoryCoding = Coding()
        categoryCoding.system = "http://terminology.hl7.org/CodeSystem/observation-category"
        categoryCoding.code = "social-history"
        categoryCoding.display = "Social History"

        val category = CodeableConcept()
        category.coding = listOf(categoryCoding)
        category.text = "History"

        observationResource.category = listOf(category)

        // Create code
        val codeCoding = Coding()
        codeCoding.system = "http://loinc.org"
        codeCoding.code = "11348-0"
        codeCoding.display = "Past medical history"

        val code = CodeableConcept()
        code.coding = listOf(codeCoding)
        code.text = "Past medical history"

        observationResource.code = code

        // Create components
        val components = mutableListOf<ObservationComponentComponent>()

        val count = binding.pastIllnessExtra.childCount
        for (i in 0 until count) {
            val childView: View? = binding.pastIllnessExtra?.getChildAt(i)
            val illnessVal = childView?.findViewById<AutoCompleteTextView>(R.id.illnessText)
            val durationVal = childView?.findViewById<TextInputEditText>(R.id.inputDuration)
            val unitDurationVal = childView?.findViewById<AutoCompleteTextView>(R.id.dropdownDurUnit)

            if (illnessVal?.text?.isNotEmpty()!! && durationVal?.text?.isNotEmpty()!! && unitDurationVal?.text?.isNotEmpty()!!) {
                val id = findKeyByValue(illnessMap, illnessVal?.text?.toString())
                val pastIllnessCoding = Coding()
                pastIllnessCoding.code = id.toString() // Replace with actual code
                pastIllnessCoding.display = illnessVal.text.toString() // Replace with actual display

                val pastIllnessCode = CodeableConcept()
                pastIllnessCode.coding = listOf(pastIllnessCoding)
                pastIllnessCode.text = "pastIllness"
                observationResource.code = pastIllnessCode

                val illComponent = ObservationComponentComponent()
                illComponent.code = pastIllnessCode
                illComponent.valueQuantity.value =
                    BigDecimal(durationVal.text.toString()) // Set the duration value
                illComponent.valueQuantity.unit =
                    unitDurationVal.text.toString()

                components.add(illComponent)
            }
        }

        val count2 = binding.pastSurgeryExtra.childCount
        for (i in 0 until count2) {
            val childView: View? = binding.pastSurgeryExtra?.getChildAt(i)
            val surgeryVal = childView?.findViewById<AutoCompleteTextView>(R.id.surgeryText)
            val durationVal = childView?.findViewById<TextInputEditText>(R.id.inputDuration)
            val unitDurationVal =
                childView?.findViewById<AutoCompleteTextView>(R.id.dropdownDurUnit)

            if (surgeryVal?.text?.isNotEmpty()!! && durationVal?.text?.isNotEmpty()!! && unitDurationVal?.text?.isNotEmpty()!!) {
                val id = findKeyByValue(surgeryMap, surgeryVal?.text?.toString())
                val pastSurgeryCoding = Coding()
                pastSurgeryCoding.code = id.toString() // Replace with actual code
                pastSurgeryCoding.display = surgeryVal.text.toString() // Replace with actual display

                val pastSurgeryCode = CodeableConcept()
                pastSurgeryCode.coding = listOf(pastSurgeryCoding)
                pastSurgeryCode.text = "pastSurgery"
                observationResource.code = pastSurgeryCode

                val surgComponent = ObservationComponentComponent()
                surgComponent.code = pastSurgeryCode
                surgComponent.valueQuantity.value =
                    BigDecimal(durationVal.text.toString()) // Set the duration value
                surgComponent.valueQuantity.unit =
                    unitDurationVal.text.toString()

                components.add(surgComponent)
            }
        }
        observationResource.component = components
        addExtensionsToObservationResources(observationResource)
        viewModel.saveIllnessORSurgeryDetailsInfo(observationResource)
    }
    private fun addExtensionsToObservationResources(
        observation: Observation,
    ) {
        if (userInfo != null) {
            observation.addExtension( observationExtension.getExtenstion(
                observationExtension.getUrl(vanID),
                observationExtension.getStringType(userInfo!!.vanId.toString())))

            observation.addExtension( observationExtension.getExtenstion(
                observationExtension.getUrl(parkingPlaceID),
                observationExtension.getStringType(userInfo!!.parkingPlaceId.toString())))

            observation.addExtension( observationExtension.getExtenstion(
                observationExtension.getUrl(providerServiceMapId),
                observationExtension.getStringType(userInfo!!.serviceMapId.toString()) ) )

            observation.addExtension( observationExtension.getExtenstion(
                observationExtension.getUrl(createdBy),
                observationExtension.getStringType(userInfo!!.userName) ) )
        }
    }
    private fun addExtensionsToMedicationRequestResources(
        medicationRequest: MedicationRequest,
    ) {
        if (userInfo != null) {
            medicationRequest.addExtension( observationExtension.getExtenstion(
                observationExtension.getUrl(vanID),
                observationExtension.getStringType(userInfo!!.vanId.toString())))

            medicationRequest.addExtension( observationExtension.getExtenstion(
                observationExtension.getUrl(parkingPlaceID),
                observationExtension.getStringType(userInfo!!.parkingPlaceId.toString())))

            medicationRequest.addExtension( observationExtension.getExtenstion(
                observationExtension.getUrl(providerServiceMapId),
                observationExtension.getStringType(userInfo!!.serviceMapId.toString()) ) )

            medicationRequest.addExtension( observationExtension.getExtenstion(
                observationExtension.getUrl(createdBy),
                observationExtension.getStringType(userInfo!!.userName) ) )
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}