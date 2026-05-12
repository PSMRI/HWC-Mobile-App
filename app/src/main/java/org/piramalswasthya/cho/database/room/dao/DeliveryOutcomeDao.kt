package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapInfo
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.cho.database.room.SyncStateValue
import org.piramalswasthya.cho.model.DeliveryOutcomeCache
import org.piramalswasthya.cho.model.SyncStatusCache

@Dao
interface DeliveryOutcomeDao {

    @Query("SELECT * FROM DELIVERY_OUTCOME WHERE patientID = :patientID and isActive=1 limit 1")
    fun getDeliveryOutcome(patientID: String): DeliveryOutcomeCache?

    @Query("SELECT * FROM DELIVERY_OUTCOME WHERE patientID in (:patientID) and isActive = 1")
    fun getAllDeliveryOutcomes(patientID: Set<String>): List<DeliveryOutcomeCache>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun saveDeliveryOutcome(deliveryOutcomeCache: DeliveryOutcomeCache)

    @Query("SELECT * FROM DELIVERY_OUTCOME WHERE processed in ('N','U')")
    suspend fun getAllUnprocessedDeliveryOutcomes(): List<DeliveryOutcomeCache>

    @Update
    suspend fun updateDeliveryOutcome(it: DeliveryOutcomeCache): Int

    @Query(
        """
        SELECT
            9 AS id,
            'Delivery Outcome' AS name,
            COUNT(CASE WHEN do.syncState = :syncedState THEN 1 END) AS synced,
            COUNT(CASE WHEN do.syncState = :unsyncedState THEN 1 END) AS notSynced,
            COUNT(CASE WHEN do.syncState = :syncingState THEN 1 END) AS syncing
        FROM DELIVERY_OUTCOME do
        WHERE do.isActive = 1
        """
    )
    fun getDeliveryOutcomeSyncStatus(
        syncedState: Int = SyncStateValue.SYNCED,
        syncingState: Int = SyncStateValue.SYNCING,
        unsyncedState: Int = SyncStateValue.UNSYNCED
    ): Flow<List<SyncStatusCache>>

//    @MapInfo(keyColumn = "benId", valueColumn ="dateOfDelivery")
//    @Query("select * from delivery_outcome where isActive = 1")
//    suspend fun getAllBenIdAndDeliverDate(): Map<Long, Long>
}
