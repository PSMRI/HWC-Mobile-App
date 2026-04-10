package org.piramalswasthya.cho.work


import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import org.piramalswasthya.sakhi.work.PullBenFlowFromAmritWorker
import org.piramalswasthya.sakhi.work.PushBenToAmritWorker
import timber.log.Timber
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

        val pullCbacFromAmritWorker = OneTimeWorkRequestBuilder<PullCbacFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullEligibleCouplesWorker = OneTimeWorkRequestBuilder<PullEligibleCouplesWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullPregnantWomenWorker = OneTimeWorkRequestBuilder<PullPregnantWomenWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullAncVisitsWorker = OneTimeWorkRequestBuilder<PullAncVisitsWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()


        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork(syncName, ExistingWorkPolicy.APPEND_OR_REPLACE, pullPatientFromAmritWorker)
            .then(pullBenFlowFromAmritWorker)
            .then(pullCbacFromAmritWorker)
            .then(pullEligibleCouplesWorker)
            .then(pullPregnantWomenWorker)
            .then(pullAncVisitsWorker)
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

        val pushAncToAmritWorker = OneTimeWorkRequestBuilder<PushAncToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pushPNCWorkRequest = OneTimeWorkRequestBuilder<PushPNCToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pushImmunizationWorkRequest = OneTimeWorkRequestBuilder<PushChildImmunizationToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pushInfantRegisterWorkRequest = OneTimeWorkRequestBuilder<PushInfantRegisterToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pushCbacWorkRequest = OneTimeWorkRequestBuilder<PushCbacToAmirtWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()



        val pushECToAmritWorker = OneTimeWorkRequestBuilder<PushECToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullEligibleCouplesWorker = OneTimeWorkRequestBuilder<PullEligibleCouplesWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullPregnantWomenWorker = OneTimeWorkRequestBuilder<PullPregnantWomenWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullAncVisitsWorker = OneTimeWorkRequestBuilder<PullAncVisitsWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork(syncOneTimeAmritSyncWorker, ExistingWorkPolicy.APPEND_OR_REPLACE, pullPatientFromAmritWorker)
            .then(pushBenToAmritWorker)
            .then(pushCbacWorkRequest)
            .then(createRevisitBenflowWorker)
            .then(pullBenFlowFromAmritWorker)
            .then(pushBenVisitInfoRequest)
            // The three doctor-info variants are independent — run them in parallel.
            .then(listOf(pushBenDoctorInfoPendingTestToAmrit, pushBenDoctorInfoWithoutTestToAmrit, pushBenDoctorInfoAfterTestToAmrit))
            // Specialty health pushes are also independent — run them in parallel.
            .then(listOf(pushPWRToAmritWorker, pushAncToAmritWorker, pushInfantRegisterWorkRequest, pushPNCWorkRequest, pushECToAmritWorker, pushImmunizationWorkRequest))
            // Pull eligible couple data from server after pushes complete.
            .then(pullEligibleCouplesWorker)
            .then(pullPregnantWomenWorker)
            .then(pullAncVisitsWorker)
//           .then(pushLabDataToAmrit)
            .enqueue()
    }

    /**
     * Targeted EC sync for eligible-couple tracking form submission.
     * Pushes local ECT updates first, then refreshes EC data from server.
     */
    fun triggerEligibleCoupleTrackingSync(context: Context) {
        val pushECToAmritWorker = OneTimeWorkRequestBuilder<PushECToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullEligibleCouplesWorker = OneTimeWorkRequestBuilder<PullEligibleCouplesWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork("ec-tracking-sync", ExistingWorkPolicy.APPEND_OR_REPLACE, pushECToAmritWorker)
            .then(pullEligibleCouplesWorker)
            .enqueue()
    }

    /**
     * Targeted PWR sync for pregnant woman registration form submission.
     * Pushes local PWR updates immediately after successful save.
     */
    fun triggerPregnantWomanRegistrationSync(context: Context) {
        val pushPWRToAmritWorker = OneTimeWorkRequestBuilder<PushPWRToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushAncToAmritWorker = OneTimeWorkRequestBuilder<PushAncToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullPregnantWomenWorker = OneTimeWorkRequestBuilder<PullPregnantWomenWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullAncVisitsWorker = OneTimeWorkRequestBuilder<PullAncVisitsWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val workManager = WorkManager.getInstance(context)
        Timber.d("Enqueuing targeted PWR registration sync worker")
        workManager
            .beginUniqueWork("pwr-registration-sync", ExistingWorkPolicy.REPLACE, pushPWRToAmritWorker)
            .then(pushAncToAmritWorker)
            .then(pullPregnantWomenWorker)
            .then(pullAncVisitsWorker)
            .enqueue()
    }

    /**
     * Targeted beneficiary sync to push patient reproductive status/id updates.
     * Triggers beneficiariesToServer/update beneficiariesToServer via PushBenToAmritWorker.
     */
    fun triggerBeneficiarySync(context: Context) {
        val pushBenToAmritWorker = OneTimeWorkRequestBuilder<PushBenToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val workManager = WorkManager.getInstance(context)
        Timber.d("Enqueuing targeted beneficiary sync worker")
        workManager
            .beginUniqueWork("beneficiary-sync", ExistingWorkPolicy.REPLACE, pushBenToAmritWorker)
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

    fun doctorPushWorker(context: Context) {
        val pushDoctorPendingTest = OneTimeWorkRequestBuilder<PushBenDoctorInfoPendingTestToAmrit>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushDoctorWithoutTest = OneTimeWorkRequestBuilder<PushBenDoctorInfoWithoutTestToAmrit>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushDoctorAfterTest = OneTimeWorkRequestBuilder<PushBenDoctorInfoAfterTestToAmrit>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork("doctor-data-sync", ExistingWorkPolicy.APPEND_OR_REPLACE, pushDoctorPendingTest)
            .then(pushDoctorWithoutTest)
            .then(pushDoctorAfterTest)
            .enqueue()
    }

    /**
     * Lightweight clinical push used after nurse/doctor form save.
     * Pushes local clinical updates without triggering the full down-sync chain.
     */
    fun clinicalPushWorker(context: Context) {
        val pushBenVisitInfoRequest = OneTimeWorkRequestBuilder<PushBenVisitInfoToAmrit>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushDoctorPendingTest = OneTimeWorkRequestBuilder<PushBenDoctorInfoPendingTestToAmrit>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushDoctorWithoutTest = OneTimeWorkRequestBuilder<PushBenDoctorInfoWithoutTestToAmrit>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushDoctorAfterTest = OneTimeWorkRequestBuilder<PushBenDoctorInfoAfterTestToAmrit>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork("clinical-data-sync", ExistingWorkPolicy.APPEND_OR_REPLACE, pushBenVisitInfoRequest)
            .then(pushDoctorPendingTest)
            .then(pushDoctorWithoutTest)
            .then(pushDoctorAfterTest)
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
        Log.d("WU", "pharmacistPushWorker: ")
        val pushPharmacistDataToAmrit = OneTimeWorkRequestBuilder<PushPharmacistDataToAmrit>()
            .setConstraints(networkOnlyConstraint)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR, // or BackoffPolicy.LINEAR
                10, // Minimum delay between retries
                TimeUnit.MINUTES // Time unit for the delay
            )
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
