package org.piramalswasthya.cho

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import org.piramalswasthya.cho.ui.home_activity.DemoDataStore
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class CHOApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    private val dataStore by lazy { DemoDataStore(this) }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()


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

//         dataCaptureConfig =
//          DataCaptureConfig().apply {
//            urlResolver = ReferenceUrlResolver(this@CHOApplication as Context)
//          }
    }

    companion object {
        fun dataStore(context: Context) = (context.applicationContext as CHOApplication).dataStore
    }
}