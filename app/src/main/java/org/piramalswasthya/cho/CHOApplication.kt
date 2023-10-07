package org.piramalswasthya.cho

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.android.datatransport.BuildConfig
import com.google.android.fhir.DatabaseErrorStrategy
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.FhirEngineConfiguration
import com.google.android.fhir.FhirEngineProvider
import com.google.android.fhir.NetworkConfiguration
import com.google.android.fhir.ServerConfiguration
import com.google.android.fhir.datacapture.DataCaptureConfig
import com.google.android.fhir.datacapture.XFhirQueryResolver
import com.google.android.fhir.search.search
import com.google.android.fhir.sync.remote.HttpLogger


import dagger.hilt.android.HiltAndroidApp
import org.piramalswasthya.cho.ui.home_activity.DemoDataStore
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class CHOApplication : Application(), Configuration.Provider,DataCaptureConfig.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    private val dataStore by lazy { DemoDataStore(this) }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()


    private val fhirEngine: FhirEngine by lazy { constructFhirEngine() }

    private var dataCaptureConfig: DataCaptureConfig? = null

//    private var dataCaptureConfig: DataCaptureConfig? = DataCaptureConfig(xFhirQueryResolver = {fhirEngine.search(it) })

    override fun onCreate() {

//        HttpLogger
        super.onCreate()
//        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
//        }
//        FhirEngineProvider.init(
//            FhirEngineConfiguration(
//                enableEncryptionIfSupported = true,
//                DatabaseErrorStrategy.RECREATE_AT_OPEN,
//                ServerConfiguration(
//                    "https://hapi.fhir.org/baseR4/",
//                    httpLogger =
//                        HttpLogger(
//                            HttpLogger.Configuration(
//                                if (BuildConfig.DEBUG) HttpLogger.Level.BODY else HttpLogger.Level.BASIC
//                            )
//                        ) { Timber.tag("App-HttpLog").d(it) },
//                    networkConfiguration = NetworkConfiguration()
//                )
//            )
//        )

         dataCaptureConfig =
          DataCaptureConfig().apply {
            urlResolver = ReferenceUrlResolver(this@CHOApplication as Context)
            xFhirQueryResolver = XFhirQueryResolver { fhirEngine.search(it) }
          }
    }

    private fun constructFhirEngine(): FhirEngine {
        return FhirEngineProvider.getInstance(this)
    }

    companion object {
        fun fhirEngine(context: Context) = (context.applicationContext as CHOApplication).fhirEngine

        fun dataStore(context: Context) = (context.applicationContext as CHOApplication).dataStore
    }

    override fun getDataCaptureConfig(): DataCaptureConfig = dataCaptureConfig ?: DataCaptureConfig()
    object ServerConstants {
        const val BASE_URL = "http://10.0.2.2:8080/"
    }
}