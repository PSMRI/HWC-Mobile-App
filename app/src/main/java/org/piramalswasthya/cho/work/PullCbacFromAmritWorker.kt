package org.piramalswasthya.cho.work

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.cho.repositories.BenFlowRepo
import org.piramalswasthya.cho.repositories.CbacRepo
import timber.log.Timber
import java.net.SocketTimeoutException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@HiltWorker
class PullCbacFromAmritWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val preferenceDao: PreferenceDao,
    private val benFlowRepo: BenFlowRepo,
    private val cbacRepo: CbacRepo
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "PullCbacFromAmritWorker"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        init()
        return try {

            Log.d("Benflow In Progress", "Benflow In Progress")

            val currentInstant = Instant.now()
            val currentDateTime = LocalDateTime.ofInstant(currentInstant, ZoneId.of("Asia/Kolkata"))
            val startOfHour = currentDateTime.withMinute(0).withSecond(0).withNano(0)
            val currTimeStamp = startOfHour.atZone(ZoneId.of("Asia/Kolkata")).toInstant().toEpochMilli()

            val cbacResult = cbacRepo.downloadAndSyncCbacRecords()
            if (cbacResult) {
                preferenceDao.setLastCbacSyncTime(currTimeStamp)
            }

            val benflowResult = benFlowRepo.downloadAndSyncFlowRecords()
            if (benflowResult) {
                preferenceDao.setLastBenflowSyncTime(currTimeStamp)
            }

            Timber.d("Cbac + Benflow Download Worker completed")
            Result.success()
//            }
        } catch (e: SocketTimeoutException) {
            Timber.e("Caught Exception for push amrit worker $e")
            Result.retry()
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