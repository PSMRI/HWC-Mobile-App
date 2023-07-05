package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass


@Entity(tableName = "GOV_ID_ENTITY_MASTER")
@JsonClass(generateAdapter = true)
data class GovIdEntityMaster (
    @PrimaryKey val govtIdentityTypeID: Int,
    @ColumnInfo(name = "identity_type") val identityType: String
)