package org.piramalswasthya.cho.ui.commons.fhir_add_patient.location_fragment

import android.util.Log
import android.widget.ArrayAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.model.LocationRequest
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.District
import org.piramalswasthya.cho.network.DistrictBlock
import org.piramalswasthya.cho.network.State
import org.piramalswasthya.cho.network.Village
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(private val apiService: AmritApiService) : ViewModel() {

    enum class RefreshState {
        IDLE,
        REFRESHING,
        REFRESH_SUCCESS,
        REFRESH_FAILED
    }

    private val _refreshState = MutableLiveData(RefreshState.IDLE)

    val refreshState: LiveData<RefreshState>
        get() = _refreshState

    fun resetRefreshState() {
        _refreshState.value = RefreshState.IDLE
    }

    var stateList: List<State>? = null
    var districtList: List<District>? = null
    var blockList: List<DistrictBlock>? = null
    var villageList: List<Village>? = null

    fun getStateList() {
        viewModelScope.launch {
            try {
                _refreshState.value = RefreshState.REFRESHING
                val request = LocationRequest(vanID = 153, spPSMID = "64")
                withContext(Dispatchers.IO) {
                    val stateData = apiService.getStates(request)
                    if (stateData != null) {
                        stateList = stateData.data.stateMaster
                    }
                }
                _refreshState.value = RefreshState.REFRESH_SUCCESS
            } catch (e: Exception) {
                Timber.d("Fetching states failed ${e.message}")
                _refreshState.value = RefreshState.REFRESH_FAILED
            }
        }
    }

    fun getDistrictList(stateId: Int) {
        viewModelScope.launch {
            try {
                _refreshState.value = RefreshState.REFRESHING
                withContext(Dispatchers.IO) {
                    val response = apiService.getDistricts(stateId)
                    if (response != null) {
                        districtList = response?.data
                    }
                }
                _refreshState.value = RefreshState.REFRESH_SUCCESS
            } catch (e: Exception) {
                Timber.d("Fetching districts failed ${e.message}")
                _refreshState.value = RefreshState.REFRESH_FAILED
            }
        }
    }

    fun getTaluks(districtId: Int) {
        viewModelScope.launch {
            try {
                _refreshState.value = RefreshState.REFRESHING
                withContext(Dispatchers.IO) {
                    val response = apiService.getDistrictBlocks(districtId)
                    if (response != null) {
                        blockList = response?.data
                    }
                }
                _refreshState.value = RefreshState.REFRESH_SUCCESS
            } catch (e: java.lang.Exception) {
                Timber.d("Fetching Taluks failed ${e.message}")
                _refreshState.value = RefreshState.REFRESH_FAILED
            }
        }
    }

    fun getVillages(blockId: Int) {
        viewModelScope.launch {
            try {
                _refreshState.value = RefreshState.REFRESHING
                withContext(Dispatchers.IO) {
                    val response = apiService.getVillages(blockId)
                    if (response != null) {
                        villageList = response?.data
                    }
                }
                _refreshState.value = RefreshState.REFRESH_SUCCESS
            } catch (e: Exception) {
                Timber.d("Fetching villages failed ${e.message}")
                _refreshState.value = RefreshState.REFRESH_FAILED
            }
        }
    }
}