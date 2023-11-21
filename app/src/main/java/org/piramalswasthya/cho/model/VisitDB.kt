package org.piramalswasthya.cho.model

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import org.piramalswasthya.cho.utils.DateTimeUtil
import org.piramalswasthya.cho.utils.generateUuid
import java.text.SimpleDateFormat
import java.util.Date

@Entity(
    tableName = "Visit_DB",
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
data class VisitDB(
    @PrimaryKey
    val visitId:String,
    @ColumnInfo(name = "category") val category: String?,
    @ColumnInfo(name = "reasonForVisit") val reasonForVisit: String?,
    @ColumnInfo(name = "subCategory") val subCategory: String?,
    @ColumnInfo(name = "patientID") val patientID: String,
    @ColumnInfo(name = "benFlowID") var benFlowID: Long? = null,
    @ColumnInfo(name = "benVisitNo") var benVisitNo: Int? = 0,
    @ColumnInfo(name = "benVisitDate") var benVisitDate: String? = null,
    @ColumnInfo(name = "createdBy") var createdBy: String? = null,
){
    constructor(nurseData: BenDetailsDownsync, patient: Patient, benFlow: BenFlow) : this(
        generateUuid(),
        nurseData.GOPDNurseVisitDetail?.visitCategory,
        nurseData.GOPDNurseVisitDetail?.visitReason,
        nurseData.GOPDNurseVisitDetail?.subVisitCategory,
        patient.patientID,
        benFlow.benFlowID,
        benFlow.benVisitNo,
        DateTimeUtil.formatBenVisitDate(benFlow.registrationDate),
        benFlow.agentId
    )
}
