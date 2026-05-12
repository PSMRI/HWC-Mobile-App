package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.cho.database.room.SyncModuleIds
import org.piramalswasthya.cho.database.room.SyncStateValue
import org.piramalswasthya.cho.model.PNCVisitCache
import org.piramalswasthya.cho.model.PatientWithDeliveryOutcomeAndPncCache
import org.piramalswasthya.cho.model.SyncStatusCache

@Dao
interface PncDao {

    @Query("select * from pnc_visit where patientID = :patientID and pncPeriod = :visitNumber and isActive = 1 limit 1")
    suspend fun getSavedRecord(patientID: String, visitNumber: Int): PNCVisitCache?

    @Query("select pncPeriod from pnc_visit where patientID = :patientID order by pncPeriod desc limit 1")
    suspend fun getLastVisitNumber(patientID: String): Int?

    @Query("select * from pnc_visit where patientID = :patientID and isActive = 1 order by pncPeriod desc limit 1")
    suspend fun getLastSavedRecord(patientID: String): PNCVisitCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pncCache: PNCVisitCache): Long

    @Query("SELECT * FROM pnc_visit WHERE processed in ('N', 'U')")
    suspend fun getAllUnprocessedPncVisits(): List<PNCVisitCache>

    @Update
    suspend fun update(vararg pnc: PNCVisitCache)

    @Query("select * from pnc_visit where patientID in (:eligBenIds) and isActive = 1")
    suspend fun getAllPNCs(eligBenIds: Set<String>): List<PNCVisitCache>

    @Query("select * from pnc_visit where patientID = :patientID and isActive = 1")
    suspend fun getAllPNCsByPatId(patientID: String): List<PNCVisitCache>

    @Query(
        """
        SELECT
            ${SyncModuleIds.PNC} AS id,
            'PNC' AS name,
            COUNT(CASE WHEN pnc.syncState = :syncedState THEN 1 END) AS synced,
            COUNT(CASE WHEN pnc.syncState = :unsyncedState THEN 1 END) AS notSynced,
            COUNT(CASE WHEN pnc.syncState = :syncingState THEN 1 END) AS syncing
        FROM PNC_VISIT pnc
        WHERE pnc.isActive = 1
        """
    )
    fun getPncSyncStatus(
        syncedState: Int = SyncStateValue.SYNCED,
        syncingState: Int = SyncStateValue.SYNCING,
        unsyncedState: Int = SyncStateValue.UNSYNCED
    ): Flow<List<SyncStatusCache>>

    /**
     * Get patientIDs of women eligible for PNC mothers list.
     * Source: DeliveryOutcome + Patient (postnatal), excluding completed 42-day PNC.
     */
    @Query("""
        SELECT DISTINCT do.patientID
        FROM DELIVERY_OUTCOME do
        INNER JOIN PATIENT p ON do.patientID = p.patientID
        WHERE do.isActive = 1
          AND do.dateOfDelivery IS NOT NULL
          AND p.genderID = 2
          AND p.age BETWEEN 15 AND 49
          AND p.maritalStatusID = 2
          AND p.statusOfWomanID = 3
          AND NOT EXISTS (
              SELECT 1 FROM PNC_VISIT p42
              WHERE p42.patientID = do.patientID
                AND p42.isActive = 1
                AND p42.pncPeriod = 42
          )
    """)
    fun getPNCMothersPatientIDs(): Flow<List<String>>

    /**
     * Get count of women eligible for PNC mothers list.
     */
    @Query("""
        SELECT COUNT(DISTINCT do.patientID)
        FROM DELIVERY_OUTCOME do
        INNER JOIN PATIENT p ON do.patientID = p.patientID
        WHERE do.isActive = 1
          AND do.dateOfDelivery IS NOT NULL
          AND p.genderID = 2
          AND p.age BETWEEN 15 AND 49
          AND p.maritalStatusID = 2
          AND p.statusOfWomanID = 3
          AND NOT EXISTS (
              SELECT 1 FROM PNC_VISIT p42
              WHERE p42.patientID = do.patientID
                AND p42.isActive = 1
                AND p42.pncPeriod = 42
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
     * Get all PNC mothers with their delivery outcome and PNC data.
     * Source of truth: DeliveryOutcome + Patient status postnatal.
     */
    @Transaction
    @Query("""
        SELECT DISTINCT p.*
        FROM PATIENT p
        INNER JOIN DELIVERY_OUTCOME do ON p.patientID = do.patientID
        WHERE do.isActive = 1
          AND do.dateOfDelivery IS NOT NULL
          AND p.genderID = 2
          AND p.age BETWEEN 15 AND 49
          AND p.maritalStatusID = 2
          AND p.statusOfWomanID = 3
          AND NOT EXISTS (
              SELECT 1 FROM PNC_VISIT p42
              WHERE p42.patientID = p.patientID
                AND p42.isActive = 1
                AND p42.pncPeriod = 42
          )
        ORDER BY p.registrationDate DESC
    """)
    fun getAllPNCMothersWithData(): Flow<List<PatientWithDeliveryOutcomeAndPncCache>>

}
