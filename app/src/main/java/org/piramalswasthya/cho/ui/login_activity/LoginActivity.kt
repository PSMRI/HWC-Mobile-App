package org.piramalswasthya.cho.ui.login_activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.ActivityLoginBinding
import org.piramalswasthya.cho.helpers.MyContextWrapper
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.ui.commons.case_record.CaseRecordViewModel
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.ui.login_activity.username.UsernameFragment
import timber.log.Timber
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private var _binding: ActivityLoginBinding? = null
    private val binding: ActivityLoginBinding
        get() = _binding!!

    private val navController by lazy {
        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_login) as NavHostFragment
        navHostFragment.navController
    }
    private var showDashboard : Boolean? = null
    @Inject
    lateinit var userRepo: UserRepo

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WrapperEntryPoint {
        val preferenceDao: PreferenceDao
    }

    override fun attachBaseContext(newBase: Context) {
        val pref = EntryPointAccessors.fromApplication(
            newBase,
            WrapperEntryPoint::class.java
        ).preferenceDao
        super.attachBaseContext(MyContextWrapper.wrap(
            newBase,
            newBase.applicationContext,
            pref.getCurrentLanguage().symbol))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        print("app started")
        setUpActionBar()

//        binding.homeButton.setOnClickListener {
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//        }

        lifecycleScope.launch {
            try {
//                val isLoggedIn = userRepo.isUserLoggedIn() // Assuming this function returns 1 or 0
                if (userRepo.getLoggedInUser()!=null) {
                    showDashboard = true
                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    intent.putExtra("showDashboard", showDashboard)
                    startActivity(intent)
                    finish()
                }
            }catch (e:Exception){
                Log.d("Failed to get Login flag","${e}")
            }
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        NavigationUI.setupWithNavController(binding.toolbar, navController)
        NavigationUI.setupActionBarWithNavController(this, navController)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // This is the ID for the Up button (left arrow in the ActionBar).
                onBackPressed() // This will simulate the back button press.
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
//    private fun createSyncServiceNotificationChannel() {
//        // Create the NotificationChannel, but only on API 26+ because
//        // the NotificationChannel class is new and not in the support library
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val name = getString(R.string.notification_sync_channel_name)
//            val descriptionText = getString(R.string.notification_sync_channel_description)
//            val importance = NotificationManager.IMPORTANCE_DEFAULT
//            val channel = NotificationChannel(getString(R.string.notification_sync_channel_id), name, importance).apply {
//                description = descriptionText
//            }
//            // Register the channel with the system
//            val notificationManager: NotificationManager =
//                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//        }
//    }


}