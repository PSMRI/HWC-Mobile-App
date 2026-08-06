package org.piramalswasthya.cho.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.cho.repositories.BenVisitRepo
import org.piramalswasthya.cho.repositories.UserRepo
import timber.log.Timber
import java.net.SocketTimeoutException


@HiltWorker
class PushLoginAuditDataWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val userRepo: UserRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "PushLoginAuditDataToAmrit"
    }

    override suspend fun doWork(): Result {
        init()
        return try {
            val workerResult = userRepo.processUnsyncedAuditData()
            if (workerResult) {
                Timber.d("Worker completed")
                Result.success()
            } else {
                Timber.d("Worker Failed as usual!")
                Result.failure()
            }
        } catch (e: SocketTimeoutException) {
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