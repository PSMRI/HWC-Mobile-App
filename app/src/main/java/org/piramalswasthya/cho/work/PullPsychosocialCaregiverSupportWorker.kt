package org.piramalswasthya.cho.work


import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.PsychosocialCaregiverSupportRepo
import org.piramalswasthya.cho.network.interceptors.TokenInsertTmcInterceptor
import timber.log.Timber
import java.net.SocketTimeoutException


@HiltWorker
class PullPsychosocialCaregiverSupportWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: PsychosocialCaregiverSupportRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {
    companion object {
        const val name = "PullPsychosocialCaregiverSupportWorker"
    }


    override suspend fun doWork(): Result {
        init()
        try {
            val workerResult = repo.pullPsychosocialCaregiverSupportVisitsFromServer()
            return if (workerResult) {
                Timber.d("Pull Psychosocial Caregiver Support Worker completed")
                Result.success()
            } else {
                Timber.d("Pull Psychosocial Caregiver Support Worker Failed")
                Result.failure()
            }
        } catch (e: SocketTimeoutException) {
            Timber.e("Caught Exception for pull amrit worker $e")
            return Result.retry()
        }
    }


    private fun init() {
        if (TokenInsertTmcInterceptor.getToken() == "")
            preferenceDao.getPrimaryApiToken()?.let {
                TokenInsertTmcInterceptor.setToken(it)
            }
        if (TokenInsertTmcInterceptor.getJwt() == "")
            preferenceDao.getJWTAmritToken()?.let {
                TokenInsertTmcInterceptor.setJwt(it)
            }
    }
}