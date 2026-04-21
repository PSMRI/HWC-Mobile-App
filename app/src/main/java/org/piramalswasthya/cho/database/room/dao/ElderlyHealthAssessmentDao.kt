package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.piramalswasthya.cho.model.ElderlyHealthAssessment

@Dao
interface ElderlyHealthAssessmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assessment: ElderlyHealthAssessment)

    @Update
    suspend fun update(assessment: ElderlyHealthAssessment)

    @Query("SELECT * FROM ELDERLY_HEALTH_ASSESSMENT WHERE assessment_id = :id")
    suspend fun getAssessmentById(id: Long): ElderlyHealthAssessment?

    @Query("SELECT * FROM ELDERLY_HEALTH_ASSESSMENT WHERE patient_id = :patientID ORDER BY assessment_id DESC LIMIT 1")
    suspend fun getAssessmentByPatientId(patientID: String): ElderlyHealthAssessment?

    @Query("SELECT * FROM ELDERLY_HEALTH_ASSESSMENT WHERE patient_id = :patientID AND ben_visit_no = :benVisitNo")
    suspend fun getAssessment(patientID: String, benVisitNo: Int): ElderlyHealthAssessment?

    @Query("SELECT * FROM ELDERLY_HEALTH_ASSESSMENT WHERE syncState = 0")
    suspend fun getUnsyncedAssessments(): List<ElderlyHealthAssessment>
}