package org.piramalswasthya.cho.ui.commons.maternal_health.delivery_outcome.form

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.configuration.DeliveryOutcomeDataset
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.model.DeliveryOutcomeCache
import org.piramalswasthya.cho.repositories.DeliveryOutcomeRepo
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.R
import javax.inject.Inject

    @HiltViewModel
class DeliveryOutcomeFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext private val context: Context,
    private val deliveryOutcomeRepo: DeliveryOutcomeRepo,
    private val maternalHealthRepo: MaternalHealthRepo,
    private val patientRepo: PatientRepo,
    private val userRepo: UserRepo,
) : ViewModel() {

    val patientID: String = DeliveryOutcomeFormFragmentArgs.fromSavedStateHandle(savedStateHandle).patientID
    val visitNumber: Int = DeliveryOutcomeFormFragmentArgs.fromSavedStateHandle(savedStateHandle).visitNumber

    sealed class State {
        object IDLE : State()
        object SAVING : State()
        object SAVE_SUCCESS_NAVIGATE_VITALS : State()
        object SAVE_FAILED : State()
    }

    sealed class Alert {
        data class InformDistrictNodalOfficer(val message: String) : Alert()
        data class IntensivePncMonitoring(val message: String) : Alert()
        data class HysterectomyNote(val message: String) : Alert()
    }

    private val _state = MutableLiveData<State>(State.IDLE)
    val state: LiveData<State> get() = _state

    private val _alert = MutableLiveData<Alert?>(null)
    val alert: LiveData<Alert?> get() = _alert

    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String> get() = _benName

    private val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String> get() = _benAgeGender

    private val _recordExists = MutableLiveData<Boolean>(false)
    val recordExists: LiveData<Boolean> get() = _recordExists

    private val dataset = DeliveryOutcomeDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    private lateinit var deliveryOutcome: DeliveryOutcomeCache

    init {
        viewModelScope.launch {
            val user = userRepo.getLoggedInUser()
            val userName = user?.userName ?: ""
            val saved = withContext(Dispatchers.IO) { deliveryOutcomeRepo.getDeliveryOutcome(patientID) }
            val lastAnc = withContext(Dispatchers.IO) { maternalHealthRepo.getLastAnc(patientID) }
            val isDelivered = lastAnc?.pregnantWomanDelivered == true
            
            patientRepo.getPatientDisplay(patientID)?.let { ben ->
                _benName.value = "${ben.patient.firstName} ${ben.patient.lastName ?: ""}"
                _benAgeGender.value = "${ben.patient.age} ${ben.ageUnit?.name} | ${ben.gender?.genderName}"
            } ?: run {
                _benName.value = ""
                _benAgeGender.value = ""
            }
            if (saved != null) {
                deliveryOutcome = saved
                _recordExists.value = true
                val deliveryDate = saved.dateOfDelivery ?: System.currentTimeMillis()
                dataset.setUpPage(deliveryDate, saved, isDelivered)
            } else {
                deliveryOutcome = DeliveryOutcomeCache(
                    patientID = patientID,
                    isActive = true,
                    createdBy = userName,
                    updatedBy = userName,
                    syncState = SyncState.UNSYNCED
                )
                _recordExists.value = false
                dataset.setUpPage(System.currentTimeMillis(), null, isDelivered)
            }
        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            dataset.updateList(formId, index)
            checkAndEmitAlerts(formId, index)
        }
    }

    /** Mother's condition form element id. */
    private val motherConditionId = 1
    /** Maternal complications form element id. */
    private val maternalComplicationsId = 2
    /** Index of Maternal Death in do_mother_condition_array. */
    private val motherConditionMaternalDeathIndex = 3
    /** Index of PPH in do_maternal_complications_array. */
    private val complicationPphIndex = 0
    /** Index of Uterine rupture in do_maternal_complications_array. */
    private val complicationUterineRuptureIndex = 4
    /** Index of Hysterectomy performed in do_maternal_complications_array. */
    private val complicationHysterectomyIndex = 8

    /**
     * Show alert only when the option that was just selected is the one that triggers that alert.
     * Prevents alerts from popping on other options (e.g. no Hysterectomy alert when selecting Retained placenta).
     */
    private fun checkAndEmitAlerts(formId: Int, index: Int) {
        _alert.value = when {
            // Only show "Inform District Nodal Officer" when user selected Maternal Death (index 3).
            formId == motherConditionId && index == motherConditionMaternalDeathIndex && dataset.isMaternalDeath() ->
                Alert.InformDistrictNodalOfficer(context.getString(R.string.do_inform_district_nodal_officer))
            // Only show "Intensive PNC" when user just checked PPH (0) or Uterine rupture (4). index can be negative when unchecked.
            formId == maternalComplicationsId && (index == complicationPphIndex || index == complicationUterineRuptureIndex) && dataset.hasPphOrUterineRupture() ->
                Alert.IntensivePncMonitoring(context.getString(R.string.do_alert_intensive_pnc))
            // Only show "Hysterectomy" when user just checked Hysterectomy performed (index 8).
            formId == maternalComplicationsId && index == complicationHysterectomyIndex && dataset.hasHysterectomy() ->
                Alert.HysterectomyNote(context.getString(R.string.do_hysterectomy_note))
            else -> null
        }
    }

    fun clearAlert() {
        _alert.value = null
    }

    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)
                    dataset.mapValues(deliveryOutcome, 1)
                    // Ensure delivery date is saved (from form field or fallback to current time)
                    if (deliveryOutcome.dateOfDelivery == null) {
                        deliveryOutcome.dateOfDelivery = System.currentTimeMillis()
                    }
                    userRepo.getLoggedInUser()?.let { user ->
                        deliveryOutcome.updatedBy = user.userName
                        deliveryOutcome.updatedDate = System.currentTimeMillis()
                    }
                    deliveryOutcomeRepo.saveDeliveryOutcome(deliveryOutcome)
                    _state.postValue(State.SAVE_SUCCESS_NAVIGATE_VITALS)
                } catch (e: Exception) {
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }
}
