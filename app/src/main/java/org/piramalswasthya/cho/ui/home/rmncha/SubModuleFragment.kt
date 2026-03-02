package org.piramalswasthya.cho.ui.home.rmncha

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.IconGridAdapter
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.MODULE_MATERNAL_HEALTH
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.MODULE_TYPE_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_EC_TRACKING_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_PWR_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_ANC_VISITS_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_DELIVERY_OUTCOME_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_NEONATAL_OUTCOME_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_PNC_MOTHER_LIST_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_ABORTION_LIST_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_INFANT_LIST_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_CHILD_LIST_KEY
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset.Companion.SHOW_ADOLESCENT_LIST_KEY
import org.piramalswasthya.cho.databinding.RvIconGridBinding
import timber.log.Timber
import javax.inject.Inject

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
                // Skip navigation for placeholder entries (actionId == 0)
                if (navDirections.actionId == 0) {
                    return@GridIconClickListener
                }
                
                try {
                    // Check if this is a direct list navigation
                    val showECTracking = navDirections.arguments.getBoolean(SHOW_EC_TRACKING_KEY, false)
                    val showPWR = navDirections.arguments.getBoolean(SHOW_PWR_KEY, false)
                    val showANCVisits = navDirections.arguments.getBoolean(SHOW_ANC_VISITS_KEY, false)
                    val showDeliveryOutcome = navDirections.arguments.getBoolean(SHOW_DELIVERY_OUTCOME_KEY, false)
                    val showNeonatalOutcome = navDirections.arguments.getBoolean(SHOW_NEONATAL_OUTCOME_KEY, false)
                    val showPNCMotherList = navDirections.arguments.getBoolean(SHOW_PNC_MOTHER_LIST_KEY, false)
                    val showAbortionList = navDirections.arguments.getBoolean(SHOW_ABORTION_LIST_KEY, false)
                    val showInfantList = navDirections.arguments.getBoolean(SHOW_INFANT_LIST_KEY, false)
                    val showChildList = navDirections.arguments.getBoolean(SHOW_CHILD_LIST_KEY, false)
                    val showAdolescentList = navDirections.arguments.getBoolean(SHOW_ADOLESCENT_LIST_KEY, false)
                    
                    when {
                        showECTracking -> {
                            // Navigate to Eligible Couple Tracking List Fragment
                            val fragment = org.piramalswasthya.cho.ui.home.rmncha.eligible_couple.EligibleCoupleTrackingFragment()
                            requireActivity().supportFragmentManager.commit {
                                replace(R.id.fragment_container, fragment)
                                addToBackStack(null)
                            }
                        }
                        showPWR -> {
                            // Navigate to Maternal Health Nav Host (which uses nav_maternal_health.xml)
                            val fragment = org.piramalswasthya.cho.ui.home.rmncha.maternal_health.MaternalHealthNavHostFragment()
                            requireActivity().supportFragmentManager.commit {
                                replace(R.id.fragment_container, fragment)
                                addToBackStack(null)
                            }
                        }
                        showANCVisits -> {
                            // Navigate to ANC Visits List Fragment
                            val fragment = org.piramalswasthya.cho.ui.home.rmncha.maternal_health.ANCVisitsFragment()
                            requireActivity().supportFragmentManager.commit {
                                replace(R.id.fragment_container, fragment)
                                addToBackStack(null)
                            }
                        }
                        showDeliveryOutcome -> {
                            // Navigate to Delivery Outcome List Fragment
                            val fragment = org.piramalswasthya.cho.ui.home.rmncha.maternal_health.DeliveryOutcomeFragment()
                            requireActivity().supportFragmentManager.commit {
                                replace(R.id.fragment_container, fragment)
                                addToBackStack(null)
                            }
                        }
                        showNeonatalOutcome -> {
                            // Navigate to Neonatal Outcome List Fragment
                            val fragment = org.piramalswasthya.cho.ui.home.rmncha.maternal_health.NeonatalOutcomeFragment()
                            requireActivity().supportFragmentManager.commit {
                                replace(R.id.fragment_container, fragment)
                                addToBackStack(null)
                            }
                        }
                        showPNCMotherList -> {
                            // Navigate to PNC Mother List Fragment
                            val fragment = org.piramalswasthya.cho.ui.home.rmncha.maternal_health.PNCMotherListFragment()
                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit()
                        }
                        showAbortionList -> {
                            // Navigate to Abortion List Fragment
                            val fragment = org.piramalswasthya.cho.ui.home.rmncha.maternal_health.AbortionListFragment()
                            requireActivity().supportFragmentManager.commit {
                                replace(R.id.fragment_container, fragment)
                                addToBackStack(null)
                            }
                        }
                        showInfantList -> {
                            // Navigate to Infant List Fragment
                            val fragment = org.piramalswasthya.cho.ui.home.rmncha.child_care.InfantListFragment()
                            requireActivity().supportFragmentManager.commit {
                                replace(R.id.fragment_container, fragment)
                                addToBackStack(null)
                            }
                        }
                        showChildList -> {
                            // Navigate to Child Registration list (registered infants)
                            val fragment = org.piramalswasthya.cho.ui.home.rmncha.child_care.ChildRegistrationListFragment()
                            requireActivity().supportFragmentManager.commit {
                                replace(R.id.fragment_container, fragment)
                                addToBackStack(null)
                            }
                        }
                        showAdolescentList -> {
                            // Navigate to Adolescent List Fragment
                            val fragment = org.piramalswasthya.cho.ui.home.rmncha.child_care.AdolescentListFragment()
                            requireActivity().supportFragmentManager.commit {
                                replace(R.id.fragment_container, fragment)
                                addToBackStack(null)
                            }
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
            }
        )
        
        binding.rvIconGrid.adapter = rvAdapter
        
        // Load appropriate dataset based on module type
        val iconList = when (moduleType) {
            MODULE_MATERNAL_HEALTH -> iconDataset.getMaternalHealthDataset(resources)
            else -> emptyList()
        }
        
        rvAdapter.submitList(iconList)
    }

    override fun onResume() {
        super.onResume()
        val titleRes = when (moduleType) {
            MODULE_MATERNAL_HEALTH -> R.string.maternal_health
            else -> null
        }
        titleRes?.let {
            (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = getString(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
