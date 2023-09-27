package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.database.room.SyncState

@Dao
interface PatientVisitInfoSyncDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatientVisitInfoSync(patientVisitInfoSync: PatientVisitInfoSync)

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET createNewBenFlow = :createNewBenFlow WHERE patientID = :patientID")
    suspend fun updateCreateBenflowFlag(patientID: String, createNewBenFlow: Boolean? = false)

    @Query("SELECT * FROM PATIENT_VISIT_INFO_SYNC WHERE patientID = :patientID")
    suspend fun getPatientVisitInfoSync(patientID: String): PatientVisitInfoSync?

    @Query("SELECT * FROM PATIENT_VISIT_INFO_SYNC WHERE createNewBenFlow = :createNewBenFlow AND benVisitNo > 1")
    suspend fun getUnsyncedRevisitRecords(createNewBenFlow: Boolean? = true): List<PatientVisitInfoSync>

    @Query("SELECT * FROM PATIENT_VISIT_INFO_SYNC WHERE nurseDataSynced = :unSynced")
    suspend fun getPatientNurseDataUnsynced(unSynced: SyncState? = SyncState.UNSYNCED) : List<PatientVisitInfoSync>

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET nurseFlag = 9, doctorFlag = 1 WHERE patientID = :patientID")
    suspend fun updateDoctorDataSubmitted(patientID: String)

    @Query("SELECT * FROM PATIENT_VISIT_INFO_SYNC WHERE doctorDataSynced = :unSynced AND nurseDataSynced = :synced AND nurseFlag = 9 AND doctorFlag = 1")
    suspend fun getPatientDoctorDataUnsynced(unSynced: SyncState? = SyncState.UNSYNCED, synced: SyncState? = SyncState.SYNCED,) : List<PatientVisitInfoSync>

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET beneficiaryID = :beneficiaryID, beneficiaryRegID = :beneficiaryRegID WHERE patientID = :patientID")
    suspend fun updatePatientVisitInfoBenIdAndBenRegId(beneficiaryID: Long, beneficiaryRegID: Long, patientID: String)

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET nurseDataSynced = :synced, nurseFlag = 9, doctorFlag = 1 WHERE patientID = :patientID")
    suspend fun updatePatientNurseDataSyncSuccess(synced: SyncState? = SyncState.SYNCED, patientID: String)

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET nurseDataSynced = :syncFailed WHERE patientID = :patientID")
    suspend fun updatePatientNurseDataSyncFailed(syncFailed: SyncState? = SyncState.UNSYNCED, patientID: String)

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET nurseDataSynced = :syncing WHERE patientID = :patientID")
    suspend fun updatePatientNurseDataSyncSyncing(syncing: SyncState? = SyncState.SYNCING, patientID: String)

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET doctorDataSynced = :synced, nurseFlag = 9, doctorFlag = 9 WHERE patientID = :patientID")
    suspend fun updatePatientDoctorDataSyncSuccess(synced: SyncState? = SyncState.SYNCED, patientID: String)

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET doctorDataSynced = :syncFailed WHERE patientID = :patientID")
    suspend fun updatePatientDoctorDataSyncFailed(syncFailed: SyncState? = SyncState.UNSYNCED, patientID: String)

    @Transaction
    @Query("UPDATE PATIENT_VISIT_INFO_SYNC SET doctorDataSynced = :syncing WHERE patientID = :patientID")
    suspend fun updatePatientDoctorDataSyncSyncing(syncing: SyncState? = SyncState.SYNCING, patientID: String)

    @Query("SELECT nurseDataSynced FROM PATIENT_VISIT_INFO_SYNC WHERE patientID = :patientID")
    suspend fun getNurseDataSyncStatus(patientID: String) : SyncState?

    @Query("SELECT benVisitNo FROM PATIENT_VISIT_INFO_SYNC WHERE patientID = :patientID")
    suspend fun getLastVisitNo(patientID: String) : Int?

}