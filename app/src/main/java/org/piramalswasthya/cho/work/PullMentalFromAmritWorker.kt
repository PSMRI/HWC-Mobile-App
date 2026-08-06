package org.piramalswasthya.cho.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.MentalHealthScreeningRepo

@HiltWorker
class PullMentalFromAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: MentalHealthScreeningRepo,
    private val preferenceDao: PreferenceDao,
) : BasePullAmritSyncWorker(appContext, params, preferenceDao) {
    companion object {
        const val name = "PullMentalFromAmritWorker"
    }

    override suspend fun runSync(): Boolean = repo.pullMentalVisitsFromServer()
    override val successLog: String = "Pull Mental Worker completed"
    override val failureLog: String = "Pull Mental Worker Failed"
}
