package org.piramalswasthya.cho.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.PncRepo

@HiltWorker
class PullPncFromAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val pncRepo: PncRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "PullPncFromAmritWorker"
    }

    override suspend fun doWork(): Result {
        WorkerExecutionUtils.initAuthTokens(preferenceDao)
        return WorkerExecutionUtils.runBooleanWorker(
            successLog = "PullPncFromAmritWorker completed",
            failureLog = "PullPncFromAmritWorker failed",
            retryLog = "PullPncFromAmritWorker retry"
        ) {
            pncRepo.pullPncVisitsFromServer()
        }
    }
}
