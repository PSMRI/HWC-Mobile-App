package org.piramalswasthya.cho.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass


@Entity(tableName = "Health_Center")
@JsonClass(generateAdapter = true)
data class HigherHealthCenter (
    @PrimaryKey
    val institutionID: Int,
    val institutionName: String
)