package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Drug_Form_Master")
@JsonClass(generateAdapter = true)
data class DrugFormMaster(
    @PrimaryKey
    val itemFormID:Int,
    @ColumnInfo(name = "itemFormName") val itemFormName:String
)
