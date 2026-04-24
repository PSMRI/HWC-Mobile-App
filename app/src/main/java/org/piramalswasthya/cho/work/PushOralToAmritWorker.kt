package org.piramalswasthya.cho.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.OralHealthRepo

@HiltWorker
class PushOralToAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: OralHealthRepo,
    private val preferenceDao: PreferenceDao,
) : BasePushAmritSyncWorker(appContext, params, preferenceDao) {
    companion object {
        const val name = "PushOralToAmritWorker"
    }

    override suspend fun runSync(): Boolean = repo.processOralVisits()
    override val successLog: String = "PushOralToAmritWorker: completed processing oral visits"
    override val failureLog: String = "PushOralToAmritWorker: failed processing oral visits"
}
