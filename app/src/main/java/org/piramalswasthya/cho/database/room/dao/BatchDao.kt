package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.piramalswasthya.cho.model.Batch

@Dao
interface BatchDao {

    // Insert a single Batch into the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: Batch)

    // Insert a list of Batches into the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBatches(batches: List<Batch>)

    @Update
    suspend fun updateBatch(batch: Batch)

    // Delete a single Batch from the database
    @Delete
    suspend fun deleteBatch(batch: Batch)

    @Query("SELECT * FROM Batch WHERE itemID = :itemID")
    suspend fun getBatchesByItemID(itemID: Int): List<Batch>

    @Query("SELECT * FROM Batch WHERE stockEntityId = :stockEntityId")
    suspend fun getBatchByStockEntityId(stockEntityId: Long): Batch?
}
