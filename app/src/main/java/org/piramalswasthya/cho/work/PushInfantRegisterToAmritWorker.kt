package org.piramalswasthya.cho.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.cho.repositories.InfantRegRepo
import timber.log.Timber
import java.net.SocketTimeoutException

@HiltWorker
class PushInfantRegisterToAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val infantRegRepo: InfantRegRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "PushInfantRegisterToAmritWorker"
    }

    override suspend fun doWork(): Result {
        init()
        return try {
            val workerResult = infantRegRepo.processNewInfantRegister()
            if (workerResult) {
                Timber.d("Infant registration push worker completed")
                Result.success()
            } else {
                Timber.d("Infant registration push worker failed")
                Result.failure()
            }
        } catch (e: SocketTimeoutException) {
            Timber.e(e, "Infant registration push worker retry")
            Result.retry()
        }
    }

    private fun init() {
        if (TokenInsertTmcInterceptor.getToken().isBlank()) {
            preferenceDao.getPrimaryApiToken()?.let {
                TokenInsertTmcInterceptor.setToken(it)
            }
        }
        if (TokenInsertTmcInterceptor.getJwt().isBlank()) {
            preferenceDao.getJWTAmritToken()?.let {
                TokenInsertTmcInterceptor.setJwt(it)
            }
        }
    }
}
