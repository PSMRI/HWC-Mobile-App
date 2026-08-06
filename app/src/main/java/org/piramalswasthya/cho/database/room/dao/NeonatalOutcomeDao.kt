package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.model.NeonatalOutcomeCache

@Dao
interface NeonatalOutcomeDao {
    
    /**
     * Insert a new neonatal outcome record
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNeonatalOutcome(neonatalOutcome: NeonatalOutcomeCache): Long
    
    /**
     * Insert multiple neonatal outcome records (for twins/triplets)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNeonatalOutcomes(neonatalOutcomes: List<NeonatalOutcomeCache>): List<Long>
    
    /**
     * Update an existing neonatal outcome record
     */
    @Update
    suspend fun updateNeonatalOutcome(neonatalOutcome: NeonatalOutcomeCache): Int
    
    /**
     * Get all neonatal outcomes for a specific delivery
     * Ordered by neonateIndex (Baby 1, Baby 2, etc.)
     */
    @Query("SELECT * FROM NEONATAL_OUTCOME WHERE deliveryOutcomeId = :deliveryOutcomeId ORDER BY neonateIndex ASC")
    suspend fun getNeonatalOutcomesByDeliveryId(deliveryOutcomeId: Long): List<NeonatalOutcomeCache>
    
    /**
     * Get all neonatal outcomes for a specific delivery as Flow (for reactive UI)
     */
    @Query("SELECT * FROM NEONATAL_OUTCOME WHERE deliveryOutcomeId = :deliveryOutcomeId ORDER BY neonateIndex ASC")
    fun getNeonatalOutcomesByDeliveryIdFlow(deliveryOutcomeId: Long): Flow<List<NeonatalOutcomeCache>>
    
    /**
     * Get a specific neonatal outcome by ID
     */
    @Query("SELECT * FROM NEONATAL_OUTCOME WHERE id = :neonatalOutcomeId")
    suspend fun getNeonatalOutcomeById(neonatalOutcomeId: Long): NeonatalOutcomeCache?
    
    /**
     * Get a specific neonate by delivery ID and neonate index
     */
    @Query("SELECT * FROM NEONATAL_OUTCOME WHERE deliveryOutcomeId = :deliveryOutcomeId AND neonateIndex = :neonateIndex")
    suspend fun getNeonatalOutcomeByIndexAndDelivery(deliveryOutcomeId: Long, neonateIndex: Int): NeonatalOutcomeCache?
    
    /**
     * Get all neonatal outcomes that need to be synced with server
     */
    @Query("SELECT * FROM NEONATAL_OUTCOME WHERE syncState = :syncState")
    suspend fun getAllPendingSync(syncState: SyncState = SyncState.UNSYNCED): List<NeonatalOutcomeCache>
    
    /**
     * Get count of neonates for a delivery
     */
    @Query("SELECT COUNT(*) FROM NEONATAL_OUTCOME WHERE deliveryOutcomeId = :deliveryOutcomeId")
    suspend fun getCountByDeliveryId(deliveryOutcomeId: Long): Int
    
    /**
     * Update sync state for a neonatal outcome
     */
    @Query("UPDATE NEONATAL_OUTCOME SET syncState = :syncState WHERE id = :id")
    suspend fun updateSyncState(id: Long, syncState: SyncState): Int
    
    /**
     * Delete neonatal outcomes for a specific delivery (cascade should handle this, but adding for explicit control)
     */
    @Query("DELETE FROM NEONATAL_OUTCOME WHERE deliveryOutcomeId = :deliveryOutcomeId")
    suspend fun deleteByDeliveryId(deliveryOutcomeId: Long): Int
    
    /**
     * Check if neonatal outcomes exist for a delivery
     */
    @Query("SELECT COUNT(*) > 0 FROM NEONATAL_OUTCOME WHERE deliveryOutcomeId = :deliveryOutcomeId")
    suspend fun hasNeonatalOutcomes(deliveryOutcomeId: Long): Boolean
}
