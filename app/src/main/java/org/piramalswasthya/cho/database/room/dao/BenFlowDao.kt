package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import org.piramalswasthya.cho.model.BenFlow

@Dao
interface BenFlowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBenFlow(benFlow: BenFlow)
    @Query("SELECT COUNT(DISTINCT Visit_DB.patientID) FROM Visit_DB INNER JOIN PATIENT ON PATIENT.patientID = Visit_DB.patientID WHERE PATIENT.genderID = :genderID AND (Visit_DB.category LIKE 'General OPD') AND createdBy = :createdBy AND Visit_DB.benVisitDate LIKE '%' || :periodParam || '%' ")
    suspend fun getOpdCount(genderID: Int, periodParam: String, createdBy: String) : Int?

    @Query("SELECT COUNT(DISTINCT Visit_DB.patientID) FROM Visit_DB WHERE (Visit_DB.category LIKE 'ANC') AND createdBy = :createdBy AND Visit_DB.benVisitDate LIKE '%' || :periodParam || '%' ")
    suspend fun getAncCount(periodParam: String, createdBy: String) : Int?

    @Query("SELECT COUNT(DISTINCT Visit_DB.patientID) FROM Visit_DB WHERE (Visit_DB.category LIKE 'PNC') AND createdBy = :createdBy AND Visit_DB.benVisitDate LIKE '%' || :periodParam || '%' ")
    suspend fun getPncCount(periodParam: String, createdBy: String) : Int?

    @Query("SELECT COUNT(DISTINCT Visit_DB.patientID) FROM Visit_DB WHERE (Visit_DB.category LIKE 'Neonatal and Infant Health Care Services') AND createdBy = :createdBy AND Visit_DB.benVisitDate LIKE '%' || :periodParam || '%' ")
    suspend fun getImmunizationCount(periodParam: String, createdBy: String) : Int?

    @Query("SELECT COUNT(DISTINCT Visit_DB.patientID) FROM Visit_DB WHERE (Visit_DB.category LIKE 'FP & Contraceptive Services') AND createdBy = :createdBy AND Visit_DB.benVisitDate LIKE '%' || :periodParam || '%' ")
    suspend fun getEctCount(periodParam: String, createdBy: String): Int?

    @Query("SELECT * FROM BENFLOW WHERE beneficiaryRegID = :beneficiaryRegID LIMIT 1")
    suspend fun getBenFlowByBenRegId(beneficiaryRegID: Long) : BenFlow?

    @Query("SELECT * FROM BENFLOW WHERE benFlowID = :benFlowID LIMIT 1")
    suspend fun getBenFlowByBenFlowID(benFlowID: Long) : BenFlow

    @Query("SELECT * FROM BENFLOW WHERE beneficiaryID = :beneficiaryRegID")
    suspend fun getBenFlowByBeneficiaryID(beneficiaryRegID: Long) : List<BenFlow>


    @Query("SELECT * FROM BenFlow")
    suspend fun getAllBenFlows(): List<BenFlow>

    @Query("SELECT * FROM BENFLOW WHERE beneficiaryRegID = :beneficiaryRegID AND benVisitNo = :benVisitNo")
    suspend fun getBenFlowByBenRegIdAndBenVisitNo(beneficiaryRegID: Long, benVisitNo: Int) : BenFlow?

    @Transaction
    @Query("UPDATE BENFLOW SET nurseFlag = 9, doctorFlag = 1, visitCode = :visitCode, benVisitID= :benVisitID WHERE benFlowID = :benFlowID")
    suspend fun updateNurseCompleted(visitCode: Long, benVisitID: Long, benFlowID: Long)

    @Transaction
    @Query("UPDATE BENFLOW SET nurseFlag = 9, doctorFlag = :doctorFlag WHERE benFlowID = :benFlowID")
    suspend fun updateDoctorFlag(benFlowID: Long, doctorFlag: Int)

    @Transaction
    @Query("UPDATE BENFLOW SET nurseFlag = 9, doctorFlag = 9 WHERE benFlowID = :benFlowID")
    suspend fun updateDoctorCompletedWithoutTest(benFlowID: Long)

}