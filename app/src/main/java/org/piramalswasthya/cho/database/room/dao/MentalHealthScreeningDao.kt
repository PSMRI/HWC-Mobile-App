package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.piramalswasthya.cho.model.MentalHealthScreeningCache

@Dao
interface MentalHealthScreeningDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(screening: MentalHealthScreeningCache): Long

    @Update
    suspend fun update(screening: MentalHealthScreeningCache)

    @Query(
        "SELECT * FROM MENTAL_HEALTH_SCREENING " +
                "WHERE screening_id = :id"
    )
    suspend fun getScreeningById(id: Long): MentalHealthScreeningCache?

    @Query(
        "SELECT * FROM MENTAL_HEALTH_SCREENING " +
                "WHERE patient_id = :patientID " +
                "ORDER BY screening_id DESC LIMIT 1"
    )
    suspend fun getScreeningByPatientId(patientID: String): MentalHealthScreeningCache?

    @Query(
        "SELECT * FROM MENTAL_HEALTH_SCREENING " +
                "WHERE patient_id = :patientID " +
                "AND ben_visit_no = :benVisitNo " +
                "ORDER BY screening_id DESC LIMIT 1"
    )
    suspend fun getScreeningByPatientIdAndVisitNo(
        patientID: String,
        benVisitNo: Int
    ): MentalHealthScreeningCache?
}