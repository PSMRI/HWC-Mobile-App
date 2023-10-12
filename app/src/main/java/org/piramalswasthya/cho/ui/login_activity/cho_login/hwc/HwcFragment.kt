package org.piramalswasthya.cho.ui.login_activity.cho_login.hwc

import android.opengl.Visibility
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentChoLoginBinding
import org.piramalswasthya.cho.databinding.FragmentHwcBinding
import org.piramalswasthya.cho.ui.home.HomeViewModel
import org.piramalswasthya.cho.ui.login_activity.asha_login.AshaLoginFragmentDirections
import org.piramalswasthya.cho.ui.login_activity.cho_login.ChoLoginFragmentDirections
import org.piramalswasthya.cho.ui.login_activity.cho_login.outreach.OutreachViewModel
import org.piramalswasthya.cho.ui.login_activity.username.UsernameFragmentDirections
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
@AndroidEntryPoint
class HwcFragment constructor(
    private val userName: String,
    private val rememberUsername: Boolean,
    private val isBiometric: Boolean,
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
        viewModel = ViewModelProvider(this).get(HwcViewModel::class.java)

        _binding = FragmentHwcBinding.inflate(layoutInflater, container, false)
        if (isBiometric) {
            binding.tilPasswordHwc.visibility = View.GONE
            binding.btnHwcLogin.text = "Proceed to Home"
        } else {
            binding.tilPasswordHwc.visibility = View.VISIBLE
            if (!viewModel.fetchRememberedPassword().isNullOrBlank()) {
            viewModel.fetchRememberedPassword()?.let {
                binding.etPasswordHwc.setText(it)
            }
        }
    }
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val pattern = "yyyy-MM-dd'T'HH:mm:ssZ"
        val timeZone = TimeZone.getTimeZone("GMT+0530")
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        formatter.timeZone = timeZone
        val timestamp = formatter.format(Date())
            binding.btnHwcLogin.setOnClickListener {
                if (!isBiometric) {
                    viewModel.authUser(
                        userName,
                        binding.etPasswordHwc.text.toString(),
                        "HWC",
                        null,
                        timestamp,
                        null,
                        null,
                        null,
                        null,
                    )

                    viewModel.state.observe(viewLifecycleOwner) { state ->
                        when (state!!) {

                            OutreachViewModel.State.SUCCESS -> {
                                binding.patientListFragment.visibility = View.VISIBLE
                                binding.rlSaving.visibility = View.GONE

                                if (rememberUsername)
                                    viewModel.rememberUser(
                                        userName,
                                        binding.etPasswordHwc.text.toString()
                                    )
                                else {
                                    viewModel.forgetUser()
                                }
                                findNavController().navigate(
                                    ChoLoginFragmentDirections.actionSignInToHomeFromCho(true)
                                )
                                viewModel.resetState()
                                activity?.finish()
                            }

                            OutreachViewModel.State.SAVING -> {
                                binding.patientListFragment.visibility = View.GONE
                                binding.rlSaving.visibility = View.VISIBLE
                            }

                            OutreachViewModel.State.ERROR_SERVER,
                            OutreachViewModel.State.ERROR_NETWORK -> {
                                binding.patientListFragment.visibility = View.VISIBLE
                                binding.rlSaving.visibility = View.GONE
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.error_while_logging_in),
                                    Toast.LENGTH_LONG
                                ).show()
//                        viewModel.forgetUser()
                                viewModel.resetState()
                            }

                            else -> {}
                        }

                    }
                }
                else{
                lifecycleScope.launch {
                    viewModel.setOutreachDetails(
                        "HWC",
                        null,
                        timestamp,
                        null,
                        null,
                        null,
                        null,
                    )
                    findNavController().navigate(
                        ChoLoginFragmentDirections.actionSignInToHomeFromCho(true)
                    )
                    viewModel.resetState()
                    activity?.finish()
                }
            }
            }
        }

//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
////        binding.btnHwcLogin.setOnClickListener {
////            findNavController().navigate(
////                ChoLoginFragmentDirections.actionSignInToHomeFromCho()
////            )
////        }
//        // TODO: Use the ViewModel
//    }

}