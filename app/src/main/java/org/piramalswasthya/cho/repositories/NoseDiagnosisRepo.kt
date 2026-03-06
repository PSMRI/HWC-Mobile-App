package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.NoseDiagnosisAssessmentDao
import org.piramalswasthya.cho.model.NoseDiagnosisAssessment
import javax.inject.Inject

class NoseDiagnosisRepo @Inject constructor(
    private val noseDiagnosisAssessmentDao: NoseDiagnosisAssessmentDao
) {

    suspend fun saveAssessment(assessment: NoseDiagnosisAssessment) {
        if (assessment.assessmentId == 0L) {
            noseDiagnosisAssessmentDao.insert(assessment)
        } else {
            noseDiagnosisAssessmentDao.update(assessment)
        }
    }

    suspend fun getAssessmentByPatientId(patientID: String): NoseDiagnosisAssessment? {
        return noseDiagnosisAssessmentDao.getAssessmentByPatientId(patientID)
    }

    suspend fun getAssessmentByPatientIdAndVisitNo(
        patientID: String,
        benVisitNo: Int
    ): NoseDiagnosisAssessment? {
        return noseDiagnosisAssessmentDao.getAssessmentByPatientIdAndVisitNo(patientID, benVisitNo)
    }
}
