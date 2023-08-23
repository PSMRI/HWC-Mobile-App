package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.cho.model.DoseType
import org.piramalswasthya.cho.model.IllnessDropdown
import org.piramalswasthya.cho.model.VaccineType


@Dao
interface VaccinationTypeAndDoseDao {
    @Query("SELECT * FROM VACCINE_TYPE")
     fun getVaccineType(): LiveData<List<VaccineType>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccineType(vaccineType: VaccineType)

    @Query("SELECT * FROM DOSE_TYPE")
     fun getDoseType(): LiveData<List<DoseType>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseType(doseType: DoseType)

    @Query("select * from VACCINE_TYPE")
    suspend fun getVaccineTypeMasterMap():List<VaccineType>

    @Query("select * from DOSE_TYPE")
    suspend fun getDoseTypeMasterMap():List<DoseType>
}