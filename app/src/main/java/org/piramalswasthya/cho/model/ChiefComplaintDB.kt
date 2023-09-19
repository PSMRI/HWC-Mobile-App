package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(
    tableName = "Chielf_Complaint_DB",
    foreignKeys = [
        ForeignKey(
            entity = VisitDB::class,
            parentColumns = ["visitId"],
            childColumns = ["visitId"],
            onDelete = ForeignKey.NO_ACTION
        ),
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
data class ChiefComplaintDB(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "chiefComplaint") val chiefComplaint: String,
    @ColumnInfo(name = "duration") val duration: String,
    @ColumnInfo(name = "durationUnit") val durationUnit: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "visitId") val visitId:String,
    @ColumnInfo(name = "patientID") val patientID: String,
    @ColumnInfo(name = "beneficiaryID") var beneficiaryID: Long? = null,
    @ColumnInfo(name = "beneficiaryRegID") var beneficiaryRegID: Long? = null,
    @ColumnInfo(name = "benFlowID") var benFlowID: Long? = null,
)
