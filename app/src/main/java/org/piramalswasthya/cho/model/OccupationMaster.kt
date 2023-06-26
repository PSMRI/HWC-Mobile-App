package org.piramalswasthya.cho.moddel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "OCCUPATION_MASTER")
@JsonClass(generateAdapter = true)
data class OccupationMaster (
    @PrimaryKey val occupationID: Int,
    @ColumnInfo(name = "occupation_type") val occupationType: String
        )
