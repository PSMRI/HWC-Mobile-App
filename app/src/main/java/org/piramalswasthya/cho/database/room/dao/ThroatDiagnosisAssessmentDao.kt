package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.piramalswasthya.cho.model.ThroatDiagnosisAssessment

@Dao
interface ThroatDiagnosisAssessmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assessment: ThroatDiagnosisAssessment)

    @Update
    suspend fun update(assessment: ThroatDiagnosisAssessment)

    @Query(
        "SELECT * FROM THROAT_DIAGNOSIS_ASSESSMENT " +
                "WHERE assessment_id = :id"
    )
    suspend fun getAssessmentById(id: Long): ThroatDiagnosisAssessment?

    @Query(
        "SELECT * FROM THROAT_DIAGNOSIS_ASSESSMENT " +
                "WHERE patient_id = :patientID " +
                "ORDER BY assessment_id DESC LIMIT 1"
    )
    suspend fun getAssessmentByPatientId(patientID: String): ThroatDiagnosisAssessment?
}