package org.piramalswasthya.cho.ui.home

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.room.InAppDb
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.UserRepo

import org.piramalswasthya.cho.repositories.RegistrarMasterDataRepo

import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val database: InAppDb,
    private val pref: PreferenceDao,
    private val userRepo: UserRepo,
    private val userDao: UserDao,
    private val registrarMasterDataRepo: RegistrarMasterDataRepo,


    ) : ViewModel() {

    init {
        viewModelScope.launch{
            try {
                registrarMasterDataRepo.saveGovIdEntityMasterResponseToCache()
                registrarMasterDataRepo.saveOtherGovIdEntityMasterResponseToCache()
            }
            catch (e : Exception){

            }
        }
    }


    val scope: CoroutineScope
        get() = viewModelScope
    private var _unprocessedRecords: Int = 0
    val unprocessedRecords: Int
        get() = _unprocessedRecords



    private val _navigateToLoginPage = MutableLiveData(false)
    val navigateToLoginPage: MutableLiveData<Boolean>
        get() = _navigateToLoginPage


    fun logout() {
        viewModelScope.launch {
            _navigateToLoginPage.value = true
            userDao.resetAllUsersLoggedInState()
        }
    }

    fun navigateToLoginPageComplete() {
        _navigateToLoginPage.value = false
    }


}