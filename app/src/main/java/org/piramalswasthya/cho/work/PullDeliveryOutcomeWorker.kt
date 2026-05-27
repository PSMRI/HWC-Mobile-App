package org.piramalswasthya.cho.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.DeliveryOutcomeRepo

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
        WorkerExecutionUtils.initAuthTokens(preferenceDao)
        return WorkerExecutionUtils.runBooleanWorker(
            startLog = "PullDeliveryOutcomeWorker started",
            successLog = "PullDeliveryOutcomeWorker completed",
            failureLog = "PullDeliveryOutcomeWorker failed",
            retryLog = "Caught exception for PullDeliveryOutcomeWorker"
        ) {
            deliveryOutcomeRepo.pullDeliveryOutcomesFromServer()
        }
    }
}
