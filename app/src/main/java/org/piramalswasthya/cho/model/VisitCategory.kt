package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass


@Entity(tableName = "VISIT_CATEGORY")
@JsonClass(generateAdapter = true)

data class VisitCategory (
    @PrimaryKey
    val visitCategoryID: Int,
    @ColumnInfo(name = "visit_category") val visitCategory: String
)