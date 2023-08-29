package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Associate_Ailments_history")
@JsonClass(generateAdapter = true)
data class AssociateAilmentsHistory(
    @PrimaryKey
    var associateAilmentsId:String,
    @ColumnInfo(name = "associateAilments") val associateAilment: String,
    @ColumnInfo(name = "associateAilmentsOther") val associateAilmentOther: String?,
    @ColumnInfo(name = "duration") val duration: String?,
    @ColumnInfo(name = "durationUnit") val durationUnit: String,
    @ColumnInfo(name = "familyMembers") val familyMembers: String,
    @ColumnInfo(name = "inactiveVisit") val inactiveVisit: Boolean
)
