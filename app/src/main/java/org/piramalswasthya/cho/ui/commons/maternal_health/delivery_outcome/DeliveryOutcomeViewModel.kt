package org.piramalswasthya.cho.ui.commons.maternal_health.delivery_outcome

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
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.DeliveryOutcomeCache
import org.piramalswasthya.cho.repositories.DeliveryOutcomeRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DeliveryOutcomeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val deliveryOutcomeRepo: DeliveryOutcomeRepo,
    private val patientRepo: PatientRepo
) : ViewModel() {
    
    val patientID = DeliveryOutcomeFragmentArgs.fromSavedStateHandle(savedStateHandle).patientID

    sealed class State {
        object IDLE : State()
        object SAVING : State()
        data class SAVE_SUCCESS(val shouldNavigateToInfantReg: Boolean) : State()
        object SAVE_FAILED : State()
    }

    private val _state = MutableLiveData<State>(State.IDLE)
    val state: LiveData<State>
        get() = _state

    private val _patientName = MutableLiveData<String>()
    val patientName: LiveData<String>
        get() = _patientName
        
    private val _patientAgeGender = MutableLiveData<String>()
    val patientAgeGender: LiveData<String>
        get() = _patientAgeGender

    private val _recordExists = MutableLiveData<Boolean>()
    val recordExists: LiveData<Boolean>
        get() = _recordExists

    private val dataset =
        DeliveryOutcomeDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    private lateinit var deliveryOutcome: DeliveryOutcomeCache

    init {
        viewModelScope.launch {
            try {
                val user = preferenceDao.getLoggedInUser()!!
                
                patientRepo.getPatient(patientID).also { patient ->
                    _patientName.value = patient.parentName ?: "Unknown"
                    _patientAgeGender.value = "${patient.age ?: ""} | ${patient.genderID ?: ""}"
                    
                    deliveryOutcome = DeliveryOutcomeCache(
                        patientID = patient.patientID,
                        syncState = SyncState.UNSYNCED,
                        createdBy = user.userName,
                        updatedBy = user.userName,
                        isActive = true
                    )
                }

                deliveryOutcomeRepo.getDeliveryOutcome(patientID)?.let {
                    deliveryOutcome = it
                    _recordExists.value = true
                } ?: run {
                    _recordExists.value = false
                }

                dataset.setUpPage(
                    if (recordExists.value == true) deliveryOutcome else null,
                    patientID
                )
            } catch (e: Exception) {
                Timber.e(e, "Error initializing DeliveryOutcomeViewModel")
                _recordExists.value = false
            }
        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }
    }

    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)

                    dataset.mapValues(deliveryOutcome, 1)

                    deliveryOutcomeRepo.saveDeliveryOutcome(deliveryOutcome)

                    val hasLiveBirth = (deliveryOutcome.liveBirth ?: 0) > 0
                    val shouldNavigateToInfantReg = hasLiveBirth

                    if (deliveryOutcome.isDeath == true || deliveryOutcome.complication?.equals("DEATH", ignoreCase = true) == true) {
                        patientRepo.getPatient(patientID).let { patient ->
                            Timber.d("Patient died during delivery: $patientID")
                        }
                    }

                    _state.postValue(State.SAVE_SUCCESS(shouldNavigateToInfantReg))
                } catch (e: Exception) {
                    Timber.e(e, "Error saving delivery outcome")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }
}
