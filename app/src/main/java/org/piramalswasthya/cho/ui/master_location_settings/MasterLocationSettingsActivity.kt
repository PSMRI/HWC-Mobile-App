package org.piramalswasthya.cho.ui.master_location_settings

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.ActivityRegisterPatientBinding
import org.piramalswasthya.cho.databinding.ActivityUserMasterLocationBinding


@AndroidEntryPoint
class MasterLocationSettingsActivity : AppCompatActivity() {
    private var _binding: ActivityUserMasterLocationBinding? = null

    private val binding: ActivityUserMasterLocationBinding
        get() = _binding!!
    private lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityUserMasterLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navHostFragment = supportFragmentManager.findFragmentById(binding.navHostUserLocation.id) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(R.id.loginSettingsFragmentMaster)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}
