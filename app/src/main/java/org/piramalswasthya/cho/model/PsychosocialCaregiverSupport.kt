package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import org.piramalswasthya.cho.configuration.FormDataModel

@Entity(tableName = "PSYCHOSOCIAL_CAREGIVER_SUPPORT")
@JsonClass(generateAdapter = true)
data class PsychosocialCaregiverSupport(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "assessment_id")
    val assessmentId: Long = 0L,

    @ColumnInfo(name = "patient_id")
    val patientID: String,

    @ColumnInfo(name = "ben_visit_no")
    val benVisitNo: Int?,

    // ---------------- Counselling ----------------

    @ColumnInfo(name = "psychosocial_counselling_provided")
    var psychosocialCounsellingProvided: Boolean? = null,

    @ColumnInfo(name = "caregiver_counselling_provided")
    var caregiverCounsellingProvided: Boolean? = null,

    // ---------------- Caregiver ----------------

    @ColumnInfo(name = "caregiver_distress_identified")
    var caregiverDistressIdentified: Boolean? = null,

    // ---------------- Remarks ----------------

    @ColumnInfo(name = "counselling_remarks")
    var counsellingRemarks: String? = null,

    // ---------------- Referral & Follow-up (Section F) ----------------

    @ColumnInfo(name = "referral_required")
    var referralRequired: Boolean? = null,

    @ColumnInfo(name = "referral_level")
    var referralLevel: String? = null,

    @ColumnInfo(name = "reason_for_referral")
    var reasonForReferral: String? = null,

    @ColumnInfo(name = "follow_up_required")
    var followUpRequired: Boolean? = null,

    @ColumnInfo(name = "follow_up_date")
    var followUpDate: String? = null

) : FormDataModel