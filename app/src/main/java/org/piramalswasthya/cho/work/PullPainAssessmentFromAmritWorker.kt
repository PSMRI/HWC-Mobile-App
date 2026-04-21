package org.piramalswasthya.cho.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.PainAndSymptomAssessmentRepo
import org.piramalswasthya.cho.network.interceptors.TokenInsertTmcInterceptor
import timber.log.Timber
import java.net.SocketTimeoutException

@HiltWorker
class PullPainAssessmentFromAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: PainAndSymptomAssessmentRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {
    companion object {
        const val name = "PullPainAssessmentFromAmritWorker"
    }

    override suspend fun doWork(): Result {
        init()
        try {
            val workerResult = repo.pullPainVisitsFromServer()
            return if (workerResult) {
                Timber.d("Pull Pain Worker completed")
                Result.success()
            } else {
                Timber.d("Pull Pain Worker Failed")
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