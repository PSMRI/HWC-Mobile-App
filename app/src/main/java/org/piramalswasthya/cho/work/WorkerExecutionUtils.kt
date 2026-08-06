package org.piramalswasthya.cho.work

import androidx.work.ListenableWorker.Result
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.interceptors.TokenInsertTmcInterceptor
import timber.log.Timber
import java.net.SocketTimeoutException

object WorkerExecutionUtils {

    fun initAuthTokens(preferenceDao: PreferenceDao) {
        if (TokenInsertTmcInterceptor.getToken().isBlank()) {
            preferenceDao.getPrimaryApiToken()?.let(TokenInsertTmcInterceptor::setToken)
        }
        if (TokenInsertTmcInterceptor.getJwt().isBlank()) {
            preferenceDao.getJWTAmritToken()?.let(TokenInsertTmcInterceptor::setJwt)
        }
    }

    suspend fun runBooleanWorker(
        startLog: String? = null,
        successLog: String,
        failureLog: String,
        retryLog: String,
        work: suspend () -> Boolean
    ): Result {
        return try {
            if (!startLog.isNullOrBlank()) {
                Timber.d(startLog)
            }
            if (work()) {
                Timber.d(successLog)
                Result.success()
            } else {
                Timber.d(failureLog)
                Result.failure()
            }
        } catch (e: SocketTimeoutException) {
            Timber.e(e, retryLog)
            Result.retry()
        }
    }
}
