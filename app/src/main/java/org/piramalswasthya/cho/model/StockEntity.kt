package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass


//@Entity(
//    tableName = "StockEntity"
//)
//@JsonClass(generateAdapter = true)
//data class StockEntity(
//    @PrimaryKey(autoGenerate = true) val id: Long = 0,
//    @ColumnInfo(name = "itemID") val itemID: Int,
//    @ColumnInfo(name = "itemName") val itemName: String,
//    @ColumnInfo(name = "itemForm") val itemForm: String,
//    @ColumnInfo(name = "strength") val strength: String,
//    @ColumnInfo(name = "quantityInHand") val quantityInHand: Int,
//    @ColumnInfo(name = "routeName") val routeName: String
//)

@Entity(
    tableName = "Batch",
    foreignKeys = [
        ForeignKey(
            entity = ItemMasterList::class,
            parentColumns = ["itemID"],
            childColumns = ["itemID"]
        )
    ],
    indices = [Index(value = ["stockEntityId"], unique = true)]
)
@JsonClass(generateAdapter = true)
data class
Batch(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "itemID") val itemID: Int,
    @ColumnInfo(name = "stockEntityId") val stockEntityId: Long,
    @ColumnInfo(name = "batchNo") val batchNo: String,
    @ColumnInfo(name = "expiryDate") val expiryDate: String,
    @ColumnInfo(name = "quantity") val quantity: Int,
    @ColumnInfo(name = "quantityInHand") val quantityInHand: Int
)

@JsonClass(generateAdapter = true)
data class ApiItemStockEntry(
    val itemID: Int,
    val quantityInHand: Int,
   // val item: ApiItem,
    val batchNo: String,
    val expiryDate: String,
    val itemStockEntryID: Int,
    val quantity: Int,
)

@JsonClass(generateAdapter = true)
data class ApiItem(
    val itemID: Int,
    val itemName: String,
    val itemForm: ApiItemForm,
    val strength: String,
    val route: ApiRoute
)
@JsonClass(generateAdapter = true)
data class ApiItemForm(
    val itemForm: String
)
@JsonClass(generateAdapter = true)
data class ApiRoute(
    val routeName: String
)

//{
//    "data": [
//    {
//        "itemStockEntryID": 24215,
//        "vanSerialNo": 24215,
//        "facilityID": 96,
//        "syncFacilityID": 96,
//        "itemID": 149,
//        "quantityInHand": 5000,
//        "batchNo": "10005",
//        "expiryDate": "2030-10-05T00:00:00.000Z",
//        "item": {
//        "itemID": 149,
//        "itemForm": {
//        "itemFormID": 1,
//        "itemForm": "Tablet"
//    },
//        "strength": "500",
//        "route": {
//        "routeID": 1,
//        "routeName": "Oral"
//    }
//    }
//    }
//    // ... more items
//    ]
//}

