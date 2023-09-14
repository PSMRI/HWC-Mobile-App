package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Item_Master_List")
@JsonClass(generateAdapter = true)
data class ItemMasterList(
    @PrimaryKey
    val id:Int,
    @ColumnInfo(name = "itemID") val itemID:Int,
    @ColumnInfo(name = "itemName") val itemName:String,
    @ColumnInfo(name = "DropDownForMed") var dropdownForMed:String?,
    @ColumnInfo(name = "strength") val strength:String?,
    @ColumnInfo(name = "unitOfMeasurement") val unitOfMeasurement:String?,
    @ColumnInfo(name = "quantityInHand") val quantityInHand:Int?,
    @ColumnInfo(name = "itemFormID") val itemFormID:Int,
    @ColumnInfo(name = "routeID") val routeID:Int,
    @ColumnInfo(name = "facilityID") val facilityID:Int,
    @ColumnInfo(name = "isEDL") val isEDL:Boolean
    )
