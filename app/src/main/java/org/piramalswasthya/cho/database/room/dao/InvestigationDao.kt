package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import org.piramalswasthya.cho.model.InvestigationCaseRecord
import org.piramalswasthya.cho.model.PrescriptionCaseRecord

@Dao
interface InvestigationDao {

    @Insert
    suspend fun insertInvestigation(investigationCaseRecord: InvestigationCaseRecord)

    @Transaction
    @Query("delete from Investigation_Case_Record where patientID =:patientID")
    suspend fun deleteInvestigationCaseRecordByPatientId(patientID: String): Int
}