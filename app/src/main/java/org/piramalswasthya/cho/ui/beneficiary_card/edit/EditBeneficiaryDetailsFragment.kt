package org.piramalswasthya.cho.ui.beneficiary_card.edit

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.dropdown_adapters.DropdownAdapter
import org.piramalswasthya.cho.adapter.model.DropdownList
import org.piramalswasthya.cho.databinding.FragmentEditBeneficiaryDetailsBinding
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity
import java.util.Locale

@AndroidEntryPoint
class EditBeneficiaryDetailsFragment : Fragment() {

    private var _binding: FragmentEditBeneficiaryDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditBeneficiaryDetailsViewModel by viewModels()

    private var patientInfo: PatientDisplayWithVisitInfo? = null
    private var currentSpeechTarget: SpeechTarget? = null

    private enum class SpeechTarget {
        LAST_NAME,
        PHONE_NUMBER
    }

    private val speechRecognizerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            spokenText?.let { text ->
                when (currentSpeechTarget) {
                    SpeechTarget.LAST_NAME -> {
                        // Filter to only alphabets, spaces, and hyphens
                        val filtered = text.filter { it.isLetter() || it == ' ' || it == '-' }
                        binding.etLastName.setText(filtered)
                    }
                    SpeechTarget.PHONE_NUMBER -> {
                        // Extract only digits
                        val digits = text.filter { it.isDigit() }.take(10)
                        binding.etPhoneNumber.setText(digits)
                    }

                    else -> {
                        // No action needed for IDLE or SAVING states
                    }
                }
            }
        }
        currentSpeechTarget = null
    }

    companion object {
        private const val ARG_PATIENT_INFO = "patientInfo"

        fun newInstance(patientInfo: PatientDisplayWithVisitInfo): EditBeneficiaryDetailsFragment {
            return EditBeneficiaryDetailsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PATIENT_INFO, patientInfo)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set window soft input mode
        activity?.window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        
        arguments?.let {
            @Suppress("DEPRECATION")
            patientInfo = it.getSerializable(ARG_PATIENT_INFO) as? PatientDisplayWithVisitInfo
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditBeneficiaryDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        patientInfo?.let {
            viewModel.setPatientInfo(it)
        }

        setupUI()
        setupObservers()
        setupTextWatchers()
        setupClickListeners()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupUI() {
        patientInfo?.let { patient ->
            // Set patient info summary
            val firstName = patient.patient.firstName ?: ""
            val lastName = patient.patient.lastName ?: ""
            binding.tvPatientName.text = "$firstName $lastName".trim()
            binding.tvPatientId.text = "ID: ${patient.patient.beneficiaryID ?: patient.patient.patientID}"

            // Load patient photo
            patient.patient.benImage?.let { imageString ->
                Glide.with(this)
                    .load(Uri.parse(imageString))
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .into(binding.ivPatientPhoto)
            }

            // Pre-fill editable fields
            binding.etLastName.setText(patient.patient.lastName ?: "")
            binding.etPhoneNumber.setText(patient.patient.phoneNo ?: "")

            // Pre-fill age
            patient.patient.dob?.let { dob ->
                val age = org.piramalswasthya.cho.utils.DateTimeUtil.calculateAgePicker(dob)
                binding.etAgeYears.setText(age.years.toString())
                if (age.months > 0) binding.etAgeMonths.setText(age.months.toString())
                if (age.days > 0) binding.etAgeDays.setText(age.days.toString())
            } ?: run {
                patient.patient.age?.let { age ->
                    binding.etAgeYears.setText(age.toString())
                }
            }
        }
    }

    private fun setupObservers() {
        viewModel.filteredStatusOfWomanList.observe(viewLifecycleOwner) { list ->
            if (list.isNotEmpty() && viewModel.shouldShowStatusOfWoman()) {
                binding.statusOfWomanText.visibility = View.VISIBLE
                setupStatusOfWomanDropdown(list)
            } else {
                binding.statusOfWomanText.visibility = View.GONE
            }
        }

        viewModel.saveState.observe(viewLifecycleOwner) { state ->
            handleSaveState(state)
        }

        viewModel.ageYearsValid.observe(viewLifecycleOwner) { isValid ->
            if (!isValid) {
                binding.ageYearsText.error = getString(R.string.age_years_required)
            } else {
                binding.ageYearsText.error = null
            }
        }

        viewModel.phoneNumberValid.observe(viewLifecycleOwner) { isValid ->
            if (!isValid) {
                binding.phoneNumberText.error = getString(R.string.invalid_phone_number)
            } else {
                binding.phoneNumberText.error = null
            }
        }

        viewModel.statusOfWomanValid.observe(viewLifecycleOwner) { isValid ->
            if (!isValid && viewModel.shouldShowStatusOfWoman()) {
                binding.statusOfWomanText.error = getString(R.string.status_of_woman_required)
            } else {
                binding.statusOfWomanText.error = null
            }
        }
    }

    private fun handleSaveState(state: EditBeneficiaryDetailsViewModel.SaveState) {
        when (state) {
            EditBeneficiaryDetailsViewModel.SaveState.SUCCESS -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.beneficiary_updated_successfully),
                    Toast.LENGTH_SHORT
                ).show()
                handlePostSaveNavigation()
                viewModel.resetSaveState()
            }
            EditBeneficiaryDetailsViewModel.SaveState.ERROR -> {
                showValidationErrors()
                viewModel.resetSaveState()
            }
            else -> {
                // No action needed for IDLE or SAVING states
            }
        }
    }

    private fun setupStatusOfWomanDropdown(list: List<org.piramalswasthya.cho.model.StatusOfWomanMaster>) {
        val dropdownList = list.map { DropdownList(it.statusID, it.statusName) }
        val adapter = DropdownAdapter(
            requireContext(),
            R.layout.drop_down,
            dropdownList,
            binding.dropdownStatusOfWoman
        )
        binding.dropdownStatusOfWoman.setAdapter(adapter)

        // Pre-select if patient already has a status
        viewModel.selectedStatusOfWoman?.let { selected ->
            binding.dropdownStatusOfWoman.setText(selected.statusName, false)
        }

        binding.dropdownStatusOfWoman.setOnItemClickListener { _, _, position, _ ->
            if (position >= 0 && position < list.size) {
                viewModel.selectedStatusOfWoman = list[position]
                binding.dropdownStatusOfWoman.setText(list[position].statusName, false)
            }
        }
    }

    private fun setupTextWatchers() {
        binding.etLastName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed - we only handle onTextChanged
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.lastName = s?.toString()?.trim()
            }
            override fun afterTextChanged(s: Editable?) {
                // Not needed - we only handle onTextChanged
            }
        })

        binding.etPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed - we only handle onTextChanged
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val phone = s?.toString()?.trim()
                viewModel.phoneNumber = phone
                if (!phone.isNullOrEmpty()) {
                    viewModel.validatePhoneNumber(phone)
                } else {
                    binding.phoneNumberText.error = null
                }
            }
            override fun afterTextChanged(s: Editable?) {
                // Not needed - we only handle onTextChanged
            }
        })

        binding.etAgeYears.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed - we only handle onTextChanged
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val years = s?.toString()?.toIntOrNull()
                viewModel.ageYears = years
                viewModel.validateAgeYears(years)
                // Update Status of Woman options when age changes
                viewModel.updateFilteredStatusOfWomanList()
            }
            override fun afterTextChanged(s: Editable?) {
                // Not needed - we only handle onTextChanged
            }
        })

        binding.etAgeMonths.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed - we only handle onTextChanged
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.ageMonths = s?.toString()?.toIntOrNull()
            }
            override fun afterTextChanged(s: Editable?) {
                // Not needed - we only handle onTextChanged
            }
        })

        binding.etAgeDays.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed - we only handle onTextChanged
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.ageDays = s?.toString()?.toIntOrNull()
            }
            override fun afterTextChanged(s: Editable?) {
                // Not needed - we only handle onTextChanged
            }
        })
    }

    private fun setupClickListeners() {
        // Speech-to-text for Last Name
        binding.lastNameText.setEndIconOnClickListener {
            currentSpeechTarget = SpeechTarget.LAST_NAME
            startSpeechRecognition()
        }

        // Speech-to-text for Phone Number
        binding.phoneNumberText.setEndIconOnClickListener {
            currentSpeechTarget = SpeechTarget.PHONE_NUMBER
            startSpeechRecognition()
        }

        // Cancel button
        binding.btnCancel.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        // Save button
        binding.btnSave.setOnClickListener {
            viewModel.saveChanges()
        }
    }

    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speak_now))
        }

        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e("EditBeneficiaryDetailsFragment", "Speech recognition unavailable", e)
            Toast.makeText(
                requireContext(),
                "Speech recognition not available",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showValidationErrors() {
        if (viewModel.ageYearsValid.value != true) {
            binding.ageYearsText.error = getString(R.string.age_years_required)
        }
        if (viewModel.phoneNumberValid.value != true) {
            binding.phoneNumberText.error = getString(R.string.invalid_phone_number)
        }
        if (viewModel.statusOfWomanValid.value != true && viewModel.shouldShowStatusOfWoman()) {
            binding.statusOfWomanText.error = getString(R.string.status_of_woman_required)
        }
    }

    private fun handlePostSaveNavigation() {
        val statusOfWomanChanged = viewModel.statusOfWomanChanged.value == true
        val newStatusId = viewModel.getNewStatusOfWomanId()

        if (statusOfWomanChanged && newStatusId != null) {
            // Navigate to appropriate screen based on new Status of Woman
            viewModel.patientInfo.value?.let { patient ->
                val intent = Intent(requireContext(), EditPatientDetailsActivity::class.java)
                intent.putExtra("benVisitInfo", patient)

                when (newStatusId) {
                    1 -> intent.putExtra("navigateToEC", true)  // Eligible Couple
                    2 -> intent.putExtra("navigateToPW", true)  // Pregnant Woman
                    3 -> intent.putExtra("navigateToPN", true)  // Postnatal
                }

                startActivity(intent)
            }
        }

        activity?.supportFragmentManager?.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
