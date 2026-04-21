package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.piramalswasthya.cho.model.PainAndSymptomAssessment

@Dao
interface PainAndSymptomAssessmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assessment: PainAndSymptomAssessment)

    @Update
    suspend fun update(assessment: PainAndSymptomAssessment)

    @Query(
        "SELECT * FROM PAIN_SYMPTOM_ASSESSMENT " +
                "WHERE assessment_id = :id"
    )
    suspend fun getAssessmentById(id: Long): PainAndSymptomAssessment?

    @Query(
        "SELECT * FROM PAIN_SYMPTOM_ASSESSMENT " +
                "WHERE patient_id = :patientID " +
                "ORDER BY assessment_id DESC LIMIT 1"
    )
    suspend fun getAssessmentByPatientId(
        patientID: String
    ): PainAndSymptomAssessment?

    @Query(
        "SELECT * FROM PAIN_SYMPTOM_ASSESSMENT " +
                "WHERE patient_id = :patientID " +
                "AND ben_visit_no = :benVisitNo " +
                "ORDER BY assessment_id DESC LIMIT 1"
    )
    suspend fun getAssessmentByPatientIdAndVisitNo(
        patientID: String,
        benVisitNo: Int
    ): PainAndSymptomAssessment?

    @Query("SELECT * FROM PAIN_SYMPTOM_ASSESSMENT WHERE syncState = 0")
    suspend fun getUnsyncedAssessments(): List<PainAndSymptomAssessment>
}