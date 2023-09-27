package org.piramalswasthya.cho.ui.login_activity

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.launch
import okhttp3.internal.userAgent
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.ui.home.HomeViewModel
import timber.log.Timber
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val userRepo: UserRepo
) : ViewModel() {

    private val _isLoggedInStatus = MutableLiveData<Int>(-1)

    val isLoggedInStatus: LiveData<Int>
        get() = _isLoggedInStatus


    private fun isLoggedInStatusFun(logd:Int) {
            try {
                _isLoggedInStatus.value = logd
            } catch (e: Exception) {
                Timber.d("Error in login $e")
            }
    }
}

