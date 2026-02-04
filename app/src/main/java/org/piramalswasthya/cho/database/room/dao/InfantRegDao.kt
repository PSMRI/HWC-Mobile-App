package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.cho.model.InfantRegCache
import org.piramalswasthya.cho.model.InfantRegWithPatient
import org.piramalswasthya.cho.model.PatientWithDeliveryOutcomeAndInfantRegCache

@Dao
interface InfantRegDao {

    @Query("SELECT * FROM INFANT_REG WHERE motherPatientID = :patientID and babyIndex = :babyIndex and isActive = 1 limit 1")
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
        AND p.genderID = 2
        AND p.age BETWEEN 15 AND 49
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
        AND p.genderID = 2
        AND p.age BETWEEN 15 AND 49
    """)
    fun getInfantRegisterCount(): Flow<Int>

    /**
     * Get patient with delivery outcome and infant reg by patientID
     */
    @Transaction
    @Query("SELECT * FROM PATIENT WHERE patientID = :patientID")
    suspend fun getPatientWithDeliveryOutcomeAndInfantRegByID(patientID: String): PatientWithDeliveryOutcomeAndInfantRegCache?

    /**
     * Get all registered infants for child registration list
     * Returns infants that are active and have been registered
     */
    @Transaction
    @Query("""
        SELECT ir.* FROM INFANT_REG ir
        INNER JOIN PATIENT p ON ir.motherPatientID = p.patientID
        WHERE ir.isActive = 1
        AND p.genderID = 2
        AND p.age BETWEEN 15 AND 49
        ORDER BY ir.createdDate DESC
    """)
    fun getAllRegisteredInfants(): Flow<List<InfantRegWithPatient>>

    /**
     * Get count of registered infants
     */
    @Query("""
        SELECT COUNT(*) FROM INFANT_REG ir
        INNER JOIN PATIENT p ON ir.motherPatientID = p.patientID
        WHERE ir.isActive = 1
        AND p.genderID = 2
        AND p.age BETWEEN 15 AND 49
    """)
    fun getAllRegisteredInfantsCount(): Flow<Int>
}
