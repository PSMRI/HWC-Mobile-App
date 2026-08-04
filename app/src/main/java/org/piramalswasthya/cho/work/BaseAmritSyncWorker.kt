package org.piramalswasthya.cho.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.interceptors.TokenInsertTmcInterceptor
import timber.log.Timber
import java.net.SocketTimeoutException

abstract class BaseAmritSyncWorker(
    appContext: Context,
    params: WorkerParameters,
    private val preferenceDao: PreferenceDao
) : CoroutineWorker(appContext, params) {

    protected abstract suspend fun runSync(): Boolean
    protected abstract val successLog: String
    protected abstract val failureLog: String
    protected abstract val timeoutLogScope: String

    final override suspend fun doWork(): Result {
        initTokens()
        return try {
            val workerResult = runSync()
            if (workerResult) {
                Timber.d(successLog)
                Result.success()
            } else {
                Timber.d(failureLog)
                Result.failure()
            }
        } catch (e: SocketTimeoutException) {
            Timber.e("Caught Exception for $timeoutLogScope $e")
            Result.retry()
        }
    }

    private fun initTokens() {
        if (TokenInsertTmcInterceptor.getToken() == "") {
            preferenceDao.getPrimaryApiToken()?.let {
                TokenInsertTmcInterceptor.setToken(it)
            }
        }
        if (TokenInsertTmcInterceptor.getJwt() == "") {
            preferenceDao.getJWTAmritToken()?.let {
                TokenInsertTmcInterceptor.setJwt(it)
            }
        }
    }
}

abstract class BasePushAmritSyncWorker(
    appContext: Context,
    params: WorkerParameters,
    preferenceDao: PreferenceDao
) : BaseAmritSyncWorker(appContext, params, preferenceDao) {
    final override val timeoutLogScope: String = "push amrit worker"
}

abstract class BasePullAmritSyncWorker(
    appContext: Context,
    params: WorkerParameters,
    preferenceDao: PreferenceDao
) : BaseAmritSyncWorker(appContext, params, preferenceDao) {
    final override val timeoutLogScope: String = "pull amrit worker"
}
