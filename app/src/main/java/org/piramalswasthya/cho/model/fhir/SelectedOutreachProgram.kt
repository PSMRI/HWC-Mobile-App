package org.piramalswasthya.cho.model.fhir

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SELECTED_OUTREACH_PROGRAM")
data class SelectedOutreachProgram(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "option")
    val option: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: String
)