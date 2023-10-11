package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.squareup.moshi.JsonClass
import org.piramalswasthya.cho.utils.generateUuid

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
) {
    constructor(patient: Patient, benFlow: BenFlow, prescriptionData: PrescriptionData) : this(
        generateUuid(),
        prescriptionData.drugID,
        prescriptionData.frequency,
        prescriptionData.duration,
        instruciton = null,
        prescriptionData.unit,
        patient.patientID,
        benFlow.benFlowID,
        benFlow.benVisitNo
    )
}

@DatabaseView(
    viewName = "Prescription_With_ItemMaster_And_DrugFormMaster",
    value = "SELECT p.prescriptionCaseRecordId as prescriptionCaseRecordId, p.itemId as itemId, p.frequency as frequency, p.duration as duration, " +
            "p.instruction as instruction, p.unit as unit, p.patientID as patientID, p.benFlowID as benFlowID, p.benVisitNo as benVisitNo, " +
            "i.id as id, i.itemName as itemName, i.DropDownForMed as dropdownForMed, i.strength as strength, i.unitOfMeasurement as unitOfMeasurement, " +
            "i.quantityInHand as quantityInHand, i.itemFormId as itemFormID, i.routeID as routeID, i.facilityID as facilityID, i.isEDL as isEDL, " +
            "d.itemFormName as itemFormName " +
            "FROM Prescription_Cases_Recorde p " +
            "LEFT OUTER JOIN Item_Master_List i ON p.itemId = i.itemID " +
            "LEFT OUTER JOIN Drug_Form_Master d ON i.itemFormId = d.itemFormID",
)
data class PrescriptionWithItemMasterAndDrugFormMaster(

    var prescriptionCaseRecordId:String,
    val itemId: Int,
    val frequency: String?,
    val duration: String?,
    val instruciton: String?,
    val unit: String?,
    val patientID: String,
    var benFlowID: Long?,
    var benVisitNo: Int?,

    val id:Int,
    val itemName:String,
    var dropdownForMed:String?,
    val strength:String?,
    val unitOfMeasurement:String?,
    val quantityInHand:Int?,
    val itemFormID:Int,
    val routeID:Int,
    val facilityID:Int,
    val isEDL: Boolean,

    val itemFormName:String

//    @Embedded val prescription: PrescriptionCaseRecord,
//    @Embedded val itemMaster: ItemMasterList,
//    @Embedded val drugMaster: DrugFormMaster
)

//data class PrescriptionCaseRecordWithItemMaster(
//    @Embedded val prescriptionCaseRecord: PrescriptionCaseRecord,
//    @Relation(
//        parentColumn = "itemId",
//        entityColumn = "itemID"
//    )
//    val itemMaster: ItemMasterList?,
//    @Relation(
//        parentColumn = "itemId",
//        entityColumn = "itemFormID"
//    )
//    val drugFormMaster: DrugFormMaster?,
//)
