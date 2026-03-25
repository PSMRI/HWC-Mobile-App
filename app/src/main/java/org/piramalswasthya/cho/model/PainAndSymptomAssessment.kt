package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
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

    // ---------------- Referral & Follow-up (Section F) ----------------

    @ColumnInfo(name = "referral_required")
    override var referralRequired: Boolean? = null,

    @ColumnInfo(name = "referral_level")
    override var referralLevel: String? = null,

    @ColumnInfo(name = "reason_for_referral")
    override var reasonForReferral: String? = null,

    @ColumnInfo(name = "follow_up_required")
    override var followUpRequired: Boolean? = null,

    @ColumnInfo(name = "follow_up_date")
    override var followUpDate: String? = null

) : FormDataModel, ReferralFollowUpModel