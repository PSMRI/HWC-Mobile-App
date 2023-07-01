package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass


@Entity(tableName = "STATE_MASTER")
@JsonClass(generateAdapter = true)
data class StateMaster (
    @PrimaryKey val stateID: Int,
    @ColumnInfo(name = "state_name") val stateName: String
)