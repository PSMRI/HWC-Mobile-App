package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import org.piramalswasthya.cho.model.BenFlow
import org.piramalswasthya.cho.model.BlockMaster

@Dao
interface BenFlowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBenFlow(benFlow: BenFlow)

    @Query("SELECT * FROM BENFLOW WHERE beneficiaryRegID = :beneficiaryRegID LIMIT 1")
    suspend fun getBenFlowByBenRegId(beneficiaryRegID: Long) : BenFlow?

    @Query("SELECT * FROM BENFLOW WHERE beneficiaryRegID = :beneficiaryRegID AND benVisitNo = :benVisitNo")
    suspend fun getBenFlowByBenRegIdAndBenVisitNo(beneficiaryRegID: Long, benVisitNo: Int) : BenFlow?

    @Transaction
    @Query("UPDATE BENFLOW SET nurseFlag = 9, doctorFlag = 1, visitCode = :visitCode, benVisitID= :benVisitID WHERE benFlowID = :benFlowID")
    suspend fun updateNurseCompleted(visitCode: Long, benVisitID: Long, benFlowID: Long)

    @Transaction
    @Query("UPDATE BENFLOW SET nurseFlag = 9, doctorFlag = 9 WHERE benFlowID = :benFlowID")
    suspend fun updateDoctorCompleted(benFlowID: Long)

}