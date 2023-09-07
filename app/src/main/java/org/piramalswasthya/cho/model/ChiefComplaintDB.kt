package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Chielf_Complaint_DB")
@JsonClass(generateAdapter = true)
data class ChiefComplaintDB(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "chiefComplaint") val chiefComplaint: String,
    @ColumnInfo(name = "duration") val duration: String,
    @ColumnInfo(name = "durationUnit") val durationUnit: String,
    @ColumnInfo(name = "description") val description: String
)
