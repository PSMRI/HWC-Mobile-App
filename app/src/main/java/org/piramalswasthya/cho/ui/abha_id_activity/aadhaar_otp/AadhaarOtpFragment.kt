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
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentAadhaarOtpBinding
import org.piramalswasthya.cho.helpers.AnalyticsHelper
import org.piramalswasthya.cho.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_id.AadhaarIdViewModel
import org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_otp.AadhaarOtpViewModel.State
import javax.inject.Inject

@AndroidEntryPoint
class AadhaarOtpFragment : Fragment() {

    private var _binding: FragmentAadhaarOtpBinding? = null
    private val binding: FragmentAadhaarOtpBinding
        get() = _binding!!

    private val viewModel: AadhaarOtpViewModel by viewModels()

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    private val parentViewModel: AadhaarIdViewModel by viewModels({ requireActivity() })

    val args: AadhaarOtpFragmentArgs by lazy {
        AadhaarOtpFragmentArgs.fromBundle(requireArguments())
    }

    private var timer = object : CountDownTimer(60000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val sec = millisUntilFinished / 1000 % 60
            //  binding.timerResendOtp.text = "Resend OTP in 00:$sec"
            binding.timerCount.text = "$sec"
        }

        // When the task is over it will print 00:00:00 there
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
        _binding = FragmentAadhaarOtpBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startResendTimer()
        binding.btnVerifyOTP.setOnClickListener {
            if (parentViewModel.abhaMode.value == AadhaarIdViewModel.Abha.SEARCH) {
                viewModel.verifyLoginOtpClicked(binding.otpView.text.toString())
            } else {
                viewModel.verifyOtpClicked(
                    binding.otpView.text.toString(),
                    parentViewModel.mobileNumber
                )
            }
        }
        binding.resendOtp.setOnClickListener {
            if (parentViewModel.abhaMode.value == AadhaarIdViewModel.Abha.CREATE && parentViewModel.mobileNumber == viewModel.mobileNumber) {
                viewModel.resendCreateAadhaarOtp(parentViewModel.aadhaarNumber)
                startResendTimer()

            } else {
                viewModel.resendOtpForSearchAbha(parentViewModel.selectedAbhaIndex,parentViewModel.otpTxnId)
                startResendTimer()

            }
        }

        if (parentViewModel.abhaMode.value == AadhaarIdViewModel.Abha.SEARCH) {
            binding.textView6.text = "Verify ABHA OTP"
        }


        binding.otpView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.btnVerifyOTP.isEnabled = s != null && s.length == 6
                binding.tvErrorText.visibility = View.GONE
            }

            override fun afterTextChanged(s: Editable) {
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

        viewModel.state2.observe(viewLifecycleOwner) {
            if (it == AadhaarIdViewModel.State.SUCCESS) {
                Toast.makeText(
                    context,
                    resources.getString(R.string.otp_resent),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                State.IDLE -> {
//                    binding.tvOtpMsg.text = String.format(
//                        "%s%s",
//                        resources.getString(R.string.otp_sent_to),
//                        args.mobileNumber
//                    )
                    binding.tvOtpMsg.visibility = View.VISIBLE
                    var string = getMobileNumber(parentViewModel.otpMobileNumberMessage) ?: ""

                    if (parentViewModel.abhaMode.value == AadhaarIdViewModel.Abha.SEARCH) {
                        binding.tvOtpMsg.text = getString(R.string.str_abha_otp_number).replace("@mobileNumber", string)
                        binding.tvOTPNote.visibility = View.VISIBLE
                    }else{
                        binding.tvOtpMsg.text = getString(R.string.str_aadhaar_otp_number).replace("@mobileNumber", string)
                        binding.tvOTPNote.visibility = View.VISIBLE
                    }


                }

                State.LOADING -> {
                    binding.clContent.visibility = View.INVISIBLE
                    binding.pbLoadingAadharOtp.visibility = View.VISIBLE
                    binding.clError.visibility = View.INVISIBLE
                }

                State.OTP_VERIFY_SUCCESS -> {
                    if (parentViewModel.abhaMode.value == AadhaarIdViewModel.Abha.SEARCH) {
                        findNavController().navigate(
                            AadhaarOtpFragmentDirections.actionAadhaarOtpFragmentToCreateAbhaFragment(
                                viewModel.txnId, viewModel.name, viewModel.phrAddress, viewModel.abhaNumber,""
                            )
                        )
                    } else if (parentViewModel.abhaMode.value == AadhaarIdViewModel.Abha.CREATE &&
                        parentViewModel.mobileNumber == viewModel.mobileNumber) {
                        val timestamp = System.currentTimeMillis()
                        analyticsHelper.logCustomTimestampEvent("create_abha_reponse",timestamp)
                        findNavController().navigate(
                            AadhaarOtpFragmentDirections.actionAadhaarOtpFragmentToCreateAbhaFragment(
                                viewModel.txnId, viewModel.name, viewModel.phrAddress, viewModel.abhaNumber,viewModel.abhaResponse
                            )
                        )
                    } else {
                        viewModel.generateOtpClicked()
                    }
                    viewModel.resetState()
                }

                State.SUCCESS -> {
                    findNavController().navigate(
                        AadhaarOtpFragmentDirections.actionAadhaarOtpFragmentToVerifyMobileOtpFragment(
                            viewModel.txnId, viewModel.mobileNumber, viewModel.mobileFromArgs,viewModel.name, viewModel.phrAddress, viewModel.abhaNumber,viewModel.abhaResponse
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
                    Toast.makeText(
                        activity,
                        resources.getString(R.string.otp_was_resent),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            it?.let {
                binding.otpView.setText("")
                binding.tvErrorText.text = it
                viewModel.resetErrorMessage()
            }
        }

        // Mobile Number observation for ABHA v1
//        viewModel.mobileNumber.observe(viewLifecycleOwner) {
//            it?.let {
//                parentViewModel.setMobileNumber(it)
//            }
//        }

        viewModel.otpMobileNumberMessage.observe(viewLifecycleOwner) {
            it?.let {
                parentViewModel.setOTPMsg(it)
            }
        }

    }

    private fun getMobileNumber(input: String): String {
        val regex = Regex("""\*+\d+""")
        val matches = regex.findAll(input).toList()
        val lastMatch = matches.lastOrNull()?.value
        println("Extracted: $lastMatch") // Output: ******0180
        return lastMatch?:""
    }

    private fun startResendTimer() {
        binding.resendOtp.isEnabled = false
        binding.timerResendOtp.visibility = View.VISIBLE
        binding.timerCount.visibility = View.VISIBLE
        binding.timerSeconds.visibility = View.VISIBLE
        timer.start()
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        timer.cancel()
    }

}
