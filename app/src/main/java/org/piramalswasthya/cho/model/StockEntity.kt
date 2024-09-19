package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass



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


