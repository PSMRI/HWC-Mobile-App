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

        // Set on the request (via input data) when this worker runs inside an upsync/form-save
        // chain purely to land the just-pushed patient's benflow so the nurse/doctor push can
        // proceed. In that mode an incomplete run is RELEASED immediately (no retry): the local
        // patient we just registered always matches on the first pass, so its benflow row is
        // inserted regardless of other beneficiaries. Retrying would re-download the full village
        // payload 3x with backoff and block the push chain for no benefit — the other
        // beneficiaries' worklists are recovered by the periodic/full down-sync instead.
        const val KEY_RELEASE_ON_INCOMPLETE = "release_on_incomplete"

        // Set on the request (via input data) when this worklist pull runs in the upsync/form-save
        // chain to land a JUST-created benflow. It pulls the full worklist (epoch watermark) instead
        // of the incremental hour-truncated watermark, which would otherwise filter out a benflow
        // created seconds earlier and leave the nurse/doctor push gate permanently UNSYNCED. Safe
        // because the worklist pass does not advance the watermark.
        const val KEY_IGNORE_WATERMARK = "ignore_watermark"

        // Initial run + up to 2 retries (3 executions total). A retry fires when the
        // benflow->patient join misses because the concurrent patient pull hasn't
        // landed the row yet; by the retry the patient pull has typically finished. We
        // cap attempts and then release the chain with success() — the watermark is
        // still at epoch on an incomplete run, so the next periodic down-sync
        // re-downloads the full payload and recovers with no data loss.
        private const val MAX_RUN_ATTEMPTS = 3
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        init()
        return try {

                Log.d("Benflow In Progress", "Benflow In Progress")

                // Worklist-only pass: inserts benflow rows and builds the role worklists
                // (PATIENT_VISIT_INFO_SYNC) so the Nurse/Doctor/Lab/Pharmacist lists appear early.
                // The slow per-beneficiary nurse/doctor case-record pull (~2 API calls per
                // beneficiary) AND the benflow watermark advance are deferred to
                // PullClinicalRecordsWorker at the END of the chain, so the RMNCH/CPHC pulls are
                // not blocked behind the per-beneficiary loop. Because the watermark is NOT
                // advanced here, the same fresh payload remains available to the clinical worker.
                val ignoreWatermark = inputData.getBoolean(KEY_IGNORE_WATERMARK, false)
                val workerResult = benFlowRepo.downloadAndSyncFlowRecords(pullClinical = false, ignoreWatermark = ignoreWatermark)
                val releaseOnIncomplete = inputData.getBoolean(KEY_RELEASE_ON_INCOMPLETE, false)
                if (workerResult) {
                    Timber.d("Benflow worklist sync completed")
                    Result.success()
                } else if (releaseOnIncomplete) {
                    // Upsync/form-save path: the just-pushed patient's benflow is already inserted
                    // in this single pass, so the nurse/doctor push can proceed now. Skip the
                    // full-payload retry storm (other beneficiaries are recovered by down-sync).
                    Timber.d("Benflow worklist incomplete; releasing upsync chain without retry")
                    Result.success()
                } else if (runAttemptCount < MAX_RUN_ATTEMPTS - 1) {
                    // Incomplete run: a benflow row had no matching local patient yet
                    // (the patient down-sync on the concurrent chain hasn't landed it).
                    // Retry shortly — the patient pull will have finished and the join
                    // populates the role worklists.
                    Timber.d("Benflow worklist incomplete (patient join missed); retry attempt ${runAttemptCount + 1}")
                    Result.retry()
                } else {
                    // Give up after MAX_RUN_ATTEMPTS so the chain is released. The clinical
                    // worker at the end retries the join and only then advances the watermark.
                    Timber.d("Benflow worklist still incomplete after $MAX_RUN_ATTEMPTS attempts; releasing chain")
                    Result.success()
                }
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