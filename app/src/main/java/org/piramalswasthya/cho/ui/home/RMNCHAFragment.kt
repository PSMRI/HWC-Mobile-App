package org.piramalswasthya.cho.ui.home

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
                try {
                    // Check if this module has sub-modules
                    val moduleType = navDirections.arguments.getString(
                        org.piramalswasthya.cho.configuration.RMNCHAIconDataset.MODULE_TYPE_KEY
                    )
                    
                    if (moduleType != null) {
                        // Navigate to SubModuleActivity for modules with sub-cards
                        val intent = org.piramalswasthya.cho.ui.home.rmncha.SubModuleActivity.getIntent(
                            requireContext(),
                            moduleType
                        )
                        startActivity(intent)
                    } else {
                        // Handle "All Beneficiaries" - navigate to dedicated activity
                        val showAllBeneficiaries = navDirections.arguments.getBoolean(
                            org.piramalswasthya.cho.configuration.RMNCHAIconDataset.SHOW_ALL_BENEFICIARIES_KEY,
                            false
                        )
                        
                        if (showAllBeneficiaries) {
                            // Navigate to AllBeneficiariesActivity
                            val intent = org.piramalswasthya.cho.ui.home.rmncha.AllBeneficiariesActivity.getIntent(requireContext())
                            startActivity(intent)
                        } else {
                            // Other modules - list view coming soon
                            Toast.makeText(
                                requireContext(),
                                "List view coming soon",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    Timber.d("RMNCHA+ icon clicked: ${navDirections.actionId}")
                } catch (e: Exception) {
                    Timber.e(e, "Navigation failed")
                }
            },
            lifecycleScope
        )
        
        binding.rvIconGrid.adapter = rvAdapter
        rvAdapter.submitList(iconDataset.getRMNCHAIconDataset(resources))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
