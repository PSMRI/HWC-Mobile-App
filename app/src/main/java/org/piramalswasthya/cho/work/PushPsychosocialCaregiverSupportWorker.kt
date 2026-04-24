package org.piramalswasthya.cho.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.PsychosocialCaregiverSupportRepo

@HiltWorker
class PushPsychosocialCaregiverSupportWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: PsychosocialCaregiverSupportRepo,
    private val preferenceDao: PreferenceDao,
) : BasePushAmritSyncWorker(appContext, params, preferenceDao) {
    companion object {
        const val name = "PushPsychosocialCaregiverSupportWorker"
    }

    override suspend fun runSync(): Boolean = repo.processPsychosocialCaregiverVisits()
    override val successLog: String = "Psychosocial Caregiver Support worker completed"
    override val failureLog: String = "Psychosocial Caregiver Support worker failed"
}
