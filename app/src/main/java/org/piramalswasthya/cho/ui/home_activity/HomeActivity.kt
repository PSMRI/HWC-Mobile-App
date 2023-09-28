package org.piramalswasthya.cho.ui.home_activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.ActivityHomeBinding
import org.piramalswasthya.cho.list.benificiaryList
import org.piramalswasthya.cho.model.PatientDetails
import org.piramalswasthya.cho.model.PatientListAdapter
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.cho.ui.login_activity.LoginActivity
import org.piramalswasthya.cho.ui.login_activity.login_settings.LoginSettingsFragment
import java.util.Calendar
import javax.inject.Inject


@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    private var _binding: ActivityHomeBinding? = null

    private val binding: ActivityHomeBinding
        get() = _binding!!
    private lateinit var navController: NavController

    private val viewModel: HomeActivityViewModel by viewModels()
    @Inject
    lateinit var prefDao: PreferenceDao

    @Inject
    lateinit var userRepo: UserRepo

    private var myLocation: Location? = null
    private var myInitialLoc: Location? = null
    private var locationManager: LocationManager? = null

    private var locationListener: LocationListener? = null
    val patientDetails = PatientDetails()
    private val handler = Handler()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.homeFragment) as NavHostFragment
        navController = navHostFragment.navController
        viewModel.updateLastSyncTimestamp()
        getCurrentLocation()
        setUpNavHeader()

        drawerLayout = binding.drawerLayout
        navigationView = binding.navView

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_dehaze_24)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        viewModel.navigateToLoginPage.observe(this) {
            if (it) {
                startActivity(Intent(this, LoginActivity::class.java))
                viewModel.navigateToLoginPageComplete()
                finish()
            }
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.abha_id_activity -> {
                    // Start the DestinationActivity
                    startActivity(Intent(this, AbhaIdActivity::class.java))
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.menu_logout -> {
                    logoutAlert.show()
                    true
                }
                else -> false
            }
        }
        // Create an ArrayList to hold your data
        val dataList = ArrayList<String>()
        dataList.add("Item 1")
        dataList.add("Item 2")
        dataList.add("Item 3")
        dataList.add("Item 3")
        dataList.add("Item 3")
        dataList.add("Item 3")
        dataList.add("Item 3")
        dataList.add("Item 3")

        // Create an ArrayAdapter to bind the data to the ListView
        var adapter = PatientListAdapter(this, benificiaryList)


        // Set the desired time (hour and minute) when you want the method to run
        val desiredHour = 17 // Change this to your desired hour (0-23)
        val desiredMinute = 0 // Change this to your desired minute (0-59)

        // Get the current time
        val currentTime = Calendar.getInstance()
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
        val currentMinute = currentTime.get(Calendar.MINUTE)

        // Calculate the delay until the desired time
        val delayMillis = calculateDelayMillis(currentHour, currentMinute, desiredHour, desiredMinute)

        // Schedule the method to run after the delay
        handler.postDelayed({
            // Call your method here
            myMethodToRunAtSpecificTime()

        }, delayMillis)

    }
    private val logoutAlert by lazy {
        MaterialAlertDialogBuilder(this).setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.please_confirm_to_logout))
            .setPositiveButton(getString(R.string.select_yes)) { dialog, _ ->
                viewModel.logout(myLocation,"By User")
                dialog.dismiss()
            }.setNegativeButton(getString(R.string.select_no)) { dialog, _ ->
                dialog.dismiss()
            }.create()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    private fun setUpNavHeader() {
        CoroutineScope(Dispatchers.IO).launch {
            val user = userRepo.getLoggedInUser()
            val headerView = binding.navView.getHeaderView(0)
            prefDao.getLoggedInUser()?.let {
                headerView.findViewById<TextView>(R.id.tv_nav_name).text =
                    getString(R.string.nav_item_1_text, user?.name)
                headerView.findViewById<TextView>(R.id.tv_nav_role).text =
                    getString(R.string.nav_item_2_text, user?.userName)
                headerView.findViewById<TextView>(R.id.tv_nav_id).text =
                    getString(R.string.nav_item_3_text, user?.userId)
            }
        }
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit the app?")
            .setPositiveButton("Yes") { _, _ ->
                // Close the app
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }


    private fun getCurrentLocation() {
        // Check if location permissions are granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            //  Location listener
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    myLocation = location
                    Log.i("Location From home is", "${myLocation!!.longitude}")
                }

                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

                override fun onProviderEnabled(provider: String) {}

                override fun onProviderDisabled(provider: String) {
                    Toast.makeText(
                        applicationContext, "Location Provider/GPS disabled",
                        Toast.LENGTH_SHORT
                    ).show()
                    val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(settingsIntent)
                }
            }

            // Request location updates
            locationManager?.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0,
                0f,
                locationListener!!
            )
        } else {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSION_REQUEST_CODE
            )
        }
    }
    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
        private const val MIN_TIME_BETWEEN_UPDATES: Long = 1000 // 1 second
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 10f // 10 meters
    }




private fun calculateDelayMillis(currentHour: Int, currentMinute: Int, desiredHour: Int, desiredMinute: Int): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, desiredHour)
    calendar.set(Calendar.MINUTE, desiredMinute)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val desiredTimeMillis = calendar.timeInMillis
    val currentTimeMillis = Calendar.getInstance().timeInMillis

    if (currentTimeMillis > desiredTimeMillis) {
        // The desired time is already in the past, schedule it for the next day
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }

    // Calculate the delay in milliseconds
    return calendar.timeInMillis - currentTimeMillis
}

private fun myMethodToRunAtSpecificTime() {
    // Your method code here
    viewModel.logout(myLocation,"By System")
}
}