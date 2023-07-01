package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.cho.model.DoseType
import org.piramalswasthya.cho.model.VaccineType


@Dao
interface VaccinationTypeAndDoseDao {
    @Query("SELECT * FROM VACCINE_TYPE")
    suspend fun getVaccineType(): List<VaccineType>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccineType(vaccineType: VaccineType)

    @Query("SELECT * FROM DOSE_TYPE")
    suspend fun getDoseType(): List<DoseType>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseType(doseType: DoseType)
}