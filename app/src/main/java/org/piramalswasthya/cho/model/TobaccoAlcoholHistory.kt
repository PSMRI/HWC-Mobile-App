package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Tobacco_Alcohol_history")
@JsonClass(generateAdapter = true)
data class TobaccoAlcoholHistory(
    @PrimaryKey
    var tobaccoAndAlcoholId:String,
    @ColumnInfo(name = "tobacco") val tobacco: String,
    @ColumnInfo(name = "alcohol") val alcohol: String,
)
