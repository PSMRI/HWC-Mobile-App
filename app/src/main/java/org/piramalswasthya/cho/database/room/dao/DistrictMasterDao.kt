package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.cho.model.BlockMaster
import org.piramalswasthya.cho.model.DistrictMaster
import org.piramalswasthya.cho.model.StateMaster

@Dao
interface DistrictMasterDao {

    @Query("SELECT * FROM DISTRICT_MASTER WHERE stateID = :stateID")
    suspend fun getDistricts(stateID: Int) : List<DistrictMaster>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(blockMaster: BlockMaster)

}