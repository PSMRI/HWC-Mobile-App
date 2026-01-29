package org.piramalswasthya.cho.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "STATUS_OF_WOMAN_MASTER")
data class StatusOfWomanMaster(
    @PrimaryKey
    val statusID: Int,
    val statusName: String,
    val statusCode: String
)
