package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass



@Entity(tableName = "VACCINE_TYPE")
@JsonClass(generateAdapter = true)
data class VaccineType (
    @PrimaryKey
    val covidVaccineTypeID: Int,
    @ColumnInfo(name = "vaccine_type") val vaccineType: String,
    @ColumnInfo(name = "deleted") val deleted: Boolean,
    @ColumnInfo(name = "processed") val processed: String,
    @ColumnInfo(name = "created_by") val createdBy: String,
    @ColumnInfo(name = "created_date") val createdDate: String,
    @ColumnInfo(name = "last_mod_date") val lastModDate: String,
)