package org.piramalswasthya.cho.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.IconGridAdapter
import org.piramalswasthya.cho.configuration.RMNCHAIconDataset
import org.piramalswasthya.cho.databinding.RvIconGridBinding
import timber.log.Timber
import javax.inject.Inject

/**
 * RMNCHA+ Dashboard Fragment
 * Displays RMNCHA+ service modules including:
 * - Maternal Health (Pregnancy, ANC, PNC, Delivery)
 * - Child Care (Infant, Child, Adolescent)
 * - Eligible Couple Management
 * - Family Planning Services
 * - And other RMNCHA+ services
 */
@AndroidEntryPoint
class RMNCHAFragment : Fragment() {

    @Inject
    lateinit var iconDataset: RMNCHAIconDataset

    private var _binding: RvIconGridBinding? = null
    private val binding: RvIconGridBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RvIconGridBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRMNCHAIconAdapter()
    }

    private fun setUpRMNCHAIconAdapter() {
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
                    // Check if this module has sub-modules
                    val moduleType = navDirections.arguments.getString(
                        RMNCHAIconDataset.MODULE_TYPE_KEY
                    )

                    // Check for direct fragment navigation
                    val showECTracking = navDirections.arguments.getBoolean(
                        RMNCHAIconDataset.SHOW_EC_TRACKING_KEY, false
                    )
                    val showAdolescentList = navDirections.arguments.getBoolean(
                        RMNCHAIconDataset.SHOW_ADOLESCENT_LIST_KEY, false
                    )

                    when {
                        moduleType != null -> {
                            // Navigate to SubModuleActivity for modules with sub-cards
                            val intent = org.piramalswasthya.cho.ui.home.rmncha.SubModuleActivity.getIntent(
                                requireContext(),
                                moduleType
                            )
                            startActivity(intent)
                        }
                        showECTracking -> {
                            // Navigate directly to EC Tracking
                            val intent = org.piramalswasthya.cho.ui.home.rmncha.SubModuleActivity.getDirectFragmentIntent(
                                requireContext(),
                                RMNCHAIconDataset.SHOW_EC_TRACKING_KEY
                            )
                            startActivity(intent)
                        }
                        showAdolescentList -> {
                            // Navigate directly to Adolescent List
                            val intent = org.piramalswasthya.cho.ui.home.rmncha.SubModuleActivity.getDirectFragmentIntent(
                                requireContext(),
                                RMNCHAIconDataset.SHOW_ADOLESCENT_LIST_KEY
                            )
                            startActivity(intent)
                        }
                    }
                    Timber.d("RMNCHA+ icon clicked: ${navDirections.actionId}")
                } catch (e: Exception) {
                    Timber.e(e, "Navigation failed")
                }
            }
        )
        
        binding.rvIconGrid.adapter = rvAdapter
        rvAdapter.submitList(iconDataset.getRMNCHAIconDataset(resources))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
