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

//    private fun fetchStates() {
//        Log.i("states fetching", "done")
//        coroutineScope.launch {
//            try {
////                val request = LocationRequest(vanID = 153, spPSMID = "64")
////                val stateData = stateMasterRepo.getStateList(request)
////                when (stateData){
////                    is NetworkResult.Success -> {
//                        stateMap = stateMasterRepo.getAllStatesAsMap()
//                        val stateNames = stateMap!!.values.toTypedArray()
//                        binding.dropdownState.setAdapter(ArrayAdapter<String>(requireContext(), R.layout.drop_down, emptyArray()))//                    binding.dropdownState.text = (state.stateName)
//                        if(stateNames.isNotEmpty()) {
//                            if(userInfo != null && userInfo!!.stateID != null && stateMap!!.containsKey(userInfo!!.stateID)){
//                                Log.i("state if", "")
//                                binding.dropdownState.setText(stateMap!![userInfo!!.stateID], false)
//                                selectedState = StateMaster(stateID = userInfo!!.stateID!!, stateName = stateMap!![userInfo!!.stateID]!!)
//                                fetchDistricts(userInfo!!.stateID!!)
//                            }
//                            else{
//                                Log.i("state else", "")
//                                val firstState = stateMap!!.entries.toList()[0]
//                                selectedState = StateMaster(stateID = firstState.key, stateName = firstState.value!!)
//                                binding.dropdownState.setText(firstState.value, false)
//                                coroutineScope.launch {
//                                    if(updateUserStateId(firstState.key) > 0 && userInfo != null){
//                                        userInfo!!.stateID = firstState.key
//                                    }
//                                }
//                                fetchDistricts(firstState.key)
//                            }
//                        }
////                        binding.dropdownState.setOnItemClickListener { parent, _, position, _ ->
////                            val selectedStateId = stateMap!!.keys.toList()[position]
////                            coroutineScope.launch {
////                                if(updateUserStateId(selectedStateId) > 0 && userInfo != null){
////                                    userInfo!!.stateID = selectedStateId
////                                }
////                            }
////                            fetchDistricts(selectedStateId)
////                        }
////                    }
////
////                    else -> {
////
////                    }
////                }
//
//            } catch (e: Exception) {
//                Timber.d("Fetching states failed ${e.message}")
//            }
//        }
//    }
//
//    private suspend fun updateUserStateId(stateId : Int) : Int{
//        return userDao.updateUserStateId(stateId)
//    }
//
//    private fun fetchDistricts(selectedStateId: Int) {
//
//        coroutineScope.launch {
//            try {
//
////                val districtData = districtMasterRepo.districtMasterService(selectedStateId)
////                when (districtData){
////                    is NetworkResult.Success -> {
//                        districtMap = districtMasterRepo.getDistrictsByStateIdAsMap(selectedStateId)
//                        val districtNames = districtMap!!.values.toTypedArray()
//                        binding.dropdownDist.setAdapter(ArrayAdapter<String>(requireContext(), R.layout.drop_down, emptyArray()))//                    binding.dropdownState.text = (state.stateName)
//                        if(districtNames.isNotEmpty()) {
//                            if(userInfo != null && userInfo!!.districtID != null && districtMap!!.containsKey(userInfo!!.districtID)){
//                                Log.i("district if", "")
//                                binding.dropdownDist.setText(districtMap!![userInfo!!.districtID], false)
//                                selectedDistrict = DistrictMaster(districtID = userInfo!!.districtID!!, stateID = selectedStateId, districtName = districtMap!![userInfo!!.districtID]!!)
//                                fetchTaluks(userInfo!!.districtID!!)
//                            }
//                            else{
//                                Log.i("district else", "")
//                                val firstDistrict = districtMap!!.entries.toList()[0]
//                                selectedDistrict = DistrictMaster(districtID = firstDistrict.key, stateID = selectedStateId, districtName = firstDistrict.value)
//                                binding.dropdownDist.setText(firstDistrict.value, false)
//                                coroutineScope.launch {
//                                    if(updateUserDistrictId(firstDistrict.key) > 0 && userInfo != null){
//                                        userInfo!!.districtID = firstDistrict.key
//                                    }
//                                }
//                                fetchTaluks(firstDistrict.key)
//                            }
//                        }
////                        binding.dropdownDist.setOnItemClickListener { parent, _, position, _ ->
////                            val selectedDistrictId = districtMap!!.keys.toList()[position]
////                            coroutineScope.launch {
////                                if(updateUserDistrictId(selectedDistrictId) > 0 && userInfo != null){
////                                    userInfo!!.districtID = selectedDistrictId
////                                }
////                            }
////                            fetchTaluks(selectedDistrictId)
////                        }
////                    }
////
////                    else -> {
////
////                    }
////                }
//
//            } catch (e: Exception) {
//                Timber.d("Fetching Districts failed ${e.message}")
//            }
//        }
//    }
//
//    private suspend fun updateUserDistrictId(districtId : Int) : Int{
//        return userDao.updateUserDistrictId(districtId)
//    }
//
//    private fun fetchTaluks(selectedDistrictId: Int) {
//
//        coroutineScope.launch {
//            try {
//                // Find the selected state by name
////                val blockData = blockMasterRepo.blockMasterService(selectedDistrictId)
////                when (blockData){
////                    is NetworkResult.Success -> {
//                        blockMap = blockMasterRepo.getBlocksByDistrictIdAsMap(selectedDistrictId)
//                        val blockNames = blockMap!!.values.toTypedArray()
//                        binding.dropdownTaluk.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down, blockNames))//                    binding.dropdownState.text = (state.stateName)
//                        if(blockNames.isNotEmpty()) {
//                            if(userInfo != null && userInfo!!.blockID != null && blockMap!!.containsKey(userInfo!!.blockID)){
//                                Log.i("district if", "")
//                                selectedBlock = BlockMaster(blockID = userInfo!!.blockID!!, districtID = selectedDistrictId, blockName = blockMap!![userInfo!!.blockID]!!)
//                                binding.dropdownTaluk.setText(blockMap!![userInfo!!.blockID], false)
//                                fetchVillageMaster(userInfo!!.blockID!!)
//                            }
//                            else{
//                                Log.i("district else", "")
//                                val firstBlock = blockMap!!.entries.toList()[0]
//                                selectedBlock = BlockMaster(blockID = firstBlock.key, districtID = selectedDistrictId, blockName = firstBlock.value)
//                                binding.dropdownTaluk.setText(firstBlock.value, false)
//                                coroutineScope.launch {
//                                    if(updateUserBlockId(firstBlock.key) > 0 && userInfo != null){
//                                        userInfo!!.blockID = firstBlock.key
//                                    }
//                                }
//                                fetchVillageMaster(firstBlock.key)
//                            }
//                        }
//                        binding.dropdownTaluk.setOnItemClickListener { parent, _, position, _ ->
//                            val selectedBlockId = blockMap!!.keys.toList()[position]
//                            selectedBlock = BlockMaster(blockID = selectedBlockId, districtID = selectedDistrictId, blockName = blockMap!![selectedBlockId]!!)
//                            coroutineScope.launch {
//                                if(updateUserBlockId(selectedBlockId) > 0 && userInfo != null){
//                                    userInfo!!.blockID = selectedBlockId
//                                }
//                            }
//                            fetchVillageMaster(selectedBlockId)
//                        }
////                    }
////
////                    else -> {
////
////                    }
////                }
//
//            } catch (e: Exception) {
//                Timber.d("Fetching Taluks failed ${e.message}")
//            }
//        }
//    }
//
//    private suspend fun updateUserBlockId(blockId : Int) : Int{
//        return userDao.updateUserBlockId(blockId)
//    }
//
//    private fun fetchVillageMaster(selectedBlockId: Int, ) {
//
//        coroutineScope.launch {
//            try {
//                // Find the selected state by name
////                val villageData = villageMasterRepo.villageMasterService(selectedBlockId)
////                when (villageData){
////                    is NetworkResult.Success -> {
//                        villageMap = villageMasterRepo.getBlocksByDistrictIdAsMap(selectedBlockId)
//                        val villageNames = villageMap!!.values.toTypedArray()
//                        binding.dropdownPanchayat.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down, villageNames))//                    binding.dropdownState.text = (state.stateName)
//                        if(villageNames.isNotEmpty()) {
//                            if(userInfo != null && userInfo!!.districtBranchID != null && villageMap!!.containsKey(userInfo!!.districtBranchID)){
//                                Log.i("village if", "")
//                                selectedVillage = VillageMaster(districtBranchID = userInfo!!.districtBranchID!!, blockID = selectedBlockId, villageName = villageMap!![userInfo!!.districtBranchID]!!)
//                                binding.dropdownPanchayat.setText(villageMap!![userInfo!!.districtBranchID], false)
//                            }
//                            else{
//                                Log.i("village else", "")
//                                val firstVillage = villageMap!!.entries.toList()[0]
//                                binding.dropdownPanchayat.setText(firstVillage.value, false)
//                                selectedVillage = VillageMaster(districtBranchID = firstVillage.key, blockID = selectedBlockId, villageName = firstVillage.value)
//                                coroutineScope.launch {
//                                    if(updateUserVillageId(firstVillage.key) > 0 && userInfo != null){
//                                        userInfo!!.districtBranchID = firstVillage.key
//                                    }
//                                }
//                            }
//                        }
//                        binding.dropdownPanchayat.setOnItemClickListener { parent, _, position, _ ->
//                            val selectedVillageId = villageMap!!.keys.toList()[position]
//                            selectedVillage = VillageMaster(districtBranchID = selectedVillageId, blockID = selectedBlockId, villageName = villageMap!![selectedVillageId]!!)
//                            coroutineScope.launch {
//                                if(updateUserVillageId(selectedVillageId) > 0 && userInfo != null){
//                                    userInfo!!.districtBranchID = selectedVillageId
//                                }
//                            }
//                        }
////                    }
////
////                    else -> {
////
////                    }
////                }
//
//            } catch (e: Exception) {
//                Timber.d("Fetching villages failed ${e.message}")
//            }
//        }
//    }
//
//    private suspend fun updateUserVillageId(districtBranchId : Int) : Int{
//        return userDao.updateUserVillageId(districtBranchId)
//    }

//    private fun fetchStates() {
//        Log.i("states fetching", "done")
//        coroutineScope.launch {
//            try {
//                val request = LocationRequest(vanID = 153, spPSMID = "64")
//                val stateData = apiService.getStates(request)
//                if (stateData != null){
//                    stateList = listOf()
//                    val stateNames = stateList!!.map { it.stateName }.toTypedArray()
//                    binding.dropdownState.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down, stateNames))//                    binding.dropdownState.text = (state.stateName)
//                    if(stateNames.isNotEmpty()) {
//                        if(userInfo != null && userInfo!!.stateID != null){
//                            Log.i("state if", "")
//                            for(index in stateList!!.indices){
//                                if(stateList!![index].stateID == userInfo!!.stateID){
//                                    binding.dropdownState.setText(stateList!![index].stateName, false)
//                                }
//                            }
//                        }
//                        else{
//                            Log.i("state else", "")
//                            binding.dropdownState.setText(stateList!![0].stateName, false)
//                            updateUserStateId(stateList!![0].stateID)
//                        }
//                    }
//                    binding.dropdownState.setOnItemClickListener { parent, _, position, _ ->
//                        val selectedStateName = parent.getItemAtPosition(position) as String
//                        selectedState = stateList?.find { it.stateName == selectedStateName }
//                        coroutineScope.launch { updateUserStateId(selectedState!!.stateID) }
//                        // Fetch districts based on the selected state ID
//                        fetchDistricts(selectedStateName)
//                    }
//
////                    binding.dropdownState.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
////                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
////                            val selectedStateName = binding.dropdownState.adapter.getItem(position) as String
////                            selectedState = stateList?.find { it.stateName == selectedStateName }
////                            coroutineScope.launch { updateUserStateId(selectedState!!.stateID) }
////                            // Fetch districts based on the selected state ID
////                            fetchDistricts(selectedStateName)
////                        }
////
////                        override fun onNothingSelected(parent: AdapterView<*>?) {
////                            // Do nothing
////                        }
////                    }
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
//                        binding.dropdownDistrict.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, districtNames)
//                        if(districtNames.isNotEmpty()) {
//                            if(userInfo != null && userInfo!!.districtID != null){
//                                for(index in districtList!!.indices){
//                                    if(districtList!![index].districtID == userInfo!!.districtID){
//                                        binding.dropdownDistrict.setSelection(index)
//                                    }
//                                }
//                            }
//                            else{
////                                binding.dropdownDistrict.setText(districtList!![0].districtName)
////                                updateUserDistrictId(districtList!![0].districtID)
//                            }
//                        }
////                        binding.dropdownDistrict.setOnItemClickListener { parent, _, position, _ ->
////                            val selectedDistrictName = parent.getItemAtPosition(position) as String
////
////                            selectedDistrict = districtList?.find { it.districtName == selectedDistrictName }
////                            coroutineScope.launch { updateUserDistrictId(selectedDistrict!!.districtID) }
////
////                            fetchTaluks(selectedDistrictName)
////                        }
//
//                        binding.dropdownDistrict.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                                val selectedDistrictName = binding.dropdownDistrict.adapter.getItem(position) as String
////                                 Fetch Taluks based on the selected value
//                                selectedDistrict = districtList?.find { it.districtName == selectedDistrictName }
//                                coroutineScope.launch { updateUserDistrictId(selectedDistrict!!.districtID) }
//                                fetchTaluks(selectedDistrictName)
//                            }
//                            override fun onNothingSelected(parent: AdapterView<*>?) {
//                                // Do nothing
//                            }
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
//                        binding.dropdownTaluk.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, blockNames)
////                        binding.dropdownTaluk.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down, blockNames))
//                        if(blockNames.isNotEmpty()) {
//                            if(userInfo != null && userInfo!!.blockID != null){
//                                for(index in blockList!!.indices){
//                                    if(blockList!![index].blockID == userInfo!!.blockID){
//                                        binding.dropdownTaluk.setSelection(index)
//                                    }
//                                }
//                            }
//                            else{
////                                binding.dropdownTaluk.setText(blockList!![0].blockName)
////                                updateUserBlockId(blockList!![0].blockID)
//                            }
//                        }
////                        binding.dropdownTaluk.setOnItemClickListener { parent, _, position, _ ->
////                            val selectedTaluk = parent.getItemAtPosition(position) as String
////                            selectedBlock = blockList?.find { it.blockName == selectedTaluk }
////                            coroutineScope.launch { updateUserBlockId(selectedBlock!!.blockID) }
////
////                            fetchVillageMaster(selectedTaluk)
////                        }
//
//                        binding.dropdownTaluk.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                                val selectedBlockName = binding.dropdownTaluk.adapter.getItem(position) as String
////                                 Fetch Village/Area/Street based on the selected value
//                                selectedBlock = blockList?.find { it.blockName == selectedBlockName }
//                                coroutineScope.launch { updateUserBlockId(selectedBlock!!.blockID) }
//                                fetchVillageMaster(selectedBlockName)
//                            }
//                            override fun onNothingSelected(parent: AdapterView<*>?) {
//                                // Do nothing
//                            }
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
//    private suspend fun addVillagesToDb(currVillageList : List<Village>,  blockId : Int){
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
//                        binding.dropdownStreet.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, villageNames)
////                        binding.dropdownStreet.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down, villageNames))
//                        if(villageNames.isNotEmpty()) {
//                            if(userInfo != null && userInfo!!.districtBranchID != null){
//                                for(index  in villageList!!.indices){
//                                    if(villageList!![index].districtBranchID == userInfo!!.districtBranchID){
//                                        binding.dropdownStreet.setSelection(index)
//                                    }
//                                }
//                            }
//                            else{
////                                updateUserVillageId(villageList!![0].districtBranchID)
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
////                        binding.dropdownStreet.setOnItemClickListener { parent, _, position, _ ->
////                            val selectedVillageName = parent.getItemAtPosition(position) as String
////
////                            selectedVillage = villageList?.find { it.villageName == selectedVillageName }
////                            coroutineScope.launch { updateUserVillageId(selectedVillage!!.districtBranchID) }
////                        }
//                        binding.dropdownStreet.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                                val selectedVillageName = binding.dropdownStreet.adapter.getItem(position) as String
////                                 Fetch Village/Area/Street based on the selected value
//
//                                selectedVillage = villageList?.find { it.villageName == selectedVillageName }
//                                coroutineScope.launch { updateUserVillageId(selectedVillage!!.districtBranchID) }
//
//                            }
//                            override fun onNothingSelected(parent: AdapterView<*>?) {
//                                // Do nothing
//                            }
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
//
//    private suspend fun updateUserVillageId(districtBranchId : Int){
//        userDao.updateUserVillageId(districtBranchId)
//    }

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