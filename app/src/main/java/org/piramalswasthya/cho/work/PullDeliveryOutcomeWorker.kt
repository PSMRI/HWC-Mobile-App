package org.piramalswasthya.cho.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.cho.repositories.DeliveryOutcomeRepo
import timber.log.Timber
import java.net.SocketTimeoutException

@HiltWorker
class PullDeliveryOutcomeWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val deliveryOutcomeRepo: DeliveryOutcomeRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "PullDeliveryOutcomeWorker"
    }

    override suspend fun doWork(): Result {
        init()
        return try {
            Timber.d("PullDeliveryOutcomeWorker started")
            val workerResult = deliveryOutcomeRepo.pullDeliveryOutcomesFromServer()
            if (workerResult) {
                Timber.d("PullDeliveryOutcomeWorker completed")
                Result.success()
            } else {
                Timber.d("PullDeliveryOutcomeWorker failed")
                Result.failure()
            }
        } catch (e: SocketTimeoutException) {
            Timber.e("Caught exception for PullDeliveryOutcomeWorker $e")
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
