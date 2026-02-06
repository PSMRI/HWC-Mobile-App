package org.piramalswasthya.cho.ui.home.rmncha.maternal_health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
