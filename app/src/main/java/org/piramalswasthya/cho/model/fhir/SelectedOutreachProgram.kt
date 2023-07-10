package org.piramalswasthya.cho.model.fhir

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.piramalswasthya.cho.model.UserCache


@Entity(
    tableName = "SELECTED_OUTREACH_PROGRAM",
    foreignKeys = [
        ForeignKey(
            entity = UserCache::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SelectedOutreachProgram(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "user_id")
    val userId: Int?,

    @ColumnInfo(name = "option")
    val option: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: String
)