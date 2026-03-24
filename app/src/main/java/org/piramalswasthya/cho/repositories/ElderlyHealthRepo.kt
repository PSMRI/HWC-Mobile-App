package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.ElderlyHealthAssessmentDao
import org.piramalswasthya.cho.model.ElderlyHealthAssessment
import javax.inject.Inject

class ElderlyHealthRepo @Inject constructor(
    private val elderlyHealthAssessmentDao: ElderlyHealthAssessmentDao
) {
    suspend fun saveAssessment(assessment: ElderlyHealthAssessment) {
        if (assessment.assessmentId == 0L) {
            elderlyHealthAssessmentDao.insert(assessment)
        } else {
            elderlyHealthAssessmentDao.update(assessment)
        }
    }

    suspend fun getAssessmentByPatientId(patientID: String): ElderlyHealthAssessment? {
        return elderlyHealthAssessmentDao.getAssessmentByPatientId(patientID)
    }
}