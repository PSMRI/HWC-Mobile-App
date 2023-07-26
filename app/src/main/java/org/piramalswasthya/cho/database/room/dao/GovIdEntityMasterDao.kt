package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Query
import org.piramalswasthya.cho.model.GovIdEntityMaster

@Dao
interface GovIdEntityMasterDao {

    @Query("SELECT * FROM GOV_ID_ENTITY_MASTER")
    suspend fun getGovIdEntityMaster() : List<GovIdEntityMaster>

}