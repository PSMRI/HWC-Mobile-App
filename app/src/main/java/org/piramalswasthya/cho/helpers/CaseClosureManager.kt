package org.piramalswasthya.cho.helpers

import org.piramalswasthya.cho.database.room.dao.CaseRecordeDao
import org.piramalswasthya.cho.database.room.dao.PrescriptionDao
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized business logic for case closure workflow.
 * 
 * RULES:
 * 1. AUTO-CLOSE: Only medicine prescribed (no lab) + medicine dispensed
 * 2. AUTO-CLOSE: Nothing prescribed (no lab, no medicine)
 * 3. MANUAL-CLOSE: Lab test involved (with or without medicine)
 * 4. CANNOT CLOSE: Pending lab test or undispensed medicine
 */
@Singleton
class CaseClosureManager @Inject constructor(
    private val caseRecordeDao: CaseRecordeDao,
    private val prescriptionDao: PrescriptionDao
) {

    /**
     * Determines if case should auto-close based on prescription type.
     * 
     * @return true if case should auto-close, false if manual closure required
     */
    suspend fun shouldAutoClose(visitInfo: PatientVisitInfoSync): Boolean {
        val hasMedicine = hasMedicinePrescribed(visitInfo.patientID, visitInfo.benVisitNo)
        val hasLab = hasLabTestPrescribed(visitInfo.patientID, visitInfo.benVisitNo)
        val medicineDispensed = visitInfo.pharmacist_flag == 9

        return when {
            // Scenario 1: Nothing prescribed -> auto close
            !hasMedicine && !hasLab -> {
                Timber.d("CaseClosureManager: Auto-close - nothing prescribed")
                true
            }
            // Scenario 2: Only medicine + dispensed -> auto close
            hasMedicine && !hasLab && medicineDispensed -> {
                Timber.d("CaseClosureManager: Auto-close - only medicine dispensed")
                true
            }
            // All other scenarios require manual closure
            else -> {
                Timber.d("CaseClosureManager: Manual close required - hasLab=$hasLab, hasMedicine=$hasMedicine, dispensed=$medicineDispensed")
                false
            }
        }
    }

    /**
     * Checks if case can be manually closed by doctor.
     * Validates that all prerequisites are met.
     * 
     * @return Pair<Boolean, String?> - (canClose, errorMessage)
     */
    suspend fun canManuallyClose(visitInfo: PatientVisitInfoSync): Pair<Boolean, String?> {
        val hasPendingLab = hasLabTestPending(visitInfo)
        val hasUndispensedMedicine = hasUndispensedMedicine(visitInfo)

        return when {
            hasPendingLab -> {
                Timber.w("CaseClosureManager: Cannot close - pending lab test")
                false to "Cannot close case: Lab test is pending"
            }
            hasUndispensedMedicine -> {
                Timber.w("CaseClosureManager: Cannot close - undispensed medicine")
                false to "Cannot close case: Medicine has not been dispensed"
            }
            else -> {
                Timber.d("CaseClosureManager: Can manually close")
                true to null
            }
        }
    }

    /**
     * Checks if lab test was ever prescribed for this visit.
     * Lab involvement determines if closure should be manual.
     */
    suspend fun hasLabTestPrescribed(patientID: String, benVisitNo: Int): Boolean {
        return try {
            val investigation = caseRecordeDao.getPrescriptionCasesRecordByPatientIDAndBenVisitNo(
                patientID,
                benVisitNo
            )
            val hasTests = !investigation?.previousTestIds.isNullOrBlank() || 
                          !investigation?.newTestIds.isNullOrBlank()
            Timber.d("CaseClosureManager: hasLabTestPrescribed=$hasTests for $patientID/$benVisitNo")
            hasTests
        } catch (e: Exception) {
            Timber.e(e, "Error checking lab test prescription")
            false
        }
    }

    /**
     * Checks if medicine was prescribed for this visit.
     */
    suspend fun hasMedicinePrescribed(patientID: String, benVisitNo: Int): Boolean {
        return try {
            val prescriptions = caseRecordeDao.getPrescriptionByPatientIDAndBenVisitNo(
                patientID,
                benVisitNo
            )
            val hasMedicine = !prescriptions.isNullOrEmpty()
            Timber.d("CaseClosureManager: hasMedicinePrescribed=$hasMedicine for $patientID/$benVisitNo")
            hasMedicine
        } catch (e: Exception) {
            Timber.e(e, "Error checking medicine prescription")
            false
        }
    }

    /**
     * Checks if lab test is pending (prescribed but not completed).
     */
    private suspend fun hasLabTestPending(visitInfo: PatientVisitInfoSync): Boolean {
        val hasLab = hasLabTestPrescribed(visitInfo.patientID, visitInfo.benVisitNo)
        // Backward compatibility: older records may stay at labtechFlag=1 even after lab submission,
        // but doctorFlag=3 represents the post-lab review state.
        val isLabCompleted = (visitInfo.labtechFlag ?: 0) == 9 || (visitInfo.doctorFlag ?: 0) == 3
        val isPending = hasLab && !isLabCompleted
        Timber.d("CaseClosureManager: hasLabTestPending=$isPending (hasLab=$hasLab, labtechFlag=${visitInfo.labtechFlag}, doctorFlag=${visitInfo.doctorFlag})")
        return isPending
    }

    /**
     * Checks if medicine is prescribed but not dispensed.
     */
    private suspend fun hasUndispensedMedicine(visitInfo: PatientVisitInfoSync): Boolean {
        val hasMedicine = hasMedicinePrescribed(visitInfo.patientID, visitInfo.benVisitNo)
        // Medicine is undispensed if prescribed but pharmacist_flag != 9 (not dispensed)
        val isUndispensed = hasMedicine && (visitInfo.pharmacist_flag ?: 0) != 9
        Timber.d("CaseClosureManager: hasUndispensedMedicine=$isUndispensed (hasMedicine=$hasMedicine, pharmacist_flag=${visitInfo.pharmacist_flag})")
        return isUndispensed
    }

    /**
     * Determines if case requires manual closure confirmation dialog.
     */
    suspend fun requiresManualClosureConfirmation(visitInfo: PatientVisitInfoSync): Boolean {
        // Manual confirmation required if lab was ever involved
        val hasLab = hasLabTestPrescribed(visitInfo.patientID, visitInfo.benVisitNo)
        Timber.d("CaseClosureManager: requiresManualClosureConfirmation=$hasLab")
        return hasLab
    }

    /**
     * Helper for PatientDisplayWithVisitInfo overload.
     */
    suspend fun shouldAutoClose(benVisitInfo: PatientDisplayWithVisitInfo): Boolean {
        val visitInfo = PatientVisitInfoSync(
            patientID = benVisitInfo.patient.patientID,
            benVisitNo = benVisitInfo.benVisitNo ?: 0,
            pharmacist_flag = benVisitInfo.pharmacist_flag,
            labtechFlag = benVisitInfo.labtechFlag,
            doctorFlag = benVisitInfo.doctorFlag,
            nurseFlag = benVisitInfo.nurseFlag
        )
        return shouldAutoClose(visitInfo)
    }

    /**
     * Helper for PatientDisplayWithVisitInfo overload.
     */
    suspend fun canManuallyClose(benVisitInfo: PatientDisplayWithVisitInfo): Pair<Boolean, String?> {
        val visitInfo = PatientVisitInfoSync(
            patientID = benVisitInfo.patient.patientID,
            benVisitNo = benVisitInfo.benVisitNo ?: 0,
            pharmacist_flag = benVisitInfo.pharmacist_flag,
            labtechFlag = benVisitInfo.labtechFlag,
            doctorFlag = benVisitInfo.doctorFlag,
            nurseFlag = benVisitInfo.nurseFlag
        )
        return canManuallyClose(visitInfo)
    }

    /**
     * Helper for PatientDisplayWithVisitInfo overload.
     */
    suspend fun requiresManualClosureConfirmation(benVisitInfo: PatientDisplayWithVisitInfo): Boolean {
        val visitInfo = PatientVisitInfoSync(
            patientID = benVisitInfo.patient.patientID,
            benVisitNo = benVisitInfo.benVisitNo ?: 0,
            pharmacist_flag = benVisitInfo.pharmacist_flag,
            labtechFlag = benVisitInfo.labtechFlag,
            doctorFlag = benVisitInfo.doctorFlag,
            nurseFlag = benVisitInfo.nurseFlag
        )
        return requiresManualClosureConfirmation(visitInfo)
    }
}
