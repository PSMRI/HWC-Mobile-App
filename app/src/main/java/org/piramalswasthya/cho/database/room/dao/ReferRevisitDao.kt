package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.cho.model.ReferRevisitModel


@Dao
interface ReferRevisitDao {
    @Query("SELECT * FROM REFER_REVISIT WHERE referId = :Id")
    fun getReferDetailsById(Id : String): LiveData<ReferRevisitModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReferRevisitDetails(referRevisitModel: ReferRevisitModel)
}