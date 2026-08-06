package org.piramalswasthya.cho.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.cho.repositories.BenVisitRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import timber.log.Timber
import java.net.SocketTimeoutException

@HiltWorker
class PushBenVisitInfoToAmrit @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val benVisitRepo: BenVisitRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "PushBenVisitInfoToAmrit"

        // Initial run + up to 2 retries (3 executions total). The nurse push is gated on the
        // beneficiary registration (beneficiaryRegID) and the server BenFlow (nurseFlag==1) having
        // landed in the local DB. On a slow/flaky network those prerequisites can arrive after this
        // worker first runs, so we retry with WorkManager backoff to let the upstream
        // PushBenToAmritWorker / benflow pull catch up. After the cap we release the chain — the
        // periodic down-sync + next push recover with no data loss.
        private const val MAX_RUN_ATTEMPTS = 3
    }

    override suspend fun doWork(): Result {
        init()
        try {
            val allResolved = benVisitRepo.processUnsyncedNurseData()
            return if (allResolved) {
                Timber.d("Worker completed")
                Result.success()
            } else if (runAttemptCount < MAX_RUN_ATTEMPTS - 1) {
                // Some visits were skipped because beneficiaryRegID or the benflow (nurseFlag==1)
                // had not landed locally yet. Retry with backoff so the prerequisite registration /
                // benflow pull can catch up, instead of leaving the visit UNSYNCED.
                Timber.d("Nurse push has pending preconditions; retry attempt ${runAttemptCount + 1}")
                Result.retry()
            } else {
                Timber.d("Nurse push still has pending preconditions after $MAX_RUN_ATTEMPTS attempts; releasing chain")
                Result.success()
            }
        } catch (e: SocketTimeoutException) {
            Timber.e("Caught Exception for push amrit worker $e")
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