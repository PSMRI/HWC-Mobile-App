package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import org.piramalswasthya.cho.configuration.FormDataModel

@Entity(tableName = "MENTAL_HEALTH_SCREENING")
@JsonClass(generateAdapter = true)
data class MentalHealthScreeningCache(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "screening_id")
    val screeningId: Long = 0L,

    @ColumnInfo(name = "patient_id")
    val patientID: String,

    @ColumnInfo(name = "ben_visit_no")
    val benVisitNo: Int?,

    // ── Q1: Emotional or behavioural concerns ─────────────────────────

    @ColumnInfo(name = "emotional_behavioural_concerns")
    var emotionalBehaviouralConcerns: Boolean? = null,

    // ── Q2: Substance use related concerns ────────────────────────────

    @ColumnInfo(name = "substance_use_concerns")
    var substanceUseConcerns: Boolean? = null,

    // ── Q3: Thoughts of self-harm or suicide ──────────────────────────

    @ColumnInfo(name = "self_harm_suicide_thoughts")
    var selfHarmSuicideThoughts: Boolean? = null,

    // ── Q4: Memory loss or confusion ──────────────────────────────────

    @ColumnInfo(name = "memory_loss_confusion")
    var memoryLossConfusion: Boolean? = null,

    // ── Q5: Seizures, fits, or loss of consciousness ──────────────────

    @ColumnInfo(name = "seizures_fits_loc")
    var seizuresFitsLoc: Boolean? = null,

    // ── Q6: Post-partum woman (auto-derived) ──────────────────────────

    @ColumnInfo(name = "is_postpartum")
    var isPostpartum: Boolean? = null,

    // ── PHQ-9 Screening (enabled by Q1=Yes, Q3=Yes, or Q6=Yes) ───────

    @ColumnInfo(name = "phq9_little_interest")
    var phq9LittleInterest: Int? = null,

    @ColumnInfo(name = "phq9_feeling_down")
    var phq9FeelingDown: Int? = null,

    @ColumnInfo(name = "phq9_sleep_trouble")
    var phq9SleepTrouble: Int? = null,

    @ColumnInfo(name = "phq9_feeling_tired")
    var phq9FeelingTired: Int? = null,

    @ColumnInfo(name = "phq9_appetite")
    var phq9Appetite: Int? = null,

    @ColumnInfo(name = "phq9_feeling_bad")
    var phq9FeelingBad: Int? = null,

    @ColumnInfo(name = "phq9_concentration")
    var phq9Concentration: Int? = null,

    @ColumnInfo(name = "phq9_moving_slowly")
    var phq9MovingSlowly: Int? = null,

    @ColumnInfo(name = "phq9_self_harm_thoughts")
    var phq9SelfHarmThoughts: Int? = null,

    @ColumnInfo(name = "phq9_total_score")
    var phq9TotalScore: Int? = null,

    // ── Substance Use Screening (enabled by Q2=Yes) ──────────────────

    @ColumnInfo(name = "substance_alcohol_use")
    var substanceAlcoholUse: Boolean? = null,

    @ColumnInfo(name = "substance_tobacco_use")
    var substanceTobaccoUse: Boolean? = null,

    @ColumnInfo(name = "substance_other_use")
    var substanceOtherUse: Boolean? = null,

    @ColumnInfo(name = "substance_other_specify")
    var substanceOtherSpecify: String? = null,

    @ColumnInfo(name = "substance_frequency")
    var substanceFrequency: String? = null,

    @ColumnInfo(name = "brief_intervention_given")
    var briefInterventionGiven: Boolean? = null,

    // ── Suicide Risk Screening (enabled by Q3=Yes, after PHQ-9) ──────

    @ColumnInfo(name = "suicide_current_thoughts")
    var suicideCurrentThoughts: Boolean? = null,

    @ColumnInfo(name = "suicide_plan")
    var suicidePlan: Boolean? = null,

    @ColumnInfo(name = "suicide_previous_attempt")
    var suicidePreviousAttempt: Boolean? = null,

    @ColumnInfo(name = "suicide_hopelessness")
    var suicideHopelessness: Boolean? = null,

    @ColumnInfo(name = "suicide_risk_level")
    var suicideRiskLevel: String? = null,

    // ── Dementia Screening (enabled by Q4=Yes) ───────────────────────

    @ColumnInfo(name = "dementia_progressive_memory_loss")
    var dementiaProgressiveMemoryLoss: Boolean? = null,

    @ColumnInfo(name = "dementia_forgetting_recent")
    var dementiaForgettingRecent: Boolean? = null,

    @ColumnInfo(name = "dementia_disorientation")
    var dementiaDisorientation: Boolean? = null,

    @ColumnInfo(name = "dementia_daily_activities")
    var dementiaDailyActivities: Boolean? = null,

    @ColumnInfo(name = "dementia_behavioural_changes")
    var dementiaBehaviouralChanges: Boolean? = null,

    // ── Epilepsy Screening (enabled by Q5=Yes) ───────────────────────

    @ColumnInfo(name = "epilepsy_recurrent_seizures")
    var epilepsyRecurrentSeizures: Boolean? = null,

    @ColumnInfo(name = "epilepsy_jerky_movements")
    var epilepsyJerkyMovements: Boolean? = null,

    @ColumnInfo(name = "epilepsy_tongue_bite")
    var epilepsyTongueBite: Boolean? = null,

    @ColumnInfo(name = "epilepsy_confusion_after")
    var epilepsyConfusionAfter: Boolean? = null,

    @ColumnInfo(name = "epilepsy_loc_duration")
    var epilepsyLocDuration: String? = null,

    // ── Referral & Follow-up ─────────────────────────────────────────

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