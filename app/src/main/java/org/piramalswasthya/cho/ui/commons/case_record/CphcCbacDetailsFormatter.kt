package org.piramalswasthya.cho.ui.commons.case_record

import android.content.res.Resources
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.model.CbacCache

object CphcCbacDetailsFormatter {

    fun format(resources: Resources, cbac: CbacCache, isMale: Boolean): String {
        return buildString {
            appendLine(resources.getString(R.string.cphc_ncd_screening))
            appendField(resources, R.string.cbac_hint_age, arrayValue(resources, R.array.cbac_age, cbac.cbac_age_posi))
            appendField(resources, R.string.cbac_hint_smoke, arrayValue(resources, R.array.cbac_smoke, cbac.cbac_smoke_posi))
            appendField(resources, R.string.cbac_hint_alcohol, arrayValue(resources, R.array.cbac_alcohol, cbac.cbac_alcohol_posi))
            val waistArray = if (isMale) R.array.cbac_waist_mes_male else R.array.cbac_waist_mes_female
            appendField(resources, R.string.cbac_hint_waist, arrayValue(resources, waistArray, cbac.cbac_waist_posi))
            appendField(resources, R.string.cbac_hint_pa, arrayValue(resources, R.array.cbac_pa, cbac.cbac_pa_posi))
            appendField(resources, R.string.cbac_hint_fh, arrayValue(resources, R.array.cbac_fh, cbac.cbac_familyhistory_posi))
            appendField(resources, R.string.cphc_cbac_total_score, cbac.total_score.toString())

            appendLine()
            appendLine(resources.getString(R.string.cbac_early_detection))
            appendYesNoField(resources, R.string.cbac_fh_tb, cbac.cbac_sufferingtb_pos)
            appendYesNoField(resources, R.string.cbac_taking_tb_drug, cbac.cbac_antitbdrugs_pos)
            appendYesNoField(resources, R.string.cbac_histb, cbac.cbac_tbhistory_pos)
            appendYesNoField(resources, R.string.cbac_coughing, cbac.cbac_coughing_pos)
            appendYesNoField(resources, R.string.cbac_blsputum, cbac.cbac_bloodsputum_pos)
            appendYesNoField(resources, R.string.cbac_feverwks, cbac.cbac_fivermore_pos)
            appendYesNoField(resources, R.string.cbac_lsweight, cbac.cbac_loseofweight_pos)
            appendYesNoField(resources, R.string.cbac_ntswets, cbac.cbac_nightsweats_pos)
            appendYesNoField(resources, R.string.cbac_hifits, cbac.cbac_historyoffits_pos)
            appendYesNoField(resources, R.string.cbac_difmouth, cbac.cbac_difficultyinmouth_pos)
            appendYesNoField(resources, R.string.cbac_heald, cbac.cbac_uicers_pos)
            appendYesNoField(resources, R.string.cbac_voice, cbac.cbac_toneofvoice_pos)
            appendYesNoField(resources, R.string.cbac_recurrent_cloudy, cbac.cbac_cloudy_posi)
            appendYesNoField(resources, R.string.cbac_recurrent_diffculty_reading, cbac.cbac_diffreading_posi)
            appendYesNoField(resources, R.string.cbac_recurrent_pain_eyes, cbac.cbac_pain_ineyes_posi)
            appendYesNoField(resources, R.string.cbac_recurrent_redness_eyes, cbac.cbac_redness_ineyes_posi)
            appendYesNoField(resources, R.string.cbac_recurrent_diff_hearing, cbac.cbac_diff_inhearing_posi)
            appendYesNoField(resources, R.string.cbac_Any_Growth, cbac.cbac_growth_in_mouth_posi)
            appendYesNoField(resources, R.string.cbac_Any_white, cbac.cbac_white_or_red_patch_posi)
            appendYesNoField(resources, R.string.cbac_Pain_while_chewing, cbac.cbac_Pain_while_chewing_posi)
            appendYesNoField(resources, R.string.cbac_Any_hyper_pigmented, cbac.cbac_hyper_pigmented_patch_posi)
            appendYesNoField(resources, R.string.cbac_any_thickend_skin, cbac.cbac_any_thickend_skin_posi)
            appendYesNoField(resources, R.string.cbac_any_nodules_skin, cbac.cbac_nodules_on_skin_posi)
            appendYesNoField(resources, R.string.cbac_Recurrent_numbness, cbac.cbac_numbness_on_palm_posi)
            appendYesNoField(resources, R.string.cbac_Clawing_of_fingers, cbac.cbac_clawing_of_fingers_posi)
            appendYesNoField(resources, R.string.cbac_Tingling_or_Numbness, cbac.cbac_tingling_or_numbness_posi)
            appendYesNoField(resources, R.string.cbac_Inability_close_eyelid, cbac.cbac_inability_close_eyelid_posi)
            appendYesNoField(resources, R.string.cbac_diff_holding_objects, cbac.cbac_diff_holding_obj_posi)
            appendYesNoField(resources, R.string.cbac_Weekness_in_feet, cbac.cbac_weekness_in_feet_posi)

            appendLine()
            appendLine(resources.getString(R.string.cbac_early_detection_2))
            appendYesNoField(resources, R.string.cbac_lumpbrest, cbac.cbac_lumpinbreast_pos)
            appendYesNoField(resources, R.string.cbac_nipple, cbac.cbac_blooddischage_pos)
            appendYesNoField(resources, R.string.cbac_breast, cbac.cbac_changeinbreast_pos)
            appendYesNoField(resources, R.string.cbac_blperiods, cbac.cbac_bleedingbtwnperiods_pos)
            appendYesNoField(resources, R.string.cbac_blmenopause, cbac.cbac_bleedingaftermenopause_pos)
            appendYesNoField(resources, R.string.cbac_blintercorse, cbac.cbac_bleedingafterintercourse_pos)
            appendYesNoField(resources, R.string.cbac_fouldis, cbac.cbac_foulveginaldischarge_pos)

            appendLine()
            appendLine(resources.getString(R.string.cbac_early_detection_3))
            appendYesNoField(resources, R.string.cbac_unsteady, cbac.cbac_feeling_unsteady_posi)
            appendYesNoField(resources, R.string.cbac_pd_rm, cbac.cbac_suffer_physical_disability_posi)
            appendYesNoField(resources, R.string.cbac_nhop, cbac.cbac_needing_help_posi)
            appendYesNoField(resources, R.string.cbac_forget_names, cbac.cbac_forgetting_names_posi)

            appendLine()
            appendLine(resources.getString(R.string.cbac_risk_Factors_COPD))
            appendField(
                resources,
                R.string.cbac_occupational_exposure,
                arrayValue(resources, R.array.cbac_type_occupational_exposure, cbac.cbac_occupational_exposure_posi),
            )

            appendLine()
            appendLine(resources.getString(R.string.cbac_PHQ2))
            appendField(
                resources,
                R.string.cbac_little_interest,
                arrayValue(resources, R.array.cbac_li, cbac.cbac_little_interest_posi),
            )
            appendField(
                resources,
                R.string.cbac_feeling_down,
                arrayValue(resources, R.array.cbac_fd, cbac.cbac_feeling_down_posi),
            )

            appendLine()
            appendField(resources, R.string.cphc_cbac_ncd_suspected, valueOrNoData(resources, cbac.ncd_suspected))
            appendField(resources, R.string.cphc_cbac_suspected_ncd, valueOrNoData(resources, cbac.suspected_ncd))
            appendField(resources, R.string.cphc_cbac_suspected_ncd_diseases, valueOrNoData(resources, cbac.suspected_ncd_diseases))
            appendField(resources, R.string.cphc_cbac_confirmed_ncd, valueOrNoData(resources, cbac.confirmed_ncd))
            appendField(resources, R.string.cphc_cbac_confirmed_ncd_diseases, valueOrNoData(resources, cbac.confirmed_ncd_diseases))
            appendField(resources, R.string.cphc_cbac_suspected_tb, valueOrNoData(resources, cbac.suspected_tb))
            appendField(resources, R.string.cphc_cbac_confirmed_tb, valueOrNoData(resources, cbac.confirmed_tb))
            appendField(resources, R.string.cphc_cbac_suspected_hrp, valueOrNoData(resources, cbac.suspected_hrp))
            appendField(resources, R.string.cphc_cbac_diagnosis_status, valueOrNoData(resources, cbac.diagnosis_status))
        }.trim()
    }

    private fun StringBuilder.appendField(resources: Resources, labelRes: Int, value: String) {
        append("- ").append(resources.getString(labelRes)).append(": ").append(value).appendLine()
    }

    private fun StringBuilder.appendYesNoField(resources: Resources, labelRes: Int, pos: Int) {
        appendField(resources, labelRes, yesNo(resources, pos))
    }

    private fun yesNo(resources: Resources, pos: Int): String {
        return when (pos) {
            1 -> resources.getString(R.string.cbac_yes)
            2 -> resources.getString(R.string.cbac_no)
            else -> resources.getString(R.string.no_data)
        }
    }

    private fun arrayValue(resources: Resources, arrayResId: Int, pos: Int): String {
        if (pos <= 0) return resources.getString(R.string.no_data)
        val array = resources.getStringArray(arrayResId)
        return array.getOrNull(pos - 1) ?: resources.getString(R.string.no_data)
    }

    private fun valueOrNoData(resources: Resources, value: String?): String {
        return value?.takeIf { it.isNotBlank() } ?: resources.getString(R.string.no_data)
    }
}
