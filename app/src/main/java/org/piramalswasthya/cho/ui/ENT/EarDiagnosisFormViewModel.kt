package org.piramalswasthya.cho.ui.ENT

import android.content.Context
import androidx.lifecycle.SavedStateHandle
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
import org.piramalswasthya.cho.ui.commons.BaseFormViewModel
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
) : BaseFormViewModel() {

    /* -------------------- BEN DETAILS -------------------- */

    val patientID: String? = savedStateHandle["patientID"]
    val benVisitNo: Int? = savedStateHandle["benVisitNo"]

    /* -------------------- DATASET -------------------- */

    private val dataset =
        EarDiagnosisDataset(context, preferenceDao.getCurrentLanguage())

    val formList = dataset.listFlow

    private lateinit var assessmentCache: EarDiagnosisAssessment

    /* -------------------- INIT -------------------- */

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
                Timber.e(e, "Error initializing EarDiagnosisFormViewModel")
            }
        }
    }

    /* -------------------- DATASET CALLBACKS -------------------- */

    private fun setupDatasetCallbacks() {
        dataset.onShowAlert = { message ->
            Timber.d("Dataset requested alert: $message")
            _showAlert.postValue(message)
        }
    }

    /* -------------------- FORM EVENTS -------------------- */

    fun updateListOnValueChanged(formId: Int, index: Int) {
        Timber.d("updateListOnValueChanged: formId=$formId, index=$index")
        viewModelScope.launch {
            try {
                dataset.updateList(formId, index)
            } catch (e: Exception) {
                Timber.e(e, "Error updating ear diagnosis form")
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

                Timber.d("Ear Diagnosis saved successfully")

                _state.postValue(State.SAVE_SUCCESS)

            } catch (e: Exception) {
                Timber.e(e, "Saving Ear Diagnosis failed")
                _state.postValue(State.SAVE_FAILED)
            }
        }
    }
}