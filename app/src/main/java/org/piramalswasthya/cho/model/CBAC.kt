package org.piramalswasthya.cho.model

import android.content.res.Resources
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.repositories.CbacRepo
import java.text.SimpleDateFormat
import java.util.Locale

@Entity(
    tableName = "CBAC",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = arrayOf("patientID"/* "householdId"*/),
        childColumns = arrayOf("patId"/* "hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_cbac", value = ["patId"/* "hhId"*/])]
)
data class CbacCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val patId: String,

//    val hhId: Long,
    @ColumnInfo(index = true)
    val ashaId: Int,
//    var gender : Gender?,
    var fillDate: Long = 0,
    var cbac_age_posi: Int = 0,
    var cbac_smoke_posi: Int = 0,
    var cbac_alcohol_posi: Int = 0,
    var cbac_waist_posi: Int = 0,
    var cbac_pa_posi: Int = 0,
    var cbac_familyhistory_posi: Int = 0,
    var total_score: Int = 0,
    var cbac_sufferingtb_pos: Int = 0,
    var cbac_antitbdrugs_pos: Int = 0,
    var cbac_tbhistory_pos: Int = 0,
    var cbac_sortnesofbirth_pos: Int = 0,
    var cbac_coughing_pos: Int = 0,
    var cbac_bloodsputum_pos: Int = 0,
    var cbac_fivermore_pos: Int = 0,
    var cbac_loseofweight_pos: Int = 0,
    var cbac_nightsweats_pos: Int = 0,
    var cbac_historyoffits_pos: Int = 0,
    var cbac_difficultyinmouth_pos: Int = 0,
    var cbac_uicers_pos: Int = 0,
    var cbac_toneofvoice_pos: Int = 0,
    var cbac_lumpinbreast_pos: Int = 0,
    var cbac_blooddischage_pos: Int = 0,
    var cbac_changeinbreast_pos: Int = 0,
    var cbac_bleedingbtwnperiods_pos: Int = 0,
    var cbac_bleedingaftermenopause_pos: Int = 0,
    var cbac_bleedingafterintercourse_pos: Int = 0,
    var cbac_foulveginaldischarge_pos: Int = 0,
    //Extras
    var cbac_cloudy_posi: Int = 0,
    var cbac_diffreading_posi: Int = 0,
    var cbac_pain_ineyes_posi: Int = 0,
    var cbac_diff_inhearing_posi: Int = 0,
    var cbac_redness_ineyes_posi: Int = 0,
    var cbac_growth_in_mouth_posi: Int = 0,
    var cbac_white_or_red_patch_posi: Int = 0,
    var cbac_Pain_while_chewing_posi: Int = 0,
    var cbac_hyper_pigmented_patch_posi: Int = 0,
    var cbac_any_thickend_skin_posi: Int = 0,
    var cbac_nodules_on_skin_posi: Int = 0,
    var cbac_numbness_on_palm_posi: Int = 0,
    var cbac_clawing_of_fingers_posi: Int = 0,
    var cbac_tingling_palm_posi: Int = 0,
    var cbac_tingling_or_numbness_posi: Int = 0,
    var cbac_inability_close_eyelid_posi: Int = 0,
    var cbac_diff_holding_obj_posi: Int = 0,
    var cbac_weekness_in_feet_posi: Int = 0,
    var cbac_fuel_used_posi: Int = 0,
    var cbac_occupational_exposure_posi: Int = 0,

    var cbac_feeling_unsteady_posi: Int = 0,
    var cbac_suffer_physical_disability_posi: Int = 0,
    var cbac_needing_help_posi: Int = 0,
    var cbac_forgetting_names_posi: Int = 0,

    var cbac_little_interest_posi: Int = 0,
    var cbac_feeling_down_posi: Int = 0,

    var cbac_little_interest_score: Int = 0,
    var cbac_feeling_down_score: Int = 0,

    var cbac_referpatient_mo: String? = null,
    var cbac_tracing_all_fm: String? = null,
    var cbac_sputemcollection: String? = null,
    var serverUpdatedStatus: Int = 0,
    var createdBy: String? = null,
    var createdDate: Long = 0L,
    var ProviderServiceMapID: Int = 0,
    var VanID: Int = 0,
    var Processed: String? = null,
    var Countyid: Int = 0,
    var stateid: Int = 0,
    var districtid: Int = 0,
    var districtname: String? = null,
    var villageid: Int = 0,
    var cbac_reg_id: Long = 0,
    var hrp_suspected: Boolean? = null,
    var suspected_hrp: String? = null,
    var confirmed_hrp: String? = null,
    var ncd_suspected: String? = null,
    var suspected_ncd: String? = null,
    var ncd_confirmed: Boolean? = null,
    var confirmed_ncd: String? = null,
    var suspected_tb: String? = null,
    var confirmed_tb: String? = null,
    var suspected_ncd_diseases: String? = null,
    var confirmed_ncd_diseases: String? = null,
    var diagnosis_status: String? = null,

    var syncState: SyncState


) {
   /* fun asPostModel(hhId: Long, benGender: Gender, resources: Resources, benId: Long): CbacPost {
        return  CbacPost(
            houseoldId = hhId,
            beneficiaryId = benId,
            ashaid = ashaId,
            filledDate = getDateTimeStringFromLong(fillDate) ?: "",
            cbac_age = resources.getStringArray(R.array.cbac_age)[cbac_age_posi - 1],
            cbac_age_posi = cbac_age_posi,
            cbac_smoke = resources.getStringArray(R.array.cbac_smoke)[cbac_smoke_posi - 1],
            cbac_smoke_posi = cbac_smoke_posi,
            cbac_alcohol = resources.getStringArray(R.array.cbac_alcohol)[cbac_alcohol_posi - 1],
            cbac_alcohol_posi = cbac_alcohol_posi,
            cbac_waist = if (benGender == Gender.MALE)
                resources.getStringArray(R.array.cbac_waist_mes_male)[cbac_waist_posi - 1]
            else
                resources.getStringArray(R.array.cbac_waist_mes_female)[cbac_waist_posi - 1],
            cbac_waist_posi = cbac_waist_posi,
            cbac_pa = resources.getStringArray(R.array.cbac_pa)[cbac_pa_posi - 1],
            cbac_pa_posi = cbac_pa_posi,
            cbac_familyhistory = resources.getStringArray(R.array.cbac_fh)[cbac_familyhistory_posi - 1],
            cbac_familyhistory_posi = cbac_familyhistory_posi,
            total_score = total_score,
            cbac_sufferingtb = when (cbac_sufferingtb_pos) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_fh_tb)
            },
            cbac_sufferingtb_pos = cbac_sufferingtb_pos,
            cbac_antitbdrugs = when (cbac_antitbdrugs_pos) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_taking_tb_drug)
            },
            cbac_antitbdrugs_pos = cbac_antitbdrugs_pos,
            cbac_tbhistory = when (cbac_tbhistory_pos) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_histb)
            },
            cbac_tbhistory_pos = cbac_tbhistory_pos,
            cbac_sortnesofbirth = when (cbac_sortnesofbirth_pos) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_breath)
            },
            cbac_sortnesofbirth_pos = cbac_sortnesofbirth_pos,
            cbac_coughing = when (cbac_coughing_pos) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_coughing)
            },
            cbac_coughing_pos = cbac_coughing_pos,
            cbac_bloodsputum = when (cbac_bloodsputum_pos) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_blsputum)
            },
            cbac_bloodsputum_pos = cbac_bloodsputum_pos,
            cbac_fivermore = when (cbac_fivermore_pos) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_feverwks)
            },
            cbac_fivermore_pos = cbac_fivermore_pos,
            cbac_loseofweight = when (cbac_loseofweight_pos) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_lsweight)
            },
            cbac_loseofweight_pos = cbac_loseofweight_pos,
            cbac_nightsweats = when (cbac_nightsweats_pos) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_ntswets)
            },
            cbac_nightsweats_pos = cbac_nightsweats_pos,
            cbac_historyoffits = when (cbac_historyoffits_pos) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_hifits)
            },
            cbac_historyoffits_pos = cbac_historyoffits_pos,
            cbac_difficultyinmouth = when (cbac_difficultyinmouth_pos) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_difmouth)
            },
            cbac_difficultyinmouth_pos = cbac_difficultyinmouth_pos,
            cbac_uicers = when (cbac_uicers_pos) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_recurrent_ulceration)
            },
            cbac_uicers_pos = cbac_uicers_pos,
            cbac_toneofvoice = when (cbac_toneofvoice_pos) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_voice)
            },
            cbac_toneofvoice_pos = cbac_toneofvoice_pos,
            cbac_lumpinbreast = when (cbac_lumpinbreast_pos) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_lumpbrest)
            },
            cbac_lumpinbreast_pos = cbac_lumpinbreast_pos,
            cbac_blooddischage = when (cbac_blooddischage_pos) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_nipple)
            },
            cbac_blooddischage_pos = cbac_blooddischage_pos,
            cbac_changeinbreast = when (cbac_changeinbreast_pos) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_breast)
            },
            cbac_changeinbreast_pos = cbac_changeinbreast_pos,
            cbac_bleedingbtwnperiods = when (cbac_bleedingbtwnperiods_pos) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_blperiods)
            },
            cbac_bleedingbtwnperiods_pos = cbac_bleedingbtwnperiods_pos,
            cbac_bleedingaftermenopause = when (cbac_bleedingaftermenopause_pos) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_blmenopause)
            },
            cbac_bleedingaftermenopause_pos = cbac_bleedingaftermenopause_pos,
            cbac_bleedingafterintercourse = when (cbac_bleedingafterintercourse_pos) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_blintercorse)
            },
            cbac_bleedingafterintercourse_pos = cbac_bleedingafterintercourse_pos,
            cbac_foulveginaldischarge = when (cbac_foulveginaldischarge_pos) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_fouldis)
            },
            cbac_foulveginaldischarge_pos = cbac_foulveginaldischarge_pos,
            cbac_referpatient_mo = cbac_referpatient_mo ?: "0",
            cbac_tracing_all_fm = cbac_tracing_all_fm ?: "0",
            cbac_sputemcollection = cbac_sputemcollection ?: "0",
            serverUpdatedStatus = serverUpdatedStatus,
            createdBy = createdBy!!,
            createdDate = getDateTimeStringFromLong(createdDate)!!,
            ProviderServiceMapID = ProviderServiceMapID,
            VanID = VanID,
            Processed = Processed!!,
            Countyid = Countyid,
            stateid = stateid,
            districtid = districtid,
            districtname = districtname,
            villageid = villageid,
            hrp_suspected = hrp_suspected ?: false,
            suspected_hrp = suspected_hrp ?: "N",
            ncd_suspected = ncd_suspected ?: "N",
            suspected_ncd = suspected_ncd ?: "N",
            suspected_tb = suspected_tb ?: "N",
            suspected_ncd_diseases = suspected_ncd_diseases ?: "N",
            cbac_reg_id = cbac_reg_id,
            ncd_suspected_cancer = false,
            ncd_suspected_hypertension = false,
            ncd_suspected_breastCancer = false,
            ncd_suspected_diabettis = false,
            ncd_confirmed = ncd_confirmed ?: false,
            confirmed_ncd = confirmed_ncd ?: "No",
            confirmed_hrp = null,
            confirmed_tb = null,
            suspected_confirmed_tb = false,
            confirmed_ncd_diseases = null,
            diagnosis_status = null,
            cbac_growth_in_mouth = when (cbac_growth_in_mouth_posi) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_Any_Growth)
            },
            cbac_growth_in_mouth_posi = cbac_growth_in_mouth_posi,
            cbac_white_or_red_patch = when (cbac_white_or_red_patch_posi) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_Any_white)
            },
            cbac_white_or_red_patch_posi = cbac_white_or_red_patch_posi,
            cbac_Pain_while_chewing = when (cbac_Pain_while_chewing_posi) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_Pain_while_chewing)
            },
            cbac_Pain_while_chewing_posi = cbac_Pain_while_chewing_posi,
            cbac_hyper_pigmented_patch = when (cbac_hyper_pigmented_patch_posi) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_Any_hyper_pigmented)
            },
            cbac_hyper_pigmented_patch_posi = cbac_hyper_pigmented_patch_posi,
            cbac_any_thickend_skin = when (cbac_any_thickend_skin_posi) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_any_thickend_skin)
            },
            cbac_any_thickend_skin_posi = cbac_any_thickend_skin_posi,
            cbac_nodules_on_skin = when (cbac_nodules_on_skin_posi) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_any_nodules_skin)
            },
            cbac_nodules_on_skin_posi = cbac_nodules_on_skin_posi,
            cbac_numbness_on_palm = when (cbac_numbness_on_palm_posi) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_Recurrent_numbness)
            },
            cbac_numbness_on_palm_posi = cbac_numbness_on_palm_posi,
            cbac_clawing_of_fingers = when (cbac_clawing_of_fingers_posi) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_Clawing_of_fingers)
            },
            cbac_clawing_of_fingers_posi = cbac_clawing_of_fingers_posi,
            cbac_tingling_or_numbness = when (cbac_tingling_or_numbness_posi) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_Tingling_or_Numbness)
            },
            cbac_tingling_or_numbness_posi = cbac_tingling_or_numbness_posi,
            cbac_cloudy = when (cbac_cloudy_posi) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_recurrent_cloudy)
            },
            cbac_cloudy_posi = cbac_cloudy_posi,
            cbac_diffreading = when (cbac_diffreading_posi) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_recurrent_diffculty_reading)
            },
            cbac_diffreading_posi = cbac_diffreading_posi,
            cbac_pain_ineyes = when (cbac_pain_ineyes_posi) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_recurrent_pain_eyes)
            },
            cbac_pain_ineyes_posi = cbac_pain_ineyes_posi,
            cbac_redness_ineyes = when (cbac_redness_ineyes_posi) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_recurrent_redness_eyes)
            },
            cbac_redness_ineyes_posi = cbac_redness_ineyes_posi,
            cbac_diff_inhearing = when (cbac_diff_inhearing_posi) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_recurrent_diff_hearing)
            },
            cbac_diff_inhearing_posi = cbac_diff_inhearing_posi,
            cbac_inability_close_eyelid = when (cbac_inability_close_eyelid_posi) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_Inability_close_eyelid)
            },
            cbac_rec_tingling = when (cbac_tingling_palm_posi) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_recurrent_tingling)
            },
            cbac_rec_tingling_posi = cbac_tingling_palm_posi,
            cbac_inability_close_eyelid_posi = cbac_inability_close_eyelid_posi,
            cbac_diff_holding_obj = when (cbac_diff_holding_obj_posi) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_diff_holding_objects)
            },
            cbac_diff_holding_obj_posi = cbac_diff_holding_obj_posi,
            cbac_weekness_in_feet = when (cbac_weekness_in_feet_posi) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_Weekness_in_feet)
            },
            cbac_weekness_in_feet_posi = cbac_weekness_in_feet_posi,
            cbac_feeling_unsteady = when (cbac_feeling_unsteady_posi) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_unsteady)
            },
            cbac_feeling_unsteady_posi = cbac_feeling_unsteady_posi,
            cbac_suffer_physical_disability = when (cbac_suffer_physical_disability_posi) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_pd_rm)
            },
            cbac_suffer_physical_disability_posi = cbac_suffer_physical_disability_posi,
            cbac_needing_help = when (cbac_needing_help_posi) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_nhop)
            },
            cbac_fuel_used = if (cbac_fuel_used_posi > 0) resources.getStringArray(R.array.cbac_type_Cooking_fuel)[cbac_fuel_used_posi - 1] else "",
            cbac_fuel_used_posi = cbac_fuel_used_posi,
            cbac_occupational_exposure = if (cbac_occupational_exposure_posi > 0) resources.getStringArray(
                R.array.cbac_type_occupational_exposure
            )[cbac_occupational_exposure_posi - 1] else "",
            cbac_occupational_exposure_posi = cbac_occupational_exposure_posi,
            cbac_little_interest = if (cbac_little_interest_posi > 0) resources.getStringArray(R.array.cbac_li)[cbac_little_interest_posi - 1] else "",
            cbac_little_interest_posi = cbac_little_interest_posi,
            cbac_feeling_down = if (cbac_feeling_down_posi > 0) resources.getStringArray(R.array.cbac_fd)[cbac_feeling_down_posi - 1] else "",
            cbac_feeling_down_posi = cbac_feeling_down_posi,
            cbac_little_interest_score = cbac_little_interest_score,
            cbac_feeling_down_score = cbac_feeling_down_score,
            cbac_needing_help_posi = cbac_needing_help_posi,
            cbac_forgetting_names = when (cbac_forgetting_names_posi) {
                1 -> "Yes"
                2 -> "No"
                else -> resources.getString(R.string.cbac_forget_names)
            },
            cbac_forgetting_names_posi = cbac_forgetting_names_posi,

            )
    }*/

    fun asPostModel(
        benGender: CbacRepo.Gender,
        resources: Resources,
        benId: Long
    ): CbacPostNew {
        return CbacPostNew(
            id = id,
            beneficiaryId = benId,
            cbacAge = resources.getStringArray(R.array.cbac_age)[cbac_age_posi - 1],
            cbacAgeScore = cbac_age_posi,
            cbacConsumeGutka = resources.getStringArray(R.array.cbac_smoke)[cbac_smoke_posi - 1],
            cbacConsumeGutkaScore = cbac_smoke_posi,
            cbacAlcohol = resources.getStringArray(R.array.cbac_alcohol)[cbac_alcohol_posi - 1],
            cbacAlcoholScore = cbac_alcohol_posi,

            cbacWaistMale = if (benGender == CbacRepo.Gender.MALE)
                resources.getStringArray(R.array.cbac_waist_mes_male)[cbac_waist_posi - 1]
            else null,
            cbacWaistMaleScore = if (benGender == CbacRepo.Gender.MALE) cbac_waist_posi else null,

            cbacWaistFemale = if (benGender == CbacRepo.Gender.FEMALE)
                resources.getStringArray(R.array.cbac_waist_mes_female)[cbac_waist_posi - 1]
            else null,
            cbacWaistFemaleScore = if (benGender == CbacRepo.Gender.FEMALE) cbac_waist_posi else null,

            cbacPhysicalActivity = resources.getStringArray(R.array.cbac_pa)[cbac_pa_posi - 1],
            cbacPhysicalActivityScore = cbac_pa_posi,
            cbacFamilyHistoryBpdiabetes = resources.getStringArray(R.array.cbac_fh)[cbac_familyhistory_posi - 1],
            cbacFamilyHistoryBpdiabetesScore = cbac_familyhistory_posi,

            cbacShortnessBreath = yesNoFromPos(cbac_sortnesofbirth_pos, resources.getString(R.string.cbac_breath)),
            cbacCough2weeks = yesNoFromPos(cbac_coughing_pos, resources.getString(R.string.cbac_coughing)),
            cbacBloodsputum = yesNoFromPos(cbac_bloodsputum_pos, resources.getString(R.string.cbac_blsputum)),
            cbacFever2weeks = yesNoFromPos(cbac_fivermore_pos, resources.getString(R.string.cbac_feverwks)),
            cbacWeightLoss = yesNoFromPos(cbac_loseofweight_pos, resources.getString(R.string.cbac_lsweight)),
            cbacNightSweats = yesNoFromPos(cbac_nightsweats_pos, resources.getString(R.string.cbac_ntswets)),
            cbacAntiTBDrugs = yesNoFromPos(cbac_antitbdrugs_pos, resources.getString(R.string.cbac_taking_tb_drug)),
            cbacTb = yesNoFromPos(cbac_sufferingtb_pos, resources.getString(R.string.cbac_fh_tb)),
            cbacTBHistory = yesNoFromPos(cbac_tbhistory_pos, resources.getString(R.string.cbac_histb)),

            cbacUlceration = yesNoFromPos(cbac_uicers_pos, resources.getString(R.string.cbac_recurrent_ulceration)),
            cbacRecurrentTingling = yesNoFromPos(cbac_tingling_palm_posi, resources.getString(R.string.cbac_recurrent_tingling)),
            cbacFitsHistory = yesNoFromPos(cbac_historyoffits_pos, resources.getString(R.string.cbac_hifits)),
            cbacMouthopeningDifficulty = yesNoFromPos(cbac_difficultyinmouth_pos, resources.getString(R.string.cbac_difmouth)),
            cbacMouthUlcers = yesNoFromPos(cbac_uicers_pos, resources.getString(R.string.cbac_recurrent_ulceration)),
            cbacMouthUlcersGrowth = yesNoFromPos(cbac_growth_in_mouth_posi, resources.getString(R.string.cbac_Any_Growth)),
            cbacMouthredpatch = yesNoFromPos(cbac_white_or_red_patch_posi, resources.getString(R.string.cbac_Any_white)),
            cbacPainchewing = yesNoFromPos(cbac_Pain_while_chewing_posi, resources.getString(R.string.cbac_Pain_while_chewing)),
            cbacTonechange = yesNoFromPos(cbac_toneofvoice_pos, resources.getString(R.string.cbac_voice)),

            cbacHypopigmentedpatches = yesNoFromPos(cbac_hyper_pigmented_patch_posi, resources.getString(R.string.cbac_Any_hyper_pigmented)),
            cbacThickenedskin = yesNoFromPos(cbac_any_thickend_skin_posi, resources.getString(R.string.cbac_any_thickend_skin)),
            cbacNodulesonskin = yesNoFromPos(cbac_nodules_on_skin_posi, resources.getString(R.string.cbac_any_nodules_skin)),
            cbacRecurrentNumbness = yesNoFromPos(cbac_numbness_on_palm_posi, resources.getString(R.string.cbac_Recurrent_numbness)),

            cbacBlurredVision = yesNoFromPos(cbac_cloudy_posi, resources.getString(R.string.cbac_recurrent_cloudy)),
            cbacDifficultyreading = yesNoFromPos(cbac_diffreading_posi, resources.getString(R.string.cbac_recurrent_diffculty_reading)),
            cbacPainineyes = yesNoFromPosNullable(cbac_pain_ineyes_posi, resources.getString(R.string.cbac_recurrent_pain_eyes)),
            cbacRednessPain = yesNoFromPos(cbac_redness_ineyes_posi, resources.getString(R.string.cbac_recurrent_redness_eyes)),
            cbacDifficultyHearing = yesNoFromPos(cbac_diff_inhearing_posi, resources.getString(R.string.cbac_recurrent_diff_hearing)),

            cbacClawingfingers = yesNoFromPos(cbac_clawing_of_fingers_posi, resources.getString(R.string.cbac_Clawing_of_fingers)),
            cbacHandTingling = yesNoFromPos(cbac_tingling_or_numbness_posi, resources.getString(R.string.cbac_Tingling_or_Numbness)),
            cbacInabilityCloseeyelid = yesNoFromPos(cbac_inability_close_eyelid_posi, resources.getString(R.string.cbac_Inability_close_eyelid)),
            cbacDifficultHoldingObjects = yesNoFromPos(cbac_diff_holding_obj_posi, resources.getString(R.string.cbac_diff_holding_objects)),
            cbacFeetweakness = yesNoFromPos(cbac_weekness_in_feet_posi, resources.getString(R.string.cbac_Weekness_in_feet)),

            cbacLumpBreast = yesNoFromPosNullable(cbac_lumpinbreast_pos, resources.getString(R.string.cbac_lumpbrest)),
            cbacBloodnippleDischarge = yesNoFromPosNullable(cbac_blooddischage_pos, resources.getString(R.string.cbac_nipple)),
            cbacBreastsizechange = yesNoFromPosNullable(cbac_changeinbreast_pos, resources.getString(R.string.cbac_breast)),
            cbacBleedingPeriods = yesNoFromPosNullable(cbac_bleedingbtwnperiods_pos, resources.getString(R.string.cbac_blperiods)),
            cbacBleedingMenopause = yesNoFromPosNullable(cbac_bleedingaftermenopause_pos, resources.getString(R.string.cbac_blmenopause)),
            cbacBleedingIntercourse = yesNoFromPosNullable(cbac_bleedingafterintercourse_pos, resources.getString(R.string.cbac_blintercorse)),
            cbacVaginalDischarge = yesNoFromPosNullable(cbac_foulveginaldischarge_pos, resources.getString(R.string.cbac_fouldis)),

            cbacFeelingUnsteady = yesNoFromPosNullable(cbac_feeling_unsteady_posi, resources.getString(R.string.cbac_unsteady)),
            cbacPhysicalDisabilitySuffering = yesNoFromPosNullable(cbac_suffer_physical_disability_posi, resources.getString(R.string.cbac_pd_rm)),
            cbacNeedhelpEverydayActivities = yesNoFromPosNullable(cbac_needing_help_posi, resources.getString(R.string.cbac_nhop)),
            cbacForgetnearones = yesNoFromPosNullable(cbac_forgetting_names_posi, resources.getString(R.string.cbac_forget_names)),

            totalScore = total_score
        )
    }

    // Helper methods
    private fun yesNoFromPos(pos: Int, default: String): String {
        return when (pos) {
            1 -> "Yes"
            2 -> "No"
            else -> default
        }
    }

    private fun yesNoFromPosNullable(pos: Int, default: String): String? {
        return when (pos) {
            1 -> "Yes"
            2 -> "No"
            else -> null
        }
    }

    fun asDomainModel(): CbacDomain {
        return CbacDomain(
            cbacId = this.id,
            date = "Filled on ${getCbacCreatedDateFromLong(this.fillDate)}",
            syncState = this.syncState
        )
    }

    companion object {
        private val dateFormat = SimpleDateFormat("EEE, MMM dd yyyy", Locale.getDefault())

        private fun getCbacCreatedDateFromLong(long: Long): String {
            return dateFormat.format(long)
        }
    }
}

fun getDateTimeStringFromLong(dateLong: Long?): String? {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    dateLong?.let {
        val dateString = dateFormat.format(dateLong)
        val timeString = timeFormat.format(dateLong)
        return "${dateString}T${timeString}.000Z"
    } ?: run {
        return null
    }

}

data class CbacCachePush(
    @Embedded
    val cbac: CbacCache,
    val hhId: Long,
    val benGender: Gender,
)

data class CbacDomain(
    val cbacId: Int,
    val date: String,
    val syncState: SyncState
)

@JsonClass(generateAdapter = true)
data class CbacPost(
    val id: Int = 1,
    val houseoldId: Long,
    val beneficiaryId: Long,
    val ashaid: Int,
    val filledDate: String,
    @Json(name = "cbacAge")
    val cbac_age: String,
    @Json(name = "cbacAgePosi")
    val cbac_age_posi: Int,
    @Json(name = "cbacSmoke")
    val cbac_smoke: String,
    @Json(name = "cbacSmokePosi")
    val cbac_smoke_posi: Int,
    @Json(name = "cbacAlcohol")
    val cbac_alcohol: String,
    @Json(name = "cbacAlcoholPosi")
    val cbac_alcohol_posi: Int,
    @Json(name = "cbacWaist")
    val cbac_waist: String,
    @Json(name = "cbacWaistPosi")
    val cbac_waist_posi: Int,
    @Json(name = "cbacPa")
    val cbac_pa: String,
    @Json(name = "cbacPaPosi")
    val cbac_pa_posi: Int,
    @Json(name = "cbacFamilyhistory")
    val cbac_familyhistory: String,
    @Json(name = "cbacFamilyhistoryPosi")
    val cbac_familyhistory_posi: Int,
    @Json(name = "totalScore")
    val total_score: Int,
    @Json(name = "cbacSufferingtb")
    val cbac_sufferingtb: String,
    @Json(name = "cbacSufferingtbPos")
    val cbac_sufferingtb_pos: Int,
    @Json(name = "cbacAntitbdrugs")
    val cbac_antitbdrugs: String,
    @Json(name = "cbacAntitbdrugsPos")
    val cbac_antitbdrugs_pos: Int,
    @Json(name = "cbacTbhistory")
    val cbac_tbhistory: String,
    @Json(name = "cbacTbhistoryPos")
    val cbac_tbhistory_pos: Int,
    @Json(name = "cbacSortnesofbirth")
    val cbac_sortnesofbirth: String,
    @Json(name = "cbacSortnesofbirthPos")
    val cbac_sortnesofbirth_pos: Int,
    @Json(name = "cbacCoughing")
    val cbac_coughing: String,
    @Json(name = "cbacCoughingPos")
    val cbac_coughing_pos: Int,
    @Json(name = "cbacBloodsputum")
    val cbac_bloodsputum: String,
    @Json(name = "cbacBloodsputumPos")
    val cbac_bloodsputum_pos: Int,
    @Json(name = "cbacFivermore")
    val cbac_fivermore: String,
    @Json(name = "cbacFivermorePos")
    val cbac_fivermore_pos: Int,
    @Json(name = "cbacLoseofweight")
    val cbac_loseofweight: String,
    @Json(name = "cbacLoseofweightPos")
    val cbac_loseofweight_pos: Int,
    @Json(name = "cbacNightsweats")
    val cbac_nightsweats: String,
    @Json(name = "cbacNightsweatsPos")
    val cbac_nightsweats_pos: Int,
    @Json(name = "cbacHistoryoffits")
    val cbac_historyoffits: String,
    @Json(name = "cbacHistoryoffitsPos")
    val cbac_historyoffits_pos: Int,
    @Json(name = "cbacDifficultyinmouth")
    val cbac_difficultyinmouth: String,
    @Json(name = "cbacDifficultyinmouthPos")
    val cbac_difficultyinmouth_pos: Int,
    @Json(name = "cbacUicers")
    val cbac_uicers: String,
    @Json(name = "cbacUicersPos")
    val cbac_uicers_pos: Int,
    @Json(name = "cbacToneofvoice")
    val cbac_toneofvoice: String,
    @Json(name = "cbacToneofvoicePos")
    val cbac_toneofvoice_pos: Int,
    @Json(name = "cbacLumpinbreast")
    val cbac_lumpinbreast: String,
    @Json(name = "cbacLumpinbreastPos")
    val cbac_lumpinbreast_pos: Int,
    @Json(name = "cbacBlooddischage")
    val cbac_blooddischage: String,
    @Json(name = "cbacBlooddischagePos")
    val cbac_blooddischage_pos: Int,
    @Json(name = "cbacChangeinbreast")
    val cbac_changeinbreast: String,
    @Json(name = "cbacChangeinbreastPos")
    val cbac_changeinbreast_pos: Int,
    @Json(name = "cbacBleedingbtwnperiods")
    val cbac_bleedingbtwnperiods: String,
    @Json(name = "cbacBleedingbtwnperiodsPos")
    val cbac_bleedingbtwnperiods_pos: Int,
    @Json(name = "cbacBleedingaftermenopause")
    val cbac_bleedingaftermenopause: String,
    @Json(name = "cbacBleedingaftermenopausePos")
    val cbac_bleedingaftermenopause_pos: Int,
    @Json(name = "cbacBleedingafterintercourse")
    val cbac_bleedingafterintercourse: String,
    @Json(name = "cbacBleedingafterintercoursePos")
    val cbac_bleedingafterintercourse_pos: Int,
    @Json(name = "cbacFoulveginaldischarge")
    val cbac_foulveginaldischarge: String,
    @Json(name = "cbacFoulveginaldischargePos")
    val cbac_foulveginaldischarge_pos: Int,
    @Json(name = "cbacReferpatientMo")
    val cbac_referpatient_mo: String,
    @Json(name = "cbacTracingAllFm")
    val cbac_tracing_all_fm: String,
    @Json(name = "cbacSputemcollection")
    val cbac_sputemcollection: String,

    val serverUpdatedStatus: Int,

    val createdBy: String,

    val createdDate: String,
    @Json(name = "providerServiceMapId")
    val ProviderServiceMapID: Int,
    @Json(name = "vanId")
    val VanID: Int,
    @Json(name = "processed")
    val Processed: String,
    @Json(name = "countryid")
    val Countyid: Int,
    val stateid: Int,
    val districtid: Int,
    val districtname: String?,
    val villageid: Int,

    val hrp_suspected: Boolean,
    @Json(name = "suspectedHrp")
    val suspected_hrp: String,
    val ncd_suspected: String,
    @Json(name = "suspectedNcd")
    val suspected_ncd: String,
    @Json(name = "suspectedTb")
    val suspected_tb: String,
    @Json(name = "suspectedNcdDiseases")
    val suspected_ncd_diseases: String,
    @Json(name = "BeneficiaryRegId")
    val cbac_reg_id: Long,

    val ncd_suspected_cancer: Boolean,

    val ncd_suspected_hypertension: Boolean,

    val ncd_suspected_breastCancer: Boolean,

    val ncd_suspected_diabettis: Boolean,

    val ncd_confirmed: Boolean,
    @Json(name = "confirmedNcd")
    val confirmed_ncd: String,
    @Json(name = "confirmedHrp")
    val confirmed_hrp: String?,
    @Json(name = "confirmedTb")
    val confirmed_tb: String?,
    val suspected_confirmed_tb: Boolean,
    @Json(name = "confirmedNcdDiseases")
    val confirmed_ncd_diseases: String?,
    @Json(name = "diagnosisStatus")
    val diagnosis_status: String?,
    @Json(name = "cbacGrowthInMouth")
    val cbac_growth_in_mouth: String,
    @Json(name = "cbacGrowthInMouthPosi")
    val cbac_growth_in_mouth_posi: Int,
    @Json(name = "cbacWhiteOrRedPatch")
    val cbac_white_or_red_patch: String,
    @Json(name = "cbacWhiteOrRedPatchPosi")
    val cbac_white_or_red_patch_posi: Int,
    @Json(name = "cbacPainWhileChewing")
    val cbac_Pain_while_chewing: String,
    @Json(name = "cbacPainWhileChewingPosi")
    val cbac_Pain_while_chewing_posi: Int,
    @Json(name = "cbacHyperPigmentedPatch")
    val cbac_hyper_pigmented_patch: String,
    @Json(name = "cbacHyperPigmentedPatchPosi")
    val cbac_hyper_pigmented_patch_posi: Int,
    @Json(name = "cbacAnyThickendSkin")
    val cbac_any_thickend_skin: String,
    @Json(name = "cbacAnyThickendSkinPosi")
    val cbac_any_thickend_skin_posi: Int,
    @Json(name = "cbacNodulesOnSkin")
    val cbac_nodules_on_skin: String,
    @Json(name = "cbacNodulesOnSkinPosi")
    val cbac_nodules_on_skin_posi: Int,
    @Json(name = "cbacNumbnessOnPalm")
    val cbac_numbness_on_palm: String,
    @Json(name = "cbacNumbnessOnPalmPosi")
    val cbac_numbness_on_palm_posi: Int,
    @Json(name = "cbacTinglingPalm")
    val cbac_rec_tingling: String,
    @Json(name = "cbacTinglingPalmPosi")
    val cbac_rec_tingling_posi: Int,

    @Json(name = "cbacClawingOfFingers")
    val cbac_clawing_of_fingers: String,
    @Json(name = "cbacClawingOfFingersPosi")
    val cbac_clawing_of_fingers_posi: Int,
    @Json(name = "cbacTinglingOrNumbness")
    val cbac_tingling_or_numbness: String,
    @Json(name = "cbacTinglingOrNumbnessPosi")
    val cbac_tingling_or_numbness_posi: Int,
    @Json(name = "cbacCloudy")
    val cbac_cloudy: String,
    @Json(name = "cbacCloudyPosi")
    val cbac_cloudy_posi: Int,
    @Json(name = "cbacDiffreading")
    val cbac_diffreading: String,
    @Json(name = "cbacDiffreadingPosi")
    val cbac_diffreading_posi: Int,
    @Json(name = "cbacPainIneyes")
    val cbac_pain_ineyes: String,
    @Json(name = "cbacPainIneyesPosi")
    val cbac_pain_ineyes_posi: Int,
    @Json(name = "cbacRednessIneyes")
    val cbac_redness_ineyes: String,
    @Json(name = "cbacRednessIneyesPosi")
    val cbac_redness_ineyes_posi: Int,
    @Json(name = "cbacDiffInhearing")
    val cbac_diff_inhearing: String,
    @Json(name = "cbacDiffInhearingPosi")
    val cbac_diff_inhearing_posi: Int,
    @Json(name = "cbacInabilityCloseEyelid")
    val cbac_inability_close_eyelid: String,
    @Json(name = "cbacInabilityCloseEyelidPosi")
    val cbac_inability_close_eyelid_posi: Int,
    @Json(name = "cbacDiffHoldingObj")
    val cbac_diff_holding_obj: String,
    @Json(name = "cbacDiffHoldingObjPosi")
    val cbac_diff_holding_obj_posi: Int,
    @Json(name = "cbacWeeknessInFeet")
    val cbac_weekness_in_feet: String,
    @Json(name = "cbacWeeknessInFeetPosi")
    val cbac_weekness_in_feet_posi: Int,
    @Json(name = "cbacFeelingUnsteady")
    val cbac_feeling_unsteady: String,
    @Json(name = "cbacFeelingUnsteadyPosi")
    val cbac_feeling_unsteady_posi: Int,
    @Json(name = "cbacSufferPhysicalDisability")
    val cbac_suffer_physical_disability: String,
    @Json(name = "cbacSufferPhysicalDisabilityPosi")
    val cbac_suffer_physical_disability_posi: Int,
    @Json(name = "cbacNeedingHelp")
    val cbac_needing_help: String,
    @Json(name = "cbacNeedingHelpPosi")
    val cbac_needing_help_posi: Int,
    @Json(name = "cbacForgettingNames")
    val cbac_forgetting_names: String,
    @Json(name = "cbacForgettingNamesPosi")
    val cbac_forgetting_names_posi: Int,
    @Json(name = "cbacFuelUsed")
    val cbac_fuel_used: String,
    @Json(name = "cbacFuelUsedPosi")
    val cbac_fuel_used_posi: Int,
    @Json(name = "cbacOccupationalExposure")
    val cbac_occupational_exposure: String,
    @Json(name = "cbacOccupationalExposurePosi")
    val cbac_occupational_exposure_posi: Int,
    @Json(name = "cbacLittleInterest")
    val cbac_little_interest: String,
    @Json(name = "cbacLittleInterestPosi")
    val cbac_little_interest_posi: Int,
    @Json(name = "cbacFeelingDown")
    val cbac_feeling_down: String,
    @Json(name = "cbacFeelingDownPosi")
    val cbac_feeling_down_posi: Int,
    @Json(name = "cbacLittleInterestScore")
    val cbac_little_interest_score: Int,
    @Json(name = "cbacFeelingDownScore")
    val cbac_feeling_down_score: Int,


    )

//data class BenWithCbacCache(
////    @ColumnInfo(name = "benId")
//    @Embedded
//    val ben: BenBasicCache,
//    @Relation(
//        parentColumn = "benId", entityColumn = "benId", entity = CbacCache::class
//    )
//    val savedCbacRecords: List<CbacCache>
//) {
//
//    fun asDomainModel(): BenWithCbacDomain {
//        return BenWithCbacDomain(
//            ben.asBasicDomainModel(), savedCbacRecords
//        )
//    }
//}
//
//data class BenWithCbacDomain(
////    @ColumnInfo(name = "benId")
//    val ben: BenBasicDomain,
//    val savedCbacRecords: List<CbacCache>,
//    val allSynced: SyncState? = if (savedCbacRecords.isEmpty()) null else
//        if (savedCbacRecords.map { it.syncState }
//                .all { it == SyncState.SYNCED }) SyncState.SYNCED else SyncState.UNSYNCED
//)
@JsonClass(generateAdapter = true)
data class CbacPostNew(
    val id: Int,
    val beneficiaryId: Long,
    val cbacAge: String,
    val cbacAgeScore: Int,
    val cbacConsumeGutka: String,
    val cbacConsumeGutkaScore: Int,
    val cbacAlcohol: String,
    val cbacAlcoholScore: Int,
    val cbacWaistMale: String?,
    val cbacWaistMaleScore: Int?,
    val cbacWaistFemale: String?,
    val cbacWaistFemaleScore: Int?,
    val cbacPhysicalActivity: String,
    val cbacPhysicalActivityScore: Int,
    val cbacFamilyHistoryBpdiabetes: String,
    val cbacFamilyHistoryBpdiabetesScore: Int,
    val cbacShortnessBreath: String,
    val cbacCough2weeks: String,
    val cbacBloodsputum: String,
    val cbacFever2weeks: String,
    val cbacWeightLoss: String,
    val cbacNightSweats: String,
    val cbacAntiTBDrugs: String,
    val cbacTb: String,
    val cbacTBHistory: String,
    val cbacUlceration: String,
    val cbacRecurrentTingling: String,
    val cbacFitsHistory: String,
    val cbacMouthopeningDifficulty: String,
    val cbacMouthUlcers: String,
    val cbacMouthUlcersGrowth: String,
    val cbacMouthredpatch: String,
    val cbacPainchewing: String,
    val cbacTonechange: String,
    val cbacHypopigmentedpatches: String,
    val cbacThickenedskin: String,
    val cbacNodulesonskin: String,
    val cbacRecurrentNumbness: String,
    val cbacBlurredVision: String,
    val cbacDifficultyreading: String,
    val cbacPainineyes: String?,
    val cbacRednessPain: String,
    val cbacDifficultyHearing: String,
    val cbacClawingfingers: String,
    val cbacHandTingling: String,
    val cbacInabilityCloseeyelid: String,
    val cbacDifficultHoldingObjects: String,
    val cbacFeetweakness: String,
    val cbacLumpBreast: String?,
    val cbacBloodnippleDischarge: String?,
    val cbacBreastsizechange: String?,
    val cbacBleedingPeriods: String?,
    val cbacBleedingMenopause: String?,
    val cbacBleedingIntercourse: String?,
    val cbacVaginalDischarge: String?,
    val cbacFeelingUnsteady: String?,
    val cbacPhysicalDisabilitySuffering: String?,
    val cbacNeedhelpEverydayActivities: String?,
    val cbacForgetnearones: String?,
    val totalScore: Int
)

data class CbacRequest(
    val visitDetails: CbacVisitDetails,
    val cbac: CbacPostNew,
    val benFlowID: Long?,
    val beneficiaryID: Long,
    val sessionID: Int?,
    val parkingPlaceID: Int?,
    val createdBy: String,
    val vanID: Int?,
    val beneficiaryRegID: Long,
    val benVisitID: Long?,
    val providerServiceMapID: Int?
)

data class CbacVisitDetails(
    val beneficiaryRegID: Long,
    val providerServiceMapID: Int,
    val visitNo: Int? = null,
    val visitReason: String,
    val visitCategory: String,
    val IdrsOrCbac: String,
    val createdBy: String,
    val vanID: Int,
    val parkingPlaceID: Int
)
