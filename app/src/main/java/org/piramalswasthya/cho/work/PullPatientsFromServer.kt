package org.piramalswasthya.cho.work

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.cho.repositories.PatientRepo
import timber.log.Timber
import java.net.SocketTimeoutException
import java.util.Date
import java.sql.Timestamp


@HiltWorker
class PullPatientsFromServer @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val patientRepo: PatientRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "PullPatientFromPullBenFromServer"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        init()
        return try {
            val workerResult = patientRepo.downloadAndSyncPatientRecords()
//            if (workerResult) {
                preferenceDao.setLastPatientSyncTime()
//            }
            Timber.d("Patient Download Worker completed")
            Result.success()
        } catch (e: SocketTimeoutException) {
            Timber.e("Caught Exception for Patient Download worker $e")
            Result.retry()
        }
    }

    private fun init() {
        if (TokenInsertTmcInterceptor.getToken() == "")
            preferenceDao.getPrimaryApiToken()?.let{
                TokenInsertTmcInterceptor.setToken(it)
            }
    }
}
