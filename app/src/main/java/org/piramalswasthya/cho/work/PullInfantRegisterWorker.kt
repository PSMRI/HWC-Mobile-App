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
class PullInfantRegisterWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val infantRegRepo: InfantRegRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "PullInfantRegisterWorker"
    }

    override suspend fun doWork(): Result {
        init()
        return try {
            val workerResult = infantRegRepo.pullInfantsFromServer()
            if (workerResult) {
                Timber.d("PullInfantRegisterWorker completed")
                Result.success()
            } else {
                Timber.d("PullInfantRegisterWorker failed")
                Result.failure()
            }
        } catch (e: SocketTimeoutException) {
            Timber.e(e, "PullInfantRegisterWorker retry")
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
