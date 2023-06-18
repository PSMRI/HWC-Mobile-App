package org.piramalswasthya.cho.ui.login_activity.cho_login.outreach

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.UserAuthRepo
import org.piramalswasthya.cho.repositories.UserRepo
import javax.inject.Inject

@HiltViewModel
class OutreachViewModel @Inject constructor(
    private val userRepo: UserRepo,
    private val userAuthRepo: UserAuthRepo,
    private val pref: PreferenceDao
) : ViewModel() {
    // TODO: Implement the ViewModel

    enum class State {
        IDLE,
        LOADING,
        ERROR_INPUT,
        ERROR_SERVER,
        ERROR_NETWORK,
        SUCCESS
    }

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state

    fun loginInClicked() {
        _state.value = State.LOADING
    }

    fun dummyAuthUser(username: String, password: String) {
        userAuthRepo.authenticateUser(username, password);
    }

    fun authUser(username: String, password: String, state: String) {
        viewModelScope.launch {
            //Temporary Placement - need to move to  assets and load from there.
            _state.value = userRepo.authenticateUser(username, password, state)

        }
    }

}