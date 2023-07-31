package org.piramalswasthya.cho.ui.home_activity

import android.content.Context
import androidx.work.WorkerParameters
import com.google.android.fhir.sync.AcceptLocalConflictResolver
import com.google.android.fhir.sync.DownloadWorkManager
import com.google.android.fhir.sync.FhirSyncWorker
import org.piramalswasthya.cho.CHOApplication

class DemoFhirSyncWorker(appContext: Context, workerParams: WorkerParameters) :
    FhirSyncWorker(appContext, workerParams) {

    override fun getDownloadWorkManager(): DownloadWorkManager {
        return TimestampBasedDownloadWorkManagerImpl(CHOApplication.dataStore(applicationContext))
    }

    override fun getConflictResolver() = AcceptLocalConflictResolver

    override fun getFhirEngine() = CHOApplication.fhirEngine(applicationContext)
}