package org.piramalswasthya.cho.ui.register_patient_activity.location_details

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.room.dao.StateMasterDao
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
//import org.piramalswasthya.cho.databinding.FragmentLoginSettingBinding
import org.piramalswasthya.cho.model.BlockMaster
import org.piramalswasthya.cho.model.DistrictMaster
import org.piramalswasthya.cho.model.LocationRequest
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.model.VillageMaster
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.District
import org.piramalswasthya.cho.network.DistrictBlock
import org.piramalswasthya.cho.network.State
import org.piramalswasthya.cho.network.Village
import org.piramalswasthya.cho.repositories.BlockMasterRepo
import org.piramalswasthya.cho.repositories.DistrictMasterRepo
import org.piramalswasthya.cho.repositories.LoginSettingsDataRepository
import org.piramalswasthya.cho.repositories.VillageMasterRepo
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LocationFragment : Fragment() , NavigationAdapter {

    @Inject
    lateinit var districtMasterRepo: DistrictMasterRepo

    @Inject
    lateinit var blockMasterRepo: BlockMasterRepo

    @Inject
    lateinit var villageMasterRepo: VillageMasterRepo

    @Inject
    lateinit var userDao: UserDao

    private var userInfo: UserCache? = null

//    @Inject
//    lateinit var loginSettingsDataRepository: LoginSettingsDataRepository
//
//    @Inject
//    lateinit var apiService: AmritApiService
//    private val binding by lazy{
//        FragmentLoginSettingBinding.inflate(layoutInflater)
//    }
//    private var stateList: List<State>? = null
//    private var selectedState: State? = null
//    private var districtList: List<District>? = null
//    private var selectedDistrict: District? = null
//    private var blockList: List<DistrictBlock>? = null
//    private var selectedBlock: DistrictBlock? = null
//    private var villageList: List<Village>? = null
//    private var selectedVillage: Village? = null
//
//
//    private val coroutineScope = CoroutineScope(Dispatchers.Main)
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        fetchStates()
//    }
//
//
//    private fun fetchStates() {
//        coroutineScope.launch {
//            try {
//                val request = LocationRequest(vanID = 153, spPSMID = "64")
//                val stateData = apiService.getStates(request)
//                if (stateData != null){
//                    stateList = stateData.data.stateMaster
//                    val stateNames = stateList!!.map { it.stateName }.toTypedArray()
//                    binding.dropdownState.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down, stateNames))
//                    binding.dropdownState.setSelection(0)
////                    binding.dropdownState.text = (state.stateName)
////                    if(stateNames.isNotEmpty()) {
////                        if(userInfo != null && userInfo!!.stateID != null){
////                            Log.i("state if", "")
////                            for(state in stateList!!){
////                                if(state.stateID == userInfo!!.stateID){
////                                    binding.dropdownState.setText(state.stateName)
////                                }
////                            }
////                        }
////                        else{
////                            Log.i("state else", "")
////                            binding.dropdownState.setText(stateList!![0].stateName)
////                            updateUserStateId(stateList!![0].stateID)
////                        }
////                    }
//                    binding.dropdownState.setOnItemClickListener { parent, _, position, _ ->
//                        val selectedStateName = parent.getItemAtPosition(position) as String
//                        selectedState = stateList?.find { it.stateName == selectedStateName }
//                        coroutineScope.launch { updateUserStateId(selectedState!!.stateID) }
//                        // Fetch districts based on the selected state ID
//                        fetchDistricts(selectedStateName)
//                    }
//                }
//
//            } catch (e: Exception) {
//                Timber.d("Fetching states failed ${e.message}")
//            }
//        }
//    }
//
//    private suspend fun addDistrictsToDb(stateId : Int){
//        try {
//            districtList?.let{
//                for(district in districtList!!){
//                    districtMasterRepo.insertDistrict(DistrictMaster(district.districtID, stateId, district.districtName))
//                }
//            }
//        }
//        catch (e : Exception){
//            Log.i("exception in district", e.toString())
//        }
//    }
//
//    private suspend fun updateUserStateId(stateId : Int){
//        userDao.updateUserStateId(stateId)
//    }
//
//    private fun fetchDistricts(selectedStateName: String) {
//
//        coroutineScope.launch {
//            try {
//                // Find the selected state by name
//                selectedState = stateList?.find { it.stateName == selectedStateName }
//
//                // If the selected state is found, make the API call with its stateID
//                selectedState?.let {
//                    val stateId = it.stateID
//                    val response = apiService.getDistricts(stateId)
//
//                    if (response!=null) {
//                        districtList = response?.data
////                        async { addDistrictsToDb(stateId) }.await()
//                        addDistrictsToDb(stateId)
//                        val districtNames = districtList!!.map { it.districtName }.toTypedArray()
//                        Log.i("Dist" ,"$districtNames")
//                        binding.dropdownDist.setAdapter(ArrayAdapter(requireContext(),R.layout.drop_down, districtNames))
//                        if(districtNames.isNotEmpty()) {
//                            if(userInfo != null && userInfo!!.districtID != null){
//                                for(district in districtList!!){
//                                    if(district.districtID == userInfo!!.districtID){
//                                        binding.dropdownDist.setText(district.districtName)
//                                    }
//                                }
//                            }
//                            else{
//                                binding.dropdownDist.setText(districtList!![0].districtName)
//                                updateUserDistrictId(districtList!![0].districtID)
//                            }
//                        }
//                        binding.dropdownDist.setOnItemClickListener { parent, _, position, _ ->
//                            val selectedDistrictName = parent.getItemAtPosition(position) as String
//
//                            selectedDistrict = districtList?.find { it.districtName == selectedDistrictName }
//                            coroutineScope.launch { updateUserDistrictId(selectedDistrict!!.districtID) }
//
//                            fetchTaluks(selectedDistrictName)
//                        }
//                    }
//                }
//
//            } catch (e: Exception) {
//                Timber.d("Fetching Districts failed ${e.message}")
//            }
//        }
//    }
//
//    private suspend fun addBlocksToDb(districtId : Int){
//        try {
//            blockList?.let{
//                for(block in blockList!!){
//                    Log.i("adding blocks", block.blockID.toString() + " " + districtId)
//                    blockMasterRepo.insertBlock(BlockMaster(blockID = block.blockID, districtID = districtId, blockName = block.blockName))
//                }
//            }
//        }
//        catch (e : Exception){
//            Log.i("exception in block", e.toString())
//        }
//    }
//
//    private suspend fun updateUserDistrictId(districtId : Int){
//        userDao.updateUserDistrictId(districtId)
//    }
//
//    private fun fetchTaluks(selectedDistrictName: String) {
//
//        coroutineScope.launch {
//            try {
//                // Find the selected state by name
//                selectedDistrict = districtList?.find { it.districtName == selectedDistrictName }
//
//                // If the selected state is found, make the API call with its stateID
//                selectedDistrict?.let {
//                    val districtId = it.districtID
//                    val response = apiService.getDistrictBlocks(districtId)
//
//                    if (response!=null) {
//                        blockList = response?.data
////                        async { addBlocksToDb(districtId) }.await()
//                        addBlocksToDb(districtId)
//                        val blockNames = blockList!!.map { it.blockName }.toTypedArray()
//                        binding.dropdownTaluk.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down, blockNames))
//                        if(blockNames.isNotEmpty()) {
//                            if(userInfo != null && userInfo!!.blockID != null){
//                                for(block in blockList!!){
//                                    if(block.blockID == userInfo!!.blockID){
//                                        binding.dropdownTaluk.setText(block.blockName)
//                                    }
//                                }
//                            }
//                            else{
//                                binding.dropdownTaluk.setText(blockList!![0].blockName)
//                                updateUserBlockId(blockList!![0].blockID)
//                            }
//                        }
//                        binding.dropdownTaluk.setOnItemClickListener { parent, _, position, _ ->
//                            val selectedTaluk = parent.getItemAtPosition(position) as String
//                            selectedBlock = blockList?.find { it.blockName == selectedTaluk }
//                            coroutineScope.launch { updateUserBlockId(selectedBlock!!.blockID) }
//
//                            fetchVillageMaster(selectedTaluk)
//                        }
//                    }
//                }
//
//            } catch (e: Exception) {
//                Timber.d("Fetching Taluks failed ${e.message}")
//            }
//        }
//    }
//
//    private suspend fun getVillagesByBlockIdAndAddToDb(){
//        try {
//            blockList?.let {
//                for (block in blockList!!){
//                    val response = apiService.getVillages(block.blockID)
//                    if (response != null) {
//                        addVillagesToDb(response.data, block.blockID)
//                    }
//                }
//            }
//        }
//        catch (e : Exception){
//            Log.i("exception in village", e.toString())
//        }
//    }
//
//    private suspend fun addVillagesToDb(currVillageList : List<Village>, blockId : Int){
//        try {
//            currVillageList?.let{
//                for(village in currVillageList){
//                    villageMasterRepo.insertVillage(VillageMaster(districtBranchID = village.districtBranchID, blockID = blockId, villageName = village.villageName))
//                }
//            }
//        }
//        catch (e : Exception){
//            Log.i("exception in village", e.toString())
//        }
//    }
//
//    private suspend fun updateUserBlockId(blockId : Int){
//        userDao.updateUserBlockId(blockId)
//    }
//
//    private fun fetchVillageMaster(selectedBlockName: String, ) {
//
//        coroutineScope.launch {
//            try {
//                // Find the selected state by name
//                selectedBlock = blockList?.find { it.blockName == selectedBlockName }
//
//                // If the selected state is found, make the API call with its stateID
//                selectedBlock?.let {
//                    val blockId = it.blockID
//                    val response = apiService.getVillages(blockId)
//
//                    if (response!=null) {
//                        villageList = response?.data
////                        async { getVillagesByBlockIdAndAddToDb() }.await()
//                        getVillagesByBlockIdAndAddToDb()
//                        val villageNames = villageList!!.map { it.villageName }.toTypedArray()
//                        binding.dropdownPanchayat.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down, villageNames))
//                        if(villageNames.isNotEmpty()) {
//                            if(userInfo != null && userInfo!!.districtBranchID != null){
//                                for(village in villageList!!){
//                                    if(village.districtBranchID == userInfo!!.districtBranchID){
//                                        binding.dropdownPanchayat.setText(village.villageName)
//                                    }
//                                }
//                            }
//                            else{
//                                binding.dropdownPanchayat.setText(villageList!![0].villageName)
//                                updateUserVillageId(villageList!![0].districtBranchID)
//                            }
//                        }
////                        binding.dropdownPanchayat.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
////                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
////                                val selectedVillageName = binding.dropdownPanchayat.adapter.getItem(position) as String
//////                                 Fetch Village/Area/Street based on the selected value
////
////                                selectedVillage = villageList?.find { it.villageName == selectedVillageName }
////
////                            }
////                            override fun onNothingSelected(parent: AdapterView<*>?) {
////                                // Do nothing
////                            }
////                        }
//
//                        binding.dropdownPanchayat.setOnItemClickListener { parent, _, position, _ ->
//                            val selectedVillageName = parent.getItemAtPosition(position) as String
//
//                            selectedVillage = villageList?.find { it.villageName == selectedVillageName }
//                            coroutineScope.launch { updateUserVillageId(selectedVillage!!.districtBranchID) }
//                        }
//
//
//                    }
//                }
//
//            } catch (e: Exception) {
//                Timber.d("Fetching villages failed ${e.message}")
//            }
//        }
//    }

    private suspend fun updateUserVillageId(districtBranchId : Int){
        userDao.updateUserVillageId(districtBranchId)
    }


    override fun getFragmentId(): Int {
        return R.id.fragment_add_patient_location;
    }

    override fun onSubmitAction() {

    }

    override fun onCancelAction() {

    }


}