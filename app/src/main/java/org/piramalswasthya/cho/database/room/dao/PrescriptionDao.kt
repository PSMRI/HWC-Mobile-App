package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.model.ComponentDetails
import org.piramalswasthya.cho.model.ComponentOption
import org.piramalswasthya.cho.model.PrescribedDrugs
import org.piramalswasthya.cho.model.PrescribedDrugsBatch
import org.piramalswasthya.cho.model.Prescription
import org.piramalswasthya.cho.model.PrescriptionCaseRecord
import org.piramalswasthya.cho.model.Procedure

@Dao
interface PrescriptionDao {

    @Insert
    suspend fun insertAll(prescriptionCaseRecord: List<PrescriptionCaseRecord>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prescription: Prescription): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prescribedDrugsBatch: PrescribedDrugsBatch): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prescribedDrugs: PrescribedDrugs): Long

    @Transaction
    @Query("delete from prescription where patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun deletePrescriptionByPatientIDAndBenVisitNo(patientID: String, benVisitNo: Int): Int

//    @Transaction
//    @Query("delete from prescription where prescriptionID = :prescriptionID and beneficiaryRegID = :beneficiaryRegID")
//    suspend fun deletePrescriptionByIDAndBenRegID(prescriptionID: Long, beneficiaryRegID: Long): Int

    @Transaction
    @Query("delete from Prescription_Cases_Recorde where patientID =:patientID")
    suspend fun deletePrescriptionByPatientId(patientID: String): Int

    @Query("select * from prescription where patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun getPrescriptionsByPatientIdAndBenVisitNo(patientID: String, benVisitNo: Int): List<Prescription>?
    @Query("select * from prescription where patientID = :patientID and benVisitNo = :benVisitNo and prescriptionID = :prescriptionID limit 1")
    fun getPrescription(patientID: String, benVisitNo: Int, prescriptionID: Long): Prescription

    @Query("select * from prescribed_drugs where prescriptionID = :prescriptionID")
    suspend fun getPrescribedDrugs(prescriptionID: Long): List<PrescribedDrugs>?

    @Query("select * from prescribed_drugs_batch where drugID = :drugID")
    fun getPrescribedDrugsBatch(drugID: Long): List<PrescribedDrugsBatch>?

    @Transaction
    @Query("UPDATE prescription SET issueType = :issueType WHERE prescriptionID =:prescriptionID")
    suspend fun updatePrescription(issueType: String?, prescriptionID: Long) : Int
//
//    @Query("select * from component_details where procedure_id = :procedureId and test_component_id = :testComponentID")
//    fun getComponentDetails(procedureId: Long, testComponentID: Long): ComponentDetails
    @Query("delete from Prescription_Cases_Recorde where patientID =:patientID and benVisitNo = :benVisitNo")
    suspend fun deletePrescriptionByPatientIdAndBenVisitNo(patientID: String, benVisitNo: Int): Int

}