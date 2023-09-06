package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "RELIGION_MASTER")
@JsonClass(generateAdapter = true)
data class ReligionMaster (
    @PrimaryKey val religionID: Int,
    @ColumnInfo(name = "religion_type") val religionType: String
)