package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Entity(tableName = "Prescription_Template_DB")
@JsonClass(generateAdapter = true)
data class PrescriptionTemplateDB(
    @Json(ignore = true)
    @PrimaryKey
    @ColumnInfo(name = "id") var id :String = "",
    @ColumnInfo(name = "tempID") val tempID :Int,
    @ColumnInfo(name = "template_name") val templateName: String,
    @ColumnInfo(name = "user_id") val userID: Int,
    @ColumnInfo(name = "drugName") val drugName: String,
    @ColumnInfo(name = "drugId") val drugId: Int,
    @ColumnInfo(name = "frequency") val frequency: String?,
    @ColumnInfo(name = "duration") val duration: String?,
    @ColumnInfo(name = "unit") val unit: String,
    @ColumnInfo(name = "instructions") val instructions: String?,
    @Json(ignore = true)
    @ColumnInfo(name = "deleteStatus") var deleteStatus: Int? = 0,
)
