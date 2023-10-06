package org.piramalswasthya.cho.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Outreach_Dropdown_List")
@JsonClass(generateAdapter = true)
data class OutreachDropdownList(
    @PrimaryKey
    val id:Int,
    val outreachType:String,
    val outreachDesc:String?
)