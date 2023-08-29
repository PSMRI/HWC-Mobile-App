package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Past_Illness_History")
@JsonClass(generateAdapter = true)
data class PastIllnessHistory(
    @PrimaryKey
    var pastIllnessHistoryId:String,
    @ColumnInfo(name = "pastIllnessHistory") val pastIllnessHistory: String,
    @ColumnInfo(name = "duration") val duration: String?,
    @ColumnInfo(name = "durationUnit") val durationUnit: String
)
