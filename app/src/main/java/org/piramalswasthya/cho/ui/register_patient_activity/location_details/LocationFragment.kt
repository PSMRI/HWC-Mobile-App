package org.piramalswasthya.cho.ui.register_patient_activity.location_details

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.ResourceType
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.dropdown_adapters.BlockAdapter
import org.piramalswasthya.cho.adapter.dropdown_adapters.DistrictAdapter
import org.piramalswasthya.cho.adapter.dropdown_adapters.StatesAdapter
import org.piramalswasthya.cho.adapter.dropdown_adapters.VillageAdapter
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.databinding.FragmentLocationBinding
//import org.piramalswasthya.cho.databinding.FragmentLoginSettingBinding
import org.piramalswasthya.cho.model.BlockMaster
import org.piramalswasthya.cho.model.DistrictMaster
import org.piramalswasthya.cho.model.StateMaster
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.model.VillageMaster
import org.piramalswasthya.cho.patient.patient
import org.piramalswasthya.cho.repositories.BlockMasterRepo
import org.piramalswasthya.cho.repositories.DistrictMasterRepo
import org.piramalswasthya.cho.repositories.StateMasterRepo
import org.piramalswasthya.cho.repositories.VillageMasterRepo
import org.piramalswasthya.cho.fhir_utils.FhirExtension
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import timber.log.Timber
import org.piramalswasthya.cho.fhir_utils.extension_names.*
import org.piramalswasthya.cho.ui.login_activity.login_settings.LoginSettingsViewModel
import javax.inject.Inject

@AndroidEntryPoint
class LocationFragment : Fragment() , NavigationAdapter {

    @Inject
    lateinit var stateMasterRepo: StateMasterRepo

    @Inject
    lateinit var districtMasterRepo: DistrictMasterRepo

    @Inject
    lateinit var blockMasterRepo: BlockMasterRepo

    @Inject
    lateinit var villageMasterRepo: VillageMasterRepo

    @Inject
    lateinit var userDao: UserDao

    private var userInfo: UserCache? = null

    private val binding by lazy{
        FragmentLocationBinding.inflate(layoutInflater)
    }

    private lateinit var viewModel: LocationViewModel

    private val extension: FhirExtension = FhirExtension(ResourceType.Patient)

    private var stateMap: Map<Int, String>? = null
    private var districtMap: Map<Int, String>? = null
    private var blockMap: Map<Int, String>? = null
    private var villageMap: Map<Int, String>? = null

    private var selectedState: StateMaster? = null
    private var selectedDistrict: DistrictMaster? = null
    private var selectedBlock: BlockMaster? = null
    private var selectedVillage: VillageMaster? = null

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
//
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }
//
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        lifecycleScope.launch {
//            async { userInfo = userDao.getLoggedInUser() }.await()
//            Log.i("states fetching", "not done")
//            fetchStates()
//        }
        viewModel = ViewModelProvider(this).get(LocationViewModel::class.java)

//        binding.dropdownState.setOnItemClickListener { parent, _, position, _ ->
//            viewModel.selectedState = viewModel.stateList[position]
//            viewModel.updateUserStateId(viewModel.selectedState!!.stateID)
//            viewModel.fetchDistricts(viewModel.selectedState!!.stateID)
//            binding.dropdownState.setText(viewModel.selectedState!!.stateName,false)
//        }
//
//        binding.dropdownDist.setOnItemClickListener { parent, _, position, _ ->
//            viewModel.selectedDistrict = viewModel.districtList[position]
//            viewModel.updateUserDistrictId(viewModel.selectedDistrict!!.districtID)
//            viewModel.fetchTaluks(viewModel.selectedDistrict!!.districtID)
//            binding.dropdownDist.setText(viewModel.selectedDistrict!!.districtName,false)
//        }

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
        addPatientOtherDetalis()
        findNavController().navigate(
            LocationFragmentDirections.actionFragmentLocationToOtherInformationsFragment()
        )
    }

    private fun addPatientLocationDetalis(){
        if(selectedState != null){
            patient.addExtension( extension.getExtenstion(
                    extension.getUrl(state),
                    extension.getCoding(selectedState!!.stateID.toString(), selectedState!!.stateName) ) )
        }

        if(selectedDistrict != null){
            patient.addExtension( extension.getExtenstion(
                    extension.getUrl(district),
                    extension.getCoding(selectedDistrict!!.districtID.toString(), selectedDistrict!!.districtName) ) )
        }

        if(selectedBlock != null){
            patient.addExtension( extension.getExtenstion(
                    extension.getUrl(block),
                    extension.getCoding(selectedBlock!!.blockID.toString(), selectedBlock!!.blockName) ) )
        }

        if(selectedVillage != null){
            patient.addExtension( extension.getExtenstion(
                    extension.getUrl(districtBranch),
                    extension.getCoding(selectedVillage!!.districtBranchID.toString(), selectedVillage!!.villageName) ) )
        }

    }

    private fun addPatientOtherDetalis(){

        if(userInfo != null){

            patient.addExtension( extension.getExtenstion(
                extension.getUrl(vanID),
                extension.getStringType(userInfo!!.vanId.toString()) ) )

            patient.addExtension( extension.getExtenstion(
                extension.getUrl(parkingPlaceID),
                extension.getStringType(userInfo!!.parkingPlaceId.toString()) ) )

            patient.addExtension( extension.getExtenstion(
                extension.getUrl(providerServiceMapId),
                extension.getStringType(userInfo!!.serviceMapId.toString()) ) )

            patient.addExtension( extension.getExtenstion(
                extension.getUrl(createdBy),
                extension.getStringType(userInfo!!.userName) ) )

        }

    }

    override fun onCancelAction() {
        findNavController().navigate(
            LocationFragmentDirections.actionFragmentLocationToFhirAddPatientFragment()
        )
    }


}