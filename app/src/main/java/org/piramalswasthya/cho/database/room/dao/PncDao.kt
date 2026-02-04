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
     * Excludes patients who have completed the 42-day PNC visit
     */
    @Query("""
        SELECT DISTINCT do.patientID FROM DELIVERY_OUTCOME do
        WHERE do.isActive = 1
        AND do.dateOfDelivery IS NOT NULL
        AND NOT EXISTS (
            SELECT 1 FROM pnc_visit pnc
            WHERE pnc.patientID = do.patientID
            AND pnc.isActive = 1
            AND pnc.pncPeriod = 42
        )
        ORDER BY do.dateOfDelivery DESC
    """)
    fun getPNCMothersPatientIDs(): Flow<List<String>>

    /**
     * Get count of PNC mothers
     * Excludes patients who have completed the 42-day PNC visit
     */
    @Query("""
        SELECT COUNT(DISTINCT do.patientID) FROM DELIVERY_OUTCOME do
        WHERE do.isActive = 1
        AND do.dateOfDelivery IS NOT NULL
        AND NOT EXISTS (
            SELECT 1 FROM pnc_visit p
            WHERE p.patientID = do.patientID
            AND p.isActive = 1
            AND p.pncPeriod = 42
        )
    """)
    fun getPNCMothersCount(): Flow<Int>

    /**
     * Get patient with delivery outcome and PNC by patientID
     */
    @Transaction
    @Query("SELECT * FROM PATIENT WHERE patientID = :patientID")
    suspend fun getPatientWithDeliveryOutcomeAndPncByID(patientID: String): PatientWithDeliveryOutcomeAndPncCache?

    /**
     * Get all PNC mothers with their delivery outcome and PNC data in a single query
     * Filters for females (genderID=2) aged 15-49 who have delivered and are eligible for PNC
     */
    @Transaction
    @Query("""
        SELECT DISTINCT p.* FROM PATIENT p
        INNER JOIN DELIVERY_OUTCOME do ON p.patientID = do.patientID
        WHERE do.isActive = 1
        AND do.dateOfDelivery IS NOT NULL
        AND p.genderID = 2
        AND p.age BETWEEN 15 AND 49
        AND NOT EXISTS (
            SELECT 1 FROM pnc_visit pnc
            WHERE pnc.patientID = p.patientID
            AND pnc.isActive = 1
            AND pnc.pncPeriod = 42
        )
        ORDER BY do.dateOfDelivery DESC
    """)
    fun getAllPNCMothersWithData(): Flow<List<PatientWithDeliveryOutcomeAndPncCache>>

}