package org.piramalswasthya.cho.ui.login_activity.username

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.AmritApiService
import javax.inject.Inject

@HiltViewModel
class UsernameViewModel@Inject constructor(
    private val pref: PreferenceDao


) : ViewModel(){

    @Inject
    lateinit var apiService: AmritApiService

    init{

    }

    fun rememberUser(username: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                pref.registerLoginCred(username)
            }
        }
    }
    fun fetchRememberedUserName(): String? =
        pref.getRememberedUserName()
}