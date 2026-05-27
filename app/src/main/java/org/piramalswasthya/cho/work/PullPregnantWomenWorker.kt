package org.piramalswasthya.cho.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.MaternalHealthRepo

@HiltWorker
class PullPregnantWomenWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val maternalHealthRepo: MaternalHealthRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "PullPregnantWomenWorker"
    }

    override suspend fun doWork(): Result {
        WorkerExecutionUtils.initAuthTokens(preferenceDao)
        return WorkerExecutionUtils.runBooleanWorker(
            startLog = "PullPregnantWomenWorker started",
            successLog = "PullPregnantWomenWorker completed successfully",
            failureLog = "PullPregnantWomenWorker failed",
            retryLog = "Caught exception for PullPregnantWomenWorker"
        ) {
            maternalHealthRepo.pullPregnantWomenFromServer()
        }
    }
}
