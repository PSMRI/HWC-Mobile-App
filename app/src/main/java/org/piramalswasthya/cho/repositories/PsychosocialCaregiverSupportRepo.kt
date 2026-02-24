package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.PsychosocialCaregiverSupportDao
import org.piramalswasthya.cho.model.PsychosocialCaregiverSupport
import javax.inject.Inject

class PsychosocialCaregiverSupportRepo @Inject constructor(
    private val psychosocialCaregiverSupportDao: PsychosocialCaregiverSupportDao
) {

    suspend fun saveAssessment(assessment: PsychosocialCaregiverSupport) {
        if (assessment.assessmentId == 0L) {
            psychosocialCaregiverSupportDao.insert(assessment)
        } else {
            psychosocialCaregiverSupportDao.update(assessment)
        }
    }

    suspend fun getAssessmentByPatientId(
        patientID: String
    ): PsychosocialCaregiverSupport? {
        return psychosocialCaregiverSupportDao
            .getAssessmentByPatientId(patientID)
    }

    suspend fun getAssessmentByPatientIdAndVisitNo(
        patientID: String,
        benVisitNo: Int
    ): PsychosocialCaregiverSupport? {
        return psychosocialCaregiverSupportDao
            .getAssessmentByPatientIdAndVisitNo(patientID, benVisitNo)
    }
}