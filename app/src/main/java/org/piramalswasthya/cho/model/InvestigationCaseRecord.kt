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
    @ColumnInfo(name = "testIds") val testIds: String?,
    @ColumnInfo(name = "externalInvestigation") val externalInvestigation: String?,
    @ColumnInfo(name = "CounsellingTypes") val counsellingTypes: String?,
    @ColumnInfo(name = "institutionId") val institutionId: Int? = null,
    @ColumnInfo(name = "patientID") val patientID: String,
    @ColumnInfo(name = "benFlowID") var benFlowID: Long? = null,
    @ColumnInfo(name = "benVisitNo") var benVisitNo: Int? = 0,
)

data class InvestigationCaseRecordWithHigherHealthCenter(
    @Embedded val investigationCaseRecord: InvestigationCaseRecord,
    @Relation(
        parentColumn = "institutionId",
        entityColumn = "institutionID",
    )
    val higherHealthCenter: HigherHealthCenter?,
)

