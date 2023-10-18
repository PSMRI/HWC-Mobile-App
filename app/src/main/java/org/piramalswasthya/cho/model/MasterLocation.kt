package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(
    tableName = "MASTER_LOCATION",
)
@JsonClass(generateAdapter = true)
data class MasterLocation (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "state_id") val stateId: Int?,
    @ColumnInfo(name = "state_name") val stateName: String?,
    @ColumnInfo(name = "district_id") val districtId: Int?,
    @ColumnInfo(name = "district_name") val districtName: String?,
    @ColumnInfo(name = "block_id") val blockId: Int?,
    @ColumnInfo(name = "block_name") val blockName: String?,
    @ColumnInfo(name = "village_id") val villageId: Int?,
    @ColumnInfo(name = "village_name") val villageName: String?,
    @ColumnInfo(name = "latitude") val masterLatitude: Double?,
    @ColumnInfo(name = "longitude") val masterLongitude: Double?,
)

