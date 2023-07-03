package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "VISIT_REASON")
@JsonClass(generateAdapter = true)

data class VisitReason (
    @PrimaryKey
    val visitReasonID: Int,
    @ColumnInfo(name = "visit_reason") val visitReason: String
)