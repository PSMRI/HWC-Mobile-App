package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import org.piramalswasthya.cho.configuration.FormDataModel

@Entity(tableName = "ELDERLY_HEALTH_ASSESSMENT",
    indices = [
        Index(name = "index_elderly_health_assessment_patient_visit", value = ["patient_id", "ben_visit_no"], unique = true)
    ]
)
@JsonClass(generateAdapter = true)
data class ElderlyHealthAssessment(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "assessment_id")
    val assessmentId: Long = 0L,

    @ColumnInfo(name = "patient_id")
    val patientID: String,

    @ColumnInfo(name = "ben_visit_no")
    val benVisitNo: Int = 0,

    @ColumnInfo(name = "geriatric_complaints")
    var geriatricComplaints: Boolean? = null,

    @ColumnInfo(name = "multiple_chronic_conditions")
    var multipleChronicConditions: Boolean? = null,

    @ColumnInfo(name = "recent_falls")
    var recentFalls: Boolean? = null,

    @ColumnInfo(name = "difficulty_walking_balance")
    var difficultyWalkingBalance: Boolean? = null,

    @ColumnInfo(name = "visual_hearing_difficulty")
    var visualHearingDifficulty: Boolean? = null,

    @ColumnInfo(name = "functional_decline")
    var functionalDecline: Boolean? = null,

    @ColumnInfo(name = "memory_loss")
    var memoryLoss: Boolean? = null,

    @ColumnInfo(name = "dementia_memory_loss")
    var dementiaMemoryLoss: Boolean? = null,

    @ColumnInfo(name = "dementia_disorientation")
    var dementiaDisorientation: Boolean? = null,

    @ColumnInfo(name = "dementia_behavioural_changes")
    var dementiaBehaviouralChanges: Boolean? = null,

    @ColumnInfo(name = "dementia_self_care_decline")
    var dementiaSelfCareDecline: Boolean? = null,

    @ColumnInfo(name = "dementia_screening_outcome")
    var dementiaScreeningOutcome: String? = null,

    @ColumnInfo(name = "dementia_referral_required")
    var dementiaReferralRequired: Boolean? = null,

    @Embedded
    val referralFollowUp: ReferralFollowUpFields = ReferralFollowUpFields()

) : FormDataModel, ReferralFollowUpModel by referralFollowUp
