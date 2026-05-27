package org.piramalswasthya.cho.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.EcrRepo

@HiltWorker
class PullEligibleCouplesWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val ecrRepo: EcrRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "PullEligibleCouplesWorker"
    }

    override suspend fun doWork(): Result {
        WorkerExecutionUtils.initAuthTokens(preferenceDao)
        return WorkerExecutionUtils.runBooleanWorker(
            startLog = "PullEligibleCouplesWorker started!",
            successLog = "PullEligibleCouplesWorker completed successfully",
            failureLog = "PullEligibleCouplesWorker failed",
            retryLog = "Caught Exception for pull EC worker"
        ) {
            ecrRepo.pullEligibleCouplesFromServer()
        }
    }
}
