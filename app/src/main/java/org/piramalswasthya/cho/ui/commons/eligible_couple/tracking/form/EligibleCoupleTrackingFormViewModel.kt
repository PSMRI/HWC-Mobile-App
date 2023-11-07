package org.piramalswasthya.cho.ui.commons.eligible_couple.tracking.form

import android.content.Context
import android.util.Log
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
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.EligibleCoupleTrackingCache
import org.piramalswasthya.cho.repositories.EcrRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.UserRepo
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
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val ecrRepo: EcrRepo,
    private val patientRepo: PatientRepo,
    private val userRepo: UserRepo,
) : ViewModel() {


//    val patientID = ""
//
//    private val zero = 0
//
//    val createdDate = zero.toLong()

    val patientID =
        EligibleCoupleTrackingFormFragmentArgs.fromSavedStateHandle(savedStateHandle).patientID

    val createdDate =
        EligibleCoupleTrackingFormFragmentArgs.fromSavedStateHandle(savedStateHandle).createdDate

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

    //    private lateinit var user: UserDomain
    private val dataset =
        EligibleCoupleTrackingDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    var isPregnant: Boolean = false

    private lateinit var eligibleCoupleTracking: EligibleCoupleTrackingCache

    init {
        viewModelScope.launch {
            val asha = userRepo.getLoggedInUser()!!
            val ben = patientRepo.getPatientDisplay(patientID)?.also { ben ->
                _benName.value =
                    "${ben.patient.firstName} ${if (ben.patient.lastName == null) "" else ben.patient.lastName}"
                _benAgeGender.value = "${ben.patient.age} ${ben.ageUnit.name} | ${ben.gender.genderName}"
                eligibleCoupleTracking = EligibleCoupleTrackingCache(
                    patientID = patientID,
                    syncState = SyncState.UNSYNCED,
                    createdBy = asha.userName,
                    updatedBy = asha.userName,
                )
            }

            val pastTrack = ecrRepo.getLatestEctByBenId(patientID)

            Log.d("patient Id is ", patientID)
            Log.d("createdDate is ", createdDate.toString())

            ecrRepo.getEct(patientID, createdDate)?.let {
                eligibleCoupleTracking = it
                _recordExists.value = true
            } ?: run {
                _recordExists.value = false
            }


            dataset.setUpPage(
                ben,
                pastTrack?.visitDate ?: 0,
                pastTrack,
                if (recordExists.value == true) eligibleCoupleTracking else null
            )


//            ecrRepo.getSavedRecord(patientID)?.let {
//                dataset.setUpPage(
//                    ben,
//                    it.dateOfReg,
//                    pastTrack,
//                    if (recordExists.value == true) eligibleCoupleTracking else null
//                )
//            }

        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }

    }

//    fun getIndexOfEdd(): Int = dataset.getIndexOfEdd()
//    fun getIndexOfWeeksOfPregnancy(): Int = dataset.getIndexOfWeeksPregnancy()
//    fun getIndexOfPastIllness(): Int = dataset.getIndexOfPastIllness()

    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)

                    dataset.mapValues(eligibleCoupleTracking, 1)
                    ecrRepo.saveEct(eligibleCoupleTracking)
                    isPregnant = (eligibleCoupleTracking.isPregnant == "Yes") ||
                            (eligibleCoupleTracking.pregnancyTestResult == "Positive")
                    if (isPregnant) {
//                        ecrRepo.getBenFromId(benId)?.let {
//                            dataset.updateBen(it)
//                            benRepo.updateRecord(it)
//                        }
                    }

                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving ECT data failed due to $e")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }

    fun getIndexOfIsPregnant() = dataset.getIndexOfIsPregnant()

}