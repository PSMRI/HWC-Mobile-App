package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import org.piramalswasthya.cho.model.PrescriptionCaseRecord

@Dao
interface PrescriptionDao {

    @Insert
    suspend fun insertAll(prescriptionCaseRecord: List<PrescriptionCaseRecord>)

    @Transaction
    @Query("delete from Prescription_Cases_Recorde where patientID =:patientID")
    suspend fun deletePrescriptionByPatientId(patientID: String): Int
}