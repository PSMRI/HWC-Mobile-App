package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.MentalHealthScreeningDao
import org.piramalswasthya.cho.model.MentalHealthScreeningCache
import javax.inject.Inject

class MentalHealthScreeningRepo @Inject constructor(
    private val mentalHealthScreeningDao: MentalHealthScreeningDao
) {

    suspend fun saveScreening(screening: MentalHealthScreeningCache): MentalHealthScreeningCache {
        return if (screening.screeningId == 0L) {
            val screeningId = mentalHealthScreeningDao.insert(screening)
            screening.copy(screeningId = screeningId)
        } else {
            mentalHealthScreeningDao.update(screening)
            screening
        }
    }

    suspend fun getScreeningByPatientId(
        patientID: String
    ): MentalHealthScreeningCache? {
        return mentalHealthScreeningDao.getScreeningByPatientId(patientID)
    }

    suspend fun getScreeningByPatientIdAndVisitNo(
        patientID: String,
        benVisitNo: Int
    ): MentalHealthScreeningCache? {
        return mentalHealthScreeningDao.getScreeningByPatientIdAndVisitNo(
            patientID, benVisitNo
        )
    }
}