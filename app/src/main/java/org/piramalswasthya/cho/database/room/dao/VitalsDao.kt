package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.cho.model.PatientVitalsModel

@Dao
interface VitalsDao {
    @Query("SELECT * FROM PATIENT_VITALS WHERE vitalsId = :Id")
    fun getPatientVitalsById(Id : String): LiveData<PatientVitalsModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPatientVitals(patientVitals: PatientVitalsModel)
}