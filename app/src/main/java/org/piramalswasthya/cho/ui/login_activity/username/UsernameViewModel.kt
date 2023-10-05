package org.piramalswasthya.cho.ui.login_activity.username

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.repositories.OutreachRepo
import javax.inject.Inject

@HiltViewModel
class UsernameViewModel@Inject constructor(
    private val pref: PreferenceDao,
     private val outreachRepo: OutreachRepo

) : ViewModel(){

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
}