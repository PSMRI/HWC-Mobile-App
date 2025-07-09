package org.piramalswasthya.cho.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.cho.repositories.BenVisitRepo
import timber.log.Timber
import java.net.SocketTimeoutException

@HiltWorker
class PushPharmacistDataToAmrit @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val benVisitRepo: BenVisitRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "PushPharmacistDataToAmrit"
    }

    override suspend fun doWork(): Result {
        init()
        return try {
            val workerResult = benVisitRepo.processUnsyncedPharmacistData()
            if (workerResult) {
                Log.d("WU", "PushPharmacistDataToAmrit: success ")
                Timber.d("Worker completed")
                Result.success()
            } else {
                Log.d("WU", "PushPharmacistDataToAmrit: false")
                Timber.d("Worker Failed as usual!")
                Result.retry()
            }
        } catch (e: SocketTimeoutException) {
            Log.d("WU", "PushPharmacistDataToAmrit:error ")
            Timber.e("Caught Exception for push amrit worker $e")
            Result.retry()
        }
    }

    private fun init() {
        if (TokenInsertTmcInterceptor.getToken() == "")
            preferenceDao.getPrimaryApiToken()?.let {
                TokenInsertTmcInterceptor.setToken(it)
            }
        if (TokenInsertTmcInterceptor.getJwt() == "")
            preferenceDao.getJWTAmritToken()?.let {
                TokenInsertTmcInterceptor.setJwt(it)
            }
    }

}