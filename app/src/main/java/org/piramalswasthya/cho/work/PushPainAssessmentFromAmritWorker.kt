package org.piramalswasthya.cho.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.PainAndSymptomAssessmentRepo

@HiltWorker
class PushPainAssessmentFromAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: PainAndSymptomAssessmentRepo,
    private val preferenceDao: PreferenceDao,
) : BasePushAmritSyncWorker(appContext, params, preferenceDao) {
    companion object {
        const val name = "PushPainAssessmentFromAmritWorker"
    }

    override suspend fun runSync(): Boolean = repo.processPainvisits()
    override val successLog: String = "Worker completed"
    override val failureLog: String = "Worker Failed as usual!"
}
