package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.cho.model.ChiefComplaintMaster

@Dao
interface ChiefComplaintMasterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChiefCompMaster(chiefComplaintMaster: ChiefComplaintMaster)

    @Query("select * from CHIEF_COMPLAINT_MASTER")
    fun getAllChiefCompMaster(): Flow<List<ChiefComplaintMaster>>

    @Query("select * from CHIEF_COMPLAINT_MASTER")
    suspend fun getChiefCompMasterMap(): List<ChiefComplaintMaster>
}