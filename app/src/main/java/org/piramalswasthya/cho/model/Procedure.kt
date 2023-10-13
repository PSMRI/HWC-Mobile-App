package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass


@Entity(
    tableName = "procedure",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["patientID"],
            childColumns = ["patientID"],
            onDelete = ForeignKey.NO_ACTION
        ),
    ]
)
@JsonClass(generateAdapter = true)
data class Procedure (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "procedure_id") val procedureID: Long,
    @ColumnInfo(name = "procedureDesc") val procedureDesc: String,
    @ColumnInfo(name = "procedureType") val procedureType: String,
    @ColumnInfo(name = "prescriptionID") val prescriptionID: Long,
    @ColumnInfo(name = "procedureName") val procedureName: String,
    @ColumnInfo(name = "isMandatory") val isMandatory: Boolean,
    @ColumnInfo(name = "patientID") val patientID: String,
    @ColumnInfo(name = "benVisitNo") var benVisitNo: Int? = 0,
)

@Entity(tableName = "component_details",
    foreignKeys = [ForeignKey(
        entity = Procedure::class,
        childColumns = ["procedure_id"],
        parentColumns = ["id"],
        onDelete = CASCADE
    )])
@JsonClass(generateAdapter = true)
data class ComponentDetails (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "test_component_id") val testComponentID: Long,
    @ColumnInfo(name = "procedure_id") val procedureID: Long,
    @ColumnInfo(name = "range_normal_min") val rangeNormalMin: Int? = null,
    @ColumnInfo(name = "range_normal_max") val rangeNormalMax: Int? = null,
    @ColumnInfo(name = "range_min") val rangeMin: Int? = null,
    @ColumnInfo(name = "range_max") val rangeMax: Int? = null,
    @ColumnInfo(name = "isDecimal") val isDecimal: Boolean? = null,
    @ColumnInfo(name = "inputType") val inputType: String,
    @ColumnInfo(name = "measurement_nit") val measurementUnit: String? = null,
    @ColumnInfo(name = "test_component_name") val testComponentName: String,
    @ColumnInfo(name = "test_component_desc") val testComponentDesc: String,
    @ColumnInfo(name = "test_result_value") var testResultValue: String? = null,
    @ColumnInfo(name = "remarks") var remarks: String? = null,
)

@Entity(tableName = "component_option",
    foreignKeys = [ForeignKey(
        entity = ComponentDetails::class,
        childColumns = ["component_details_id"],
        parentColumns = ["id"],
        onUpdate = CASCADE,
        onDelete = CASCADE
    )])
data class ComponentOption (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "component_details_id") var componentDetailsId: Long,
    @ColumnInfo(name = "name") val name: String? = null
)


@JsonClass(generateAdapter = true)
data class ProcedureDTO(
    val benRegId: Long,
    val procedureDesc: String,
    val procedureType: String,
    val prescriptionID: Long,
    val procedureID: Long,
    val procedureName: String,
    var compListDetails: List<ComponentDetailDTO>,
    val isMandatory: Boolean
)

@JsonClass(generateAdapter = true)
data class ComponentDetailDTO(
    val id: Long,
    val range_normal_min: Int? = null,
    val range_normal_max: Int? = null,
    val range_min: Int? = null,
    val range_max: Int? = null,
    val isDecimal: Boolean? = null,
    val inputType: String,
    val testComponentID: Long,
    val measurementUnit: String? = null,
    val testComponentName: String,
    var testComponentDesc: String,
    var testResultValue: String?,
    var remarks: String?,
    var compOpt: List<ComponentOptionDTO>
)

@JsonClass(generateAdapter = true)
data class ComponentOptionDTO(
    val name: String?,
)

@JsonClass(generateAdapter = true)
data class ProcedureResultDTO(
    val prescriptionID: Long,
    val procedureID: Long,
    var compList: List<ComponentResultDTO>
)

@JsonClass(generateAdapter = true)
data class ComponentResultDTO(
    val testComponentID: Long,
    val testResultValue: String?,
    val testResultUnit: String?,
    val remarks: String?
)
@JsonClass(generateAdapter = true)
data class LabResultDTO(
    val labTestResults: List<ProcedureResultDTO>,
    val radiologyTestResults: List<ProcedureResultDTO>?,
    val labCompleted: Boolean,
    val createdBy: String,
    val doctorFlag: String,
    val nurseFlag: String,
    val beneficiaryRegID: Long?,
    val beneficiaryID: Long?,
    val benFlowID: Long?,
    val visitID: Long?,
    val visitCode: Long?,
    val providerServiceMapID: Int?,
    val specialist_flag: String?,
    val vanID: Int?,
    val parkingPlaceID:Int?
)