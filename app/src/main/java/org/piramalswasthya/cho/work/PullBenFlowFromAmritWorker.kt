package org.piramalswasthya.sakhi.work

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.VillageIdList
import org.piramalswasthya.cho.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.cho.repositories.BenFlowRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.work.WorkerUtils
import org.piramalswasthya.cho.work.WorkerUtils.amritSyncInProgress
import org.piramalswasthya.cho.work.WorkerUtils.downloadSyncInProgress
import timber.log.Timber
import java.net.SocketTimeoutException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

@HiltWorker
class PullBenFlowFromAmritWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val userRepo: UserRepo,
    private val patientRepo: PatientRepo,
    private val preferenceDao: PreferenceDao,
    private val benFlowRepo: BenFlowRepo
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "PullBenFlowFromAmritWorker"
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

                val workerResult = benFlowRepo.downloadAndSyncFlowRecords()
                if (workerResult) {
                    preferenceDao.setLastBenflowSyncTime(currTimeStamp)
                }

                Timber.d("Benflow Download Worker completed")
                Result.success()
//            }
        } catch (e: SocketTimeoutException) {
            Timber.e("Caught Exception for push amrit worker $e")
            Result.retry()
        }

    }

    private fun init() {
        if (TokenInsertTmcInterceptor.getToken() == "")
            preferenceDao.getPrimaryApiToken()?.let{
                TokenInsertTmcInterceptor.setToken(it)
            }
    }

}