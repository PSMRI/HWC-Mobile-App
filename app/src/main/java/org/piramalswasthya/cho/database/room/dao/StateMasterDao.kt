package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.cho.model.DistrictMaster
import org.piramalswasthya.cho.model.StateMaster

@Dao
interface StateMasterDao {

    @Query("SELECT * FROM STATE_MASTER")
    suspend fun getAllStates(): List<StateMaster>
    @Query("SELECT * FROM STATE_MASTER where stateID = :stateID")
    suspend fun getStateById(stateID : Int): StateMaster?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStates(stateMaster: StateMaster)

}