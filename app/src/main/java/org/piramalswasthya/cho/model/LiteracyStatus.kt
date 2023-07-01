package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "LITERACY_STATUS")
@JsonClass(generateAdapter = true)
data class LiteracyStatus (
    @PrimaryKey val literacystatusID: Int,
    @ColumnInfo(name = "literacy_status") val literacystatus: String
        )