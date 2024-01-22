package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.piramalswasthya.cho.model.CbacCache


@Dao
interface CbacDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg cbacCache: CbacCache)

    @Update
    suspend fun update(vararg cbacCache: CbacCache)

    @Query("SELECT * FROM CBAC WHERE id = :cbacId LIMIT 1")
    suspend fun getCbacFromBenId(cbacId: Int): CbacCache?

    @Query("SELECT * FROM CBAC WHERE patId = :benId  order by fillDate desc LIMIT 1")
    suspend fun getLastFilledCbacFromBenId(benId: String): CbacCache?


//    @Query("SELECT c.*, b.householdId as hhId, b.gender as benGender FROM CBAC c join beneficiary b on c.benId= b.beneficiaryId WHERE c.processed in ('N','U') ")
//    suspend fun getAllUnprocessedCbac(): List<CbacCachePush>
//
//    @Query("UPDATE CBAC SET syncState = 1 WHERE benId =:benId")
//    suspend fun setCbacSyncing(vararg benId: Long)
//
//
//    @Query("UPDATE CBAC SET processed = 'P', syncState = 2 WHERE benId =:benId")
//    suspend fun cbacSyncedWithServer(vararg benId: Long)
//
//    @Query("UPDATE CBAC SET processed = 'N', syncState = 0 WHERE benId =:benId")
//    suspend fun cbacSyncWithServerFailed(vararg benId: Long)
//
//    @Query("select count(*)>0 from cbac where createdDate between :createdDate-:range and :createdDate+:range")
//    suspend fun sameCreateDateExists(createdDate: Long, range: Long): Boolean

}