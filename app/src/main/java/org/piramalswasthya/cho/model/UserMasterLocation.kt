package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(
    tableName = "USER_MASTER_LOCATION",
    foreignKeys = [
        ForeignKey(
            entity = UserCache::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.NO_ACTION
        )
    ]
)
@JsonClass(generateAdapter = true)
data class UserMasterLocation (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "user_id")
    val userId: Int?,
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