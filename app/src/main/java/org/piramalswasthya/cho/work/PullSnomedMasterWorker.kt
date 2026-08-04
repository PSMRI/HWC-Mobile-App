package org.piramalswasthya.cho.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.cho.repositories.SnomedDiagnosisRepo
import java.net.SocketTimeoutException

@HiltWorker
class PullSnomedMasterWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: SnomedDiagnosisRepo,
    private val preferenceDao: PreferenceDao
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        if (TokenInsertTmcInterceptor.getToken().isEmpty()) preferenceDao.getPrimaryApiToken()?.let(TokenInsertTmcInterceptor::setToken)
        if (TokenInsertTmcInterceptor.getJwt().isEmpty()) preferenceDao.getJWTAmritToken()?.let(TokenInsertTmcInterceptor::setJwt)
        return try {
            if (repo.pullMaster()) Result.success() else Result.failure()
        } catch (_: SocketTimeoutException) {
            Result.retry()
        }
    }
}
