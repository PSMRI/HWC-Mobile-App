package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
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
    @ColumnInfo(name = "testName") val testName: String?,
    @ColumnInfo(name = "externalInvestigation") val externalInvestigation: String?,
    @ColumnInfo(name = "CounsellingTypes") val counsellingTypes:  String?,
    @ColumnInfo(name = "refer") val refer:  String?,
    @ColumnInfo(name = "patientID") val patientID: String,
    @ColumnInfo(name = "beneficiaryID") var beneficiaryID: Long? = null,
    @ColumnInfo(name = "beneficiaryRegID") var beneficiaryRegID: Long? = null,
    @ColumnInfo(name = "benFlowID") var benFlowID: Long? = null,
)
