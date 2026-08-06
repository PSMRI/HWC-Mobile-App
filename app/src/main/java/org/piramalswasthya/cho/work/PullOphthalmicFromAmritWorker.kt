package org.piramalswasthya.cho.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.OphthalmicRepository

@HiltWorker
class PullOphthalmicFromAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: OphthalmicRepository,
    private val preferenceDao: PreferenceDao,
) : BasePullAmritSyncWorker(appContext, params, preferenceDao) {
    companion object {
        const val name = "PullOphthalmicFromAmritWorker"
    }

    override suspend fun runSync(): Boolean = repo.pullOphthalmicVisitsFromServer()
    override val successLog: String = "Pull Ophthalmic Worker completed"
    override val failureLog: String = "Pull Ophthalmic Worker Failed"
}
