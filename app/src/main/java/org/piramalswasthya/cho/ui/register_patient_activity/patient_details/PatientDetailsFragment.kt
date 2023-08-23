package org.piramalswasthya.cho.ui.register_patient_activity.patient_details

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.dropdown_adapters.DropdownAdapter
import org.piramalswasthya.cho.adapter.dropdown_adapters.VillageAdapter
import org.piramalswasthya.cho.adapter.model.DropdownList
import org.piramalswasthya.cho.databinding.FragmentLocationBinding
import org.piramalswasthya.cho.databinding.FragmentPatientDetailsBinding
import org.piramalswasthya.cho.model.AgeUnit

@AndroidEntryPoint
class PatientDetailsFragment : Fragment() {

    private val binding by lazy{
        FragmentPatientDetailsBinding.inflate(layoutInflater)
    }

    private lateinit var viewModel: PatientDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_patient_details, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[PatientDetailsViewModel::class.java]
        setChangeListeners()
        setAdapters()
    }

    private fun setChangeListeners(){
        binding.ageInUnitDropdown.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectedAgeUnit = viewModel.ageUnitList[position];
            binding.ageInUnitDropdown.setText(viewModel.selectedAgeUnit!!.name)
        }

        binding.maritalStatusDropdown.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectedMaritalStatus = viewModel.maritalStatusList[position];
            binding.maritalStatusDropdown.setText(viewModel.selectedMaritalStatus!!.status)
        }

        binding.genderDropdown.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectedGenderMaster = viewModel.genderMasterList[position];
            binding.genderDropdown.setText(viewModel.selectedGenderMaster!!.genderName)
        }
    }

    private fun setAdapters(){
        viewModel.ageUnit.observe(viewLifecycleOwner) { state ->
            when (state!!){
                PatientDetailsViewModel.NetworkState.SUCCESS -> {
                    val dropdownList = viewModel.ageUnitList.map { it -> DropdownList(it.id, it.name) }
                    val dropdownAdapter = DropdownAdapter(requireContext(), R.layout.drop_down, dropdownList)
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
                    val dropdownAdapter = DropdownAdapter(requireContext(), R.layout.drop_down, dropdownList)
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
                    val dropdownAdapter = DropdownAdapter(requireContext(), R.layout.drop_down, dropdownList)
                    binding.genderDropdown.setAdapter(dropdownAdapter)
                }
                else -> {

                }
            }
        }
    }

}