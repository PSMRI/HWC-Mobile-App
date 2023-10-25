package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.piramalswasthya.cho.model.PNCVisitCache

@Dao
interface PncDao {

    @Query("select * from pnc_visit where patientID = :patientID and pncPeriod = :visitNumber and isActive = 1 limit 1")
    suspend fun getSavedRecord(patientID: String, visitNumber: Int): PNCVisitCache?

    @Query("select * from pnc_visit where patientID = :patientID and isActive = 1 order by pncPeriod desc limit 1")
    suspend fun getLastSavedRecord(patientID: String): PNCVisitCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pncCache: PNCVisitCache)

    @Query("SELECT * FROM pnc_visit WHERE processed in ('N', 'U')")
    suspend fun getAllUnprocessedPncVisits(): List<PNCVisitCache>

    @Update
    suspend fun update(vararg pnc: PNCVisitCache)
    @Query("select * from pnc_visit where patientID in (:eligBenIds) and isActive = 1")
    suspend fun getAllPNCs(eligBenIds: Set<String>): List<PNCVisitCache>


}