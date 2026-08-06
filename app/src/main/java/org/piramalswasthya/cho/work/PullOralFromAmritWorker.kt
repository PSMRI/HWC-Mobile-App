package org.piramalswasthya.cho.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.OralHealthRepo

@HiltWorker
class PullOralFromAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: OralHealthRepo,
    private val preferenceDao: PreferenceDao,
) : BasePullAmritSyncWorker(appContext, params, preferenceDao) {
    companion object {
        const val name = "PullOralFromAmritWorker"
    }

    override suspend fun runSync(): Boolean = repo.pullOralVisitsFromServer()
    override val successLog: String = "Pull Oral Worker completed"
    override val failureLog: String = "Pull Oral Worker Failed"
}
