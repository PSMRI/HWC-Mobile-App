package org.piramalswasthya.cho.ui.abha_id_activity.verify_mobile_otp

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentVerifyMobileOtpBinding
import org.piramalswasthya.cho.ui.abha_id_activity.verify_mobile_otp.VerifyMobileOtpViewModel.State

@AndroidEntryPoint
class VerifyMobileOtpFragment : Fragment() {

    private var _binding: FragmentVerifyMobileOtpBinding? = null
    private val binding: FragmentVerifyMobileOtpBinding
        get() = _binding!!

    private lateinit var navController: NavController

    private val viewModel: VerifyMobileOtpViewModel by viewModels()


    private var timer = object : CountDownTimer(30000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val sec = millisUntilFinished / 1000 % 60
            binding.timerResendOtp.text = sec.toString()
        }

        override fun onFinish() {
            binding.resendOtp.isEnabled = true
            binding.timerResendOtp.visibility = View.INVISIBLE
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVerifyMobileOtpBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        startResendTimer()
        binding.btnVerifyOTP.setOnClickListener {
            viewModel.verifyOtpClicked(binding.tietVerifyMobileOtp.text.toString())
        }

        binding.resendOtp.setOnClickListener {
            viewModel.resendOtp()
            startResendTimer()
        }

        binding.tietVerifyMobileOtp.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                binding.btnVerifyOTP.isEnabled = p0 != null && p0.length == 6
            }
        })
        viewModel.showExit.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    binding.exit.visibility =  View.VISIBLE
                } else {
                    binding.exit.visibility =  View.GONE
                }
            }
        }

        binding.exit.setOnClickListener{
            requireActivity().finish()
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                State.IDLE -> {}
                State.LOADING -> {
                    binding.clVerifyMobileOtp.visibility = View.INVISIBLE
                    binding.pbVmotp.visibility = View.VISIBLE
                    binding.clError.visibility = View.INVISIBLE
                }
                State.OTP_VERIFY_SUCCESS -> {
                    findNavController().navigate(
                        VerifyMobileOtpFragmentDirections.actionVerifyMobileOtpFragmentToCreateAbhaFragment(
                            viewModel.txnID,
                            null.toString(),
                            null.toString(),
                            null.toString()
                        )
                    )
                    viewModel.resetState()
                }
                State.ERROR_NETWORK -> {
                    binding.clVerifyMobileOtp.visibility = View.INVISIBLE
                    binding.pbVmotp.visibility = View.INVISIBLE
                    binding.clError.visibility = View.VISIBLE
                }
                State.ERROR_SERVER -> {
                    binding.clVerifyMobileOtp.visibility = View.VISIBLE
                    binding.pbVmotp.visibility = View.INVISIBLE
                    binding.clError.visibility = View.INVISIBLE
                    binding.tvErrorText.visibility = View.VISIBLE
                }
                State.OTP_GENERATED_SUCCESS -> {
                    binding.clVerifyMobileOtp.visibility = View.VISIBLE
                    binding.pbVmotp.visibility = View.INVISIBLE
                    binding.clError.visibility = View.INVISIBLE
                    Toast.makeText(activity, getString(R.string.otp_was_resent), Toast.LENGTH_LONG)
                        .show()
                }
                State.ABHA_GENERATED_SUCCESS -> {
                    findNavController().navigate(
                        VerifyMobileOtpFragmentDirections.actionVerifyMobileOtpFragmentToCreateAbhaFragment(
                            viewModel.txnID,
                            null.toString(),
                            null.toString(),
                            null.toString()
                        )
                    )
                    viewModel.resetState()
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvErrorText.text = it
                viewModel.resetErrorMessage()
            }
        }
    }

    private fun startResendTimer() {
        binding.resendOtp.isEnabled = false
        binding.timerResendOtp.visibility = View.VISIBLE
        timer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        _binding = null
    }

}