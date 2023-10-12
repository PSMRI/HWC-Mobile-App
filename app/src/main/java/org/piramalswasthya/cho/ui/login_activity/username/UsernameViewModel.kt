package org.piramalswasthya.cho.ui.login_activity.username

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.repositories.OutreachRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.ui.login_activity.cho_login.outreach.OutreachViewModel
import javax.inject.Inject

@HiltViewModel
class UsernameViewModel@Inject constructor(
    private val pref: PreferenceDao,
    private val outreachRepo: OutreachRepo,
    private val userRepo: UserRepo,

    ) : ViewModel(){
    val _state = MutableLiveData(OutreachViewModel.State.IDLE)
    val state: LiveData<OutreachViewModel.State>
        get() = _state
    @Inject
    lateinit var apiService: AmritApiService

    init{

    }
  fun getOutreach(){
      viewModelScope.launch {
          outreachRepo.getOutreachDropdownListData()
      }
  }
    fun fetchRememberedUserName(): String? =
        pref.getRememberedUserName()
    fun authUser(
        username: String,
        password: String,
        loginType: String?,
        selectedOption: String?,
        loginTimeStamp: String?,
        logoutTimeStamp: String?,
        lat: Double?,
        long: Double?,
        logoutType: String?
    ) {
        _state.value  = OutreachViewModel.State.SAVING
        viewModelScope.launch {
            _state.value = userRepo.authenticateUser(
                username,
                password,
                loginType,
                selectedOption,
                loginTimeStamp,
                logoutTimeStamp,
                lat,
                long,
                logoutType,
            true)
        }
    }
    fun resetState() {
        _state.value = OutreachViewModel.State.IDLE
    }
}