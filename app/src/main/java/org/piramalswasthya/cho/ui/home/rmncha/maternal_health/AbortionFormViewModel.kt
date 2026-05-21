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

    private val _loadError = MutableLiveData<String?>(null)
    val loadError: LiveData<String?> = _loadError

    private val dataset = PregnantWomanAncAbortionDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow
    val isEditable: LiveData<Boolean> get() = _isEditable
    private val _isEditable = MutableLiveData(actionType.equals("Add", true))

    private lateinit var ancCache: PregnantWomanAncCache

    @Volatile
    private var loadInProgress: Boolean = false

    init {
        loadForm()
    }

    /** Re-runs the form load. Safe to call multiple times (e.g. from
     *  Fragment.onResume) — concurrent calls are coalesced. Used to recover
     *  from a load that failed transiently (e.g. a background pull hadn't
     *  finished populating the patient row yet). */
    fun reload() {
        if (_formReady.value == true) return
        loadForm()
    }

    private fun loadForm() {
        if (loadInProgress) return
        loadInProgress = true
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    _loadError.postValue(null)
                    Timber.d("AbortionForm load: start patientId=$patientId")

                    val user = userRepo.getLoggedInUser()
                    if (user == null) {
                        Timber.w("AbortionForm load: no logged-in user")
                        _loadError.postValue("Session expired. Please sign in again.")
                        return@withContext
                    }
                    Timber.d("AbortionForm load: user ok")

                    val patient = try {
                        patientRepo.getPatientDisplay(patientId)
                    } catch (e: Exception) {
                        Timber.e(e, "AbortionForm load: getPatientDisplay threw")
                        _loadError.postValue("Patient record could not be loaded.")
                        return@withContext
                    }
                    _benName.postValue("${patient.patient.firstName} ${patient.patient.lastName ?: ""}".trim())
                    _benAgeGender.postValue("${patient.patient.age} ${patient.ageUnit?.name} | ${patient.gender?.genderName}")
                    Timber.d("AbortionForm load: patient ok")

                    val allAnc = maternalHealthRepo.getAllActiveAncRecords(patientId) +
                        listOfNotNull(maternalHealthRepo.getLastAnc(patientId))
                    val aborted = allAnc
                        .distinctBy { it.id }
                        .filter { it.isAborted && it.abortionDate != null }
                        .maxByOrNull { it.abortionDate ?: 0L }
                    if (aborted == null) {
                        Timber.w("AbortionForm load: no aborted ANC record found for $patientId (scanned ${allAnc.size})")
                        _loadError.postValue("No abortion record found for this patient yet.")
                        return@withContext
                    }
                    Timber.d("AbortionForm load: aborted found id=${aborted.id}")

                    val reg = maternalHealthRepo.getSavedRegistrationRecord(patientId)
                        ?: PregnantWomanRegistrationCache(
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
                    Timber.d("AbortionForm load: complete patientId=$patientId")
                }
            } catch (e: Exception) {
                Timber.e(e, "AbortionForm load: uncaught failure for patientId=$patientId")
                _loadError.postValue("Couldn't load form: ${e.message ?: e.javaClass.simpleName}")
            } finally {
                loadInProgress = false
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
            // Clear pregnancy markers and keep (or recreate) one active ECT row,
            // so the EC-tracking DAO (filters on isActive = 1) still surfaces her
            // after statusOfWomanID is reset to 1.
            val createdBy = userRepo.getLoggedInUser()?.userName ?: "system"
            ecrRepo.resetEctAfterAbortion(patientId, createdBy)
        } catch (e: Exception) {
            Timber.e(e, "Failed to retire pregnancy lifecycle for patientID: $patientId")
            throw e
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
