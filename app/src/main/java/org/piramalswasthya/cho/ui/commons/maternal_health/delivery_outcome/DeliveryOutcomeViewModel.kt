package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.delivery_outcome

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
import org.piramalswasthya.sakhi.configuration.DeliveryOutcomeDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.DeliveryOutcomeCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.DeliveryOutcomeRepo
import org.piramalswasthya.sakhi.repositories.EcrRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DeliveryOutcomeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val deliveryOutcomeRepo: DeliveryOutcomeRepo,
    private val ecrRepo: EcrRepo,
    private val pwrRepo: MaternalHealthRepo,
    private val benRepo: BenRepo
) : ViewModel() {
    val benId =
        DeliveryOutcomeFragmentArgs.fromSavedStateHandle(savedStateHandle).benId

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

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

    private val dataset =
        DeliveryOutcomeDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    private lateinit var deliveryOutcome: DeliveryOutcomeCache

    init {
        viewModelScope.launch {
            val asha  = preferenceDao.getLoggedInUser()!!
            val pwr = pwrRepo.getLatestActiveRegistrationRecord(benId)!!
            val anc = pwrRepo.getLatestAncRecord(benId)!!
            benRepo.getBenFromId(benId)?.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                deliveryOutcome = DeliveryOutcomeCache(
                    benId = ben.beneficiaryId,
                    syncState = SyncState.UNSYNCED,
                    createdBy = asha.userName,
                    updatedBy = asha.userName,
                    isActive =  true
                )
            }

            deliveryOutcomeRepo.getDeliveryOutcome(benId)?.let {
                deliveryOutcome = it
                _recordExists.value = true
            } ?: run {
                _recordExists.value = false
            }

            dataset.setUpPage(pwr, anc,
                if (recordExists.value == true) deliveryOutcome else null
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
                    dataset.mapValues(deliveryOutcome, 1)
                    deliveryOutcomeRepo.saveDeliveryOutcome(deliveryOutcome)

                    val ecr = ecrRepo.getSavedRecord(deliveryOutcome.benId)
                    if (ecr != null) {
                        deliveryOutcome.liveBirth?.let {
                            ecr.noOfLiveChildren = ecr.noOfLiveChildren + it
                        }
                        deliveryOutcome.deliveryOutcome?.let {
                            ecr.noOfChildren = ecr.noOfChildren + it
                        }
                        if(ecr.processed!="N")ecr.processed = "U"
                        ecr.syncState = SyncState.UNSYNCED
                        ecrRepo.persistRecord(ecr)
                    }

//                    val pwr = pwrRepo.getSavedRegistrationRecord(deliveryOutcome.benId)
//                    if(pwr != null) {
//                        pwr.active = false
//                        pwr.processed = "U"
//                        pwrRepo.persistRegisterRecord(pwr)
//                    }

                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving delivery outcome data failed!!")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }

}
