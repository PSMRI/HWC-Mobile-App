package org.piramalswasthya.cho.ui.elder_health

import android.content.Context
import androidx.lifecycle.SavedStateHandle
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
import org.piramalswasthya.cho.ui.commons.BaseFormViewModel
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
) : BaseFormViewModel() {

    val patientID: String? = savedStateHandle["patientID"]
    val benVisitNo: Int? = savedStateHandle["benVisitNo"]

    private val dataset =
        PainAndSymptomAssessmentDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    private lateinit var assessmentCache: PainAndSymptomAssessment

    init {
        viewModelScope.launch {
            try {
                val patient = loadPatientDetails(userRepo, patientRepo, patientID)
                    ?: return@launch

                val existingRecord = if (benVisitNo != null) {
                    painAssessmentRepo.getAssessmentByPatientIdAndVisitNo(
                        patient.patient.patientID, benVisitNo
                    )
                } else {
                    painAssessmentRepo.getAssessmentByPatientId(patient.patient.patientID)
                }

                assessmentCache = existingRecord ?: PainAndSymptomAssessment(
                    patientID = patient.patient.patientID,
                    benVisitNo = benVisitNo
                )

                bindAlertToDataset(dataset)

                dataset.setUpPage(savedRecord = existingRecord)

            } catch (e: Exception) {
                Timber.e(e, "Error initializing PainAndSymptomAssessmentFormViewModel")
            }
        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
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
                check(::assessmentCache.isInitialized) { "Assessment cache not initialized" }
                dataset.mapValues(assessmentCache, 1)
                painAssessmentRepo.saveAssessment(assessmentCache)
                _state.postValue(State.SAVE_SUCCESS)
            } catch (e: Exception) {
                Timber.e(e, "Saving Pain & Symptom Assessment failed")
                _state.postValue(State.SAVE_FAILED)
            }
        }
    }
}