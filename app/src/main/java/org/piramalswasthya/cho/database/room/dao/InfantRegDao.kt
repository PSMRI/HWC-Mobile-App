package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.piramalswasthya.cho.model.InfantRegCache

@Dao
interface InfantRegDao {

    @Query("SELECT * FROM INFANT_REG WHERE motherPatientID = :patientID and babyIndex = :babyIndex and isActive = 1 limit 1")
    suspend fun getInfantReg(patientID: String, babyIndex: Int): InfantRegCache?

    @Query("SELECT * FROM INFANT_REG WHERE childPatientID = :patientID and isActive = 1 limit 1")
    suspend fun getInfantRegFromChildPatientID(patientID: String): InfantRegCache?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveInfantReg(infantRegCache: InfantRegCache)

    @Query("SELECT * FROM INFANT_REG WHERE processed in ('N', 'U')")
    suspend fun getAllUnprocessedInfantReg(): List<InfantRegCache>

    @Update
    suspend fun updateInfantReg(it: InfantRegCache)

    @Query("SELECT count(*) FROM INFANT_REG WHERE isActive = 1 and motherPatientID = :patientID")
    suspend fun getNumBabiesRegistered(patientID: String): Int

    @Query("SELECT * FROM INFANT_REG WHERE motherPatientID in (:patientIDs) and isActive = 1")
    suspend fun getAllInfantRegs(patientIDs: Set<String>): List<InfantRegCache>
}
