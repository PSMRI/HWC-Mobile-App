package org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_id.aadhaar_num_asha


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentAadhaarNumberAshaBinding
import org.piramalswasthya.cho.helpers.AadhaarValidationUtils
import org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_id.AadhaarIdViewModel
import org.piramalswasthya.cho.utils.HelperUtil
import java.util.regex.Matcher
import java.util.regex.Pattern


@AndroidEntryPoint
class AadhaarNumberAshaFragment : Fragment() {

    private var isPasswordVisible:Boolean = false

    var isValidAadhaar = false
    var isValidMobile = false
    var isValidBenName = false

    private var _binding: FragmentAadhaarNumberAshaBinding? = null
    private val binding: FragmentAadhaarNumberAshaBinding
        get() = _binding!!

    private val parentViewModel: AadhaarIdViewModel by viewModels({ requireActivity() })

    private val viewModel: AadhaarNumberAshaViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAadhaarNumberAshaBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentViewModel.verificationType.observe(viewLifecycleOwner) {
            when (it) {
                "OTP" -> binding.btnVerifyAadhaar.text = resources.getString(R.string.generate_otp)
                "FP" -> {
                    binding.btnVerifyAadhaar.text = resources.getString(R.string.validate_fp)
                }
            }
        }

        val intent = requireActivity().intent

        val benId = intent.getLongExtra("benId", 0)

        if (benId > 0) {
            viewModel.getBen(benId)
        }

        binding.btnVerifyAadhaar.setOnClickListener {
            verifyAadhaar()
        }

        viewModel.ben.observe(viewLifecycleOwner) {
            if(it!=null){
                binding.benNameTitle.visibility = View.VISIBLE
                binding.benName.visibility = View.VISIBLE
                binding.benName.text =it
                parentViewModel.setBeneficiaryName(it)
                binding.clBenName.visibility = View.GONE

            }else{
                binding.clBenName.visibility = View.VISIBLE
            }

        }

        viewModel.state.observe(viewLifecycleOwner) {
            if (it == AadhaarIdViewModel.State.SUCCESS) {
                viewModel.resetState()
            }
            parentViewModel.setAbhaMode(AadhaarIdViewModel.Abha.CREATE)
            parentViewModel.setState(it)
        }

        viewModel.mobileNumber.observe(viewLifecycleOwner) {
            it?.let {
                parentViewModel.setMobileNumber(it)
            }
        }

        viewModel.txnId.observe(viewLifecycleOwner) {
            it?.let {
                parentViewModel.setTxnId(it)
            }
        }
        viewModel.otpMobileNumberMessage.observe(viewLifecycleOwner) {
            it?.let {
                parentViewModel.setOTPMsg(it)
            }
        }

        binding.clickview.setOnClickListener {
            if(parentViewModel.beneficiaryName.value!=null && !parentViewModel.beneficiaryName.value.isNullOrBlank()) {
                viewModel.aadhaarNumber.value = binding.tietAadhaarNumber.text.toString()
                parentViewModel.navigateToAadhaarConsent(true)
            }else{
                Toast.makeText(requireContext(),"Please Enter Beneficiary Name",Toast.LENGTH_SHORT).show()
            }
        }
        binding.tietAadhaarNumber.setEdiTextBackground(ContextCompat.getDrawable(requireContext(), R.drawable.selector_edittext_round_border_line))

        binding.tietAadhaarNumber.setTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Required override for TextWatcher.
                // No pre-change logic needed for this input field.
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if(s.toString().isNullOrBlank()){
                    binding.tvErrorText.visibility = View.GONE
                    binding.tvErrorText.text = ""
                    binding.ivValidAadhaar.setImageResource(R.drawable.ic_check_circle_grey)
                }else if(AadhaarValidationUtils.isValidAadhaar(s.toString())){
                    binding.tvErrorText.visibility = View.GONE
                    binding.tvErrorText.text = ""
                    binding.ivValidAadhaar.setImageResource(R.drawable.ic_check_circle_green)
                    binding.tietAadhaarNumber.setEdiTextBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_border_line_green))

                }else{
                    binding.tvErrorText.visibility = View.VISIBLE
                    binding.tvErrorText.text = getString(R.string.str_invalid_aadhaar_no)
                    binding.ivValidAadhaar.setImageResource(R.drawable.ic_check_circle_grey)
                    binding.tietAadhaarNumber.setEdiTextBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_border_line))
                }

                isValidAadhaar = (s != null) && AadhaarValidationUtils.isValidAadhaar(s.toString())
                binding.btnVerifyAadhaar.isEnabled = isValidAadhaar && isValidMobile
                        && (parentViewModel.consentChecked.value==true)//binding.aadharConsentCheckBox.isChecked
            }

            override fun afterTextChanged(s: Editable?) {
                // intentionally left blank - not required for this use case
            }

        })


        binding.tietMobileNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // intentionally left blank - not required for this use case
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // intentionally left blank - not required for this use case
            }

            override fun afterTextChanged(s: Editable?) {
                if((s != null) && isValidMobileNumber(s.toString())){
                    binding.tilMobileNumber.error = null
                    binding.ivValidMobile.setImageResource(R.drawable.ic_check_circle_green)

                }else{
                    binding.tilMobileNumber.error = getString(R.string.str_invalid_mobile_no)
                    binding.ivValidMobile.setImageResource(R.drawable.ic_check_circle_grey)
                }
                if(s.isNullOrEmpty()){
                    binding.tilMobileNumber.error = null
                }
                isValidMobile = (s != null) && isValidMobileNumber(s.toString())
                if (isValidMobile)
                    parentViewModel.setMobileNumber(s.toString())
                binding.btnVerifyAadhaar.isEnabled = isValidAadhaar && isValidMobile
                        && (parentViewModel.consentChecked.value==true) // binding.aadharConsentCheckBox.isChecked
            }

        })

        binding.tietBenName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // intentionally left blank - not required for this use case
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // intentionally left blank - not required for this use case
            }

            override fun afterTextChanged(s: Editable?) {
                if((s != null) && HelperUtil.isValidName(s.toString())){
                    binding.tvErrorTextBenName.visibility = View.GONE
                    binding.tvErrorTextBenName.text = ""
                    binding.ivValidBenName.setImageResource(R.drawable.ic_check_circle_green)

                }else{
                    binding.tvErrorTextBenName.visibility = View.VISIBLE
                    binding.tvErrorTextBenName.text = getString(R.string.str_invalid_ben_name)
                    binding.ivValidBenName.setImageResource(R.drawable.ic_check_circle_grey)
                }
                isValidBenName = (s != null && s.length >= 3 && HelperUtil.isValidName(s.toString()))
                if (isValidBenName)
                    parentViewModel.setBeneficiaryName(s.toString())
                binding.btnVerifyAadhaar.isEnabled = isValidAadhaar && isValidMobile
                        && (parentViewModel.consentChecked.value==true) //isValidBenName //binding.aadharConsentCheckBox.isChecked
            }

        })


        // observing error message from parent and updating error text field
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvErrorText.visibility = View.VISIBLE
                binding.tvErrorText.text = it
                viewModel.resetErrorMessage()
            }
        }

        binding.tietAadhaarNumber.setInputType(android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD)
        binding.ivShowText.setBackgroundResource(R.drawable.ic_hide)
        binding.ivShowText.setOnClickListener {
            if (isPasswordVisible) {
                binding.tietAadhaarNumber.setInputType(android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                binding.ivShowText.setBackgroundResource(R.drawable.ic_show)
            }else{
                binding.tietAadhaarNumber.setInputType(android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD)
                binding.ivShowText.setBackgroundResource(R.drawable.ic_hide)
            }
            isPasswordVisible = !isPasswordVisible
            binding.tietAadhaarNumber.setSelection(binding.tietAadhaarNumber.text?.length!!)

        }

        parentViewModel.consentChecked.observe(viewLifecycleOwner){
            if (it ==true){
                binding.tietAadhaarNumber.text= viewModel.aadhaarNumber.value
                binding.btnVerifyAadhaar.isEnabled = isValidAadhaar && isValidMobile
                        && (parentViewModel.consentChecked.value==true)
                binding.aadharDisclaimer.isChecked = true
            }
        }
    }

    private fun verifyAadhaar() {
        Toast.makeText(requireContext(), parentViewModel.verificationType.value, Toast.LENGTH_SHORT)
            .show()
        parentViewModel.setAadhaarNumber(binding.tietAadhaarNumber.text.toString())
        when (parentViewModel.verificationType.value) {
            "OTP" -> viewModel.generateOtpClicked(binding.tietAadhaarNumber.text.toString())
//            "FP" -> rdServiceCapturePIDContract.launch(Unit)
        }
    }

    fun isValidMobileNumber(str: String?): Boolean {
        val regex = "^(\\+91[\\-\\s]?|0)?[6-9]\\d{9}$"
        val p: Pattern = Pattern.compile(regex)
        if (str == null) {
            return false
        }
        val m: Matcher = p.matcher(str)
        return m.matches()
    }
}