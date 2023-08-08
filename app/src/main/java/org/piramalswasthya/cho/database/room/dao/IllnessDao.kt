package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.cho.model.IllnessDropdown

@Dao
interface IllnessDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIllnessDropdown(illnessDropdown: IllnessDropdown)

    @Query("select * from Illness_Dropdown")
    fun getAllIllnessDropdown(): LiveData<List<IllnessDropdown>>
}