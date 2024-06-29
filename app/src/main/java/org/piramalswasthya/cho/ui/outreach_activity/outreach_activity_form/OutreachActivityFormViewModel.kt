package org.piramalswasthya.cho.ui.outreach_activity.outreach_activity_form

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.model.OutreachActivityModel
import org.piramalswasthya.cho.model.OutreachActivityNetworkModel
import org.piramalswasthya.cho.network.ActivityResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.repositories.ActivityRepo
import org.piramalswasthya.cho.repositories.BenFlowRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.PatientVisitInfoSyncRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.utils.ImgUtils
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class OutreachActivityFormViewModel @Inject constructor(
    @ApplicationContext private val application: Context,
    private val activityRepo: ActivityRepo
) : ViewModel() {

    var activityList: List<String> = listOf(
        "School Visit",
        "Special Screening Camp",
        "Community Meeting",
        "Yoga",
        "Meditation",
        "Awareness Activity",
        "Other"
    )

    val outreachActivityModel: OutreachActivityModel = OutreachActivityModel()

    private val _isDataSaved = MutableLiveData<Boolean?>(null)

    val isDataSaved: MutableLiveData<Boolean?>
        get() = _isDataSaved

    var activities: List<OutreachActivityNetworkModel> = mutableListOf()

    init {
        getData()
    }

    private fun getData(){
        viewModelScope.launch {
            when (val result = activityRepo.getActivityByUser()) {
                is NetworkResult.Success -> {
                    val data = result.data as ActivityResponse
                    activities = data.activityList
                }

                is NetworkResult.Error -> {
                    activities = emptyList()
                }
                NetworkResult.NetworkError -> {
                    activities = emptyList()
                }
            }
        }
    }
    fun saveNewActivity(activity: OutreachActivityModel){
        val act = activity
        var st = act.img1
        viewModelScope.launch {
            when(val result = activityRepo.saveNewActivity(activity)){
                is NetworkResult.Success -> {
                    _isDataSaved.value = true
                }
                is NetworkResult.Error -> {
                    _isDataSaved.value = false
                }
                is NetworkResult.NetworkError -> {
                    _isDataSaved.value = false
                }
            }
        }
    }

}