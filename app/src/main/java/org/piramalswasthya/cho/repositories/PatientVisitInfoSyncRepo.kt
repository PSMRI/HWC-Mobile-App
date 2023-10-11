package org.piramalswasthya.cho.repositories

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.PatientVisitInfoSyncDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.model.PatientVisitInfoSyncWithPatient
import org.piramalswasthya.cho.network.AmritApiService
import javax.inject.Inject

class PatientVisitInfoSyncRepo  @Inject constructor(
    private val patientVisitInfoSyncDao: PatientVisitInfoSyncDao,
) {

    suspend fun insertPatientVisitInfoSync(patientVisitInfoSync: PatientVisitInfoSync){
        patientVisitInfoSyncDao.insertPatientVisitInfoSync(patientVisitInfoSync)
    }

    suspend fun updateCreateBenflowFlag(patientID: String, benVisitNo: Int){
        patientVisitInfoSyncDao.updateCreateBenflowFlag(patientID = patientID, benVisitNo = benVisitNo)
    }

    suspend fun getPatientVisitInfoSyncByPatientIdAndBenVisitNo(patientID: String, benVisitNo: Int) : PatientVisitInfoSync?{
        return patientVisitInfoSyncDao.getPatientVisitInfoSyncByPatientIdAndBenVisitNo(patientID = patientID, benVisitNo = benVisitNo)
    }

    suspend fun getPatientNurseDataUnsynced() : List<PatientVisitInfoSyncWithPatient>{
        return patientVisitInfoSyncDao.getPatientNurseDataUnsynced()
    }

    suspend fun getPatientDoctorDataPendingTestUnsynced() : List<PatientVisitInfoSyncWithPatient>{
        return patientVisitInfoSyncDao.getPatientDoctorDataPendingTestUnsynced()
    }

    suspend fun getPatientDoctorDataWithoutTestUnsynced() : List<PatientVisitInfoSyncWithPatient>{
        return patientVisitInfoSyncDao.getPatientDoctorDataWithoutTestUnsynced()
    }

    suspend fun getPatientDoctorDataAfterTestUnsynced() : List<PatientVisitInfoSyncWithPatient>{
        return patientVisitInfoSyncDao.getPatientDoctorDataAfterTestUnsynced()
    }

    suspend fun getPatientLabDataUnsynced() : List<PatientVisitInfoSyncWithPatient>{
        return patientVisitInfoSyncDao.getPatientLabDataUnsynced()
    }

    suspend fun getSinglePatientDoctorDataNotSubmitted(patientID: String) : PatientVisitInfoSync?{
        return patientVisitInfoSyncDao.getSinglePatientDoctorDataNotSubmitted(patientID = patientID)
    }

//    suspend fun updatePatientVisitInfoBenIdAndBenRegId(beneficiaryID: Long, beneficiaryRegID: Long, patientID: String){
//        patientVisitInfoSyncDao.updatePatientVisitInfoBenIdAndBenRegId(beneficiaryID, beneficiaryRegID, patientID)
//    }

    suspend fun updatePatientNurseDataSyncSuccess(patientID: String, benVisitNo: Int){
        patientVisitInfoSyncDao.updatePatientNurseDataSyncSuccess(patientID = patientID, benVisitNo = benVisitNo)
    }

    suspend fun updatePatientNurseDataSyncFailed(patientID: String, benVisitNo: Int){
        patientVisitInfoSyncDao.updatePatientNurseDataSyncFailed(patientID = patientID, benVisitNo = benVisitNo)
    }

    suspend fun updatePatientNurseDataSyncSyncing(patientID: String, benVisitNo: Int){
        patientVisitInfoSyncDao.updatePatientNurseDataSyncSyncing(patientID = patientID, benVisitNo = benVisitNo)
    }

    suspend fun updatePatientDoctorDataSyncSuccess(patientID: String, benVisitNo: Int){
        patientVisitInfoSyncDao.updatePatientDoctorDataSyncSuccess(patientID = patientID, benVisitNo = benVisitNo)
    }

    suspend fun updatePatientDoctorDataSyncFailed(patientID: String, benVisitNo: Int){
        patientVisitInfoSyncDao.updatePatientDoctorDataSyncFailed(patientID = patientID, benVisitNo = benVisitNo)
    }

    suspend fun updatePatientDoctorDataSyncSyncing(patientID: String, benVisitNo: Int){
        patientVisitInfoSyncDao.updatePatientDoctorDataSyncSyncing(patientID = patientID, benVisitNo = benVisitNo)
    }

    suspend fun updateLabDataSyncState(patientID: String, benVisitNo: Int, syncState: SyncState){
        patientVisitInfoSyncDao.updateLabDataSyncState(patientID = patientID, syncState = syncState, benVisitNo = benVisitNo )
    }

    suspend fun hasUnSyncedNurseData(patientID: String) : Boolean {
        val syncState = patientVisitInfoSyncDao.getNurseDataSyncStatus(patientID);
        return (syncState != null && syncState == SyncState.UNSYNCED);
    }

    suspend fun getLastVisitInfoSync(patientID: String) : PatientVisitInfoSync? {
        return patientVisitInfoSyncDao.getLastVisitInfoSync(patientID)
    }

    fun getPatientDisplayListForDoctor() : Flow<List<PatientDisplayWithVisitInfo>> {
        return patientVisitInfoSyncDao.getPatientDisplayListForDoctor()
    }

    fun getPatientDisplayListForLab() : Flow<List<PatientDisplayWithVisitInfo>> {
        return patientVisitInfoSyncDao.getPatientDisplayListForLab()
    }

}