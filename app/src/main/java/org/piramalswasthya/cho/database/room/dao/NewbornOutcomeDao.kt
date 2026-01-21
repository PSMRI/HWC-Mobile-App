package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.cho.model.NeonateDetailsCache
import org.piramalswasthya.cho.model.NewbornOutcomeCache

@Dao
interface NewbornOutcomeDao {

    // ========== NEWBORN OUTCOME (Main Record) ==========
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewbornOutcome(newbornOutcome: NewbornOutcomeCache): Long
    
    @Update
    suspend fun updateNewbornOutcome(newbornOutcome: NewbornOutcomeCache)
    
    @Query("SELECT * FROM NEWBORN_OUTCOME WHERE motherPatientID = :patientID AND isActive = 1 ORDER BY updatedDate DESC LIMIT 1")
    fun getActiveNewbornOutcomeByPatientID(patientID: String): Flow<NewbornOutcomeCache?>
    
    @Query("SELECT * FROM NEWBORN_OUTCOME WHERE motherPatientID = :patientID AND isActive = 1")
    fun getAllActiveNewbornOutcomesByPatientID(patientID: String): Flow<List<NewbornOutcomeCache>>
    
    @Query("SELECT * FROM NEWBORN_OUTCOME WHERE id = :id")
    suspend fun getNewbornOutcomeById(id: Long): NewbornOutcomeCache?
    
    // ========== NEONATE DETAILS (Child Records) ==========
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNeonateDetails(neonateDetails: NeonateDetailsCache): Long
    
    @Update
    suspend fun updateNeonateDetails(neonateDetails: NeonateDetailsCache)
    
    @Query("SELECT * FROM NEONATE_DETAILS WHERE newbornOutcomeID = :newbornOutcomeID AND isActive = 1 ORDER BY neonateIndex")
    fun getNeonateDetailsByOutcomeID(newbornOutcomeID: Long): Flow<List<NeonateDetailsCache>>
    
    @Query("SELECT * FROM NEONATE_DETAILS WHERE newbornOutcomeID = :newbornOutcomeID AND neonateIndex = :index AND isActive = 1")
    suspend fun getNeonateDetailsByIndex(newbornOutcomeID: Long, index: Int): NeonateDetailsCache?
    
    @Query("SELECT * FROM NEONATE_DETAILS WHERE id = :id")
    suspend fun getNeonateDetailsById(id: Long): NeonateDetailsCache?
    
    @Query("SELECT * FROM NEONATE_DETAILS WHERE neonateID = :neonateID")
    suspend fun getNeonateDetailsByNeonateID(neonateID: String): NeonateDetailsCache?
    
    // ========== COMBINED QUERIES ==========
    
    @Transaction
    suspend fun saveCompleteNewbornOutcome(
        newbornOutcome: NewbornOutcomeCache,
        neonateDetailsList: List<NeonateDetailsCache>
    ) {
        val outcomeId = insertNewbornOutcome(newbornOutcome)
        neonateDetailsList.forEach { neonate ->
            val updatedNeonate = neonate.copy(newbornOutcomeID = outcomeId)
            insertNeonateDetails(updatedNeonate)
        }
    }
    
    // Get count of live births for a mother
    @Query("""
        SELECT COUNT(*) FROM NEONATE_DETAILS nd
        INNER JOIN NEWBORN_OUTCOME nbo ON nd.newbornOutcomeID = nbo.id
        WHERE nbo.motherPatientID = :patientID 
        AND nd.outcomeAtBirth = 'Live Birth'
        AND nd.isActive = 1
        AND nbo.isActive = 1
    """)
    fun getLiveBirthCountByPatientID(patientID: String): Flow<Int>
    
    // Get all neonates for a mother (for tracking)
    @Query("""
        SELECT nd.* FROM NEONATE_DETAILS nd
        INNER JOIN NEWBORN_OUTCOME nbo ON nd.newbornOutcomeID = nbo.id
        WHERE nbo.motherPatientID = :patientID 
        AND nd.isActive = 1
        AND nbo.isActive = 1
        ORDER BY nbo.updatedDate DESC, nd.neonateIndex ASC
    """)
    fun getAllNeonatesByPatientID(patientID: String): Flow<List<NeonateDetailsCache>>
}
