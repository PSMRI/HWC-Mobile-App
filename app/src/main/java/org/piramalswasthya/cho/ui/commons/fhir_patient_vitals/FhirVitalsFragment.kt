package org.piramalswasthya.cho.ui.commons.fhir_patient_vitals

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentVitalsCustomBinding
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.model.MasterDb
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.model.VitalsMasterDb
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.utils.generateUuid
import org.piramalswasthya.cho.utils.nullIfEmpty
import org.piramalswasthya.cho.utils.setBoxColor
import org.piramalswasthya.cho.work.WorkerUtils
import timber.log.Timber
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject
import kotlin.math.pow

@AndroidEntryPoint
class FhirVitalsFragment : Fragment(R.layout.fragment_vitals_custom), NavigationAdapter {

    private var _binding: FragmentVitalsCustomBinding? = null

    private val binding: FragmentVitalsCustomBinding
        get() {
            return _binding!!
        }

    val viewModel: FhirVitalsViewModel by viewModels()

    var fragment: Fragment = this;
    @Inject
    lateinit var preferenceDao: PreferenceDao
    var fragmentContainerId = 0;
    private var userInfo: UserCache? = null
    private var isNull:Boolean = true

    val jsonFile : String = "vitals-page.json"

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

        masterDb = arguments?.getSerializable("MasterDb") as? MasterDb
        Log.d("aryan","category --  ${masterDb?.visitMasterDb?.subCategory}")
        viewModel.getLoggedInUserDetails()
        viewModel.boolCall.observe(viewLifecycleOwner){
            if(it){
                userInfo = viewModel.loggedInUser
                viewModel.resetBool()
            }
        }
        if (masterDb?.visitMasterDb?.chiefComplaint?.any { it.chiefComplaint?.equals("fever", ignoreCase = true) == true } == true) {
            binding.temperatureEditTxt.helperText = "Temprature Required"
        } else {
            binding.temperatureEditTxt.helperText = null
        }

        binding.bpSystolicEditTxt.helperText=null
        binding.bpDiastolicEditTxt.helperText=null
        binding.pulseRateEditTxt.helperText=null
        binding.spo2EditTxt.helperText=null
        binding.respiratoryEditTxt.helperText=null
        binding.rbsEditTxt.helperText=null
        binding.heightEditTxt.helperText=null
        binding.weightEditTxt.helperText=null
        textwatchers()
    }

    private fun textwatchers() {
        binding.inputTemperature.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isTemp = s?.isNotEmpty() == true
                if(isTemp){
                    validateTemperature(s.toString())
                }else{
                    binding.temperatureEditTxt.helperText=null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.inputBpSystolic.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isTemp = s?.isNotEmpty() == true
                if(isTemp){
                    validateBPSystolic(s.toString())
                }else{
                    binding.bpSystolicEditTxt.helperText=null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.inputBpDiastolic.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isTemp = s?.isNotEmpty() == true
                if(isTemp){
                    validateBPD(s.toString())
                }else{
                    binding.bpDiastolicEditTxt.helperText=null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.inputPulseRate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isTemp = s?.isNotEmpty() == true
                if(isTemp){
                    validatePulse(s.toString())
                }else{
                    binding.pulseRateEditTxt.helperText=null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.inputSpo2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isTemp = s?.isNotEmpty() == true
                if(isTemp){
                    validateSpo2(s.toString())
                }else{
                    binding.spo2EditTxt.helperText=null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.inputRespiratoryPerMin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isTemp = s?.isNotEmpty() == true
                if(isTemp){
                    validateResp(s.toString())
                }else{
                    binding.respiratoryEditTxt.helperText=null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.inputRBS.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isTemp = s?.isNotEmpty() == true
                if(isTemp){
                    validateRBS(s.toString())
                }else{
                    binding.rbsEditTxt.helperText=null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.inputHeight.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isTemp = s?.isNotEmpty() == true
                if(isTemp){
                    validateHeight(s.toString())
                }else{
                    binding.heightEditTxt.helperText=null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.inputWeight.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isTemp = s?.isNotEmpty() == true
                if(isTemp){
                    validateWeight(s.toString())
                }else{
                    binding.weightEditTxt.helperText=null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

    }
    private fun validateBPSystolic(bpSystolic: String) {
        val isValid = bpSystolic.matches(Regex("^\\d{2,3}$")) &&
                bpSystolic.toInt() in 50..300

        if (isValid) {
            binding.bpSystolicEditTxt.helperText = null
        } else {
            binding.bpSystolicEditTxt.helperText =
                "Please enter value between 50 and 300."
        }
    }

    private fun validateBPD(bpD: String) {
        val isValid = bpD.matches(Regex("^\\d{2,3}$")) &&
                bpD.toInt() in 30..200
        if (isValid) {
            binding.bpDiastolicEditTxt.helperText = null
        } else {
            binding.bpDiastolicEditTxt.helperText =
                "Please enter value between 30 and 200."
        }
    }

    private fun validateTemperature(temperature: String) {
        val isValid = temperature.matches(Regex("^\\d{2,3}(\\.\\d{1,2})?$"))
        if (isValid) {
            binding.temperatureEditTxt.helperText = null
        } else {
            binding.temperatureEditTxt.helperText =
                "Invalid temperature."
        }
    }
    private fun validateHeight(hei: String) {
        val isValid = hei.matches(Regex("^\\d{2,3}(\\.\\d{1,2})?$"))
        if (isValid) {
            binding.heightEditTxt.helperText = null
        } else {
            binding.heightEditTxt.helperText =
                "Invalid Height."
        }
    }
    private fun validateWeight(w: String) {
        val isValid = w.matches(Regex("^\\d{2,3}(\\.\\d{1,2})?$"))
        if (isValid) {
            binding.weightEditTxt.helperText = null
        } else {
            binding.weightEditTxt.helperText =
                "Invalid Weight."
        }
    }
    private fun validatePulse(pul: String) {
        val isValid = pul.matches(Regex("^\\d{2,3}$"))
        if (isValid) {
            binding.pulseRateEditTxt.helperText = null
        } else {
            binding.pulseRateEditTxt.helperText =
                "Invalid Pulse Rate."
        }
    }
    private fun validateRBS(rbs: String) {
        val isValid = rbs.matches(Regex("^\\d{2,3}$"))
        if (isValid) {
            binding.rbsEditTxt.helperText = null
        } else {
            binding.rbsEditTxt.helperText =
                "Invalid RBS."
        }
    }
    private fun validateSpo2(spo2: String) {
        val isValid = spo2.matches(Regex("^\\d+$")) &&
                spo2.toInt() in 30..100

        if (isValid) {
            binding.spo2EditTxt.helperText = null
        } else {
            binding.spo2EditTxt.helperText =
                "Please enter a numeric value between 30 and 100."
        }
    }


    private fun validateResp(resp: String) {
        val isValid = resp.matches(Regex("^\\d+$")) &&
                resp.toInt() in 10..40

        if (isValid) {
            binding.respiratoryEditTxt.helperText = null
        } else {
            binding.respiratoryEditTxt.helperText =
                "Please enter a numeric value between 10 and 40."
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

        val visitDB = VisitDB(
            visitId = generateUuid(),
            category = masterDb?.visitMasterDb?.category.nullIfEmpty(),
            reasonForVisit = masterDb?.visitMasterDb?.reason.nullIfEmpty() ,
            subCategory = masterDb?.visitMasterDb?.subCategory.nullIfEmpty(),
            patientID = masterDb!!.patientId.toString(),
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
                patientID = masterDb!!.patientId.toString(),
                benVisitNo = benVisitNo,
                benFlowID=null
            )
            chiefComplaints.add(chiefC)
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
            patientID = masterDb!!.patientId.toString(),
            benVisitNo = benVisitNo,
        )

        val patientVisitInfoSync = PatientVisitInfoSync(
            patientID = masterDb!!.patientId.toString(),
            benVisitNo = benVisitNo,
            createNewBenFlow = createNewBenflow,
            nurseDataSynced = SyncState.UNSYNCED,
            doctorDataSynced = SyncState.SYNCED,
            nurseFlag = 9,
            doctorFlag = 1
        )

        viewModel.saveNurseDataToDb(visitDB, chiefComplaints, patientVitals, patientVisitInfoSync)

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

    fun navigateNext() {
        if (preferenceDao.isUserNurseOrCHOAndDoctorOrMo() && !preferenceDao.isCHO()){
            extractFormValues()
            if (!isNull) {
//                viewModel.saveObservationResource(observation)
                isNull = true
            }
            if(isHelperTrue()){
                setVitalsMasterData()
                findNavController().navigate(
                    R.id.action_customVitalsFragment_to_caseRecordCustom, bundle
                )
            }
        }else{
            CoroutineScope(Dispatchers.Main).launch {

                var benVisitNo = 0;
                var createNewBenflow = false;
                viewModel.getLastVisitInfoSync(masterDb!!.patientId.toString()).let {
                    if (it == null) {
                        benVisitNo = 1;
                    } else if (it.nurseFlag == 1) {
                        benVisitNo = it.benVisitNo
                    } else {
                        benVisitNo = it.benVisitNo + 1
                        createNewBenflow = true;
                    }
                }
                if (isHelperTrue()) {
                    extractFormValues()
                    setVitalsMasterData()
//                addVisitRecordDataToCache(benVisitNo)
//                addVitalsDataToCache(benVisitNo)
//                addPatientVisitInfoSyncToCache(benVisitNo, createNewBenflow)
                    saveNurseData(benVisitNo, createNewBenflow)

                    viewModel.isDataSaved.observe(viewLifecycleOwner) {
                        when (it!!) {
                            true -> {
                                WorkerUtils.triggerAmritSyncWorker(requireContext())
                                val intent = Intent(context, HomeActivity::class.java)
                                startActivity(intent)
                                requireActivity().finish()
                            }

                            else -> {
//                            requireActivity().runOnUiThread {
//                                Toast.makeText(requireContext(), resources.getString(R.string.something_wend_wong), Toast.LENGTH_SHORT).show()
//                            }
                            }
                        }
                    }

                }
            }

        }
    }

    private fun isHelperTrue(): Boolean {
        if(binding.temperatureEditTxt.helperText==null && binding.bpDiastolicEditTxt.helperText==null&& binding.bpSystolicEditTxt.helperText==null
            && binding.respiratoryEditTxt.helperText==null&& binding.pulseRateEditTxt.helperText==null&& binding.spo2EditTxt.helperText==null
            && binding.rbsEditTxt.helperText==null&& binding.heightEditTxt.helperText==null&& binding.weightEditTxt.helperText==null)
            return true
        return false
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