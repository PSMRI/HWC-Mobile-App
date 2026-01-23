package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.cho.model.PNCVisitCache
import org.piramalswasthya.cho.model.PatientWithDeliveryOutcomeAndPncCache

@Dao
interface PncDao {

    @Query("select * from pnc_visit where patientID = :patientID and pncPeriod = :visitNumber and isActive = 1 limit 1")
    suspend fun getSavedRecord(patientID: String, visitNumber: Int): PNCVisitCache?

    @Query("select pncPeriod from pnc_visit where patientID = :patientID order by pncPeriod desc limit 1")
    suspend fun getLastVisitNumber(patientID: String): Int?

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

    @Query("select * from pnc_visit where patientID = :patientID and isActive = 1")
    suspend fun getAllPNCsByPatId(patientID: String): List<PNCVisitCache>

    /**
     * Get patientIDs of women eligible for PNC (have delivered and within 42 days or not completed all visits)
     */
    @Query("""
        SELECT DISTINCT do.patientID FROM DELIVERY_OUTCOME do
        LEFT OUTER JOIN pnc_visit pnc ON do.patientID = pnc.patientID
        WHERE do.isActive = 1
        AND do.dateOfDelivery IS NOT NULL
        AND (pnc.isActive IS NULL OR pnc.isActive = 1)
        AND (pnc.pncPeriod IS NULL OR pnc.pncPeriod != 42)
        ORDER BY do.dateOfDelivery DESC
    """)
    fun getPNCMothersPatientIDs(): Flow<List<String>>

    /**
     * Get count of PNC mothers
     */
    @Query("""
        SELECT COUNT(DISTINCT do.patientID) FROM DELIVERY_OUTCOME do
        LEFT OUTER JOIN pnc_visit pnc ON do.patientID = pnc.patientID
        WHERE do.isActive = 1
        AND do.dateOfDelivery IS NOT NULL
        AND (pnc.isActive IS NULL OR pnc.isActive = 1)
        AND (pnc.pncPeriod IS NULL OR pnc.pncPeriod != 42)
    """)
    fun getPNCMothersCount(): Flow<Int>

    /**
     * Get patient with delivery outcome and PNC by patientID
     */
    @Transaction
    @Query("SELECT * FROM PATIENT WHERE patientID = :patientID")
    suspend fun getPatientWithDeliveryOutcomeAndPncByID(patientID: String): PatientWithDeliveryOutcomeAndPncCache?

}