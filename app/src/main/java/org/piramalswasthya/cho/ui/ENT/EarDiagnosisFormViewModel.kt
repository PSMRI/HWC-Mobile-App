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
import org.piramalswasthya.cho.configuration.EarDiagnosisDataset
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.EarDiagnosisAssessment
import org.piramalswasthya.cho.repositories.EarDiagnosisRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.UserRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EarDiagnosisFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val patientRepo: PatientRepo,
    private val userRepo: UserRepo,
    private val earDiagnosisRepo: EarDiagnosisRepo
) : ViewModel() {

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }


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


    private val dataset =
        EarDiagnosisDataset(context, preferenceDao.getCurrentLanguage())

    val formList = dataset.listFlow

    private lateinit var assessmentCache: EarDiagnosisAssessment

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

                val existingRecord = if (benVisitNo != null) {
                    earDiagnosisRepo.getAssessmentByPatientIdAndVisitNo(
                        patient.patient.patientID, benVisitNo
                    )
                } else {
                    earDiagnosisRepo.getAssessmentByPatientId(patient.patient.patientID)
                }

                assessmentCache = existingRecord ?: EarDiagnosisAssessment(
                    patientID = patient.patient.patientID,
                    benVisitNo = benVisitNo
                )

                setupDatasetCallbacks()

                dataset.setUpPage(
                    savedRecord = existingRecord
                )

            } catch (e: Exception) {
                Timber.Forest.e(e, "Error initializing EarDiagnosisFormViewModel")
            }
        }
    }

    private fun setupDatasetCallbacks() {
        dataset.onShowAlert = { message ->
            Timber.Forest.d("Dataset requested alert: $message")
            _showAlert.postValue(message)
        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        Timber.Forest.d("updateListOnValueChanged: formId=$formId, index=$index")
        viewModelScope.launch {
            try {
                dataset.updateList(formId, index)
            } catch (e: Exception) {
                Timber.Forest.e(e, "Error updating ear diagnosis form")
            }
        }
    }

    fun saveForm() {
        viewModelScope.launch {
            try {
                _state.postValue(State.SAVING)

                check(this@EarDiagnosisFormViewModel::assessmentCache.isInitialized) {
                    "Assessment cache not initialized"
                }

                dataset.mapValues(assessmentCache, 1)

                earDiagnosisRepo.saveAssessment(assessmentCache)

                Timber.Forest.d("Ear Diagnosis saved successfully")

                _state.postValue(State.SAVE_SUCCESS)

            } catch (e: Exception) {
                Timber.Forest.e(e, "Saving Ear Diagnosis failed")
                _state.postValue(State.SAVE_FAILED)
            }
        }
    }

    fun clearAlert() {
        _showAlert.value = null
    }
}