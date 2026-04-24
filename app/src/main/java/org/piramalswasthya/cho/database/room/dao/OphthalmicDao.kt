package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.piramalswasthya.cho.database.room.SyncStateValue
import org.piramalswasthya.cho.model.OphthalmicVisit

@Dao
interface OphthalmicDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOphthalmicVisit(visit: OphthalmicVisit)

    @Update
    suspend fun updateOphthalmicVisit(visit: OphthalmicVisit)

    @Query("SELECT * FROM OPHTHALMIC_VISIT WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun getOphthalmicVisit(patientID: String, benVisitNo: Int): OphthalmicVisit?

    @Query("SELECT * FROM OPHTHALMIC_VISIT WHERE visitId = :visitId")
    suspend fun getOphthalmicVisitById(visitId: String): OphthalmicVisit?
    
    @Query("SELECT * FROM OPHTHALMIC_VISIT WHERE syncState = :unsyncedState")
    suspend fun getUnsyncedOphthalmicVisits(
        unsyncedState: Int = SyncStateValue.UNSYNCED
    ): List<OphthalmicVisit>
}
