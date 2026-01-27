package org.piramalswasthya.cho.ui.home.rmncha

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.IconGridAdapter
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.MODULE_CHILD_CARE
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.MODULE_ELIGIBLE_COUPLE
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.MODULE_MATERNAL_HEALTH
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.MODULE_TYPE_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_EC_REGISTRATION_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_EC_TRACKING_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_PWR_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_ANC_VISITS_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_DELIVERY_OUTCOME_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_PNC_MOTHER_LIST_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_INFANT_REG_LIST_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_CHILD_REG_LIST_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_ABORTION_LIST_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_PMSMA_LIST_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_INFANT_LIST_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_CHILD_LIST_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_ADOLESCENT_LIST_KEY
import org.piramalswasthya.cho.databinding.RvIconGridBinding
import timber.log.Timber
import javax.inject.Inject

/**
 * Sub-Module Fragment for RMNCHA+
 * Displays inner cards for specific modules like:
 * - Maternal Health (Pregnant Women Registration, ANC Visits, etc.)
 * - Child Care (Infant List, Child List, etc.)
 * - Eligible Couple (Registration, Tracking)
 */
@AndroidEntryPoint
class SubModuleFragment : Fragment() {

    @Inject
    lateinit var iconDataset: RMNCHAIconDataset

    private var _binding: RvIconGridBinding? = null
    private val binding: RvIconGridBinding
        get() = _binding!!

    private var moduleType: String? = null

    companion object {
        fun newInstance(moduleType: String): SubModuleFragment {
            return SubModuleFragment().apply {
                arguments = Bundle().apply {
                    putString(MODULE_TYPE_KEY, moduleType)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        moduleType = arguments?.getString(MODULE_TYPE_KEY)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RvIconGridBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpSubModuleIconAdapter()
    }

    private fun setUpSubModuleIconAdapter() {
        val rvLayoutManager = GridLayoutManager(
            context,
            requireContext().resources.getInteger(R.integer.icon_grid_span)
        )
        binding.rvIconGrid.layoutManager = rvLayoutManager
        
        val rvAdapter = IconGridAdapter(
            IconGridAdapter.GridIconClickListener { navDirections ->
                try {
                    // Check if this is a direct list navigation
                    val showECRegistration = navDirections.arguments.getBoolean(SHOW_EC_REGISTRATION_KEY, false)
                    val showECTracking = navDirections.arguments.getBoolean(SHOW_EC_TRACKING_KEY, false)
                    val showPWR = navDirections.arguments.getBoolean(SHOW_PWR_KEY, false)
                    val showANCVisits = navDirections.arguments.getBoolean(SHOW_ANC_VISITS_KEY, false)
                    val showDeliveryOutcome = navDirections.arguments.getBoolean(SHOW_DELIVERY_OUTCOME_KEY, false)
                    val showPNCMotherList = navDirections.arguments.getBoolean(SHOW_PNC_MOTHER_LIST_KEY, false)
                    val showInfantRegList = navDirections.arguments.getBoolean(SHOW_INFANT_REG_LIST_KEY, false)
                    val showChildRegList = navDirections.arguments.getBoolean(SHOW_CHILD_REG_LIST_KEY, false)
                    val showAbortionList = navDirections.arguments.getBoolean(SHOW_ABORTION_LIST_KEY, false)
                    val showPmsmaList = navDirections.arguments.getBoolean(SHOW_PMSMA_LIST_KEY, false)
                    val showInfantList = navDirections.arguments.getBoolean(SHOW_INFANT_LIST_KEY, false)
                    val showChildList = navDirections.arguments.getBoolean(SHOW_CHILD_LIST_KEY, false)
                    val showAdolescentList = navDirections.arguments.getBoolean(SHOW_ADOLESCENT_LIST_KEY, false)
                    
                    when {
                        showECRegistration -> {
                            // Navigate to Eligible Couple Registration List
                            val intent = org.piramalswasthya.cho.ui.home.rmncha.eligible_couple.EligibleCoupleRegistrationActivity.getIntent(requireContext())
                            startActivity(intent)
                        }
                        showECTracking -> {
                            // Navigate to Eligible Couple Tracking List
                            val intent = org.piramalswasthya.cho.ui.home.rmncha.eligible_couple.EligibleCoupleTrackingActivity.getIntent(requireContext())
                            startActivity(intent)
                        }
                        showPWR -> {
                            // Navigate to Pregnant Women Registration List
                            val intent = org.piramalswasthya.cho.ui.home.rmncha.maternal_health.PregnantWomenRegistrationActivity.getIntent(requireContext())
                            startActivity(intent)
                        }
                        showANCVisits -> {
                            // Navigate to ANC Visits List
                            val intent = org.piramalswasthya.cho.ui.home.rmncha.maternal_health.ANCVisitsActivity.getIntent(requireContext())
                            startActivity(intent)
                        }
                        showDeliveryOutcome -> {
                            // Navigate to Delivery Outcome List
                            val intent = org.piramalswasthya.cho.ui.home.rmncha.maternal_health.DeliveryOutcomeActivity.getIntent(requireContext())
                            startActivity(intent)
                        }
                        showPNCMotherList -> {
                            // Navigate to PNC Mother List
                            val intent = org.piramalswasthya.cho.ui.home.rmncha.maternal_health.PNCMotherListActivity.getIntent(requireContext())
                            startActivity(intent)
                        }
                        showInfantRegList -> {
                            // Navigate to Infant Registration List
                            val intent = org.piramalswasthya.cho.ui.home.rmncha.maternal_health.InfantRegListActivity.getIntent(requireContext())
                            startActivity(intent)
                        }
                        showChildRegList -> {
                            // Navigate to Child Registration List
                            val intent = org.piramalswasthya.cho.ui.home.rmncha.maternal_health.ChildRegListActivity.getIntent(requireContext())
                            startActivity(intent)
                        }
                        showAbortionList -> {
                            // Navigate to Abortion List
                            val intent = org.piramalswasthya.cho.ui.home.rmncha.maternal_health.AbortionListActivity.getIntent(requireContext())
                            startActivity(intent)
                        }
                        showPmsmaList -> {
                            // Navigate to e-PMSMA List
                            val intent = org.piramalswasthya.cho.ui.home.rmncha.maternal_health.PmsmaListActivity.getIntent(requireContext())
                            startActivity(intent)
                        }
                        showInfantList -> {
                            // Navigate to Infant List
                            val intent = org.piramalswasthya.cho.ui.home.rmncha.child_care.InfantListActivity.getIntent(requireContext())
                            startActivity(intent)
                        }
                        showChildList -> {
                            // Navigate to Child List
                            val intent = org.piramalswasthya.cho.ui.home.rmncha.child_care.ChildListActivity.getIntent(requireContext())
                            startActivity(intent)
                        }
                        showAdolescentList -> {
                            // Navigate to Adolescent List
                            val intent = org.piramalswasthya.cho.ui.home.rmncha.child_care.AdolescentListActivity.getIntent(requireContext())
                            startActivity(intent)
                        }
                        else -> {
                            // Other sub-modules - placeholder
                            Toast.makeText(
                                requireContext(),
                                "List view coming soon",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    Timber.d("Sub-module icon clicked: ${navDirections.actionId}")
                } catch (e: Exception) {
                    Timber.e(e, "Navigation failed")
                }
            },
            lifecycleScope
        )
        
        binding.rvIconGrid.adapter = rvAdapter
        
        // Load appropriate dataset based on module type
        val iconList = when (moduleType) {
            MODULE_MATERNAL_HEALTH -> iconDataset.getMaternalHealthDataset(resources)
            MODULE_CHILD_CARE -> iconDataset.getChildCareDataset(resources)
            MODULE_ELIGIBLE_COUPLE -> iconDataset.getEligibleCoupleDataset(resources)
            else -> emptyList()
        }
        
        rvAdapter.submitList(iconList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
