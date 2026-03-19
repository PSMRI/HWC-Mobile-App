package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.piramalswasthya.cho.model.NoseDiagnosisAssessment

@Dao
interface NoseDiagnosisAssessmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assessment: NoseDiagnosisAssessment): Long

    @Update
    suspend fun update(assessment: NoseDiagnosisAssessment)

    @Query(
        "SELECT * FROM NOSE_DIAGNOSIS_ASSESSMENT " +
                "WHERE assessment_id = :id"
    )
    suspend fun getAssessmentById(id: Long): NoseDiagnosisAssessment?

    @Query(
        "SELECT * FROM NOSE_DIAGNOSIS_ASSESSMENT " +
                "WHERE patient_id = :patientID " +
                "ORDER BY assessment_id DESC LIMIT 1"
    )
    suspend fun getAssessmentByPatientId(patientID: String): NoseDiagnosisAssessment?

    @Query(
        "SELECT * FROM NOSE_DIAGNOSIS_ASSESSMENT " +
                "WHERE patient_id = :patientID AND ben_visit_no = :benVisitNo " +
                "ORDER BY assessment_id DESC LIMIT 1"
    )
    suspend fun getAssessmentByPatientIdAndVisitNo(
        patientID: String,
        benVisitNo: Int
    ): NoseDiagnosisAssessment?
}
