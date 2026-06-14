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
import timber.log.Timber
import java.net.SocketTimeoutException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Pulls the per-beneficiary nurse (beneficiaryGeneralOPDNurseFormDataToApp) and doctor
 * (getBenCaseRecordFromDoctorGeneralOPD) case records for the current benflow payload.
 *
 * This is the slow part of the benflow downsync — ~2 API calls per beneficiary. The per-beneficiary
 * pulls are now run with bounded concurrency inside BenFlowRepo.syncFlowIds rather than one at a
 * time. This worker is enqueued in the SAME parallel stage as the RMNCH/CPHC pulls (not last,
 * behind them) so that:
 *  - the role worklists (PATIENT_VISIT_INFO_SYNC), which are created early by the worklist-only
 *    benflow pass in [org.piramalswasthya.sakhi.work.PullBenFlowFromAmritWorker], appear quickly,
 *  - the case-record data is pulled concurrently with RMNCH/CPHC so it is available early, and
 *  - the RMNCH and CPHC pulls are still not blocked behind the per-beneficiary loop.
 *
 * The benflow watermark ([PreferenceDao.setLastBenflowSyncTime]) is advanced HERE, not in the
 * worklist pass. The doctor pull condition is not idempotent across re-derivation, so it relies
 * on the watermark to bound re-pulls; keeping the watermark un-advanced until this worker runs
 * guarantees the same fresh, watermark-gated payload is still available to it.
 */
@HiltWorker
class PullClinicalRecordsWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val preferenceDao: PreferenceDao,
    private val benFlowRepo: BenFlowRepo
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "PullClinicalRecordsWorker"

        // Initial run + up to 2 retries (3 executions total). A retry fires when the
        // benflow->patient join still misses; the watermark is only advanced on a fully
        // matched run, so an incomplete run is recovered by the next periodic down-sync.
        private const val MAX_RUN_ATTEMPTS = 3
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        init()
        return try {
            Log.d("ClinicalRecords In Progress", "ClinicalRecords In Progress")

            val currentInstant = Instant.now()
            val currentDateTime = LocalDateTime.ofInstant(currentInstant, ZoneId.of("Asia/Kolkata"))
            val startOfHour = currentDateTime.withMinute(0).withSecond(0).withNano(0)
            val currTimeStamp = startOfHour.atZone(ZoneId.of("Asia/Kolkata")).toInstant().toEpochMilli()

            val workerResult = benFlowRepo.downloadAndSyncFlowRecords(pullClinical = true)
            if (workerResult) {
                preferenceDao.setLastBenflowSyncTime(currTimeStamp)
                Timber.d("Clinical records download worker completed")
                Result.success()
            } else if (runAttemptCount < MAX_RUN_ATTEMPTS - 1) {
                Timber.d("Clinical records download incomplete (patient join missed); retry attempt ${runAttemptCount + 1}")
                Result.retry()
            } else {
                Timber.d("Clinical records download still incomplete after $MAX_RUN_ATTEMPTS attempts; releasing chain")
                Result.success()
            }
        } catch (e: SocketTimeoutException) {
            Timber.e("Caught Exception for clinical records worker $e")
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
