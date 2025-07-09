package org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_id.find_abha

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentFindAbhaBinding
import org.piramalswasthya.cho.network.Abha
import org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_id.AadhaarIdViewModel
import org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_id.find_abha.FindAbhaViewModel
import org.piramalswasthya.cho.utils.HelperUtil
import java.util.regex.Matcher
import java.util.regex.Pattern


@AndroidEntryPoint
class FindAbhaFragment : Fragment() {

    private var _binding: FragmentFindAbhaBinding? = null
    private val binding: FragmentFindAbhaBinding
        get() = _binding!!

    private val parentViewModel: AadhaarIdViewModel by viewModels({ requireActivity() })

    private val viewModel: FindAbhaViewModel by viewModels()

    private lateinit var adapterType: ArrayAdapter<String>

    private var abhaList = mutableListOf<String>()
    private var abhaData = mutableListOf<Abha>()

    private var selectedAbhaIndex = 0

    var isValidMobile = false
    var isValidAbha = false
    var isConsent = false
    var isValidBenName = false
    private val aadhaarDisclaimer by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(resources.getString(R.string.individual_s_consent_for_creation_of_abha_number))
            .setMessage(resources.getString(R.string.aadhar_disclaimer_consent_text))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFindAbhaBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val intent = requireActivity().intent

        val benId = intent.getLongExtra("benId", 0)
        val benRegId = intent.getLongExtra("benRegId", 0)

        if (benId > 0) {
            viewModel.getBen(benId)
        }

        binding.btnSearchAbha.setOnClickListener {
            binding.abhaDropdown.setText("")
            binding.abhaDropdown.setAdapter(null)
            abhaList.clear()
            abhaData.clear()
            selectedAbhaIndex = 0
            val imm =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
            searchAbha()
        }

        binding.btnGenerateOtp.setOnClickListener {
            viewModel.generateOtpClicked(selectedAbhaIndex.toString())
            val imm =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }

        viewModel.state.observe(viewLifecycleOwner) {
            if (it == AadhaarIdViewModel.State.SUCCESS) {
                viewModel.resetState()
            }
//            parentViewModel.setState(it)
        }

        viewModel.fnlState.observe(viewLifecycleOwner) {
            if (it == AadhaarIdViewModel.State.SUCCESS) {
                viewModel.resetState()
            }
            parentViewModel.setAbhaMode(AadhaarIdViewModel.Abha.SEARCH)
            parentViewModel.setState(it)
        }

        viewModel.fnlTxnId.observe(viewLifecycleOwner) {
            it?.let {
                parentViewModel.setTxnId(it)
            }
        }

        viewModel.txnId.observe(viewLifecycleOwner) {
            it?.let {
                parentViewModel.setOtpTxnId(it)
            }
        }

        viewModel.abha.observe(viewLifecycleOwner) {
            binding.tvErrorTextAbha.visibility = View.GONE
            it?.let {
                binding.tilSelectAbha.isEnabled = true
                abhaData.addAll(it)
                it.forEach { abha ->
                    abhaList.add(abha.name + " : " + abha.ABHANumber)
                }
                adapterType = ArrayAdapter<String>(
                    requireContext(),
                    R.layout.dropdown_item_abha,
                    abhaList
                )
                binding.abhaDropdown.setAdapter(adapterType)
            }
        }

        binding.aadharConsentCheckBox.setOnCheckedChangeListener { _, ischecked ->
            isConsent = ischecked
            enableButton()
//            binding.btnGenerateOtp.isEnabled = isValidAbha && isValidMobile && ischecked
        }

        binding.aadharDisclaimer.setOnClickListener {
            if(parentViewModel.beneficiaryName.value!=null && !parentViewModel.beneficiaryName.value.isNullOrBlank()) {
                parentViewModel.navigateToAadhaarConsent(true)
            }else{
                Toast.makeText(requireContext(),"Please Enter Beneficiary Name", Toast.LENGTH_SHORT).show()
            }
           // aadhaarDisclaimer.show()
        }

        binding.tietMobileNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                isValidMobile = (s != null) && isValidMobileNumber(s.toString())

                if (isValidMobile) {
                    parentViewModel.setMobileNumber(s.toString())
                    binding.tilMobileNumber.error = null
                    binding.btnSearchAbha.isEnabled = isValidMobile
                    enableButton()
                    binding.ivValidMobile.setImageResource(R.drawable.ic_check_circle_green)

                }else{
                    binding.tilMobileNumber.error = getString(R.string.str_invalid_mobile_no)
                    binding.btnSearchAbha.isEnabled = isValidMobile
                    binding.ivValidMobile.setImageResource(R.drawable.ic_check_circle_grey)
                }
                if(s.isNullOrEmpty()){
                    binding.tilMobileNumber.error = null
                }
                binding.tvErrorTextAbha.visibility = View.GONE
            }

        })

        (binding.tilSelectAbha.getEditText() as AutoCompleteTextView).onItemClickListener =
            OnItemClickListener { adapterView, view, position, id ->
                selectedAbhaIndex = abhaData[position].index
                parentViewModel.setSelectedAbhaIndex(selectedAbhaIndex.toString())
                isValidAbha = true
                enableButton()
            }

        // observing error message from parent and updating error text field
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvErrorTextAbha.visibility = View.VISIBLE
                binding.tvErrorTextAbha.text = "No ABHA Found"
                viewModel.resetErrorMessage()
            }
        }

        viewModel.ben.observe(viewLifecycleOwner) {
            if(it!=null){
                parentViewModel.setBeneficiaryName(it)
                binding.clBenName.visibility = View.GONE

            }else{
              //  binding.clBenName.visibility = View.VISIBLE
            }

        }
        binding.tietBenName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if((s != null&& HelperUtil.isValidName(s.toString()))){
                    binding.tvErrorTextBenName.visibility = View.GONE
                    binding.tvErrorTextBenName.text = ""
                    binding.ivValidBenName.setImageResource(R.drawable.ic_check_circle_green)

                }else{
                    binding.tvErrorTextBenName.visibility = View.VISIBLE
                    binding.tvErrorTextBenName.text = getString(R.string.str_invalid_mobile_no)
                    binding.ivValidBenName.setImageResource(R.drawable.ic_check_circle_grey)
                }

                isValidBenName = (s != null && s.length >= 3 && HelperUtil.isValidName(s.toString()))
                if (isValidBenName)
                    parentViewModel.setBeneficiaryName(s.toString())
                binding.btnGenerateOtp.isEnabled = isValidAbha && isValidMobile //&& (parentViewModel.consentChecked.value==true)

            }

        })

        viewModel.otpMobileNumberMessage.observe(viewLifecycleOwner) {
            it?.let {
                parentViewModel.setOTPMsg(it)
            }
        }

        parentViewModel.consentChecked.observe(viewLifecycleOwner){
            if (it ==true){
//                binding.btnGenerateOtp.isEnabled = isValidAbha && isValidMobile && (parentViewModel.consentChecked.value==true)
            }
        }
    }

    private fun searchAbha() {
        viewModel.searchAbhaClicked(binding.tietMobileNumber.text.toString())
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

    private fun enableButton() {
        if (isValidAbha && isValidMobile) {
            binding.btnGenerateOtp.isEnabled = true
        } else {
            binding.btnGenerateOtp.isEnabled = false
        }
    }
}