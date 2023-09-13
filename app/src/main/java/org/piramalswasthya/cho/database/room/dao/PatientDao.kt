package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.model.DistrictMaster
import org.piramalswasthya.cho.model.GenderMaster
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientDisplay

@Dao
interface PatientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient)

    @Transaction
    @Query("DELETE FROM PATIENT WHERE patientID = :patientID")
    suspend fun deletePatient(patientID : String)

    @Transaction
    @Query("SELECT * FROM PATIENT pat LEFT JOIN GENDER_MASTER gen WHERE gen.genderID = pat.genderID")
    suspend fun getPatientList() : List<PatientDisplay>

    @Transaction
    @Query("SELECT * FROM PATIENT pat LEFT JOIN GENDER_MASTER gen ON gen.genderID = pat.genderID WHERE pat.syncState =:unsynced ")
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

}