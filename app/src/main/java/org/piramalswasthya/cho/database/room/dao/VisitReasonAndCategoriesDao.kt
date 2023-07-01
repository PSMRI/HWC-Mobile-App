package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.cho.model.VisitCategory
import org.piramalswasthya.cho.model.VisitReason

@Dao
interface VisitReasonsAndCategoriesDao {
    @Query("SELECT * FROM VISIT_REASON")
    suspend fun getVisitReasons(): List<VisitReason>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitReason(visitReason: VisitReason)

    @Query("SELECT * FROM VISIT_CATEGORY")
    suspend fun getVisitCategories(): List<VisitCategory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitCategory(visitReason: VisitCategory)
}