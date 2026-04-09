package org.piramalswasthya.cho.work

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.cho.repositories.ProcedureRepo
import timber.log.Timber
import java.net.SocketTimeoutException


@HiltWorker
class PullLabRecordFormWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val patientRepo: ProcedureRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "PrescripTemplateWorker"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        init()
        return try {
            withContext(Dispatchers.IO) {
            patientRepo.ensureLabProcedureMasterSeed()
            Timber.d("PullLabRecordFormWorker completed: API fetched and database sync attempted")
            Result.success()
            }
        } catch (e: SocketTimeoutException) {
            Timber.e("Caught Exception for Patient Download worker $e")
            Result.retry()
        } catch (e: Exception) {
            Timber.e("PullLabRecordFormWorker failed while syncing lab procedure data", e)
            Result.failure()
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
