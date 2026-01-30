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
            val user = userRepo.getLoggedInUser() ?: return@launch
            val saved = withContext(Dispatchers.IO) { deliveryOutcomeRepo.getDeliveryOutcome(patientID) }
            patientRepo.getPatientDisplay(patientID)?.let { ben ->
                _benName.value = "${ben.patient.firstName} ${ben.patient.lastName ?: ""}"
                _benAgeGender.value = "${ben.patient.age} ${ben.ageUnit?.name} | ${ben.gender?.genderName}"
            }
            if (saved != null) {
                deliveryOutcome = saved
                _recordExists.value = true
                val deliveryDate = saved.dateOfDelivery ?: System.currentTimeMillis()
                dataset.setUpPage(deliveryDate, saved)
            } else {
                deliveryOutcome = DeliveryOutcomeCache(
                    patientID = patientID,
                    isActive = true,
                    createdBy = user.userName,
                    updatedBy = user.userName,
                    syncState = SyncState.UNSYNCED
                )
                _recordExists.value = false
                dataset.setUpPage(System.currentTimeMillis(), null)
            }
        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            dataset.updateList(formId, index)
            checkAndEmitAlerts()
        }
    }

    private fun checkAndEmitAlerts() {
        if (dataset.isMaternalDeath()) {
            _alert.value = Alert.InformDistrictNodalOfficer(
                context.getString(R.string.do_inform_district_nodal_officer)
            )
        }
        if (dataset.isComplicationOrCritical() && dataset.hasPphOrUterineRupture()) {
            _alert.value = Alert.IntensivePncMonitoring(
                context.getString(R.string.do_alert_intensive_pnc)
            )
        }
        if (dataset.hasHysterectomy()) {
            _alert.value = Alert.HysterectomyNote(
                context.getString(R.string.do_hysterectomy_note)
            )
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
                    deliveryOutcome.dateOfDelivery = deliveryOutcome.dateOfDelivery ?: System.currentTimeMillis()
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
