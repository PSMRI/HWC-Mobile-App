package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientVitalsModel

@Dao
interface VitalsDao {
    @Query("SELECT * FROM PATIENT_VITALS WHERE vitalsId = :Id")
    fun getPatientVitalsById(Id : String): LiveData<PatientVitalsModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPatientVitals(patientVitals: PatientVitalsModel)


    @Query("DELETE FROM PATIENT_VITALS WHERE patientID = :patientID")
    fun deleteAllVitalsByPatientIDSharedOffline(patientID: String)

    @Update
    fun updateVitals(vitals: PatientVitalsModel)

//    @Query("SELECT * FROM PATIENT_VITALS WHERE beneficiaryRegID = :beneficiaryRegID")
//    suspend fun getPatientVitalsByBenRegId(beneficiaryRegID: Long): PatientVitalsModel?

    @Query("SELECT * FROM PATIENT_VITALS WHERE patientID = :patientID")
    suspend fun getPatientVitalsByPatientID(patientID: String): PatientVitalsModel

    @Query("SELECT * FROM PATIENT_VITALS WHERE patientID = :patientID ORDER BY benVisitNo DESC LIMIT 1")
    suspend fun getPatientVitalsByPatientIDAndBenVisitNoForFollowUp(patientID: String): PatientVitalsModel?
    @Query("SELECT * FROM PATIENT_VITALS WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun getPatientVitalsByPatientIDAndBenVisitNo(patientID: String, benVisitNo: Int): PatientVitalsModel?

//    @Transaction
//    @Query("UPDATE PATIENT_VITALS SET benFlowId = :benFlowId WHERE beneficiaryRegID = :beneficiaryRegID AND benVisitNo = :benVisitNo")
//    suspend fun updateBenFlowId(benFlowId: Long, beneficiaryRegID: Long, benVisitNo: Int): Int

//    @Transaction
//    @Query("UPDATE PATIENT_VITALS SET beneficiaryID = :beneficiaryID, beneficiaryRegID = :beneficiaryRegID WHERE patientID = :patientID")
//    suspend fun updateBenIdBenRegId(beneficiaryID: Long, beneficiaryRegID: Long, patientID: String): Int

    @Transaction
    @Query("DELETE FROM PATIENT_VITALS WHERE patientID = :patientID")
    suspend fun deletePatientVitalsByPatientId(patientID: String): Int

    @Transaction
    @Query("DELETE FROM PATIENT_VITALS WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun deletePatientVitalsByPatientIdAndBenVisitNo(patientID: String, benVisitNo: Int): Int

}