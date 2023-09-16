package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Counselling_Provided")
@JsonClass(generateAdapter = true)
data class CounsellingProvided(
    @PrimaryKey
    val id:Int,
    @ColumnInfo(name = "name") val name:String,
    @ColumnInfo(name = "visitCategoryID") val visitCategoryID:String
)
