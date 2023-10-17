package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.form

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
import org.piramalswasthya.cho.repositories.BenRepo
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PwAncFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val maternalHealthRepo: MaternalHealthRepo,
    private val benRepo: BenRepo
) : ViewModel() {

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    private val benId =
        PwAncFormFragmentArgs.fromSavedStateHandle(savedStateHandle).benId
    private val visitNumber =
        PwAncFormFragmentArgs.fromSavedStateHandle(savedStateHandle).visitNumber


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
            val asha = preferenceDao.getLoggedInUser()!!
            val ben = maternalHealthRepo.getBenFromId(benId)?.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                ancCache = PregnantWomanAncCache(
                    benId = ben.beneficiaryId,
                    visitNumber = visitNumber,
                    syncState = SyncState.UNSYNCED,
                    createdBy = asha.userName,
                    updatedBy = asha.userName
                )
            }
            registerRecord = maternalHealthRepo.getSavedRegistrationRecord(benId)!!
            maternalHealthRepo.getSavedAncRecord(benId, visitNumber)?.let {
                ancCache = it
                _recordExists.value = true
            } ?: run {
                _recordExists.value = false
            }
            val lastAnc = maternalHealthRepo.getSavedAncRecord(benId, visitNumber - 1)

            dataset.setUpPage(
                visitNumber,
                ben,
                registerRecord,
                lastAnc,
                if (recordExists.value == true) ancCache else null
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
                    if (registerRecord.syncState == SyncState.UNSYNCED)
                        maternalHealthRepo.persistRegisterRecord(registerRecord)
                    if (ancCache.pregnantWomanDelivered == true) {
                        maternalHealthRepo.getBenFromId(benId)?.let {
                            dataset.updateBenRecordToDelivered(it)
                            benRepo.updateRecord(it)
                        }
                    } else if (ancCache.isAborted) {
                        maternalHealthRepo.getSavedRegistrationRecord(benId)?.let {
                            it.active = false
                            if (it.processed != "N") it.processed = "U"
                            it.syncState = SyncState.UNSYNCED
                            maternalHealthRepo.persistRegisterRecord(it)
                        }
                        maternalHealthRepo.getAllActiveAncRecords(benId).apply {
                            forEach {
                                it.isActive = false
                                if (it.processed != "N") it.processed = "U"
                                it.syncState = SyncState.UNSYNCED

                            }
                            maternalHealthRepo.updateAncRecord(toTypedArray())
                        }
                        maternalHealthRepo.getBenFromId(benId)?.let {
                            dataset.updateBenRecordToEligibleCouple(it)
                            benRepo.updateRecord(it)
                        }

                    } else if (ancCache.maternalDeath == true) {
                        maternalHealthRepo.getSavedRegistrationRecord(benId)?.let {
                            it.active = false
                            if (it.processed != "N") it.processed = "U"
                            it.syncState = SyncState.UNSYNCED
                            maternalHealthRepo.persistRegisterRecord(it)
                        }
                        maternalHealthRepo.getAllActiveAncRecords(benId).apply {
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