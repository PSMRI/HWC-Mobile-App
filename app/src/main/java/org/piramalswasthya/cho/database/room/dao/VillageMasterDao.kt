package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Query
import org.piramalswasthya.cho.model.VillageMaster


@Dao
interface VillageMasterDao {

    @Query("SELECT * FROM VILLAGE_MASTER WHERE blockID = :blockID")
    suspend fun getVillages(blockID: Int) : List<VillageMaster>

}