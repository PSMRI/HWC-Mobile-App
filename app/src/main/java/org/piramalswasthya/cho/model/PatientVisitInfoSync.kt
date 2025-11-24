package org.piramalswasthya.cho.model

import android.util.Log
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.squareup.moshi.JsonClass
import org.piramalswasthya.cho.database.room.SyncState
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    @ColumnInfo(name="prescriptionID")
    var prescriptionID: Int? = null,

    @ColumnInfo(name="nurseDataSynced")
    var nurseDataSynced: SyncState? = SyncState.UNSYNCED,

    @ColumnInfo(name="doctorDataSynced")
    var doctorDataSynced: SyncState? = SyncState.UNSYNCED,

    @ColumnInfo(name="pharmacistDataSynced")
    var pharmacistDataSynced: SyncState? = SyncState.UNSYNCED,

    @ColumnInfo(name="labDataSynced")
    var labDataSynced: SyncState? = SyncState.NOT_ADDED,

    @ColumnInfo(name="createNewBenFlow")
    var createNewBenFlow: Boolean? = false,

    @ColumnInfo(name = "benVisitNo")
    var benVisitNo: Int = 0,

    @ColumnInfo(name = "benFlowID")
    var benFlowID: Long? = null,

    @ColumnInfo(name = "nurseFlag")
    var nurseFlag: Int? = 1,

    @ColumnInfo(name = "doctorFlag")
    var doctorFlag: Int? = 0,

    @ColumnInfo(name = "labtechFlag")
    var labtechFlag: Int? = 0,

    @ColumnInfo(name = "pharmacist_flag")
    var pharmacist_flag: Int? = 0,

    @ColumnInfo(name = "visitDate")
    var visitDate: Date? = null,

    @ColumnInfo(name = "visitCategory")
    var visitCategory: String = "General OPD",

    @ColumnInfo(name = "referDate")
    var referDate: String? = null,

    @ColumnInfo(name = "referTo")
    var referTo: String? = null,

    @ColumnInfo(name = "referralReason")
    var referralReason: String? = null,

):Serializable{
    constructor(benFlow: BenFlow, patient: Patient) : this(
        patientID = patient.patientID,
        benVisitNo = benFlow.benVisitNo!!,
        benFlowID = benFlow.benFlowID,
//        nurseFlag = benFlow.nurseFlag,
//        doctorFlag = benFlow.doctorFlag,
//        pharmacist_flag = benFlow.pharmacist_flag,
        nurseDataSynced = SyncState.SYNCED,
        doctorDataSynced = SyncState.SYNCED,
        labDataSynced = SyncState.SYNCED,
        pharmacistDataSynced = SyncState.SYNCED,
        visitCategory = benFlow.VisitCategory ?: "",
        visitDate = benFlow.visitDate?.let {
            try {
                val inputFormat = SimpleDateFormat("MMM d, yyyy, h:mm:ss a", Locale.ENGLISH)
                val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                val parsedDate = inputFormat.parse(it)
                val onlyDateString = parsedDate?.let { d -> outputFormat.format(d) }
                onlyDateString?.let { s -> outputFormat.parse(s) }
            } catch (e: Exception) {
                null
            }
        }


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