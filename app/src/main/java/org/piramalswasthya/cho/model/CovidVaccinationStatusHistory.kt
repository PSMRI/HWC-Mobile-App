package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Covid_Vaccination_Status_history")
@JsonClass(generateAdapter = true)
data class CovidVaccinationStatusHistory(
    @PrimaryKey
    var covidVaccinationStatusHistoryId:String,
    @ColumnInfo(name = "vaccinationStatus") val vaccinationStatus: String,
    @ColumnInfo(name = "vaccineType") val vaccineType: String,
    @ColumnInfo(name = "doseTaken") val doseTaken: String
)
