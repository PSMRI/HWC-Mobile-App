package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import org.piramalswasthya.cho.model.Batch

@Dao
interface BatchDao {

    // Insert a single Batch into the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: Batch)

    // Insert a list of Batches into the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBatches(batches: List<Batch>)

    // Delete a single Batch from the database
    @Delete
    suspend fun deleteBatch(batch: Batch)
}
