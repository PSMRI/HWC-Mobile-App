package org.piramalswasthya.cho.ui.commons.maternal_health.newborn_outcome

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.configuration.NewbornOutcomeDataset
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.NeonateDetailsCache
import org.piramalswasthya.cho.model.NewbornOutcomeCache
import org.piramalswasthya.cho.repositories.NewbornOutcomeRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NewbornOutcomeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val newbornOutcomeRepo: NewbornOutcomeRepo,
    private val patientRepo: PatientRepo
) : ViewModel() {

    val patientID = NewbornOutcomeFragmentArgs.fromSavedStateHandle(savedStateHandle).patientID

    sealed class State {
        object IDLE : State()
        object SAVING : State()
        object SAVE_SUCCESS : State()
        object SAVE_FAILED : State()
    }

    private val _state = MutableLiveData<State>(State.IDLE)
    val state: LiveData<State>
        get() = _state

    private val _patientName = MutableLiveData<String>()
    val patientName: LiveData<String>
        get() = _patientName

    private val _patientAgeGender = MutableLiveData<String>()
    val patientAgeGender: LiveData<String>
        get() = _patientAgeGender

    private val _recordExists = MutableLiveData<Boolean>()
    val recordExists: LiveData<Boolean>
        get() = _recordExists

    private val dataset = NewbornOutcomeDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    private lateinit var newbornOutcome: NewbornOutcomeCache
    private var neonatesList = mutableListOf<NeonateDetailsCache>()

    init {
        viewModelScope.launch {
            try {
                val user = preferenceDao.getLoggedInUser()!!

                patientRepo.getPatient(patientID).also { patient ->
                    _patientName.value = patient.parentName ?: "${patient.firstName} ${patient.lastName}"
                    _patientAgeGender.value = "${patient.age ?: ""} | ${patient.genderID ?: ""}"

                    val currentTime = System.currentTimeMillis()
                    newbornOutcome = NewbornOutcomeCache(
                        motherPatientID = patient.patientID,
                        numberOfNeonates = 1,
                        syncState = SyncState.UNSYNCED,
                        createdBy = user.userName,
                        createdDate = currentTime,
                        updatedBy = user.userName,
                        updatedDate = currentTime,
                        isActive = true
                    )

                    // Initialize with one baby by default
                    neonatesList.add(
                        NeonateDetailsCache(
                            newbornOutcomeID = 0,
                            neonateIndex = 0,
                            neonateID = newbornOutcomeRepo.generateNeonateID(patientID, 0),
                            outcomeAtBirth = "Live Birth",
                            currentStatusOfBaby = "Healthy and with mother",
                            createdBy = user.userName,
                            createdDate = currentTime,
                            updatedBy = user.userName,
                            updatedDate = currentTime,
                            syncState = SyncState.UNSYNCED
                        )
                    )
                }

                // Check for existing record (use first() to get single value, not collect forever)
                val existing = newbornOutcomeRepo.getActiveNewbornOutcomeByPatientID(patientID).firstOrNull()
                if (existing != null) {
                    newbornOutcome = existing
                    _recordExists.value = true
                    // Load neonates
                    val existingNeonates = newbornOutcomeRepo.getNeonateDetailsByOutcomeID(existing.id).firstOrNull()
                    if (existingNeonates != null && existingNeonates.isNotEmpty()) {
                        neonatesList.clear()
                        neonatesList.addAll(existingNeonates)
                    }
                } else {
                    _recordExists.value = false
                }

                // Setup form page with existing data
                dataset.setUpPage(
                    existingData = if (recordExists.value == true) newbornOutcome else null,
                    neonatesList = if (recordExists.value == true && neonatesList.isNotEmpty()) neonatesList else null,
                    patientID = patientID
                )
            } catch (e: Exception) {
                Timber.e(e, "Error initializing NewbornOutcomeViewModel")
                _recordExists.value = false
                // Still setup page even if error
                dataset.setUpPage(
                    existingData = null,
                    neonatesList = null,
                    patientID = patientID
                )
            }
        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }
    }

    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)

                    // Map form values to newborn outcome cache
                    dataset.mapValues(newbornOutcome, 0)

                    // Map Baby 1 fields to first neonate
                    if (neonatesList.isNotEmpty()) {
                        dataset.mapBaby1ToNeonate(neonatesList[0])
                    }

                    // Save complete newborn outcome
                    newbornOutcomeRepo.saveCompleteNewbornOutcome(newbornOutcome, neonatesList)

                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.e(e, "Error saving newborn outcome")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }

    fun getBirthWeight(): Double? {
        return neonatesList.firstOrNull()?.birthWeight
    }

    fun hasComplications(): Boolean? {
        val complications = neonatesList.firstOrNull()?.newbornComplications
        return complications != null && complications != "None" && complications.isNotBlank()
    }
}
