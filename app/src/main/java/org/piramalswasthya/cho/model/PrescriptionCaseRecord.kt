package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Prescription_Cases_Recorde")
@JsonClass(generateAdapter = true)
data class PrescriptionCaseRecord(
    @PrimaryKey
    var prescriptionCaseRecordId:String,
    @ColumnInfo(name = "form") val form: String,
    @ColumnInfo(name = "frequency") val frequency: String,
    @ColumnInfo(name = "duration") val duration: String,
    @ColumnInfo(name = "instruction") val instruciton: String?,
    @ColumnInfo(name = "unit") val unit: String,
    @ColumnInfo(name = "CounsellingTypes") val counsellingTypes:  String?
)
