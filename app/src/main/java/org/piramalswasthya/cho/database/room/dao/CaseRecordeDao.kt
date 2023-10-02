package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import org.piramalswasthya.cho.model.DiagnosisCaseRecord
import org.piramalswasthya.cho.model.InvestigationCaseRecord
import org.piramalswasthya.cho.model.PrescriptionCaseRecord
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.repositories.CaseRecordeRepo

@Dao
interface CaseRecordeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiagnosisCaseRecord(diagnosisCaseRecord: DiagnosisCaseRecord)
    @Query("SELECT * FROM Diagnosis_Cases_Recorde WHERE beneficiaryRegID = :beneficiaryRegID AND patientID = :patientID")
    suspend fun getDiagnosisCaseRecordeByBenRegIdAndPatientID(beneficiaryRegID: Long, patientID: String) : List<DiagnosisCaseRecord>?

    @Query("SELECT * FROM Diagnosis_Cases_Recorde WHERE beneficiaryRegID = :beneficiaryRegID AND patientID = :patientID")
    suspend fun getPrescriptionCaseRecordeByBenRegIdAndPatientID(beneficiaryRegID: Long, patientID: String) : List<PrescriptionCaseRecord>?

    @Query("SELECT * FROM Diagnosis_Cases_Recorde WHERE beneficiaryRegID = :beneficiaryRegID AND patientID = :patientID")
    suspend fun getInvestigationCaseRecordeByBenRegIdAndPatientID(beneficiaryRegID: Long, patientID: String) : InvestigationCaseRecord?
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

    @Transaction
    @Query("UPDATE Prescription_Cases_Recorde SET beneficiaryID = :beneficiaryID, beneficiaryRegID = :beneficiaryRegID WHERE patientID = :patientID")
    suspend fun updateBenIdBenRegIdPrescription(beneficiaryID: Long, beneficiaryRegID: Long, patientID: String): Int

    @Transaction
    @Query("UPDATE Investigation_Case_Record SET beneficiaryID = :beneficiaryID, beneficiaryRegID = :beneficiaryRegID WHERE patientID = :patientID")
    suspend fun updateBenIdBenRegIdInvestigation(beneficiaryID: Long, beneficiaryRegID: Long, patientID: String): Int

    @Transaction
    @Query("UPDATE Diagnosis_Cases_Recorde SET beneficiaryID = :beneficiaryID, beneficiaryRegID = :beneficiaryRegID WHERE patientID = :patientID")
    suspend fun updateBenIdBenRegIdDiagnosis(beneficiaryID: Long, beneficiaryRegID: Long, patientID: String): Int
    @Transaction
    @Query("delete from Diagnosis_Cases_Recorde where patientID =:patientID")
    suspend fun deleteDiagnosisByPatientId(patientID: String): Int

    @Insert
    suspend fun insertAll(diagnosisCaseRecord: List<DiagnosisCaseRecord>)

}