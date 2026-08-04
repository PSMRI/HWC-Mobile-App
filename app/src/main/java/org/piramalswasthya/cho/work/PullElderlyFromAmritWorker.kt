package org.piramalswasthya.cho.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.ElderlyHealthRepo

@HiltWorker
class PullElderlyFromAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: ElderlyHealthRepo,
    private val preferenceDao: PreferenceDao,
) : BasePullAmritSyncWorker(appContext, params, preferenceDao) {
    companion object {
        const val name = "PullElderlyFromAmritWorker"
    }

    override suspend fun runSync(): Boolean = repo.pullElderlyVisitsFromServer()
    override val successLog: String = "Pull Elderly Worker completed"
    override val failureLog: String = "Pull Elderly Worker Failed"
}
