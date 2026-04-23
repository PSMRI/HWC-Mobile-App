package org.piramalswasthya.cho.work


import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.OphthalmicRepository


@HiltWorker
class PushOphthalmicToAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: OphthalmicRepository,
    private val preferenceDao: PreferenceDao,
) : BasePushAmritSyncWorker(appContext, params, preferenceDao) {
    companion object {
        const val name = "PushOphthalmicToAmritWorker"
    }

    override suspend fun runSync(): Boolean = repo.processOphthalmicVisits()
    override val successLog: String = "Worker completed"
    override val failureLog: String = "Worker Failed as usual!"
}
