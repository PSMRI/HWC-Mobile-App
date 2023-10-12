package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(
    tableName = "VILLAGE_MASTER",
    foreignKeys = [
        ForeignKey(
            entity = BlockMaster::class,
            parentColumns = ["blockID"],
            childColumns = ["blockID"],
            onDelete = ForeignKey.NO_ACTION
        )
    ]
)
@JsonClass(generateAdapter = true)
data class VillageMaster (
    @PrimaryKey val districtBranchID: Int,
    @ColumnInfo(name = "blockID") val blockID: Int,
    @ColumnInfo(name = "lgd_subDistrictID") val govtLGDSubDistrictID: Int?,
    @ColumnInfo(name = "lgd_villageID") val govtLGDVillageID: Int?,
    @ColumnInfo(name = "village_name") val villageName: String?
)
