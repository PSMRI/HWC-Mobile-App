package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import org.piramalswasthya.cho.configuration.FormDataModel

@Entity(tableName = "PAIN_SYMPTOM_ASSESSMENT")
@JsonClass(generateAdapter = true)
data class PainAndSymptomAssessment(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "assessment_id")
    val assessmentId: Long = 0L,

    @ColumnInfo(name = "patient_id")
    val patientID: String,

    @ColumnInfo(name = "ben_visit_no")
    val benVisitNo: Int?,


    @ColumnInfo(name = "pain_severity")
    var painSeverity: String? = null,

    @ColumnInfo(name = "pain_duration")
    var painDuration: String? = null,


    @ColumnInfo(name = "symptoms_present")
    var symptomsPresent: Boolean? = null,

    @ColumnInfo(name = "other_symptoms_severity")
    var otherSymptomsSeverity: String? = null,

    // ---------------- Relief ----------------

    @ColumnInfo(name = "immediate_relief_provided")
    var immediateReliefProvided: Boolean? = null,

    // ---- Section C: Palliative Care Identification ----

    @ColumnInfo(name = "persistent_pain_present")
    var persistentPainPresent: Boolean? = null,

    @ColumnInfo(name = "pain_assessment_enabled")
    var painAssessmentEnabled: Boolean? = null,

    @ColumnInfo(name = "distressing_symptoms_present")
    var distressingSymptoms: String? = null,

    @ColumnInfo(name = "bedridden_or_severely_dependent")
    var bedriddenOrSeverelyDependent: Boolean? = null,

    @ColumnInfo(name = "life_limiting_illness_known")
    var lifeLimitingIllnessKnown: Boolean? = null,

    @ColumnInfo(name = "caregiver_support_required")
    var caregiverSupportRequired: Boolean? = null,

    @ColumnInfo(name = "palliative_care_eligible")
    var palliativeCareEligible: Boolean? = null,

    // ---- Symptom Assessment (Basic) ----
    @ColumnInfo(name = "basic_symptoms_selected")
    var basicSymptomsSelected: String? = null,

    // ---- Basic Management (CHO Level) ----
    @ColumnInfo(name = "basic_symptom_relief_provided")
    var basicSymptomReliefProvided: Boolean? = null,

    @ColumnInfo(name = "basic_psychosocial_support_provided")
    var basicPsychosocialSupportProvided: Boolean? = null,

    @ColumnInfo(name = "basic_caregiver_counselling_provided")
    var basicCaregiverCounsellingProvided: Boolean? = null,

    @ColumnInfo(name = "basic_management_remarks")
    var basicManagementRemarks: String? = null,

    @ColumnInfo(name = "syncState")
    var syncState: Int = 0,


    // ---------------- Referral & Follow-up (Section F) ----------------

    @Embedded
    val referralFollowUp: ReferralFollowUpFields = ReferralFollowUpFields()

) : FormDataModel, ReferralFollowUpModel by referralFollowUp