package org.piramalswasthya.cho.ui.home.rmncha.maternal_health

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.configuration.PregnantWomanAncAbortionDataset
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.PregnantWomanAncCache
import org.piramalswasthya.cho.model.PregnantWomanRegistrationCache
import org.piramalswasthya.cho.repositories.EcrRepo
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.UserRepo
import timber.log.Timber

class AbortionFormViewModel(
    private val patientId: String,
    private val actionType: String,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val maternalHealthRepo: MaternalHealthRepo,
    private val patientRepo: PatientRepo,
    private val userRepo: UserRepo,
    private val ecrRepo: EcrRepo
) : ViewModel() {

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State> = _state

    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String> = _benName
    private val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String> = _benAgeGender

    private val _recordExists = MutableLiveData<Boolean>(false)
    val recordExists: LiveData<Boolean> = _recordExists

    private val _formReady = MutableLiveData(false)
    val formReady: LiveData<Boolean> = _formReady

    private val dataset = PregnantWomanAncAbortionDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow
    val isEditable: LiveData<Boolean> get() = _isEditable
    private val _isEditable = MutableLiveData(actionType.equals("Add", true))

    private lateinit var ancCache: PregnantWomanAncCache

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val user = userRepo.getLoggedInUser() ?: return@withContext
                val patient = patientRepo.getPatientDisplay(patientId) ?: return@withContext
                _benName.postValue("${patient.patient.firstName} ${patient.patient.lastName ?: ""}".trim())
                _benAgeGender.postValue("${patient.patient.age} ${patient.ageUnit?.name} | ${patient.gender?.genderName}")

                val allAnc = maternalHealthRepo.getAllActiveAncRecords(patientId) + listOfNotNull(maternalHealthRepo.getLastAnc(patientId))
                val aborted = allAnc
                    .distinctBy { it.id }
                    .filter { it.isAborted && it.abortionDate != null }
                    .maxByOrNull { it.abortionDate ?: 0L }
                    ?: return@withContext

                val reg = maternalHealthRepo.getSavedRegistrationRecord(patientId) ?: PregnantWomanRegistrationCache(
                    patientID = patientId,
                    lmpDate = aborted.lmpDate ?: 0L,
                    createdBy = user.userName,
                    updatedBy = user.userName,
                    syncState = SyncState.UNSYNCED,
                    active = false
                )

                ancCache = aborted.copy(updatedBy = user.userName)
                _recordExists.postValue(ancCache.terminationDoneBy != null || ancCache.methodOfTermination != null)
                if (actionType.equals("View", true)) {
                    _isEditable.postValue(false)
                }
                dataset.setUpPage(reg, ancCache)
                _formReady.postValue(true)
            }
        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }
    }

    fun enableEditMode() {
        _isEditable.value = true
    }

    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)
                    dataset.mapValues(ancCache, 1)
                    if (ancCache.processed != "N") ancCache.processed = "U"
                    ancCache.syncState = SyncState.UNSYNCED
                    ancCache.updatedDate = System.currentTimeMillis()
                    maternalHealthRepo.persistAncRecord(ancCache)
                    // Idempotent retirement: matches PwAncFormViewModel abortion branch so re-saves
                    // from this dedicated form converge state for women aborted before this fix.
                    retirePregnancyLifecycle()
                    _state.postValue(State.SAVE_SUCCESS)
                } catch (_: Exception) {
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }

    private suspend fun retirePregnancyLifecycle() {
        try {
            maternalHealthRepo.getSavedRegistrationRecord(patientId)?.let { pwr ->
                if (pwr.active) {
                    pwr.active = false
                    if (pwr.processed != "N") pwr.processed = "U"
                    pwr.syncState = SyncState.UNSYNCED
                    maternalHealthRepo.persistRegisterRecord(pwr)
                }
            }

            val activeAnc = maternalHealthRepo.getAllActiveAncRecords(patientId)
            if (activeAnc.isNotEmpty()) {
                activeAnc.forEach {
                    it.isActive = false
                    if (it.processed != "N") it.processed = "U"
                    it.syncState = SyncState.UNSYNCED
                }
                maternalHealthRepo.updateAncRecord(activeAnc.toTypedArray())
            }

            val patient = patientRepo.getPatient(patientId)
            if (patient.statusOfWomanID != 1) {
                patient.statusOfWomanID = 1 // Eligible Couple — fresh start after abortion
                patient.syncState = SyncState.UNSYNCED
                patientRepo.updateRecord(patient)
            }
            // Deactivate every ECT row + clear pregnancy markers so EC list shows
            // her as "needs visit" and the ECT catch-all does not leak her back.
            ecrRepo.getAllECT(patientId).forEach { ect ->
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
            Timber.e(e, "Failed to retire pregnancy lifecycle for patientID: $patientId")
        }
    }

    fun getWeeksOfPregnancyIndex(): Int = dataset.getWeeksOfPregnancyIndex()

    fun setImageUriToFormElement(formId: Int, uri: String) {
        dataset.setImageUriToFormElement(formId, uri)
    }

    fun getImageFieldIndex(formId: Int): Int = dataset.getImageFieldIndex(formId)

    fun getAbortionImageFieldValue(formId: Int): String? = dataset.getAbortionImageFieldValue(formId)

    class Factory(
        private val patientId: String,
        private val actionType: String,
        private val preferenceDao: PreferenceDao,
        private val context: Context,
        private val maternalHealthRepo: MaternalHealthRepo,
        private val patientRepo: PatientRepo,
        private val userRepo: UserRepo,
        private val ecrRepo: EcrRepo
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AbortionFormViewModel(
                patientId,
                actionType,
                preferenceDao,
                context,
                maternalHealthRepo,
                patientRepo,
                userRepo,
                ecrRepo
            ) as T
        }
    }
}
