package org.piramalswasthya.cho.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "FP_DATA")
data class FingerPrint(
    @PrimaryKey
    val id: Int,
    val userName: String?,
    val fpVal: String?,
    val fingerType: String?
)