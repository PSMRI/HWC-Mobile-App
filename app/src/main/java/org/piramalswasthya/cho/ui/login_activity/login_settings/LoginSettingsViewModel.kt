package org.piramalswasthya.cho.ui.login_activity.login_settings

import android.content.Context
import android.util.Log
import android.widget.ArrayAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.dropdown_adapters.StatesAdapter
import org.piramalswasthya.cho.database.room.dao.StateMasterDao
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.BlockMaster
import org.piramalswasthya.cho.model.DistrictMaster
import org.piramalswasthya.cho.model.LocationRequest
import org.piramalswasthya.cho.model.StateMaster
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.model.VillageLocationData
import org.piramalswasthya.cho.model.VillageMaster
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.BlockList
import org.piramalswasthya.cho.network.District
import org.piramalswasthya.cho.network.DistrictBlock
import org.piramalswasthya.cho.network.DistrictList
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.State
import org.piramalswasthya.cho.network.StateList
import org.piramalswasthya.cho.network.Village
import org.piramalswasthya.cho.network.VillageList
import org.piramalswasthya.cho.repositories.BlockMasterRepo
import org.piramalswasthya.cho.repositories.DistrictMasterRepo
import org.piramalswasthya.cho.repositories.LoginSettingsDataRepository
import org.piramalswasthya.cho.repositories.StateMasterRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.repositories.VillageMasterRepo
import org.piramalswasthya.cho.ui.login_activity.cho_login.outreach.OutreachViewModel
import org.piramalswasthya.cho.ui.register_patient_activity.patient_details.PatientDetailsViewModel
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class LoginSettingsViewModel@Inject constructor(
    private val userRepo: UserRepo,
    private val loginSettingsDataRepository: LoginSettingsDataRepository,
    private val stateMasterRepo: StateMasterRepo,
    private val districtMasterRepo: DistrictMasterRepo,
    private val blockMasterRepo: BlockMasterRepo,
    private val villageMasterRepo: VillageMasterRepo,
    private val userDao: UserDao,
    private val pref: PreferenceDao,
    private val apiService: AmritApiService
) : ViewModel(){

    enum class NetworkState {
        IDLE,
        LOADING,
        SUCCESS,
        FAILURE
    }
    private val _locationMaster = MutableLiveData(NetworkState.IDLE)
    val locationMaster: LiveData<NetworkState>
        get() = _locationMaster
    private val _state = MutableLiveData(NetworkState.IDLE)
    val state: LiveData<NetworkState>
        get() = _state

    private val _district = MutableLiveData(NetworkState.IDLE)
    val district: LiveData<NetworkState>
        get() = _district

    private val _block = MutableLiveData(NetworkState.IDLE)
    val block: LiveData<NetworkState>
        get() = _block

    private val _village = MutableLiveData(NetworkState.IDLE)
    val village: LiveData<NetworkState>
        get() = _village


    var masterVillageList: List<VillageLocationData> = mutableListOf()
    var masterState: String? = ""
    var masterDistrict: String? = ""
    var masterBlock: String? = ""
    var masterStateId: Int? = null
    var masterDistrictId: Int? = null
    var masterBlockId: Int? = null
    var masterVillage: String? = ""
    var masterVillageId: Int? = null
    var userMasterVillage: VillageLocationData? = null

    var stateList: List<State> = mutableListOf()
    var districtList: List<District> = mutableListOf()
    var blockList: List<DistrictBlock> = mutableListOf()
    var villageList: List<Village> = mutableListOf()

    var selectedState: State? = null
    var selectedDistrict: District? = null
    var selectedBlock: DistrictBlock? = null
    var selectedVillage: Village? = null


     var userInfo: UserCache? = null

    init{
        viewModelScope.launch {
            async { userInfo = userDao.getLoggedInUser() }.await()
//            fetchStates()
            fetchUserMasterLocation()
        }
    }

    fun updateUserStateId(stateId : Int){
        viewModelScope.launch {
            val result = userDao.updateUserStateId(stateId)
            if(result > 0 && userInfo != null){
                userInfo!!.stateID = stateId
            }
        }
    }

    fun updateUserDistrictId(districtId : Int) {
        viewModelScope.launch {
            val result =  userDao.updateUserDistrictId(districtId)
            if(result > 0 && userInfo != null){
                userInfo!!.districtID = districtId
            }
        }
    }

    fun updateUserBlockId(blockId : Int) {
        viewModelScope.launch {
            val result =  userDao.updateUserBlockId(blockId)
            if(result > 0 && userInfo != null){
                userInfo!!.blockID = blockId
            }
        }
    }

    fun updateUserVillageId(districtBranchId : Int) {
        viewModelScope.launch {
            val result =  userDao.updateUserVillageId(districtBranchId)
            if(result > 0 && userInfo != null){
                userInfo!!.districtBranchID = districtBranchId
            }
        }
    }

    suspend fun saveMasterLatLong(
        lat: Double?,
        long: Double?
    ) {
        viewModelScope.launch {
            userRepo.saveMasterLatLong(
                lat,
                long
            )
        }
    }

    private fun initializeStateSelection(){
        if(stateList.isNotEmpty()) {
            if(userInfo != null && userInfo!!.stateID != null && stateList.any{ it.stateID == userInfo!!.stateID!! }){
                selectedState = stateList.firstOrNull { it.stateID == userInfo!!.stateID!!}!!
                fetchDistricts(userInfo!!.stateID!!)
            }
            else{
                selectedState = stateList[0]
                updateUserStateId(selectedState!!.stateID)
                fetchDistricts(selectedState!!.stateID)
            }
        }
    }

    private fun initializeDistrictSelection(){
        if(districtList.isNotEmpty()) {
            if(userInfo != null && userInfo!!.districtID != null && districtList.any{ it.districtID == userInfo!!.districtID!! }){
                selectedDistrict = districtList.firstOrNull { it.districtID == userInfo!!.districtID!!}!!
                fetchTaluks(userInfo!!.districtID!!)
            }
            else{
                selectedDistrict = districtList[0]
                updateUserDistrictId(selectedDistrict!!.districtID)
                fetchTaluks(selectedDistrict!!.districtID)
            }
        }
    }

    private fun initializeBlockSelection(){
        if(blockList.isNotEmpty()) {
            if(userInfo != null && userInfo!!.blockID != null && blockList.any{ it.blockID == userInfo!!.blockID!! }){
                selectedBlock = blockList.firstOrNull { it.blockID == userInfo!!.blockID!!}!!
                fetchVillageMaster(userInfo!!.blockID!!)
            }
            else{
                selectedBlock = blockList[0]
                updateUserBlockId(selectedBlock!!.blockID)
                fetchVillageMaster(selectedBlock!!.blockID)
            }
        }
    }

    private fun initializeVillageSelection(){
        if(villageList.isNotEmpty()) {
            if(userInfo != null && userInfo!!.districtBranchID != null && villageList.any { it.districtBranchID == userInfo!!.districtBranchID!! }){
                selectedVillage = villageList.firstOrNull { it.districtBranchID == userInfo!!.districtBranchID!!}!!
            }
            else{
                selectedVillage = villageList[0]
                updateUserVillageId(selectedVillage!!.districtBranchID)
            }
        }
    }


    fun fetchDistricts(selectedStateId: Int) {
        _district.value = NetworkState.LOADING
        viewModelScope.launch {
            try {
                when (val districtData = districtMasterRepo.districtMasterService(selectedStateId)){
                    is NetworkResult.Success -> {
                        districtList = (districtData.data as DistrictList).districtMaster
                        addDistrictsToDb(selectedStateId)
                        initializeDistrictSelection()
                        _district.value = NetworkState.SUCCESS

                    }
                    else -> {

                    }

                }

            } catch (e: Exception) {
                Timber.d("Fetching Districts failed ${e.message}")
            }
        }
    }

    fun fetchTaluks(selectedDistrictId: Int) {
        _block.value = NetworkState.LOADING
        viewModelScope.launch {
            try {
                when (val blockData = blockMasterRepo.blockMasterService(selectedDistrictId)){
                    is NetworkResult.Success -> {
                        blockList = (blockData.data as BlockList).blockMaster
                        addBlocksToDb(selectedDistrictId)
                        initializeBlockSelection()
                        _block.value = NetworkState.SUCCESS
                    }
                    else -> {

                    }
                }

            } catch (e: Exception) {
                Timber.d("Fetching Taluks failed ${e.message}")
            }
        }
    }

    fun fetchVillageMaster(selectedBlockId: Int, ) {
        _village.value = NetworkState.LOADING
        viewModelScope.launch {
            try {
                // Find the selected state by name
                when (val villageData = villageMasterRepo.villageMasterService(selectedBlockId)){
                    is NetworkResult.Success -> {
                        villageList = (villageData.data as VillageList).villageMaster
                        getVillagesByBlockIdAndAddToDb()
                        initializeVillageSelection()
                        _village.value = NetworkState.SUCCESS
                    }

                    else -> {

                    }
                }

            } catch (e: Exception) {
                Timber.d("Fetching villages failed ${e.message}")
            }
        }
    }

    private suspend fun addStatesToDb(){
        try {
            stateList.let{
                for(states in stateList){
                    stateMasterRepo.insertStateMaster(StateMaster(states.stateID,states.stateName,states.govtLGDStateID))
                }
            }
        }
        catch (e : Exception){
            Log.i("exception in district", e.toString())
        }
    }

    private suspend fun addDistrictsToDb(stateId : Int){
        try {
            val govtLGDStateID = stateMasterRepo.getStateById(stateId)?.govtLGDStateID
            districtList.let{
                for(district in districtList){
                    districtMasterRepo.insertDistrict(DistrictMaster(district.districtID, stateId,govtLGDStateID!!,district.govtLGDDistrictID, district.districtName))
                }
            }
        }
        catch (e : Exception){
            Log.i("exception in district", e.toString())
        }
    }

    private suspend fun addBlocksToDb(districtId : Int){
        try {
            val govtLGDDistrictID = districtMasterRepo.getDistrictByDistrictId(districtId)?.govtLGDDistrictID

            blockList.let{
                for(block in blockList){
                    blockMasterRepo.insertBlock(BlockMaster(blockID = block.blockID, districtID = districtId, govtLGDDistrictID = govtLGDDistrictID!!, govLGDSubDistrictID = block.govLGDSubDistrictID, blockName = block.blockName))
                }
            }
        }
        catch (e : Exception){
            Log.i("exception in block", e.toString())
        }
    }

    private suspend fun getVillagesByBlockIdAndAddToDb(){
        try {
            blockList.let {
                for (block in blockList){
                    when (val villageData = villageMasterRepo.villageMasterService(block.blockID)){
                        is NetworkResult.Success -> {
                            addVillagesToDb((villageData.data as VillageList).villageMaster, block.blockID)
                        }
                        else -> {

                        }
                    }
                }
            }
        }
        catch (e : Exception){
            Log.i("exception in getting village", e.toString())
        }
    }

    private suspend fun addVillagesToDb(currVillageList : List<Village>,  blockId : Int){
        try {
            val govtLGDSubDistrictID = blockMasterRepo.getBlocksById(blockId)?.govLGDSubDistrictID
            currVillageList.let{
                for(village in currVillageList){
                    villageMasterRepo.insertVillage(VillageMaster(districtBranchID = village.districtBranchID, blockID = blockId, govtLGDSubDistrictID, govtLGDVillageID = village.govtLGDVillageID, villageName = village.villageName))
                }
            }
        }
        catch (e : Exception){
            Log.i("exception in saving village", e.toString())
        }
    }
    private fun fetchUserMasterLocation(){
        _locationMaster.value = NetworkState.LOADING
        val loc = pref.getUserLocationData()
        try {
            masterState = loc?.stateName
            masterVillageId = loc?.stateId
            masterDistrict = loc?.districtName
            masterDistrictId = loc?.districtId
            masterBlock = loc?.blockName
            masterBlockId = loc?.blockId
            masterVillageList = loc?.villageList!!
            _locationMaster.value = NetworkState.SUCCESS
        }
        catch (_: Exception){
            _locationMaster.value = NetworkState.FAILURE
        }
    }
}
