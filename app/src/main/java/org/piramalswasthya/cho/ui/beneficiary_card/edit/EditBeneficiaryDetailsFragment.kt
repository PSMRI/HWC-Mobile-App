package org.piramalswasthya.cho.ui.beneficiary_card.edit

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.dropdown_adapters.DropdownAdapter
import org.piramalswasthya.cho.adapter.model.DropdownList
import org.piramalswasthya.cho.databinding.FragmentEditBeneficiaryDetailsBinding
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.StatusOfWomanMaster
import org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity
import org.piramalswasthya.cho.utils.DateTimeUtil
import java.util.Locale
import android.text.InputFilter

@AndroidEntryPoint
class EditBeneficiaryDetailsFragment : Fragment() {

    private var _binding: FragmentEditBeneficiaryDetailsBinding? = null
    private val binding get() = requireNotNull(_binding) { "Binding is not initialized" }

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
            handleSpeechResult(result.data)
        }
        currentSpeechTarget = null
    }

    private fun handleSpeechResult(data: Intent?) {
        val spokenText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        spokenText?.let { text ->
            when (currentSpeechTarget) {
                SpeechTarget.LAST_NAME -> handleLastNameSpeech(text)
                SpeechTarget.PHONE_NUMBER -> handlePhoneNumberSpeech(text)
                null -> Unit
            }
        }
    }

    private fun handleLastNameSpeech(text: String) {
        val filtered = text.filter { it.isLetter() || it == ' ' || it == '-' }
        binding.etLastName.setText(filtered)
    }

    private fun handlePhoneNumberSpeech(text: String) {
        val digits = text.filter { it.isDigit() }.take(10)
        binding.etPhoneNumber.setText(digits)
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
        patientInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(ARG_PATIENT_INFO, PatientDisplayWithVisitInfo::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable(ARG_PATIENT_INFO) as? PatientDisplayWithVisitInfo
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

        patientInfo?.let { info ->
            viewModel.setPatientInfo(info)
        }

        setupUI()
        setupObservers()
        setupTextWatchers()
        setupClickListeners()
        setupNameFilters()
    }

    private fun setupNameFilters() {
        val nameFilter = InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                val char = source[i]
                if (!char.isLetter() && char != ' ' && char != '-') {
                    return@InputFilter ""
                }
            }
            null
        }
        binding.etLastName.filters = arrayOf(nameFilter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupUI() {
        patientInfo?.let { patient ->
            setupPatientName(patient)
            setupPatientId(patient)
            setupPatientPhoto(patient)
            setupPatientDetails(patient)
        }
    }

    private fun setupPatientName(patient: PatientDisplayWithVisitInfo) {
        val firstName = patient.patient.firstName.orEmpty()
        val lastName = patient.patient.lastName.orEmpty()
        binding.tvPatientName.text = listOf(firstName, lastName).joinToString(" ").trim()
    }

    private fun setupPatientId(patient: PatientDisplayWithVisitInfo) {
        val patientId = patient.patient.beneficiaryID?.toString() 
            ?: patient.patient.beneficiaryRegID?.toString() 
            ?: patient.patient.patientID
        binding.tvPatientId.text = getString(R.string.beneficiary_id_format, patientId)
    }

    private fun setupPatientPhoto(patient: PatientDisplayWithVisitInfo) {
        patient.patient.benImage?.let { imageString ->
            Glide.with(requireContext())
                .load(Uri.parse(imageString))
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(binding.ivPatientPhoto)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupPatientDetails(patient: PatientDisplayWithVisitInfo) {
        binding.etLastName.setText(patient.patient.lastName.orEmpty())
        binding.etPhoneNumber.setText(patient.patient.phoneNo.orEmpty())
        setupAgeFields(patient)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupAgeFields(patient: PatientDisplayWithVisitInfo) {
        patient.patient.dob?.let { dob ->
            val age = DateTimeUtil.calculateAgePicker(dob)
            binding.etAgeYears.setText(age.years.toString())
            if (age.months > 0) binding.etAgeMonths.setText(age.months.toString())
            if (age.days > 0) binding.etAgeDays.setText(age.days.toString())
        } ?: run {
            patient.patient.age?.let { age ->
                binding.etAgeYears.setText(age.toString())
            }
        }
    }

    private fun setupObservers() {
        setupStatusOfWomanObserver()
        setupSaveStateObserver()
        setupValidationObservers()
    }

    private fun setupStatusOfWomanObserver() {
        viewModel.filteredStatusOfWomanList.observe(viewLifecycleOwner) { list ->
            val shouldShow = viewModel.shouldShowStatusOfWoman() && list.isNotEmpty()
            binding.statusOfWomanText.isVisible = shouldShow
            if (shouldShow) {
                setupStatusOfWomanDropdown(list)
            }
        }
    }

    private fun setupSaveStateObserver() {
        viewModel.saveState.observe(viewLifecycleOwner) { state ->
            when (state) {
                EditBeneficiaryDetailsViewModel.SaveState.SUCCESS -> handleSaveSuccess()
                EditBeneficiaryDetailsViewModel.SaveState.ERROR -> handleSaveError()
                EditBeneficiaryDetailsViewModel.SaveState.IDLE -> Unit
                EditBeneficiaryDetailsViewModel.SaveState.SAVING -> Unit
                null -> Unit
            }
        }
    }

    private fun handleSaveSuccess() {
        Toast.makeText(
            requireContext(),
            getString(R.string.patient_edited_successfully_title),
            Toast.LENGTH_SHORT
        ).show()
        
        // Pass updated info back to BeneficiaryCardFragment
        viewModel.patientInfo.value?.let { updatedInfo ->
            // Set result for parent fragment to pick up
            parentFragmentManager.setFragmentResult(
                "beneficiary_updated",
                Bundle().apply {
                    putSerializable("updatedPatientInfo", updatedInfo)
                }
            )
        }
        
        handlePostSaveNavigation()
        viewModel.resetSaveState()
    }

    private fun handleSaveError() {
        showValidationErrors()
        viewModel.resetSaveState()
    }

    private fun setupValidationObservers() {
        viewModel.ageYearsValid.observe(viewLifecycleOwner) { isValid ->
            binding.ageYearsText.error = if (isValid == false) {
                getString(R.string.age_years_required)
            } else {
                null
            }
        }

        viewModel.phoneNumberValid.observe(viewLifecycleOwner) { isValid ->
            binding.phoneNumberText.error = if (isValid == false) {
                getString(R.string.invalid_phone_number)
            } else {
                null
            }
        }

        viewModel.statusOfWomanValid.observe(viewLifecycleOwner) { isValid ->
            if (viewModel.shouldShowStatusOfWoman()) {
                binding.statusOfWomanText.error = if (isValid == false) {
                    getString(R.string.status_of_woman_required)
                } else {
                    null
                }
            }
        }
    }

    private fun setupStatusOfWomanDropdown(list: List<StatusOfWomanMaster>) {
        val dropdownList = list.map { DropdownList(it.statusID, it.statusName) }
        val adapter = DropdownAdapter(
            requireContext(),
            R.layout.drop_down,
            dropdownList,
            binding.dropdownStatusOfWoman
        )
        binding.dropdownStatusOfWoman.setAdapter(adapter)

        viewModel.selectedStatusOfWoman?.let { selected ->
            binding.dropdownStatusOfWoman.setText(selected.statusName, false)
        }
        
        // Listen for async load of initial status
        viewModel.initialStatusOfWoman.observe(viewLifecycleOwner) { status ->
             status?.let {
                 binding.dropdownStatusOfWoman.setText(it.statusName, false)
             }
        }

        binding.dropdownStatusOfWoman.setOnItemClickListener { _, _, position, _ ->
            if (position in list.indices) {
                val selectedStatus = list[position]
                viewModel.selectedStatusOfWoman = selectedStatus
                binding.dropdownStatusOfWoman.setText(selectedStatus.statusName, false)
            }
        }
    }

    private fun setupTextWatchers() {
        binding.etLastName.addTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.lastName = s?.toString()?.trim()
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        binding.etPhoneNumber.addTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val phone = s?.toString()?.trim()
                viewModel.phoneNumber = phone
                phone?.let {
                    viewModel.validatePhoneNumber(it)
                } ?: run {
                    binding.phoneNumberText.error = null
                }
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        binding.etAgeYears.addTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val years = s?.toString()?.toIntOrNull()
                viewModel.ageYears = years
                viewModel.validateAgeYears(years)
                viewModel.updateFilteredStatusOfWomanList()
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        binding.etAgeMonths.addTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.ageMonths = s?.toString()?.toIntOrNull()
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        binding.etAgeDays.addTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.ageDays = s?.toString()?.toIntOrNull()
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupClickListeners() {
        binding.lastNameText.setEndIconOnClickListener {
            currentSpeechTarget = SpeechTarget.LAST_NAME
            startSpeechRecognition()
        }

        binding.phoneNumberText.setEndIconOnClickListener {
            currentSpeechTarget = SpeechTarget.PHONE_NUMBER
            startSpeechRecognition()
        }

        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

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
            Toast.makeText(
                requireContext(),
                getString(R.string.speech_recognition_error),
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
        // If in RegisterPatientActivity, finish the activity to return to HomeActivity
        if (requireActivity() is org.piramalswasthya.cho.ui.register_patient_activity.RegisterPatientActivity) {
            requireActivity().finish()
            return
        }

        val fragmentManager = parentFragmentManager
        if (fragmentManager.backStackEntryCount > 0) {
            val firstEntry = fragmentManager.getBackStackEntryAt(0)
            fragmentManager.popBackStack(firstEntry.id, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        } else {
            parentFragmentManager.popBackStack()
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private fun android.widget.EditText.addTextWatcher(textWatcher: TextWatcher) {
    this.addTextChangedListener(textWatcher)
}