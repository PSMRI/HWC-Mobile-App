package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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
    @Query("delete from prescription where beneficiaryRegID = :benRegID")
    suspend fun deletePrescriptionByBenRegID(benRegID: Long): Int

    @Transaction
    @Query("delete from prescription where prescriptionID = :prescriptionID and beneficiaryRegID = :beneficiaryRegID")
    suspend fun deletePrescriptionByIDAndBenRegID(prescriptionID: Long, beneficiaryRegID: Long): Int

    @Transaction
    @Query("delete from Prescription_Cases_Recorde where patientID =:patientID")
    suspend fun deletePrescriptionByPatientId(patientID: String): Int

    @Query("select * from prescription where beneficiaryRegID = :benRegID")
    suspend fun getPrescriptions(benRegID: Long): List<Prescription>?
    @Query("select * from prescription where beneficiaryRegID = :benRegId and prescriptionID = :prescriptionID limit 1")
    fun getPrescription(benRegId: Long, prescriptionID: Long): Prescription

    @Query("select * from prescribed_drugs where prescriptionID = :prescriptionID")
    suspend fun getPrescribedDrugs(prescriptionID: Long): List<PrescribedDrugs>?

    @Query("select * from prescribed_drugs_batch where drugID = :drugID")
    fun getPrescribedDrugsBatch(drugID: Long): List<PrescribedDrugsBatch>?

//    @Query("update component_details set test_result_value = :testResultValue and remarks = :remarks where id = :id")
//    fun addComponentResult(id: Long, testResultValue: String?, remarks: String?)
//
//    @Query("select * from component_details where procedure_id = :procedureId and test_component_id = :testComponentID")
//    fun getComponentDetails(procedureId: Long, testComponentID: Long): ComponentDetails
}