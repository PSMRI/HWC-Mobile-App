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
import org.piramalswasthya.cho.utils.nullIfEmpty

@Entity(
    tableName = "Investigation_Case_Record",
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
data class InvestigationCaseRecord(
    @PrimaryKey
    var investigationCaseRecordId:String,
    @ColumnInfo(name = "previousTestIds") val previousTestIds: String?,
    @ColumnInfo(name = "newTestIds") val newTestIds: String?,
    @ColumnInfo(name = "externalInvestigations") val externalInvestigations: String?,
    @ColumnInfo(name = "counsellingProvidedList") val counsellingProvidedList: String?,
    @ColumnInfo(name = "CounsellingTypes") val counsellingTypes: String?,
    @ColumnInfo(name = "institutionId") val institutionId: Int? = null,
    @ColumnInfo(name = "referReson") var referReson: String? = null,
    @ColumnInfo(name = "patientID") val patientID: String,
    @ColumnInfo(name = "benFlowID") var benFlowID: Long? = null,
    @ColumnInfo(name = "benVisitNo") var benVisitNo: Int? = 0,
) {
    constructor(docData: DoctorDataDownSync, patient: Patient, benFlow: BenFlow) : this(
        generateUuid(),
        previousTestIds = docData.investigation?.laboratoryList?.map {
            it.procedureID
        }?.joinToString(",").nullIfEmpty(),
        newTestIds = null,
        externalInvestigations = docData.investigation?.externalInvestigations,
        counsellingProvidedList = docData.investigation?.counsellingProvidedList,
        counsellingTypes = null,
        institutionId = docData.Refer?.referredToInstituteID,
        referReson = docData.Refer?.referralReason,
        patientID = patient.patientID,
        benFlowID = benFlow.benFlowID,
        benVisitNo = benFlow.benVisitNo
    )
}

data class InvestigationCaseRecordWithHigherHealthCenter(
    @Embedded val investigationCaseRecord: InvestigationCaseRecord,
    @Relation(
        parentColumn = "institutionId",
        entityColumn = "institutionID",
    )
    val higherHealthCenter: HigherHealthCenter?,
)

