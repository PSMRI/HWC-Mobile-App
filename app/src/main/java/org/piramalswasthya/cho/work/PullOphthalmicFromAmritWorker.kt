package org.piramalswasthya.cho.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.cho.repositories.OphthalmicRepository
import timber.log.Timber
import java.net.SocketTimeoutException

@HiltWorker
class PullOphthalmicFromAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: OphthalmicRepository,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {
    companion object {
        const val name = "PullOphthalmicFromAmritWorker"
    }

    override suspend fun doWork(): Result {
        init()
        try {
            val workerResult = repo.pullOphthalmicVisitsFromServer()
            return if (workerResult) {
                Timber.d("Pull Ophthalmic Worker completed")
                Result.success()
            } else {
                Timber.d("Pull Ophthalmic Worker Failed")
                Result.failure()
            }
        } catch (e: SocketTimeoutException) {
            Timber.e("Caught Exception for pull amrit worker $e")
            return Result.retry()
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