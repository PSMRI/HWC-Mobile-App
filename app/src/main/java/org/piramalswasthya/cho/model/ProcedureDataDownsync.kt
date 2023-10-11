package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.squareup.moshi.JsonClass

@Entity(
    tableName = "PROCEDURE_DATA_DOWNSYNC",
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
data class ProcedureDataDownsync (

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name="prescriptionID")
    val prescriptionID: Int?,

    @ColumnInfo(name="procedureID")
    val procedureID: Int?,

    @ColumnInfo(name="createdDate")
    val createdDate: String?,

    @ColumnInfo(name="procedureName")
    val procedureName: String?,

    @ColumnInfo(name="patientID")
    val patientID: String,

    @ColumnInfo(name="benVisitNo")
    val benVisitNo: Int,
){
    constructor(labReportData: LabReportData, patientVisitInfoSync: PatientVisitInfoSync) : this(
        0,
        labReportData.prescriptionID,
        labReportData.procedureID,
        labReportData.createdDate,
        labReportData.procedureName,
        patientVisitInfoSync.patientID,
        patientVisitInfoSync.benVisitNo,
    )
}

@Entity(
    tableName = "COMPONENT_DATA_DOWNSYNC",
    foreignKeys = [
        ForeignKey(
            entity = ProcedureDataDownsync::class,
            parentColumns = ["id"],
            childColumns = ["procedureDataID"],
            onDelete = ForeignKey.CASCADE
        ),
    ]
)
@JsonClass(generateAdapter = true)
data class ComponentDataDownsync (

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name="procedureDataID")
    val procedureDataID: Long?,

    @ColumnInfo(name="testResultValue")
    val testResultValue: String?,

    @ColumnInfo(name="testResultUnit")
    val testResultUnit: String?,

    @ColumnInfo(name="testComponentID")
    val testComponentID: Int?,

    @ColumnInfo(name="componentName")
    val componentName: String?,

    @ColumnInfo(name="remarks")
    val remarks: String?
){
    constructor(componentData: ComponentData, procedureDataID: Long) : this(
        0,
        procedureDataID,
        componentData.testResultValue,
        componentData.testResultUnit,
        componentData.testComponentID,
        componentData.componentName,
        componentData.remarks
    )
}

data class ProcedureDataWithComponent(
    @Embedded val procedure: ProcedureDataDownsync,
    @Relation(
        parentColumn = "id",
        entityColumn = "procedureDataID"
    )
    val entityCList: List<ComponentDataDownsync>
)

