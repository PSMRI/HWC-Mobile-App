package org.piramalswasthya.cho.ui.home_activity

import android.app.Application
import android.os.Build
import android.text.format.DateFormat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import com.google.android.fhir.sync.PeriodicSyncConfiguration
import com.google.android.fhir.sync.RepeatInterval
import com.google.android.fhir.sync.Sync
import com.google.android.fhir.sync.SyncJobStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.database.room.InAppDb
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.RegistrarMasterDataRepo
import org.piramalswasthya.cho.repositories.UserRepo
import java.lang.Exception
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeActivityViewModel @Inject constructor (application: Application,
                                                 private val database: InAppDb,
                                                 private val pref: PreferenceDao,
                                                 private val userRepo: UserRepo,
                                                 private val userDao: UserDao,
                                                 private val registrarMasterDataRepo: RegistrarMasterDataRepo,) : AndroidViewModel(application) {

    private val _lastSyncTimestampLiveData = MutableLiveData<String>()

    val lastSyncTimestampLiveData: LiveData<String>
        get() = _lastSyncTimestampLiveData

    private val _pollState = MutableSharedFlow<SyncJobStatus>()

    val pollState: Flow<SyncJobStatus>
        get() = _pollState

    init {
        Log.i("view model is launched", "")
        viewModelScope.launch {
            try {
                registrarMasterDataRepo.saveGovIdEntityMasterResponseToCache()
                registrarMasterDataRepo.saveOtherGovIdEntityMasterResponseToCache()
            }
            catch (e : Exception){

            }

            Sync.periodicSync<DemoFhirSyncWorker>(
                application.applicationContext,
                periodicSyncConfiguration =
                PeriodicSyncConfiguration(
                    syncConstraints = Constraints.Builder().build(),
                    repeat = RepeatInterval(interval = 1, timeUnit = TimeUnit.MINUTES)
                )
            )
                .shareIn(this, SharingStarted.Eagerly, 10)
                .collect { _pollState.emit(it) }
        }
    }

    fun triggerOneTimeSync() {
        viewModelScope.launch {
            Sync.oneTimeSync<DemoFhirSyncWorker>(getApplication())
                .shareIn(this, SharingStarted.Eagerly, 10)
                .collect { _pollState.emit(it) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateLastSyncTimestamp() {
        val formatter =
            DateTimeFormatter.ofPattern(
                if (DateFormat.is24HourFormat(getApplication())) formatString24 else formatString12
            )
        _lastSyncTimestampLiveData.value =
            Sync.getLastSyncTimestamp(getApplication())?.toLocalDateTime()?.format(formatter) ?: ""
    }

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
    companion object {
        private const val formatString24 = "yyyy-MM-dd HH:mm:ss"
        private const val formatString12 = "yyyy-MM-dd hh:mm:ss a"
    }

}