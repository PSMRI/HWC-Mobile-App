package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.cho.model.StatusOfWomanMaster

@Dao
interface StatusOfWomanDao {

    @Query("SELECT * FROM STATUS_OF_WOMAN_MASTER")
    suspend fun getAllStatusOfWoman(): List<StatusOfWomanMaster>

    @Query("SELECT * FROM STATUS_OF_WOMAN_MASTER WHERE statusID = :statusId")
    suspend fun getStatusById(statusId: Int): StatusOfWomanMaster?

    @Query("SELECT * FROM STATUS_OF_WOMAN_MASTER WHERE statusID IN (:statusIds)")
    suspend fun getStatusByIds(statusIds: List<Int>): List<StatusOfWomanMaster>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(statuses: List<StatusOfWomanMaster>)

    @Query("SELECT COUNT(*) FROM STATUS_OF_WOMAN_MASTER")
    suspend fun getCount(): Int
}
