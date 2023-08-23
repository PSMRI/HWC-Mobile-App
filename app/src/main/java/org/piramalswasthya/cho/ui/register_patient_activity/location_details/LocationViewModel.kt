package org.piramalswasthya.cho.ui.register_patient_activity.location_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.LocationRequest
import org.piramalswasthya.cho.model.UserCache
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
import org.piramalswasthya.cho.repositories.VillageMasterRepo
import org.piramalswasthya.cho.ui.login_activity.login_settings.LoginSettingsViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LocationViewModel@Inject constructor(
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


    var stateList: List<State> = mutableListOf()
    var districtList: List<District> = mutableListOf()
    var blockList: List<DistrictBlock> = mutableListOf()
    var villageList: List<Village> = mutableListOf()

    var selectedState: State? = null
    var selectedDistrict: District? = null
    var selectedBlock: DistrictBlock? = null
    var selectedVillage: Village? = null


    private var userInfo: UserCache? = null

    init{
        viewModelScope.launch {
            async { userInfo = userDao.getLoggedInUser() }.await()
            fetchStates()
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

    fun fetchStates() {
        viewModelScope.launch {
            _state.value = NetworkState.LOADING
            try {
                stateList = stateMasterRepo.getAllStates()
                initializeStateSelection()
                _state.value = NetworkState.SUCCESS
            } catch (e: Exception) {
                _state.value = NetworkState.FAILURE
                Timber.d("Fetching states failed ${e.message}")
            }
        }
    }

    fun fetchDistricts(selectedStateId: Int) {
        _district.value = NetworkState.LOADING
        viewModelScope.launch {
            try {
                districtList = districtMasterRepo.getDistrictsByStateId(selectedStateId)
                initializeDistrictSelection()
                _district.value = NetworkState.SUCCESS
            } catch (e: Exception) {
                _district.value = NetworkState.FAILURE
                Timber.d("Fetching Districts failed ${e.message}")
            }
        }
    }

    fun fetchTaluks(selectedDistrictId: Int) {
        _block.value = NetworkState.LOADING
        viewModelScope.launch {
            try {
                blockList = blockMasterRepo.getBlocksByDistrictId(selectedDistrictId)
                initializeBlockSelection()
                _block.value = NetworkState.SUCCESS
            } catch (e: Exception) {
                _block.value = NetworkState.FAILURE
                Timber.d("Fetching Taluks failed ${e.message}")
            }
        }
    }

    fun fetchVillageMaster(selectedBlockId: Int, ) {
        _village.value = NetworkState.LOADING
        viewModelScope.launch {
            try {
                // Find the selected state by name
                villageList = villageMasterRepo.getVillagesByBlockId(selectedBlockId)
                initializeVillageSelection()
                _village.value = NetworkState.SUCCESS
            } catch (e: Exception) {
                _village.value = NetworkState.FAILURE
                Timber.d("Fetching villages failed ${e.message}")
            }
        }
    }
}