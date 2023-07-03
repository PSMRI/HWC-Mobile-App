package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.cho.model.StateMaster

@Dao
interface StateMasterDao {
    @Query("SELECT * FROM STATE_MASTER")
    suspend fun getAllStates(): List<StateMaster>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStates(stateMaster: StateMaster)
}