package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.cho.model.DiagnosisCaseRecord
import org.piramalswasthya.cho.model.InvestigationCaseRecord
import org.piramalswasthya.cho.model.PrescriptionCaseRecord

@Dao
interface CaseRecordeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiagnosisCaseRecord(diagnosisCaseRecord: DiagnosisCaseRecord)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvestigationCaseRecord(investigationCaseRecord: InvestigationCaseRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescriptionCaseRecord(prescriptionCaseRecord: PrescriptionCaseRecord)

    @Query("SELECT * FROM Diagnosis_Cases_Recorde WHERE diagnosisCaseRecordId = :diagnosisId")
    fun getDiagnosisCasesRecordById(diagnosisId: String): LiveData<DiagnosisCaseRecord>
    @Query("SELECT * FROM Investigation_Case_Record WHERE investigationCaseRecordId = :investigationId")
    fun getInvestigationCasesRecordId(investigationId: String): LiveData<InvestigationCaseRecord>

    @Query("SELECT * FROM Prescription_Cases_Recorde WHERE prescriptionCaseRecordId = :prescriptionId")
    fun getPrescriptionCasesRecordId(prescriptionId: String): LiveData<PrescriptionCaseRecord>
}