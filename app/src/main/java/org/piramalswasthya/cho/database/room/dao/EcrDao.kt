package org.piramalswasthya.cho.database.room.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.cho.model.EligibleCoupleRegCache
import org.piramalswasthya.cho.model.EligibleCoupleTrackingCache
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientWithECRCache

@Dao
interface EcrDao {

    // ===== Eligible Couple Registration Queries =====
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg ecrCache: EligibleCoupleRegCache)

    @Query("SELECT * FROM ELIGIBLE_COUPLE_REG WHERE processed in ('N', 'U')")
    suspend fun getAllUnprocessedECR(): List<EligibleCoupleRegCache>

    @Query("select count(*) from ELIGIBLE_COUPLE_REG")
    suspend fun ecrCount(): Int

    @Query("SELECT * FROM ELIGIBLE_COUPLE_REG WHERE patientID = :patientId limit 1")
    suspend fun getSavedECR(patientId: String): EligibleCoupleRegCache?

    @Update
    suspend fun update(it: EligibleCoupleRegCache)

    @Transaction
    @Query("SELECT * FROM PATIENT")
    fun getAllPatientsWithECR(): Flow<List<PatientWithECRCache>>

    @Transaction
    @Query("SELECT * FROM PATIENT WHERE patientID = :patientId")
    suspend fun getPatientWithECR(patientId: String): PatientWithECRCache?

    // ===== Eligible Couple Tracking Queries =====
    
    @Query("SELECT * FROM ELIGIBLE_COUPLE_TRACKING WHERE processed in ('N','U')")
    suspend fun getAllUnprocessedECT(): List<EligibleCoupleTrackingCache>

    @Query("SELECT * FROM ELIGIBLE_COUPLE_TRACKING WHERE patientID = :benId")
    suspend fun getAllECT(benId: String): List<EligibleCoupleTrackingCache>
    @Update
    suspend fun updateEligibleCoupleTracking(it: EligibleCoupleTrackingCache)

    @Query("select * from eligible_couple_tracking where patientID = :patientID and visitDate =:visitDate limit 1")
//    @Query("select * from eligible_couple_tracking where benId = :benId and CAST((strftime('%s','now') - visitDate/1000)/60/60/24 AS INTEGER) < 30 order by visitDate limit 1")
    suspend fun getEct(patientID: String, visitDate : Long): EligibleCoupleTrackingCache?

    @Query(
        """
        SELECT * FROM eligible_couple_tracking
        WHERE patientID = :patientID
        AND date(visitDate / 1000, 'unixepoch', 'localtime') = date(:visitDate / 1000, 'unixepoch', 'localtime')
        LIMIT 1
        """
    )
    suspend fun getEctByVisitDay(patientID: String, visitDate: Long): EligibleCoupleTrackingCache?

    @Query("select * from eligible_couple_tracking where patientID = :patientID order by visitDate desc limit 1")
    suspend fun getLatestEct(patientID: String) : EligibleCoupleTrackingCache?

    @Query("""
        SELECT p.* FROM PATIENT p
        LEFT JOIN (
            SELECT patientID, MAX(visitDate) AS lastVisitDate
            FROM ELIGIBLE_COUPLE_TRACKING
            GROUP BY patientID
        ) ect ON ect.patientID = p.patientID
        WHERE p.statusOfWomanID = 1
        AND p.genderID = 2
        AND p.age BETWEEN 15 AND 49
        ORDER BY COALESCE(ect.lastVisitDate, 0) DESC, p.registrationDate DESC
    """)
    fun getPatientsForTrackingList(): Flow<List<Patient>>

    @Query("""
        SELECT COUNT(*) FROM PATIENT
        WHERE statusOfWomanID = 1
        AND genderID = 2
        AND age BETWEEN 15 AND 49
    """)
    fun getEligibleCoupleTrackingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg eligibleCoupleTrackingCache: EligibleCoupleTrackingCache)

//    @Query("select count(*)>0 from eligible_couple_tracking where createdDate=:createdDate")
//    suspend fun ectWithsameCreateDateExists(createdDate: Long): Boolean


}
