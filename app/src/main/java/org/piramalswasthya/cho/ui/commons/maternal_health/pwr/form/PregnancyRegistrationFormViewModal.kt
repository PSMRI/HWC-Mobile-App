package org.piramalswasthya.cho.ui.commons.maternal_health.pregnant_women_registration.form

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.configuration.PregnantWomanRegistrationDataset
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.model.PregnantWomanRegistrationCache
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.UserRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PregnancyRegistrationFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val patientRepo: PatientRepo,
    private val maternalHealthRepo: MaternalHealthRepo,
    private val userRepo: UserRepo
) : ViewModel() {

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    // Add LiveData for showing alerts
    private val _showAlert = MutableLiveData<String?>()
    val showAlert: LiveData<String?> get() = _showAlert

    // Add navigation events
    private val _navigateTo = MutableLiveData<NavigationEvent?>()
    val navigateTo: LiveData<NavigationEvent?> get() = _navigateTo

    sealed class NavigationEvent {
        data class ToEligibleCouple(val patientID: String, val showSuccessToast: Boolean = false) : NavigationEvent()
        object ToVitalsAndPrescription : NavigationEvent()
        data class ToVitalsActivity(
            val benVisitInfo: org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo,
            val showSuccessToast: Boolean = false
        ) : NavigationEvent()
        object NavigateUp : NavigationEvent()
    }

    val patientID: String? = savedStateHandle["patientID"]

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

    private val _isReadOnly = MutableLiveData<Boolean>(false)
    val isReadOnly: LiveData<Boolean>
        get() = _isReadOnly

    private val dataset = PregnantWomanRegistrationDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    private lateinit var registrationCache: PregnantWomanRegistrationCache

    init {
        viewModelScope.launch {
            try {
                val asha = userRepo.getLoggedInUser()
                if (asha == null) {
                    Timber.e("No logged in user found!")
                    return@launch
                }

                val patientDeferred = async { patientID?.let { patientRepo.getPatientDisplay(it) } }
                val recordDeferred = async { patientID?.let { maternalHealthRepo.getSavedRegistrationRecord(it) } }

                val patient = patientDeferred.await()
                val existingRecord = recordDeferred.await()

                patient?.let { ben ->
                    _benName.value = "${ben.patient.firstName} ${ben.patient.lastName ?: ""}"
                    _benAgeGender.value = "${ben.patient.age} ${ben.ageUnit?.name} | ${ben.gender?.genderName}"

                    if (existingRecord != null) {
                        // Self-healing: Check if ANC 1 is actually completed
                        if (existingRecord.isFirstAncSubmitted) {
                            val completedAncRecords = maternalHealthRepo.getCompletedActiveAncRecords(patientID!!)
                            val isAnc1Completed = completedAncRecords.any { it.visitNumber == 1 }

                            if (!isAnc1Completed) {
                                Timber.w("Validation Fixed: isFirstAncSubmitted was true but ANC 1 not completed. Resetting flag.")
                                existingRecord.isFirstAncSubmitted = false
                                // Update DB asynchronously to fix persistent state
                                maternalHealthRepo.persistRegisterRecord(existingRecord)
                            }
                        }

                        // Use existing record
                        registrationCache = existingRecord
                        _recordExists.value = true
                        Timber.d("Existing registration record found for patient: ${ben.patient.patientID}")
                    } else {
                        // Create new record
                        registrationCache = createNewRegistrationCache(ben, asha.userName)
                        _recordExists.value = false
                        Timber.d("Creating new registration record for patient: ${ben.patient.patientID}")
                    }

                    // Setup dataset callbacks BEFORE setting up the page
                    setupDatasetCallbacks()

                    // Set up form dataset with all required parameters
                    dataset.setUpPage(
                        ben = ben,
                        savedRecord = if (_recordExists.value == true) registrationCache else null
                    )
                    _isReadOnly.value = dataset.isFormReadOnly
                } ?: run {
                    Timber.e("Patient not found for ID: $patientID")
                    // Handle case where patient is not found
                    _recordExists.value = false
                }
            } catch (e: Exception) {
                Timber.e(e, "Error initializing PregnancyRegistrationFormViewModel")
                _recordExists.value = false
            }
        }
    }

    /** True when form is locked (first ANC already submitted). Used for back navigation to home. */
    fun isFormReadOnly(): Boolean = dataset.isFormReadOnly
    
    fun validateHeight(): Boolean = dataset.validateHeightStrict()

    fun getIndexOfEdd(): Int = dataset.getIndexById(6)
    fun getIndexOfGestationalAge(): Int = dataset.getIndexById(7)
    fun getIndexOfTrimester(): Int = dataset.getIndexById(8)
    fun getIndexOfPara(): Int = dataset.getIndexById(dataset.getParaId())
    fun getIndexOfComplications(): Int = dataset.getIndexById(14)
    fun getIndexOfPreExistingConditions(): Int = dataset.getIndexById(18)
    fun getIndexOfBmi(): Int = dataset.getIndexById(dataset.getBmiId())
    fun getIndexOfHeight(): Int = dataset.getIndexById(15)

    // Helper method to get index of any test date by ID
    fun getIndexOfTestDate(testDateId: Int): Int = dataset.getIndexById(testDateId)

    private fun createNewRegistrationCache(ben: PatientDisplay, userName: String): PregnantWomanRegistrationCache {
        // Use the dataset's helper method and update user fields
        val cache = dataset.createDefaultRegistrationCache(ben)
        cache.createdBy = userName
        cache.updatedBy = userName
        return cache
    }

    private fun setupDatasetCallbacks() {
        // Setup alert callback
        dataset.onShowAlert = { message ->
            _showAlert.postValue(message)
        }

        // Setup navigation callbacks
        dataset.onNavigateToEligibleCouple = {
            patientID?.let { id ->
                _navigateTo.postValue(NavigationEvent.ToEligibleCouple(patientID = id, showSuccessToast = true))
            }
        }

        dataset.onNavigateToVitalsAndPrescription = {
            _navigateTo.postValue(NavigationEvent.ToVitalsAndPrescription)
        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            try {
                dataset.updateList(formId, index)

                // Log specific form field changes for debugging
                when (formId) {
                    5 -> Timber.d("LMP field changed") // lmp.id
                    10 -> Timber.d("Gravida field changed") // gravida.id
                    14 -> Timber.d("Complications in previous pregnancy changed") // complicationsInPreviousPregnancy.id
                    15, 16 -> Timber.d("Height or Weight changed") // height.id or weight.id
                    18 -> Timber.d("Pre-existing conditions changed") // preExistingConditions.id
                    19 -> Timber.d("VDRL/RPR result changed") // vdrlRprResult.id
                    21 -> Timber.d("HIV result changed") // hivResult.id
                    23 -> Timber.d("HBsAg result changed") // hbsAgResult.id
                }
            } catch (e: Exception) {
                Timber.e(e, "Error updating list on value changed")
            }
        }
    }

    fun saveForm() {
        viewModelScope.launch {
            try {
                _state.postValue(State.SAVING)

                // Check for UPT Negative Flow first
                if (dataset.shouldNavigateToEligibleCouple()) {
                    patientID?.let { id ->
                        _navigateTo.postValue(NavigationEvent.ToEligibleCouple(patientID = id, showSuccessToast = true))
                    }
                    _state.postValue(State.IDLE) 
                    return@launch
                }

                // Check if patientID is available
                check(this@PregnancyRegistrationFormViewModel::registrationCache.isInitialized) {
                    "Registration cache not initialized"
                }

                // Update timestamps
                registrationCache.updatedDate = System.currentTimeMillis()

                // If it's a new record, set created date
                if (_recordExists.value != true) {
                    registrationCache.createdDate = System.currentTimeMillis()
                }

                // Map form values to cache
                dataset.mapValues(registrationCache, 1) // Assuming pageNumber = 1

                // Save to repository
                maternalHealthRepo.persistRegisterRecord(registrationCache)

                Timber.d("Pregnancy registration saved successfully for patient: ${registrationCache.patientID}")

                _recordExists.postValue(true)
                _state.postValue(State.SAVE_SUCCESS)
                _navigateTo.postValue(NavigationEvent.NavigateUp)
            } catch (e: Exception) {
                Timber.e(e, "Saving pregnancy registration data failed!!")
                _state.postValue(State.SAVE_FAILED)
            }
        }
    }

    fun setRecordExist(b: Boolean) {
        _recordExists.value = b
    }

    // Clear alert after showing
    fun clearAlert() {
        _showAlert.value = null
    }

    // Clear navigation after handling
    fun clearNavigation() {
        _navigateTo.value = null
    }
}