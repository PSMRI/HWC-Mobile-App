package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "GENDER_MASTER")
@JsonClass(generateAdapter = true)
data class GenderMaster (
    @PrimaryKey val genderID: Int,
    @ColumnInfo(name = "gender_name") val genderName: String
)
