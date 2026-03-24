package org.piramalswasthya.cho.ui.mental_health

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.configuration.MentalHealthScreeningDataset
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.MentalHealthScreeningCache
import org.piramalswasthya.cho.repositories.DeliveryOutcomeRepo
import org.piramalswasthya.cho.repositories.MentalHealthScreeningRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.ui.commons.BaseFormViewModel
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MentalHealthScreeningFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val patientRepo: PatientRepo,
    private val userRepo: UserRepo,
    private val mentalHealthScreeningRepo: MentalHealthScreeningRepo,
    private val deliveryOutcomeRepo: DeliveryOutcomeRepo

) : BaseFormViewModel() {

    val patientID: String? = savedStateHandle["patientID"]
    val benVisitNo: Int? = savedStateHandle["benVisitNo"]

    private val dataset =
        MentalHealthScreeningDataset(context, preferenceDao.getCurrentLanguage())

    val formList = dataset.listFlow

    private lateinit var screeningCache: MentalHealthScreeningCache

    init {
        viewModelScope.launch {
            dataset.alertErrorMessageFlow.collect { message ->
                message?.let {
                    _showAlert.value = it
                    dataset.resetErrorMessageFlow()
                }
            }
        }
        viewModelScope.launch {
            try {
                val patient = loadPatientDetails(userRepo, patientRepo, patientID)
                    ?: return@launch

                val existingRecord = if (benVisitNo != null) {
                    mentalHealthScreeningRepo.getScreeningByPatientIdAndVisitNo(
                        patient.patient.patientID,
                        benVisitNo
                    )
                } else {
                    mentalHealthScreeningRepo.getScreeningByPatientId(
                        patient.patient.patientID
                    )
                }

                screeningCache = existingRecord ?: MentalHealthScreeningCache(
                    patientID = patient.patient.patientID,
                    benVisitNo = benVisitNo
                )

                val deliveryOutcome = deliveryOutcomeRepo.getDeliveryOutcome(patient.patient.patientID)
                val isPostpartumFromRmncha = (deliveryOutcome?.dateOfDelivery?.let { deliveryDateMs ->
                    val twelveMonthsInMs = TimeUnit.DAYS.toMillis(365)
                    val now = System.currentTimeMillis()
                    (now - deliveryDateMs) in 0..twelveMonthsInMs
                } ?: false) || (patient.patient.statusOfWomanID == 3)


                dataset.setUpPage(
                    savedRecord = existingRecord,
                    isPostpartumFromRmncha = isPostpartumFromRmncha
                )

            } catch (e: Exception) {
                Timber.e(e, "Error initializing MentalHealthScreeningFormViewModel")
            }
        }
    }

    // ── Form Updates ──────────────────────────────────────────────────

    fun updateListOnValueChanged(formId: Int, index: Int) {
        launchUpdateList(
            dataset,
            formId,
            index,
            "Error updating Mental Health Screening form"
        )
    }

    // ── Save ──────────────────────────────────────────────────────────

    fun saveForm() {
        launchSave("Saving Mental Health Screening failed") {
            check(::screeningCache.isInitialized) {
                "Screening cache not initialized"
            }
            dataset.mapValues(screeningCache, 1)
            mentalHealthScreeningRepo.saveScreening(screeningCache)
        }
    }
}