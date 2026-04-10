package org.piramalswasthya.cho.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import timber.log.Timber
import java.net.SocketTimeoutException

@HiltWorker
class PullAncVisitsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val maternalHealthRepo: MaternalHealthRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "PullAncVisitsWorker"
    }

    override suspend fun doWork(): Result {
        init()
        return try {
            Timber.d("PullAncVisitsWorker started")
            val workerResult = maternalHealthRepo.pullAncVisitsFromServer()
            if (workerResult) {
                Timber.d("PullAncVisitsWorker completed successfully")
                Result.success()
            } else {
                Timber.d("PullAncVisitsWorker failed")
                Result.failure()
            }
        } catch (e: SocketTimeoutException) {
            Timber.e("Caught exception for PullAncVisitsWorker $e")
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
