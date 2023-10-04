package org.piramalswasthya.cho.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Drug_Form_Master")
@JsonClass(generateAdapter = true)
data class DrugFormMaster(
    @PrimaryKey
    @ColumnInfo(name = "itemFormID", typeAffinity = ColumnInfo.Companion.INTEGER, defaultValue = "0")
    val itemFormID:Int? = 0,
    @ColumnInfo(name = "itemFormName") val itemFormName:String
)
