package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass


@Entity(tableName = "QUALIFICATION_MASTER")
@JsonClass(generateAdapter = true)
data class QualificationMaster (
    @PrimaryKey val educationID: Int,
    @ColumnInfo(name = "education_type") val educationType: String
)