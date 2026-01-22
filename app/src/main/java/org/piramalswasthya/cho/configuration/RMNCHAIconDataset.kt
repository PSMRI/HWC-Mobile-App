package org.piramalswasthya.cho.configuration

import android.content.res.Resources
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

    // Placeholder NavDirections - will be replaced with actual navigation when fragments are created
    private val placeholderNavAction = object : NavDirections {
        override val actionId: Int = 0
        override val arguments = android.os.Bundle()
    }

    fun getRMNCHAIconDataset(resources: Resources): List<Icon> {
        return listOf(
            Icon(
                R.drawable.ic_home, // Placeholder - replace with actual household icon
                resources.getString(R.string.all_household),
                null, // Count will be implemented when repository is ready
                placeholderNavAction
            ),
            Icon(
                R.drawable.ic_person, // Placeholder - replace with actual beneficiary icon
                resources.getString(R.string.all_beneficiaries),
                null,
                placeholderNavAction
            ),
            Icon(
                R.drawable.ic_person, // Placeholder - replace with actual eligible couple icon
                resources.getString(R.string.eligible_couple_list),
                null,
                placeholderNavAction
            ),
            Icon(
                R.drawable.ic_new_born_baby, // Placeholder - replace with actual maternal health icon
                resources.getString(R.string.maternal_health),
                null,
                placeholderNavAction
            ),
            Icon(
                R.drawable.ic_child, // Placeholder - replace with actual child care icon
                resources.getString(R.string.child_care),
                null,
                placeholderNavAction
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
}
