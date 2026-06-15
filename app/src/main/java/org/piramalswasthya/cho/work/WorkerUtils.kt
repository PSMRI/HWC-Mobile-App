package org.piramalswasthya.cho.work

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.WorkContinuation
import androidx.work.WorkManager
import org.piramalswasthya.sakhi.work.PullBenFlowFromAmritWorker
import org.piramalswasthya.sakhi.work.PushBenToAmritWorker
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.TimeUnit

object WorkerUtils {

    const val syncOneTimeAmritSyncWorker = "upsync-full"
    const val syncPeriodicDownSyncWorker = "downsync-periodic-15m"
    const val syncOneTimeDownSyncWorker = "downsync-manual"

    private const val UPSYNC_CLINICAL = "upsync-clinical"
    private const val UPSYNC_RMNCHA = "upsync-rmncha"
    private const val UPSYNC_CPHC = "upsync-cphc"
    private const val UPSYNC_LAB = "upsync-lab"
    private const val UPSYNC_PHARMACIST = "upsync-pharmacist"
    private const val UPSYNC_AUDIT = "upsync-audit"
    private const val UPSYNC_BENEFICIARY = "upsync-beneficiary"

    var totalRecordsToDownload = 0
    var totalPercentageCompleted = MutableLiveData<Int>(0)

    var amritSyncInProgress = false
    var downloadSyncInProgress = false

    enum class DownsyncScope {
        FULL
    }

    enum class UpsyncScope {
        FULL,
        BENEFICIARY,
        CLINICAL,
        LAB,
        PHARMACIST,
        EC_TRACKING,
        PREGNANCY_REGISTRATION,
        ANC_VISIT,
        DELIVERY_OUTCOME,
        INFANT_REGISTRATION,
        IMMUNIZATION,
        PNC,
        CBAC,
        EAR,
        OPHTHALMIC,
        ORAL,
        PAIN_ASSESSMENT,
        PSYCHOSOCIAL,
        NOSE,
        THROAT,
        ELDERLY,
        MENTAL
    }

    enum class SyncReason {
        APP_INIT,
        MANUAL,
        PERIODIC,
        FORM_SAVE
    }

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

    private data class DownsyncWorkers(
        val patients: OneTimeWorkRequest,
        val benflow: OneTimeWorkRequest,
        val parallelPulls: List<OneTimeWorkRequest>
    )

    private inline fun <reified T : ListenableWorker> networkWorker(): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<T>()
            .setConstraints(networkOnlyConstraint)
            .build()
    }

    // Benflow workers retry quickly when the patient table has not caught up yet.
    private inline fun <reified T : ListenableWorker> benflowWorker(): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<T>()
            .setConstraints(networkOnlyConstraint)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
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

    private fun createFullDownsyncWorkers(): DownsyncWorkers {
        val rmnchPull = createRmnchPullWorkers()
        return DownsyncWorkers(
            patients = networkWorker<PullPatientsFromServer>(),
            benflow = benflowWorker<PullBenFlowFromAmritWorker>(),
            parallelPulls = listOf(
                benflowWorker<PullClinicalRecordsWorker>(),
                networkWorker<PullLabRecordFormWorker>(),
                networkWorker<PullCbacFromAmritWorker>(),
                rmnchPull.eligibleCouples,
                rmnchPull.pregnantWomen,
                rmnchPull.ancVisits,
                rmnchPull.deliveryOutcome,
                rmnchPull.pnc,
                rmnchPull.infantRegister,
                networkWorker<PullEarFromAmritWorker>(),
                networkWorker<PullOphthalmicFromAmritWorker>(),
                networkWorker<PullOralFromAmritWorker>(),
                networkWorker<PullPainAssessmentFromAmritWorker>(),
                networkWorker<PullPsychosocialCaregiverSupportWorker>(),
                networkWorker<PullNoseFromAmritWorker>(),
                networkWorker<PullThroatFromAmritWorker>(),
                networkWorker<PullElderlyFromAmritWorker>(),
                networkWorker<PullMentalFromAmritWorker>()
            )
        )
    }

    private fun enqueueChain(
        context: Context,
        workName: String,
        policy: ExistingWorkPolicy,
        first: OneTimeWorkRequest,
        next: List<OneTimeWorkRequest> = emptyList()
    ) {
        val workManager = WorkManager.getInstance(context)
        var chain = workManager.beginUniqueWork(workName, policy, first)
        next.forEach { request ->
            chain = chain.then(request)
        }
        chain.enqueue()
    }

    private fun rmnchaWorkName(scope: UpsyncScope): String {
        return "$UPSYNC_RMNCHA-${scope.name.lowercase(Locale.US)}"
    }

    fun enqueueDownsync(
        context: Context,
        scope: DownsyncScope = DownsyncScope.FULL,
        reason: SyncReason = SyncReason.MANUAL
    ) {
        if (scope != DownsyncScope.FULL) return

        val policy = when (reason) {
            SyncReason.PERIODIC -> ExistingWorkPolicy.KEEP
            else -> ExistingWorkPolicy.REPLACE
        }
        val workName = when (reason) {
            SyncReason.PERIODIC -> syncPeriodicDownSyncWorker
            else -> syncOneTimeDownSyncWorker
        }
        val workers = createFullDownsyncWorkers()
        WorkManager.getInstance(context)
            .beginUniqueWork(workName, policy, workers.patients)
            .then(workers.benflow)
            .then(workers.parallelPulls)
            .enqueue()
    }

    fun enqueuePeriodicDownsync(context: Context) {
        enqueueDownsync(context, DownsyncScope.FULL, SyncReason.PERIODIC)
    }

    fun enqueueFullSync(context: Context, reason: SyncReason = SyncReason.APP_INIT) {
        val workManager = WorkManager.getInstance(context)
        var chain = workManager
            .beginUniqueWork(
                syncOneTimeAmritSyncWorker,
                ExistingWorkPolicy.REPLACE,
                networkWorker<PushBenToAmritWorker>()
            )
            .then(networkWorker<CreateRevisitBenflowWorker>())
            .then(
                listOf(
                    networkWorker<PushBenVisitInfoToAmrit>(),
                    networkWorker<PushBenDoctorInfoPendingTestToAmrit>(),
                    networkWorker<PushBenDoctorInfoWithoutTestToAmrit>(),
                    networkWorker<PushBenDoctorInfoAfterTestToAmrit>(),
                    networkWorker<PushCbacToAmirtWorker>(),
                    networkWorker<PushPWRToAmritWorker>(),
                    networkWorker<PushAncToAmritWorker>(),
                    networkWorker<PushDeliveryOutcomeToAmritWorker>(),
                    networkWorker<PushInfantRegisterToAmritWorker>(),
                    networkWorker<PushPNCToAmritWorker>(),
                    networkWorker<PushECToAmritWorker>(),
                    networkWorker<PushChildImmunizationToAmritWorker>(),
                    networkWorker<PushLabDataToAmrit>(),
                    networkWorker<PushPharmacistDataToAmrit>(),
                    networkWorker<PushEarToAmritWorker>(),
                    networkWorker<PushOphthalmicToAmritWorker>(),
                    networkWorker<PushOralToAmritWorker>(),
                    networkWorker<PushPainAssessmentToAmritWorker>(),
                    networkWorker<PushPsychosocialCaregiverSupportWorker>(),
                    networkWorker<PushNoseToAmritWorker>(),
                    networkWorker<PushThroatToAmritWorker>(),
                    networkWorker<PushElderlyToAmritWorker>(),
                    networkWorker<PushMentalToAmritWorker>()
                )
            )

        if (reason == SyncReason.APP_INIT || reason == SyncReason.MANUAL) {
            chain = appendFullDownsync(chain)
        }

        chain.enqueue()
    }

    private fun appendFullDownsync(chain: WorkContinuation): WorkContinuation {
        val downsync = createFullDownsyncWorkers()
        return chain
            .then(downsync.patients)
            .then(downsync.benflow)
            .then(downsync.parallelPulls)
    }

    fun enqueueUpsync(
        context: Context,
        scope: UpsyncScope,
        reason: SyncReason = SyncReason.FORM_SAVE
    ) {
        when (scope) {
            UpsyncScope.FULL -> enqueueFullSync(context, reason)
            UpsyncScope.BENEFICIARY -> enqueueChain(
                context,
                UPSYNC_BENEFICIARY,
                ExistingWorkPolicy.REPLACE,
                networkWorker<PushBenToAmritWorker>()
            )
            UpsyncScope.CLINICAL -> enqueueChain(
                context,
                UPSYNC_CLINICAL,
                ExistingWorkPolicy.REPLACE,
                networkWorker<PushBenToAmritWorker>(),
                listOf(
                    networkWorker<CreateRevisitBenflowWorker>(),
                    networkWorker<PushBenVisitInfoToAmrit>(),
                    networkWorker<PushBenDoctorInfoPendingTestToAmrit>(),
                    networkWorker<PushBenDoctorInfoWithoutTestToAmrit>(),
                    networkWorker<PushBenDoctorInfoAfterTestToAmrit>()
                )
            )
            UpsyncScope.LAB -> enqueueChain(
                context,
                UPSYNC_LAB,
                ExistingWorkPolicy.REPLACE,
                networkWorker<PushBenToAmritWorker>(),
                listOf(networkWorker<PushLabDataToAmrit>())
            )
            UpsyncScope.PHARMACIST -> enqueueChain(
                context,
                UPSYNC_PHARMACIST,
                ExistingWorkPolicy.REPLACE,
                networkWorker<PushBenToAmritWorker>(),
                listOf(networkWorker<PushPharmacistDataToAmrit>())
            )
            UpsyncScope.EC_TRACKING -> enqueueChain(
                context,
                rmnchaWorkName(scope),
                ExistingWorkPolicy.REPLACE,
                networkWorker<PushBenToAmritWorker>(),
                listOf(networkWorker<PushECToAmritWorker>(), networkWorker<PullEligibleCouplesWorker>())
            )
            UpsyncScope.PREGNANCY_REGISTRATION,
            UpsyncScope.ANC_VISIT -> enqueueChain(
                context,
                rmnchaWorkName(scope),
                ExistingWorkPolicy.REPLACE,
                networkWorker<PushBenToAmritWorker>(),
                listOf(
                    networkWorker<PushPWRToAmritWorker>(),
                    networkWorker<PushAncToAmritWorker>(),
                    networkWorker<PullPregnantWomenWorker>(),
                    networkWorker<PullAncVisitsWorker>()
                )
            )
            UpsyncScope.DELIVERY_OUTCOME -> enqueueChain(
                context,
                rmnchaWorkName(scope),
                ExistingWorkPolicy.REPLACE,
                networkWorker<PushBenToAmritWorker>(),
                listOf(
                    networkWorker<PushDeliveryOutcomeToAmritWorker>(),
                    networkWorker<PullDeliveryOutcomeWorker>()
                )
            )
            UpsyncScope.INFANT_REGISTRATION -> enqueueChain(
                context,
                rmnchaWorkName(scope),
                ExistingWorkPolicy.REPLACE,
                networkWorker<PushBenToAmritWorker>(),
                listOf(
                    networkWorker<PushInfantRegisterToAmritWorker>(),
                    networkWorker<PullInfantRegisterWorker>()
                )
            )
            UpsyncScope.IMMUNIZATION -> enqueueChain(
                context,
                rmnchaWorkName(scope),
                ExistingWorkPolicy.REPLACE,
                networkWorker<PushBenToAmritWorker>(),
                listOf(networkWorker<PushChildImmunizationToAmritWorker>())
            )
            UpsyncScope.PNC -> enqueueChain(
                context,
                rmnchaWorkName(scope),
                ExistingWorkPolicy.REPLACE,
                networkWorker<PushBenToAmritWorker>(),
                listOf(networkWorker<PushPNCToAmritWorker>(), networkWorker<PullPncFromAmritWorker>())
            )
            UpsyncScope.CBAC -> enqueueChain(
                context,
                "upsync-cbac",
                ExistingWorkPolicy.REPLACE,
                networkWorker<PushBenToAmritWorker>(),
                listOf(networkWorker<PushCbacToAmirtWorker>())
            )
            UpsyncScope.EAR -> enqueueCphcPush(context, "ear-push-sync", networkWorker<PushEarToAmritWorker>())
            UpsyncScope.OPHTHALMIC -> enqueueCphcPush(context, "ophthalmic-push-sync", networkWorker<PushOphthalmicToAmritWorker>())
            UpsyncScope.ORAL -> enqueueCphcPush(context, "oral-push-sync", networkWorker<PushOralToAmritWorker>())
            UpsyncScope.PAIN_ASSESSMENT -> enqueueCphcPush(context, "pain-assessment-push-sync", networkWorker<PushPainAssessmentToAmritWorker>())
            UpsyncScope.PSYCHOSOCIAL -> enqueueCphcPush(context, "psychosocial-push-sync", networkWorker<PushPsychosocialCaregiverSupportWorker>())
            UpsyncScope.NOSE -> enqueueCphcPush(context, "nose-push-sync", networkWorker<PushNoseToAmritWorker>())
            UpsyncScope.THROAT -> enqueueCphcPush(context, "throat-push-sync", networkWorker<PushThroatToAmritWorker>())
            UpsyncScope.ELDERLY -> enqueueCphcPush(context, "elderly-push-sync", networkWorker<PushElderlyToAmritWorker>())
            UpsyncScope.MENTAL -> enqueueCphcPush(context, "mental-push-sync", networkWorker<PushMentalToAmritWorker>())
        }
    }

    private fun enqueueCphcPush(context: Context, workName: String, modulePush: OneTimeWorkRequest) {
        enqueueChain(
            context,
            "$UPSYNC_CPHC-$workName",
            ExistingWorkPolicy.REPLACE,
            networkWorker<PushBenToAmritWorker>(),
            listOf(modulePush)
        )
    }

    fun triggerDownSyncWorker(context: Context, syncName: String) {
        val reason = if (syncName == syncPeriodicDownSyncWorker) SyncReason.PERIODIC else SyncReason.MANUAL
        enqueueDownsync(context, DownsyncScope.FULL, reason)
    }

    fun triggerAmritSyncWorker(context: Context) {
        enqueueFullSync(context, SyncReason.APP_INIT)
    }

    fun triggerEligibleCoupleTrackingSync(context: Context) {
        Timber.d("Enqueuing targeted EC sync worker")
        enqueueUpsync(context, UpsyncScope.EC_TRACKING)
    }

    fun triggerPregnantWomanRegistrationSync(context: Context) {
        Timber.d("Enqueuing targeted PWR registration sync worker")
        enqueueUpsync(context, UpsyncScope.PREGNANCY_REGISTRATION)
    }

    fun triggerAncVisitSync(context: Context) {
        Timber.d("Enqueuing targeted ANC sync worker")
        enqueueUpsync(context, UpsyncScope.ANC_VISIT)
    }

    fun triggerDeliveryOutcomeSync(context: Context) {
        Timber.d("Enqueuing targeted Delivery Outcome sync worker")
        enqueueUpsync(context, UpsyncScope.DELIVERY_OUTCOME)
    }

    fun triggerInfantRegistrationSync(context: Context) {
        Timber.d("Enqueuing targeted Infant registration sync worker")
        enqueueUpsync(context, UpsyncScope.INFANT_REGISTRATION)
    }

    fun triggerPncSync(context: Context) {
        Timber.d("Enqueuing targeted PNC sync worker")
        enqueueUpsync(context, UpsyncScope.PNC)
    }

    fun triggerBeneficiarySync(context: Context) {
        Timber.d("Enqueuing targeted beneficiary sync worker")
        enqueueUpsync(context, UpsyncScope.BENEFICIARY)
    }

    fun labPushWorker(context: Context) {
        enqueueUpsync(context, UpsyncScope.LAB)
    }

    fun doctorPushWorker(context: Context) {
        enqueueUpsync(context, UpsyncScope.CLINICAL)
    }

    fun clinicalPushWorker(context: Context) {
        enqueueUpsync(context, UpsyncScope.CLINICAL)
    }

    fun pushAuditDetailsWorker(context: Context) {
        enqueueChain(
            context,
            UPSYNC_AUDIT,
            ExistingWorkPolicy.KEEP,
            networkWorker<PushLoginAuditDataWorker>()
        )
    }

    fun pharmacistPushWorker(context: Context) {
        Log.d("WU", "pharmacistPushWorker")
        enqueueUpsync(context, UpsyncScope.PHARMACIST)
    }

    fun labPullWorker(context: Context, patientId: String) {
        val data = Data.Builder().putString("patientId", patientId).build()
        val pullLabDataToAmrit = OneTimeWorkRequestBuilder<PullLabDataToAmrit>()
            .setConstraints(networkOnlyConstraint)
            .setInputData(data)
            .build()

        enqueueChain(
            context,
            "lab-sync-pull",
            ExistingWorkPolicy.REPLACE,
            pullLabDataToAmrit
        )
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

    fun presTemplate(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<PrescripTemplateWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(PrescripTemplateWorker.name, ExistingWorkPolicy.KEEP, workRequest).state
    }

    fun earPushWorker(context: Context) {
        enqueueUpsync(context, UpsyncScope.EAR)
    }

    fun ophthalmicPushWorker(context: Context) {
        enqueueUpsync(context, UpsyncScope.OPHTHALMIC)
    }

    fun oralPushWorker(context: Context) {
        enqueueUpsync(context, UpsyncScope.ORAL)
    }

    fun painAssessmentPushWorker(context: Context) {
        enqueueUpsync(context, UpsyncScope.PAIN_ASSESSMENT)
    }

    fun psychosocialCaregiverSupport(context: Context) {
        enqueueUpsync(context, UpsyncScope.PSYCHOSOCIAL)
    }

    fun nosePushWorker(context: Context) {
        enqueueUpsync(context, UpsyncScope.NOSE)
    }

    fun throatPushWorker(context: Context) {
        enqueueUpsync(context, UpsyncScope.THROAT)
    }

    fun elderlyPushWorker(context: Context) {
        enqueueUpsync(context, UpsyncScope.ELDERLY)
    }

    fun mentalPushWorker(context: Context) {
        enqueueUpsync(context, UpsyncScope.MENTAL)
    }

    fun cancelAllWork(context: Context) {
        WorkManager.getInstance(context).cancelAllWork()
    }
}
