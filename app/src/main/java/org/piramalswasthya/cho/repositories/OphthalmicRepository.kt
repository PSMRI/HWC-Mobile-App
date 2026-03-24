package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.OphthalmicDao
import org.piramalswasthya.cho.model.OphthalmicVisit
import javax.inject.Inject

class OphthalmicRepository @Inject constructor(
    private val ophthalmicDao: OphthalmicDao
) {
    suspend fun getOphthalmicVisit(patientID: String, benVisitNo: Int): OphthalmicVisit? {
        return ophthalmicDao.getOphthalmicVisit(patientID, benVisitNo)
    }

    suspend fun saveOphthalmicVisit(visit: OphthalmicVisit) {
        if (visit.visitId.isNotEmpty()) {
             val existing = ophthalmicDao.getOphthalmicVisitById(visit.visitId)
             if (existing == null) {
                 ophthalmicDao.insertOphthalmicVisit(visit)
             } else {
                 ophthalmicDao.updateOphthalmicVisit(visit)
             }
        } else {
            ophthalmicDao.insertOphthalmicVisit(visit)
        }
    }
    
    suspend fun updateOphthalmicVisit(visit: OphthalmicVisit) {
        ophthalmicDao.updateOphthalmicVisit(visit)
    }
}
