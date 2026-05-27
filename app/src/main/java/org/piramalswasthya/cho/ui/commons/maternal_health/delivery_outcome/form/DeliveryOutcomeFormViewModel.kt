package org.piramalswasthya.cho.ui.commons.maternal_health.delivery_outcome.form

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
import org.piramalswasthya.cho.configuration.DeliveryOutcomeDataset
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.model.DeliveryOutcomeCache
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PregnantWomanRegistrationCache
import org.piramalswasthya.cho.repositories.DeliveryOutcomeRepo
import org.piramalswasthya.cho.repositories.EcrRepo
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.R
import timber.log.Timber
import javax.inject.Inject

    @HiltViewModel
class DeliveryOutcomeFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext private val context: Context,
    private val deliveryOutcomeRepo: DeliveryOutcomeRepo,
    private val maternalHealthRepo: MaternalHealthRepo,
    private val patientRepo: PatientRepo,
    private val userRepo: UserRepo,
    private val patientDao: PatientDao,
    private val ecrRepo: EcrRepo
) : ViewModel() {

    companion object {
        const val STATUS_POST_NATAL_MOTHER = 3
    }

    val patientID: String = DeliveryOutcomeFormFragmentArgs.fromSavedStateHandle(savedStateHandle).patientID
    val visitNumber: Int = DeliveryOutcomeFormFragmentArgs.fromSavedStateHandle(savedStateHandle).visitNumber

    sealed class State {
        object IDLE : State()
        object SAVING : State()
        object SAVE_SUCCESS_NAVIGATE_VITALS : State()
        object SAVE_FAILED : State()
    }

    sealed class Alert {
        data class InformDistrictNodalOfficer(val message: String) : Alert()
        data class IntensivePncMonitoring(val message: String) : Alert()
        data class HysterectomyNote(val message: String) : Alert()
    }

    private val _state = MutableLiveData<State>(State.IDLE)
    val state: LiveData<State> get() = _state

    private val _alert = MutableLiveData<Alert?>(null)
    val alert: LiveData<Alert?> get() = _alert

    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String> get() = _benName

    private val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String> get() = _benAgeGender

    private val _caseId = MutableLiveData<String>()
    val caseId: LiveData<String> get() = _caseId

    private val _recordExists = MutableLiveData<Boolean>(false)
    val recordExists: LiveData<Boolean> get() = _recordExists

    private val dataset = DeliveryOutcomeDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow
    val alertMessageFlow = dataset.alertErrorMessageFlow

    lateinit var deliveryOutcome: DeliveryOutcomeCache
        private set
    
    private val _deliveryOutcomeId = MutableLiveData<Long>(0L)
    val deliveryOutcomeId: LiveData<Long> get() = _deliveryOutcomeId

    init {
        viewModelScope.launch {
            try {
                val user = userRepo.getLoggedInUser()
                val userName = user?.userName ?: ""
                val saved = withContext(Dispatchers.IO) { deliveryOutcomeRepo.getDeliveryOutcome(patientID) }
                val lastAnc = withContext(Dispatchers.IO) { maternalHealthRepo.getLastAnc(patientID) }
                patientRepo.getPatientDisplay(patientID)?.let { ben ->
                    val patientName = "${ben.patient.firstName} ${ben.patient.lastName ?: ""}"
                    val patientAge = "${ben.patient.age} ${ben.ageUnit?.name} | ${ben.gender?.genderName}"
                    val caseId = ben.patient.patientID
                    val pwr = withContext(Dispatchers.IO) {
                        maternalHealthRepo.getSavedRegistrationRecord(patientID)
                    } ?: createFallbackPwr(
                        patient = ben.patient,
                        saved = saved,
                        userName = userName,
                        lmpFromAnc = lastAnc?.lmpDate
                    )

                    _benName.value = patientName
                    _benAgeGender.value = patientAge
                    _caseId.value = caseId

                    if (saved != null) {
                        deliveryOutcome = saved
                        _deliveryOutcomeId.value = saved.id
                        _recordExists.value = true
                        // Use full setUpPage method to show ALL fields (both delivery details and mother condition)
                        dataset.setUpPage(
                            pwr = pwr,
                            anc = lastAnc,
                            saved = saved,
                            patientName = patientName,
                            patientAge = patientAge,
                            caseId = caseId
                        )
                    } else {
                        deliveryOutcome = DeliveryOutcomeCache(
                            patientID = patientID,
                            isActive = true,
                            createdBy = userName,
                            updatedBy = userName,
                            syncState = SyncState.UNSYNCED
                        )
                        _recordExists.value = false
                        // Use full setUpPage method to show ALL fields (both delivery details and mother condition)
                        dataset.setUpPage(
                            pwr = pwr,
                            anc = lastAnc,
                            saved = null,
                            patientName = patientName,
                            patientAge = patientAge,
                            caseId = caseId
                        )
                    }
                } ?: run {
                    _benName.value = "Error: Patient not found"
                    _benAgeGender.value = ""
                    Timber.e("Patient not found for ID: $patientID")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error initializing delivery outcome form")
                _benName.value = "Error loading form"
                _benAgeGender.value = e.message ?: "Unknown error"
                _recordExists.value = false
            }
        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            dataset.updateList(formId, index)
            checkAndEmitAlerts(formId, index)
        }
    }

    fun clearAlertMessage() {
        viewModelScope.launch {
            dataset.resetErrorMessageFlow()
        }
    }

    /** Mother's condition form element id. */
    private val motherConditionId = 10 // ID in DeliveryOutcomeDataset
    /** Maternal complications form element id. */
    private val maternalComplicationsId = 11 // ID in DeliveryOutcomeDataset
    /** Index of Maternal Death in do_mother_condition_array. */
    private val motherConditionMaternalDeathIndex = 3
    /** Index of PPH in do_maternal_complications_array. */
    private val complicationPphIndex = 0
    /** Index of Uterine rupture in do_maternal_complications_array. */
    private val complicationUterineRuptureIndex = 4
    /** Index of Hysterectomy performed in do_maternal_complications_array. */
    private val complicationHysterectomyIndex = 8

    /**
     * Show alert only when the option that was just selected is the one that triggers that alert.
     * Prevents alerts from popping on other options (e.g. no Hysterectomy alert when selecting Retained placenta).
     * For checkboxes, index is positive when checked, negative when unchecked.
     */
    private fun checkAndEmitAlerts(formId: Int, index: Int) {
        _alert.value = when {
            // Only show "Inform District Nodal Officer" when user selected Maternal Death (index 3).
            formId == motherConditionId && index == motherConditionMaternalDeathIndex && dataset.isMaternalDeath() ->
                Alert.InformDistrictNodalOfficer(context.getString(R.string.do_inform_district_nodal_officer))
            // Only show "Intensive PNC" when user just checked PPH (0) or Uterine rupture (4).
            // Index is positive when checkbox is checked, negative when unchecked.
            formId == maternalComplicationsId &&
            (index == complicationPphIndex || index == complicationUterineRuptureIndex) &&
            index >= 0 && // Only when checking (not unchecking)
            dataset.hasPphOrUterineRupture() ->
                Alert.IntensivePncMonitoring(context.getString(R.string.do_alert_intensive_pnc))
            // Only show "Hysterectomy" when user just checked Hysterectomy performed (index 8).
            formId == maternalComplicationsId &&
            index == complicationHysterectomyIndex &&
            index >= 0 && // Only when checking (not unchecking)
            dataset.hasHysterectomy() ->
                Alert.HysterectomyNote(context.getString(R.string.do_hysterectomy_note))
            else -> null
        }
    }

    fun clearAlert() {
        _alert.value = null
    }

    fun saveForm() {
        // Guard against accessing deliveryOutcome before async initialization completes
        if (!::deliveryOutcome.isInitialized) {
            Timber.e("DeliveryOutcomeFormViewModel: saveForm() called before deliveryOutcome initialization")
            _state.postValue(State.SAVE_FAILED)
            return
        }
        
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)
                    dataset.mapValues(deliveryOutcome, 1)
                    // Ensure delivery date is saved (from form field or fallback to current time)
                    if (deliveryOutcome.dateOfDelivery == null) {
                        deliveryOutcome.dateOfDelivery = System.currentTimeMillis()
                    }
                    userRepo.getLoggedInUser()?.let { user ->
                        deliveryOutcome.updatedBy = user.userName
                        deliveryOutcome.updatedDate = System.currentTimeMillis()
                    }
                    val savedId = deliveryOutcomeRepo.saveDeliveryOutcome(deliveryOutcome)
                    if(savedId != null){
                        _deliveryOutcomeId.postValue(deliveryOutcome.id)
                        // Update patient status to Post Natal Mother so PNC list picks her up
                        updatePatientStatusToPostNatal()
                        // Retire upstream pregnancy state so she drops off PWR/ANC/e-PMSMA lists.
                        retirePregnancyLifecycle()
                        _state.postValue(State.SAVE_SUCCESS_NAVIGATE_VITALS)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "DeliveryOutcomeFormViewModel: Failed to save delivery outcome")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }

    private fun createFallbackPwr(
        patient: Patient,
        saved: DeliveryOutcomeCache?,
        userName: String,
        lmpFromAnc: Long?
    ): PregnantWomanRegistrationCache {
        val fallbackLmp = when {
            (lmpFromAnc ?: 0L) > 0L -> lmpFromAnc!!
            (saved?.dateOfDelivery ?: 0L) > 0L -> saved!!.dateOfDelivery!!
            patient.registrationDate?.time != null -> patient.registrationDate!!.time
            else -> System.currentTimeMillis()
        }
        return PregnantWomanRegistrationCache(
            patientID = patient.patientID,
            dateOfRegistration = patient.registrationDate?.time ?: fallbackLmp,
            lmpDate = fallbackLmp,
            active = true,
            createdBy = if (userName.isBlank()) "system" else userName,
            updatedBy = if (userName.isBlank()) "system" else userName,
            syncState = SyncState.SYNCED
        )
    }

    private suspend fun updatePatientStatusToPostNatal() {
        try {
            val patient = patientDao.getPatient(patientID)
            patient.statusOfWomanID = STATUS_POST_NATAL_MOTHER
            patient.syncState = SyncState.UNSYNCED
            patientDao.updatePatient(patient)
            Timber.d("Patient status updated to Post Natal Mother for patientID: $patientID")
        } catch (e: Exception) {
            Timber.e(e, "Failed to update patient status to Post Natal Mother")
        }
    }

    private suspend fun retirePregnancyLifecycle() {
        try {
            maternalHealthRepo.getSavedRegistrationRecord(patientID)?.let { pwr ->
                if (pwr.active) {
                    pwr.active = false
                    pwr.syncState = SyncState.UNSYNCED
                    maternalHealthRepo.updatePwr(pwr)
                }
            }

            val activeAnc = maternalHealthRepo.getAllActiveAncRecords(patientID)
            if (activeAnc.isNotEmpty()) {
                activeAnc.forEach {
                    it.pregnantWomanDelivered = true
                    it.isActive = false
                    it.processed = "U"
                    it.syncState = SyncState.UNSYNCED
                }
                maternalHealthRepo.updateAncRecord(activeAnc.toTypedArray())
            }

            // Clear pregnancy markers on every ECT row so the catch-all cannot leak
            // post-delivery women back into ANC/PWR lists via stale historical visits.
            ecrRepo.getAllECT(patientID).forEach { ect ->
                if (ect.isPregnant != null || ect.pregnancyTestResult != null) {
                    ect.isPregnant = null
                    ect.pregnancyTestResult = null
                    if (ect.processed != "N") ect.processed = "U"
                    ect.syncState = SyncState.UNSYNCED
                    ecrRepo.saveEct(ect)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to retire pregnancy lifecycle for patientID: $patientID")
            throw e
        }
    }
}
