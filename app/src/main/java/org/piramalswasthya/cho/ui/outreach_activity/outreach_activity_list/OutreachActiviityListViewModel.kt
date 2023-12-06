package org.piramalswasthya.cho.ui.outreach_activity.outreach_activity_list

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.model.BenHealthIdDetails
import org.piramalswasthya.cho.model.OutreachActivityNetworkModel
import org.piramalswasthya.cho.network.ActivityResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.repositories.ActivityRepo
import org.piramalswasthya.cho.ui.abha_id_activity.create_abha_id.CreateAbhaViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class OutreachActiviityListViewModel @Inject constructor(
    @ApplicationContext private val application: Context,
    private val activityRepo: ActivityRepo,
)  : ViewModel() {

    var activityList: List<OutreachActivityNetworkModel> = mutableListOf()

    private val _isDataLoaded= MutableLiveData<Boolean?>(null)

    val isDataLoaded: MutableLiveData<Boolean?>
        get() = _isDataLoaded

    init {
        getData()
    }

    fun getData(){
        viewModelScope.launch {
            when (val result = activityRepo.getActivityByUser()) {
                is NetworkResult.Success -> {
                    val data = result.data as ActivityResponse
                    activityList = data.activityList
                    _isDataLoaded.value = true
                }
                is NetworkResult.Error -> {
                    _isDataLoaded.value = false
                }
                is NetworkResult.NetworkError -> {
                    _isDataLoaded.value = false
                }
            }
        }
    }

    // TODO: Implement the ViewModel
}