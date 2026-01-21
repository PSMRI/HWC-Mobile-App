package org.piramalswasthya.cho.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.room.dao.NewbornOutcomeDao
import org.piramalswasthya.cho.model.NeonateDetailsCache
import org.piramalswasthya.cho.model.NewbornOutcomeCache
import timber.log.Timber
import javax.inject.Inject

class NewbornOutcomeRepo @Inject constructor(
    private val newbornOutcomeDao: NewbornOutcomeDao
) {

    // ========== NEWBORN OUTCOME (Main Record) ==========
    
    suspend fun saveNewbornOutcome(newbornOutcome: NewbornOutcomeCache): Long {
        return withContext(Dispatchers.IO) {
            try {
                newbornOutcomeDao.insertNewbornOutcome(newbornOutcome)
            } catch (e: Exception) {
                Timber.e("Error saving newborn outcome: ${e.message}")
                throw e
            }
        }
    }
    
    suspend fun updateNewbornOutcome(newbornOutcome: NewbornOutcomeCache) {
        withContext(Dispatchers.IO) {
            try {
                newbornOutcomeDao.updateNewbornOutcome(newbornOutcome)
            } catch (e: Exception) {
                Timber.e("Error updating newborn outcome: ${e.message}")
                throw e
            }
        }
    }
    
    fun getActiveNewbornOutcomeByPatientID(patientID: String): Flow<NewbornOutcomeCache?> {
        return newbornOutcomeDao.getActiveNewbornOutcomeByPatientID(patientID)
    }
    
    suspend fun getNewbornOutcomeById(id: Long): NewbornOutcomeCache? {
        return withContext(Dispatchers.IO) {
            newbornOutcomeDao.getNewbornOutcomeById(id)
        }
    }
    
    // ========== NEONATE DETAILS (Child Records) ==========
    
    suspend fun saveNeonateDetails(neonateDetails: NeonateDetailsCache): Long {
        return withContext(Dispatchers.IO) {
            try {
                newbornOutcomeDao.insertNeonateDetails(neonateDetails)
            } catch (e: Exception) {
                Timber.e("Error saving neonate details: ${e.message}")
                throw e
            }
        }
    }
    
    suspend fun updateNeonateDetails(neonateDetails: NeonateDetailsCache) {
        withContext(Dispatchers.IO) {
            try {
                newbornOutcomeDao.updateNeonateDetails(neonateDetails)
            } catch (e: Exception) {
                Timber.e("Error updating neonate details: ${e.message}")
                throw e
            }
        }
    }
    
    fun getNeonateDetailsByOutcomeID(newbornOutcomeID: Long): Flow<List<NeonateDetailsCache>> {
        return newbornOutcomeDao.getNeonateDetailsByOutcomeID(newbornOutcomeID)
    }
    
    suspend fun getNeonateDetailsByIndex(newbornOutcomeID: Long, index: Int): NeonateDetailsCache? {
        return withContext(Dispatchers.IO) {
            newbornOutcomeDao.getNeonateDetailsByIndex(newbornOutcomeID, index)
        }
    }
    
    suspend fun getNeonateDetailsByNeonateID(neonateID: String): NeonateDetailsCache? {
        return withContext(Dispatchers.IO) {
            newbornOutcomeDao.getNeonateDetailsByNeonateID(neonateID)
        }
    }
    
    // ========== COMBINED OPERATIONS ==========
    
    suspend fun saveCompleteNewbornOutcome(
        newbornOutcome: NewbornOutcomeCache,
        neonateDetailsList: List<NeonateDetailsCache>
    ) {
        withContext(Dispatchers.IO) {
            try {
                newbornOutcomeDao.saveCompleteNewbornOutcome(newbornOutcome, neonateDetailsList)
                Timber.d("Successfully saved complete newborn outcome with ${neonateDetailsList.size} neonates")
            } catch (e: Exception) {
                Timber.e("Error saving complete newborn outcome: ${e.message}")
                throw e
            }
        }
    }
    
    fun getLiveBirthCountByPatientID(patientID: String): Flow<Int> {
        return newbornOutcomeDao.getLiveBirthCountByPatientID(patientID)
    }
    
    fun getAllNeonatesByPatientID(patientID: String): Flow<List<NeonateDetailsCache>> {
        return newbornOutcomeDao.getAllNeonatesByPatientID(patientID)
    }
    
    /**
     * Generate unique neonate ID
     * Format: MOT{motherPatientID}_NEO{index}_{timestamp}
     */
    fun generateNeonateID(motherPatientID: String, index: Int): String {
        val timestamp = System.currentTimeMillis()
        return "MOT${motherPatientID}_NEO${index}_${timestamp}"
    }
}
