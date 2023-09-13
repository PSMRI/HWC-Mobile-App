package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(
    tableName = "BLOCK_MASTER",
    foreignKeys = [
        ForeignKey(
            entity = DistrictMaster::class,
            parentColumns = ["districtID"],
            childColumns = ["districtID"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@JsonClass(generateAdapter = true)
data class BlockMaster (
    @PrimaryKey val blockID: Int,
    @ColumnInfo(name = "districtID") val districtID: Int,
    @ColumnInfo(name = "lgd_districtID") val govtLGDDistrictID: Int,
    @ColumnInfo(name = "lgd_subDistrictID") val govLGDSubDistrictID: Int,
    @ColumnInfo(name = "block_name") val blockName: String
)
