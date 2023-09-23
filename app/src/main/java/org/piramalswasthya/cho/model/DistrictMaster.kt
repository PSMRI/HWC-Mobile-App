package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass


@Entity(
    tableName = "DISTRICT_MASTER",
    foreignKeys = [
        ForeignKey(
            entity = StateMaster::class,
            parentColumns = ["stateID"],
            childColumns = ["stateID"],
            onDelete = ForeignKey.NO_ACTION
        )
    ]
)
@JsonClass(generateAdapter = true)
data class DistrictMaster (
    @PrimaryKey val districtID: Int,
    @ColumnInfo(name = "stateID") val stateID: Int,
    @ColumnInfo(name = "govtLGDStateID") val govtLGDStateID: Int?,
    @ColumnInfo(name = "govtLGDDistrictID") val govtLGDDistrictID: Int?,
    @ColumnInfo(name = "district_name") val districtName: String
)
