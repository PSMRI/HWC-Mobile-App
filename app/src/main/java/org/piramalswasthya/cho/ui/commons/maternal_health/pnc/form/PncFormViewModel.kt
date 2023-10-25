package org.piramalswasthya.cho.ui.commons.maternal_health.pnc.form

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
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.model.PregnantWomanAncCache
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.configuration.PncFormDataset
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.PNCVisitCache
import org.piramalswasthya.cho.repositories.DeliveryOutcomeRepo
import org.piramalswasthya.cho.repositories.PncRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PncFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val deliveryOutcomeRepo: DeliveryOutcomeRepo,
    private val pncRepo: PncRepo,
    private val patientRepo: PatientRepo,
) : ViewModel() {

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    private val patientID = ""
    private val visitNumber = 0

//    private val benId =
//        PncFormFragmentArgs.fromSavedStateHandle(savedStateHandle).benId
//    private val visitNumber =
//        PncFormFragmentArgs.fromSavedStateHandle(savedStateHandle).visitNumber


    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state

    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String>
        get() = _benName
    private val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String>
        get() = _benAgeGender

    private val _recordExists = MutableLiveData<Boolean>()
    val recordExists: LiveData<Boolean>
        get() = _recordExists

    //    private lateinit var user: UserDomain
    private val dataset =
        PncFormDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    private lateinit var pncCache: PNCVisitCache

    init {
        viewModelScope.launch {
            val asha = preferenceDao.getLoggedInUser()!!
            val ben = patientRepo.getPatientDisplay(patientID)?.also { ben ->
                _benName.value =
                    "${ben.patient.firstName} ${ben.patient.lastName ?: ""}"
                _benAgeGender.value = "${ben.patient.age} ${ben.ageUnit.name} | ${ben.gender.genderName}"
                pncCache = PNCVisitCache(
                    patientID = patientID,
                    pncPeriod = visitNumber,
                    isActive = true,
                    syncState = SyncState.UNSYNCED,
                    createdBy = asha.userName,
                    updatedBy = asha.userName
                )
            }
            val outcomeRecord = deliveryOutcomeRepo.getDeliveryOutcome(patientID)!!
            pncRepo.getSavedPncRecord(patientID, visitNumber)?.let {
                pncCache = it
                _recordExists.value = true
            } ?: run {
                _recordExists.value = false
            }
            val lastPnc = pncRepo.getLastFilledPncRecord(patientID)

            dataset.setUpPage(
                visitNumber,
                ben,
                outcomeRecord,
                lastPnc,
                if (recordExists.value == true) pncCache else null
            )


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
                    dataset.mapValues(pncCache, 1)
                    pncRepo.persistPncRecord(pncCache)
                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving PW-ANC data failed!! $e")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }

    fun setRecordExist(b: Boolean) {
        _recordExists.value = b

    }

}