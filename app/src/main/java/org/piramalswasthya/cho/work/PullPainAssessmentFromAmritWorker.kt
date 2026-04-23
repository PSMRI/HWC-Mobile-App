package org.piramalswasthya.cho.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.PainAndSymptomAssessmentRepo

@HiltWorker
class PullPainAssessmentFromAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: PainAndSymptomAssessmentRepo,
    private val preferenceDao: PreferenceDao,
) : BasePullAmritSyncWorker(appContext, params, preferenceDao) {
    companion object {
        const val name = "PullPainAssessmentFromAmritWorker"
    }

    override suspend fun runSync(): Boolean = repo.pullPainVisitsFromServer()
    override val successLog: String = "Pull Pain Worker completed"
    override val failureLog: String = "Pull Pain Worker Failed"
}
