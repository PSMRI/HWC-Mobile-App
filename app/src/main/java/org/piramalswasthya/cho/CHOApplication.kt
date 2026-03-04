package org.piramalswasthya.cho

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import org.piramalswasthya.cho.network.TokenRefreshHolder
import org.piramalswasthya.cho.network.TokenRefreshProvider
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

    val activityList = mutableListOf<Activity>()

    fun addActivity(activity: Activity) {
        activityList.add(activity)
    }

    fun closeAllActivities() {
        for (activity in activityList) {
            activity.finish()
        }
    }

    override fun onCreate() {

//        HttpLogger
        super.onCreate()
//        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Set token refresh provider so AuthRefreshInterceptor can refresh JWT when expired
        try {
            val entryPoint = EntryPointAccessors.fromApplication(this, TokenRefreshEntryPoint::class.java)
            TokenRefreshHolder.provider = entryPoint.tokenRefreshProvider()
        } catch (e: Exception) {
            Timber.w(e, "CHOApplication: Could not set TokenRefreshProvider")
        }
    }

    @dagger.hilt.EntryPoint
    @dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
    interface TokenRefreshEntryPoint {
        fun tokenRefreshProvider(): TokenRefreshProvider
    }

    companion object {
        fun dataStore(context: Context) = (context.applicationContext as CHOApplication).dataStore
    }
}