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

    @ColumnInfo(name = "user_name")
    val userName: String?,

    @ColumnInfo(name = "login_type")
    val loginType: String?,

    @ColumnInfo(name = "option")
    val option: String?,

    @ColumnInfo(name = "logoutTimestamp")
    val logoutTimestamp: String?,

    @ColumnInfo(name = "loginTimestamp")
    val loginTimestamp: String?,

    @ColumnInfo(name = "latitude")
    val latitude: Double?,

    @ColumnInfo(name = "longitude")
    val longitude: Double?,

    @ColumnInfo(name = "logout_type")
    val logoutType: String? ,

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean? = false
)