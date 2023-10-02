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
    tableName = "Prescription_Cases_Recorde",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["patientID"],
            childColumns = ["patientID"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = BenFlow::class,
            parentColumns = ["benFlowID"],
            childColumns = ["benFlowID"],
            onDelete = ForeignKey.NO_ACTION
        ),
    ]
)
@JsonClass(generateAdapter = true)
data class PrescriptionCaseRecord(
    @PrimaryKey
    var prescriptionCaseRecordId:String,
    @ColumnInfo(name = "itemId") val itemId: Int?,
    @ColumnInfo(name = "frequency") val frequency: String?,
    @ColumnInfo(name = "duration") val duration: String?,
    @ColumnInfo(name = "instruction") val instruciton: String?,
    @ColumnInfo(name = "unit") val unit: String?,
    @ColumnInfo(name = "patientID") val patientID: String,
    @ColumnInfo(name = "benFlowID") var benFlowID: Long? = null,
    @ColumnInfo(name = "benVisitNo") var benVisitNo: Int? = 0,
)

data class PrescriptionCaseRecordWithItemMaster(
    @Embedded val prescriptionCaseRecord: PrescriptionCaseRecord,
    @Relation(
        parentColumn = "itemId",
        entityColumn = "itemID"
    )
    val itemMasterWithDrugMaster: ItemMasterWithDrugMaster?,
//    @Relation(
//        parentColumn = "itemFormID",
//        entityColumn = "formID",
//    )
//    val drugFormMaster: DrugFormMaster?
)
