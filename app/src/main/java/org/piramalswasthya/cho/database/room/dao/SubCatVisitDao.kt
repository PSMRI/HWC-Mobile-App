package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.cho.model.SubVisitCategory

@Dao
interface SubCatVisitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubCat(subCat: SubVisitCategory)

    @Query("select * from SUB_VISIT_CAT")
    fun getAllSubCatVisit(): LiveData<List<SubVisitCategory>>
}