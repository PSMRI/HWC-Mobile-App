package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import org.piramalswasthya.cho.utils.generateUuid

@Entity(
    tableName = "Diagnosis_Cases_Recorde",
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
data class DiagnosisCaseRecord(
    @PrimaryKey
    var diagnosisCaseRecordId:String,
    @ColumnInfo(name = "diagnosis") val diagnosis: String,
    @ColumnInfo(name = "patientID") val patientID: String,
    @ColumnInfo(name = "benFlowID") var benFlowID: Long? = null,
    @ColumnInfo(name = "benVisitNo") var benVisitNo: Int? = 0,
) {
    constructor(patient: Patient, benFlow: BenFlow, provisionalDiagnosisUpsync: ProvisionalDiagnosisUpsync) : this(
        generateUuid(),
        provisionalDiagnosisUpsync.term,
        patient.patientID,
        benFlow.benFlowID,
        benFlow.benVisitNo,
    )
}
