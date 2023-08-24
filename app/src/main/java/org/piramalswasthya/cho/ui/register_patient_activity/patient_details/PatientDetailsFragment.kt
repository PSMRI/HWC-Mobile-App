package org.piramalswasthya.cho.ui.register_patient_activity.patient_details

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.dropdown_adapters.DropdownAdapter
import org.piramalswasthya.cho.adapter.dropdown_adapters.VillageAdapter
import org.piramalswasthya.cho.adapter.model.DropdownList
import org.piramalswasthya.cho.databinding.FragmentLocationBinding
import org.piramalswasthya.cho.databinding.FragmentPatientDetailsBinding
import org.piramalswasthya.cho.model.AgeUnit
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this).get(PatientDetailsViewModel::class.java)
        setChangeListeners()
        setAdapters()
    }

//    private fun showDatePickerDialog() {
//
//        val calendar: Calendar = Calendar.getInstance()
//        selectedDate?.let { calendar.time = it }
//
//        val year = calendar.get(Calendar.YEAR)
//        val month = calendar.get(Calendar.MONTH)
//        val day = calendar.get(Calendar.DAY_OF_MONTH)
//
//        val datePickerDialog = DatePickerDialog(requireContext(),
//            { view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
//                // This callback is called when the user selects a date
//                calendar.set(Calendar.YEAR, year)
//                calendar.set(Calendar.MONTH, monthOfYear)
//                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
//
//                selectedDate = calendar.time
//                val selectedDateText = "$year-${monthOfYear + 1}-$dayOfMonth"
//                binding.dateOfBirth.setText(selectedDateText)
//            }, year, month, day)
//
//        datePickerDialog.show()
//    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setChangeListeners(){
        binding.ageInUnitDropdown.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectedAgeUnit = viewModel.ageUnitList[position];
            binding.ageInUnitDropdown.setText(viewModel.selectedAgeUnit!!.name, false)
        }

        binding.maritalStatusDropdown.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectedMaritalStatus = viewModel.maritalStatusList[position];
            binding.maritalStatusDropdown.setText(viewModel.selectedMaritalStatus!!.status, false)
        }

        binding.genderDropdown.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectedGenderMaster = viewModel.genderMasterList[position];
            binding.genderDropdown.setText(viewModel.selectedGenderMaster!!.genderName, false)
        }

        binding.dateOfBirth.setOnClickListener {
            DateTimeUtil.showDatePickerDialog(requireContext(), viewModel.selectedDate)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setAdapters(){
        viewModel.ageUnit.observe(viewLifecycleOwner) { state ->
            when (state!!){
                PatientDetailsViewModel.NetworkState.SUCCESS -> {
                    val dropdownList = viewModel.ageUnitList.map { it -> DropdownList(it.id, it.name) }
                    val dropdownAdapter = DropdownAdapter(requireContext(), R.layout.drop_down, dropdownList, binding.ageInUnitDropdown)
                    binding.ageInUnitDropdown.setAdapter(dropdownAdapter)
                    setAgeUnitMap()
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

        DateTimeUtil.selectedDate.observe(viewLifecycleOwner) { date ->
            if(date != null){
                viewModel.selectedDate = date;
                binding.dateOfBirth.setText(DateTimeUtil.formattedDate(date))
                setAge(date);
            }
        }

    }

    fun setAgeUnitMap(){
        viewModel.ageUnitList.forEach {
            when(it.name.first().lowercaseChar()){
                'd' -> viewModel.ageUnitMap[AgeUnitEnum.DAYS] = it
                'w' -> viewModel.ageUnitMap[AgeUnitEnum.WEEKS] = it
                'm' -> viewModel.ageUnitMap[AgeUnitEnum.MONTHS] = it
                'y' -> viewModel.ageUnitMap[AgeUnitEnum.YEARS] = it
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setAge(date: Date){
        val age = DateTimeUtil.calculateAge(date);
        binding.age.setText(age.value.toString());
        viewModel.selectedAgeUnit = viewModel.ageUnitMap[age.unit]
        binding.ageInUnitDropdown.setText(viewModel.selectedAgeUnit?.name ?: "", false)
    }

    override fun getFragmentId(): Int {
        return R.id.fragment_add_patient_location;
    }

    override fun onSubmitAction() {
        findNavController().navigate(
            PatientDetailsFragmentDirections.actionPatientDetailsFragmentToFragmentLocation()
        )
    }

    override fun onCancelAction() {
        val intent = Intent(context, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

}