package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Visit_DB")
@JsonClass(generateAdapter = true)
data class VisitDB(
    @PrimaryKey
    val visitId:String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "reasonForVisit") val reasonForVisit: String,
    @ColumnInfo(name = "subCategory") val subCategory: String,
)
