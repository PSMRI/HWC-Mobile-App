package org.piramalswasthya.cho.ui.service_point_activity

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.UserBlockDetails
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.model.UserDistrictDetails
import org.piramalswasthya.cho.model.UserStateDetails
import org.piramalswasthya.cho.model.UserVanSpDetails
import org.piramalswasthya.cho.model.UserVillageDetails
import org.piramalswasthya.cho.model.VillageLocationData
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.District
import org.piramalswasthya.cho.network.DistrictBlock
import org.piramalswasthya.cho.network.State
import org.piramalswasthya.cho.network.Village
import org.piramalswasthya.cho.repositories.BlockMasterRepo
import org.piramalswasthya.cho.repositories.DistrictMasterRepo
import org.piramalswasthya.cho.repositories.LoginSettingsDataRepository
import org.piramalswasthya.cho.repositories.StateMasterRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.repositories.VillageMasterRepo
import org.piramalswasthya.cho.ui.login_activity.cho_login.outreach.OutreachViewModel
import org.piramalswasthya.cho.ui.login_activity.login_settings.LoginSettingsViewModel.NetworkState
import javax.inject.Inject


@HiltViewModel
class ServicePointViewModel@Inject constructor(
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

    val _state = MutableLiveData(OutreachViewModel.State.IDLE)
    val state: LiveData<OutreachViewModel.State>
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

    private val _vanSpDetails = MutableLiveData(NetworkState.IDLE)
    val vanSpDetails: LiveData<NetworkState>
        get() = _vanSpDetails

    private val _stateDetails = MutableLiveData(NetworkState.IDLE)
    val stateDetails: LiveData<NetworkState>
        get() = _stateDetails

    private val _districtDetails = MutableLiveData(NetworkState.IDLE)
    val districtDetails: LiveData<NetworkState>
        get() = _districtDetails

    private val _blockDetails = MutableLiveData(NetworkState.IDLE)
    val blockDetails: LiveData<NetworkState>
        get() = _blockDetails

    private val _villageDetails = MutableLiveData(NetworkState.IDLE)
    val villageDetails: LiveData<NetworkState>
        get() = _villageDetails

    var masterVanSpDetailsList: List<UserVanSpDetails> = mutableListOf()
    var masterStateDetailsList: List<UserStateDetails> = mutableListOf()
    var masterDistrictDetailsList: List<UserDistrictDetails> = mutableListOf()
    var masterBlockDetailsList: List<UserBlockDetails> = mutableListOf()
    var masterVillageDetailsList: List<UserVillageDetails> = mutableListOf()
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

    var selectedVan: UserVanSpDetails? = null
    var selectedServicePoint: UserVanSpDetails? = null
    var selectedState: UserStateDetails? = null
    var selectedDistrict: UserDistrictDetails? = null
    var selectedBlock: UserBlockDetails? = null
    var selectedVillage: UserVillageDetails? = null

    var userInfo: UserCache? = null

    init{
        viewModelScope.launch {
            async { userInfo = userDao.getLoggedInUser() }.await()
            fetchUserVanSpDetails()
        }
    }

    private fun fetchUserVanSpDetails() {
        _vanSpDetails.value = NetworkState.LOADING
        val userVanSp = pref.getUserVanSpDetailsData()
        try {
            masterVanSpDetailsList = userVanSp?.userVanSpDetails!!
            _vanSpDetails.value = NetworkState.SUCCESS
        }
        catch (_: Exception){
            _vanSpDetails.value = NetworkState.FAILURE
        }
    }

    suspend fun fetchMmuStateDetails() {
        _stateDetails.value = NetworkState.LOADING
        viewModelScope.launch {
            val result = userRepo.getMmuLocDetailsBasedOnSpIDAndPsmID(userInfo!!.servicePointId, userDao.getLoggedInUserProviderServiceMapId())
            if (result) {
                val userState = pref.getUserStateDetailsData()
                try {
                    masterStateDetailsList = userState?.userStateDetails!!
                    _stateDetails.value = NetworkState.SUCCESS
                }
                catch (_: Exception){
                    _stateDetails.value = NetworkState.FAILURE
                }
                _stateDetails.value = NetworkState.SUCCESS
            } else {
                _stateDetails.value = NetworkState.FAILURE
            }
        }
    }

    suspend fun fetchMmuDistrictDetails(stateId: Int) {
        _districtDetails.value = NetworkState.LOADING
        viewModelScope.launch {
            val result = userRepo.getMmuDistricts(stateId)
            if (result) {
                val userDistrict = pref.getUserDistrictDetailsData()
                try {
                    masterDistrictDetailsList = userDistrict?.userDistrictDetails!!
                    _districtDetails.value = NetworkState.SUCCESS
                }
                catch (_: Exception){
                    _districtDetails.value = NetworkState.FAILURE
                }
                _districtDetails.value = NetworkState.SUCCESS
            } else {
                _districtDetails.value = NetworkState.FAILURE
            }
        }
    }

    suspend fun fetchMmuBlockDetails(districtId: Int) {
        _blockDetails.value = NetworkState.LOADING
        viewModelScope.launch {
            val result = userRepo.getMmuBlocks(districtId)
            if (result) {
                val userBlock = pref.getUserBlockDetailsData()
                try {
                    masterBlockDetailsList = userBlock?.userBlockDetails!!
                    _blockDetails.value = NetworkState.SUCCESS
                }
                catch (_: Exception){
                    _blockDetails.value = NetworkState.FAILURE
                }
                _blockDetails.value = NetworkState.SUCCESS
            } else {
                _blockDetails.value = NetworkState.FAILURE
            }
        }
    }

    suspend fun fetchMmuVillageDetails(blockId: Int) {
        _villageDetails.value = NetworkState.LOADING
        viewModelScope.launch {
            val result = userRepo.getMmuVillages(blockId)
            if (result) {
                val userVillage = pref.getUserVillageDetailsData()
                try {
                    masterVillageDetailsList = userVillage?.userVillageDetails!!
                    _villageDetails.value = NetworkState.SUCCESS
                }
                catch (_: Exception){
                    _villageDetails.value = NetworkState.FAILURE
                }
                _villageDetails.value = NetworkState.SUCCESS
            } else {
                _villageDetails.value = NetworkState.FAILURE
            }
        }
    }

    fun updateUserVanId(vanId : Int) {
        viewModelScope.launch {
            val result =  userDao.updateUserVanId(vanId)
            if(result > 0 && userInfo != null) {
                userInfo!!.vanId = vanId
            }
        }
    }

    fun updateUserServicePointId(servicePointId : Int) {
        viewModelScope.launch {
            val result =  userDao.updateUserServicePointId(servicePointId)
            if(result > 0 && userInfo != null) {
                userInfo!!.servicePointId = servicePointId
            }
        }
    }

    fun updateUserState(stateId : Int) {
        viewModelScope.launch {
//            val result =  userDao.updateUserStateId(stateId)
//            if(result > 0 && userInfo != null) {
//                userInfo!!.stateID = stateId
//            }
        }
    }

    fun updateUserDistrict(districtId : Int) {
        viewModelScope.launch {
//            val result =  userDao.updateUserDistrictId(districtId)
//            if(result > 0 && userInfo != null) {
//                userInfo!!.districtID = districtId
//            }
        }
    }

    fun updateUserBlock(blockId : Int) {
        viewModelScope.launch {
//            val result =  userDao.updateUserBlockId(blockId)
//            if(result > 0 && userInfo != null) {
//                userInfo!!.blockID = blockId
//            }
        }
    }

    fun resetState() {
        _state.value = OutreachViewModel.State.IDLE
    }

    fun updateUserVillage(villageId : Int) {
//        viewModelScope.launch {
//            val result =  userDao.updateUserVillageId(villageId)
//            if(result > 0 && userInfo != null) {
////                userInfo!!.villageID = villageId
//            }
//        }
    }

}
