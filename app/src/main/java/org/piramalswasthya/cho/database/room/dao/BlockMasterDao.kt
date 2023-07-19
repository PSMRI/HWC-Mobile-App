package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.cho.model.BlockMaster
import org.piramalswasthya.cho.model.VillageMaster

@Dao
interface BlockMasterDao {

    @Query("SELECT * FROM BLOCK_MASTER WHERE districtID = :districtID")
    suspend fun getBlocks(districtID: Int) : List<BlockMaster>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(blockMaster: BlockMaster)

}