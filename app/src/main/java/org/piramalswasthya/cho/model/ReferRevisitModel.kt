package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass


@Entity(tableName = "REFER_REVISIT")
@JsonClass(generateAdapter = true)
data class ReferRevisitModel (
    @PrimaryKey
    val referId: String,
    @ColumnInfo(name = "higher_center") val higherCenter: String?,
    @ColumnInfo(name = "referral_reason") val referralReason: String?,
    @ColumnInfo(name = "revisit_date") val revisitDate: String?,
    @ColumnInfo(name = "other_services") val otherServices: String?
)