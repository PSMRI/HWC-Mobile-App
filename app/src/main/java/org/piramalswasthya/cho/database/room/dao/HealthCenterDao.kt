package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.cho.model.HigherHealthCenter


@Dao
interface HealthCenterDao {

    @Query("SELECT * FROM HEALTH_CENTER")
    fun getHealthCenter() : LiveData<List<HigherHealthCenter>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthCenter(higherHealthCenter: HigherHealthCenter)

}