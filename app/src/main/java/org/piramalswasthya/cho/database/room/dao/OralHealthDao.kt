package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.piramalswasthya.cho.model.OralHealth

@Dao
interface OralHealthDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(oralHealth: OralHealth)

    @Update
    suspend fun update(oralHealth: OralHealth)

    @Query("SELECT * FROM ORAL_HEALTH WHERE oral_health_id = :id")
    suspend fun getById(id: Long): OralHealth?

    @Query(
        "SELECT * FROM ORAL_HEALTH " +
            "WHERE patient_id = :patientID " +
            "ORDER BY oral_health_id DESC LIMIT 1"
    )
    suspend fun getByPatientId(patientID: String): OralHealth?

    @Query(
        "SELECT * FROM ORAL_HEALTH " +
            "WHERE patient_id = :patientID " +
            "AND ben_visit_no = :benVisitNo " +
            "ORDER BY oral_health_id DESC LIMIT 1"
    )
    suspend fun getByPatientIdAndVisitNo(
        patientID: String,
        benVisitNo: Int
    ): OralHealth?

    @Query("SELECT * FROM ORAL_HEALTH WHERE syncState = 0")
    suspend fun getUnsyncedAssessments(): List<OralHealth>
}

