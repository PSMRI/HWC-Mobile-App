package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.EarDiagnosisAssessmentDao
import org.piramalswasthya.cho.model.EarDiagnosisAssessment
import javax.inject.Inject

class EarDiagnosisRepo @Inject constructor(
    private val earDiagnosisAssessmentDao: EarDiagnosisAssessmentDao
) {

    suspend fun saveAssessment(assessment: EarDiagnosisAssessment) {
        if (assessment.assessmentId == 0L) {
            earDiagnosisAssessmentDao.insert(assessment)
        } else {
            earDiagnosisAssessmentDao.update(assessment)
        }
    }

    suspend fun getAssessmentByPatientId(patientID: String): EarDiagnosisAssessment? {
        return earDiagnosisAssessmentDao.getAssessmentByPatientId(patientID)
    }

    suspend fun getAssessmentByPatientIdAndVisitNo(patientID: String, benVisitNo: Int): EarDiagnosisAssessment? {
        return earDiagnosisAssessmentDao.getAssessmentByPatientIdAndVisitNo(patientID, benVisitNo)
    }
}
