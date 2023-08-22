package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass


@Entity(tableName = "Medication_history")
@JsonClass(generateAdapter = true)
data class MedicationHistory(
    @PrimaryKey
    var medicationHistoryId:String,
    @ColumnInfo(name = "currentMedication") val currentMedication: String,
    @ColumnInfo(name = "duration") val duration: String,
    @ColumnInfo(name = "durationUnit") val durationUnit: String
)
