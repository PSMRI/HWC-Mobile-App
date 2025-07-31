package org.piramalswasthya.cho.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.util.Log
import com.google.android.datatransport.BuildConfig
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import org.piramalswasthya.cho.R

class InAppUpdateHelper(
    private val activity: Activity,
    private val requestCode: Int = 123
) {
    private val TAG = "InAppUpdateHelper"
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            showUpdateDownloadedSnackbar()
        }
    }

    init {
        remoteConfig.setConfigSettingsAsync(
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = 0
            }
        )
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    fun checkForUpdate() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Remote Config fetch failed, using defaults.")
                }

                val currentVersionCode = BuildConfig.VERSION_CODE
                val latestVersionCode = try {
                    remoteConfig.getLong("latest_version_code").toInt()
                } catch (e: Exception) {
                    Log.e(TAG, "Invalid latest_version_code in Remote Config", e)
                    currentVersionCode
                }

                val forceUpdate = try {
                    remoteConfig.getBoolean("force_update")
                } catch (e: Exception) {
                    Log.e(TAG, "Invalid force_update flag in Remote Config", e)
                    false
                }

                if (currentVersionCode < latestVersionCode) {
                    val updateType = if (forceUpdate) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE
                    startUpdate(updateType)
                } else {
                    Log.d(TAG, "App is up-to-date.")
                }
            }
    }

    private fun startUpdate(updateType: Int) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            val isUpdateAvailable = appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val isAllowed = appUpdateInfo.isUpdateTypeAllowed(updateType)

            if (isUpdateAvailable && isAllowed) {
                try {
                    if (updateType == AppUpdateType.FLEXIBLE) {
                        appUpdateManager.registerListener(installStateUpdatedListener)
                    }

                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        updateType,
                        activity,
                        requestCode
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "Update flow failed to launch", e)
                    showPlayStoreFallback()
                }
            } else {
                Log.d(TAG, "Update not available or type not allowed.")
            }
        }.addOnFailureListener {
            Log.e(TAG, "Failed to get update info", it)
            showPlayStoreFallback()
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int): Boolean {
        if (requestCode == this.requestCode) {
            if (resultCode != Activity.RESULT_OK) {
                Log.w(TAG, "Update flow canceled or failed (code: $resultCode)")
                showRetrySnackbar()
            }
            return true
        }
        return false
    }

    fun resumeUpdateIfNeeded() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            when {
                appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED -> {
                    showUpdateDownloadedSnackbar()
                }

                appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            activity,
                            requestCode
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e(TAG, "Failed to resume update", e)
                        showPlayStoreFallback()
                    }
                }
            }
        }
    }

    private fun showUpdateDownloadedSnackbar() {
        Snackbar.make(
            activity.findViewById(android.R.id.content),
            "An update is ready to install.",
            Snackbar.LENGTH_INDEFINITE
        ).setAction("Restart") {
            appUpdateManager.completeUpdate()
        }.show()
    }

    private fun showRetrySnackbar() {
        Snackbar.make(
            activity.findViewById(android.R.id.content),
            "Update was canceled or failed.",
            Snackbar.LENGTH_LONG
        ).setAction("Try Again") {
            checkForUpdate()
        }.show()
    }

    private fun showPlayStoreFallback() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${activity.packageName}"))
            if (intent.resolveActivity(activity.packageManager) != null) {
                activity.startActivity(intent)
            }
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "Play Store not found", e)
        }
    }

    fun unregisterListener() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }
}