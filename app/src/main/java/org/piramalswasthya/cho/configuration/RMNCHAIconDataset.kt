package org.piramalswasthya.cho.configuration

import android.content.res.Resources
import android.os.Bundle
import androidx.navigation.NavDirections
import dagger.hilt.android.scopes.ActivityRetainedScoped
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.model.Icon
import javax.inject.Inject

/**
 * RMNCHA+ Icon Dataset
 * This class provides icons and navigation for RMNCHA+ service delivery modules:
 * - Maternal Health
 * - Child Care  
 * - Eligible Couple List
 * - And other RMNCHA+ services
 */
@ActivityRetainedScoped
class RMNCHAIconDataset @Inject constructor() {

    companion object {
        const val MODULE_TYPE_KEY = "module_type"
        const val SHOW_ALL_BENEFICIARIES_KEY = "show_all_beneficiaries"
        const val SHOW_EC_REGISTRATION_KEY = "show_ec_registration"
        const val SHOW_EC_TRACKING_KEY = "show_ec_tracking"
        const val MODULE_MATERNAL_HEALTH = "maternal_health"
        const val MODULE_CHILD_CARE = "child_care"
        const val MODULE_ELIGIBLE_COUPLE = "eligible_couple"
        private const val ACTION_SUBMODULE = 1001 // Custom action ID for sub-modules
        private const val ACTION_SHOW_LIST = 1002 // Custom action ID for showing lists
        private const val ACTION_EC_REGISTRATION = 1003 // Action for EC registration list
        private const val ACTION_EC_TRACKING = 1004 // Action for EC tracking list
    }

    // NavDirections for sub-module navigation
    private fun createNavAction(moduleType: String): NavDirections {
        return object : NavDirections {
            override val actionId: Int = ACTION_SUBMODULE
            override val arguments = Bundle().apply {
                putString(MODULE_TYPE_KEY, moduleType)
            }
        }
    }

    // NavDirections for showing all beneficiaries (switches to Home tab)
    private val showAllBeneficiariesAction = object : NavDirections {
        override val actionId: Int = ACTION_SHOW_LIST
        override val arguments = Bundle().apply {
            putBoolean(SHOW_ALL_BENEFICIARIES_KEY, true)
        }
    }

    // NavDirections for showing EC Registration list
    private val showECRegistrationAction = object : NavDirections {
        override val actionId: Int = ACTION_EC_REGISTRATION
        override val arguments = Bundle().apply {
            putBoolean(SHOW_EC_REGISTRATION_KEY, true)
        }
    }

    // NavDirections for showing EC Tracking list
    private val showECTrackingAction = object : NavDirections {
        override val actionId: Int = ACTION_EC_TRACKING
        override val arguments = Bundle().apply {
            putBoolean(SHOW_EC_TRACKING_KEY, true)
        }
    }

    private val placeholderNavAction = object : NavDirections {
        override val actionId: Int = 0
        override val arguments = Bundle()
    }

    fun getRMNCHAIconDataset(resources: Resources): List<Icon> {
        return listOf(
            Icon(
                R.drawable.ic_home,
                resources.getString(R.string.all_household),
                null,
                placeholderNavAction
            ),
            Icon(
                R.drawable.ic_person,
                resources.getString(R.string.all_beneficiaries),
                null,
                showAllBeneficiariesAction
            ),
            Icon(
                R.drawable.ic_person,
                resources.getString(R.string.eligible_couple_list),
                null,
                createNavAction(MODULE_ELIGIBLE_COUPLE)
            ),
            Icon(
                R.drawable.ic_new_born_baby,
                resources.getString(R.string.maternal_health),
                null,
                createNavAction(MODULE_MATERNAL_HEALTH)
            ),
            Icon(
                R.drawable.ic_child,
                resources.getString(R.string.child_care),
                null,
                createNavAction(MODULE_CHILD_CARE)
            ),
            Icon(
                R.drawable.ic_medical_briefcase, // Placeholder - replace with actual disease control icon
                resources.getString(R.string.disease_control),
                null,
                placeholderNavAction
            ),
            Icon(
                R.drawable.ic_medical_briefcase, // Placeholder - replace with actual communicable diseases icon
                resources.getString(R.string.communicable_diseases),
                null,
                placeholderNavAction
            ),
            Icon(
                R.drawable.ic_medical_briefcase, // Placeholder - replace with actual immunization icon
                resources.getString(R.string.routine_immunization),
                null,
                placeholderNavAction
            ),
            Icon(
                R.drawable.ic_exclamation_circle, // Placeholder - replace with actual HRP icon
                resources.getString(R.string.high_risk_assessment),
                null,
                placeholderNavAction
            ),
            Icon(
                R.drawable.ic_doctor, // Placeholder - replace with actual general OP icon
                resources.getString(R.string.general_opd_care_list),
                null,
                placeholderNavAction
            ),
            Icon(
                R.drawable.ic_close, // Placeholder - replace with actual death reports icon
                resources.getString(R.string.death_reports),
                null,
                placeholderNavAction
            ),
            Icon(
                R.drawable.ic_edit, // Placeholder - replace with actual village level forms icon
                resources.getString(R.string.village_level_forms),
                null,
                placeholderNavAction
            )
        ).apply {
            forEachIndexed { index, icon ->
                icon.colorPrimary = index % 2 == 0
            }
        }
    }

    /**
     * Maternal Health sub-modules dataset
     */
    fun getMaternalHealthDataset(resources: Resources): List<Icon> {
        return listOf(
            Icon(
                R.drawable.ic_register,
                resources.getString(R.string.pregnant_women_registration),
                null,
                placeholderNavAction
            ),
            Icon(
                R.drawable.ic_medical_briefcase,
                resources.getString(R.string.anc_visits),
                null,
                placeholderNavAction
            ),
            Icon(
                R.drawable.ic_new_born_baby,
                resources.getString(R.string.delivery_outcome),
                null,
                placeholderNavAction
            ),
            Icon(
                R.drawable.ic_new_born_baby,
                resources.getString(R.string.pnc_mother_list),
                null,
                placeholderNavAction
            ),
            Icon(
                R.drawable.ic_infant,
                resources.getString(R.string.infant_registration),
                null,
                placeholderNavAction
            ),
            Icon(
                R.drawable.ic_child,
                resources.getString(R.string.child_registration),
                null,
                placeholderNavAction
            ),
            Icon(
                R.drawable.ic_close,
                resources.getString(R.string.abortion_list),
                null,
                placeholderNavAction
            ),
            Icon(
                R.drawable.ic_exclamation_circle,
                resources.getString(R.string.e_pmsma_list),
                null,
                placeholderNavAction
            )
        ).apply {
            forEachIndexed { index, icon ->
                icon.colorPrimary = index % 2 == 0
            }
        }
    }

    /**
     * Child Care sub-modules dataset
     */
    fun getChildCareDataset(resources: Resources): List<Icon> {
        return listOf(
            Icon(
                R.drawable.ic_infant,
                resources.getString(R.string.infant_list),
                null,
                placeholderNavAction
            ),
            Icon(
                R.drawable.ic_child,
                resources.getString(R.string.child_list),
                null,
                placeholderNavAction
            ),
            Icon(
                R.drawable.ic_person,
                resources.getString(R.string.adolescent_list),
                null,
                placeholderNavAction
            ),
            Icon(
                R.drawable.ic_child,
                resources.getString(R.string.children_under_5_years),
                null,
                placeholderNavAction
            )
        ).apply {
            forEachIndexed { index, icon ->
                icon.colorPrimary = index % 2 == 0
            }
        }
    }

    /**
     * Eligible Couple sub-modules dataset
     */
    fun getEligibleCoupleDataset(resources: Resources): List<Icon> {
        return listOf(
            Icon(
                R.drawable.ic_register,
                resources.getString(R.string.eligible_couple_registration),
                null,
                showECRegistrationAction
            ),
            Icon(
                R.drawable.ic_person,
                resources.getString(R.string.eligible_couple_tracking),
                null,
                showECTrackingAction
            )
        ).apply {
            forEachIndexed { index, icon ->
                icon.colorPrimary = index % 2 == 0
            }
        }
    }
}
