package org.piramalswasthya.cho.ui.commons.fhir_patient_vitals

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.ResourceType
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentVitalsCustomBinding
import org.piramalswasthya.cho.fhir_utils.FhirExtension
import org.piramalswasthya.cho.fhir_utils.extension_names.benFlowID
import org.piramalswasthya.cho.fhir_utils.extension_names.beneficiaryID
import org.piramalswasthya.cho.fhir_utils.extension_names.beneficiaryRegID
import org.piramalswasthya.cho.fhir_utils.extension_names.modifiedBy
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.model.ChiefComplaintValues
import org.piramalswasthya.cho.model.MasterDb
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.model.VisitMasterDb
import org.piramalswasthya.cho.model.VitalsMasterDb
import org.piramalswasthya.cho.ui.commons.FhirFragmentService
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.utils.nullIfEmpty
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.utils.generateUuid
import java.math.BigDecimal
import javax.inject.Inject
import kotlin.math.pow

@AndroidEntryPoint
class FhirVitalsFragment : Fragment(R.layout.fragment_vitals_custom), FhirFragmentService, NavigationAdapter {

    private var _binding: FragmentVitalsCustomBinding? = null

    private val binding: FragmentVitalsCustomBinding
        get() {
            return _binding!!
        }

    override val viewModel: FhirVitalsViewModel by viewModels()

    override var fragment: Fragment = this;
    @Inject
    lateinit var preferenceDao: PreferenceDao
    override var fragmentContainerId = 0;
    private var userInfo: UserCache? = null
    private var isNull:Boolean = true

    override val jsonFile : String = "vitals-page.json"
    var observation = Observation()
    private val observationExtension: FhirExtension = FhirExtension(ResourceType.Observation)

    var heightValue:String?=null
    var weightValue :String?=null
    var bmiValue :String?=null
    var waistCircumferenceValue :String?=null
    var temperatureValue :String?=null
    var pulseRateValue :String?=null
    var spo2Value :String?=null
    var bpSystolicValue :String?=null
    var bpDiastolicValue :String?=null
    var respiratoryValue :String?=null
    var rbsValue :String?=null
    private val bundle = Bundle()

    private var masterDb: MasterDb? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVitalsCustomBinding.inflate(layoutInflater, container, false)
        binding.inputWeight.addTextChangedListener(textWatcher)
        binding.inputHeight.addTextChangedListener(textWatcher)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        masterDb = arguments?.getSerializable("MasterDb") as? MasterDb
        Log.d("aryan","category --  ${masterDb?.visitMasterDb?.subCategory}")
        viewModel.getLoggedInUserDetails()
        viewModel.boolCall.observe(viewLifecycleOwner){
            if(it){
                userInfo = viewModel.loggedInUser
                viewModel.resetBool()
            }
        }
    }
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            calculateAndDisplayBMI()
        }

        override fun afterTextChanged(s: Editable?) {}
    }

   private fun extractFormValues(){
        heightValue = binding.inputHeight.text?.toString()?.trim()
        weightValue = binding.inputWeight.text?.toString()?.trim()
        bmiValue = binding.inputBmi.text?.toString()?.trim()
        waistCircumferenceValue = binding.inputWaistCircum.text?.toString()?.trim()
        temperatureValue = binding.inputTemperature.text?.toString()?.trim()
        pulseRateValue = binding.inputPulseRate.text?.toString()?.trim()
        spo2Value = binding.inputSpo2.text?.toString()?.trim()
        bpSystolicValue = binding.inputBpSystolic.text?.toString()?.trim()
        bpDiastolicValue = binding.inputBpDiastolic.text?.toString()?.trim()
        respiratoryValue = binding.inputRespiratoryPerMin.text?.toString()?.trim()
        rbsValue = binding.inputRbs.text?.toString()?.trim()
    }

    private fun addVitalsDataToCache(lastVisitNo: Int){
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
            patientID = masterDb!!.patientId,
            benVisitNo = lastVisitNo + 1,
        )
        viewModel.savePatientVitalInfoToCache(patientVitals)
    }
    private fun addVisitRecordDataToCache(lastVisitNo: Int){
        val visitDB = VisitDB(
            visitId = generateUuid(),
            category = masterDb?.visitMasterDb?.category.nullIfEmpty(),
            reasonForVisit = masterDb?.visitMasterDb?.reason.nullIfEmpty() ,
            subCategory = masterDb?.visitMasterDb?.subCategory.nullIfEmpty(),
            patientID = masterDb!!.patientId,
            benVisitNo = lastVisitNo + 1,
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
                patientID = masterDb!!.patientId,
                beneficiaryID = null,
                beneficiaryRegID=null,
                benVisitNo = lastVisitNo + 1,
                benFlowID=null
            )
            viewModel.saveChiefComplaintDbToCatche(chiefC)
        }
    }

    private fun addPatientVisitInfoSyncToCache(lastVisitNo: Int){
        val patientVisitInfoSync = PatientVisitInfoSync(
            patientID = masterDb!!.patientId,
            benVisitNo = lastVisitNo + 1,
            createNewBenFlow = true,
        )
        viewModel.savePatientVisitInfoSync(patientVisitInfoSync)
    }

    private fun createObservationResource(){
        //Code
        var observationCode = Coding()
        observationCode.system = getString(R.string.lonic_url)
        observationCode.code = ""
        observationCode.display = getString(R.string.vital_signs_panel)
        observation.code = CodeableConcept().addCoding(observationCode)
        observation.code.text = getString(R.string.vital_signs_panel_code_text)

        //Category
        var observationCategory = Coding()
        observationCategory.system =getString(R.string.observation_category_url)
        observationCategory.code = getString(R.string.vital_signs_code)
        observationCategory.display = getString(R.string.vital_signs)
        val categoryCodeableConcept  = CodeableConcept()
        categoryCodeableConcept.addCoding(observationCategory)
        categoryCodeableConcept.text = getString(R.string.vital_signs)
        observation.category.add(categoryCodeableConcept)

        //Components
        if(!heightValue.isNullOrBlank()) {
            val heightComponent = Observation.ObservationComponentComponent()
            heightComponent.code.text = getString(R.string.height_cm_text)
            heightComponent.valueQuantity.value = BigDecimal(heightValue.toString())
            observation.component.add(heightComponent)
            isNull = false
        }
        if(!weightValue.isNullOrBlank()) {
            val weightComponent = Observation.ObservationComponentComponent()
            weightComponent.code.text = getString(R.string.weight_kg)
            weightComponent.valueQuantity.value = BigDecimal(weightValue.toString())
            observation.component.add(weightComponent)
            isNull = false
        }
        if(!bmiValue.isNullOrBlank()) {
            val bmiComponent = Observation.ObservationComponentComponent()
            bmiComponent.code.text = getString(R.string.bmi_text)
            bmiComponent.valueQuantity.value = BigDecimal(bmiValue.toString())
            observation.component.add(bmiComponent)
            isNull = false
        }
        if(!waistCircumferenceValue.isNullOrBlank()) {
            val waistCircumferenceComponent = Observation.ObservationComponentComponent()
            waistCircumferenceComponent.code.text = getString(R.string.waistcircumference_cm)
            waistCircumferenceComponent.valueQuantity.value = BigDecimal(waistCircumferenceValue.toString())
            observation.component.add(waistCircumferenceComponent)
            isNull = false
        }
        if(!temperatureValue.isNullOrBlank()) {
            val tempComponent = Observation.ObservationComponentComponent()
            tempComponent.code.text = getString(R.string.temperature)
            tempComponent.valueQuantity.value = BigDecimal(temperatureValue.toString())
            observation.component.add(tempComponent)
            isNull = false
        }
        if(!pulseRateValue.isNullOrBlank()) {
            val pulseRateComponent = Observation.ObservationComponentComponent()
            pulseRateComponent.code.text = getString(R.string.pulserate)
            pulseRateComponent.valueQuantity.value = BigDecimal(pulseRateValue.toString())
            observation.component.add(pulseRateComponent)
            isNull = false
        }
        if(!spo2Value.isNullOrBlank()) {
            val spo2Component = Observation.ObservationComponentComponent()
            spo2Component.code.text = getString(R.string.spo2_text)
            spo2Component.valueQuantity.value = BigDecimal(spo2Value.toString())
            observation.component.add(spo2Component)
            isNull = false
        }
        if(!bpSystolicValue.isNullOrBlank()) {
            val bpSystolicComponent = Observation.ObservationComponentComponent()
            bpSystolicComponent.code.text = getString(R.string.systolicbp_1streading)
            bpSystolicComponent.valueQuantity.value = BigDecimal(bpSystolicValue.toString())
            observation.component.add(bpSystolicComponent)
            isNull = false
        }
        if(!bpDiastolicValue.isNullOrBlank()) {
            val bpDiastolicComponent = Observation.ObservationComponentComponent()
            bpDiastolicComponent.code.text = getString(R.string.diastolicbp_1streading)
            bpDiastolicComponent.valueQuantity.value = BigDecimal(bpDiastolicValue.toString())
            observation.component.add(bpDiastolicComponent)
            isNull = false
        }
        if(!respiratoryValue.isNullOrBlank()) {
            val respiratoryComponent = Observation.ObservationComponentComponent()
            respiratoryComponent.code.text = getString(R.string.respiratoryrate)
            respiratoryComponent.valueQuantity.value = BigDecimal(respiratoryValue.toString())
            observation.component.add(respiratoryComponent)
            isNull = false
        }
        if(!rbsValue.isNullOrBlank()) {
            val rbsComponent = Observation.ObservationComponentComponent()
            rbsComponent.code.text = getString(R.string.rbstestresult)
            rbsComponent.valueQuantity.value = BigDecimal(rbsValue.toString())
            observation.component.add(rbsComponent)
            isNull = false
        }
        if(!isNull) {
            observation.status = Observation.ObservationStatus.FINAL //Status
            addExtensionsToObservationResources(observation)         //Extensions
        }
    }
    private fun calculateAndDisplayBMI() {
        val heightValue: Float? = binding.inputHeight.text.toString().trim().toFloatOrNull()
        val weightValue : Float? = binding.inputWeight.text.toString().trim().toFloatOrNull()

        if (weightValue != null && heightValue != null && heightValue > 0 &&  weightValue > 0) {
            val bmi = weightValue / (heightValue / 100).pow(2)
            val formattedBMI = "%.2f".format(bmi)
    //            var status : String
            binding.inputBmi.text = Editable.Factory.getInstance().newEditable(formattedBMI)
            if(bmi > 25 && bmi < 30){
                binding.bmiCategory.isVisible = true
                binding.bmiCategory.text = getString(R.string.overweight_txt)
    //                status = getString(R.string.overweight_txt)
                binding.bmiCategory.setTextColor(resources.getColor(R.color.red))
                binding.inputBmi.setTextColor(resources.getColor(R.color.red))
            }
            else if (bmi > 30){
                binding.bmiCategory.isVisible = true
                binding.bmiCategory.text = getString(R.string.obese_txt)
    //                status = getString(R.string.obese_txt)
                binding.bmiCategory.setTextColor(resources.getColor(R.color.red))
                binding.inputBmi.setTextColor(resources.getColor(R.color.red))
            }
            else{
                binding.bmiCategory.isVisible = true
    //                status = getString(R.string.normal_txt)
                binding.bmiCategory.text = getString(R.string.normal_txt)
                binding.bmiCategory.setTextColor(resources.getColor(R.color.green))
                binding.inputBmi.setTextColor(resources.getColor(R.color.black))
            }
    //            val bmiText = "$formattedBMI                          Status: $status"
    //
    //            val spannable = SpannableString(bmiText)
    //
    //            // Color status text
    //            val statusStart = bmiText.indexOf("Status:")
    //            spannable.setSpan(ForegroundColorSpan(resources.getColor(R.color.red)), statusStart, bmiText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    //
    //            val indentation = resources.getDimensionPixelSize(R.dimen.bmi_status_indentation) // Define this dimension in resources
    //            spannable.setSpan(LeadingMarginSpan.Standard(0, indentation), statusStart, bmiText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

    //            binding.inputBmi.text = Editable.Factory.getInstance().newEditable(spannable)
        }
        else{
            binding.inputBmi.text = null
            binding.bmiCategory.isVisible = false
        }
    }

    private fun addExtensionsToObservationResources(
        observation: Observation,
    ) {
        if (userInfo != null) {
            observation.addExtension(observationExtension.getExtenstion(
                observationExtension.getUrl(modifiedBy),
                observationExtension.getStringType(userInfo!!.userName)))
        }

        observation.addExtension(observationExtension.getExtenstion(
            observationExtension.getUrl(beneficiaryID),
            observationExtension.getStringType(""))) //add beneficiaryID

        observation.addExtension(observationExtension.getExtenstion(
            observationExtension.getUrl(beneficiaryRegID),
            observationExtension.getStringType(""))) //add beneficiaryRegID

        observation.addExtension(observationExtension.getExtenstion(
            observationExtension.getUrl(benFlowID),
            observationExtension.getStringType(""))) //add benFlowID
    }
    override fun getFragmentId(): Int {
        return R.id.fragment_vitals_info;
    }

    override fun onSubmitAction() {
    //        saveEntity()
        navigateNext()
    }

    override fun onCancelAction() {
    //        findNavController().navigate(
    //            FhirVitalsFragmentDirections.actionFhirVitalsFragmentToFhirVisitDetailsFragment()
    //        )
        findNavController().navigateUp()
    }

    override fun navigateNext() {
        if (preferenceDao.isUserNurseAndDoctorOrMo()){
            extractFormValues()
            createObservationResource()
            if (!isNull) {
                viewModel.saveObservationResource(observation)
                isNull = true
            }
            setVitalsMasterData()
            findNavController().navigate(
                R.id.action_customVitalsFragment_to_caseRecordCustom, bundle
            )
        }else{
            CoroutineScope(Dispatchers.IO).launch {
//                val hasUnSyncedNurseData = viewModel.hasUnSyncedNurseData(masterDb!!.patientId)
//                if(hasUnSyncedNurseData){
//                    Toast.makeText(
//                        requireContext(),
//                        resources.getString(R.string.unsyncedNurseData),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//                else{
                    val lastVisitNo = viewModel.getLastVisitNo(masterDb!!.patientId)
                    extractFormValues()
                    setVitalsMasterData()
                    addVisitRecordDataToCache(lastVisitNo)
                    addVitalsDataToCache(lastVisitNo)
                    addPatientVisitInfoSyncToCache(lastVisitNo)
                    setNurseComplete()
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.vitals_information_is_saved),
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(context, HomeActivity::class.java)
                    startActivity(intent)
//                }
            }

        }
    }
    private fun setNurseComplete() {
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.setNurseCompleted(masterDb!!.patientId)
        }
    }

    private fun setVitalsMasterData(){
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


}