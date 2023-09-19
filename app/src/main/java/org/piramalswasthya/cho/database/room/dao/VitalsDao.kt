package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import org.piramalswasthya.cho.model.PatientVitalsModel

@Dao
interface VitalsDao {
    @Query("SELECT * FROM PATIENT_VITALS WHERE vitalsId = :Id")
    fun getPatientVitalsById(Id : String): LiveData<PatientVitalsModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPatientVitals(patientVitals: PatientVitalsModel)

    @Query("SELECT * FROM PATIENT_VITALS WHERE beneficiaryRegID = :beneficiaryRegID")
    suspend fun getPatientVitalsByBenRegId(beneficiaryRegID: Long): PatientVitalsModel?

    @Transaction
    @Query("UPDATE PATIENT_VITALS SET benFlowId = :benFlowId WHERE beneficiaryRegID = :beneficiaryRegID")
    suspend fun updateBenFlowId(benFlowId: Long, beneficiaryRegID: Long): Int

    @Transaction
    @Query("UPDATE PATIENT_VITALS SET beneficiaryID = :beneficiaryID, beneficiaryRegID = :beneficiaryRegID WHERE patientID = :patientID")
    suspend fun updateBenIdBenRegId(beneficiaryID: Long, beneficiaryRegID: Long, patientID: String): Int

}