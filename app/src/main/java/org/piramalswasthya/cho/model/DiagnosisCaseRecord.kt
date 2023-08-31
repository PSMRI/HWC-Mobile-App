package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Diagnosis_Cases_Recorde")
@JsonClass(generateAdapter = true)
data class DiagnosisCaseRecord(
    @PrimaryKey
    var diagnosisCaseRecordId:String,
    @ColumnInfo(name = "diagnosis") val diagnosis: String,
)
