package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Procedures_Master_Data")
@JsonClass(generateAdapter = true)
data class  ProceduresMasterData(
    @PrimaryKey
    var procedureID:Int,
    @ColumnInfo(name = "procedureName") val procedureName: String,
    @ColumnInfo(name = "procedureDesc") val procedureDesc: String,
    @ColumnInfo(name = "procedureType") val procedureType: String,
    @ColumnInfo(name = "gender") val gender: String?,
    @ColumnInfo(name = "providerServiceMapID") val providerServiceMapID: Int
)
