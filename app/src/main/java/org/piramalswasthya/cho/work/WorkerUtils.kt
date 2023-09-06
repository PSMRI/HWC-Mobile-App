package org.piramalswasthya.cho.work


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import org.piramalswasthya.sakhi.work.PullFromAmritWorker
import org.piramalswasthya.sakhi.work.PushToAmritWorker
import java.util.concurrent.TimeUnit

object WorkerUtils {

    const val syncWorkerUniqueName  = "SYNC-WITH-AMRIT"

    private val networkOnlyConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun triggerAmritSyncWorker(context : Context){
//        val pullWorkRequest = OneTimeWorkRequestBuilder<PullFromAmritWorker>()
//            .setConstraints(networkOnlyConstraint)
//            .build()
        val pushWorkRequest = OneTimeWorkRequestBuilder<PushToAmritWorker>()
//            .setInitialDelay(10, TimeUnit.SECONDS)
            .setConstraints(networkOnlyConstraint)
            .build()
        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork(syncWorkerUniqueName, ExistingWorkPolicy.APPEND_OR_REPLACE, pushWorkRequest)
            .enqueue()
    }

    fun triggerDownloadCardWorker(
        context: Context,
        fileName: String,
        otpTxnID: MutableLiveData<String?>
    ): LiveData<Operation.State> {

        val workRequest = OneTimeWorkRequestBuilder<DownloadCardWorker>()
            .setConstraints(networkOnlyConstraint)
            .setInputData(Data.Builder().apply { putString(DownloadCardWorker.file_name, fileName) }.build())
            .build()

        return WorkManager.getInstance(context)
            .enqueueUniqueWork(DownloadCardWorker.name, ExistingWorkPolicy.REPLACE, workRequest).state
    }

    fun cancelAllWork(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWork()
    }

}