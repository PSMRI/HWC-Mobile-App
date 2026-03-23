package org.piramalswasthya.cho.ui.ENT

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.configuration.ThroatDiagnosisDataset
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.ThroatDiagnosisAssessment
import org.piramalswasthya.cho.repositories.ThroatDiagnosisRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.UserRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ThroatDiagnosisFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val patientRepo: PatientRepo,
    private val userRepo: UserRepo,
    private val throatDiagnosisRepo: ThroatDiagnosisRepo
) : ViewModel() {

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    /* -------------------- ALERT & STATE -------------------- */

    private val _showAlert = MutableLiveData<String?>()
    val showAlert: LiveData<String?> get() = _showAlert

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State> get() = _state

    private val _triggerMultiSelect = MutableLiveData<MultiSelectData?>()
    val triggerMultiSelect: LiveData<MultiSelectData?> get() = _triggerMultiSelect

    data class MultiSelectData(
        val formId: Int,
        val title: String,
        val items: Array<String>,
        val selectedItems: BooleanArray
    )

    /* -------------------- BEN DETAILS -------------------- */

    val patientID: String? = savedStateHandle["patientID"]

    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String> get() = _benName

    private val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String> get() = _benAgeGender

    /* -------------------- DATASET -------------------- */

    private val dataset =
        ThroatDiagnosisDataset(context, preferenceDao.getCurrentLanguage())

    val formList = dataset.listFlow

    private lateinit var assessmentCache: ThroatDiagnosisAssessment

    init {
        viewModelScope.launch {
            try {
                val user = userRepo.getLoggedInUser()
                if (user == null) {
                    Timber.Forest.e("No logged in user found")
                    return@launch
                }

                val patient = patientID?.let { patientRepo.getPatientDisplay(it) }
                if (patient == null) {
                    Timber.Forest.e("Patient not found for ID: $patientID")
                    return@launch
                }

                _benName.value =
                    "${patient.patient.firstName} ${patient.patient.lastName ?: ""}"

                _benAgeGender.value =
                    "${patient.patient.age} ${patient.ageUnit?.name} | ${patient.gender?.genderName}"

                val existingRecord =
                    throatDiagnosisRepo.getAssessmentByPatientId(patient.patient.patientID)

                assessmentCache = existingRecord ?: ThroatDiagnosisAssessment(
                    patientID = patient.patient.patientID,
                    benVisitNo = null
                )

                setupDatasetCallbacks()

                dataset.setUpPage(
                    savedRecord = existingRecord
                )

            } catch (e: Exception) {
                Timber.Forest.e(e, "Error initializing ThroatDiagnosisFormViewModel")
            }
        }
    }

    private fun setupDatasetCallbacks() {
        dataset.onShowAlert = { message ->
            Timber.Forest.d("Dataset requested alert: $message")
            _showAlert.postValue(message)
        }

        dataset.onTriggerMultiSelect = { formId, title, items, selectedItems ->
            _triggerMultiSelect.value =
                MultiSelectData(formId, title, items, selectedItems)
        }
    }

    /* -------------------- FORM EVENTS -------------------- */

    fun updateListOnValueChanged(formId: Int, index: Int) {
        Timber.Forest.d("updateListOnValueChanged: formId=$formId, index=$index")
        viewModelScope.launch {
            try {
                dataset.updateList(formId, index)
            } catch (e: Exception) {
                Timber.Forest.e(e, "Error updating throat diagnosis form")
            }
        }
    }

    fun onMultiSelectClick(formId: Int) {
        Timber.Forest.d("onMultiSelectClick: formId=$formId")
        dataset.triggerMultiSelect(formId)
    }

    fun updateMultiSelectValue(formId: Int, selectedItems: List<String>) {
        Timber.Forest.d("updateMultiSelectValue: formId=$formId, selected=$selectedItems")
        viewModelScope.launch {
            try {
                dataset.updateMultiSelectValue(formId, selectedItems)
            } catch (e: Exception) {
                Timber.Forest.e(e, "Error updating multi-select value")
            }
        }
    }

    fun onMultiSelectDialogDismissed() {
        _triggerMultiSelect.value = null
    }

    fun saveForm() {
        viewModelScope.launch {
            try {
                _state.postValue(State.SAVING)

                check(this@ThroatDiagnosisFormViewModel::assessmentCache.isInitialized) {
                    "Assessment cache not initialized"
                }

                dataset.mapValues(assessmentCache, 1)

                throatDiagnosisRepo.saveAssessment(assessmentCache)

                Timber.Forest.d("Throat Diagnosis saved successfully")

                _state.postValue(State.SAVE_SUCCESS)

            } catch (e: Exception) {
                Timber.Forest.e(e, "Saving Throat Diagnosis failed")
                _state.postValue(State.SAVE_FAILED)
            }
        }
    }

    fun clearAlert() {
        _showAlert.value = null
    }
}