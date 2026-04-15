package org.piramalswasthya.cho.ui.home.rmncha.maternal_health

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.configuration.DeliveryOutcomeDataset
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.model.DeliveryOutcomeCache
import org.piramalswasthya.cho.model.PregnantWomanAncCache
import org.piramalswasthya.cho.model.PregnantWomanRegistrationCache
import org.piramalswasthya.cho.repositories.DeliveryOutcomeRepo
import org.piramalswasthya.cho.repositories.InfantRegRepo
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.utils.DateTimeUtil
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DeliveryOutcomeFormViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val preferenceDao: org.piramalswasthya.cho.database.shared_preferences.PreferenceDao,
    private val deliveryOutcomeRepo: DeliveryOutcomeRepo,
    private val infantRegRepo: InfantRegRepo,
    private val maternalHealthRepo: MaternalHealthRepo,
    private val patientRepo: PatientRepo,
    private val userRepo: UserRepo,
    private val patientDao: PatientDao
) : ViewModel() {

    companion object {
        const val STATUS_POST_NATAL_MOTHER = 3
    }

    enum class State { IDLE, LOADING, LOAD_FAILED, SAVING, SAVE_SUCCESS, SAVE_FAILED }

    private var patientID: String = ""

    fun setPatientID(id: String) {
        if (patientID.isEmpty() && id.isNotEmpty()) {
            patientID = id
            initializeForm()
        }
    }

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State> get() = _state

    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String> get() = _benName

    private val _benAge = MutableLiveData<String>()
    val benAge: LiveData<String> get() = _benAge

    private val _caseId = MutableLiveData<String>()
    val caseId: LiveData<String> get() = _caseId

    private val _recordExists = MutableLiveData<Boolean?>()
    val recordExists: LiveData<Boolean?> get() = _recordExists

    private val dataset = DeliveryOutcomeDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow
    val alertMessageFlow = dataset.alertErrorMessageFlow

    private var deliveryOutcome: DeliveryOutcomeCache? = null

    private fun initializeForm() {
        if (patientID.isEmpty()) return
        viewModelScope.launch {
            try {
                val user = userRepo.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in")
                val (patientName, patientAge, patientCaseId) = loadPatientDetails()
                val (pwr, anc) = loadPregnancyData()
                loadDeliveryOutcome(user)
                
                dataset.setUpPage(
                    pwr = pwr,
                    anc = anc,
                    saved = if (_recordExists.value == true) deliveryOutcome else null,
                    patientName = patientName,
                    patientAge = patientAge,
                    caseId = patientCaseId
                )
            } catch (e: Exception) {
                handleInitializationError(e)
            }
        }
    }

    private suspend fun loadPatientDetails(): Triple<String, String, String> {
        val patientDisplay = patientRepo.getPatientDisplay(patientID)
            ?: throw IllegalStateException("Patient not found: $patientID")
        val patient = patientDisplay.patient
        val patientName = "${patient.firstName ?: ""} ${patient.lastName ?: ""}".trim()
        val patientAge = patient.dob?.let { DateTimeUtil.calculateAgeString(it) } ?: "NA"
        val genderName = patientDisplay.gender?.genderName ?: "NA"
        val patientAgeGender = "$patientAge | $genderName"
        val patientCaseId = patient.patientID

        _benName.value = patientName
        _benAge.value = patientAgeGender
        _caseId.value = patientCaseId

        return Triple(patientName, patientAge, patientCaseId)
    }

    private suspend fun loadPregnancyData(): Pair<PregnantWomanRegistrationCache, PregnantWomanAncCache?> {
        val pwr = maternalHealthRepo.getSavedRegistrationRecord(patientID)
            ?: throw IllegalStateException("No pregnancy registration found")
        if (pwr.lmpDate <= 0) {
            throw IllegalStateException("Invalid LMP date in pregnancy registration")
        }
        val anc: PregnantWomanAncCache? = maternalHealthRepo.getLastAnc(patientID)
        return Pair(pwr, anc)
    }

    private suspend fun loadDeliveryOutcome(user: org.piramalswasthya.cho.model.UserDomain) {
        deliveryOutcome = DeliveryOutcomeCache(
            patientID = patientID,
            syncState = SyncState.UNSYNCED,
            createdBy = user.userName,
            updatedBy = user.userName,
            isActive = true
        )
        deliveryOutcomeRepo.getDeliveryOutcome(patientID)?.let {
            deliveryOutcome = it
            _recordExists.value = true
        } ?: run {
            _recordExists.value = false
        }
    }

    private suspend fun handleInitializationError(e: Exception) {
        Timber.e(e, "Error initializing delivery outcome form for patientID: $patientID")
        _state.value = State.LOAD_FAILED
        _benName.value = "Error loading form"
        _benAge.value = e.message ?: "Unknown error"
        if (_recordExists.value == null) _recordExists.value = false
        if (deliveryOutcome == null) {
            createFallbackDeliveryOutcome()
        }
    }

    private suspend fun createFallbackDeliveryOutcome() {
        try {
            val u = userRepo.getLoggedInUser()
            deliveryOutcome = DeliveryOutcomeCache(
                patientID = patientID,
                syncState = SyncState.UNSYNCED,
                createdBy = u?.userName ?: "Unknown",
                updatedBy = u?.userName ?: "Unknown",
                isActive = true
            )
        } catch (ex: Exception) {
            Timber.e(ex, "Failed to init delivery outcome cache")
        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch { dataset.updateList(formId, index) }
    }

    fun clearAlertMessage() {
        viewModelScope.launch { dataset.resetErrorMessageFlow() }
    }

    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val outcome = deliveryOutcome
                if (outcome == null) {
                    Timber.e("deliveryOutcome is null")
                    _state.postValue(State.SAVE_FAILED)
                    return@withContext
                }
                try {
                    _state.postValue(State.SAVING)
                    dataset.mapValues(outcome, 1)
                    deliveryOutcomeRepo.saveDeliveryOutcome(outcome)
                    ensureInfantPlaceholdersFromDeliveryOutcome(outcome)
                    // Update patient status to Post Natal Mother so PNC list picks her up
                    updatePatientStatusToPostNatal()
                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.e(e, "Error saving delivery outcome")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
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

    private suspend fun ensureInfantPlaceholdersFromDeliveryOutcome(outcome: DeliveryOutcomeCache) {
        try {
            val count = (outcome.deliveryOutcome ?: 0).takeIf { it > 0 }
                ?: (outcome.liveBirth ?: 0).takeIf { it > 0 }
                ?: 0
            if (count <= 0) return

            val patient = patientDao.getPatient(patientID)
            val motherName = listOfNotNull(patient.firstName, patient.lastName)
                .joinToString(" ")
                .trim()
            val userName = userRepo.getLoggedInUser()?.userName ?: outcome.updatedBy

            infantRegRepo.ensureInfantPlaceholdersForDeliveryOutcome(
                motherPatientID = patientID,
                motherName = motherName,
                infantCount = count,
                userName = userName
            )
        } catch (e: Exception) {
            Timber.w(e, "Failed creating infant placeholders from delivery outcome for patient=$patientID")
        }
    }
}
