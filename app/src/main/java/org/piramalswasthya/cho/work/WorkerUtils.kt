package org.piramalswasthya.cho.work


import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import org.piramalswasthya.sakhi.work.PullBenFlowFromAmritWorker
import org.piramalswasthya.sakhi.work.PushBenToAmritWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

object WorkerUtils {

    const val syncOneTimeAmritSyncWorker = "SYNC-WITH-AMRIT"
    const val syncPeriodicDownSyncWorker = "PERIODIC-DOWN-SYNC"
    const val syncOneTimeDownSyncWorker = "ONE-TIME-DOWN-SYNC"

    var totalRecordsToDownload = 0;
    var totalPercentageCompleted = MutableLiveData<Int>(0)

    var amritSyncInProgress = false
    var downloadSyncInProgress = false

    private val networkOnlyConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun triggerDownSyncWorker(context : Context, syncName: String){

        val pullBenFlowFromAmritWorker = OneTimeWorkRequestBuilder<PullBenFlowFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullPatientFromAmritWorker = OneTimeWorkRequestBuilder<PullPatientsFromServer>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork(syncName, ExistingWorkPolicy.APPEND_OR_REPLACE, pullPatientFromAmritWorker)
            .then(pullBenFlowFromAmritWorker)
            .enqueue()
    }

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
        val pushBenDoctorInfoPendingTestToAmrit = OneTimeWorkRequestBuilder<PushBenDoctorInfoPendingTestToAmrit>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushBenDoctorInfoWithoutTestToAmrit = OneTimeWorkRequestBuilder<PushBenDoctorInfoWithoutTestToAmrit>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushBenDoctorInfoAfterTestToAmrit = OneTimeWorkRequestBuilder<PushBenDoctorInfoAfterTestToAmrit>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullPatientFromAmritWorker = OneTimeWorkRequestBuilder<PullPatientsFromServer>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val createRevisitBenflowWorker = OneTimeWorkRequestBuilder<CreateRevisitBenflowWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pushLabDataToAmrit = OneTimeWorkRequestBuilder<PushLabDataToAmrit>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pushPWRToAmritWorker = OneTimeWorkRequestBuilder<PushPWRToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pushPNCWorkRequest = OneTimeWorkRequestBuilder<PushPNCToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pushImmunizationWorkRequest = OneTimeWorkRequestBuilder<PushChildImmunizationToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pushECToAmritWorker = OneTimeWorkRequestBuilder<PushECToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork(syncOneTimeAmritSyncWorker, ExistingWorkPolicy.APPEND_OR_REPLACE, pullPatientFromAmritWorker)
            .then(pushBenToAmritWorker)
            .then(createRevisitBenflowWorker)
            .then(pullBenFlowFromAmritWorker)
            .then(pushBenVisitInfoRequest)
            .then(pushBenDoctorInfoPendingTestToAmrit)
            .then(pushBenDoctorInfoWithoutTestToAmrit)
            .then(pushBenDoctorInfoAfterTestToAmrit)
            .then(pushPWRToAmritWorker)
            .then(pushPNCWorkRequest)
            .then(pushECToAmritWorker)
            .then(pushImmunizationWorkRequest)
//            .then(pushLabDataToAmrit)
            .enqueue()
    }

    fun labPushWorker(context : Context){

        val pushLabDataToAmrit = OneTimeWorkRequestBuilder<PushLabDataToAmrit>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork("lab-sync", ExistingWorkPolicy.APPEND_OR_REPLACE, pushLabDataToAmrit)
            .enqueue()
    }

    fun pushAuditDetailsWorker(context : Context){
        val pushLoginAuditDataToAmrit = OneTimeWorkRequestBuilder<PushLoginAuditDataWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork("audit-data-sync", ExistingWorkPolicy.APPEND, pushLoginAuditDataToAmrit)
            .enqueue()
    }

    fun pharmacistPushWorker(context : Context){

        val pushPharmacistDataToAmrit = OneTimeWorkRequestBuilder<PushPharmacistDataToAmrit>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork("pharmacist-sync", ExistingWorkPolicy.APPEND_OR_REPLACE, pushPharmacistDataToAmrit)
            .enqueue()
    }

    fun labPullWorker(context : Context, patientId: String){

        val data = Data.Builder()
        data.putString("patientId", patientId)
        val pullLabDataToAmrit = OneTimeWorkRequestBuilder<PullLabDataToAmrit>()
            .setConstraints(networkOnlyConstraint)
            .setInputData(data.build())
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork("lab-sync-pull", ExistingWorkPolicy.APPEND_OR_REPLACE, pullLabDataToAmrit)
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

    fun presTemplate(
        context: Context,
    ) {

        val workRequest = OneTimeWorkRequestBuilder<PrescripTemplateWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(PrescripTemplateWorker.name, ExistingWorkPolicy.KEEP, workRequest).state
    }

    fun cancelAllWork(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWork()
    }


}