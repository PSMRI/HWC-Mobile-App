package org.piramalswasthya.cho.repositories

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.BenFlowDao
import org.piramalswasthya.cho.database.room.dao.PatientVisitInfoSyncDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.network.AmritApiService
import javax.inject.Inject

class PatientVisitInfoSyncRepo  @Inject constructor(
    private val patientVisitInfoSyncDao: PatientVisitInfoSyncDao,
) {

    suspend fun insertPatientVisitInfoSync(patientVisitInfoSync: PatientVisitInfoSync){
        patientVisitInfoSyncDao.insertPatientVisitInfoSync(patientVisitInfoSync)
    }

    suspend fun updateCreateBenflowFlag(patientID: String,){
        patientVisitInfoSyncDao.updateCreateBenflowFlag(patientID = patientID)
    }

    suspend fun getPatientVisitInfoSync(patientID: String): PatientVisitInfoSync?{
        return patientVisitInfoSyncDao.getPatientVisitInfoSync(patientID)
    }

    suspend fun getPatientNurseDataUnsynced() : List<PatientVisitInfoSync>{
        return patientVisitInfoSyncDao.getPatientNurseDataUnsynced()
    }

    suspend fun updateDoctorDataSubmitted(patientID: String) {
        return patientVisitInfoSyncDao.updateDoctorDataSubmitted(patientID)
    }

    suspend fun getPatientDoctorDataUnsynced() : List<PatientVisitInfoSync>{
        return patientVisitInfoSyncDao.getPatientDoctorDataUnsynced()
    }

    suspend fun updatePatientVisitInfoBenIdAndBenRegId(beneficiaryID: Long, beneficiaryRegID: Long, patientID: String){
        patientVisitInfoSyncDao.updatePatientVisitInfoBenIdAndBenRegId(beneficiaryID, beneficiaryRegID, patientID)
    }

    suspend fun updatePatientNurseDataSyncSuccess(patientID: String){
        patientVisitInfoSyncDao.updatePatientNurseDataSyncSuccess(patientID = patientID)
    }

    suspend fun updatePatientNurseDataSyncFailed(patientID: String){
        patientVisitInfoSyncDao.updatePatientNurseDataSyncFailed(patientID = patientID)
    }

    suspend fun updatePatientNurseDataSyncSyncing(patientID: String){
        patientVisitInfoSyncDao.updatePatientNurseDataSyncSyncing(patientID = patientID)
    }

    suspend fun updatePatientDoctorDataSyncSuccess(patientID: String){
        patientVisitInfoSyncDao.updatePatientDoctorDataSyncSuccess(patientID = patientID)
    }

    suspend fun updatePatientDoctorDataSyncFailed(patientID: String){
        patientVisitInfoSyncDao.updatePatientDoctorDataSyncFailed(patientID = patientID)
    }

    suspend fun updatePatientDoctorDataSyncSyncing(patientID: String){
        patientVisitInfoSyncDao.updatePatientDoctorDataSyncSyncing(patientID = patientID)
    }

    suspend fun hasUnSyncedNurseData(patientID: String) : Boolean {
        val syncState = patientVisitInfoSyncDao.getNurseDataSyncStatus(patientID);
        return (syncState != null && syncState == SyncState.UNSYNCED);
    }

    suspend fun getLastVisitNo(patientID: String) : Int {
        return patientVisitInfoSyncDao.getLastVisitNo(patientID) ?: 0
    }

}