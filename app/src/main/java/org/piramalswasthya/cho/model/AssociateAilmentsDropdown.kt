package org.piramalswasthya.cho.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Associate_Ailments_Dropdown")
@JsonClass(generateAdapter = true)
data class AssociateAilmentsDropdown(
    @PrimaryKey
    val assocateAilmentsId:Int,
    val assocateAilments:String,
)