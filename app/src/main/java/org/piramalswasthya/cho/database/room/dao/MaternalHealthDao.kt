package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.cho.model.*

@Dao
interface MaternalHealthDao {
    @Query("select * from pregnancy_register where patientID = :patientID and active = 1 limit 1")
    suspend fun getSavedRecord(patientID: String): PregnantWomanRegistrationCache?

//    @Query("select * from pregnancy_register where patientID = :patientID and active = 1 limit 1")
//    fun getSavedRecordObserve(patientID: String): LiveData<PregnantWomanRegistrationCache?>

    @Query("select * from pregnancy_register where patientID = :benId and active = 1 order by createdDate limit 1")
    suspend fun getSavedActiveRecord(benId: String): PregnantWomanRegistrationCache?

//    @Query("select * from pregnancy_register where patientID = :benId and active = 1 order by createdDate limit 1")
//    fun getSavedActiveRecordObserve(benId: String): LiveData<PregnantWomanRegistrationCache?>

    @Query("select * from pregnancy_anc where patientID = :patientID order by ancDate desc limit 1")
    suspend fun getLastAnc(patientID: String): PregnantWomanAncCache?

    @Query("select visitNumber from pregnancy_anc where patientID = :patientID order by visitNumber desc limit 1")
    suspend fun getLastVisitNumber(patientID: String): Int?

    @Query("select * from pregnancy_anc where patientID = :patientID and visitNumber = :visitNumber limit 1")
    suspend fun getSavedRecord(patientID: String, visitNumber: Int): PregnantWomanAncCache?

    @Query("select * from pregnancy_anc where isActive = 1 and patientID = :patientID")
    suspend fun getAllActiveAncRecords(patientID: String): List<PregnantWomanAncCache>

//    @Query("select * from pregnancy_anc where isActive = 1 and patientID = :patientID")
//    fun getAllActiveAncRecordsObserve(patientID: String): LiveData<List<PregnantWomanAncCache>>
//    @Query("select * from pregnancy_anc where benId in (:benId) and isActive = 1")
//    fun getAllActiveAncRecords(benId: Set<Long>): List<PregnantWomanAncCache>
//
//    @Query("select * from pregnancy_register where benId in (:benId)")
//    fun getAllActivePwrRecords(benId: Set<Long>): List<PregnantWomanRegistrationCache>
//
//    @Query("select * from pregnancy_anc where benId = :benId order by ancDate desc limit 1")
//    fun getLatestAnc(benId: Long): PregnantWomanAncCache?
//
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecord(pregnancyRegistrationForm: PregnantWomanRegistrationCache)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecord(ancCache: PregnantWomanAncCache)
//
//    @Query("select benId, visitNumber, 0 as filledWeek from pregnancy_anc where benId = :benId order by visitNumber")
//    suspend fun getAllAncRecordsFor(
//        benId: Long,
//    ): List<AncStatus>
//
//    @Query("select * from pregnancy_register reg left outer join pregnancy_anc anc on reg.benId=anc.benId where reg.active = 1 and (anc.benId is null or anc.isActive = 1)")
//    fun getAllPregnancyRecords(): Flow<Map<PregnantWomanRegistrationCache, List<PregnantWomanAncCache>>>
//
//    @Query("select count(*) from HRP_NON_PREGNANT_ASSESS assess where isHighRisk = 1")
//    fun getAllECRecords(): Flow<Int>
//
    @Query("SELECT * FROM pregnancy_anc WHERE processed in ('N', 'U')")
    suspend fun getAllUnprocessedAncVisits(): List<PregnantWomanAncCache>

    @Query("SELECT * FROM pregnancy_register WHERE processed in ('N', 'U')")
    suspend fun getAllUnprocessedPWRs(): List<PregnantWomanRegistrationCache>

    @Update
    suspend fun updateANC(vararg it: PregnantWomanAncCache)

    @Update
    suspend fun updatePwr(vararg it: PregnantWomanRegistrationCache)

    /**
     * Get all patients with their pregnancy registration data
     * Returns Flow for reactive updates
     */
    @Transaction
    @Query("SELECT * FROM PATIENT WHERE genderID = 2 AND age BETWEEN 15 AND 49 ORDER BY registrationDate DESC")
    fun getAllPatientsWithPWR(): Flow<List<PatientWithPwrCache>>

    /**
     * Get specific patient with pregnancy registration
     */
    @Transaction
    @Query("SELECT * FROM PATIENT WHERE patientID = :patientID")
    suspend fun getPatientWithPWR(patientID: String): PatientWithPwrCache?

    /**
     * Get count of pregnant women registrations
     */
    @Query("SELECT COUNT(DISTINCT patientID) FROM pregnancy_register WHERE active = 1")
    fun getPWRCount(): Flow<Int>

    /**
     * Get patientIDs of women who have delivered (pregnantWomanDelivered = true in ANC)
     */
    @Query("""
        SELECT DISTINCT anc.patientID FROM pregnancy_anc anc
        INNER JOIN pregnancy_register pwr ON anc.patientID = pwr.patientID
        WHERE anc.isActive = 1
        AND anc.pregnantWomanDelivered = 1
        AND pwr.active = 1
        ORDER BY anc.updatedDate DESC
    """)
    fun getDeliveredWomenPatientIDs(): Flow<List<String>>

    /**
     * Get count of delivered women
     */
    @Query("""
        SELECT COUNT(DISTINCT anc.patientID) FROM pregnancy_anc anc
        INNER JOIN pregnancy_register pwr ON anc.patientID = pwr.patientID
        WHERE anc.isActive = 1
        AND anc.pregnantWomanDelivered = 1
        AND pwr.active = 1
    """)
    fun getDeliveredWomenCount(): Flow<Int>

    /**
     * Get patient with PWR by patientID
     */
    @Transaction
    @Query("SELECT * FROM PATIENT WHERE patientID = :patientID")
    suspend fun getPatientWithPWRByID(patientID: String): PatientWithPwrCache?

    /**
     * Get all patients with abortion records (isAborted = 1 and abortionDate is not null)
     * Joins Patient, PWR, and ANC to find women with abortions
     */
    @Transaction
    @Query("""
        SELECT DISTINCT p.* FROM PATIENT p
        INNER JOIN PREGNANCY_ANC anc ON p.patientID = anc.patientID
        WHERE anc.isAborted = 1
        AND anc.abortionDate IS NOT NULL
        AND p.genderID = 2
        AND p.age BETWEEN 15 AND 49
        ORDER BY anc.abortionDate DESC
    """)
    fun getAllAbortionWomenList(): Flow<List<PatientWithPwrAndAncCache>>

    /**
     * Get count of abortion women
     */
    @Query("""
        SELECT COUNT(DISTINCT p.patientID) FROM PATIENT p
        INNER JOIN PREGNANCY_ANC anc ON p.patientID = anc.patientID
        WHERE anc.isAborted = 1
        AND anc.abortionDate IS NOT NULL
        AND p.genderID = 2
        AND p.age BETWEEN 15 AND 49
    """)
    fun getAllAbortionWomenCount(): Flow<Int>

    /**
     * Get all patients registered for pregnancy (eligible for PMSMA)
     * Returns patients with active pregnancy registration
     */
    @Transaction
    @Query("""
        SELECT DISTINCT p.* FROM PATIENT p
        INNER JOIN PREGNANCY_REGISTER pwr ON p.patientID = pwr.patientID
        WHERE pwr.active = 1
        AND p.genderID = 2
        AND p.age BETWEEN 15 AND 49
        ORDER BY pwr.createdDate DESC
    """)
    fun getAllRegisteredPmsmaWomenList(): Flow<List<PatientWithPwrForPmsmaCache>>

    /**
     * Get count of PMSMA eligible women
     */
    @Query("""
        SELECT COUNT(DISTINCT p.patientID) FROM PATIENT p
        INNER JOIN PREGNANCY_REGISTER pwr ON p.patientID = pwr.patientID
        WHERE pwr.active = 1
        AND p.genderID = 2
        AND p.age BETWEEN 15 AND 49
    """)
    fun getAllRegisteredPmsmaWomenCount(): Flow<Int>
}