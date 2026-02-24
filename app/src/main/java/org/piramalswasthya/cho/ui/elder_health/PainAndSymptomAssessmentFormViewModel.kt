package org.piramalswasthya.cho.ui.elder_health

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.configuration.PainAndSymptomAssessmentDataset
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.PainAndSymptomAssessment
import org.piramalswasthya.cho.repositories.PainAndSymptomAssessmentRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.UserRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PainAndSymptomAssessmentFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val patientRepo: PatientRepo,
    private val userRepo: UserRepo,
    private val painAssessmentRepo: PainAndSymptomAssessmentRepo
) : ViewModel() {

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    // ---------------- UI EVENTS ----------------

    private val _showAlert = MutableLiveData<String?>()
    val showAlert: LiveData<String?> get() = _showAlert

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State> get() = _state

    // ---------------- BEN DETAILS ----------------

    val patientID: String? = savedStateHandle["patientID"]
    val benVisitNo: Int? = savedStateHandle["benVisitNo"]

    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String> get() = _benName

    private val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String> get() = _benAgeGender

    // ---------------- DATASET ----------------

    private val dataset =
        PainAndSymptomAssessmentDataset(context, preferenceDao.getCurrentLanguage())

    val formList = dataset.listFlow

    private lateinit var assessmentCache: PainAndSymptomAssessment

    // ---------------- INIT ----------------

    init {
        viewModelScope.launch {
            try {
                val user = userRepo.getLoggedInUser()
                if (user == null) {
                    Timber.e("No logged in user found")
                    return@launch
                }

                val patient = patientID?.let { patientRepo.getPatientDisplay(it) }
                if (patient == null) {
                    Timber.e("Patient not found for ID: $patientID")
                    return@launch
                }

                _benName.value =
                    "${patient.patient.firstName} ${patient.patient.lastName ?: ""}"

                _benAgeGender.value =
                    "${patient.patient.age} ${patient.ageUnit?.name} | ${patient.gender?.genderName}"

                val existingRecord = if (benVisitNo != null) {
                    painAssessmentRepo.getAssessmentByPatientIdAndVisitNo(
                        patient.patient.patientID,
                        benVisitNo
                    )
                } else {
                    painAssessmentRepo.getAssessmentByPatientId(
                        patient.patient.patientID
                    )
                }

                assessmentCache = existingRecord ?: PainAndSymptomAssessment(
                    patientID = patient.patient.patientID,
                    benVisitNo = benVisitNo
                )

                setupDatasetCallbacks()

                dataset.setUpPage(
                    savedRecord = existingRecord
                )

            } catch (e: Exception) {
                Timber.e(e, "Error initializing PainAndSymptomAssessmentFormViewModel")
            }
        }
    }

    // ---------------- DATASET CALLBACKS ----------------

    private fun setupDatasetCallbacks() {
        dataset.onShowAlert = { message ->
            Timber.d("Dataset requested alert: $message")
            _showAlert.postValue(message)
        }
    }

    // ---------------- FORM EVENTS ----------------

    fun updateListOnValueChanged(formId: Int, index: Int) {
        Timber.d("updateListOnValueChanged: formId=$formId, index=$index")
        viewModelScope.launch {
            try {
                dataset.updateList(formId, index)
            } catch (e: Exception) {
                Timber.e(e, "Error updating Pain & Symptom form")
            }
        }
    }

    fun saveForm() {
        viewModelScope.launch {
            try {
                _state.postValue(State.SAVING)

                check(this@PainAndSymptomAssessmentFormViewModel::assessmentCache.isInitialized) {
                    "Assessment cache not initialized"
                }

                dataset.mapValues(assessmentCache, 1)

                painAssessmentRepo.saveAssessment(assessmentCache)

                Timber.d("Pain & Symptom Assessment saved successfully")

                _state.postValue(State.SAVE_SUCCESS)

            } catch (e: Exception) {
                Timber.e(e, "Saving Pain & Symptom Assessment failed")
                _state.postValue(State.SAVE_FAILED)
            }
        }
    }

    fun clearAlert() {
        _showAlert.value = null
    }
}