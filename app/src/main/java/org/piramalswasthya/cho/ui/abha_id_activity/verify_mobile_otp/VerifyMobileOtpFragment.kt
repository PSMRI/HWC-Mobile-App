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
import org.piramalswasthya.cho.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_id.AadhaarIdViewModel
import org.piramalswasthya.cho.ui.abha_id_activity.verify_mobile_otp.VerifyMobileOtpViewModel.State

@AndroidEntryPoint
class VerifyMobileOtpFragment : Fragment() {

    private var _binding: FragmentVerifyMobileOtpBinding? = null
    private val binding: FragmentVerifyMobileOtpBinding
        get() = _binding!!

    private lateinit var navController: NavController

    private val viewModel: VerifyMobileOtpViewModel by viewModels()

    private val parentViewModel: AadhaarIdViewModel by viewModels({ requireActivity() })

    val args: VerifyMobileOtpFragmentArgs by lazy {
        VerifyMobileOtpFragmentArgs.fromBundle(requireArguments())
    }


    private var timer = object : CountDownTimer(60000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val sec = millisUntilFinished / 1000 % 60
            //  binding.timerResendOtp.text = "Didn't receive OTP? Wait 00:$sec seconds"
            binding.timerCount.text = "$sec"
        }

        override fun onFinish() {
            binding.resendOtp.isEnabled = true
            binding.timerResendOtp.visibility = View.INVISIBLE
            binding.timerCount.visibility = View.INVISIBLE
            binding.timerSeconds.visibility = View.INVISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerifyMobileOtpBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        startResendTimer()
        binding.btnVerifyOTP.setOnClickListener {
            viewModel.verifyOtpClicked(binding.otpView.text.toString())
        }

        binding.resendOtp.setOnClickListener {
            viewModel.resendOtp()
            startResendTimer()
        }

        binding.otpView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                binding.btnVerifyOTP.isEnabled = p0 != null && p0.length == 6
                binding.tvErrorText.visibility = View.GONE
            }
        })

        viewModel.showExit.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    binding.exit.visibility = View.VISIBLE
                } else {
                    binding.exit.visibility = View.GONE
                }
            }
        }

        binding.exit.setOnClickListener {
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
                            viewModel.txnID, args.name, args.phrAddress, args.abhaNumber,viewModel.abhaResponse
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
                    Toast.makeText(
                        activity,
                        resources.getString(R.string.otp_was_resent),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }

                State.ABHA_GENERATED_SUCCESS -> {
                    findNavController().navigate(
                        VerifyMobileOtpFragmentDirections.actionVerifyMobileOtpFragmentToCreateAbhaFragment(
                            viewModel.txnID, args.name, args.phrAddress, args.abhaNumber,viewModel.abhaResponse
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

        var string = getMobileNumber(parentViewModel.otpMobileNumberMessage) ?: ""
        binding.tvOtpMsg.text = getString(R.string.str_otp_number_message).replace("@mobileNumber", string)

    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as AbhaIdActivity).updateActionBar(
                R.drawable.ic__abha_logo_v1_24,
                getString(R.string.generate_abha)
            )
        }
    }

    private fun startResendTimer() {
        binding.resendOtp.isEnabled = false
        binding.timerResendOtp.visibility = View.VISIBLE
        binding.timerCount.visibility = View.VISIBLE
        binding.timerSeconds.visibility = View.VISIBLE
        timer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        _binding = null
    }

    private fun getMobileNumber(input: String): String? {
        val regex = Regex("""\*+\d+""")
        val matches = regex.findAll(input).toList()
        val lastMatch = matches.lastOrNull()?.value
        println("Extracted: $lastMatch") // Output: ******0180
        return lastMatch
    }

}