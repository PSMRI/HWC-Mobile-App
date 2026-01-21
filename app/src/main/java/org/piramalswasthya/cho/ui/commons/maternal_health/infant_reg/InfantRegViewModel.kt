package org.piramalswasthya.cho.ui.commons.maternal_health.infant_reg

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
import org.piramalswasthya.cho.configuration.InfantRegistrationDataset
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.DeliveryOutcomeCache
import org.piramalswasthya.cho.model.InfantRegCache
import org.piramalswasthya.cho.repositories.DeliveryOutcomeRepo
import org.piramalswasthya.cho.repositories.InfantRegRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class InfantRegViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val infantRegRepo: InfantRegRepo,
    private val deliveryOutcomeRepo: DeliveryOutcomeRepo,
    private val patientRepo: PatientRepo
) : ViewModel() {
    
    val patientID = InfantRegFragmentArgs.fromSavedStateHandle(savedStateHandle).patientID
    val babyIndex: Int = InfantRegFragmentArgs.fromSavedStateHandle(savedStateHandle).babyIndex

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    private val _state = MutableLiveData(State.IDLE)
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
        InfantRegistrationDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    private lateinit var infantReg: InfantRegCache
    private var deliveryOutcome: DeliveryOutcomeCache? = null

    init {
        viewModelScope.launch {
            try {
                val user = preferenceDao.getLoggedInUser()!!
                
                patientRepo.getPatient(patientID).also { patient ->
                    val motherName = patient.parentName ?: "Unknown"
                    _patientName.value = motherName
                    _patientAgeGender.value = "${patient.age ?: ""} | ${patient.genderID ?: ""}"
                    
                    infantReg = InfantRegCache(
                        motherPatientID = patient.patientID,
                        syncState = SyncState.UNSYNCED,
                        createdBy = user.userName,
                        updatedBy = user.userName,
                        babyIndex = babyIndex,
                        isActive = true
                    )
                }

                infantRegRepo.getInfantReg(patientID, babyIndex)?.let {
                    infantReg = it
                    _recordExists.value = true
                } ?: run {
                    _recordExists.value = false
                }

                deliveryOutcome = deliveryOutcomeRepo.getDeliveryOutcome(patientID)
                
                val patient = patientRepo.getPatient(patientID)
                val motherName = patient.parentName ?: "Unknown"
                val lmpDate: Long? = null // TODO: Get from maternal health records if available

                dataset.setUpPage(
                    infantRegCache = if (recordExists.value == true) infantReg else null,
                    deliveryOutcome = deliveryOutcome,
                    motherName = motherName,
                    babyIndex = babyIndex,
                    lmpDate = lmpDate
                )
            } catch (e: Exception) {
                Timber.e(e, "Error initializing InfantRegViewModel")
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
                    dataset.mapValues(infantReg, 1)
                    infantRegRepo.saveInfantReg(infantReg)
                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.e(e, "Error saving infant registration")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }
}
