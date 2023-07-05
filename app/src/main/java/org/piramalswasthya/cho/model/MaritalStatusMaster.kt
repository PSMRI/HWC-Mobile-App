package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "MARITAL_STATUS_MASTER")
@JsonClass(generateAdapter = true)
data class MaritalStatusMaster (
    @PrimaryKey val maritalStatusID: Int,
    @ColumnInfo(name = "status") val status: String
        )