package org.piramalswasthya.cho

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
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

        // Global keyboard insets handler: ensures the layout resizes when the soft keyboard
        // opens on ALL activities. Required because targetSdk 35 enforces edge-to-edge,
        // which makes windowSoftInputMode="adjustResize" silently ignored.
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                val window = activity.window
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = false
                val statusBarBgView = View(activity).apply {
                    tag = "status_bar_bg"
                    setBackgroundColor(android.graphics.Color.parseColor("#24303C"))
                }

                val decorView = window.decorView as android.view.ViewGroup
                decorView.addView(
                    statusBarBgView,
                    android.widget.FrameLayout.LayoutParams(
                        android.widget.FrameLayout.LayoutParams.MATCH_PARENT, 0
                    )
                )

                val rootView = activity.findViewById<View>(android.R.id.content)
                ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
                    val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
                    val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    val bottomPadding = maxOf(imeInsets.bottom, systemBarInsets.bottom)
                    view.setPadding(
                        systemBarInsets.left,
                        systemBarInsets.top,
                        systemBarInsets.right,
                        bottomPadding
                    )
                    
                    statusBarBgView.layoutParams.height = systemBarInsets.top
                    statusBarBgView.requestLayout()
                    WindowInsetsCompat.CONSUMED
                }
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })

    }

    companion object {
        fun dataStore(context: Context) = (context.applicationContext as CHOApplication).dataStore
    }
}