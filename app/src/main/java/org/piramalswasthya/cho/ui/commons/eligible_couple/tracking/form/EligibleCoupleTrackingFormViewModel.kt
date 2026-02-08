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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.coroutines.DispatcherProvider
import org.piramalswasthya.cho.configuration.EligibleCoupleTrackingDataset
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.EligibleCoupleTrackingCache
import org.piramalswasthya.cho.repositories.EcrRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.UserRepo
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
    private val patientDao: PatientDao,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    companion object {
        // Status of Woman IDs
        const val STATUS_PREGNANT_WOMAN = 2
        const val STATUS_PERMANENT_STERILIZATION = 6
    }

    val patientID =
        EligibleCoupleTrackingFormFragmentArgs.fromSavedStateHandle(savedStateHandle).patientID

    val createdDate =
        EligibleCoupleTrackingFormFragmentArgs.fromSavedStateHandle(savedStateHandle).createdDate

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    // Alert types for incentives
    enum class AlertType {
        NONE, ANTRA_INCENTIVE, STERILIZATION_INCENTIVE
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

    // Alert LiveData for showing incentive dialogs
    private val _showAlert = MutableLiveData<AlertType>(AlertType.NONE)
    val showAlert: LiveData<AlertType>
        get() = _showAlert

    // Flag to track if status was updated to sterilization
    private val _statusUpdatedToSterilization = MutableLiveData(false)
    val statusUpdatedToSterilization: LiveData<Boolean>
        get() = _statusUpdatedToSterilization

    private val dataset =
        EligibleCoupleTrackingDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    var isPregnant: Boolean = false
    var isSterilized: Boolean = false
    var isAntraSelected: Boolean = false

    private lateinit var eligibleCoupleTracking: EligibleCoupleTrackingCache

    init {
        viewModelScope.launch {
            val asha = userRepo.getLoggedInUser()!!
            val ben = patientRepo.getPatientDisplay(patientID)?.also { ben ->
                _benName.value =
                    "${ben.patient.firstName} ${if (ben.patient.lastName == null) "" else ben.patient.lastName}"
                _benAgeGender.value = "${ben.patient.age} ${ben.ageUnit?.name} | ${ben.gender?.genderName}"
                eligibleCoupleTracking = EligibleCoupleTrackingCache(
                    patientID = patientID,
                    syncState = SyncState.UNSYNCED,
                    createdBy = asha.userName,
                    updatedBy = asha.userName,
                )
            }

            val pastTrack = ecrRepo.getLatestEctByBenId(patientID)
            val ecr = ecrRepo.getSavedECR(patientID)

            Log.d("patient Id is ", patientID)
            Log.d("createdDate is ", createdDate.toString())

            ecrRepo.getEct(patientID, createdDate)?.let {
                eligibleCoupleTracking = it
                _recordExists.value = true
            } ?: run {
                _recordExists.value = false
            }


            dataset.setNumberOfChildren(ecr?.noOfChildren ?: 0)
            dataset.setUpPage(
                ben,
                pastTrack?.visitDate ?: 0,
                pastTrack,
                if (recordExists.value == true) eligibleCoupleTracking else null
            )



        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
            
            // Trigger alerts immediately on selection (Requirement Phase 2)
            if (formId == dataset.getContraceptionMethodId()) {
                if (dataset.isAntraSelected()) {
                    _showAlert.value = AlertType.ANTRA_INCENTIVE
                } else if (dataset.isSterilizationSelected()) {
                    _showAlert.value = AlertType.STERILIZATION_INCENTIVE
                } else {
                    _showAlert.value = AlertType.NONE
                }
            }
        }

    }



    fun saveForm() {
        viewModelScope.launch {
            try {
                // Ensure eligibleCoupleTracking is initialized
                if (!::eligibleCoupleTracking.isInitialized) {
                    val asha = userRepo.getLoggedInUser()
                    eligibleCoupleTracking = EligibleCoupleTrackingCache(
                        patientID = patientID,
                        syncState = SyncState.UNSYNCED,
                        createdBy = asha?.userName ?: "",
                        updatedBy = asha?.userName ?: "",
                    )
                }

                _state.value = State.SAVING

                withContext(dispatcherProvider.io) {
                    dataset.mapValues(eligibleCoupleTracking, 1)
                    ecrRepo.saveEct(eligibleCoupleTracking)
                    Timber.d("ECT data saved successfully for patient: $patientID")

                    // Check statuses in background
                    isPregnant = dataset.isPregnancyPositive()
                    isSterilized = dataset.isSterilizationSelected()
                    isAntraSelected = dataset.isAntraSelected()
                }

                // Update patient status if pregnant
                if (isPregnant) {
                    updatePatientStatusToPregnant()
                }

                // Update patient status if sterilization selected
                if (isSterilized) {
                    updatePatientStatusToSterilized()
                    _statusUpdatedToSterilization.value = true
                    _showAlert.value = AlertType.STERILIZATION_INCENTIVE
                } else if (isAntraSelected) {
                    _showAlert.value = AlertType.ANTRA_INCENTIVE
                } else {
                    // Explicitly clear alert if neither selected
                    _showAlert.value = AlertType.NONE
                }

                // Set SAVE_SUCCESS last so observers see alert changes first
                _state.value = State.SAVE_SUCCESS
            } catch (e: Exception) {
                Timber.e(e, "saving ECT data failed")
                _state.value = State.SAVE_FAILED
            }
        }
    }

    private suspend fun updatePatientStatusToPregnant() {
        try {
            val patient = patientDao.getPatient(patientID)
            patient.statusOfWomanID = STATUS_PREGNANT_WOMAN
            patientDao.updatePatient(patient)
            Timber.d("Patient status updated to Pregnant Woman")
        } catch (e: Exception) {
            Timber.e("Failed to update patient status to pregnant: $e")
        }
    }

    private suspend fun updatePatientStatusToSterilized() {
        try {
            val patient = patientDao.getPatient(patientID)
            patient.statusOfWomanID = STATUS_PERMANENT_STERILIZATION
            patientDao.updatePatient(patient)
            Timber.d("Patient status updated to Permanent Sterilization")
        } catch (e: Exception) {
            Timber.e("Failed to update patient status to sterilized: $e")
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }

    fun resetAlert() {
        _showAlert.value = AlertType.NONE
    }



}