package org.piramalswasthya.cho.ui.commons

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class LocationHelper(private val context: Context,private val callback: LocationUpdateCallback) {

    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null

    fun getCurrentLocation(fragment: FragmentActivity?) {
        // Check if location permissions are granted
        if (checkLocationPermissions()) {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // Location listener
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    // Handle the location update here
                    callback.onLocationUpdated(location)
                    // Stop listening for location updates once you have the current location
                    locationManager?.removeUpdates(this)
                }

                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

                override fun onProviderEnabled(provider: String) {}

                override fun onProviderDisabled(provider: String) {
                    Toast.makeText(
                        context, "Location Provider/GPS disabled",
                        Toast.LENGTH_SHORT
                    ).show()
                    val settingsIntent = Settings.ACTION_LOCATION_SOURCE_SETTINGS
                    context.startActivity(Intent(settingsIntent))
                }
            }

            // Request location updates
            if (ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
              requestLocationPermissions(fragment!!)
            }
            else {
                locationManager?.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0,
                    0f,
                    locationListener!!
                )
            }
        } else {
            // Request location permissions if not granted
            requestLocationPermissions(fragment!!)
        }
    }

    private fun checkLocationPermissions(): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return fineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions(fragment: FragmentActivity) {
        // Request location permissions if not granted
        fragment.requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    companion object {
        const val PERMISSION_REQUEST_CODE = 123 // Define your request code here
    }
}

interface LocationUpdateCallback {
    fun onLocationUpdated(location: Location)
}

