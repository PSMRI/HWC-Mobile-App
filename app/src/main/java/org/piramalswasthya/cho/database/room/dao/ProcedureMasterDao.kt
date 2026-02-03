package org.piramalswasthya.cho.database.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.piramalswasthya.cho.model.ComponentDetailsMaster
import org.piramalswasthya.cho.model.ComponentOptionsMaster;
import org.piramalswasthya.cho.model.ProcedureMaster;

@Dao
public interface ProcedureMasterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(procudereMaster: ProcedureMaster): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(componentOptionsMaster:ComponentOptionsMaster): Long


    @Query("select * from procedure_master where procedure_id = :procedureID limit 1")
    suspend fun getMasterProcedureById(procedureID: Long): ProcedureMaster?

    @Query("select * from procedure_master order by procedure_id")
    suspend fun getAllProcedures(): List<ProcedureMaster>

    @Query("select * from component_details_master where procedure_id = :procedureID")
    suspend fun getComponentDetails(procedureID: Long): List<ComponentDetailsMaster>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(componentDetails: ComponentDetailsMaster): Long

    @Query("select * from component_options_master where component_details_id = :componentId")
    suspend fun getComponentOptions(componentId: Long): List<ComponentOptionsMaster>?

}
