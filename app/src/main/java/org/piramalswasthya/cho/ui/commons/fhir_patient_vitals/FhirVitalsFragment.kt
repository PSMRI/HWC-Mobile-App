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
import org.piramalswasthya.cho.model.UserDomain
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.model.VitalsMasterDb
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.utils.generateUuid
import org.piramalswasthya.cho.utils.nullIfEmpty
import org.piramalswasthya.cho.work.WorkerUtils
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

    @Inject
    lateinit var userRepo: UserRepo

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


    fun saveNurseData(benVisitNo: Int, createNewBenflow: Boolean, user: UserDomain?){

        val visitDB = VisitDB(
            visitId = generateUuid(),
            category = masterDb?.visitMasterDb?.category.nullIfEmpty(),
            reasonForVisit = masterDb?.visitMasterDb?.reason.nullIfEmpty() ,
            subCategory = masterDb?.visitMasterDb?.subCategory.nullIfEmpty(),
            patientID = masterDb!!.patientId.toString(),
            benVisitNo = benVisitNo,
            benVisitDate =  SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()),
            createdBy = user?.userName
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
            doctorFlag = 1,
            visitDate = Date(),
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
        if (preferenceDao.isUserCHO()){
            extractFormValues()
            if (!isNull) {
//                viewModel.saveObservationResource(observation)
                isNull = true
            }
            setVitalsMasterData()
            findNavController().navigate(
                R.id.action_customVitalsFragment_to_caseRecordCustom, bundle
            )
        }else{
            CoroutineScope(Dispatchers.Main).launch {

                var benVisitNo = 0;
                var createNewBenflow = false;
                viewModel.getLastVisitInfoSync(masterDb!!.patientId.toString()).let {
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
//                addVisitRecordDataToCache(benVisitNo)
//                addVitalsDataToCache(benVisitNo)
//                addPatientVisitInfoSyncToCache(benVisitNo, createNewBenflow)

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