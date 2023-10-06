package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.cho.model.HigherHealthCenter
import org.piramalswasthya.cho.model.OutreachDropdownList
import org.piramalswasthya.cho.model.PatientVitalsModel

@Dao
interface OutreachDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOutreachList(outreachDropdownList: OutreachDropdownList)

    @Query("SELECT * FROM Outreach_Dropdown_List")
     fun getOutreachDropdownList(): LiveData<List<OutreachDropdownList>>
    @Query("select * from Outreach_Dropdown_List")
    suspend fun getOMap():List<OutreachDropdownList>
}