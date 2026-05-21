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

    private data class RmnchPullWorkers(
        val eligibleCouples: OneTimeWorkRequest,
        val pregnantWomen: OneTimeWorkRequest,
        val ancVisits: OneTimeWorkRequest,
        val deliveryOutcome: OneTimeWorkRequest,
        val pnc: OneTimeWorkRequest,
        val infantRegister: OneTimeWorkRequest
    )

    private inline fun <reified T : ListenableWorker> networkWorker(): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<T>()
            .setConstraints(networkOnlyConstraint)
            .build()
    }

    private fun createRmnchPullWorkers(): RmnchPullWorkers {
        return RmnchPullWorkers(
            eligibleCouples = networkWorker<PullEligibleCouplesWorker>(),
            pregnantWomen = networkWorker<PullPregnantWomenWorker>(),
            ancVisits = networkWorker<PullAncVisitsWorker>(),
            deliveryOutcome = networkWorker<PullDeliveryOutcomeWorker>(),
            pnc = networkWorker<PullPncFromAmritWorker>(),
            infantRegister = networkWorker<PullInfantRegisterWorker>()
        )
    }

    private fun enqueueReplaceChain(
        context: Context,
        workName: String,
        first: OneTimeWorkRequest,
        vararg next: OneTimeWorkRequest
    ) {
        val workManager = WorkManager.getInstance(context)
        var chain = workManager.beginUniqueWork(workName, ExistingWorkPolicy.REPLACE, first)
        next.forEach { request ->
            chain = chain.then(request)
        }
        chain.enqueue()
    }

    fun triggerDownSyncWorker(context : Context, syncName: String){

        val pullBenFlowFromAmritWorker = networkWorker<PullBenFlowFromAmritWorker>()
        val pullPatientFromAmritWorker = networkWorker<PullPatientsFromServer>()
        val pullFormAmritWorker = networkWorker<PullLabRecordFormWorker>()
        val pullCbacFromAmritWorker = networkWorker<PullCbacFromAmritWorker>()
        val rmnchPull = createRmnchPullWorkers()
        val pullEarFromAmritWorker = OneTimeWorkRequestBuilder<PullEarFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullOphthalmicFromAmritWorker = OneTimeWorkRequestBuilder<PullOphthalmicFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullOralFromAmritWorker = OneTimeWorkRequestBuilder<PullOralFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullPainAssessmentFromAmritWorker = OneTimeWorkRequestBuilder<PullPainAssessmentFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullPsychosocialCaregiverSupport = OneTimeWorkRequestBuilder<PullPsychosocialCaregiverSupportWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullNoseFromAmritWorker = OneTimeWorkRequestBuilder<PullNoseFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullThroatFromAmritWorker = OneTimeWorkRequestBuilder<PullThroatFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullElderlyFromAmritWorker = OneTimeWorkRequestBuilder<PullElderlyFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullMentalFromAmritWorker = OneTimeWorkRequestBuilder<PullMentalFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork(syncName, ExistingWorkPolicy.APPEND_OR_REPLACE, pullPatientFromAmritWorker)
            .then(pullFormAmritWorker)
            .then(pullBenFlowFromAmritWorker)
            .then(pullCbacFromAmritWorker)
            .then(rmnchPull.eligibleCouples)
            .then(rmnchPull.pregnantWomen)
            .then(rmnchPull.ancVisits)
            .then(rmnchPull.deliveryOutcome)
            .then(rmnchPull.pnc)
            .then(rmnchPull.infantRegister)
            .then(listOf(
                pullEarFromAmritWorker,pullOphthalmicFromAmritWorker, pullOralFromAmritWorker, pullPainAssessmentFromAmritWorker, pullPsychosocialCaregiverSupport, pullNoseFromAmritWorker, pullThroatFromAmritWorker,  pullElderlyFromAmritWorker,
                pullMentalFromAmritWorker
            ))
            .enqueue()
    }

    fun triggerAmritSyncWorker(context : Context){

        val pullBenFlowFromAmritWorker = networkWorker<PullBenFlowFromAmritWorker>()
        val pushBenToAmritWorker = networkWorker<PushBenToAmritWorker>()
        val pushBenVisitInfoRequest = networkWorker<PushBenVisitInfoToAmrit>()
        val pushBenDoctorInfoPendingTestToAmrit = networkWorker<PushBenDoctorInfoPendingTestToAmrit>()
        val pushBenDoctorInfoWithoutTestToAmrit = networkWorker<PushBenDoctorInfoWithoutTestToAmrit>()
        val pushBenDoctorInfoAfterTestToAmrit = networkWorker<PushBenDoctorInfoAfterTestToAmrit>()
        val pullPatientFromAmritWorker = networkWorker<PullPatientsFromServer>()
        val pullFormAmritWorker = networkWorker<PullLabRecordFormWorker>()
        val createRevisitBenflowWorker = networkWorker<CreateRevisitBenflowWorker>()
        val pushLabDataToAmrit = networkWorker<PushLabDataToAmrit>()
        val pushPWRToAmritWorker = networkWorker<PushPWRToAmritWorker>()
        val pushAncToAmritWorker = networkWorker<PushAncToAmritWorker>()
        val pushDeliveryOutcomeToAmritWorker = networkWorker<PushDeliveryOutcomeToAmritWorker>()
        val pushPNCWorkRequest = networkWorker<PushPNCToAmritWorker>()
        val pushImmunizationWorkRequest = networkWorker<PushChildImmunizationToAmritWorker>()
        val pushInfantRegisterWorkRequest = networkWorker<PushInfantRegisterToAmritWorker>()
        val pushCbacWorkRequest = networkWorker<PushCbacToAmirtWorker>()
        val pushECToAmritWorker = networkWorker<PushECToAmritWorker>()
        val rmnchPull = createRmnchPullWorkers()
        val pushEarToAmritWorker = OneTimeWorkRequestBuilder<PushEarToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullEarFromAmritWorker = OneTimeWorkRequestBuilder<PullEarFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pushOphthalmicToAmritWorker = OneTimeWorkRequestBuilder<PushOphthalmicToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullOphthalmicFromAmritWorker = OneTimeWorkRequestBuilder<PullOphthalmicFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pushOralToAmritWorker = OneTimeWorkRequestBuilder<PushOralToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullOralFromAmritWorker = OneTimeWorkRequestBuilder<PullOralFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pushPainAssessmentToAmritWorker = OneTimeWorkRequestBuilder<PushPainAssessmentToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullPainAssessmentFromAmritWorker = OneTimeWorkRequestBuilder<PullPainAssessmentFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pushPsychosocialCaregiverSupportWorker = OneTimeWorkRequestBuilder<PushPsychosocialCaregiverSupportWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullPsychosocialCaregiverSupport = OneTimeWorkRequestBuilder<PullPsychosocialCaregiverSupportWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pushNoseToAmritWorker = OneTimeWorkRequestBuilder<PushNoseToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullNoseFromAmritWorker = OneTimeWorkRequestBuilder<PullNoseFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pushThroatToAmritWorker = OneTimeWorkRequestBuilder<PushThroatToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pushElderlyToAmritWorker = OneTimeWorkRequestBuilder<PushElderlyToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushMentalToAmritWorker = OneTimeWorkRequestBuilder<PushMentalToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullThroatFromAmritWorker = OneTimeWorkRequestBuilder<PullThroatFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullElderlyFromAmritWorker = OneTimeWorkRequestBuilder<PullElderlyFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullMentalFromAmritWorker = OneTimeWorkRequestBuilder<PullMentalFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork(syncOneTimeAmritSyncWorker, ExistingWorkPolicy.APPEND_OR_REPLACE, pullPatientFromAmritWorker)
            .then(pullFormAmritWorker)
            .then(pushBenToAmritWorker)
            .then(pushCbacWorkRequest)
            .then(createRevisitBenflowWorker)
            .then(pullBenFlowFromAmritWorker)
            .then(pushBenVisitInfoRequest)
            // The three doctor-info variants are independent — run them in parallel.
            .then(listOf(pushBenDoctorInfoPendingTestToAmrit, pushBenDoctorInfoWithoutTestToAmrit, pushBenDoctorInfoAfterTestToAmrit))
            // Specialty health pushes are also independent — run them in parallel.
            .then(listOf(pushPWRToAmritWorker, pushAncToAmritWorker, pushDeliveryOutcomeToAmritWorker, pushInfantRegisterWorkRequest, pushPNCWorkRequest, pushECToAmritWorker, pushImmunizationWorkRequest, ))
            // Pull eligible couple data from server after pushes complete.
            .then(rmnchPull.eligibleCouples)
            .then(rmnchPull.pregnantWomen)
            .then(rmnchPull.ancVisits)
            .then(rmnchPull.deliveryOutcome)
            .then(rmnchPull.pnc)
            .then(rmnchPull.infantRegister)
            .then(listOf(
                pushEarToAmritWorker, pushOphthalmicToAmritWorker , pushOralToAmritWorker, pushPainAssessmentToAmritWorker, pushPsychosocialCaregiverSupportWorker, pushNoseToAmritWorker, pushThroatToAmritWorker, pushElderlyToAmritWorker, pushMentalToAmritWorker
            ))
            .then(listOf(
                pullEarFromAmritWorker, pullOphthalmicFromAmritWorker , pullOralFromAmritWorker, pullPainAssessmentFromAmritWorker, pullPsychosocialCaregiverSupport, pullNoseFromAmritWorker, pullThroatFromAmritWorker, pullElderlyFromAmritWorker,
                pullMentalFromAmritWorker
            ))
//           .then(pushLabDataToAmrit)
            .enqueue()
    }

    /**
     * Targeted EC sync for eligible-couple tracking form submission.
     * Pushes local ECT updates first, then refreshes EC data from server.
     */
    fun triggerEligibleCoupleTrackingSync(context: Context) {
        val pushECToAmritWorker = networkWorker<PushECToAmritWorker>()
        val pullEligibleCouplesWorker = networkWorker<PullEligibleCouplesWorker>()

        enqueueReplaceChain(
            context = context,
            workName = "ec-tracking-sync",
            first = pushECToAmritWorker,
            pullEligibleCouplesWorker
        )
    }

    /**
     * Targeted PWR sync for pregnant woman registration form submission.
     * Pushes the beneficiary first so the server assigns a beneficiaryID; PWR push
     * silently skips records where patient.beneficiaryID is null.
     */
    fun triggerPregnantWomanRegistrationSync(context: Context) {
        val pushBenToAmritWorker = networkWorker<PushBenToAmritWorker>()
        val pullBenFlowFromAmritWorker = networkWorker<PullBenFlowFromAmritWorker>()
        val pushPWRToAmritWorker = networkWorker<PushPWRToAmritWorker>()
        val pushAncToAmritWorker = networkWorker<PushAncToAmritWorker>()
        val pullPregnantWomenWorker = networkWorker<PullPregnantWomenWorker>()
        val pullAncVisitsWorker = networkWorker<PullAncVisitsWorker>()

        Timber.d("Enqueuing targeted PWR registration sync worker")
        enqueueReplaceChain(
            context = context,
            workName = "pwr-registration-sync",
            first = pushBenToAmritWorker,
            pullBenFlowFromAmritWorker,
            pushPWRToAmritWorker,
            pushAncToAmritWorker,
            pullPregnantWomenWorker,
            pullAncVisitsWorker
        )
    }

    /**
     * Targeted ANC sync after ANC form submission.
     * Pushes beneficiary first so beneficiaryID is populated. Also pushes/pulls PWR
     * because PwAncFormViewModel.saveForm() mutates the pregnancy_register row
     * (isHrp, isFirstAncSubmitted, active) when the ANC is high-risk, first-completed,
     * or ends the pregnancy — leaving PWR UNSYNCED until the next sync.
     */
    fun triggerAncVisitSync(context: Context) {
        val pushBenToAmritWorker = networkWorker<PushBenToAmritWorker>()
        val pullBenFlowFromAmritWorker = networkWorker<PullBenFlowFromAmritWorker>()
        val pushPWRToAmritWorker = networkWorker<PushPWRToAmritWorker>()
        val pushAncToAmritWorker = networkWorker<PushAncToAmritWorker>()
        val pullPregnantWomenWorker = networkWorker<PullPregnantWomenWorker>()
        val pullAncVisitsWorker = networkWorker<PullAncVisitsWorker>()

        Timber.d("Enqueuing targeted ANC sync worker")
        enqueueReplaceChain(
            context = context,
            workName = "anc-visit-sync",
            first = pushBenToAmritWorker,
            pullBenFlowFromAmritWorker,
            pushPWRToAmritWorker,
            pushAncToAmritWorker,
            pullPregnantWomenWorker,
            pullAncVisitsWorker
        )
    }

    /**
     * Targeted Delivery Outcome sync after form submission.
     * Pushes local Delivery Outcome record and refreshes server Delivery Outcome list.
     */
    fun triggerDeliveryOutcomeSync(context: Context) {
        val pushDeliveryOutcomeToAmritWorker = networkWorker<PushDeliveryOutcomeToAmritWorker>()
        val pullDeliveryOutcomeWorker = networkWorker<PullDeliveryOutcomeWorker>()

        Timber.d("Enqueuing targeted Delivery Outcome sync worker")
        enqueueReplaceChain(
            context = context,
            workName = "delivery-outcome-sync",
            first = pushDeliveryOutcomeToAmritWorker,
            pullDeliveryOutcomeWorker
        )
    }

    /**
     * Targeted Infant registration sync after infant form submission.
     * Pushes local infant registration and refreshes infant list from server.
     */
    fun triggerInfantRegistrationSync(context: Context) {
        val pushInfantRegisterWorkRequest = networkWorker<PushInfantRegisterToAmritWorker>()
        val pullInfantRegisterWorker = networkWorker<PullInfantRegisterWorker>()

        Timber.d("Enqueuing targeted Infant registration sync worker")
        enqueueReplaceChain(
            context = context,
            workName = "infant-registration-sync",
            first = pushInfantRegisterWorkRequest,
            pullInfantRegisterWorker
        )
    }

    fun triggerPncSync(context: Context) {
        val pushPncWorker = networkWorker<PushPNCToAmritWorker>()
        val pullPncWorker = networkWorker<PullPncFromAmritWorker>()

        Timber.d("Enqueuing targeted PNC sync worker")
        enqueueReplaceChain(
            context = context,
            workName = "pnc-sync",
            first = pushPncWorker,
            pullPncWorker
        )
    }

    /**
     * Targeted beneficiary sync to push patient reproductive status/id updates.
     * Triggers beneficiariesToServer/update beneficiariesToServer via PushBenToAmritWorker.
     */
    fun triggerBeneficiarySync(context: Context) {
        val pushBenToAmritWorker = networkWorker<PushBenToAmritWorker>()

        Timber.d("Enqueuing targeted beneficiary sync worker")
        enqueueReplaceChain(
            context = context,
            workName = "beneficiary-sync",
            first = pushBenToAmritWorker
        )
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
    fun earPushWorker(context: Context) {
        val pushEarToAmrit = OneTimeWorkRequestBuilder<PushEarToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        WorkManager.getInstance(context)
            .beginUniqueWork("ear-push-sync", ExistingWorkPolicy.APPEND_OR_REPLACE, pushEarToAmrit)
            .enqueue()
    }

    fun ophthalmicPushWorker(context: Context) {
        val pushOphthalmicToAmrit = OneTimeWorkRequestBuilder<PushOphthalmicToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        WorkManager.getInstance(context)
            .beginUniqueWork("ophthalmic-push-sync", ExistingWorkPolicy.APPEND_OR_REPLACE, pushOphthalmicToAmrit)
            .enqueue()
    }

    fun oralPushWorker(context: Context) {
        val pushOralToAmrit = OneTimeWorkRequestBuilder<PushOralToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        WorkManager.getInstance(context)
            .beginUniqueWork("oral-push-sync", ExistingWorkPolicy.APPEND_OR_REPLACE, pushOralToAmrit)
            .enqueue()
    }
    fun painAssessmentPushWorker(context: Context) {
        val pushPainAssessmentToAmrit = OneTimeWorkRequestBuilder<PushPainAssessmentToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        WorkManager.getInstance(context)
            .beginUniqueWork("pain-assessment-push-sync", ExistingWorkPolicy.APPEND_OR_REPLACE, pushPainAssessmentToAmrit)
            .enqueue()
    }

    fun psychosocialCaregiverSupport(context: Context) {
        val pushPsychosocialCaregiverSupportWorker = OneTimeWorkRequestBuilder<PushPsychosocialCaregiverSupportWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        WorkManager.getInstance(context)
            .beginUniqueWork("psychosocial-push-sync", ExistingWorkPolicy.APPEND_OR_REPLACE, pushPsychosocialCaregiverSupportWorker)
            .enqueue()
    }

    fun nosePushWorker(context: Context) {
        val pushNoseToAmrit = OneTimeWorkRequestBuilder<PushNoseToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        WorkManager.getInstance(context)
            .beginUniqueWork("nose-push-sync", ExistingWorkPolicy.APPEND_OR_REPLACE, pushNoseToAmrit)
            .enqueue()
    }

    fun throatPushWorker(context: Context) {
        val pushThroatToAmrit = OneTimeWorkRequestBuilder<PushThroatToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        WorkManager.getInstance(context)
            .beginUniqueWork("throat-push-sync", ExistingWorkPolicy.APPEND_OR_REPLACE, pushThroatToAmrit)
            .enqueue()
    }

    fun elderlyPushWorker(context: Context) {
        val pushElderlyToAmrit = OneTimeWorkRequestBuilder<PushElderlyToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        WorkManager.getInstance(context)
            .beginUniqueWork("elderly-push-sync", ExistingWorkPolicy.APPEND_OR_REPLACE, pushElderlyToAmrit)
            .enqueue()
    }

    fun mentalPushWorker(context: Context) {
        val pushMentalToAmrit = OneTimeWorkRequestBuilder<PushMentalToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        WorkManager.getInstance(context)
            .beginUniqueWork("mental-push-sync", ExistingWorkPolicy.APPEND_OR_REPLACE, pushMentalToAmrit)
            .enqueue()
    }


    fun cancelAllWork(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWork()
    }


}
