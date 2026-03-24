package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import androidx.room.Index
import org.piramalswasthya.cho.configuration.FormDataModel

@Entity(
    tableName = "MENTAL_HEALTH_SCREENING",
    indices = [
        Index(value = ["patient_id"], name = "index_mhs_patient_id"),
        Index(value = ["patient_id", "ben_visit_no"], name = "index_mhs_patient_visit")
    ]
)
@JsonClass(generateAdapter = true)
data class MentalHealthScreeningCache(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "screening_id")
    val screeningId: Long = 0L,

    @ColumnInfo(name = "patient_id")
    val patientID: String,

    @ColumnInfo(name = "ben_visit_no")
    val benVisitNo: Int?,


    @ColumnInfo(name = "emotional_behavioural_concerns")
    var emotionalBehaviouralConcerns: Boolean? = null,


    @ColumnInfo(name = "substance_use_concerns")
    var substanceUseConcerns: Boolean? = null,


    @ColumnInfo(name = "self_harm_suicide_thoughts")
    var selfHarmSuicideThoughts: Boolean? = null,


    @ColumnInfo(name = "memory_loss_confusion")
    var memoryLossConfusion: Boolean? = null,


    @ColumnInfo(name = "seizures_fits_loc")
    var seizuresFitsLoc: Boolean? = null,


    @ColumnInfo(name = "is_postpartum")
    var isPostpartum: Boolean? = null,

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

    @ColumnInfo(name = "phq9_depression_severity")
    var phq9DepressionSeverity: String? = null,

    @ColumnInfo(name = "phq9_system_action")
    var phq9SystemAction: String? = null,

    @ColumnInfo(name = "substance_current_tobacco_use")
    var substanceCurrentTobaccoUse: Boolean? = null,

    @ColumnInfo(name = "substance_tobacco_type")
    var substanceTobaccoType: String? = null,

    @ColumnInfo(name = "substance_tobacco_frequency")
    var substanceTobaccoFrequency: String? = null,

    @ColumnInfo(name = "substance_tobacco_outcome")
    var substanceTobaccoOutcome: String? = null,

    @ColumnInfo(name = "substance_system_action")
    var substanceSystemAction: String? = null,

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


    @ColumnInfo(name = "suicide_current_thoughts")
    var suicideCurrentThoughts: Boolean? = null,

    @ColumnInfo(name = "suicide_plan")
    var suicidePlan: Boolean? = null,

    @ColumnInfo(name = "suicide_previous_attempt")
    var suicidePreviousAttempt: Boolean? = null,

    @ColumnInfo(name = "suicide_hopelessness")
    var suicideHopelessness: Boolean? = null,

    @ColumnInfo(name = "suicide_immediate_assess")
    var suicideImmediateAssess: Boolean? = null,

    @ColumnInfo(name = "suicide_risk_level")
    var suicideRiskLevel: String? = null,


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

    @ColumnInfo(name = "substance_alcohol_loss")
    var substance_alcohol_loss: Boolean? = null,

    @ColumnInfo(name = "substance_alcohol_impact")
    var substanceAlcoholImpact: Boolean? = null,

    @ColumnInfo(name = "substance_alcohol_withdrawal")
    var substanceAlcoholWithdrawal: Boolean? = null,

    @ColumnInfo(name = "substance_alcohol_problematic")
    var substanceAlcoholProblematic: Boolean? = null,

    @ColumnInfo(name = "substance_alcohol_classification")
    var substanceAlcoholClassification: String? = null,

    @ColumnInfo(name = "substance_alcohol_system_action")
    var substanceAlcoholSystemAction: String? = null,

    @ColumnInfo(name = "substance_alcohol_frequency")
    var substance_alcohol_frequency: String? = null,

    @ColumnInfo(name = "edRecurrentEpisodeloss")
    var edRecurrentEpisodeloss: Boolean? = null,
    @ColumnInfo(name = "ed_recurrent_jerky_movements")
    var edRecurrentJerkyMovements: Boolean? = null,

    @ColumnInfo(name = "ed_progressive_memory_loss")
    var edProgressiveMemoryLoss: Boolean? = null,

    @ColumnInfo(name = "ed_confusion_disorientation")
    var edConfusionDisorientation: Boolean? = null,

    @ColumnInfo(name = "ed_functional_decline")
    var edFunctionalDecline: Boolean? = null,

    @ColumnInfo(name = "ed_screening_outcome")
    var edScreeningOutcome: String? = null,

    @ColumnInfo(name = "ed_referral_required")
    var edReferralRequired: String? = null,

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