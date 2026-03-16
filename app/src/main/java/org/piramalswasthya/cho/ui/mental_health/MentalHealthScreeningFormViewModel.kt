package org.piramalswasthya.cho.ui.mental_health

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.configuration.MentalHealthScreeningDataset
import org.piramalswasthya.cho.database.room.dao.DeliveryOutcomeDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.MentalHealthScreeningCache
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
    private val deliveryOutcomeDao: DeliveryOutcomeDao
) : BaseFormViewModel() {

    val patientID: String? = savedStateHandle["patientID"]
    val benVisitNo: Int? = savedStateHandle["benVisitNo"]

    private val dataset =
        MentalHealthScreeningDataset(context, preferenceDao.getCurrentLanguage())

    val formList = dataset.listFlow

    private lateinit var screeningCache: MentalHealthScreeningCache

    // Track the last alert score threshold to avoid repeated alerts
    private var lastAlertedScoreThreshold: Int = 0

    init {
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

                // Auto-derive postpartum status from RMNCH+A delivery outcome data
                // A woman is postpartum if she had a delivery within the last 12 months
                val isPostpartumFromRmncha = checkPostpartumStatus(
                    patient.patient.patientID,
                    patient.patient.genderID
                )

                dataset.setUpPage(
                    savedRecord = existingRecord,
                    isPostpartumFromRmncha = isPostpartumFromRmncha
                )

            } catch (e: Exception) {
                Timber.e(e, "Error initializing MentalHealthScreeningFormViewModel")
            }
        }
    }

    /**
     * Checks if the patient is a postpartum woman (female with delivery
     * within the last 12 months) based on delivery outcome records.
     */
    private suspend fun checkPostpartumStatus(
        patientID: String,
        genderID: Int?
    ): Boolean {
        // Only applicable for female patients (genderID == 2)
        if (genderID != 2) return false

        return withContext(Dispatchers.IO) {
            try {
                val deliveryOutcome = deliveryOutcomeDao.getDeliveryOutcome(patientID)
                if (deliveryOutcome?.dateOfDelivery != null) {
                    val twelveMonthsAgo = System.currentTimeMillis() -
                            TimeUnit.DAYS.toMillis(365)
                    deliveryOutcome.dateOfDelivery!! >= twelveMonthsAgo
                } else {
                    false
                }
            } catch (e: Exception) {
                Timber.e(e, "Error checking postpartum status")
                false
            }
        }
    }

    // ── Form Updates ──────────────────────────────────────────────────

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            try {
                dataset.updateList(formId, index)

                // After PHQ-9 recalculation, check for referral alerts
                val score = dataset.calculatePhq9Score()
                checkAndShowPhq9Alert(score)
            } catch (e: Exception) {
                Timber.e(e, "Error updating Mental Health Screening form")
            }
        }
    }

    /**
     * Shows a referral alert dialog when PHQ-9 score crosses a threshold.
     * Only shows each alert level once per session to avoid spamming.
     */
    private fun checkAndShowPhq9Alert(score: Int) {
        val currentThreshold = when {
            score >= 20 -> 20
            score >= 15 -> 15
            score >= 10 -> 10
            else -> 0
        }

        // Only alert when crossing a new threshold upward
        if (currentThreshold > lastAlertedScoreThreshold) {
            lastAlertedScoreThreshold = currentThreshold
            val alert = dataset.getPhq9ReferralAlert(score)
            if (alert != null) {
                _showAlert.postValue(alert)
            }
        }
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
