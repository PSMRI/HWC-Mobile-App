package org.piramalswasthya.cho.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Alcohol_Dropdown")
@JsonClass(generateAdapter = true)
data class AlcoholDropdown(
    @PrimaryKey
    val personalHabitTypeID:Int,
    val habitType:String,
    val habitValue:String
)