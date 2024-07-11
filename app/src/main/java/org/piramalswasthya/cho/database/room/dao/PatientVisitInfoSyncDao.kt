package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PatientVisitInfoSyncWithPatient
import org.piramalswasthya.cho.model.SyncStatusCache
import java.util.Date

@Dao
interface PatientVisitInfoSyncDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatientVisitInfoSync(patientVisitInfoSync: PatientVisitInfoSync)

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET nurseFlag = 9, doctorFlag = 1, visitDate = :visitDate, nurseDataSynced = :synced WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun updateAfterNurseDataDownSync(patientID: String, benVisitNo: Int, visitDate: Date?, synced: SyncState? = SyncState.SYNCED)

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET doctorFlag = :doctorFlag, doctorDataSynced = :synced WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun updateAfterDoctorDataDownSync(doctorFlag : Int, patientID: String, benVisitNo: Int, synced: SyncState? = SyncState.SYNCED)

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET prescriptionID = :prescriptionID WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun updatePrescriptionID(prescriptionID : Int, patientID: String, benVisitNo: Int)

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET nurseFlag = :nurseFlag, doctorFlag = :doctorFlag, labtechFlag = :labtechFlag, doctorDataSynced = :unSynced WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun updateOnlyDoctorDataSubmitted(nurseFlag : Int, doctorFlag : Int, labtechFlag : Int, patientID: String, benVisitNo: Int, unSynced: SyncState? = SyncState.UNSYNCED)

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET pharmacistDataSynced = :unSynced WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun updatePharmacistDataUnsynced(patientID: String, benVisitNo: Int, unSynced: SyncState? = SyncState.UNSYNCED)

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET pharmacistDataSynced = :syncing WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun updatePharmacistDataSyncing(patientID: String, benVisitNo: Int, syncing: SyncState? = SyncState.SYNCING, )

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET pharmacistDataSynced = :synced, pharmacist_flag = 9 WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun updatePharmacistDataSynced(patientID: String, benVisitNo: Int, synced: SyncState? = SyncState.SYNCED, )

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET createNewBenFlow = :createNewBenFlow WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun updateCreateBenflowFlag(patientID: String, benVisitNo: Int, createNewBenFlow: Boolean? = false)

    @Query("SELECT * FROM PATIENT_VISIT_INFO_SYNC WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun getPatientVisitInfoSyncByPatientIdAndBenVisitNo(patientID: String, benVisitNo: Int): PatientVisitInfoSync?

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET benFlowId = :benFlowId, pharmacist_flag= :pharmacistFlag, visitCategory = :visitCategory WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun updateBenFlowIdByPatientIdAndBenVisitNo(benFlowId: Long, pharmacistFlag:Int, patientID: String, benVisitNo: Int, visitCategory: String)

    @Query("SELECT * FROM PATIENT_VISIT_INFO_SYNC WHERE createNewBenFlow = :createNewBenFlow AND benVisitNo > 1 ORDER BY benVisitNo ASC")
    suspend fun getUnsyncedRevisitRecords(createNewBenFlow: Boolean? = true): List<PatientVisitInfoSync>

    @Query("SELECT * FROM PATIENT_VISIT_INFO_SYNC WHERE nurseDataSynced = :unSynced ORDER BY benVisitNo ASC")
    suspend fun getPatientNurseDataUnsynced(unSynced: SyncState? = SyncState.UNSYNCED) : List<PatientVisitInfoSyncWithPatient>

    @Query("SELECT * FROM PATIENT_VISIT_INFO_SYNC WHERE doctorDataSynced = :unSynced AND nurseDataSynced = :synced AND doctorFlag = 2 AND labtechFlag != 1 ORDER BY benVisitNo ASC")
    suspend fun getPatientDoctorDataPendingTestUnsynced(unSynced: SyncState? = SyncState.UNSYNCED, synced: SyncState? = SyncState.SYNCED, ) : List<PatientVisitInfoSyncWithPatient>

    @Query("SELECT * FROM PATIENT_VISIT_INFO_SYNC WHERE doctorDataSynced = :unSynced AND nurseDataSynced = :synced AND doctorFlag = 9 AND labtechFlag != 1 ORDER BY benVisitNo ASC")
    suspend fun getPatientDoctorDataWithoutTestUnsynced(unSynced: SyncState? = SyncState.UNSYNCED, synced: SyncState? = SyncState.SYNCED, ) : List<PatientVisitInfoSyncWithPatient>

    @Query("SELECT * FROM PATIENT_VISIT_INFO_SYNC WHERE doctorDataSynced = :unSynced AND nurseDataSynced = :synced AND labtechFlag = 1 ORDER BY benVisitNo ASC")
    suspend fun getPatientDoctorDataAfterTestUnsynced(unSynced: SyncState? = SyncState.UNSYNCED, synced: SyncState? = SyncState.SYNCED, ) : List<PatientVisitInfoSyncWithPatient>

//    @Transaction
//    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET beneficiaryID = :beneficiaryID, beneficiaryRegID = :beneficiaryRegID WHERE patientID = :patientID")
//    suspend fun updatePatientVisitInfoBenIdAndBenRegId(beneficiaryID: Long, beneficiaryRegID: Long, patientID: String)

    @Query("SELECT * FROM PATIENT_VISIT_INFO_SYNC WHERE labDataSynced = :unSynced")
    suspend fun getPatientLabDataUnsynced(unSynced: SyncState? = SyncState.UNSYNCED) : List<PatientVisitInfoSyncWithPatient>

    @Query("SELECT * FROM PATIENT_VISIT_INFO_SYNC WHERE pharmacistDataSynced = :unSynced")
    suspend fun getPatientPharmacistDataUnsynced(unSynced: SyncState? = SyncState.UNSYNCED) : List<PatientVisitInfoSyncWithPatient>

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET nurseDataSynced = :synced WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun updatePatientNurseDataSyncSuccess(synced: SyncState? = SyncState.SYNCED, patientID: String, benVisitNo: Int)

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET nurseDataSynced = :synced WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun updatePatientNurseDataOfflineSyncSuccess(synced: SyncState? = SyncState.SHARED_OFFLINE, patientID: String, benVisitNo: Int)

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET nurseDataSynced = :syncFailed WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun updatePatientNurseDataSyncFailed(syncFailed: SyncState? = SyncState.UNSYNCED, patientID: String, benVisitNo: Int)

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET nurseDataSynced = :syncing WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun updatePatientNurseDataSyncSyncing(syncing: SyncState? = SyncState.SYNCING, patientID: String, benVisitNo: Int)

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET doctorDataSynced = :synced WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun updatePatientDoctorDataSyncSuccess(synced: SyncState? = SyncState.SYNCED, patientID: String, benVisitNo: Int)

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET doctorDataSynced = :syncFailed WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun updatePatientDoctorDataSyncFailed(syncFailed: SyncState? = SyncState.UNSYNCED, patientID: String, benVisitNo: Int)

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET doctorDataSynced = :syncing WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun updatePatientDoctorDataSyncSyncing(syncing: SyncState? = SyncState.SYNCING, patientID: String, benVisitNo: Int)

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET labDataSynced = :syncState WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun updateLabDataSyncState(syncState: SyncState?, benVisitNo: Int, patientID: String)

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET pharmacistDataSynced = :syncState WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun updatePharmacistDataSyncState(syncState: SyncState?, benVisitNo: Int, patientID: String)

    @Query("SELECT nurseDataSynced FROM PATIENT_VISIT_INFO_SYNC WHERE patientID = :patientID")
    suspend fun getNurseDataSyncStatus(patientID: String) : SyncState?

    @Query("SELECT * FROM PATIENT_VISIT_INFO_SYNC WHERE patientID = :patientID ORDER BY benVisitNo DESC LIMIT 1")
    suspend fun getLastVisitInfoSync(patientID: String) : PatientVisitInfoSync?

    @Query("SELECT * FROM PATIENT_VISIT_INFO_SYNC WHERE patientID = :patientID AND nurseFlag = 9 AND doctorFlag = 1 ORDER BY benVisitNo ASC LIMIT 1")
    suspend fun getSinglePatientDoctorDataNotSubmitted(patientID: String) : PatientVisitInfoSync?

    @Transaction
    @Query("SELECT pat.*, vis.*, gen.gender_name as genderName, vilN.village_name as villageName,age.age_name as ageUnit, mat.status as maritalStatus " +
            "FROM PATIENT_VISIT_INFO_SYNC vis " +
            "LEFT JOIN PATIENT pat ON pat.patientID = vis.patientID " +
            "LEFT JOIN GENDER_MASTER gen ON gen.genderID = pat.genderID " +
            "LEFT JOIN VILLAGE_MASTER vilN ON pat.districtBranchID = vilN.districtBranchID "+
            "LEFT JOIN AGE_UNIT age ON age.id = pat.ageUnitID " +
            "LEFT JOIN MARITAL_STATUS_MASTER mat on mat.maritalStatusID = pat.maritalStatusID " +
            "WHERE vis.nurseFlag = 9 AND vis.patientID = :patientID AND vis.visitCategory = 'General OPD' ORDER BY vis.benVisitNo ASC")
    fun getPatientDisplayListForDoctorByPatient(patientID: String) : Flow<List<PatientDisplayWithVisitInfo>>

    @Transaction
    @Query("SELECT pat.*, vis.*, gen.gender_name as genderName, vilN.village_name as villageName,age.age_name as ageUnit, mat.status as maritalStatus " +
            "FROM PATIENT_VISIT_INFO_SYNC vis " +
            "LEFT JOIN PATIENT pat ON pat.patientID = vis.patientID " +
            "LEFT JOIN GENDER_MASTER gen ON gen.genderID = pat.genderID " +
            "LEFT JOIN VILLAGE_MASTER vilN ON pat.districtBranchID = vilN.districtBranchID "+
            "LEFT JOIN AGE_UNIT age ON age.id = pat.ageUnitID " +
            "LEFT JOIN MARITAL_STATUS_MASTER mat on mat.maritalStatusID = pat.maritalStatusID " +
            "WHERE vis.nurseFlag = 9 AND vis.visitCategory = 'General OPD' ORDER BY pat.registrationDate DESC")
    fun getPatientDisplayListForDoctor(): Flow<List<PatientDisplayWithVisitInfo>>

    @Transaction
    @Query("SELECT pat.*, vis.*, gen.gender_name as genderName,vilN.village_name as villageName, age.age_name as ageUnit, mat.status as maritalStatus " +
            "FROM PATIENT_VISIT_INFO_SYNC vis " +
            "LEFT JOIN PATIENT pat ON pat.patientID = vis.patientID " +
            "LEFT JOIN GENDER_MASTER gen ON gen.genderID = pat.genderID " +
            "LEFT JOIN VILLAGE_MASTER vilN ON pat.districtBranchID = vilN.districtBranchID "+
            "LEFT JOIN AGE_UNIT age ON age.id = pat.ageUnitID " +
            "LEFT JOIN MARITAL_STATUS_MASTER mat on mat.maritalStatusID = pat.maritalStatusID " +
            "WHERE vis.nurseFlag = 9 AND vis.doctorFlag = 2 AND vis.visitCategory = 'General OPD' ORDER BY pat.registrationDate DESC")
    fun getPatientDisplayListForLab(): Flow<List<PatientDisplayWithVisitInfo>>

    @Transaction
    @Query("SELECT pat.*, vis.*, gen.gender_name as genderName,vilN.village_name as villageName, age.age_name as ageUnit, mat.status as maritalStatus " +
            "FROM PATIENT_VISIT_INFO_SYNC vis " +
            "LEFT JOIN PATIENT pat ON pat.patientID = vis.patientID " +
            "LEFT JOIN GENDER_MASTER gen ON gen.genderID = pat.genderID " +
            "LEFT JOIN VILLAGE_MASTER vilN ON pat.districtBranchID = vilN.districtBranchID "+
            "LEFT JOIN AGE_UNIT age ON age.id = pat.ageUnitID " +
            "LEFT JOIN MARITAL_STATUS_MASTER mat on mat.maritalStatusID = pat.maritalStatusID " +
            "WHERE vis.doctorFlag = 9 AND vis.visitCategory = 'General OPD' AND vis.pharmacist_flag = 1")
    fun getPatientDisplayListForPharmacist(): Flow<List<PatientDisplayWithVisitInfo>>

    @Transaction
    @Query("SELECT " +
        "1 as id,'Patient' as name," +
        "COUNT(CASE WHEN syncState = 2 THEN 1 END) AS synced," +
        "COUNT(CASE WHEN syncState = 1 THEN 1 END) AS syncing," +
        "COUNT(CASE WHEN syncState = 0 THEN 1 END) AS notSynced" +
        " FROM PATIENT UNION SELECT " +
        "2 as id,'Nurse' as name," +
        "COUNT(CASE WHEN nurseDataSynced = 2 THEN 1 END) AS synced," +
        "COUNT(CASE WHEN nurseDataSynced = 1 THEN 1 END) AS syncing," +
        "COUNT(CASE WHEN nurseDataSynced = 0 THEN 1 END) AS notSynced" +
        " FROM PATIENT_VISIT_INFO_SYNC WHERE nurseFlag = 9 UNION SELECT " +
        "3 as id,'Doctor' as name," +
        "COUNT(CASE WHEN doctorDataSynced = 2 THEN 1 END) AS synced," +
        "COUNT(CASE WHEN doctorDataSynced = 1 THEN 1 END) AS syncing," +
        "COUNT(CASE WHEN doctorDataSynced = 0 THEN 1 END) AS notSynced" +
        " FROM PATIENT_VISIT_INFO_SYNC WHERE doctorFlag > 1 UNION SELECT " +
        "4 as id,'Lab Technician' as name," +
        "COUNT(CASE WHEN labDataSynced = 2 THEN 1 END) AS synced," +
        "COUNT(CASE WHEN labDataSynced = 1 THEN 1 END) AS syncing," +
        "COUNT(CASE WHEN labDataSynced = 0 THEN 1 END) AS notSynced" +
        " FROM PATIENT_VISIT_INFO_SYNC WHERE doctorFlag = 3 UNION SELECT " +
        "5 as id,'Pharmacist' as name," +
        "COUNT(CASE WHEN pharmacistDataSynced = 2 THEN 1 END) AS synced," +
        "COUNT(CASE WHEN pharmacistDataSynced = 1 THEN 1 END) AS syncing," +
        "COUNT(CASE WHEN pharmacistDataSynced = 0 THEN 1 END) AS notSynced" +
        " FROM PATIENT_VISIT_INFO_SYNC WHERE doctorFlag = 9 AND pharmacist_flag = 9 ORDER BY id")
    fun getSyncStatus(): Flow<List<SyncStatusCache>>

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET referDate = :referDate, referTo = :referTo, referralReason = :referralReason WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun updatePatientReferData(referDate: String?, referTo: String?, referralReason: String?, patientID: String, benVisitNo: Int) : Int

}