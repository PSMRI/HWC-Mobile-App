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

    @Query("SELECT * FROM PATIENT_VITALS WHERE beneficiaryRegID = :beneficiaryRegID")
    suspend fun getPatientVitalsByBenRegId(beneficiaryRegID: Long): PatientVitalsModel?

    @Query("SELECT * FROM PATIENT_VITALS WHERE beneficiaryRegID = :beneficiaryRegID AND benVisitNo = :benVisitNo")
    suspend fun getPatientVitalsByBenRegIdAndBenVisitNo(beneficiaryRegID: Long, benVisitNo: Int): PatientVitalsModel?

    @Transaction
    @Query("UPDATE PATIENT_VITALS SET benFlowId = :benFlowId WHERE beneficiaryRegID = :beneficiaryRegID")
    suspend fun updateBenFlowId(benFlowId: Long, beneficiaryRegID: Long): Int

    @Transaction
    @Query("UPDATE PATIENT_VITALS SET beneficiaryID = :beneficiaryID, beneficiaryRegID = :beneficiaryRegID WHERE patientID = :patientID")
    suspend fun updateBenIdBenRegId(beneficiaryID: Long, beneficiaryRegID: Long, patientID: String): Int

    @Transaction
    @Query("DELETE FROM PATIENT_VITALS WHERE patientID = :patientID")
    suspend fun deletePatientVitalsByPatientId(patientID: String): Int

}