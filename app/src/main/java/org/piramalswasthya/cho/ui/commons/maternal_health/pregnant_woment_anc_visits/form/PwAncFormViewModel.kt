package org.piramalswasthya.cho.ui.commons.maternal_health.pregnant_woment_anc_visits.form



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
import org.piramalswasthya.cho.configuration.PregnantWomanAncVisitDataset
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.PregnantWomanAncCache
import org.piramalswasthya.cho.model.PregnantWomanRegistrationCache
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.UserRepo
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class PwAncFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val maternalHealthRepo: MaternalHealthRepo,
    private val patientRepo: PatientRepo,
    private val userRepo: UserRepo,
) : ViewModel() {

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }


    private val patientID =
        PwAncFormFragmentArgs.fromSavedStateHandle(savedStateHandle).patientID
    private val visitNumber =
        PwAncFormFragmentArgs.fromSavedStateHandle(savedStateHandle).visitNumber
    val isOldVisit =
        PwAncFormFragmentArgs.fromSavedStateHandle(savedStateHandle).isOldVisit


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
        PregnantWomanAncVisitDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    private lateinit var ancCache: PregnantWomanAncCache
    private lateinit var registerRecord: PregnantWomanRegistrationCache

    init {
        viewModelScope.launch {
            val asha = userRepo.getLoggedInUser()!!
            val ben = patientRepo.getPatientDisplay(patientID)?.also { ben ->
                _benName.value =
                    "${ben.patient.firstName} ${ben.patient.lastName ?: ""}"
                _benAgeGender.value = "${ben.patient.age} ${ben.ageUnit?.name} | ${ben.gender?.genderName}"
                ancCache = PregnantWomanAncCache(
                    patientID = patientID,
                    visitNumber = visitNumber,
                    syncState = SyncState.UNSYNCED,
                    createdBy = asha.userName,
                    updatedBy = asha.userName
                )
            }
            registerRecord = maternalHealthRepo.getSavedRegistrationRecord(patientID)!!
            val savedAnc = maternalHealthRepo.getSavedAncRecord(patientID, visitNumber)
            savedAnc?.let {
                ancCache = it
                _recordExists.value = (it.weight != null)
            } ?: run {
                _recordExists.value = false
            }
            val lastAnc = maternalHealthRepo.getSavedAncRecord(patientID, visitNumber - 1)

            dataset.setUpPage(
                visitNumber,
                ben,
                registerRecord,
                lastAnc,
                savedAnc
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
                    dataset.mapValues(ancCache, 1)
                    maternalHealthRepo.persistAncRecord(ancCache)
                    if (ancCache.anyHighRisk == true) {
                        maternalHealthRepo.getSavedRegistrationRecord(patientID)?.let {
                            it.isHrp = true
                            maternalHealthRepo.persistRegisterRecord(it)
                        }
                    }
                    if (ancCache.pregnantWomanDelivered == true) {

                    } else if (ancCache.isAborted) {

                        maternalHealthRepo.getSavedRegistrationRecord(patientID)?.let {
                            it.active = false
                            if (it.processed != "N") it.processed = "U"
                            it.syncState = SyncState.UNSYNCED
                            maternalHealthRepo.persistRegisterRecord(it)
                        }
                        maternalHealthRepo.getAllActiveAncRecords(patientID).apply {
                            forEach {
                                it.isActive = false
                                if (it.processed != "N") it.processed = "U"
                                it.syncState = SyncState.UNSYNCED

                            }
                            maternalHealthRepo.updateAncRecord(toTypedArray())
                        }


                    } else if (ancCache.maternalDeath == true) {
                        maternalHealthRepo.getSavedRegistrationRecord(patientID)?.let {
                            it.active = false
                            if (it.processed != "N") it.processed = "U"
                            it.syncState = SyncState.UNSYNCED
                            maternalHealthRepo.persistRegisterRecord(it)
                        }
                        maternalHealthRepo.getAllActiveAncRecords(patientID).apply {
                            forEach {
                                it.isActive = false
                                if (it.processed != "N") it.processed = "U"
                                it.syncState = SyncState.UNSYNCED
                            }
                            maternalHealthRepo.updateAncRecord(toTypedArray())
                        }
                    }
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

    fun getIndexOfWeeksOfPregnancy(): Int = dataset.getWeeksOfPregnancy()
    fun getIndexOfTT1(): Int = dataset.getIndexOfTd1()
    fun getIndexOfTT2(): Int = dataset.getIndexOfTd2()
    fun getIndexOfTTBooster(): Int = dataset.getIndexOfTdBooster()


}