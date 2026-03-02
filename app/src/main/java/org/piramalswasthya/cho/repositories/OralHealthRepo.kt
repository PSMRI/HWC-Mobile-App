package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.OralHealthDao
import org.piramalswasthya.cho.model.OralHealth
import javax.inject.Inject

class OralHealthRepo @Inject constructor(
    private val oralHealthDao: OralHealthDao
) {

    suspend fun save(oralHealth: OralHealth) {
        if (oralHealth.oralHealthId == 0L) {
            oralHealthDao.insert(oralHealth)
        } else {
            oralHealthDao.update(oralHealth)
        }
    }

    suspend fun getByPatientId(patientID: String): OralHealth? {
        return oralHealthDao.getByPatientId(patientID)
    }

    suspend fun getByPatientIdAndVisitNo(
        patientID: String,
        benVisitNo: Int
    ): OralHealth? {
        return oralHealthDao.getByPatientIdAndVisitNo(patientID, benVisitNo)
    }
}

