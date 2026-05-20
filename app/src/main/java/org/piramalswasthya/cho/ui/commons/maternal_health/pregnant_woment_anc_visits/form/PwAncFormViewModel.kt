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
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.model.PregnantWomanAncCache
import org.piramalswasthya.cho.model.PregnantWomanRegistrationCache
import org.piramalswasthya.cho.repositories.EcrRepo
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.repositories.UserRepo
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class PwAncFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext private val context: Context,
    private val maternalHealthRepo: MaternalHealthRepo,
    private val patientRepo: PatientRepo,
    private val userRepo: UserRepo,
    private val ecrRepo: EcrRepo,
) : ViewModel() {

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }


    private val patientID =
        PwAncFormFragmentArgs.fromSavedStateHandle(savedStateHandle).patientID
    private val visitNumber =
        PwAncFormFragmentArgs.fromSavedStateHandle(savedStateHandle).visitNumber
    private val initialIsOldVisit = PwAncFormFragmentArgs.fromSavedStateHandle(savedStateHandle).isOldVisit

    private val _isOldVisit = MutableLiveData(initialIsOldVisit)
    val isOldVisit: LiveData<Boolean> = _isOldVisit


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

    private val _initErrorMessage = MutableLiveData<String?>()
    val initErrorMessage: LiveData<String?> get() = _initErrorMessage

    fun clearInitError() { _initErrorMessage.value = null }

    //    private lateinit var user: UserDomain
    private val dataset =
        PregnantWomanAncVisitDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow
    val ancAlertMessage = dataset.alertErrorMessageFlow

    private lateinit var ancCache: PregnantWomanAncCache
    private lateinit var registerRecord: PregnantWomanRegistrationCache
    private var ben: PatientDisplay? = null
    private var lastAncVisitNumber: Int = 0
    private var currentVisitNumber: Int = visitNumber
    private var shouldTriggerBeneficiarySyncAfterSave: Boolean = false

    init {
        viewModelScope.launch {
            try {
            val asha = userRepo.getLoggedInUser()
            if (asha == null) {
                Timber.e("PwAncFormViewModel: no logged-in user found")
                _initErrorMessage.postValue(context.getString(R.string.form_session_expired))
                return@launch
            }
            ben = patientRepo.getPatientDisplay(patientID)?.also { ben ->
                _benName.value =
                    "${ben.patient.firstName} ${ben.patient.lastName ?: ""}"
                _benAgeGender.value =
                    "${ben.patient.age} ${ben.ageUnit?.name} | ${ben.gender?.genderName}"
                ancCache = PregnantWomanAncCache(
                    patientID = patientID,
                    visitNumber = visitNumber,
                    syncState = SyncState.UNSYNCED,
                    createdBy = asha.userName,
                    updatedBy = asha.userName
                )
            }
            if (ben == null) {
                Timber.e("PwAncFormViewModel: patient not found for ID %s", patientID)
                _initErrorMessage.postValue(context.getString(R.string.form_patient_not_found))
                return@launch
            }
            registerRecord = maternalHealthRepo.getSavedRegistrationRecord(patientID) ?: run {
                Timber.e("No registration record found for $patientID")
                _initErrorMessage.postValue(context.getString(R.string.form_patient_not_found))
                return@launch
            }
            
            // Get or create ANC record for this visit
            var savedAnc = maternalHealthRepo.getSavedAncRecord(patientID, visitNumber)
            
            // CRITICAL FIX: If savedAnc is null (e.g., HRP visit 5+), create it on-demand
            if (savedAnc == null) {
                Timber.d("Creating new ANC record for visit $visitNumber (likely HRP > 4)")
                val newAncRecord = PregnantWomanAncCache(
                    patientID = patientID,
                    visitNumber = visitNumber,
                    ancDate = System.currentTimeMillis(), // Current date
                    isActive = true,
                    createdBy = asha.userName,
                    updatedBy = asha.userName,
                    syncState = SyncState.UNSYNCED
                )
                maternalHealthRepo.persistAncRecord(newAncRecord)
                savedAnc = maternalHealthRepo.getSavedAncRecord(patientID, visitNumber)
            }
            
            // Per Jira MHWC-197: Previous forms become read-only when a NEWER visit is started (completed).
            // Schedule pre-creates all 4 visit records (no weight); so lock only when a later visit has weight.
            val allAncRecords = maternalHealthRepo.getAllActiveAncRecords(patientID)
            val maxCompletedVisitNumber = allAncRecords.filter { it.weight != null }.maxOfOrNull { it.visitNumber } ?: 0
            val hasDelivered = allAncRecords.any { it.pregnantWomanDelivered == true }
            val isOldVisit = hasDelivered || visitNumber < maxCompletedVisitNumber
            
            savedAnc?.let {
                val registerRecordOrNull = maternalHealthRepo.getSavedRegistrationRecord(patientID)
                if (registerRecordOrNull == null) {
                    Timber.e("No registration record for patient $patientID; cannot load ANC form")
                    _initErrorMessage.postValue(context.getString(R.string.form_patient_not_found))
                    _state.postValue(State.SAVE_FAILED)
                    return@launch
                }
                registerRecord = registerRecordOrNull
                lastAncVisitNumber = allAncRecords.maxOfOrNull { it.visitNumber } ?: 0
                val recordExists =
                    maternalHealthRepo.getSavedAncRecord(patientID, visitNumber)?.let {
                        ancCache = it
                        _recordExists.value = (it.weight != null)
                        true
                    } ?: run {
                        _recordExists.value = false
                        false
                    }
                val lastAnc = maternalHealthRepo.getSavedAncRecord(patientID, visitNumber - 1)
                
                // Set isOldVisit based on whether a newer visit exists
                _isOldVisit.value = isOldVisit

                dataset.setUpPage(
                    visitNumber,
                    ben,
                    registerRecord,
                    lastAnc,
                    if (recordExists) ancCache else null
                )
            } ?: run {
                // Even if savedAnc is null, we MUST still set up the form
                _isOldVisit.value = isOldVisit
                lastAncVisitNumber = allAncRecords.maxOfOrNull { it.visitNumber } ?: 0
                _recordExists.value = false
                val lastAnc = maternalHealthRepo.getSavedAncRecord(patientID, visitNumber - 1)
                
                // CRITICAL: Set up the form even for new visits
                dataset.setUpPage(
                    visitNumber,
                    ben,
                    registerRecord,
                    lastAnc,
                    null  // No existing data for new visit
                )
            }
            } catch (e: Exception) {
                Timber.e(e, "PwAncFormViewModel: failed to initialize form")
                _initErrorMessage.postValue(context.getString(R.string.form_load_failed))
            }
        }
    }

    private val ancPeriodFormId = 3

    private fun switchToVisit(selectedVisit: Int) {
        if (selectedVisit == currentVisitNumber) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    if (!::registerRecord.isInitialized) {
                        Timber.w("switchToVisit: registerRecord not initialized yet")
                        _state.postValue(State.SAVE_FAILED)
                        return@withContext
                    }
                    val saved = maternalHealthRepo.getSavedAncRecord(patientID, selectedVisit)
                    val lastAnc = maternalHealthRepo.getSavedAncRecord(patientID, selectedVisit - 1)
                    val asha = userRepo.getLoggedInUser() ?: return@withContext
                    ancCache = saved ?: PregnantWomanAncCache(
                        patientID = patientID,
                        visitNumber = selectedVisit,
                        syncState = SyncState.UNSYNCED,
                        createdBy = asha.userName,
                        updatedBy = asha.userName
                    )
                    currentVisitNumber = selectedVisit
                    _recordExists.postValue(saved != null)
                    
                    // Per Jira MHWC-197: Lock when a newer visit is completed (weight set). Schedule pre-creates all visits.
                    val allAncRecords = maternalHealthRepo.getAllActiveAncRecords(patientID)
                    val maxCompletedVisitNumber = allAncRecords.filter { it.weight != null }.maxOfOrNull { it.visitNumber } ?: 0
                    val hasDelivered = allAncRecords.any { it.pregnantWomanDelivered == true }
                    val isOld = hasDelivered || selectedVisit < maxCompletedVisitNumber
                    lastAncVisitNumber = allAncRecords.maxOfOrNull { it.visitNumber } ?: 0
                    _isOldVisit.postValue(isOld)
                    dataset.setUpPage(
                        selectedVisit,
                        ben,
                        registerRecord,
                        lastAnc,
                        if (saved != null) ancCache else null
                    )
                } catch (e: Exception) {
                    Timber.e(e, "switchToVisit failed")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }


    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
            if (formId == ancPeriodFormId) {
                dataset.getAncVisitNumber()?.let { newVisit ->
                    if (newVisit != currentVisitNumber) switchToVisit(newVisit)
                }
            }
        }
    }


    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)
                    shouldTriggerBeneficiarySyncAfterSave = false
                    val wasCompletedBefore = ancCache.weight != null
                    dataset.mapValues(ancCache, 1)
                    // Mark ANC as updated for re-sync so /maternal/ancVisit/saveAll is called on edits.
                    if (ancCache.processed != "N") ancCache.processed = "U"
                    ancCache.syncState = SyncState.UNSYNCED
                    ancCache.updatedDate = System.currentTimeMillis()
                    val isCompletedNow = ancCache.weight != null
                    
                    // Per Jira MHWC-196: Log when patient moves from due list to completed list
                    if (!wasCompletedBefore && isCompletedNow) {
                        Timber.d("ANC Visit Completed: Patient $patientID, ANC Visit ${ancCache.visitNumber} completed. " +
                                "Moving from ANC ${ancCache.visitNumber} due list to completed list.")
                    }
                    
                    maternalHealthRepo.persistAncRecord(ancCache)
                    
                    // PER-163: Ensure Registration Record is updated and persisted
                    if (::registerRecord.isInitialized) {
                        // Apply HRP update
                        if (ancCache.anyHighRisk == true) {
                            registerRecord.isHrp = true
                            if (registerRecord.processed != "N") registerRecord.processed = "U"
                            registerRecord.syncState = SyncState.UNSYNCED
                            registerRecord.updatedDate = System.currentTimeMillis()
                        }
                        
                        maternalHealthRepo.persistRegisterRecord(registerRecord)
                    } else {
                        // Fallback if not initialized (though it should be)
                         maternalHealthRepo.getSavedRegistrationRecord(patientID)?.let {
                            var changed = false
                            if (it.syncState == SyncState.UNSYNCED) changed = true
                            
                            if (ancCache.anyHighRisk == true) {
                                it.isHrp = true
                                if (it.processed != "N") it.processed = "U"
                                it.syncState = SyncState.UNSYNCED
                                it.updatedDate = System.currentTimeMillis()
                                changed = true
                            }
                            
                            if (changed) maternalHealthRepo.persistRegisterRecord(it)
                         }
                    }
                    
                    if (isCompletedNow) {
                        maternalHealthRepo.getSavedRegistrationRecord(patientID)?.let {
                            if (!it.isFirstAncSubmitted) {
                                it.isFirstAncSubmitted = true
                                if (it.processed != "N") it.processed = "U"
                                it.syncState = SyncState.UNSYNCED
                                it.updatedDate = System.currentTimeMillis()
                                maternalHealthRepo.persistRegisterRecord(it)
                                Timber.d("Setting isFirstAncSubmitted to true for patient $patientID")
                            }
                        }
                    }
                    if (ancCache.pregnantWomanDelivered == true) {
                        val patient = patientRepo.getPatient(patientID)
                        patient.statusOfWomanID = 3 // Post Natal Mother
                        patient.syncState = SyncState.UNSYNCED
                        patientRepo.updateRecord(patient)
                        shouldTriggerBeneficiarySyncAfterSave = true
                        Timber.d("ANC delivered=true for patient $patientID, marked postnatal and queued beneficiary sync")
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
                        try {
                            val patient = patientRepo.getPatient(patientID)
                            if (patient.statusOfWomanID != 1) {
                                patient.statusOfWomanID = 1 // Eligible Couple — fresh start after abortion
                                patient.syncState = SyncState.UNSYNCED
                                patientRepo.updateRecord(patient)
                            }
                            // Deactivate every ECT row + clear pregnancy markers so EC list shows
                            // her as "needs visit" and the ECT catch-all does not leak her back.
                            ecrRepo.getAllECT(patientID).forEach { ect ->
                                if (ect.isActive || ect.isPregnant != null || ect.pregnancyTestResult != null) {
                                    ect.isActive = false
                                    ect.isPregnant = null
                                    ect.pregnancyTestResult = null
                                    if (ect.processed != "N") ect.processed = "U"
                                    ect.syncState = SyncState.UNSYNCED
                                    ecrRepo.saveEct(ect)
                                }
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Abortion EC transition failed for $patientID")
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
    
    fun getPatientID(): String = patientID

    fun resetAlertMessage() {
        viewModelScope.launch { dataset.resetErrorMessageFlow() }
    }

    fun shouldTriggerBeneficiarySyncAfterSave(): Boolean = shouldTriggerBeneficiarySyncAfterSave

    fun consumeBeneficiarySyncTrigger() {
        shouldTriggerBeneficiarySyncAfterSave = false
    }

}
