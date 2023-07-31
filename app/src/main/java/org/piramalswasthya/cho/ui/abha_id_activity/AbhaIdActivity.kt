package org.piramalswasthya.cho.ui.abha_id_activity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.ActivityAbhaIdBinding
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

    private val exitAlert by lazy {
        MaterialAlertDialogBuilder(this)
            .setTitle("Exit")
            .setMessage(getString(R.string.confirm_go_back))
            .setPositiveButton("Yes") { _, _ ->
                navController.popBackStack()
                navController.navigate(R.id.aadhaarIdFragment)
            }
            .setNegativeButton("No") { d, _ ->
                d.dismiss()
            }
            .create()
    }

    private val exitActivityAlert by lazy {
        MaterialAlertDialogBuilder(this)
            .setTitle("Exit")
            .setMessage(getString(R.string.confirm_go_back))
            .setPositiveButton("Yes") { _, _ ->
                finish()
            }
            .setNegativeButton("No") { d, _ ->
                d.dismiss()
            }
            .create()
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        NavigationUI.setupWithNavController(binding.toolbar, navController)
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    override fun onDestroy() {
        super.onDestroy()
        TokenInsertAbhaInterceptor.setToken("")
        intent.removeExtra("benId")
        intent.removeExtra("benRegId")
    }
}