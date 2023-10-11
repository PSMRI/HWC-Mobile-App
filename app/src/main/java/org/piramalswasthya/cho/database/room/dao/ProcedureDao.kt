package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import org.piramalswasthya.cho.model.ComponentDataDownsync
import org.piramalswasthya.cho.model.ComponentDetails
import org.piramalswasthya.cho.model.ComponentOption
import org.piramalswasthya.cho.model.Procedure
import org.piramalswasthya.cho.model.ProcedureDataDownsync
import org.piramalswasthya.cho.model.ProcedureDataWithComponent

@Dao
interface ProcedureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(procedure: Procedure): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(componentDetails: ComponentDetails): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(componentOption: ComponentOption): Long

    @Transaction
    @Query("delete from procedure where patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun deleteProcedureByPatientIDAndBenVisitNo(patientID: String, benVisitNo: Int): Int

    @Query("select * from procedure where patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun getProceduresByPatientIdAndBenVisitNo(patientID: String, benVisitNo: Int): List<Procedure>?

    @Query("select * from component_details where procedure_id = :procedureId")
    suspend fun getComponentDetails(procedureId: Long): List<ComponentDetails>?

    @Query("select * from component_option where component_details_id = :componentId")
    fun getComponentOptions(componentId: Long): List<ComponentOption>?

    @Query("update component_details set test_result_value = :testResultValue and remarks = :remarks where id = :id")
    fun addComponentResult(id: Long, testResultValue: String?, remarks: String?)

    @Query("select * from procedure where patientID = :patientID and benVisitNo = :benVisitNo and procedure_id = :procedureID limit 1")
    fun getProcedure(patientID: String, benVisitNo: Int, procedureID: Long): Procedure

    @Query("select * from component_details where procedure_id = :procedureId and test_component_id = :testComponentID")
    fun getComponentDetails(procedureId: Long, testComponentID: Long): ComponentDetails

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(procedureDataDownsync: ProcedureDataDownsync): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(componentDataDownsync: ComponentDataDownsync): Long

    @Query("delete from PROCEDURE_DATA_DOWNSYNC where patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun deleteProcedureDownsyncByPatientIdAndVisitNo(patientID: String, benVisitNo: Int)

    @Query("select * from PROCEDURE_DATA_DOWNSYNC where patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun getProceduresWithComponent(patientID: String, benVisitNo: Int): List<ProcedureDataWithComponent>

}