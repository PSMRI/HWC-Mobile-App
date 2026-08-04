package org.piramalswasthya.cho.ui.ENT

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
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
import org.piramalswasthya.cho.ui.commons.BaseFormViewModel
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
) : BaseFormViewModel() {

    /* -------------------- MULTI-SELECT -------------------- */

    private val _triggerMultiSelect = MutableLiveData<MultiSelectData?>()
    val triggerMultiSelect: LiveData<MultiSelectData?> get() = _triggerMultiSelect

    data class MultiSelectData(
        val formId: Int,
        val title: String,
        val items: Array<String>,
        val selectedItems: BooleanArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is MultiSelectData) return false
            return formId == other.formId &&
                title == other.title &&
                items.contentEquals(other.items) &&
                selectedItems.contentEquals(other.selectedItems)
        }

        override fun hashCode(): Int {
            var result = formId
            result = 31 * result + title.hashCode()
            result = 31 * result + items.contentHashCode()
            result = 31 * result + selectedItems.contentHashCode()
            return result
        }
    }

    /* -------------------- BEN DETAILS -------------------- */

    val patientID: String? = savedStateHandle["patientID"]
    val benVisitNo: Int? = savedStateHandle["benVisitNo"]

    /* -------------------- DATASET -------------------- */

    private val dataset =
        ThroatDiagnosisDataset(context, preferenceDao.getCurrentLanguage())

    val formList = dataset.listFlow

    private lateinit var assessmentCache: ThroatDiagnosisAssessment

    init {
        viewModelScope.launch {
            try {
                val patient = loadPatientDetails(userRepo, patientRepo, patientID)
                    ?: return@launch

                val existingRecord = if (benVisitNo != null) {
                    throatDiagnosisRepo.getAssessmentByPatientIdAndVisitNo(
                        patient.patient.patientID,
                        benVisitNo
                    )
                } else {
                    throatDiagnosisRepo.getAssessmentByPatientId(patient.patient.patientID)
                }

                assessmentCache = existingRecord ?: ThroatDiagnosisAssessment(
                    patientId = patient.patient.patientID,
                    benVisitNo = benVisitNo
                )

                setupDatasetCallbacks()

                dataset.setUpPage(savedRecord = existingRecord)

            } catch (e: Exception) {
                Timber.e(e, "Error initializing ThroatDiagnosisFormViewModel")
            }
        }
    }

    private fun setupDatasetCallbacks() {
        dataset.onShowAlert = { message ->
            _showAlert.postValue(message)
        }

        dataset.onTriggerMultiSelect = { formId, title, items, selectedItems ->
            _triggerMultiSelect.value =
                MultiSelectData(formId, title, items, selectedItems)
        }
    }

    /* -------------------- FORM EVENTS -------------------- */

    fun updateListOnValueChanged(formId: Int, index: Int) {
        launchUpdateList(dataset, formId, index, "Error updating throat diagnosis form")
    }

    fun onMultiSelectClick(formId: Int) {
        dataset.triggerMultiSelect(formId)
    }

    fun updateMultiSelectValue(formId: Int, selectedItems: List<String>) {
        viewModelScope.launch {
            try {
                dataset.updateMultiSelectValue(formId, selectedItems)
            } catch (e: Exception) {
                Timber.e(e, "Error updating multi-select value")
            }
        }
    }

    fun onMultiSelectDialogDismissed() {
        _triggerMultiSelect.value = null
    }

    fun saveForm() {
        launchSave("Saving Throat Diagnosis failed") {
            check(::assessmentCache.isInitialized) { "Assessment cache not initialized" }
            dataset.mapValues(assessmentCache, 1)
            throatDiagnosisRepo.saveAssessment(assessmentCache)
        }
    }
}
