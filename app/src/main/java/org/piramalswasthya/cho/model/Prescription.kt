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
    tableName = "Prescription",
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
data class Prescription(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "prescriptionID") val prescriptionID: Long,
    @ColumnInfo(name = "beneficiaryRegID") val beneficiaryRegID: Long,
    @ColumnInfo(name = "visitCode") val visitCode: Long,
    @ColumnInfo(name = "consultantName") val consultantName: String,
    @ColumnInfo(name = "patientID") val patientID: String,
    @ColumnInfo(name = "benFlowID") var benFlowID: Long,
)

@Entity(tableName = "prescribed_drugs",
    foreignKeys = [ForeignKey(
        entity = Prescription::class,
        childColumns = ["prescriptionID"],
        parentColumns = ["prescriptionID"],
        onDelete = ForeignKey.CASCADE
    )])
@JsonClass(generateAdapter = true)
data class PrescribedDrugs (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "drugID") val drugID: Long,
    @ColumnInfo(name = "prescriptionID") val prescriptionID: Long,
    @ColumnInfo(name = "dose") val dose: String,
    @ColumnInfo(name = "drugForm") val drugForm: String,
    @ColumnInfo(name = "drugStrength") val drugStrength: String,
    @ColumnInfo(name = "duration") val duration: String,
    @ColumnInfo(name = "durationUnit") val durationUnit: String,
    @ColumnInfo(name = "frequency") val frequency: String,
    @ColumnInfo(name = "genericDrugName") val genericDrugName: String,
    @ColumnInfo(name = "isEDL") val isEDL: Boolean,
    @ColumnInfo(name = "qtyPrescribed") val qtyPrescribed: Int,
    @ColumnInfo(name = "route") val route: String,
    @ColumnInfo(name = "instructions") val instructions: String
)

@Entity(tableName = "prescribed_drugs_batch",
    foreignKeys = [ForeignKey(
        entity = PrescribedDrugs::class,
        childColumns = ["drugID"],
        parentColumns = ["drugID"],
        onDelete = ForeignKey.CASCADE
    )])
@JsonClass(generateAdapter = true)
data class PrescribedDrugsBatch (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "drugID") val drugID: Long,
    @ColumnInfo(name = "expiryDate") val expiryDate: String,
    @ColumnInfo(name = "expiresIn") val expiresIn: Int,
    @ColumnInfo(name = "batchNo") val batchNo: String,
    @ColumnInfo(name = "itemStockEntryID") val itemStockEntryID: Int,
    @ColumnInfo(name = "qty") val qty: Int
)

@JsonClass(generateAdapter = true)
data class PrescriptionDTO(
    val beneficiaryRegID: Long,
    val consultantName: String,
    val prescriptionID: Long,
    val visitCode: Long,
    var itemList: List<PrescriptionItemDTO>
    )

@JsonClass(generateAdapter = true)
data class PrescriptionItemDTO(
    val id: Long,
    val drugID: Long,
    val dose: String,
    val drugForm: String,
    val duration: String,
    val durationUnit: String,
    val frequency: String,
    val genericDrugName: String,
    val drugStrength: String,
    var batchList: List<PrescriptionBatchDTO>,
    val isEDL: Boolean,
    val qtyPrescribed: Int,
    val route: String,
    val instructions: String

)

@JsonClass(generateAdapter = true)
data class PrescriptionBatchDTO(
    val expiresIn: Int,
    val batchNo: String,
    val expiryDate: String,
    val itemStockEntryID: Int,
    val qty: Int

)
