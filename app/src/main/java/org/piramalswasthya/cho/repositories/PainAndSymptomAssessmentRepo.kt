package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.PainAndSymptomAssessmentDao
import org.piramalswasthya.cho.model.PainAndSymptomAssessment
import javax.inject.Inject

class PainAndSymptomAssessmentRepo @Inject constructor(
    private val painAndSymptomAssessmentDao: PainAndSymptomAssessmentDao
) {

    suspend fun saveAssessment(assessment: PainAndSymptomAssessment) {
        if (assessment.assessmentId == 0L) {
            painAndSymptomAssessmentDao.insert(assessment)
        } else {
            painAndSymptomAssessmentDao.update(assessment)
        }
    }

    suspend fun getAssessmentByPatientId(
        patientID: String
    ): PainAndSymptomAssessment? {
        return painAndSymptomAssessmentDao.getAssessmentByPatientId(patientID)
    }

    suspend fun getAssessmentByPatientIdAndVisitNo(
        patientID: String,
        benVisitNo: Int
    ): PainAndSymptomAssessment? {
        return painAndSymptomAssessmentDao
            .getAssessmentByPatientIdAndVisitNo(patientID, benVisitNo)
    }
}