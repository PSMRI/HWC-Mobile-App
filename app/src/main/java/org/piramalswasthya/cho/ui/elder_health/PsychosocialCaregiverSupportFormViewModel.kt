package org.piramalswasthya.cho.ui.elder_health

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.configuration.PsychosocialCaregiverSupportDataset
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.PsychosocialCaregiverSupport
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.PsychosocialCaregiverSupportRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.ui.commons.BaseFormViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PsychosocialCaregiverSupportFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val patientRepo: PatientRepo,
    private val userRepo: UserRepo,
    private val psychosocialRepo: PsychosocialCaregiverSupportRepo
) : BaseFormViewModel() {

    val patientID: String? = savedStateHandle["patientID"]
    val benVisitNo: Int? = savedStateHandle["benVisitNo"]

    private val dataset =
        PsychosocialCaregiverSupportDataset(context, preferenceDao.getCurrentLanguage())

    val formList = dataset.listFlow

    private lateinit var assessmentCache: PsychosocialCaregiverSupport

    init {
        viewModelScope.launch {
            try {
                val patient = loadPatientDetails(userRepo, patientRepo, patientID)
                    ?: return@launch

                val existingRecord = if (benVisitNo != null) {
                    psychosocialRepo.getAssessmentByPatientIdAndVisitNo(
                        patient.patient.patientID,
                        benVisitNo
                    )
                } else {
                    psychosocialRepo.getAssessmentByPatientId(
                        patient.patient.patientID
                    )
                }

                assessmentCache = existingRecord ?: PsychosocialCaregiverSupport(
                    patientId = patient.patient.patientID,
                    benVisitNo = benVisitNo
                )

                dataset.setUpPage(savedRecord = existingRecord)

            } catch (e: Exception) {
                Timber.e(e, "Error initializing PsychosocialCaregiverSupportFormViewModel")
            }
        }
    }

    // ── Form Updates ──────────────────────────────────────────────────────────

    fun updateListOnValueChanged(formId: Int, index: Int) {
        launchUpdateList(
            dataset,
            formId,
            index,
            "Error updating Psychosocial & Caregiver Support form"
        )
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    fun saveForm() {
        launchSave("Saving Psychosocial & Caregiver Support failed") {
            check(::assessmentCache.isInitialized) {
                "Assessment cache not initialized"
            }
            dataset.mapValues(assessmentCache, 1)
            psychosocialRepo.saveAssessment(assessmentCache)
        }
    }
}