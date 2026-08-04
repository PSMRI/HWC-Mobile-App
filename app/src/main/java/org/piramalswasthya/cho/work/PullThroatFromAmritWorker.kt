package org.piramalswasthya.cho.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.ThroatDiagnosisRepo

@HiltWorker
class PullThroatFromAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: ThroatDiagnosisRepo,
    private val preferenceDao: PreferenceDao,
) : BasePullAmritSyncWorker(appContext, params, preferenceDao) {
    companion object {
        const val name = "PullThroatFromAmritWorker"
    }

    override suspend fun runSync(): Boolean = repo.pullThroatVisitsFromServer()
    override val successLog: String = "Pull Throat Worker completed"
    override val failureLog: String = "Pull Throat Worker Failed"
}
