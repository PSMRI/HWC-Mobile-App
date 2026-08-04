package org.piramalswasthya.cho.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.NeonatalOutcomeDao
import org.piramalswasthya.cho.model.NeonatalOutcomeCache
import javax.inject.Inject

/**
 * Repository for Neonatal Outcome operations
 * Handles data access and business logic for neonatal outcome records
 */
class NeonatalOutcomeRepo @Inject constructor(
    private val neonatalOutcomeDao: NeonatalOutcomeDao
) {
    
    /**
     * Insert a single neonatal outcome record
     */
    suspend fun insertNeonatalOutcome(neonatalOutcome: NeonatalOutcomeCache): Long {
        return neonatalOutcomeDao.insertNeonatalOutcome(neonatalOutcome)
    }
    
    /**
     * Insert multiple neonatal outcomes (for twins/triplets/etc.)
     */
    suspend fun insertNeonatalOutcomes(neonatalOutcomes: List<NeonatalOutcomeCache>): List<Long> {
        return neonatalOutcomeDao.insertNeonatalOutcomes(neonatalOutcomes)
    }
    
    /**
     * Update an existing neonatal outcome record
     */
    suspend fun updateNeonatalOutcome(neonatalOutcome: NeonatalOutcomeCache): Int {
        return neonatalOutcomeDao.updateNeonatalOutcome(neonatalOutcome)
    }
    
    /**
     * Get all neonatal outcomes for a specific delivery
     */
    suspend fun getNeonatalOutcomesByDeliveryId(deliveryOutcomeId: Long): List<NeonatalOutcomeCache> {
        return neonatalOutcomeDao.getNeonatalOutcomesByDeliveryId(deliveryOutcomeId)
    }
    
    /**
     * Get all neonatal outcomes for a delivery as Flow (for reactive UI)
     */
    fun getNeonatalOutcomesByDeliveryIdFlow(deliveryOutcomeId: Long): Flow<List<NeonatalOutcomeCache>> {
        return neonatalOutcomeDao.getNeonatalOutcomesByDeliveryIdFlow(deliveryOutcomeId)
    }
    
    /**
     * Get all neonatal outcomes for a delivery as LiveData
     */
    fun getNeonatalOutcomesByDeliveryIdLiveData(deliveryOutcomeId: Long): LiveData<List<NeonatalOutcomeCache>> {
        return neonatalOutcomeDao.getNeonatalOutcomesByDeliveryIdFlow(deliveryOutcomeId).asLiveData()
    }
    
    /**
     * Get a specific neonatal outcome by ID
     */
    suspend fun getNeonatalOutcomeById(neonatalOutcomeId: Long): NeonatalOutcomeCache? {
        return neonatalOutcomeDao.getNeonatalOutcomeById(neonatalOutcomeId)
    }
    
    /**
     * Get a specific neonate by delivery ID and index
     */
    suspend fun getNeonatalOutcomeByIndexAndDelivery(deliveryOutcomeId: Long, neonateIndex: Int): NeonatalOutcomeCache? {
        return neonatalOutcomeDao.getNeonatalOutcomeByIndexAndDelivery(deliveryOutcomeId, neonateIndex)
    }
    
    /**
     * Get all records pending sync with server
     */
    suspend fun getAllPendingSync(): List<NeonatalOutcomeCache> {
        return neonatalOutcomeDao.getAllPendingSync(SyncState.UNSYNCED)
    }
    
    /**
     * Get count of neonates for a delivery
     */
    suspend fun getCountByDeliveryId(deliveryOutcomeId: Long): Int {
        return neonatalOutcomeDao.getCountByDeliveryId(deliveryOutcomeId)
    }
    
    /**
     * Update sync state for a neonatal outcome
     */
    suspend fun updateSyncState(id: Long, syncState: SyncState): Int {
        return neonatalOutcomeDao.updateSyncState(id, syncState)
    }
    
    /**
     * Check if neonatal outcomes exist for a delivery
     */
    suspend fun hasNeonatalOutcomes(deliveryOutcomeId: Long): Boolean {
        return neonatalOutcomeDao.hasNeonatalOutcomes(deliveryOutcomeId)
    }
    
    /**
     * Delete all neonatal outcomes for a delivery
     */
    suspend fun deleteByDeliveryId(deliveryOutcomeId: Long): Int {
        return neonatalOutcomeDao.deleteByDeliveryId(deliveryOutcomeId)
    }
}
