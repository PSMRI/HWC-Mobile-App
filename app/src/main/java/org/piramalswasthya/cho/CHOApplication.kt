package org.piramalswasthya.cho

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
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


    }

    companion object {
        fun dataStore(context: Context) = (context.applicationContext as CHOApplication).dataStore
    }
}