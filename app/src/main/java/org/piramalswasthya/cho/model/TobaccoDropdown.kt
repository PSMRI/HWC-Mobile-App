package org.piramalswasthya.cho.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Tobacco_Dropdown")
@JsonClass(generateAdapter = true)
data class TobaccoDropdown(
    @PrimaryKey
    val personalHabitTypeID:Int,
    val habitType:String,
    val habitValue:String
)
