package org.piramalswasthya.cho.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Surgery_Dropdown")
@JsonClass(generateAdapter = true)
data class SurgeryDropdown(
    @PrimaryKey
    val surgeryID: Int,
    val surgeryType: String
)
