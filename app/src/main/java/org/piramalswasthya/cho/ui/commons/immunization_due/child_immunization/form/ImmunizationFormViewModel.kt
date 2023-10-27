package org.piramalswasthya.cho.ui.commons.immunization_due.child_immunization.form

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
import org.piramalswasthya.cho.configuration.ImmunizationDataset
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.ImmunizationDao
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.ChildImmunizationCategory
import org.piramalswasthya.cho.model.ImmunizationCache
import org.piramalswasthya.cho.model.ImmunizationCategory
import org.piramalswasthya.cho.model.Vaccine
import org.piramalswasthya.cho.repositories.UserRepo

import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ImmunizationFormViewModel @Inject constructor(
    @ApplicationContext context: Context,
    preferenceDao: PreferenceDao,
    savedStateHandle: SavedStateHandle,
    private val vaccineDao: ImmunizationDao,
    private val patientDao: PatientDao,
    private val userRepo: UserRepo,
) : ViewModel() {

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state


    private val patientID =
        ImmunizationFormFragmentArgs.fromSavedStateHandle(savedStateHandle).patientID
    private val vaccineId =
        ImmunizationFormFragmentArgs.fromSavedStateHandle(savedStateHandle).vaccineId


    private val _recordExists = MutableLiveData<Boolean>()
    val recordExists: LiveData<Boolean>
        get() = _recordExists

    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String>
        get() = _benName
    private val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String>
        get() = _benAgeGender

    private val dataset = ImmunizationDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow
    private lateinit var immCache: ImmunizationCache

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val asha  = userRepo.getLoggedInUser()!!
                val savedRecord = vaccineDao.getImmunizationRecord(patientID, vaccineId)
                immCache = savedRecord?.also { _recordExists.postValue(true) } ?: run {
                    ImmunizationCache(
                        patientID = patientID,
                        vaccineId = vaccineId,
                        createdBy = asha.userName,
                        updatedBy = asha.userName,
                        syncState = SyncState.UNSYNCED)
                }.also { _recordExists.postValue(false) }
                val ben = patientDao.getPatientById(patientID)!!
                _benName.postValue("${ben.patient.firstName} ${if (ben.patient.lastName == null) "" else ben.patient.lastName}")
                _benAgeGender.postValue("${ben.patient.age} ${ben.ageUnit.name} | ${ben.gender.genderName}")
//                val saveVaccine = Vaccine(
//                    0,
//                    "vaccine",
//                    315569520000,
//                    788923800000,
//                    ImmunizationCategory.CHILD,
//                    ChildImmunizationCategory.BIRTH,
//                    788923800000,
//                    0,
//                    800000
//                )
//                vaccineDao.addVaccine(saveVaccine)

                val vaccine = vaccineDao.getVaccineById(vaccineId)
                    ?: throw IllegalStateException("Unknown Vaccine Injected, contact HAZMAT team!")
                dataset.setFirstPage(ben, vaccine, savedRecord)
            }
        }

    }
    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)
                    dataset.mapValues(immCache, 1)
                    vaccineDao.addImmunizationRecord(immCache)
                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving PW-ANC data failed!!")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }
    }

    fun updateRecordExists(b: Boolean) {
        _recordExists.value = b

    }


}