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
import org.piramalswasthya.cho.configuration.NoseDiagnosisDataset
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.NoseDiagnosisAssessment
import org.piramalswasthya.cho.repositories.NoseDiagnosisRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.UserRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NoseDiagnosisFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val patientRepo: PatientRepo,
    private val userRepo: UserRepo,
    private val noseDiagnosisRepo: NoseDiagnosisRepo
) : ViewModel() {

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    /* -------------------- ALERT & STATE -------------------- */

    private val _showAlert = MutableLiveData<String?>()
    val showAlert: LiveData<String?> get() = _showAlert

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State> get() = _state

    /* -------------------- BEN DETAILS -------------------- */

    val patientID: String? = savedStateHandle["patientID"]
    val benVisitNo: Int? = savedStateHandle["benVisitNo"]

    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String> get() = _benName

    private val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String> get() = _benAgeGender

    /* -------------------- DATASET -------------------- */

    private val dataset =
        NoseDiagnosisDataset(context, preferenceDao.getCurrentLanguage())

    val formList = dataset.listFlow

    private lateinit var assessmentCache: NoseDiagnosisAssessment

    init {
        viewModelScope.launch {
            try {
                val user = userRepo.getLoggedInUser()
                if (user == null) {
                    Timber.Forest.e("No logged in user found")
                    return@launch
                }

                if (patientID == null || benVisitNo == null) {
                    Timber.Forest.e("Missing patientID ($patientID) or benVisitNo ($benVisitNo)")
                    return@launch
                }

                val patient = patientRepo.getPatientDisplay(patientID)
                if (patient == null) {
                    Timber.Forest.e("Patient not found for ID: $patientID")
                    return@launch
                }

                _benName.value =
                    "${patient.patient.firstName} ${patient.patient.lastName ?: ""}"

                _benAgeGender.value =
                    "${patient.patient.age} ${patient.ageUnit?.name} | ${patient.gender?.genderName}"

                val existingRecord = noseDiagnosisRepo.getAssessmentByPatientIdAndVisitNo(
                    patientID,
                    benVisitNo
                )

                assessmentCache = existingRecord ?: NoseDiagnosisAssessment(
                    patientID = patient.patient.patientID,
                    benVisitNo = benVisitNo
                )

                setupDatasetCallbacks()

                dataset.setUpPage(
                    savedRecord = existingRecord
                )

            } catch (e: Exception) {
                Timber.Forest.e(e, "Error initializing NoseDiagnosisFormViewModel")
            }
        }
    }

    private fun setupDatasetCallbacks() {
        dataset.onShowAlert = { message ->
            Timber.Forest.d("Dataset requested alert: $message")
            _showAlert.postValue(message)
        }
    }

    /* -------------------- FORM EVENTS -------------------- */

    fun updateListOnValueChanged(formId: Int, index: Int) {
        Timber.Forest.d("updateListOnValueChanged: formId=$formId, index=$index")
        viewModelScope.launch {
            try {
                dataset.updateList(formId, index)
            } catch (e: Exception) {
                Timber.Forest.e(e, "Error updating nose diagnosis form")
            }
        }
    }

    fun saveForm() {
        viewModelScope.launch {
            try {
                _state.postValue(State.SAVING)

                check(this@NoseDiagnosisFormViewModel::assessmentCache.isInitialized) {
                    "Assessment cache not initialized"
                }

                dataset.mapValues(assessmentCache, 1)

                noseDiagnosisRepo.saveAssessment(assessmentCache)

                Timber.Forest.d("Nose Diagnosis saved successfully")

                _state.postValue(State.SAVE_SUCCESS)

            } catch (e: Exception) {
                Timber.Forest.e(e, "Saving Nose Diagnosis failed")
                _state.postValue(State.SAVE_FAILED)
            }
        }
    }

    fun clearAlert() {
        _showAlert.value = null
    }
}
