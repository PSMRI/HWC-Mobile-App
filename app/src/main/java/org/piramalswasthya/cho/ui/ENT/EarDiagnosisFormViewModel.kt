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

    val patientID: String? = savedStateHandle["patientID"]
    val benVisitNo: Int? = savedStateHandle["benVisitNo"]

    private val dataset = EarDiagnosisDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    private lateinit var assessmentCache: EarDiagnosisAssessment

    init {
        viewModelScope.launch {
            try {
                val patient = loadPatientDetails(userRepo, patientRepo, patientID)
                    ?: return@launch

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

                dataset.setUpPage(savedRecord = existingRecord)

            } catch (e: Exception) {
                Timber.e(e, "Error initializing EarDiagnosisFormViewModel")
            }
        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        launchUpdateList(dataset, formId, index, "Error updating ear diagnosis form")
    }

    fun saveForm() {
        launchSave("Saving Ear Diagnosis failed") {
            check(::assessmentCache.isInitialized) { "Assessment cache not initialized" }
            dataset.mapValues(assessmentCache, 1)
            earDiagnosisRepo.saveAssessment(assessmentCache)
        }
    }
}