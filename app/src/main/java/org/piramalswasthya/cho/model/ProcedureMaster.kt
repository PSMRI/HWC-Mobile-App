package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass


@Entity(
    tableName = "procedure_master"
)
@JsonClass(generateAdapter = true)
data class ProcedureMaster (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "procedure_id") val procedureID: Long,
    @ColumnInfo(name = "procedureDesc") val procedureDesc: String,
    @ColumnInfo(name = "procedureType") val procedureType: String,
    @ColumnInfo(name = "prescriptionID") val prescriptionID: Long,
    @ColumnInfo(name = "procedureName") val procedureName: String,
    @ColumnInfo(name = "isMandatory") val isMandatory: Boolean
)

@Entity(tableName = "component_details_master",
    foreignKeys = [ForeignKey(
        entity = Procedure::class,
        childColumns = ["procedure_id"],
        parentColumns = ["id"],
        onDelete = CASCADE
    )])
@JsonClass(generateAdapter = true)
data class ComponentDetailsMaster (
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
    @ColumnInfo(name = "test_component_desc") val testComponentDesc: String
)

@Entity(tableName = "component_options_master",
    foreignKeys = [ForeignKey(
        entity = ComponentDetailsMaster::class,
        childColumns = ["component_details_id"],
        parentColumns = ["id"],
        onUpdate = CASCADE,
        onDelete = CASCADE
    )])
data class ComponentOptionsMaster (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "component_details_id") var componentDetailsId: Long,
    @ColumnInfo(name = "name") val name: String? = null
)


@JsonClass(generateAdapter = true)
data class ProcedureMasterDTO(
    val benRegId: Long,
    val procedureDesc: String,
    val procedureType: String,
    val prescriptionID: Long,
    val procedureID: Long,
    val procedureName: String,
    var compListDetails: List<ComponentDetailMasterDTO>,
    val isMandatory: Boolean
)

@JsonClass(generateAdapter = true)
data class ComponentDetailMasterDTO(
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
    var compOpt: List<ComponentOptionsMasterDTO>
)

@JsonClass(generateAdapter = true)
data class ComponentOptionsMasterDTO(
    val name: String?,
)
