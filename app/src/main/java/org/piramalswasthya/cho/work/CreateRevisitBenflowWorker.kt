package org.piramalswasthya.cho.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.cho.repositories.BenFlowRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.UserRepo
import timber.log.Timber
import java.net.SocketTimeoutException

@HiltWorker
class CreateRevisitBenflowWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val userRepo: UserRepo,
    private val patientRepo: PatientRepo,
    private val preferenceDao: PreferenceDao,
    private val benFlowRepo: BenFlowRepo
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "CreateRevisitBenflowWorker"
    }

    override suspend fun doWork(): Result {
        init()
//        try {
////                setForeground(createForegroundInfo("Downloading"))
//        } catch (throwable: Throwable) {
//            Timber.d("FgLW", "Something bad happened", throwable)
//        }
        try {
            val workerResult = benFlowRepo.createRevisitBenflowRecords()
            return if (workerResult) {
                Timber.d("Worker completed")
                Result.success()
            } else {
                Timber.d("Worker Failed as usual!")
                Result.failure()
            }
        } catch (e: SocketTimeoutException) {
            Timber.e("Caught Exception for push amrit worker $e")
            return Result.retry()
        }

    }

    private fun init() {
        if (TokenInsertTmcInterceptor.getToken() == "")
            preferenceDao.getPrimaryApiToken()?.let{
                TokenInsertTmcInterceptor.setToken(it)
            }
    }

}
