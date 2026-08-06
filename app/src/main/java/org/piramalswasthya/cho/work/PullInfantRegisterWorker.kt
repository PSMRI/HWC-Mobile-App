package org.piramalswasthya.cho.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.InfantRegRepo

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
        WorkerExecutionUtils.initAuthTokens(preferenceDao)
        return WorkerExecutionUtils.runBooleanWorker(
            successLog = "PullInfantRegisterWorker completed",
            failureLog = "PullInfantRegisterWorker failed",
            retryLog = "PullInfantRegisterWorker retry"
        ) {
            val infantResult = infantRegRepo.pullInfantsFromServer()
            val childResult = infantRegRepo.pullChildrenFromServer()
            infantResult && childResult
        }
    }
}
