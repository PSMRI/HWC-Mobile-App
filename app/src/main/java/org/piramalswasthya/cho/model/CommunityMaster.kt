package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass


@Entity(tableName = "COMMUNITY_MASTER")
@JsonClass(generateAdapter = true)
data class CommunityMaster (
    @PrimaryKey val communityID: Int,
    @ColumnInfo(name = "community_type") val communityType: String
)