package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.cho.model.SnomedDiagnosis

@Dao
interface SnomedDiagnosisDao {
    @Query("SELECT * FROM SNOMED_DIAGNOSIS_MASTER ORDER BY term")
    fun getAll(): LiveData<List<SnomedDiagnosis>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<SnomedDiagnosis>)

    @Query("DELETE FROM SNOMED_DIAGNOSIS_MASTER")
    suspend fun deleteAll()
}
