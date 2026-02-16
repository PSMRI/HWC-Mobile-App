package org.piramalswasthya.cho.ui.home.rmncha.maternal_health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentMaternalHealthNavHostBinding

/**
 * Container Fragment that hosts the Navigation Component for Maternal Health module
 * Uses nav_maternal_health.xml navigation graph
 */
class MaternalHealthNavHostFragment : Fragment() {

    private var _binding: FragmentMaternalHealthNavHostBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMaternalHealthNavHostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navHostFragment = childFragmentManager.findFragmentById(R.id.nav_host_fragment_maternal_health) as NavHostFragment
        val navController = navHostFragment.navController
        timber.log.Timber.d("MaternalHealthNavHostFragment onViewCreated. Arguments: $arguments")

        arguments?.let { args ->
            val destinationId = args.getInt("destinationId", 0)
            val fromECT = args.getBoolean("fromECT", false)
            timber.log.Timber.d("MaternalHealthNavHostFragment destinationId: $destinationId, fromECT: $fromECT")
            if (destinationId != 0) {
                val navGraph = navController.navInflater.inflate(R.navigation.nav_maternal_health)
                navGraph.setStartDestination(destinationId)
                navController.setGraph(navGraph, args)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
