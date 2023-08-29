package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import org.piramalswasthya.cho.model.DistrictMaster
import org.piramalswasthya.cho.model.GenderMaster
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientDisplay

@Dao
interface PatientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient)

    @Transaction
    @Query("SELECT * FROM PATIENT pat LEFT JOIN GENDER_MASTER gen WHERE gen.genderID = pat.genderID")
    suspend fun getPatientList() : List<PatientDisplay>

}