package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.cho.database.room.SyncStateValue
import org.piramalswasthya.cho.model.InfantRegCache
import org.piramalswasthya.cho.model.InfantRegWithPatient
import org.piramalswasthya.cho.model.PatientWithDeliveryOutcomeAndInfantRegCache
import org.piramalswasthya.cho.model.SyncStatusCache

@Dao
interface InfantRegDao {

    @Query("""
        SELECT * FROM INFANT_REG
        WHERE motherPatientID = :patientID
        AND babyIndex = :babyIndex
        AND isActive = 1
        ORDER BY updatedDate DESC, createdDate DESC, id DESC
        LIMIT 1
    """)
    suspend fun getInfantReg(patientID: String, babyIndex: Int): InfantRegCache?

    @Query("SELECT * FROM INFANT_REG WHERE childPatientID = :childPatientID limit 1")
    suspend fun getInfantRegFromChildPatientID(childPatientID: String): InfantRegCache?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveInfantReg(infantRegCache: InfantRegCache)

    @Query("SELECT * FROM INFANT_REG WHERE processed in ('N', 'U')")
    suspend fun getAllUnprocessedInfantReg(): List<InfantRegCache>

    @Update
    suspend fun updateInfantReg(it: InfantRegCache)

    @Query("select count(*) from INFANT_REG where isActive = 1 and motherPatientID = :patientID")
    suspend fun getNumBabiesRegistered(patientID: String): Int

    @Query("SELECT * FROM INFANT_REG WHERE motherPatientID in (:patientIDs) and isActive = 1")
    suspend fun getAllInfantRegs(patientIDs: Set<String>): List<InfantRegCache>

    /**
     * Get all patients with delivery outcome for infant registration
     * Returns patients who have active delivery outcome with liveBirth > 0
     */
    @Transaction
    @Query("""
        SELECT p.* FROM PATIENT p
        INNER JOIN DELIVERY_OUTCOME do ON p.patientID = do.patientID
        WHERE do.isActive = 1
        AND do.liveBirth > 0
        ORDER BY do.dateOfDelivery DESC
    """)
    fun getListForInfantRegister(): Flow<List<PatientWithDeliveryOutcomeAndInfantRegCache>>

    /**
     * Get count of infants eligible for registration (sum of liveBirth)
     */
    @Query("""
        SELECT SUM(do.liveBirth)
        FROM DELIVERY_OUTCOME do
        INNER JOIN PATIENT p ON do.patientID = p.patientID
        WHERE do.isActive = 1
        AND do.liveBirth > 0
    """)
    fun getInfantRegisterCount(): Flow<Int>

    @Query(
        """
        SELECT
            11 AS id,
            'Infant Reg.' AS name,
            COUNT(CASE WHEN ir.syncState = :syncedState THEN 1 END) AS synced,
            COUNT(CASE WHEN ir.syncState = :unsyncedState THEN 1 END) AS notSynced,
            COUNT(CASE WHEN ir.syncState = :syncingState THEN 1 END) AS syncing
        FROM INFANT_REG ir
        WHERE ir.isActive = 1
        """
    )
    fun getInfantRegSyncStatus(
        syncedState: Int = SyncStateValue.SYNCED,
        syncingState: Int = SyncStateValue.SYNCING,
        unsyncedState: Int = SyncStateValue.UNSYNCED
    ): Flow<List<SyncStatusCache>>

    /**
     * Get patient with delivery outcome and infant reg by patientID
     */
    @Transaction
    @Query("SELECT * FROM PATIENT WHERE patientID = :patientID")
    suspend fun getPatientWithDeliveryOutcomeAndInfantRegByID(patientID: String): PatientWithDeliveryOutcomeAndInfantRegCache?

    /**
     * Get all registered infants for child registration list
     * Returns active infants from INFANT_REG (independent of mother PATIENT completeness).
     */
    @Transaction
    @Query("""
        SELECT ir.* FROM INFANT_REG ir
        WHERE ir.isActive = 1
        ORDER BY ir.updatedDate DESC, ir.createdDate DESC
    """)
    fun getAllRegisteredInfants(): Flow<List<InfantRegWithPatient>>

    /**
     * Get count of registered infants
     */
    @Query("""
        SELECT COUNT(*) FROM INFANT_REG ir
        WHERE ir.isActive = 1
    """)
    fun getAllRegisteredInfantsCount(): Flow<Int>

    @Query(
        """
        SELECT
            12 AS id,
            'Child Reg.' AS name,
            COUNT(CASE WHEN ir.syncState = :syncedState THEN 1 END) AS synced,
            COUNT(CASE WHEN ir.syncState = :unsyncedState THEN 1 END) AS notSynced,
            COUNT(CASE WHEN ir.syncState = :syncingState THEN 1 END) AS syncing
        FROM INFANT_REG ir
        WHERE ir.isActive = 1
          AND (ir.childPatientID IS NOT NULL OR ir.processed = 'C')
        """
    )
    fun getChildRegSyncStatus(
        syncedState: Int = SyncStateValue.SYNCED,
        syncingState: Int = SyncStateValue.SYNCING,
        unsyncedState: Int = SyncStateValue.UNSYNCED
    ): Flow<List<SyncStatusCache>>
}
