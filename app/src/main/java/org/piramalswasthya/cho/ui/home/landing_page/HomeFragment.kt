package org.piramalswasthya.cho.ui.home.landing_page

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.ActivityHomeBinding
import org.piramalswasthya.cho.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null

    private val binding: FragmentHomeBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.apply {
//            fab.setOnClickListener { findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddPatientFragment()) }
//            fab.setColorFilter(Color.WHITE)
//        }
    }


    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }}