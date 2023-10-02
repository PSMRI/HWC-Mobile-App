package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.squareup.moshi.JsonClass

@Entity(
    tableName = "Item_Master_List",
    foreignKeys = [
        ForeignKey(
            entity = DrugFormMaster::class,
            parentColumns = ["itemFormID"],
            childColumns = ["itemFormID"],
            onDelete = ForeignKey.NO_ACTION
        ),
    ]
)
@JsonClass(generateAdapter = true)
data class ItemMasterList(
    @PrimaryKey
    val itemID:Int,
    @ColumnInfo(name = "id") val id:Int,
    @ColumnInfo(name = "itemName") val itemName:String,
    @ColumnInfo(name = "DropDownForMed") var dropdownForMed:String?,
    @ColumnInfo(name = "strength") val strength:String?,
    @ColumnInfo(name = "unitOfMeasurement") val unitOfMeasurement:String?,
    @ColumnInfo(name = "quantityInHand") val quantityInHand:Int?,
    @ColumnInfo(name = "itemFormID") val itemFormID:Int,
    @ColumnInfo(name = "routeID") val routeID:Int,
    @ColumnInfo(name = "facilityID") val facilityID:Int,
    @ColumnInfo(name = "isEDL") val isEDL: Boolean,
)

data class ItemMasterWithDrugMaster(
    @Embedded val itemMaster: ItemMasterList,
    @Relation(
        parentColumn = "itemFormID",
        entityColumn = "itemFormID",
    )
    val drugFormMaster: DrugFormMaster?
)
