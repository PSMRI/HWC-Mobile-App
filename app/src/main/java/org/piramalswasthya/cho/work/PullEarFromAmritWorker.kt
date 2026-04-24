package org.piramalswasthya.cho.work


import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.EarDiagnosisRepo


@HiltWorker
class PullEarFromAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: EarDiagnosisRepo,
    private val preferenceDao: PreferenceDao,
) : BasePullAmritSyncWorker(appContext, params, preferenceDao) {
    companion object {
        const val name = "PullEarFromAmritWorker"
    }

    override suspend fun runSync(): Boolean = repo.pullEarVisitsFromServer()
    override val successLog: String = "Pull Ear Worker completed"
    override val failureLog: String = "Pull Ear Worker Failed"
}
