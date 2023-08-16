package org.piramalswasthya.cho.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Illness_Dropdown")
@JsonClass(generateAdapter = true)
data class IllnessDropdown (
    @PrimaryKey
    val illnessID: Int,
    val illnessType: String
    )