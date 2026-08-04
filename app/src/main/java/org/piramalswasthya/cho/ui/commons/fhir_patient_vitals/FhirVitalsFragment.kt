package org.piramalswasthya.cho.ui.commons.fhir_patient_vitals


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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
import org.piramalswasthya.cho.ui.commons.PendingCphcFormViewModel
import org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsViewModel
import org.piramalswasthya.cho.utils.BmiUtils
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

    private val anthropometry get() = binding.sectionAnthropometry
    private val vitals get() = binding.sectionVitals

    @Inject
    lateinit var userRepo: UserRepo



    val viewModel: FhirVitalsViewModel by viewModels()
    private val pendingCphcFormViewModel: PendingCphcFormViewModel by activityViewModels()

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
    private var sbpValue: Int? = null
    private var dbpValue: Int? = null
    private var masterDb: MasterDb? = null
    private var editPatientViewModel: EditPatientDetailsViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVitalsCustomBinding.inflate(layoutInflater, container, false)
        anthropometry.inputWeight.addTextChangedListener(textWatcher)
        anthropometry.inputHeight.addTextChangedListener(textWatcher)
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

        try {
            editPatientViewModel = ViewModelProvider(requireActivity())[EditPatientDetailsViewModel::class.java]
        } catch (_: Exception) {
            // Fragment may not be hosted by EditPatientDetailsActivity
        }

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
            vitals.temperatureEditTxt.helperText = "Temperature Required"
            viewModel.boolTemp = true
        } else {
            vitals.temperatureEditTxt.helperText = null
            viewModel.boolTemp = false
        }

        vitals.bpSystolicEditTxt.helperText=null
        vitals.bpDiastolicEditTxt.helperText=null
        vitals.pulseRateEditTxt.helperText=null
        vitals.spo2EditTxt.helperText=null
        vitals.respiratoryEditTxt.helperText=null
        vitals.rbsEditTxt.helperText=null
        anthropometry.heightEditTxt.helperText=null
        anthropometry.weightEditTxt.helperText=null
        populateExistingVitals()
        textwatchers()
    }

    private fun populateExistingVitals() {
        val existingVitals = masterDb?.vitalsMasterDb ?: return
        val hasVitals = listOf(
            existingVitals.height,
            existingVitals.weight,
            existingVitals.bmi,
            existingVitals.temperature,
            existingVitals.pulseRate,
            existingVitals.spo2,
            existingVitals.bpSystolic,
            existingVitals.bpDiastolic,
            existingVitals.respiratoryRate,
            existingVitals.rbs
        ).any { !it.isNullOrBlank() && !it.equals("null", ignoreCase = true) }
        if (!hasVitals) return

        anthropometry.inputHeight.setText(existingVitals.height.orEmpty())
        anthropometry.inputWeight.setText(existingVitals.weight.orEmpty())
        anthropometry.inputBmi.setText(existingVitals.bmi.orEmpty())
        vitals.inputTemperature.setText(existingVitals.temperature.orEmpty())
        vitals.inputPulseRate.setText(existingVitals.pulseRate.orEmpty())
        vitals.inputSpo2.setText(existingVitals.spo2.orEmpty())
        vitals.inputBpSystolic.setText(existingVitals.bpSystolic.orEmpty())
        vitals.inputBpDiastolic.setText(existingVitals.bpDiastolic.orEmpty())
        vitals.inputRespiratoryPerMin.setText(existingVitals.respiratoryRate.orEmpty())
        vitals.inputRBS.setText(existingVitals.rbs.orEmpty())

        if (!existingVitals.bmi.isNullOrBlank()) {
            BmiUtils.applyBmiCategoryFromAnthropometry(
                requireContext(),
                existingVitals.height,
                existingVitals.weight,
                existingVitals.bmi,
                anthropometry.bmiCategory,
                anthropometry.inputBmi
            )
        } else {
            calculateAndDisplayBMI()
        }
    }
    private fun textwatchers() {
        vitals.inputTemperature.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isTemp = s?.isNotEmpty() == true
                if(isTemp){
                    viewModel.tempNull = false
                    validateTemperature(s.toString())
                }else{
                    viewModel.tempNull = true
                    vitals.temperatureEditTxt.helperText=null
                }
                updateNextButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        vitals.inputBpSystolic.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isTemp = s?.isNotEmpty() == true
                if(isTemp){
                    validateBPSystolic(s.toString())
                }else{
                    vitals.bpSystolicEditTxt.helperText=null
                }
                updateNextButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        vitals.inputBpDiastolic.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isTemp = s?.isNotEmpty() == true
                if(isTemp){
                    validateBPD(s.toString())
                }else{
                    vitals.bpDiastolicEditTxt.helperText=null
                }
                updateNextButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        vitals.inputPulseRate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isTemp = s?.isNotEmpty() == true
                if(isTemp){
                    validatePulse(s.toString())
                }else{
                    vitals.pulseRateEditTxt.helperText=null
                }
                updateNextButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        vitals.inputSpo2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isTemp = s?.isNotEmpty() == true
                if(isTemp){
                    validateSpo2(s.toString())
                }else{
                    vitals.spo2EditTxt.helperText=null
                }
                updateNextButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        vitals.inputRespiratoryPerMin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isTemp = s?.isNotEmpty() == true
                if(isTemp){
                    validateResp(s.toString())
                }else{
                    vitals.respiratoryEditTxt.helperText=null
                }
                updateNextButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        vitals.inputRBS.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isTemp = s?.isNotEmpty() == true
                if(isTemp){
                    validateRBS(s.toString())
                }else{
                    vitals.rbsEditTxt.helperText=null
                }
                updateNextButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        anthropometry.inputHeight.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isTemp = s?.isNotEmpty() == true
                if(isTemp){
                    validateHeight(s.toString())
                }else{
                    anthropometry.heightEditTxt.helperText=null
                }
                updateNextButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        anthropometry.inputWeight.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isTemp = s?.isNotEmpty() == true
                if(isTemp){
                    validateWeight(s.toString())
                }else{
                    anthropometry.weightEditTxt.helperText=null
                }
                updateNextButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

    }

    /**
     * Checks all TextInputLayout helper texts. If any has a non-null helperText
     * (indicating a validation error), the Next/Submit button is disabled.
     */
    private fun updateNextButtonState() {
        val hasErrors = vitals.temperatureEditTxt.helperText != null ||
                vitals.bpSystolicEditTxt.helperText != null ||
                vitals.bpDiastolicEditTxt.helperText != null ||
                vitals.pulseRateEditTxt.helperText != null ||
                vitals.spo2EditTxt.helperText != null ||
                vitals.respiratoryEditTxt.helperText != null ||
                vitals.rbsEditTxt.helperText != null ||
                anthropometry.heightEditTxt.helperText != null ||
                anthropometry.weightEditTxt.helperText != null

        editPatientViewModel?.setSubmitActive(!hasErrors)
    }

    private fun validateBPSystolic(bpSystolic: String) {
        try {
            val isValid = bpSystolic.matches(Regex("^\\d{2,3}$")) &&
                    bpSystolic.toInt() in 40..320

            if (isValid) {
                sbpValue = bpSystolic.toInt()
                vitals.bpSystolicEditTxt.helperText = null
            } else {
                sbpValue = null
                vitals.bpSystolicEditTxt.helperText =
                    "Please enter value between 40 and 320."
            }
            validateBPRelation(sbpValue, dbpValue)
        } catch (e: NumberFormatException) {
            sbpValue = null
            vitals.bpSystolicEditTxt.helperText =
                "Please enter a valid numeric value."
        }
    }

    private fun validateBPD(bpD: String) {
        try {
            val isValid = bpD.matches(Regex("^\\d{2,3}$")) &&
                    bpD.toInt() in 10..180
            if (isValid) {
                dbpValue = bpD.toInt()
                vitals.bpDiastolicEditTxt.helperText = null
            } else {
                dbpValue = null
                vitals.bpDiastolicEditTxt.helperText =
                    "Please enter value between 10 and 180."
            }
            validateBPRelation(sbpValue, dbpValue)
        } catch (e: NumberFormatException) {
            dbpValue = null
            vitals.bpDiastolicEditTxt.helperText =
                "Please enter a valid numeric value."
        }
    }
    /* private fun validateTemperature(temperature: String) {
         try {
             val isValid = temperature.matches(Regex("^\\d{2,3}(\\.\\d{1,2})?$"))
             if (isValid) {
                 vitals.temperatureEditTxt.helperText = null
             } else {
                 vitals.temperatureEditTxt.helperText =
                     "Invalid temperature."
             }
         } catch (e: NumberFormatException) {
             vitals.temperatureEditTxt.helperText =
                 "Please enter a valid numeric value."
         }
     }*/

    private fun validateTemperature(temperature: String) {
        try {
            val isValidFormat = temperature.matches(Regex("^\\d{2,3}(\\.\\d{1,2})?$"))
            if (!isValidFormat) {
                vitals.temperatureEditTxt.helperText = "Invalid format. Eg: 98.6"
                return
            }

            val tempValue = temperature.toFloat()
            if (tempValue < 90 || tempValue > 110) {
                vitals.temperatureEditTxt.helperText = "Temperature must be between 90°F and 110°F"
            } else {
                vitals.temperatureEditTxt.helperText = null
            }

        } catch (e: NumberFormatException) {
            vitals.temperatureEditTxt.helperText = "Please enter a valid numeric value"
        }
    }

    private fun validateBPRelation(sbp: Int?, dbp: Int?) {
        if (sbp != null && dbp != null) {
            if (sbp <= dbp) {
                vitals.bpSystolicEditTxt.helperText = "Systolic BP must be greater than Diastolic BP"
                vitals.bpDiastolicEditTxt.helperText = "Diastolic BP must be less than Systolic BP"
            } else {
                vitals.bpSystolicEditTxt.helperText = null
                vitals.bpDiastolicEditTxt.helperText = null
            }
        }
    }
    /*   private fun validateHeight(hei: String) {
           try {
               val isValid = hei.matches(Regex("^\\d{2,3}(\\.\\d{1,2})?$"))
               if (isValid) {
                   anthropometry.heightEditTxt.helperText = null
               } else {
                   anthropometry.heightEditTxt.helperText =
                       "Invalid Height."
               }
           } catch (e: NumberFormatException) {
               anthropometry.heightEditTxt.helperText =
                   "Please enter a valid numeric value."
           }
       }*/

    private fun validateHeight(hei: String) {
        try {
            val isValidFormat = hei.matches(Regex("^\\d{2,3}(\\.\\d{1,2})?$"))
            val heightValue = hei.toFloatOrNull()

            if (isValidFormat && heightValue != null && heightValue in 35.0..200.0) {
                anthropometry.heightEditTxt.helperText = null
            } else {
                anthropometry.heightEditTxt.helperText = "Height must be between 35 cm and 200 cm."
            }
        } catch (e: Exception) {
            anthropometry.heightEditTxt.helperText = "Please enter a valid numeric height."
        }
    }

    /*  private fun validateWeight(w: String) {
          try {
              val isValid = w.matches(Regex("^\\d{2,3}(\\.\\d{1,2})?$"))
              if (isValid) {
                  anthropometry.weightEditTxt.helperText = null
              } else {
                  anthropometry.weightEditTxt.helperText =
                      "Invalid Weight."
              }
          } catch (e: NumberFormatException) {
              anthropometry.weightEditTxt.helperText =
                  "Please enter a valid numeric value."
          }
      }*/

    private fun validateWeight(w: String) {
        try {
            val isValidFormat = w.matches(Regex("^\\d{1,3}(\\.\\d{1,2})?$"))

            if (!isValidFormat) {
                anthropometry.weightEditTxt.helperText = "Invalid format. Example: 72 or 72.5"
                return
            }

            val weightValue = w.toFloat()
            if (weightValue < 2 || weightValue > 150) {
                anthropometry.weightEditTxt.helperText = "Weight must be between 2 kg and 150 kg"
            } else {
                anthropometry.weightEditTxt.helperText = null
            }

        } catch (e: NumberFormatException) {
            anthropometry.weightEditTxt.helperText = "Please enter a valid numeric value"
        }
    }

    /*private fun validatePulse(pul: String) {
        try {
            val isValid = pul.matches(Regex("^\\d{2,3}$"))
            if (isValid) {
                vitals.pulseRateEditTxt.helperText = null
            } else {
                vitals.pulseRateEditTxt.helperText =
                    "Invalid Pulse Rate."
            }
        } catch (e: NumberFormatException) {
            vitals.pulseRateEditTxt.helperText =
                "Please enter a valid numeric value."
        }
    }*/

    private fun validatePulse(pulse: String) {
        try {
            val isValidFormat = pulse.matches(Regex("^\\d{2,3}$"))
            if (!isValidFormat) {
                vitals.pulseRateEditTxt.helperText = "Invalid format."
                return
            }

            val pulseValue = pulse.toInt()
            if (pulseValue < 50 || pulseValue > 200) {
                vitals.pulseRateEditTxt.helperText = "Pulse rate must be between 50 and 200 BPM"
            } else {
                vitals.pulseRateEditTxt.helperText = null
            }

        } catch (e: NumberFormatException) {
            vitals.pulseRateEditTxt.helperText = "Please enter a valid number"
        }
    }
    private fun validateSpo2(spo: String) {
        try {
            val isValid = spo.matches(Regex("^\\d+$")) &&
                    spo.toInt() in 30..100

            if (isValid) {
                vitals.spo2EditTxt.helperText = null
            } else {
                vitals.spo2EditTxt.helperText =
                    "Please enter a numeric value between 30 and 100."
            }
        } catch (e: NumberFormatException) {
            vitals.spo2EditTxt.helperText =
                "Please enter a valid numeric value."
        }
    }

    private fun validateRBS(rbs: String) {
        try {
            val isValid = rbs.matches(Regex("^\\d{2,3}$"))
            if (isValid) {
                vitals.rbsEditTxt.helperText = null
            } else {
                vitals.rbsEditTxt.helperText =
                    "Invalid RBS."
            }
        } catch (e: NumberFormatException) {
            vitals.rbsEditTxt.helperText =
                "Please enter a valid numeric value."
        }
    }

    private fun validateResp(resp: String) {
        try {
            val isValid = resp.matches(Regex("^\\d+$")) &&
                    resp.toInt() in 10..40

            if (isValid) {
                vitals.respiratoryEditTxt.helperText = null
            } else {
                vitals.respiratoryEditTxt.helperText =
                    "Please enter a numeric value between 10 and 40."
            }
        } catch (e: NumberFormatException) {
            vitals.respiratoryEditTxt.helperText =
                "Please enter a valid numeric value."
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
        heightValue = anthropometry.inputHeight.text?.toString()?.trim()
        weightValue = anthropometry.inputWeight.text?.toString()?.trim()
        bmiValue = anthropometry.inputBmi.text?.toString()?.trim()
//        waistCircumferenceValue = binding.inputWaistCircum.text?.toString()?.trim()
        temperatureValue = vitals.inputTemperature.text?.toString()?.trim()
        pulseRateValue = vitals.inputPulseRate.text?.toString()?.trim()
        spo2Value = vitals.inputSpo2.text?.toString()?.trim()
        bpSystolicValue = vitals.inputBpSystolic.text?.toString()?.trim()
        bpDiastolicValue = vitals.inputBpDiastolic.text?.toString()?.trim()
        respiratoryValue = vitals.inputRespiratoryPerMin.text?.toString()?.trim()
        rbsValue = vitals.inputRBS.text?.toString()?.trim()
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
        val heightValue: Float? = anthropometry.inputHeight.text.toString().trim().toFloatOrNull()
        val weightValue : Float? = anthropometry.inputWeight.text.toString().trim().toFloatOrNull()

        if (weightValue != null && heightValue != null && heightValue > 0 &&  weightValue > 0) {
            val bmi = weightValue / (heightValue / 100).pow(2)
            val formattedBMI = "%.2f".format(bmi)
            anthropometry.inputBmi.text = Editable.Factory.getInstance().newEditable(formattedBMI)
            BmiUtils.applyBmiCategory(
                requireContext(),
                formattedBMI,
                anthropometry.bmiCategory,
                anthropometry.inputBmi
            )
        }
        else{
            anthropometry.inputBmi.text = null
            anthropometry.bmiCategory.isVisible = false
        }
    }

    private suspend fun persistPendingCphcFormSuspending() {
        if (!pendingCphcFormViewModel.hasPending()) return
        try {
            pendingCphcFormViewModel.persistPending(requireContext())
        } catch (e: Exception) {
            Timber.e(e, "Failed to persist pending CPHC assessment on visit submit")
            Toast.makeText(
                requireContext(),
                getString(R.string.form_save_failed),
                Toast.LENGTH_LONG
            ).show()
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
        val tempField = isTempFieldFilledForFever()
        if (tempField) {
            val emptyFields = isHelperTrue()
            if (preferenceDao.isUserCHO()) {
                extractFormValues()
                if (!isNull) {
//                viewModel.saveObservationResource(observation)
                    isNull = true
                }
                if (emptyFields.isEmpty()) {
                    setVitalsMasterData()
                    viewLifecycleOwner.lifecycleScope.launch {
                        persistPendingCphcFormSuspending()
                        findNavController().navigate(
                            R.id.action_customVitalsFragment_to_caseRecordCustom, bundle
                        )
                    }
                } else {
                    val message: String = if (emptyFields.size == 1) {
                        "Please fill the ${emptyFields[0]}"
                    } else {
                        "Please fill the following fields:\n${emptyFields.joinToString(", ")}"
                    }
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {

                    var benVisitNo = 0;
                    var createNewBenflow = false;

                    // Use benVisitNo from bundle if a specialized form passed it; otherwise derive from DB to avoid a split-visit mismatch.
                    val passedBenVisitNo = arguments?.getInt("benVisitNo", -1) ?: -1
                    if (passedBenVisitNo > 0) {
                        benVisitNo = passedBenVisitNo
                    } else {
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
                    }
                    if (emptyFields.isEmpty()) {
                        extractFormValues()
                        setVitalsMasterData()

                        val user = userRepo.getLoggedInUser()

                        saveNurseData(benVisitNo, createNewBenflow, user)

                        viewModel.isDataSaved.observe(viewLifecycleOwner) {
                            when (it!!) {
                                true -> {
                                    viewLifecycleOwner.lifecycleScope.launch {
                                        persistPendingCphcFormSuspending()
                                        WorkerUtils.clinicalPushWorker(requireContext())
                                        requireActivity().finish()
                                    }
                                }
                                else -> {}
                            }
                        }

                    } else {
                        val message: String = if (emptyFields.size == 1) {
                            "Please fill the ${emptyFields[0]}"
                        } else {
                            "Please fill the following fields:\n${emptyFields.joinToString(", ")}"
                        }
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }else{
            val message = "Temprature field is mandatory"
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun isTempFieldFilledForFever(): Boolean {
        if (viewModel.boolTemp && viewModel.tempNull){
            return false
        }
        return true
    }

    private fun isHelperTrue(): List<String> {
        val emptyFields = mutableListOf<String>()

        if (vitals.temperatureEditTxt.helperText != null) {
            emptyFields.add("Temperature")
        }
        if (vitals.bpDiastolicEditTxt.helperText != null) {
            emptyFields.add("BP Diastolic")
        }
        if (vitals.bpSystolicEditTxt.helperText != null) {
            emptyFields.add("BP Systolic")
        }
        if (vitals.respiratoryEditTxt.helperText != null) {
            emptyFields.add("Respiratory Rate")
        }
        if (vitals.pulseRateEditTxt.helperText != null) {
            emptyFields.add("Pulse Rate")
        }
        if (vitals.spo2EditTxt.helperText != null) {
            emptyFields.add("Spo2")
        }
        if (vitals.rbsEditTxt.helperText != null) {
            emptyFields.add("RBS")
        }
        if (anthropometry.heightEditTxt.helperText != null) {
            emptyFields.add("Height")
        }
        if (anthropometry.weightEditTxt.helperText != null) {
            emptyFields.add("Weight")
        }
        extractFormValues()
        if(bpDiastolicValue != "" && bpSystolicValue == ""){
            emptyFields.add("Systolic Also")
        }
        if( bpSystolicValue != "" && bpDiastolicValue == ""){
            emptyFields.add("Diastolic Also")
        }
        return emptyFields
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
        arguments?.getInt("benVisitNo", -1)?.takeIf { it > 0 }?.let { bundle.putInt("benVisitNo", it) }
    }

}
