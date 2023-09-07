package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.model.VisitCategory
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.model.VisitReason

@Dao
interface VisitReasonsAndCategoriesDao {
    @Query("SELECT * FROM VISIT_REASON")
    suspend fun getVisitReasons(): List<VisitReason>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitReason(visitReason: VisitReason)

    @Query("SELECT * FROM VISIT_CATEGORY")
    suspend fun getVisitCategories(): List<VisitCategory>

//    @Query("SELECT * FROM VISIT_DB WHERE visitId = :id")
//     fun getVisitDb(id:String):LiveData<VisitDB>
//
//    @Query("SELECT * FROM Chielf_Complaint_DB WHERE id = :id")
//     fun getChiefComplaintDb(id:String):LiveData<ChiefComplaintDB>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitCategory(visitReason: VisitCategory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitDB(visitDB: VisitDB)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChiefComplaintDb(chiefComplaintDB: ChiefComplaintDB)


}