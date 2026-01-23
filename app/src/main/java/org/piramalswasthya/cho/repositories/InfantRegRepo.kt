package org.piramalswasthya.cho.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.room.dao.InfantRegDao
import org.piramalswasthya.cho.model.ChildRegDomain
import org.piramalswasthya.cho.model.InfantRegCache
import org.piramalswasthya.cho.model.InfantRegDomain
import javax.inject.Inject

class InfantRegRepo @Inject constructor(
    private val infantRegDao: InfantRegDao
) {
    /**
     * Get list of infants eligible for registration
     * Returns flattened list of InfantRegDomain (one per baby based on liveBirth count)
     */
    fun getListForInfantReg(): Flow<List<InfantRegDomain>> {
        return infantRegDao.getListForInfantRegister()
            .map { list -> list.flatMap { it.asDomainModel() } }
    }

    /**
     * Get count of infants eligible for registration
     */
    fun getInfantRegisterCount(): Flow<Int> {
        return infantRegDao.getInfantRegisterCount()
    }

    /**
     * Get all registered infants for child registration list
     */
    fun getRegisteredInfants(): Flow<List<ChildRegDomain>> {
        return infantRegDao.getAllRegisteredInfants()
            .map { list -> list.map { it.asDomainModel() } }
    }

    /**
     * Get count of registered infants
     */
    fun getRegisteredInfantsCount(): Flow<Int> {
        return infantRegDao.getAllRegisteredInfantsCount()
    }

    suspend fun getInfantReg(patientID: String, babyIndex: Int): InfantRegCache? {
        return withContext(Dispatchers.IO) {
            infantRegDao.getInfantReg(patientID, babyIndex)
        }
    }

    suspend fun getInfantRegFromChildPatientID(childPatientID: String): InfantRegCache? {
        return withContext(Dispatchers.IO) {
            infantRegDao.getInfantRegFromChildPatientID(childPatientID)
        }
    }

    suspend fun saveInfantReg(infantRegCache: InfantRegCache) {
        withContext(Dispatchers.IO) {
            infantRegDao.saveInfantReg(infantRegCache)
        }
    }

    suspend fun getNumBabiesRegistered(patientID: String): Int {
        return withContext(Dispatchers.IO) {
            infantRegDao.getNumBabiesRegistered(patientID)
        }
    }
}
