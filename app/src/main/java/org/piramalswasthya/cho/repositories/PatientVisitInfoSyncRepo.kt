package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.PatientVisitInfoSyncDao
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

    suspend fun updateDoctorDataSubmitted(patientID: String) {
        return patientVisitInfoSyncDao.updateDoctorDataSubmitted(patientID)
    }

    suspend fun getPatientDoctorDataUnsynced() : List<PatientVisitInfoSyncWithPatient>{
        return patientVisitInfoSyncDao.getPatientDoctorDataUnsynced()
    }

    suspend fun getPatientLabDataUnsynced() : List<PatientVisitInfoSyncWithPatient>{
        return patientVisitInfoSyncDao.getPatientLabDataUnsynced()
    }
    suspend fun getPatientDoctorDataUnsyncedWithTest() : List<PatientVisitInfoSyncWithPatient>{
        return patientVisitInfoSyncDao.getPatientDoctorDataUnsyncedWithTest()
    }

    suspend fun getPatientDoctorDataUnsyncedWithoutTest() : List<PatientVisitInfoSyncWithPatient>{
        return patientVisitInfoSyncDao.getPatientDoctorDataUnsyncedWithoutTest()
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

    suspend fun updatePatientDoctorDataSyncSuccess(patientID: String){
        patientVisitInfoSyncDao.updatePatientDoctorDataSyncSuccess(patientID = patientID)
    }

    suspend fun updatePatientDoctorDataSyncFailed(patientID: String){
        patientVisitInfoSyncDao.updatePatientDoctorDataSyncFailed(patientID = patientID)
    }

    suspend fun updatePatientDoctorDataSyncSyncing(patientID: String){
        patientVisitInfoSyncDao.updatePatientDoctorDataSyncSyncing(patientID = patientID)
    }

    suspend fun updateLabDataSyncState(patientID: String, syncState: SyncState){
        patientVisitInfoSyncDao.updateLabDataSyncState(patientID = patientID, syncState = syncState)
    }

    suspend fun hasUnSyncedNurseData(patientID: String) : Boolean {
        val syncState = patientVisitInfoSyncDao.getNurseDataSyncStatus(patientID);
        return (syncState != null && syncState == SyncState.UNSYNCED);
    }

    suspend fun getLastVisitInfoSync(patientID: String) : PatientVisitInfoSync? {
        return patientVisitInfoSyncDao.getLastVisitInfoSync(patientID)
    }

}