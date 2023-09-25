package org.piramalswasthya.cho.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import org.piramalswasthya.cho.database.room.SyncState

@Entity(tableName = "PATIENT_VISIT_INFO_SYNC",)
@JsonClass(generateAdapter = true)
data class PatientVisitInfoSync(

    @PrimaryKey
    @NonNull
    var patientID: String = "",

    @ColumnInfo(name="beneficiaryID")
    var beneficiaryID: Long? = null,

    @ColumnInfo(name="beneficiaryRegID")
    var beneficiaryRegID: Long? = null,

    @ColumnInfo(name="nurseDataSynced")
    var nurseDataSynced: SyncState? = SyncState.UNSYNCED,

    @ColumnInfo(name="doctorDataSynced")
    var doctorDataSynced: SyncState? = SyncState.UNSYNCED,

    @ColumnInfo(name = "benVisitNo")
    var benVisitNo: Int? = 0,

    @ColumnInfo(name = "nurseFlag")
    var nurseFlag: Int? = 1,

    @ColumnInfo(name = "doctorFlag")
    var doctorFlag: Int? = 0,

    @ColumnInfo(name = "pharmacist_flag")
    var pharmacist_flag: Int? = 0,

)
