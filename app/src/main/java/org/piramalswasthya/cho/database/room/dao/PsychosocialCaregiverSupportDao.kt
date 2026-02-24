package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.piramalswasthya.cho.model.PsychosocialCaregiverSupport

@Dao
interface PsychosocialCaregiverSupportDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(assessment: PsychosocialCaregiverSupport)

    @Update
    suspend fun update(assessment: PsychosocialCaregiverSupport)

    @Query(
        "SELECT * FROM PSYCHOSOCIAL_CAREGIVER_SUPPORT " +
                "WHERE assessment_id = :id"
    )
    suspend fun getAssessmentById(
        id: Long
    ): PsychosocialCaregiverSupport?

    @Query(
        "SELECT * FROM PSYCHOSOCIAL_CAREGIVER_SUPPORT " +
                "WHERE patient_id = :patientID " +
                "ORDER BY assessment_id DESC LIMIT 1"
    )
    suspend fun getAssessmentByPatientId(
        patientID: String
    ): PsychosocialCaregiverSupport?

    @Query(
        "SELECT * FROM PSYCHOSOCIAL_CAREGIVER_SUPPORT " +
                "WHERE patient_id = :patientID " +
                "AND ben_visit_no = :benVisitNo " +
                "ORDER BY assessment_id DESC LIMIT 1"
    )
    suspend fun getAssessmentByPatientIdAndVisitNo(
        patientID: String,
        benVisitNo: Int
    ): PsychosocialCaregiverSupport?
}