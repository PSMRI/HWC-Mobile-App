package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.squareup.moshi.JsonClass
import org.piramalswasthya.cho.database.room.SyncState

@Entity(
    tableName = "PATIENT_VISIT_INFO_SYNC",
    primaryKeys = ["patientID", "benVisitNo"],
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
data class PatientVisitInfoSync(

    @ColumnInfo(name = "patientID")
    var patientID: String = "",

    @ColumnInfo(name="nurseDataSynced")
    var nurseDataSynced: SyncState? = SyncState.UNSYNCED,

    @ColumnInfo(name="doctorDataSynced")
    var doctorDataSynced: SyncState? = SyncState.UNSYNCED,


    @ColumnInfo(name="labDataSynced")
    var labDataSynced: SyncState? = SyncState.NOT_ADDED,

    @ColumnInfo(name="createNewBenFlow")
    var createNewBenFlow: Boolean = false,

    @ColumnInfo(name = "benVisitNo")
    var benVisitNo: Int = 0,

    @ColumnInfo(name = "benFlowID")
    var benFlowID: Long? = null,

    @ColumnInfo(name = "nurseFlag")
    var nurseFlag: Int? = 1,

    @ColumnInfo(name = "doctorFlag")
    var doctorFlag: Int? = 1,

    @ColumnInfo(name = "lab_technician_flag")
    var labTechnicianFlag: Int? = 0,

    @ColumnInfo(name = "pharmacist_flag")
    var pharmacist_flag: Int? = 1,

){
    constructor(benFlow: BenFlow, patient: Patient) : this(
        patientID = patient.patientID,
        benVisitNo = benFlow.benVisitNo!!,
        benFlowID = benFlow.benFlowID,
        nurseFlag = benFlow.nurseFlag,
        doctorFlag = benFlow.doctorFlag,
        pharmacist_flag = benFlow.pharmacist_flag,
        nurseDataSynced = SyncState.SYNCED,
        doctorDataSynced = SyncState.SYNCED,
    )
}

data class PatientVisitInfoSyncWithPatient(
    @Embedded val patientVisitInfoSync: PatientVisitInfoSync,
    @Relation(
        parentColumn = "patientID",
        entityColumn = "patientID"
    )
    val patient: Patient,
)