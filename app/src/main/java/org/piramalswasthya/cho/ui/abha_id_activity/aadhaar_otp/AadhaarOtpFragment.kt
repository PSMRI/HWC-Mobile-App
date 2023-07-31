package org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_otp


import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentAadhaarOtpBinding
import org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_otp.AadhaarOtpViewModel.State

@AndroidEntryPoint
class AadhaarOtpFragment : Fragment() {

    private var _binding: FragmentAadhaarOtpBinding? = null
    private val binding: FragmentAadhaarOtpBinding
        get() = _binding!!

    private val viewModel: AadhaarOtpViewModel by viewModels()

    val args: AadhaarOtpFragmentArgs by lazy {
        AadhaarOtpFragmentArgs.fromBundle(requireArguments())
    }

    private var timer = object : CountDownTimer(30000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val sec = millisUntilFinished / 1000 % 60
            binding.timerResendOtp.text = sec.toString()
        }

        // When the task is over it will print 00:00:00 there
        override fun onFinish() {
            binding.resendOtp.isEnabled = true
            binding.timerResendOtp.visibility = View.INVISIBLE
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAadhaarOtpBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startResendTimer()
        binding.btnVerifyOTP.setOnClickListener {
            viewModel.verifyOtpClicked(binding.tietAadhaarOtp.text.toString())
        }
        binding.resendOtp.setOnClickListener {
            viewModel.resendOtp()
            startResendTimer()
        }

        binding.tietAadhaarOtp.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                binding.btnVerifyOTP.isEnabled = p0 != null && p0.length == 6
            }

        })

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                State.IDLE -> {
                    binding.tvOtpMsg.text = "OTP sent to ${args.mobileNumber}"
                    binding.tvOtpMsg.visibility = View.VISIBLE
                }
                State.LOADING -> {
                    binding.clContent.visibility = View.INVISIBLE
                    binding.pbLoadingAadharOtp.visibility = View.VISIBLE
                    binding.clError.visibility = View.INVISIBLE
                }
                State.OTP_VERIFY_SUCCESS -> {
                    findNavController().navigate(
                        AadhaarOtpFragmentDirections.actionAadhaarOtpFragmentToGenerateMobileOtpFragment(
                            viewModel.txnId
                        )
                    )
                    viewModel.resetState()
                }
                State.ERROR_SERVER -> {
                    binding.pbLoadingAadharOtp.visibility = View.INVISIBLE
                    binding.clContent.visibility = View.VISIBLE
                    binding.tvErrorText.visibility = View.VISIBLE
                    binding.clError.visibility = View.INVISIBLE
                }
                State.ERROR_NETWORK -> {
                    binding.clContent.visibility = View.INVISIBLE
                    binding.pbLoadingAadharOtp.visibility = View.INVISIBLE
                    binding.clError.visibility = View.VISIBLE
                }
                State.OTP_GENERATED_SUCCESS -> {
                    binding.clContent.visibility = View.VISIBLE
                    binding.pbLoadingAadharOtp.visibility = View.INVISIBLE
                    binding.clError.visibility = View.INVISIBLE
                    binding.tvErrorText.visibility = View.INVISIBLE
                    Toast.makeText(activity, getString(R.string.otp_was_resent), Toast.LENGTH_LONG)
                        .show()
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
        _binding = null
        timer.cancel()
    }
}
