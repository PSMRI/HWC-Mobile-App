package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import org.piramalswasthya.cho.model.DiagnosisCaseRecord
import org.piramalswasthya.cho.model.InvestigationCaseRecord
import org.piramalswasthya.cho.model.InvestigationCaseRecordWithHigherHealthCenter
import org.piramalswasthya.cho.model.PrescriptionCaseRecord
import org.piramalswasthya.cho.model.PrescriptionWithItemMasterAndDrugFormMaster
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.repositories.CaseRecordeRepo

@Dao
interface CaseRecordeDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertDiagnosisCaseRecord(diagnosisCaseRecord: DiagnosisCaseRecord)

    @Query("SELECT * FROM Diagnosis_Cases_Recorde WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun getDiagnosisCaseRecordeByPatientIDAndBenVisitNo(patientID: String, benVisitNo: Int) : List<DiagnosisCaseRecord>?

    @Query("SELECT * FROM Prescription_With_ItemMaster_And_DrugFormMaster WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun getPrescriptionCaseRecordeByPatientIDAndBenVisitNo(patientID: String, benVisitNo: Int) : List<PrescriptionWithItemMasterAndDrugFormMaster>?

    @Query("SELECT * FROM Investigation_Case_Record WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun getInvestigationCaseRecordeByPatientIDAndBenVisitNo(patientID: String, benVisitNo: Int) : InvestigationCaseRecordWithHigherHealthCenter?
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertInvestigationCaseRecord(investigationCaseRecord: InvestigationCaseRecord)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertPrescriptionCaseRecord(prescriptionCaseRecord: PrescriptionCaseRecord)

    @Query("SELECT * FROM Diagnosis_Cases_Recorde WHERE diagnosisCaseRecordId = :diagnosisId")
    fun getDiagnosisCasesRecordById(diagnosisId: String): LiveData<DiagnosisCaseRecord>

    @Query("SELECT * FROM Investigation_Case_Record WHERE investigationCaseRecordId = :investigationId")
    fun getInvestigationCasesRecordId(investigationId: String): LiveData<InvestigationCaseRecord>

    @Query("SELECT * FROM Prescription_Cases_Recorde WHERE prescriptionCaseRecordId = :prescriptionId")
    fun getPrescriptionCasesRecordId(prescriptionId: String): LiveData<PrescriptionCaseRecord>

//    @Transaction
//    @Query("UPDATE Prescription_Cases_Recorde SET beneficiaryID = :beneficiaryID, beneficiaryRegID = :beneficiaryRegID WHERE patientID = :patientID")
//    suspend fun updateBenIdBenRegIdPrescription(beneficiaryID: Long, beneficiaryRegID: Long, patientID: String): Int
//
//    @Transaction
//    @Query("UPDATE Investigation_Case_Record SET beneficiaryID = :beneficiaryID, beneficiaryRegID = :beneficiaryRegID WHERE patientID = :patientID")
//    suspend fun updateBenIdBenRegIdInvestigation(beneficiaryID: Long, beneficiaryRegID: Long, patientID: String): Int
//
//    @Transaction
//    @Query("UPDATE Diagnosis_Cases_Recorde SET beneficiaryID = :beneficiaryID, beneficiaryRegID = :beneficiaryRegID WHERE patientID = :patientID")
//    suspend fun updateBenIdBenRegIdDiagnosis(beneficiaryID: Long, beneficiaryRegID: Long, patientID: String): Int
    @Transaction
    @Query("delete from Diagnosis_Cases_Recorde where patientID =:patientID and benVisitNo = :benVisitNo")
    suspend fun deleteDiagnosisByPatientIdAndBenVisitNo(patientID: String, benVisitNo: Int): Int

    @Insert
    suspend fun insertAll(diagnosisCaseRecord: List<DiagnosisCaseRecord>)

    @Query("SELECT * FROM Investigation_Case_Record WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun getPrescriptionCasesRecordByPatientIDAndBenVisitNo(patientID: String, benVisitNo:Int): InvestigationCaseRecord?

    @Query("SELECT * FROM Prescription_Cases_Recorde WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun getPrescriptionByPatientIDAndBenVisitNo(patientID: String, benVisitNo:Int): List<PrescriptionCaseRecord?>

    @Query("SELECT * FROM Diagnosis_Cases_Recorde WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun getDiagnosisByPatientIDAndBenVisitNo(patientID: String, benVisitNo:Int): List<DiagnosisCaseRecord?>
}