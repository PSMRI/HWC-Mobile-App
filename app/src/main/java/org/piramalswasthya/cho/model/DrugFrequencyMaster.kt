package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Drug_Frequency_Master_List")
@JsonClass(generateAdapter = true)
data class DrugFrequencyMaster(
    @PrimaryKey
    val drugFrequencyID:Int,
    @ColumnInfo(name = "frequency") val frequency:String,
)
