package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "AGE_UNIT")
@JsonClass(generateAdapter = true)
data class AgeUnit (
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "age_name") val name: String
)