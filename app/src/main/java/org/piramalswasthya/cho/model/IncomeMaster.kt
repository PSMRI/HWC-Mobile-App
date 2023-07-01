package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "INCOME_MASTER")
@JsonClass(generateAdapter = true)
data class IncomeMaster (
    @PrimaryKey val incomeStatusID: Int,
    @ColumnInfo(name = "income_status") val incomeStatus: String
        )