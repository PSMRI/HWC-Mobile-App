package org.piramalswasthya.cho.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SUB_VISIT_CAT")
data class SubVisitCategory(
    @PrimaryKey
    val id : Int,
    val name : String
)