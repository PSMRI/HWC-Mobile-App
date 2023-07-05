package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass


@Entity(tableName = "OTHER_GOV_ID_ENTITY_MASTER")
@JsonClass(generateAdapter = true)
data class OtherGovIdEntityMaster (
    @PrimaryKey val govtIdentityTypeID: Int,
    @ColumnInfo(name = "identity_type") val identityType: String
)