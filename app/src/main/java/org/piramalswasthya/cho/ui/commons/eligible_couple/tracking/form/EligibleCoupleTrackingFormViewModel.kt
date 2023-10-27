package org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.tracking.form

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
import org.piramalswasthya.cho.configuration.EligibleCoupleTrackingDataset
import org.piramalswasthya.cho.model.EligibleCoupleTrackingCache
import org.piramalswasthya.cho.repositories.PatientRepo
//import org.piramalswasthya.sakhi.configuration.EligibleCoupleTrackingDataset
//import org.piramalswasthya.sakhi.database.room.SyncState
//import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
//import org.piramalswasthya.sakhi.model.EligibleCoupleTrackingCache
//import org.piramalswasthya.sakhi.repositories.BenRepo
//import org.piramalswasthya.sakhi.repositories.EcrRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EligibleCoupleTrackingFormViewModel @Inject constructor(
//    savedStateHandle: SavedStateHandle,
//    preferenceDao: PreferenceDao,
//    @ApplicationContext context: Context,
//    private val ecrRepo: EcrRepo,
    private val patientRepo: PatientRepo
) : ViewModel() {


//    val benId =
//        EligibleCoupleTrackingFormFragmentArgs.fromSavedStateHandle(savedStateHandle).benId
//
//    val createdDate =
//        EligibleCoupleTrackingFormFragmentArgs.fromSavedStateHandle(savedStateHandle).createdDate

//    enum class State {
//        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
//    }
//
//    private val _state = MutableLiveData(State.IDLE)
//    val state: LiveData<State>
//        get() = _state
//
//    private val _benName = MutableLiveData<String>()
//    val benName: LiveData<String>
//        get() = _benName
//    private val _benAgeGender = MutableLiveData<String>()
//    val benAgeGender: LiveData<String>
//        get() = _benAgeGender
//
//    private val _recordExists = MutableLiveData<Boolean>()
//    val recordExists: LiveData<Boolean>
//        get() = _recordExists
//
//    //    private lateinit var user: UserDomain
//    private val dataset =
//        EligibleCoupleTrackingDataset(context, preferenceDao.getCurrentLanguage())
//    val formList = dataset.listFlow
//
//    var isPregnant: Boolean = false
//
//    private lateinit var eligibleCoupleTracking: EligibleCoupleTrackingCache
//
//    init {
//        viewModelScope.launch {
//            val asha = preferenceDao.getLoggedInUser()!!
//            val ben = ecrRepo.getBenFromId(benId)?.also { ben ->
//                _benName.value =
//                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
//                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
//                eligibleCoupleTracking = EligibleCoupleTrackingCache(
//                    benId = ben.beneficiaryId,
//                    syncState = SyncState.UNSYNCED,
//                    createdBy = asha.userName,
//                    updatedBy = asha.userName,
//                )
//            }
//
//            val pastTrack = ecrRepo.getLatestEctByBenId(benId)
//
//            ecrRepo.getEct(benId, createdDate)?.let {
//                eligibleCoupleTracking = it
//                _recordExists.value = true
//            } ?: run {
//                _recordExists.value = false
//            }
//
//            ecrRepo.getSavedRecord(benId)?.let {
//                dataset.setUpPage(
//                    ben,
//                    it.dateOfReg,
//                    pastTrack,
//                    if (recordExists.value == true) eligibleCoupleTracking else null
//                )
//            }
//
//        }
//    }
//
//    fun updateListOnValueChanged(formId: Int, index: Int) {
//        viewModelScope.launch {
//            dataset.updateList(formId, index)
//        }
//
//    }
//
////    fun getIndexOfEdd(): Int = dataset.getIndexOfEdd()
////    fun getIndexOfWeeksOfPregnancy(): Int = dataset.getIndexOfWeeksPregnancy()
////    fun getIndexOfPastIllness(): Int = dataset.getIndexOfPastIllness()
//
//    fun saveForm() {
//        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//                try {
//                    _state.postValue(State.SAVING)
//
//                    dataset.mapValues(eligibleCoupleTracking, 1)
//                    ecrRepo.saveEct(eligibleCoupleTracking)
//                    isPregnant = (eligibleCoupleTracking.isPregnant == "Yes") ||
//                            (eligibleCoupleTracking.pregnancyTestResult == "Positive")
//                    if (isPregnant) {
//                        ecrRepo.getBenFromId(benId)?.let {
//                            dataset.updateBen(it)
//                            benRepo.updateRecord(it)
//                        }
//                    }
//
//                    _state.postValue(State.SAVE_SUCCESS)
//                } catch (e: Exception) {
//                    Timber.d("saving ECT data failed due to $e")
//                    _state.postValue(State.SAVE_FAILED)
//                }
//            }
//        }
//    }
//
//    fun resetState() {
//        _state.value = State.IDLE
//    }
//
//    fun getIndexOfIsPregnant() = dataset.getIndexOfIsPregnant()

}