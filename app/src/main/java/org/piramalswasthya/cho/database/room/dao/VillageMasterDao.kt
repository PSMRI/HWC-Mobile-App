package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.cho.model.VillageMaster

@Dao
interface VillageMasterDao {

    @Query("SELECT * FROM VILLAGE_MASTER WHERE blockID = :blockID")
    suspend fun getVillages(blockID: Int) : List<VillageMaster>
    @Query("SELECT * FROM VILLAGE_MASTER WHERE districtBranchID = :villageID")
    suspend fun getVillageById(villageID: Int) : VillageMaster
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVillage(villageMaster: VillageMaster)

}