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
    @Query("SELECT * FROM BLOCK_MASTER WHERE blockID = :blockID")
    suspend fun getBlockById(blockID: Int) : BlockMaster

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBlock(blockMaster: BlockMaster)

}