package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.model.DistrictMaster
import org.piramalswasthya.cho.model.GenderMaster
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo

@Dao
interface PatientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient)

    @Query("SELECT * FROM PATIENT WHERE patientID = :patientID")
    suspend fun getPatient(patientID : String) : Patient

    @Query("SELECT * FROM PATIENT WHERE patientID = :patientID")
    suspend fun getPatientDisplay(patientID : String) : PatientDisplay

//    @Transaction
//    @Query("UPDATE PATIENT SET nurseFlag = 9, doctorFlag = 1 WHERE patientID = :patientID")
//    suspend fun updateNurseSubmitted(patientID : String)

//    @Transaction
//    @Query("UPDATE PATIENT SET nurseFlag = :nurseFlag, doctorFlag = :doctorFlag WHERE beneficiaryRegID = :beneficiaryRegID")
//    suspend fun updateFlagsByBenRegId(nurseFlag: Int, doctorFlag: Int, beneficiaryRegID: Long)

//    @Transaction
//    @Query("UPDATE PATIENT SET nurseFlag = 9, doctorFlag = 9 WHERE patientID = :patientID")
//    suspend fun updateDoctorSubmitted(patientID : String)

    @Query("SELECT * FROM PATIENT WHERE beneficiaryRegID = :beneficiaryRegID")
    suspend fun getPatientByBenRegId(beneficiaryRegID: Long) : Patient?

    @Transaction
    @Query("DELETE FROM PATIENT WHERE patientID = :patientID")
    suspend fun deletePatient(patientID : String)
    @Update
    suspend fun updatePatient(patient: Patient)
    @Transaction
    @Query("SELECT * FROM PATIENT")
    suspend fun getPatientList() : List<PatientDisplay>

    @Transaction
    @Query("SELECT * FROM PATIENT")
    fun getPatientListFlow() : Flow<List<PatientDisplay>>

    @Transaction
    @Query("SELECT * FROM PATIENT pat")
    fun getPatientListFlowForNurse(): Flow<List<PatientDisplay>>

    @Transaction
    @Query("SELECT * FROM PATIENT pat")
    fun getPatientListFlowForDoctor(): Flow<List<PatientDisplay>>

    @Transaction
    @Query("select * from patient p inner join benflow bf on p.beneficiaryRegID = bf.beneficiaryRegID inner join patient_visit_info_sync pvs on bf.benFlowID = pvs.benFlowID where bf.doctorFlag = 2 and pvs.labDataSynced != 2")
    fun getPatientListFlowForLab(): Flow<List<PatientDisplay>>

    @Transaction
    @Query("SELECT pat.*, gen.gender_name as genderName, age.age_name as ageUnit, mat.status as maritalStatus, " +
            "null as nurseDataSynced, null as doctorDataSynced, null as createNewBenFlow, null as benVisitNo, " +
            "null as benFlowID, null as nurseFlag, null as doctorFlag, null as labtechFlag, null as pharmacist_flag " +
            "FROM PATIENT pat " +
            "LEFT JOIN GENDER_MASTER gen ON gen.genderID = pat.genderID " +
            "LEFT JOIN AGE_UNIT age ON age.id = pat.ageUnitID " +
            "LEFT JOIN MARITAL_STATUS_MASTER mat on mat.maritalStatusID = pat.maritalStatusID")
    fun getPatientDisplayListForNurse(): Flow<List<PatientDisplayWithVisitInfo>>

    @Transaction
    @Query("SELECT * FROM PATIENT WHERE syncState =:unsynced ")
    suspend fun getPatientListUnsynced(unsynced: SyncState = SyncState.UNSYNCED) : List<PatientDisplay>

    @Query("SELECT * FROM PATIENT WHERE patientID =:patientID")
    suspend fun getPatientById(patientID: String) : PatientDisplay
    @Transaction
    @Query("UPDATE PATIENT SET syncState = :syncing WHERE patientID =:patientID")
    suspend fun updatePatientSyncing(syncing: SyncState = SyncState.SYNCING, patientID: String) : Int

    @Transaction
    @Query("UPDATE PATIENT SET syncState = :synced, beneficiaryID = :beneficiaryID, beneficiaryRegID = :beneficiaryRegID WHERE patientID =:patientID")
    suspend fun updatePatientSynced(synced: SyncState = SyncState.SYNCED, beneficiaryID: Long, beneficiaryRegID: Long, patientID: String) : Int

    @Transaction
    @Query("UPDATE PATIENT SET syncState = :synced WHERE patientID =:patientID")
    suspend fun updatePatientSyncFailed(synced: SyncState = SyncState.UNSYNCED, patientID: String) : Int
    @Query("SELECT * FROM PATIENT WHERE beneficiaryId =:benId LIMIT 1")
    suspend fun getBen(benId: Long): Patient?

//    @Transaction
//    @Query("UPDATE PATIENT SET nurseFlag = 9, doctorFlag = 1 WHERE beneficiaryRegID = :beneficiaryRegID")
//    suspend fun updateNurseCompleted(beneficiaryRegID: Long)
//
//    @Transaction
//    @Query("UPDATE PATIENT SET nurseFlag = 9, doctorFlag = 9 WHERE beneficiaryRegID = :beneficiaryRegID")
//    suspend fun updateDoctorCompleted(beneficiaryRegID: Long)

    @Query("select count(*) from patient where beneficiaryID = :benId")
    suspend fun getCountByBenId(benId:Long): Int
}