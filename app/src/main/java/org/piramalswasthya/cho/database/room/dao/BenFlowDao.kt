package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.cho.model.BenFlow
import org.piramalswasthya.cho.model.BlockMaster

@Dao
interface BenFlowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBenFlow(benFlow: BenFlow)
    @Query(
        "SELECT COUNT(*) " +
                "FROM PATIENT pat " +
                "LEFT JOIN PATIENT_VISIT_INFO_SYNC vis ON pat.patientID = vis.patientID " +
                "LEFT JOIN PATIENT_VISIT_INFO_SYNC AS latestVisit ON pat.patientID = latestVisit.patientID AND vis.benVisitNo < latestVisit.benVisitNo " +
                "LEFT JOIN GENDER_MASTER gen ON gen.genderID = pat.genderID " +
                "WHERE ( " +
                "(:genderBucket = 'male' AND LOWER(IFNULL(gen.gender_name, '')) = 'male') " +
                "OR (:genderBucket = 'female' AND LOWER(IFNULL(gen.gender_name, '')) = 'female') " +
                "OR (:genderBucket = 'other' AND TRIM(IFNULL(gen.gender_name, '')) <> '' AND LOWER(IFNULL(gen.gender_name, '')) NOT IN ('male', 'female')) " +
                ") " +
                "AND vis.nurseFlag = 9 " +
                "AND latestVisit.patientID IS NULL " +
                "AND NOT (vis.doctorFlag = 9 AND IFNULL(vis.pharmacist_flag, 0) IN (0, 9)) " +
                "AND vis.patientID IS NOT NULL " +
                "AND COALESCE(vis.visitDate, pat.registrationDate) IS NOT NULL " +
                "AND strftime('%Y-%m-%d', COALESCE(vis.visitDate, pat.registrationDate) / 1000, 'unixepoch', 'localtime') LIKE '%' || :periodParam || '%'"
    )
    suspend fun getDoctorModuleOpdCount(genderBucket: String, periodParam: String) : Int?

    @Query(
        "SELECT COUNT(*) " +
                "FROM PATIENT pat " +
                "LEFT JOIN PATIENT_VISIT_INFO_SYNC vis ON pat.patientID = vis.patientID " +
                "LEFT JOIN PATIENT_VISIT_INFO_SYNC AS latestVisit ON pat.patientID = latestVisit.patientID AND vis.benVisitNo < latestVisit.benVisitNo " +
                "WHERE vis.nurseFlag = 9 " +
                "AND latestVisit.patientID IS NULL " +
                "AND NOT (vis.doctorFlag = 9 AND IFNULL(vis.pharmacist_flag, 0) IN (0, 9)) " +
                "AND vis.patientID IS NOT NULL"
    )
    fun observeDoctorModuleListCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM Visit_DB WHERE (Visit_DB.category LIKE 'ANC') AND createdBy = :createdBy AND Visit_DB.benVisitDate LIKE '%' || :periodParam || '%' ")
    suspend fun getAncCount(periodParam: String, createdBy: String) : Int?

    @Query("SELECT COUNT(*) FROM Visit_DB WHERE (Visit_DB.category LIKE 'PNC') AND createdBy = :createdBy AND Visit_DB.benVisitDate LIKE '%' || :periodParam || '%' ")
    suspend fun getPncCount(periodParam: String, createdBy: String) : Int?

    @Query("SELECT COUNT(*) FROM Visit_DB WHERE (Visit_DB.category LIKE 'Neonatal and Infant Health Care Services') AND createdBy = :createdBy AND Visit_DB.benVisitDate LIKE '%' || :periodParam || '%' ")
    suspend fun getImmunizationCount(periodParam: String, createdBy: String) : Int?

    @Query("SELECT COUNT(*) FROM Visit_DB WHERE (Visit_DB.category LIKE 'FP & Contraceptive Services') AND createdBy = :createdBy AND Visit_DB.benVisitDate LIKE '%' || :periodParam || '%' ")
    suspend fun getEctCount(periodParam: String, createdBy: String): Int?

    @Query("SELECT COUNT(*) FROM Visit_DB INNER JOIN PATIENT ON PATIENT.patientID = Visit_DB.patientID WHERE PATIENT.genderID = :genderID AND (Visit_DB.category LIKE 'General OPD') AND Visit_DB.benVisitDate LIKE '%' || :periodParam || '%' ")
     fun getOpdCountLive(genderID: Int, periodParam: String) : LiveData<Int?>?
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