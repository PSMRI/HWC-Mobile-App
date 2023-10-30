package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.ForeignKey.Companion.CASCADE
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
    @ColumnInfo(name = "consultantName") val consultantName: String?,
    @ColumnInfo(name = "patientID") val patientID: String,
    @ColumnInfo(name = "benFlowID") var benFlowID: Long,
    @ColumnInfo(name = "benVisitNo") var benVisitNo: Int? = 0,
    @ColumnInfo(name = "issueType") var issueType: String? = null,
)

@Entity(tableName = "prescribed_drugs",
    foreignKeys = [ForeignKey(
        entity = Prescription::class,
        childColumns = ["prescriptionID"],
        parentColumns = ["id"],
        onDelete = CASCADE
    )])
@JsonClass(generateAdapter = true)
data class PrescribedDrugs (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "drugID") val drugID: Long,
    @ColumnInfo(name = "prescriptionID") val prescriptionID: Long,
    @ColumnInfo(name = "dose") val dose: String?=null,
    @ColumnInfo(name = "drugForm") val drugForm: String?=null,
    @ColumnInfo(name = "drugStrength") val drugStrength: String? =null,
    @ColumnInfo(name = "duration") val duration: String?=null,
    @ColumnInfo(name = "durationUnit") val durationUnit: String?= null,
    @ColumnInfo(name = "frequency") val frequency: String?=null,
    @ColumnInfo(name = "genericDrugName") val genericDrugName: String,
    @ColumnInfo(name = "isEDL") val isEDL: Boolean,
    @ColumnInfo(name = "qtyPrescribed") val qtyPrescribed: Int? =0,
    @ColumnInfo(name = "route") val route: String?=null,
    @ColumnInfo(name = "instructions") val instructions: String?= null
)

@Entity(tableName = "prescribed_drugs_batch",
    foreignKeys = [ForeignKey(
        entity = PrescribedDrugs::class,
        childColumns = ["drugID"],
        parentColumns = ["id"],
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
    val consultantName: String?,
    val prescriptionID: Long,
    val visitCode: Long,
    var issueType: String? = "System Issue",
    var itemList: List<PrescriptionItemDTO>
    )

@JsonClass(generateAdapter = true)
data class PrescriptionItemDTO(
    val id: Long,
    val drugID: Long,
    val dose: String? =null,
    val drugForm: String? =null,
    val duration: String?=null,
    val durationUnit: String?= null,
    val frequency: String? =null,
    val genericDrugName: String,
    val drugStrength: String? =null,
    var batchList: List<PrescriptionBatchDTO>,
    val isEDL: Boolean,
    val qtyPrescribed: Int? =0,
    val route: String? =null,
    val instructions: String? = null

)

@JsonClass(generateAdapter = true)
data class PrescriptionBatchDTO(
    val expiresIn: Int,
    val batchNo: String,
    val expiryDate: String,
    val itemStockEntryID: Int,
    val qty: Int,
)

@JsonClass(generateAdapter = true)
data class PrescriptionBatchApiDTO(
    val id: Long,
    val itemID: Long,
    val itemName: String,
    var itemBatchList: List<PrescriptionBatchItemApiDTO>,

)
@JsonClass(generateAdapter = true)
data class PrescriptionBatchItemApiDTO(
    val expiresIn: Int,
    val batchNo: String,
    val expiryDate: String,
    val itemStockEntryID: Int,
    val quantity: Int,
    val quantityInHand: Int

)

@JsonClass(generateAdapter = true)
data class PharmacistPatientDataRequest(
    val beneficiaryRegID: Long,
    val benFlowID: Long
)

@JsonClass(generateAdapter = true)
data class PrescribedMedicineDataRequest(
    val beneficiaryRegID: Long,
    val facilityID: Int,
    val visitCode: Long,
)

@JsonClass(generateAdapter = true)
data class AllocationItemDataRequest(
    val itemID: Long,
    val quantity: Int? =0,
)

@JsonClass(generateAdapter = true)
data class PharmacistItemStockExitDataRequest(
    val itemID: Long,
    val itemStockEntryID: Int,
    val quantity: Int? =0,
    val createdBy: String
)

@JsonClass(generateAdapter = true)
data class PharmacistPatientIssueDataRequest(
    val issuedBy: String,
    val visitCode: Long?,
    val facilityID: Int,
    val age: Int?,
    val beneficiaryID: Long?,
    val benRegID: Long,
    val createdBy: String,
    val providerServiceMapID: Int?,
    val doctorName: String?,
    val gender: String?,
    val issueType: String?,
    val patientName: String,
    val prescriptionID: Long?,
    val reference: String,
    val visitID: Long?,
    val visitDate: String?,
    val parkingPlaceID: Int?,
    val vanID: Int?,
    var itemStockExit: List<PharmacistItemStockExitDataRequest>
)
