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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class HomeActivityViewModel (application: Application) : AndroidViewModel(application) {

    private val _lastSyncTimestampLiveData = MutableLiveData<String>()

    val lastSyncTimestampLiveData: LiveData<String>
        get() = _lastSyncTimestampLiveData

    private val _pollState = MutableSharedFlow<SyncJobStatus>()

    val pollState: Flow<SyncJobStatus>
        get() = _pollState

    init {
//        Log.i("view model is launched", "")
//        viewModelScope.launch {
//            Sync.periodicSync<DemoFhirSyncWorker>(
//                application.applicationContext,
//                periodicSyncConfiguration =
//                PeriodicSyncConfiguration(
//                    syncConstraints = Constraints.Builder().build(),
//                    repeat = RepeatInterval(interval = 1, timeUnit = TimeUnit.MINUTES)
//                )
//            )
//                .shareIn(this, SharingStarted.Eagerly, 10)
//                .collect { _pollState.emit(it) }
//        }
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

    companion object {
        private const val formatString24 = "yyyy-MM-dd HH:mm:ss"
        private const val formatString12 = "yyyy-MM-dd hh:mm:ss a"
    }

}