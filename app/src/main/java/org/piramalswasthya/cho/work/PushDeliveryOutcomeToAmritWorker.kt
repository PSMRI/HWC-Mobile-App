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
class PushDeliveryOutcomeToAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val deliveryOutcomeRepo: DeliveryOutcomeRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "PushDeliveryOutcomeToAmritWorker"
    }

    override suspend fun doWork(): Result {
        init()
        return try {
            Timber.d("PushDeliveryOutcomeToAmritWorker started")
            val workerResult = deliveryOutcomeRepo.processNewDeliveryOutcomes()
            if (workerResult) {
                Timber.d("PushDeliveryOutcomeToAmritWorker completed")
                Result.success()
            } else {
                Timber.d("PushDeliveryOutcomeToAmritWorker failed")
                Result.failure()
            }
        } catch (e: SocketTimeoutException) {
            Timber.e("Caught exception for PushDeliveryOutcomeToAmritWorker $e")
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
