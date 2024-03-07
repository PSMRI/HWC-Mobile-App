package org.piramalswasthya.cho.ui.register_patient_activity.location_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.dropdown_adapters.BlockAdapter
import org.piramalswasthya.cho.adapter.dropdown_adapters.DistrictAdapter
import org.piramalswasthya.cho.adapter.dropdown_adapters.StatesAdapter
import org.piramalswasthya.cho.adapter.dropdown_adapters.VillageAdapter
import org.piramalswasthya.cho.databinding.FragmentLocationBinding
//import org.piramalswasthya.cho.databinding.FragmentLoginSettingBinding
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.model.Patient

@AndroidEntryPoint
class LocationFragment : Fragment() , NavigationAdapter {


    private val binding by lazy{
        FragmentLocationBinding.inflate(layoutInflater)
    }

    private lateinit var viewModel: LocationViewModel


    private var patient : Patient? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }
//
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        patient = arguments?.getSerializable("patient") as? Patient

        viewModel = ViewModelProvider(this).get(LocationViewModel::class.java)

        binding.dropdownTaluk.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectedBlock = viewModel.blockList[position]
            viewModel.updateUserBlockId(viewModel.selectedBlock!!.blockID)
            viewModel.fetchVillageMaster(viewModel.selectedBlock!!.blockID)
            binding.dropdownTaluk.setText(viewModel.selectedBlock!!.blockName,false)
        }

        binding.dropdownPanchayat.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectedVillage = viewModel.villageList[position]
            viewModel.updateUserVillageId(viewModel.selectedVillage!!.districtBranchID)
            binding.dropdownPanchayat.setText(viewModel.selectedVillage!!.villageName,false)
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                LocationViewModel.NetworkState.SUCCESS -> {
                    val statesAdapter = StatesAdapter(requireContext(), R.layout.drop_down, emptyList(), binding.dropdownState)
                    binding.dropdownState.setAdapter(statesAdapter)

                    if(viewModel.selectedState != null){
                        binding.dropdownState.setText(viewModel.selectedState!!.stateName, false)
                    }
                    else{
                        binding.dropdownState.setText("", false)
                    }
                }
                else -> {}
            }
        }

        viewModel.district.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                LocationViewModel.NetworkState.SUCCESS -> {
                    val districtAdapter = DistrictAdapter(requireContext(), R.layout.drop_down, emptyList(), binding.dropdownDist)
                    binding.dropdownDist.setAdapter(districtAdapter)

                    if(viewModel.selectedDistrict != null){
                        binding.dropdownDist.setText(viewModel.selectedDistrict!!.districtName, false)
                    }
                    else{
                        binding.dropdownDist.setText("", false)
                    }
                }
                else -> {}
            }
        }

        viewModel.block.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                LocationViewModel.NetworkState.SUCCESS -> {
                    val blockAdapter = BlockAdapter(requireContext(), R.layout.drop_down, viewModel.blockList, binding.dropdownTaluk)
                    binding.dropdownTaluk.setAdapter(blockAdapter)

                    if(viewModel.selectedBlock != null){
                        binding.dropdownTaluk.setText(viewModel.selectedBlock!!.blockName, false)
                    }
                    else{
                        binding.dropdownTaluk.setText("", false)
                    }
                }
                else -> {}
            }
        }

        viewModel.village.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                LocationViewModel.NetworkState.SUCCESS -> {
                    val villageAdapter = VillageAdapter(requireContext(), R.layout.drop_down, viewModel.villageList, binding.dropdownPanchayat)
                    binding.dropdownPanchayat.setAdapter(villageAdapter)

                    if(viewModel.selectedVillage != null){
                        binding.dropdownPanchayat.setText(viewModel.selectedVillage!!.villageName, false)
                    }
                    else{
                        binding.dropdownPanchayat.setText("", false)
                    }
                }
                else -> {}
            }
        }

    }

    override fun getFragmentId(): Int {
        return R.id.fragment_add_patient_location;
    }

    override fun onSubmitAction() {
        addPatientLocationDetalis()
        val bundle = Bundle()
        bundle.putSerializable("patient", patient)
        findNavController().navigate(
            R.id.action_fragmentLocation_to_otherInformationsFragment, bundle
        )
    }


    private fun addPatientLocationDetalis(){
        if(viewModel.selectedState != null && patient != null){
            patient!!.stateID = viewModel.selectedState!!.stateID;
        }

        if(viewModel.selectedDistrict != null && patient != null){
            patient!!.districtID = viewModel.selectedDistrict!!.districtID;
        }

        if(viewModel.selectedBlock != null && patient != null){
            patient!!.blockID = viewModel.selectedBlock!!.blockID;
        }

        if(viewModel.selectedVillage != null && patient != null){
            patient!!.districtBranchID = viewModel.selectedVillage!!.districtBranchID;
        }
    }

    private fun addPatientOtherDetalis(){

    }

    override fun onCancelAction() {
        findNavController().navigateUp()
    }


}