package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.ThroatDiagnosisAssessmentDao
import org.piramalswasthya.cho.model.ThroatDiagnosisAssessment
import javax.inject.Inject

class ThroatDiagnosisRepo @Inject constructor(
    private val throatDiagnosisAssessmentDao: ThroatDiagnosisAssessmentDao
) {

    suspend fun saveAssessment(assessment: ThroatDiagnosisAssessment) {
        if (assessment.assessmentId == 0L) {
            throatDiagnosisAssessmentDao.insert(assessment)
        } else {
            throatDiagnosisAssessmentDao.update(assessment)
        }
    }

    suspend fun getAssessmentByPatientId(patientID: String): ThroatDiagnosisAssessment? {
        return throatDiagnosisAssessmentDao.getAssessmentByPatientId(patientID)
    }
}