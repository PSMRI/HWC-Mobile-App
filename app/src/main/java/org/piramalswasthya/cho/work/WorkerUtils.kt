package org.piramalswasthya.cho.work


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import org.piramalswasthya.sakhi.work.PullBenFlowFromAmritWorker
import org.piramalswasthya.sakhi.work.PushBenToAmritWorker
import java.util.concurrent.TimeUnit

object WorkerUtils {

    const val syncWorkerUniqueName  = "SYNC-WITH-AMRIT"

    private val networkOnlyConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun triggerAmritSyncWorker(context : Context){
        val pullBenFlowFromAmritWorker = OneTimeWorkRequestBuilder<PullBenFlowFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushBenToAmritWorker = OneTimeWorkRequestBuilder<PushBenToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushBenVisitInfoRequest = OneTimeWorkRequestBuilder<PushBenVisitInfoToAmrit>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushBenDoctorInfoToAmrit = OneTimeWorkRequestBuilder<PushBenDoctorInfoToAmrit>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullPatientFromAmritWorker = OneTimeWorkRequestBuilder<PullPatientsFromServer>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val createRevisitBenflowWorker = OneTimeWorkRequestBuilder<CreateRevisitBenflowWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork(syncWorkerUniqueName, ExistingWorkPolicy.APPEND_OR_REPLACE, pullPatientFromAmritWorker)
            .then(pushBenToAmritWorker)
            .then(createRevisitBenflowWorker)
            .then(pullBenFlowFromAmritWorker)
            .then(pushBenVisitInfoRequest)
            .then(pushBenDoctorInfoToAmrit)
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