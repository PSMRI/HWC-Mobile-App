package org.piramalswasthya.cho.work


import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.PsychosocialCaregiverSupportRepo


@HiltWorker
class PullPsychosocialCaregiverSupportWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: PsychosocialCaregiverSupportRepo,
    private val preferenceDao: PreferenceDao,
) : BasePullAmritSyncWorker(appContext, params, preferenceDao) {
    companion object {
        const val name = "PullPsychosocialCaregiverSupportWorker"
    }

    override suspend fun runSync(): Boolean = repo.pullPsychosocialCaregiverSupportVisitsFromServer()
    override val successLog: String = "Pull Psychosocial Caregiver Support Worker completed"
    override val failureLog: String = "Pull Psychosocial Caregiver Support Worker Failed"
}
