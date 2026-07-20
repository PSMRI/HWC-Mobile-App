package org.piramalswasthya.cho.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "SNOMED_DIAGNOSIS_MASTER")
data class SnomedDiagnosis(
    @PrimaryKey
    @SerializedName("conceptID") val conceptID: String,
    @SerializedName("term") val term: String
)
