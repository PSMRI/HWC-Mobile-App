package org.piramalswasthya.cho.model

import androidx.annotation.NonNull
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
            childColumns = ["itemFormId"],
            onDelete = ForeignKey.NO_ACTION
        ),
    ]
)
@JsonClass(generateAdapter = true)
data class ItemMasterList(
    @PrimaryKey
    @ColumnInfo(name = "itemID", typeAffinity = ColumnInfo.Companion.INTEGER, defaultValue = "0")
    val itemID: Int? = 0,
    @ColumnInfo(name = "id") val id:Int,
    @ColumnInfo(name = "itemName") val itemName:String,
    @ColumnInfo(name = "DropDownForMed") var dropdownForMed:String?,
    @ColumnInfo(name = "strength") val strength:String?,
    @ColumnInfo(name = "unitOfMeasurement") val unitOfMeasurement:String?,
    @ColumnInfo(name = "quantityInHand") val quantityInHand:Int?,
    @ColumnInfo(name = "itemFormId") val itemFormID:Int,
    @ColumnInfo(name = "routeID") val routeID:Int,
    @ColumnInfo(name = "facilityID") val facilityID:Int,
    @ColumnInfo(name = "isEDL") val isEDL: Boolean,
)

//data class ItemMasterWithDrugMaster(
//    @Embedded val itemMaster: ItemMasterList,
//    @Relation(
//        parentColumn = "itemFormID",
//        entityColumn = "itemFormID",
//    )
//    val drugFormMaster: DrugFormMaster?
//)
