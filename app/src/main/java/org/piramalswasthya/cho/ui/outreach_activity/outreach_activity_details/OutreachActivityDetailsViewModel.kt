package org.piramalswasthya.cho.ui.outreach_activity.outreach_activity_details

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.model.OutreachActivityNetworkModel
import org.piramalswasthya.cho.network.ActivityResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.repositories.ActivityRepo
import org.piramalswasthya.cho.ui.commons.immunization_due.child_immunization.form.ImmunizationFormFragmentArgs
import javax.inject.Inject

@HiltViewModel
class OutreachActivityDetailsViewModel @Inject constructor(
    @ApplicationContext private val application: Context,
    savedStateHandle: SavedStateHandle,
    private val activityRepo: ActivityRepo
) : ViewModel() {

    var outreachActivityNetworkModel: OutreachActivityNetworkModel? = null

    private val _isDataLoaded= MutableLiveData<Boolean?>(null)

    val isDataLoaded: MutableLiveData<Boolean?>
        get() = _isDataLoaded

    fun loadData(activityId: Int){
        viewModelScope.launch {
            when(val respose = activityRepo.getActivityById(activityId)){
                is NetworkResult.Success -> {
                    outreachActivityNetworkModel = respose.data as OutreachActivityNetworkModel
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