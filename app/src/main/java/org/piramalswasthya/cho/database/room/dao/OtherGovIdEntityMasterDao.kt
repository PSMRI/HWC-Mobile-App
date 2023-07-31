package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Query
import org.piramalswasthya.cho.model.OtherGovIdEntityMaster

@Dao
interface OtherGovIdEntityMasterDao {

    @Query("SELECT * FROM OTHER_GOV_ID_ENTITY_MASTER")
    suspend fun getOtherGovIdEntityMaster() : List<OtherGovIdEntityMaster>

}