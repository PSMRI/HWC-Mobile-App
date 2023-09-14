package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Counselling_Types")
@JsonClass(generateAdapter = true)
data class CounsellingTypes(
    @PrimaryKey
    val counsellingTypeID:Int,
    @ColumnInfo(name = "counsellingType") val counsellingType:String
)
