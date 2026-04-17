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
class PushPWRToAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val maternalHealthRepo: MaternalHealthRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {
    companion object {
        const val name = "PushPWRToAmritWorker"
    }

    override suspend fun doWork(): Result {
        WorkerExecutionUtils.initAuthTokens(preferenceDao)
        return WorkerExecutionUtils.runBooleanWorker(
            startLog = "PushPWRToAmritWorker started",
            successLog = "Worker completed",
            failureLog = "Worker Failed as usual!",
            retryLog = "Caught Exception for push amrit worker"
        ) {
            maternalHealthRepo.processNewPWRRecords()
        }
    }
}
