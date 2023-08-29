package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Past_Surgery_History")
@JsonClass(generateAdapter = true)
data class PastSurgeryHistory(
    @PrimaryKey
    var pastSurgeryHistoryId:String,
    @ColumnInfo(name = "pastSurgeryHistory") val pastSurgeryHistory: String,
    @ColumnInfo(name = "duration") val duration: String?,
    @ColumnInfo(name = "durationUnit") val durationUnit: String
)
