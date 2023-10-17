package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_women_registration.form

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
import org.piramalswasthya.sakhi.configuration.PregnantWomanRegistrationDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.HRPNonPregnantAssessCache
import org.piramalswasthya.sakhi.model.HRPPregnantAssessCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.EcrRepo
import org.piramalswasthya.sakhi.repositories.HRPRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.utils.HelperUtil
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PregnancyRegistrationFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val maternalHealthRepo: MaternalHealthRepo,
    ecrRepo: EcrRepo,
    private val hrpRepo: HRPRepo,
    private val benRepo: BenRepo
//    private val householdRepo: HouseholdRepo,
//    userRepo: UserRepo
) : ViewModel() {

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    private val benId =
        PregnancyRegistrationFormFragmentArgs.fromSavedStateHandle(savedStateHandle).benId

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
        PregnantWomanRegistrationDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    private lateinit var pregnancyRegistrationForm: PregnantWomanRegistrationCache

    private var assess: HRPPregnantAssessCache? = null

    init {
        viewModelScope.launch {
            val asha  = preferenceDao.getLoggedInUser()!!
            val ben = maternalHealthRepo.getBenFromId(benId)?.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                pregnancyRegistrationForm = PregnantWomanRegistrationCache(
                    benId = ben.beneficiaryId,
                    syncState = SyncState.UNSYNCED,
                    createdBy = asha.userName,
                    updatedBy = asha.userName
                )
            }

            val assess = hrpRepo.getPregnantAssess(benId)

            assess?.let {
                pregnancyRegistrationForm.createdDate = it.visitDate
            }

            maternalHealthRepo.getSavedRegistrationRecord(benId)?.let {
                pregnancyRegistrationForm = it
                _recordExists.value = true
            } ?: run {
                _recordExists.value = false
            }

            val latestTrack = ecrRepo.getLatestEctByBenId(benId)

            dataset.setUpPage(
                ben,
                assess,
                if (recordExists.value == true) pregnancyRegistrationForm else null,
                latestTrack?.visitDate
            )
            dataset.updateList(30,getIndexOfHRP())


        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }

    }

    fun getIndexOfChildLabel() = dataset.getIndexOfChildLabel()

    fun getIndexOfPhysicalObservationLabel() = dataset.getIndexOfPhysicalObservationLabel()

    fun getIndexOfObstetricHistoryLabel() = dataset.getIndexOfObstetricHistoryLabel()

    fun getIndexOfEdd(): Int = dataset.getIndexOfEdd()
    fun getIndexOfWeeksOfPregnancy(): Int = dataset.getIndexOfWeeksPregnancy()
    fun getIndexOfPastIllness(): Int = dataset.getIndexOfPastIllness()

    fun getIndexOfHRP(): Int = dataset.getIndexOfHRP()

    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)

                    dataset.mapValues(pregnancyRegistrationForm, 1)
                    pregnancyRegistrationForm.processed = "U"
                    pregnancyRegistrationForm.syncState = SyncState.UNSYNCED
                    maternalHealthRepo.persistRegisterRecord(pregnancyRegistrationForm)
                    maternalHealthRepo.getBenFromId(benId)?.let {
                        val hasBenUpdated = dataset.mapValueToBenRegId(it)
                        if (hasBenUpdated)
                            benRepo.updateRecord(it)
                    }
                    assess = hrpRepo.getPregnantAssess(benId)
                    if (assess == null) {
                        assess = HRPPregnantAssessCache(benId = benId, syncState = SyncState.UNSYNCED)
                    }
                    dataset.mapValuesForAssess(assess, 1)
                    assess?.let {
                        it.syncState = SyncState.UNSYNCED
                        hrpRepo.saveRecord(it)
                    }
                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving PWR data failed $e")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }

    fun setRecordExist(b: Boolean) {
        _recordExists.value = b

    }


}