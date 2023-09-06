package org.piramalswasthya.cho.ui.register_patient_activity.patient_details

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.dropdown_adapters.DropdownAdapter
import org.piramalswasthya.cho.adapter.dropdown_adapters.VillageAdapter
import org.piramalswasthya.cho.adapter.model.DropdownList
import org.piramalswasthya.cho.databinding.FragmentLocationBinding
import org.piramalswasthya.cho.databinding.FragmentPatientDetailsBinding
import org.piramalswasthya.cho.model.AgeUnit
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.ui.login_activity.login_settings.LoginSettingsViewModel
import org.piramalswasthya.cho.ui.register_patient_activity.location_details.LocationFragmentDirections
import org.piramalswasthya.cho.utils.AgeUnitEnum
import org.piramalswasthya.cho.utils.DateTimeUtil
import java.util.Calendar
import java.util.Date

@AndroidEntryPoint
class PatientDetailsFragment : Fragment() , NavigationAdapter {


    private val binding by lazy{
        FragmentPatientDetailsBinding.inflate(layoutInflater)
    }

    private lateinit var viewModel: PatientDetailsViewModel

    private var doAgeToDob = true;

    private var patient = Patient();

    private val dobUtil : DateTimeUtil = DateTimeUtil()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this).get(PatientDetailsViewModel::class.java)
        binding.registrationDate.setText(DateTimeUtil.formattedDate(Date()))
        hideMarriedFields()
        setChangeListeners()
        setAdapters()
    }

    private fun hideMarriedFields(){
        binding.spouseNameText.visibility = View.GONE
        binding.ageAtMarriageText.visibility = View.GONE
    }

    private fun setMarriedFieldsVisibility(){
        if(viewModel.selectedMaritalStatus != null && viewModel.selectedMaritalStatus!!.status.lowercase() == "married"){
            binding.spouseNameText.visibility = View.VISIBLE
            binding.ageAtMarriageText.visibility = View.VISIBLE
        }
        else{
            binding.spouseNameText.visibility = View.GONE
            binding.ageAtMarriageText.visibility = View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setChangeListeners(){
        binding.ageInUnitDropdown.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectedAgeUnit = viewModel.ageUnitList[position];
            viewModel.selectedAgeUnitEnum = viewModel.ageUnitEnumMap[viewModel.selectedAgeUnit]
            binding.ageInUnitDropdown.setText(viewModel.selectedAgeUnit!!.name, false)
            setAgeToDateOfBirth()
        }

        binding.maritalStatusDropdown.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectedMaritalStatus = viewModel.maritalStatusList[position];
            binding.maritalStatusDropdown.setText(viewModel.selectedMaritalStatus!!.status, false)
            setMarriedFieldsVisibility()
        }

        binding.genderDropdown.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectedGenderMaster = viewModel.genderMasterList[position];
            binding.genderDropdown.setText(viewModel.selectedGenderMaster!!.genderName, false)
        }

        binding.dateOfBirth.setOnClickListener {
            dobUtil.showDatePickerDialog(requireContext(), viewModel.selectedDateOfBirth)
        }

        binding.age.addTextChangedListener(ageTextWatcher)

    }

    private val ageTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            setAgeToDateOfBirth()
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setAdapters(){
        viewModel.ageUnit.observe(viewLifecycleOwner) { state ->
            when (state!!){
                PatientDetailsViewModel.NetworkState.SUCCESS -> {
                    val dropdownList = viewModel.ageUnitList.map { it -> DropdownList(it.id, it.name) }
                    val dropdownAdapter = DropdownAdapter(requireContext(), R.layout.drop_down, dropdownList, binding.ageInUnitDropdown)
                    binding.ageInUnitDropdown.setAdapter(dropdownAdapter)
                }
                else -> {

                }
            }
        }

        viewModel.maritalStatus.observe(viewLifecycleOwner) { state ->
            when (state!!){
                PatientDetailsViewModel.NetworkState.SUCCESS -> {
                    val dropdownList = viewModel.maritalStatusList.map { it -> DropdownList(it.maritalStatusID, it.status) }
                    val dropdownAdapter = DropdownAdapter(requireContext(), R.layout.drop_down, dropdownList, binding.maritalStatusDropdown)
                    binding.maritalStatusDropdown.setAdapter(dropdownAdapter)
                }
                else -> {

                }
            }
        }

        viewModel.genderMaster.observe(viewLifecycleOwner) { state ->
            when (state!!){
                PatientDetailsViewModel.NetworkState.SUCCESS -> {
                    val dropdownList = viewModel.genderMasterList.map { it -> DropdownList(it.genderID, it.genderName) }
                    val dropdownAdapter = DropdownAdapter(requireContext(), R.layout.drop_down, dropdownList, binding.genderDropdown)
                    binding.genderDropdown.setAdapter(dropdownAdapter)
                }
                else -> {

                }
            }
        }

        dobUtil.selectedDate.observe(viewLifecycleOwner) { date ->
            if(date != null){
                setDateOfBirthToAge(date);
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setAgeToDateOfBirth(){
        viewModel.enteredAge = binding.age.text.toString().trim().toIntOrNull()
        if(viewModel.enteredAge != null && viewModel.selectedAgeUnitEnum != null && doAgeToDob){
            viewModel.selectedDateOfBirth = DateTimeUtil.calculateDateOfBirth(viewModel.enteredAge!!, viewModel.selectedAgeUnitEnum!!);
            binding.dateOfBirth.setText(DateTimeUtil.formattedDate(viewModel.selectedDateOfBirth!!))
        }
        doAgeToDob = true;
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun setDateOfBirthToAge(date: Date){
        val age = DateTimeUtil.calculateAge(date);
        viewModel.enteredAge = age.value
        viewModel.selectedDateOfBirth = date
        viewModel.selectedAgeUnitEnum = age.unit
        viewModel.selectedAgeUnit = viewModel.ageUnitMap[age.unit]
        doAgeToDob = false;
        binding.age.setText(age.value.toString())
        binding.ageInUnitDropdown.setText(viewModel.ageUnitMap[age.unit]?.name ?: "", false)
        binding.dateOfBirth.setText(DateTimeUtil.formattedDate(date))
    }

    fun setPatientDetails(){
        patient.firstName = binding.firstName.text.toString().trim()
        patient.lastName = binding.lastName.text.toString().trim()
        patient.dob = viewModel.selectedDateOfBirth;
        patient.age = viewModel.enteredAge;
        patient.ageUnitID = viewModel.selectedAgeUnit?.id
        patient.maritalStatusID = viewModel.selectedMaritalStatus?.maritalStatusID
        patient.spouseName = when(viewModel.selectedMaritalStatus?.status?.lowercase()){
            "married" -> binding.spouseName.text.toString();
            else -> null
        }
        patient.ageAtMarriage = when(viewModel.selectedMaritalStatus?.status?.lowercase()){
            "married" -> binding.ageAtMarriage.text.toString().toIntOrNull();
            else -> null
        }
        patient.phoneNo = binding.phoneNo.text.toString()
        patient.genderID = viewModel.selectedGenderMaster?.genderID
        patient.registrationDate = Date()
    }

    override fun getFragmentId(): Int {
        return R.id.fragment_add_patient_location;
    }

    override fun onSubmitAction() {
        setPatientDetails()
        val bundle = Bundle()
        bundle.putSerializable("patient", patient)
        findNavController().navigate(
            R.id.action_patientDetailsFragment_to_fragmentLocation, bundle
        )
    }

    override fun onCancelAction() {
        val intent = Intent(context, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

}