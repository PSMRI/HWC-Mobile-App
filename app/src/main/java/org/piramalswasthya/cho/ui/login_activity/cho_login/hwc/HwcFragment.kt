package org.piramalswasthya.cho.ui.login_activity.cho_login.hwc

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentChoLoginBinding
import org.piramalswasthya.cho.databinding.FragmentHwcBinding
import org.piramalswasthya.cho.ui.login_activity.asha_login.AshaLoginFragmentDirections
import org.piramalswasthya.cho.ui.login_activity.cho_login.ChoLoginFragmentDirections
import org.piramalswasthya.cho.ui.login_activity.username.UsernameFragmentDirections
import javax.inject.Inject

class HwcFragment constructor(
    private val userName: String,
): Fragment() {

    @Inject
    lateinit var prefDao: PreferenceDao

    private var _binding: FragmentHwcBinding? = null
    private val binding: FragmentHwcBinding
        get() = _binding!!

    private lateinit var viewModel: HwcViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHwcBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        binding.btnHwcLogin.setOnClickListener {
//            findNavController().navigate(
//                ChoLoginFragmentDirections.actionSignInToHomeFromCho()
//            )
//        }
        // TODO: Use the ViewModel
    }

}