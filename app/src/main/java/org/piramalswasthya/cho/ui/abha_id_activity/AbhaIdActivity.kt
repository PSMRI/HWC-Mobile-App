package org.piramalswasthya.cho.ui.abha_id_activity

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.ActivityAbhaIdBinding
import org.piramalswasthya.cho.helpers.MyContextWrapper
import org.piramalswasthya.cho.network.interceptors.TokenInsertAbhaInterceptor
import org.piramalswasthya.cho.ui.abha_id_activity.AbhaIdViewModel.State

import timber.log.Timber

@AndroidEntryPoint
class AbhaIdActivity : AppCompatActivity() {

    private var _binding: ActivityAbhaIdBinding? = null
    private val binding: ActivityAbhaIdBinding
        get() = _binding!!

    private val mainViewModel: AbhaIdViewModel by viewModels()
    private val navController by lazy {
        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_abha_id) as NavHostFragment
        navHostFragment.navController
    }

    private var countDownTimer: CountDownTimer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate Called")
        _binding = ActivityAbhaIdBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpActionBar()

        mainViewModel.state.observe(this) { state ->
            when (state) {
                State.LOADING_TOKEN -> {
                    // Show progress bar
                    binding.progressBarAbhaActivity.visibility = View.VISIBLE
                    // Hide other views (if any)
                    binding.navHostFragmentAbhaId.visibility = View.GONE
                    binding.clError.visibility = View.GONE
                }

                State.SUCCESS -> {
                    binding.progressBarAbhaActivity.visibility = View.GONE
                    binding.clError.visibility = View.GONE
                    binding.navHostFragmentAbhaId.visibility = View.VISIBLE
                }

                State.ERROR_NETWORK -> {
                    binding.clError.visibility = View.VISIBLE
                    binding.progressBarAbhaActivity.visibility = View.GONE
                    binding.navHostFragmentAbhaId.visibility = View.GONE
                }

                State.ERROR_SERVER -> {
                    binding.clError.visibility = View.VISIBLE
                    binding.progressBarAbhaActivity.visibility = View.GONE
                    binding.navHostFragmentAbhaId.visibility = View.GONE
                }
            }
        }
        mainViewModel.errorMessage.observe(this) {
            binding.textView5.text = it
        }
        binding.btnTryAgain.setOnClickListener {
            mainViewModel.generateAccessToken()
        }

        binding.toolbarMenuHome.setOnClickListener {
            finish()
        }
        countDownTimer = object : CountDownTimer(30 * 60 * 1000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                binding.sessionTimer.text = formatMilliseconds(millisUntilFinished)
            }

            override fun onFinish() {
                finish()
            }
        }.start()
    }

    override fun onSupportNavigateUp(): Boolean {
        return when (navController.currentDestination?.id) {
            R.id.generateMobileOtpFragment -> {
                exitAlert.show()
                true
            }

            R.id.createAbhaFragment -> {
                exitActivityAlert.show()
                true
            }

            else -> {
                navController.popBackStack()
                navController.navigateUp() || super.onSupportNavigateUp()
            }
        }
    }

    fun updateActionBar(logoResource: Int, title: String? = null) {
//        binding.ivToolbarAbha.setImageResource(logoResource)
//        binding.toolbar.setLogo(logoResource)
        title?.let {
            binding.toolbar.title = null
            binding.tvToolbarAbha.text = it
        }
    }

    private val exitAlert by lazy {
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.exit))
            .setMessage(resources.getString(R.string.do_you_want_to_go_back))
            .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                navController.popBackStack()
                navController.navigate(R.id.aadhaarIdFragment)
            }
            .setNegativeButton(resources.getString(R.string.no)) { d, _ ->
                d.dismiss()
            }
            .create()
    }

    private val exitActivityAlert by lazy {
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.exit))
            .setMessage(resources.getString(R.string.do_you_want_to_go_back))
            .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                finish()
            }
            .setNegativeButton(resources.getString(R.string.no)) { d, _ ->
                d.dismiss()
            }
            .create()
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WrapperEntryPoint {
        val preferenceDao: PreferenceDao
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
//        NavigationUI.setupWithNavController(binding.toolbar, navController)
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    override fun onDestroy() {
        super.onDestroy()
        TokenInsertAbhaInterceptor.setToken("")
        TokenInsertAbhaInterceptor.setXToken("")
        intent.removeExtra("benId")
        intent.removeExtra("benRegId")
        countDownTimer?.cancel()
    }

    fun formatMilliseconds(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60

        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    override fun attachBaseContext(newBase: Context) {
        val pref = EntryPointAccessors.fromApplication(
            newBase,
            WrapperEntryPoint::class.java
        ).preferenceDao
        super.attachBaseContext(
            MyContextWrapper.wrap(
                newBase,
                newBase.applicationContext,
                pref.getCurrentLanguage().symbol
            )
        )
    }
}