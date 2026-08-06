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
import org.piramalswasthya.cho.ui.commons.BaseFormViewModel
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
) : BaseFormViewModel() {

    /* -------------------- BEN DETAILS -------------------- */

    val patientID: String? = savedStateHandle["patientID"]
    val benVisitNo: Int? = savedStateHandle["benVisitNo"]

    /* -------------------- DATASET -------------------- */

    private val dataset =
        NoseDiagnosisDataset(context, preferenceDao.getCurrentLanguage())

    val formList = dataset.listFlow

    private lateinit var assessmentCache: NoseDiagnosisAssessment

    init {
        setupDatasetCallbacks()

        viewModelScope.launch {
            try {
                val patient = loadPatientDetails(userRepo, patientRepo, patientID)
                    ?: return@launch

                if (benVisitNo == null) {
                    Timber.e("Missing benVisitNo ($benVisitNo)")
                    return@launch
                }

                val existingRecord = noseDiagnosisRepo.getAssessmentByPatientIdAndVisitNo(
                    patient.patient.patientID,
                    benVisitNo
                )

                assessmentCache = existingRecord ?: NoseDiagnosisAssessment(
                    patientId = patient.patient.patientID,
                    benVisitNo = benVisitNo
                )

                dataset.setUpPage(savedRecord = existingRecord)

            } catch (e: Exception) {
                Timber.e(e, "Error initializing NoseDiagnosisFormViewModel")
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
        Timber.d("updateListOnValueChanged: formId=$formId, index=$index")
        launchUpdateList(dataset, formId, index, "Error updating nose diagnosis form")
    }

    fun saveForm() {
        launchSave("Saving Nose Diagnosis failed") {
            check(::assessmentCache.isInitialized) { "Assessment cache not initialized" }
            dataset.mapValues(assessmentCache, 1)
            noseDiagnosisRepo.saveAssessment(assessmentCache)
            Timber.d("Nose Diagnosis saved successfully")
        }
    }
}

