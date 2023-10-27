package org.piramalswasthya.cho.database.room.dao

import androidx.room.*
import org.piramalswasthya.cho.model.EligibleCoupleTrackingCache

@Dao
interface EcrDao {

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun upsert(vararg ecrCache: EligibleCoupleRegCache)
//
//    @Query("SELECT * FROM ELIGIBLE_COUPLE_REG WHERE processed in ('N', 'U')")
//    suspend fun getAllUnprocessedECR(): List<EligibleCoupleRegCache>
//
//    @Query("SELECT * FROM ELIGIBLE_COUPLE_TRACKING WHERE processed in ('N','U')")
//    suspend fun getAllUnprocessedECT(): List<EligibleCoupleTrackingCache>
//
//    @Query("select count(*) from ELIGIBLE_COUPLE_REG")
//    suspend fun ecrCount(): Int
//
//    @Query("SELECT * FROM ELIGIBLE_COUPLE_REG WHERE benId =:benId limit 1")
//    suspend fun getSavedECR(benId: Long): EligibleCoupleRegCache?
//
//    @Update
//    suspend fun update(it: EligibleCoupleRegCache)
//
//    @Update
//    suspend fun updateEligibleCoupleTracking(it: EligibleCoupleTrackingCache)

    @Query("select * from eligible_couple_tracking where patientID = :patientID and createdDate =:createdDate limit 1")
//    @Query("select * from eligible_couple_tracking where benId = :benId and CAST((strftime('%s','now') - visitDate/1000)/60/60/24 AS INTEGER) < 30 order by visitDate limit 1")
    fun getEct(patientID: String, createdDate : Long): EligibleCoupleTrackingCache?

    @Query("select * from eligible_couple_tracking where patientID = :patientID order by visitDate desc limit 1")
    suspend fun getLatestEct(patientID: String) : EligibleCoupleTrackingCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg eligibleCoupleTrackingCache: EligibleCoupleTrackingCache)

//    @Query("select count(*)>0 from eligible_couple_tracking where createdDate=:createdDate")
//    suspend fun ectWithsameCreateDateExists(createdDate: Long): Boolean


}