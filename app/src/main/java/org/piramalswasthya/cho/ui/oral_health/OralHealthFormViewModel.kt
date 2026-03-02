package org.piramalswasthya.cho.ui.oral_health

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.configuration.OralHealthDataset
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.OralHealth
import org.piramalswasthya.cho.repositories.OralHealthRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.ui.commons.BaseFormViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class OralHealthFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val patientRepo: PatientRepo,
    private val userRepo: UserRepo,
    private val oralHealthRepo: OralHealthRepo
) : BaseFormViewModel() {

    val patientID: String? = savedStateHandle["patientID"]
    val benVisitNo: Int? = savedStateHandle["benVisitNo"]

    private val dataset = OralHealthDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    private lateinit var oralHealthCache: OralHealth

    init {
        viewModelScope.launch {
            try {
                val patient = loadPatientDetails(userRepo, patientRepo, patientID) ?: return@launch

                val existingRecord = if (benVisitNo != null) {
                    oralHealthRepo.getByPatientIdAndVisitNo(
                        patient.patient.patientID,
                        benVisitNo
                    )
                } else {
                    oralHealthRepo.getByPatientId(patient.patient.patientID)
                }

                oralHealthCache = existingRecord ?: OralHealth(
                    patientID = patient.patient.patientID,
                    benVisitNo = benVisitNo
                )

                dataset.onShowAlert = { _showAlert.postValue(it) }
                dataset.setUpPage(savedRecord = existingRecord)
            } catch (e: Exception) {
                Timber.e(e, "Error initializing OralHealthFormViewModel")
            }
        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        launchUpdateList(dataset, formId, index, "Error updating oral health form")
    }

    fun saveForm() {
        launchSave("Saving oral health form failed") {
            check(::oralHealthCache.isInitialized) { "Oral health cache not initialized" }
            dataset.mapValues(oralHealthCache, 1)
            if (oralHealthCache.oralHealthId == 0L) {
                val user = userRepo.getLoggedInUser()
                oralHealthCache.createdDate = System.currentTimeMillis()
                oralHealthCache.createdBy = user?.userName
            }
            oralHealthRepo.save(oralHealthCache)
        }
    }
}

