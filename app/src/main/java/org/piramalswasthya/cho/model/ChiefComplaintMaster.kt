package org.piramalswasthya.cho.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "CHIEF_COMPLAINT_MASTER")
@JsonClass(generateAdapter = true)
data class ChiefComplaintMaster(
    @PrimaryKey
    val chiefComplaintID: Int,
    val chiefComplaint: String
)